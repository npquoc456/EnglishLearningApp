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
        loadHistoryData()
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
            } catch (_: Exception) {
                binding.rvHistorySessions.visibility = View.GONE
                binding.layoutEmptyHistory.visibility = View.VISIBLE
            }

            // Urgent cards
            binding.rvUrgentCardsHistory.visibility = View.GONE
            binding.tvEmptyUrgent.visibility = View.VISIBLE
            binding.tvHistoryUrgentBadge.text = "0 từ"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsHelper.shutdown()
        _binding = null
    }
}
