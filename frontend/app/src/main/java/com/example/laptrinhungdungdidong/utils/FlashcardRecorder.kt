package com.example.laptrinhungdungdidong.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Records and replays short flashcard pronunciation clips locally on the phone. */
class FlashcardRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    val isRecording: Boolean get() = recorder != null

    fun start(file: File) {
        stopPlayback()
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(64000)
            r.setAudioSamplingRate(16000)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
        } catch (e: Exception) {
            r.release()
            file.delete()
            throw e
        }
    }

    fun stop(): Boolean {
        val current = recorder ?: return false
        recorder = null
        return try {
            current.stop()
            true
        } catch (_: RuntimeException) {
            false // Very short recording can fail with Android MediaRecorder.
        } finally {
            current.reset()
            current.release()
        }
    }

    fun play(file: File, onComplete: () -> Unit) {
        stopPlayback()
        val p = MediaPlayer()
        try {
            p.setDataSource(file.absolutePath)
            p.setOnCompletionListener {
                stopPlayback()
                onComplete()
            }
            p.prepare()
            p.start()
            player = p
        } catch (e: Exception) {
            p.release()
            throw e
        }
    }

    fun stopPlayback() {
        val playing = player
        player = null
        try { playing?.stop() } catch (_: Exception) { }
        try { playing?.release() } catch (_: Exception) { }
    }

    fun release() {
        if (isRecording) stop()
        stopPlayback()
    }
}
