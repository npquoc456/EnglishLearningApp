package com.example.laptrinhungdungdidong

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.ActivityMainBinding
import com.example.laptrinhungdungdidong.ui.auth.LoginActivity
import com.example.laptrinhungdungdidong.ui.decks.DecksFragment
import com.example.laptrinhungdungdidong.ui.history.HistoryFragment
import com.example.laptrinhungdungdidong.ui.home.HomeFragment
import com.example.laptrinhungdungdidong.ui.profile.ProfileFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle System Insets (Status bar at top; BottomNavigationView handles navigation bar internally)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.updatePadding(
                top = systemBars.top,
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }

        // Default screen is HomeFragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Disable active indicator completely so icon has natural bounds without 32dp inflated pill
        binding.bottomNavigation.isItemActiveIndicatorEnabled = false

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_decks -> {
                    loadFragment(DecksFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateDeckBadge()
    }

    fun updateDeckBadge() {
        val user = sessionManager.getUser() ?: return
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.getDecksByUserId(user.id)
                val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_decks)
                badge.backgroundColor = getColor(R.color.primary)
                badge.badgeTextColor = getColor(R.color.white)
                badge.maxCharacterCount = 3

                if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                    val count = res.body()!!.size
                    badge.number = count
                    badge.isVisible = true
                } else {
                    badge.isVisible = false
                }
            } catch (_: Exception) {
                val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_decks)
                badge.backgroundColor = getColor(R.color.primary)
                badge.badgeTextColor = getColor(R.color.white)
                badge.isVisible = false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
