package com.example.laptrinhungdungdidong.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.laptrinhungdungdidong.MainActivity
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.UserLoginRequest
import com.example.laptrinhungdungdidong.data.models.UserResponse
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // If already logged in, navigate straight to MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMainActivity()
            return
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnQuickLogin.setOnClickListener {
            binding.etLoginEmail.setText("minh.hoang@example.com")
            binding.etLoginPassword.setText("password123")
            performLogin()
        }

        binding.btnNavRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etLoginEmail.error = "Vui lòng nhập email"
            return
        }
        if (password.isEmpty()) {
            binding.etLoginPassword.error = "Vui lòng nhập mật khẩu"
            return
        }

        binding.tvLoginError.visibility = View.GONE
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Đang đăng nhập..."

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.login(UserLoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUser(user)
                    Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    showError("Email hoặc mật khẩu không chính xác")
                }
            } catch (e: Exception) {
                // If backend is still connecting or unreachable, allow demo login with sample user
                val fallbackUser = UserResponse(
                    id = 1L,
                    fullName = "Hoàng Minh",
                    email = email,
                    avatarUrl = null,
                    currentStreak = 5,
                    lastStudyDate = "2026-11-15",
                    defaultStudyDirection = null,
                    dailyNewCardsLimit = 20,
                    autoPlayAudio = true,
                    reminderTime = "20:30",
                    createdAt = null
                )
                sessionManager.saveUser(fallbackUser)
                Toast.makeText(this@LoginActivity, "Đăng nhập chế độ Offline", Toast.LENGTH_SHORT).show()
                goToMainActivity()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Đăng nhập"
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvLoginError.text = msg
        binding.tvLoginError.visibility = View.VISIBLE
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
