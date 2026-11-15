package com.example.laptrinhungdungdidong.ui.deck

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.CardCreateRequest
import com.example.laptrinhungdungdidong.data.models.DeckCreateRequest
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.ActivityCreateDeckBinding
import com.example.laptrinhungdungdidong.databinding.ItemCreateCardRowBinding
import com.example.laptrinhungdungdidong.utils.EnglishDictionaryHelper
import com.example.laptrinhungdungdidong.utils.TtsHelper
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File

class CreateDeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateDeckBinding
    private lateinit var ttsHelper: TtsHelper
    private val cardViews = mutableListOf<ItemCreateCardRowBinding>()
    private var cameraImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            processImageWithOcr(cameraImageUri!!)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            processImageWithOcr(uri)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Cần cấp quyền camera để chụp tài liệu từ vựng", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateDeckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsHelper = TtsHelper(this)

        // Handle Status Bar & Navigation Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.topBar.updatePadding(top = systemBars.top)
            binding.bottomActions.updatePadding(bottom = systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClose.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        // Start with 1 clean empty card row for the new user
        addCardRow("", "", "", "", "", "")

        binding.btnAddCard.setOnClickListener {
            addCardRow("", "", "", "", "", "")
        }

        // Camera OCR buttons
        binding.btnOpenCamera.setOnClickListener { checkPermissionAndLaunchCamera() }
        binding.btnMethodCamera.setOnClickListener { checkPermissionAndLaunchCamera() }
        binding.btnSelectPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Paste text dialog
        binding.btnMethodPaste.setOnClickListener { showPasteTextDialog() }

        binding.btnSubmitCreateDeck.setOnClickListener {
            submitDeck()
        }
    }

    private fun checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File.createTempFile("ocr_camera_", ".jpg", cacheDir)
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể khởi động camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImageWithOcr(uri: Uri) {
        Toast.makeText(this, "Đang quét từ vựng bằng AI OCR...", Toast.LENGTH_SHORT).show()
        try {
            val inputImage = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    if (text.isBlank()) {
                        Toast.makeText(this, "Không tìm thấy văn bản trong hình ảnh", Toast.LENGTH_LONG).show()
                    } else {
                        parseAndPopulateExtractedText(text)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Lỗi nhận diện văn bản: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi xử lý ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasteTextDialog() {
        val input = EditText(this).apply {
            hint = "Ví dụ:\nhome: nhà\npencil: bút chì\napple: quả táo"
            minLines = 6
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Dán danh sách từ vựng")
            .setMessage("Mỗi dòng 1 từ kèm nghĩa (ngăn cách bởi dấu hai chấm : hoặc gạch ngang -)")
            .setView(input)
            .setPositiveButton("Trích xuất thành thẻ") { _, _ ->
                val pasted = input.text.toString()
                if (pasted.isNotBlank()) {
                    parseAndPopulateExtractedText(pasted)
                }
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun parseAndPopulateExtractedText(rawText: String) {
        val lines = rawText.split("\n")
        val pairs = mutableListOf<Pair<String, String>>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Split by : or - or – or =
            val parts = trimmed.split(Regex("[:–\\-=]"), limit = 2)
            if (parts.size >= 2) {
                val term = parts[0].trim()
                val def = parts[1].trim()
                if (term.isNotEmpty() && def.isNotEmpty()) {
                    pairs.add(Pair(term, def))
                }
            } else {
                // If line contains multiple spaces / tabs
                val spaced = trimmed.split(Regex("\\s{2,}|\t"))
                if (spaced.size >= 2 && spaced[0].isNotBlank() && spaced[1].isNotBlank()) {
                    pairs.add(Pair(spaced[0].trim(), spaced[1].trim()))
                }
            }
        }

        if (pairs.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy cặp từ : nghĩa hợp lệ (VD: home: nhà)", Toast.LENGTH_LONG).show()
            return
        }

        // If the first card row is still completely empty, remove it so we don't have an empty first card
        if (cardViews.size == 1) {
            val first = cardViews.first()
            if (first.etTerm.text.isNullOrBlank() && first.etDefinition.text.isNullOrBlank()) {
                binding.llCardsContainer.removeView(first.root)
                cardViews.clear()
            }
        }

        // Auto populate all scanned cards
        for (pair in pairs) {
            addCardRow(
                term = pair.first,
                definition = pair.second,
                phonetic = "",
                wordType = "noun",
                example = "",
                synonyms = ""
            )
        }

        Toast.makeText(this, "Đã thêm thành công ${pairs.size} thẻ từ vựng vào bộ thẻ!", Toast.LENGTH_LONG).show()
    }

    private fun addCardRow(
        term: String,
        definition: String,
        phonetic: String,
        wordType: String,
        example: String,
        synonyms: String
    ) {
        val index = cardViews.size + 1
        val rowBinding = ItemCreateCardRowBinding.inflate(LayoutInflater.from(this), binding.llCardsContainer, false)

        rowBinding.tvCardIndexBadge.text = index.toString()
        rowBinding.tvCardTitle.text = "Thẻ số $index"
        rowBinding.etTerm.setText(term)
        rowBinding.etDefinition.setText(definition)
        rowBinding.etPhonetic.setText(phonetic)
        rowBinding.etWordType.setText(wordType)
        rowBinding.etExample.setText(example)
        rowBinding.etSynonyms.setText(synonyms)

        // Gợi ý định nghĩa tiếng Việt tự động khi người dùng gõ từ tiếng Anh bằng Google Dịch
        var searchJob: Runnable? = null
        val searchHandler = android.os.Handler(android.os.Looper.getMainLooper())

        val performLookup = { query: String ->
            EnglishDictionaryHelper.lookup(query) { suggestion ->
                if (!isFinishing && !isDestroyed) {
                    val currentText = rowBinding.etTerm.text.toString().trim()
                    if (currentText.equals(query, ignoreCase = true) && suggestion != null && suggestion.definitions.isNotEmpty()) {
                        rowBinding.layoutMeaningSuggestions.visibility = android.view.View.VISIBLE
                        rowBinding.chipGroupSuggestions.removeAllViews()

                        for (def in suggestion.definitions) {
                            val chip = Chip(this@CreateDeckActivity).apply {
                                text = def
                                textSize = 12f
                                chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EEF2FF"))
                                setTextColor(android.graphics.Color.parseColor("#9C1C40"))
                                chipStrokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E7FF"))
                                chipStrokeWidth = 2f
                                isClickable = true
                                isCheckable = false

                                setOnClickListener {
                                    rowBinding.etDefinition.setText(def)
                                    if (rowBinding.etPhonetic.text.isNullOrBlank() && suggestion.phonetic != null) {
                                        rowBinding.etPhonetic.setText(suggestion.phonetic)
                                    }
                                    if (rowBinding.etWordType.text.isNullOrBlank() && suggestion.wordType != null) {
                                        rowBinding.etWordType.setText(suggestion.wordType)
                                    }
                                    rowBinding.layoutMeaningSuggestions.visibility = android.view.View.GONE
                                }
                            }
                            rowBinding.chipGroupSuggestions.addView(chip)
                        }
                    } else if (currentText.isEmpty()) {
                        rowBinding.layoutMeaningSuggestions.visibility = android.view.View.GONE
                    }
                }
            }
        }

        rowBinding.etTerm.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                searchJob?.let { searchHandler.removeCallbacks(it) }

                if (query.isEmpty()) {
                    rowBinding.layoutMeaningSuggestions.visibility = android.view.View.GONE
                    return
                }

                // Debounce 250ms để không gọi liên tục khi người dùng đang gõ phím
                val job = Runnable { performLookup(query) }
                searchJob = job
                searchHandler.postDelayed(job, 250)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Nút dịch nhanh bằng Google Dịch
        rowBinding.btnQuickTranslate.setOnClickListener {
            val word = rowBinding.etTerm.text.toString().trim()
            if (word.isBlank()) {
                Toast.makeText(this, "Vui lòng nhập thuật ngữ tiếng Anh trước", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Đang dịch bằng Google Dịch...", Toast.LENGTH_SHORT).show()
            EnglishDictionaryHelper.lookup(word) { suggestion ->
                if (!isFinishing && !isDestroyed && suggestion != null) {
                    val def = suggestion.definitions.firstOrNull()
                    if (!def.isNullOrBlank()) {
                        rowBinding.etDefinition.setText(def)
                    }
                    if (rowBinding.etPhonetic.text.isNullOrBlank() && suggestion.phonetic != null) {
                        rowBinding.etPhonetic.setText(suggestion.phonetic)
                    }
                    if (rowBinding.etWordType.text.isNullOrBlank() && suggestion.wordType != null) {
                        rowBinding.etWordType.setText(suggestion.wordType)
                    }
                    Toast.makeText(this, "Đã dịch thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Không tìm thấy bản dịch", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Text-to-speech button to pronounce English term
        rowBinding.btnSpeakCardTerm.setOnClickListener {
            val word = rowBinding.etTerm.text.toString().trim()
            if (word.isNotBlank()) {
                ttsHelper.speak(word)
            } else {
                Toast.makeText(this, "Vui lòng nhập thuật ngữ tiếng Anh để nghe phát âm", Toast.LENGTH_SHORT).show()
            }
        }

        rowBinding.btnDeleteCard.setOnClickListener {
            if (cardViews.size > 1) {
                binding.llCardsContainer.removeView(rowBinding.root)
                cardViews.remove(rowBinding)
                updateCardIndices()
            } else {
                Toast.makeText(this, "Bộ thẻ cần ít nhất 1 thẻ từ vựng", Toast.LENGTH_SHORT).show()
            }
        }

        cardViews.add(rowBinding)
        binding.llCardsContainer.addView(rowBinding.root)
        updateHeaderAndButton()
    }

    private fun updateCardIndices() {
        cardViews.forEachIndexed { idx, row ->
            val num = idx + 1
            row.tvCardIndexBadge.text = num.toString()
            row.tvCardTitle.text = "Thẻ số $num"
        }
        updateHeaderAndButton()
    }

    private fun updateHeaderAndButton() {
        val count = cardViews.size
        binding.tvCardsListHeader.text = "DANH SÁCH THẺ TỪ VỰNG ($count)"
        binding.btnSubmitCreateDeck.text = "✓  Tạo bộ thẻ ($count từ)"
    }

    private fun submitDeck() {
        val title = binding.etDeckTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etDeckTitle.error = "Vui lòng nhập tên bộ thẻ"
            return
        }

        val validCards = cardViews.mapNotNull { row ->
            val t = row.etTerm.text.toString().trim()
            val d = row.etDefinition.text.toString().trim()
            val p = row.etPhonetic.text.toString().trim()
            val w = row.etWordType.text.toString().trim()
            if (t.isNotEmpty() && d.isNotEmpty()) {
                CardCreateRequest(
                    deckId = 0L,
                    term = t,
                    definition = d,
                    ipaPronunciation = p.ifEmpty { null },
                    wordType = w.ifEmpty { null }
                )
            } else null
        }

        if (validCards.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ít nhất 1 thuật ngữ và định nghĩa hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val user = SessionManager(this).getUser()
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitCreateDeck.isEnabled = false
        binding.btnSubmitCreateDeck.text = "Đang lưu bộ thẻ..."

        lifecycleScope.launch {
            try {
                val deckReq = DeckCreateRequest(
                    userId = user.id,
                    title = title,
                    categoryTag = "Từ vựng",
                    visibility = "PUBLIC"
                )
                val deckRes = ApiClient.service.createDeck(deckReq)
                if (deckRes.isSuccessful && deckRes.body() != null) {
                    val createdDeck = deckRes.body()!!
                    for ((idx, cardReq) in validCards.withIndex()) {
                        val cReq = CardCreateRequest(
                            deckId = createdDeck.id,
                            term = cardReq.term,
                            definition = cardReq.definition,
                            ipaPronunciation = cardReq.ipaPronunciation,
                            wordType = cardReq.wordType,
                            orderIndex = idx + 1
                        )
                        ApiClient.service.createCard(cReq)
                    }
                    Toast.makeText(this@CreateDeckActivity, "Tạo bộ thẻ thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateDeckActivity, "Không thể lưu bộ thẻ lên server", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateDeckActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSubmitCreateDeck.isEnabled = true
                updateHeaderAndButton()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
    }
}
