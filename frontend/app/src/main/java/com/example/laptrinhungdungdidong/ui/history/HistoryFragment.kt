package com.example.laptrinhungdungdidong.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.FragmentHistoryBinding
import com.example.laptrinhungdungdidong.ui.adapters.StudySessionAdapter
import com.example.laptrinhungdungdidong.ui.adapters.UrgentCardAdapter
import com.example.laptrinhungdungdidong.ui.study.StudyActivity
import com.example.laptrinhungdungdidong.utils.TtsHelper
import com.example.laptrinhungdungdidong.utils.AvatarHelper
import android.widget.Toast
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionAdapter: StudySessionAdapter
    private lateinit var urgentAdapter: UrgentCardAdapter
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        val user = SessionManager(requireContext()).getUser()
        val streak = user?.currentStreak ?: 1
        binding.tvHistoryStreak.text = "$streak ngày"

        setupRecyclerViews()
        user?.let { AvatarHelper.load(it.id, binding.ivAvatar, viewLifecycleOwner.lifecycleScope) }
        binding.btnAvatar.setOnClickListener {
            (activity as? com.example.laptrinhungdungdidong.MainActivity)
                ?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.example.laptrinhungdungdidong.R.id.bottom_navigation)
                ?.selectedItemId = com.example.laptrinhungdungdidong.R.id.nav_profile
        }
        binding.btnViewAllSessions.setOnClickListener {
            if (sessionAdapter.itemCount == 0) {
                Toast.makeText(requireContext(), "Chưa có phiên học nào", Toast.LENGTH_SHORT).show()
            } else {
                binding.rvHistorySessions.requestFocus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistoryData()
    }

    private fun setupRecyclerViews() {
        sessionAdapter = StudySessionAdapter(emptyList())
        binding.rvHistorySessions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }

        urgentAdapter = UrgentCardAdapter(
            cards = emptyList(),
            onPracticeClick = { card ->
                val intent = Intent(requireContext(), StudyActivity::class.java).apply {
                    putExtra("DECK_ID", card.deckId)
                    putExtra("CARD_TERM", card.term)
                }
                startActivity(intent)
            },
            onAudioClick = { card ->
                ttsHelper.speak(card.term)
            }
        )
        binding.rvUrgentCardsHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = urgentAdapter
        }
    }

    private fun loadHistoryData() {
        val user = SessionManager(requireContext()).getUser() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sessionRes = ApiClient.service.getSessionsByUserId(user.id)
                if (sessionRes.isSuccessful && sessionRes.body() != null && sessionRes.body()!!.isNotEmpty()) {
                    val sessions = sessionRes.body()!!
                    sessionAdapter.updateData(sessions)
                    binding.rvHistorySessions.visibility = View.VISIBLE
                    binding.layoutEmptyHistory.visibility = View.GONE
                } else {
                    binding.rvHistorySessions.visibility = View.GONE
                    binding.layoutEmptyHistory.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                binding.rvHistorySessions.visibility = View.GONE
                binding.layoutEmptyHistory.visibility = View.VISIBLE
            }

            // Due count from real SRS schedule.
            try {
                val dueResponse = ApiClient.service.getDueCount(user.id)
                val count = if (dueResponse.isSuccessful) dueResponse.body()?.count ?: 0L else 0L
                binding.tvHistoryUrgentBadge.text = "$count từ"
                binding.tvEmptyUrgent.text = if (count > 0)
                    "Có $count thẻ đến hạn ôn. Vào Bộ Thẻ để tiếp tục học."
                else "Hiện chưa có thẻ từ vựng đến hạn ôn."
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
            try {
                val freshUser = ApiClient.service.getUserById(user.id)
                if (freshUser.isSuccessful && freshUser.body() != null) {
                    SessionManager(requireContext()).saveUser(freshUser.body()!!)
                    binding.tvHistoryStreak.text =
                        (freshUser.body()!!.currentStreak ?: 0).toString() + " ngày"
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
            binding.rvUrgentCardsHistory.visibility = View.GONE
            binding.tvEmptyUrgent.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsHelper.shutdown()
        _binding = null
    }
}
