package com.example.laptrinhungdungdidong.utils

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.example.laptrinhungdungdidong.data.api.ApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AvatarHelper {
    fun load(userId: Long, imageView: ImageView, scope: CoroutineScope) {
        scope.launch {
            try {
                val response = ApiClient.service.getAvatar(userId)
                if (!response.isSuccessful) return@launch
                val bytes = withContext(Dispatchers.IO) { response.body()?.use { it.bytes() } }
                if (bytes != null && bytes.size <= 1024 * 1024) {
                    val bitmap = withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    if (bitmap != null && imageView.isAttachedToWindow) {
                        imageView.setImageBitmap(bitmap)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        imageView.setPadding(0, 0, 0, 0)
                        imageView.clipToOutline = true
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { /* Default profile icon remains visible offline. */ }
        }
    }
}
