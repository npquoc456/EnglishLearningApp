package com.example.laptrinhungdungdidong.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.CardResponse
import com.example.laptrinhungdungdidong.data.models.DeckResponse
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.FragmentHomeBinding
import com.example.laptrinhungdungdidong.ui.adapters.UrgentCardAdapter
import com.example.laptrinhungdungdidong.ui.deck.CreateDeckActivity
import com.example.laptrinhungdungdidong.ui.study.StudyActivity
import com.example.laptrinhungdungdidong.utils.TtsHelper
import com.example.laptrinhungdungdidong.utils.AvatarHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var ttsHelper: TtsHelper
    private lateinit var urgentAdapter: UrgentCardAdapter
    private var firstDeckId: Long? = null
    private var firstDeckTitle: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        ttsHelper = TtsHelper(requireContext())
        sessionManager.getUser()?.let { AvatarHelper.load(it.id, binding.ivAvatar, viewLifecycleOwner.lifecycleScope) }
        binding.btnAvatar.setOnClickListener {
            (activity as? com.example.laptrinhungdungdidong.MainActivity)
                ?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.example.laptrinhungdungdidong.R.id.bottom_navigation)
                ?.selectedItemId = com.example.laptrinhungdungdidong.R.id.nav_profile
        }

        setupUrgentRecyclerView()

        binding.btnCreateDeckFromHome.setOnClickListener {
            startActivity(Intent(requireContext(), CreateDeckActivity::class.java))
        }

        binding.btnStartSRS.setOnClickListener {
            if (firstDeckId != null) {
                val intent = Intent(requireContext(), StudyActivity::class.java).apply {
                    putExtra("DECK_ID", firstDeckId)
                    putExtra("DECK_TITLE", firstDeckTitle ?: "Bộ thẻ")
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Hãy tạo bộ thẻ đầu tiên của bạn!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), CreateDeckActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadHomeData()
    }

    private fun setupUrgentRecyclerView() {
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
        binding.rvUrgentCards.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = urgentAdapter
        }
    }

    private fun loadUserData() {
        val user = sessionManager.getUser()
        if (user != null) {
            val to = java.util.Date()
            val from = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -6)
            }.time
            val dateFormat = java.text.SimpleDateFormat("d 'Thg' M", java.util.Locale.forLanguageTag("vi-VN"))
            binding.tvWeeklyDateRange.text = dateFormat.format(from) + " – " + dateFormat.format(to)
            val name = user.fullName ?: "Bạn"
            val firstName = name.split(" ").lastOrNull() ?: name
            binding.tvGreetingSub.text = "CHÀO ${firstName.uppercase()}, SẴN SÀNG BỨT PHÁ?"
            val streak = user.currentStreak ?: 0
            binding.tvHeaderStreak.text = "$streak ngày"

            if (streak > 0) {
                binding.tvBannerStreakTitle.text = "Phong độ tuần này rất tốt!"
                binding.tvBannerStreakSub.text = "Chuỗi học tập hiện tại: $streak ngày liên tiếp"
                binding.tvStreakCardTitle.text = "Chuỗi $streak ngày liên tục! ✪"
                binding.tvStreakCardSub.text = "Duy trì ôn tập đều đặn mỗi ngày"
                binding.tvHeatStreak.text = "$streak ngày liên tiếp 🔥"
            } else {
                binding.tvBannerStreakTitle.text = "Chào mừng bạn mới!"
                binding.tvBannerStreakSub.text = "Bắt đầu học thẻ từ vựng để kích hoạt chuỗi trí nhớ."
                binding.tvStreakCardTitle.text = "Bắt đầu chuỗi học tập! ✪"
                binding.tvStreakCardSub.text = "Học hôm nay để kích hoạt chuỗi đầu tiên"
                binding.tvHeatStreak.text = "0 ngày liên tiếp 🔥"
            }
        }
    }

    private fun loadHomeData() {
        val user = sessionManager.getUser() ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val decksRes = ApiClient.service.getDecksByUserId(user.id)
                if (decksRes.isSuccessful && decksRes.body() != null && decksRes.body()!!.isNotEmpty()) {
                    val decks = decksRes.body()!!
                    val first = decks.first()
                    firstDeckId = first.id
                    firstDeckTitle = first.title
                    val totalCards = decks.sumOf { it.totalCards ?: 0 }

                    if (totalCards > 0) {
                        binding.tvDueCardCount.text = "$totalCards thẻ cần ôn"
                        binding.layoutSRSAction.visibility = View.VISIBLE
                        binding.layoutNoCardsDue.visibility = View.GONE
                        binding.layoutSRSControls.visibility = View.VISIBLE
                        binding.pbSRS.max = totalCards
                        binding.pbSRS.progress = 0
                        binding.tvSrsMastered.text = "● Đã thuộc: 0"
                        binding.tvSrsLearning.text = "● Đang học: $totalCards"
                        binding.tvSrsNew.text = "● Mới: $totalCards"
                    } else {
                        // User has deck(s), but 0 cards: hide "Ôn ngay" (Ảnh 1)
                        firstDeckId = first.id
                        firstDeckTitle = first.title
                        binding.tvDueCardCount.text = "0 thẻ cần ôn"
                        binding.layoutSRSAction.visibility = View.GONE
                        binding.layoutNoCardsDue.visibility = View.VISIBLE
                        binding.layoutSRSControls.visibility = View.GONE
                        binding.pbSRS.progress = 0
                        binding.tvSrsMastered.text = "● Đã thuộc: 0"
                        binding.tvSrsLearning.text = "● Đang học: 0"
                        binding.tvSrsNew.text = "● Mới: 0"
                    }
                } else {
                    // No decks: 0 cards, hide "Ôn ngay" (Ảnh 1)
                    firstDeckId = null
                    firstDeckTitle = null
                    binding.tvDueCardCount.text = "0 thẻ cần ôn"
                    binding.layoutSRSAction.visibility = View.GONE
                    binding.layoutNoCardsDue.visibility = View.VISIBLE
                    binding.layoutSRSControls.visibility = View.GONE
                    binding.pbSRS.progress = 0
                    binding.tvSrsMastered.text = "● Đã thuộc: 0"
                    binding.tvSrsLearning.text = "● Đang học: 0"
                    binding.tvSrsNew.text = "● Mới: 0"
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                firstDeckId = null
                firstDeckTitle = null
                binding.tvDueCardCount.text = "0 thẻ cần ôn"
                binding.layoutSRSAction.visibility = View.GONE
                binding.layoutNoCardsDue.visibility = View.VISIBLE
                binding.layoutSRSControls.visibility = View.GONE
                binding.pbSRS.progress = 0
                binding.tvSrsMastered.text = "● Đã thuộc: 0"
                binding.tvSrsLearning.text = "● Đang học: 0"
                binding.tvSrsNew.text = "● Mới: 0"
            }

            // Real SRS counters; no guessed "mastered" or fake completion progress.
            try {
                val summaryResponse = ApiClient.service.getStudySummary(user.id)
                if (summaryResponse.isSuccessful) {
                    val stat = summaryResponse.body()
                    if (stat != null) {
                        val due = stat.due.toInt()
                        val total = stat.total.toInt()
                        binding.tvDueCardCount.text = "$due thẻ cần ôn"
                        binding.tvUrgentBadge.text = "$due từ"
                        binding.layoutSRSAction.visibility = if (due > 0) View.VISIBLE else View.GONE
                        binding.layoutNoCardsDue.visibility = if (due > 0) View.GONE else View.VISIBLE
                        binding.layoutSRSControls.visibility = if (total > 0) View.VISIBLE else View.GONE
                        binding.pbSRS.max = total.coerceAtLeast(1)
                        binding.pbSRS.progress = stat.mastered.toInt()
                        binding.tvSrsMastered.text = "● Đã thuộc: ${stat.mastered}"
                        binding.tvSrsLearning.text = "● Đang học: ${stat.learning}"
                        binding.tvSrsNew.text = "● Mới: ${stat.newCards}"
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }

            // Refresh streak/profile values from RDS after a completed study session.
            try {
                val freshUser = ApiClient.service.getUserById(user.id)
                if (freshUser.isSuccessful && freshUser.body() != null) {
                    sessionManager.saveUser(freshUser.body()!!)
                    loadUserData()
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }

            // Real study stats for this user
            try {
                val sessionsRes = ApiClient.service.getSessionsByUserId(user.id)
                if (sessionsRes.isSuccessful && sessionsRes.body() != null && sessionsRes.body()!!.isNotEmpty()) {
                    val cutoff = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -7)
                    }.time
                    val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    val sessions = sessionsRes.body()!!.filter { session ->
                        try {
                            val raw = session.studiedAt?.take(19) ?: ""
                            parser.parse(raw)?.after(cutoff) == true
                        } catch (_: Exception) { false }
                    }
                    val totalReviewed = sessions.sumOf { it.totalCards }
                    val totalCorrect = sessions.sumOf { it.correctCards }
                    val totalMinutes = sessions.sumOf { (it.durationSeconds ?: 0) / 60 }
                    val accuracy = if (totalReviewed > 0) (totalCorrect * 100 / totalReviewed) else 0

                    binding.tvWeeklyReviewedCards.text = "$totalReviewed"
                    binding.tvWeeklyReviewedSub.text = "+$totalReviewed thẻ"
                    binding.tvWeeklyAccuracy.text = "$accuracy%"
                    binding.tvWeeklyStudyTime.text = "${totalMinutes}p"
                    binding.tvWeeklyStudyTimeSub.text = "$totalMinutes phút"
                } else {
                    binding.tvWeeklyReviewedCards.text = "0"
                    binding.tvWeeklyReviewedSub.text = "+0 thẻ"
                    binding.tvWeeklyAccuracy.text = "0%"
                    binding.tvWeeklyStudyTime.text = "0p"
                    binding.tvWeeklyStudyTimeSub.text = "0 phút"
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                binding.tvWeeklyReviewedCards.text = "0"
                binding.tvWeeklyAccuracy.text = "0%"
                binding.tvWeeklyStudyTime.text = "0p"
            }

            // Urgent cards
            binding.rvUrgentCards.visibility = View.GONE
            binding.tvEmptyHomeUrgent.visibility = View.VISIBLE
            // Badge already set from SRS due-count when the API is available.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsHelper.shutdown()
        _binding = null
    }
}
