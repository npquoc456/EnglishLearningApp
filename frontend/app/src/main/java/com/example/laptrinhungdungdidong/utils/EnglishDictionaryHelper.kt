package com.example.laptrinhungdungdidong.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WordSuggestion(
    val term: String,
    val definitions: List<String>,
    val phonetic: String? = null,
    val wordType: String? = null
)

/**
 * Helper tra cứu từ điển và dịch nghĩa tự động thông minh bằng Google Translate.
 * Kết hợp:
 * 1. Google ML Kit On-Device Translation (miễn phí, hoạt động trực tiếp trên máy).
 * 2. Dịch nghĩa & từ loại mở rộng từ Google Translate Web API & Dictionary API.
 * 3. Bộ nhớ đệm LRU Cache để phản hồi tức thì dưới 1ms cho các từ đã tra cứu.
 */
object EnglishDictionaryHelper {

    private const val TAG = "GoogleTranslateHelper"
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val mainHandler = Handler(Looper.getMainLooper())

    // Bộ nhớ đệm lưu trữ kết quả dịch
    private val cache = LruCache<String, WordSuggestion>(300)

    // Google ML Kit Translator
    private var mlKitTranslator: Translator? = null
    private var isModelReady = false

    init {
        initMlKitTranslator()
    }

    private fun initMlKitTranslator() {
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build()
            mlKitTranslator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder().build()
            mlKitTranslator?.downloadModelIfNeeded(conditions)
                ?.addOnSuccessListener {
                    isModelReady = true
                    Log.d(TAG, "Google ML Kit Translate model downloaded & ready.")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "ML Kit model download pending or error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ML Kit Translator: ${e.message}")
        }
    }

    /**
     * Tra cứu từ vựng bất kỳ bằng Google Dịch (bất đồng bộ, an toàn trên Main Thread).
     */
    fun lookup(query: String, onResult: (WordSuggestion?) -> Unit) {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) {
            onResult(null)
            return
        }

        // 1. Kiểm tra cache để trả về ngay lập tức nếu đã từng tra cứu
        val cached = cache.get(clean)
        if (cached != null) {
            onResult(cached)
            return
        }

        // 2. Chạy dịch nền đa nguồn (Google Translate Web + Dictionary API + ML Kit)
        scope.launch {
            val definitions = mutableListOf<String>()
            var wordType: String? = null
            var phonetic: String? = null

            // A. Thử gọi Google Translate Web API để lấy nghĩa chính + danh sách nghĩa mở rộng
            try {
                val encoded = URLEncoder.encode(clean, "UTF-8")
                val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=vi&dt=t&dt=bd&q=$encoded")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                }
                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONArray(response)

                    // Lấy bản dịch chính
                    if (json.length() > 0 && !json.isNull(0)) {
                        val array0 = json.getJSONArray(0)
                        val sb = StringBuilder()
                        for (i in 0 until array0.length()) {
                            val part = array0.getJSONArray(i)
                            if (part.length() > 0 && !part.isNull(0)) {
                                sb.append(part.getString(0))
                            }
                        }
                        val primary = sb.toString().trim()
                        if (primary.isNotEmpty()) {
                            definitions.add(primary)
                        }
                    }

                    // Lấy danh sách các nghĩa chi tiết theo từ điển
                    if (json.length() > 1 && !json.isNull(1)) {
                        val array1 = json.getJSONArray(1)
                        for (i in 0 until array1.length()) {
                            val entry = array1.getJSONArray(i)
                            if (wordType == null && entry.length() > 0 && !entry.isNull(0)) {
                                wordType = entry.getString(0)
                            }
                            if (entry.length() > 1 && !entry.isNull(1)) {
                                val defs = entry.getJSONArray(1)
                                for (j in 0 until defs.length()) {
                                    val d = defs.getString(j).trim()
                                    if (d.isNotEmpty() && !definitions.contains(d)) {
                                        definitions.add(d)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Google Translate Web API call failed: ${e.message}")
            }

            // B. Tra cứu phiên âm IPA và từ loại từ Dictionary API (nếu chưa có)
            try {
                val encodedWord = URLEncoder.encode(clean.split(" ").firstOrNull() ?: clean, "UTF-8")
                val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$encodedWord")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 2500
                    readTimeout = 2500
                }
                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val array = JSONArray(response)
                    if (array.length() > 0) {
                        val firstObj = array.getJSONObject(0)
                        if (firstObj.has("phonetic") && !firstObj.isNull("phonetic")) {
                            phonetic = firstObj.getString("phonetic")
                        } else if (firstObj.has("phonetics")) {
                            val phoneticsArr = firstObj.getJSONArray("phonetics")
                            for (p in 0 until phoneticsArr.length()) {
                                val pObj = phoneticsArr.getJSONObject(p)
                                if (pObj.has("text") && !pObj.isNull("text")) {
                                    phonetic = pObj.getString("text")
                                    break
                                }
                            }
                        }

                        if (wordType == null && firstObj.has("meanings")) {
                            val meanings = firstObj.getJSONArray("meanings")
                            if (meanings.length() > 0) {
                                val firstMeaning = meanings.getJSONObject(0)
                                if (firstMeaning.has("partOfSpeech")) {
                                    wordType = firstMeaning.getString("partOfSpeech")
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            // C. Nếu Web API không trả về nghĩa (mất mạng hoặc lỗi 429), dùng Google ML Kit On-Device
            if (definitions.isEmpty()) {
                val mlResult = translateWithMlKitSuspend(clean)
                if (!mlResult.isNullOrBlank()) {
                    definitions.add(mlResult.trim())
                }
            }

            // D. Đóng gói kết quả
            if (definitions.isNotEmpty()) {
                val result = WordSuggestion(
                    term = clean,
                    definitions = definitions.take(8), // Lấy tối đa 8 nghĩa phổ biến nhất
                    phonetic = phonetic,
                    wordType = wordType
                )
                cache.put(clean, result)
                mainHandler.post { onResult(result) }
            } else {
                mainHandler.post { onResult(null) }
            }
        }
    }

    /**
     * Dịch nhanh một đoạn văn bản bất kỳ bằng Google ML Kit
     */
    private suspend fun translateWithMlKitSuspend(text: String): String? = withContext(Dispatchers.IO) {
        val t = mlKitTranslator ?: return@withContext null
        try {
            kotlin.coroutines.suspendCoroutine { cont ->
                val conditions = DownloadConditions.Builder().build()
                t.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        t.translate(text)
                            .addOnSuccessListener { translated ->
                                cont.resumeWith(Result.success(translated))
                            }
                            .addOnFailureListener {
                                cont.resumeWith(Result.success(null))
                            }
                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.success(null))
                    }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Dịch trực tiếp từ tiếng Anh sang tiếng Việt (tiện cho nút bấm '🌐 Google Dịch')
     */
    fun translateDirect(query: String, onResult: (String?) -> Unit) {
        val clean = query.trim()
        if (clean.isEmpty()) {
            onResult(null)
            return
        }
        lookup(clean) { suggestion ->
            onResult(suggestion?.definitions?.firstOrNull())
        }
    }
}
