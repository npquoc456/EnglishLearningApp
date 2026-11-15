package com.example.laptrinhungdungdidong.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var pendingText: String? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val supported = trySetLanguage(Locale.US) ||
                            trySetLanguage(Locale.UK) ||
                            trySetLanguage(Locale.ENGLISH)

            if (supported) {
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.0f)
                isReady = true
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            } else {
                Log.w("TtsHelper", "English language not supported by TTS engine.")
            }
        } else {
            Log.e("TtsHelper", "Failed to initialize TTS engine, status: $status")
        }
    }

    private fun trySetLanguage(locale: Locale): Boolean {
        val result = tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String) {
        val cleanText = text.trim()
            .replace(Regex("[:;,.!?]+$"), "")
            .trim()

        if (cleanText.isBlank()) return

        if (!isReady) {
            pendingText = cleanText
            return
        }

        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "tts_word_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("TtsHelper", "Error speaking text: ${e.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isReady = false
    }
}
