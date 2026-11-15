package com.example.laptrinhungdungdidong.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.laptrinhungdungdidong.R
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.UserUpdateRequest
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.FragmentProfileBinding
import com.example.laptrinhungdungdidong.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = sessionManager.getUser()
        if (user != null) {
            binding.tvFullName.text = user.fullName ?: "Hoàng Minh"
            binding.tvEmail.text = user.email ?: "minh.hoang@example.com"
            val streak = user.currentStreak ?: 0
            binding.tvProfileStreak.text = "$streak ngày"
            binding.tvStreakCount.text = "🔥 $streak"

            val limit = user.dailyNewCardsLimit ?: 20
            binding.sbDailyLimit.progress = limit
            binding.tvDailyCardLimit.text = "$limit thẻ/ngày"
            binding.swAutoAudio.isChecked = user.autoPlayAudio ?: true

            // Fetch user cards/sessions to calculate real stats
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val sessionsRes = ApiClient.service.getSessionsByUserId(user.id)
                    if (sessionsRes.isSuccessful && sessionsRes.body() != null && sessionsRes.body()!!.isNotEmpty()) {
                        val sessions = sessionsRes.body()!!
                        val totalMastered = sessions.sumOf { it.correctCards }
                        val xp = totalMastered * 10
                        binding.tvMasteredStat.text = "🎴 $totalMastered"
                        binding.tvXpStat.text = "⚡ $xp"
                    } else {
                        binding.tvMasteredStat.text = "🎴 0"
                        binding.tvXpStat.text = "⚡ 0"
                    }
                } catch (_: Exception) {
                    binding.tvMasteredStat.text = "🎴 0"
                    binding.tvXpStat.text = "⚡ 0"
                }
            }
        }
    }

    private fun setupListeners() {
        // Daily Limit SeekBar & Stepper
        binding.sbDailyLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(5)
                binding.tvDailyCardLimit.text = "$value thẻ/ngày"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveSettings()
            }
        })

        binding.btnDecreaseLimit.setOnClickListener {
            val curr = binding.sbDailyLimit.progress
            if (curr > 5) {
                binding.sbDailyLimit.progress = curr - 5
                saveSettings()
            }
        }

        binding.btnIncreaseLimit.setOnClickListener {
            val curr = binding.sbDailyLimit.progress
            if (curr < 50) {
                binding.sbDailyLimit.progress = curr + 5
                saveSettings()
            }
        }

        binding.swAutoAudio.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }

        // Segmented direction controls
        binding.btnDirectionEnVi.setOnClickListener {
            binding.btnDirectionEnVi.setBackgroundResource(R.drawable.bg_rounded_card)
            binding.btnDirectionEnVi.setTextColor(resources.getColor(R.color.primary, null))
            binding.btnDirectionViEn.setBackgroundResource(0)
            binding.btnDirectionViEn.setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        binding.btnDirectionViEn.setOnClickListener {
            binding.btnDirectionViEn.setBackgroundResource(R.drawable.bg_rounded_card)
            binding.btnDirectionViEn.setTextColor(resources.getColor(R.color.primary, null))
            binding.btnDirectionEnVi.setBackgroundResource(0)
            binding.btnDirectionEnVi.setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        // Font size S M L
        val fontButtons = listOf(binding.btnFontS, binding.btnFontM, binding.btnFontL)
        fontButtons.forEach { btn ->
            btn.setOnClickListener { selected ->
                fontButtons.forEach { b ->
                    if (b == selected) {
                        b.setBackgroundResource(R.drawable.bg_rounded_card)
                        b.setTextColor(resources.getColor(R.color.primary, null))
                    } else {
                        b.setBackgroundResource(0)
                        b.setTextColor(resources.getColor(R.color.text_secondary, null))
                    }
                }
            }
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun saveSettings() {
        val user = sessionManager.getUser() ?: return
        val newLimit = binding.sbDailyLimit.progress.coerceAtLeast(5)
        val autoAudio = binding.swAutoAudio.isChecked

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val req = UserUpdateRequest(
                    dailyNewCardsLimit = newLimit,
                    autoPlayAudio = autoAudio
                )
                val res = ApiClient.service.updateUser(user.id, req)
                if (res.isSuccessful && res.body() != null) {
                    sessionManager.saveUser(res.body()!!)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
