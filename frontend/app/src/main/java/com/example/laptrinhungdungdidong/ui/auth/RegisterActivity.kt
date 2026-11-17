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
import com.example.laptrinhungdungdidong.data.models.UserRegisterRequest
import com.example.laptrinhungdungdidong.data.models.UserResponse
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.btnNavLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val fullName = binding.etRegisterFullName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etRegisterFullName.error = "Vui lòng nhập họ và tên"
            return
        }
        if (email.isEmpty()) {
            binding.etRegisterEmail.error = "Vui lòng nhập email"
            return
        }
        if (password.length < 6) {
            binding.etRegisterPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            return
        }
        if (password != confirmPassword) {
            binding.etRegisterConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        binding.tvRegisterError.visibility = View.GONE
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Đang đăng ký..."

        lifecycleScope.launch {
            try {
                val req = UserRegisterRequest(fullName, email, password)
                val response = ApiClient.service.register(req)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.saveUser(user)
                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    showError("Email đã tồn tại hoặc không hợp lệ")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                showError("Không kết nối được máy chủ. Kiểm tra Wi-Fi và backend rồi thử lại.")
            } finally {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Đăng ký tài khoản"
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvRegisterError.text = msg
        binding.tvRegisterError.visibility = View.VISIBLE
    }
}
