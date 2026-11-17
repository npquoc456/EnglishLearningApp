package com.example.laptrinhungdungdidong.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject
import com.example.laptrinhungdungdidong.data.models.UserResponse
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("flashcard_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USER = "key_logged_user"
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    }

    fun saveUser(user: UserResponse) {
        val userJson = gson.toJson(user.copy(accessToken = null))
        val editor = prefs.edit()
            .putString(KEY_USER, userJson)
            .putBoolean(KEY_IS_LOGGED_IN, true)
        if (!user.accessToken.isNullOrBlank()) {
            editor.putString(KEY_ACCESS_TOKEN, user.accessToken)
        }
        editor.apply()
    }

    fun getUser(): UserResponse? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, UserResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun getAuthToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun isLoggedIn(): Boolean {
        // A stale JWT should go back to the login screen rather than failing every API request.
        val token = getAuthToken() ?: return false
        val notExpired = try {
            val pieces = token.split('.')
            if (pieces.size != 3) false else {
                val claims = String(Base64.decode(pieces[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
                val expiresAt = JSONObject(claims).optLong("exp", 0)
                expiresAt > System.currentTimeMillis() / 1000 + 30
            }
        } catch (_: Exception) {
            false
        }
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUser() != null && notExpired
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
