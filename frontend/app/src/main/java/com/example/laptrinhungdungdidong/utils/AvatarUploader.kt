package com.example.laptrinhungdungdidong.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.laptrinhungdungdidong.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object AvatarUploader {
    suspend fun upload(context: Context, userId: Long, uri: Uri): Boolean {
        val bytes = withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 720 || bounds.outHeight / sample > 720) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: throw IllegalArgumentException("Không đọc được ảnh")
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            bitmap.recycle()
            out.toByteArray()
        }
        require(bytes.isNotEmpty() && bytes.size <= 1024 * 1024) { "Ảnh phải nhỏ hơn 1 MB" }
        val part = MultipartBody.Part.createFormData(
            "image", "avatar.jpg", bytes.toRequestBody("image/jpeg".toMediaType())
        )
        return ApiClient.service.uploadAvatar(userId, part).isSuccessful
    }
}
