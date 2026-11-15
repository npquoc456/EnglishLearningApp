package com.example.laptrinhungdungdidong.ui.study

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.CardResponse
import com.example.laptrinhungdungdidong.databinding.ActivityStudyBinding
import com.example.laptrinhungdungdidong.ui.adapters.StudyCardAdapter
import com.example.laptrinhungdungdidong.utils.TtsHelper
import kotlinx.coroutines.launch
import java.util.Collections

class StudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBinding
    private lateinit var ttsHelper: TtsHelper

    private var deckId: Long = 1L
    private var deckTitle: String = "CAM 21 listen"

    private var cardsList = mutableListOf<CardResponse>()
    private var currentIndex = 0
    private var isFrontShowing = true

    private lateinit var unlearnedAdapter: StudyCardAdapter
    private lateinit var masteredAdapter: StudyCardAdapter

    private var isUnlearnedVisible = true
    private var isMasteredVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.studyTopBar.updatePadding(top = systemBars.top)
            binding.studyScrollView.updatePadding(bottom = systemBars.bottom)
            insets
        }

        ttsHelper = TtsHelper(this)

        deckId = intent.getLongExtra("DECK_ID", 1L)
        deckTitle = intent.getStringExtra("DECK_TITLE") ?: "CAM 21 listen"
        binding.tvStudyDeckTitle.text = deckTitle

        binding.btnStudyBack.setOnClickListener { finish() }

        setupRecyclerViews()
        setupListeners()
        loadCards()
    }

    private fun setupRecyclerViews() {
        unlearnedAdapter = StudyCardAdapter(
            cards = emptyList(),
            onAudioClick = { card -> ttsHelper.speak(card.term) },
            onCardClick = { card -> showCardDetails(card) }
        )
        binding.rvUnlearnedCards.apply {
            layoutManager = LinearLayoutManager(this@StudyActivity)
            adapter = unlearnedAdapter
        }

        masteredAdapter = StudyCardAdapter(
            cards = emptyList(),
            onAudioClick = { card -> ttsHelper.speak(card.term) },
            onCardClick = { card -> showCardDetails(card) }
        )
        binding.rvMasteredCards.apply {
            layoutManager = LinearLayoutManager(this@StudyActivity)
            adapter = masteredAdapter
        }
    }

    private fun setupListeners() {
        // Tap card to flip
        binding.cardFlashcard.setOnClickListener { flipCard() }
        binding.btnFlipCard.setOnClickListener { flipCard() }
        binding.btnFlipBack.setOnClickListener { flipCard() }

        // Audio TTS
        binding.btnCardAudio.setOnClickListener {
            val currentCard = getCurrentCard()
            if (currentCard != null) {
                ttsHelper.speak(currentCard.term)
            }
        }

        // Mic speech simulation
        binding.btnCardMic.setOnClickListener {
            Toast.makeText(this, "Đang nghe phát âm của bạn...", Toast.LENGTH_SHORT).show()
        }

        // Paging
        binding.btnNextCard.setOnClickListener {
            if (cardsList.isNotEmpty() && currentIndex < cardsList.size - 1) {
                currentIndex++
                displayCurrentCard()
            }
        }

        binding.btnPrevCard.setOnClickListener {
            if (cardsList.isNotEmpty() && currentIndex > 0) {
                currentIndex--
                displayCurrentCard()
            }
        }

        // Shuffle
        binding.btnShuffleCards.setOnClickListener {
            if (cardsList.isNotEmpty()) {
                cardsList.shuffle()
                currentIndex = 0
                displayCurrentCard()
                Toast.makeText(this, "Đã xáo trộn danh sách thẻ!", Toast.LENGTH_SHORT).show()
            }
        }

        // Toggle list visibility
        binding.btnToggleUnlearned.setOnClickListener {
            isUnlearnedVisible = !isUnlearnedVisible
            binding.rvUnlearnedCards.visibility = if (isUnlearnedVisible) View.VISIBLE else View.GONE
            binding.btnToggleUnlearned.text = if (isUnlearnedVisible) "👁 Ẩn bớt" else "👁 Hiện lại"
        }

        binding.btnToggleMastered.setOnClickListener {
            isMasteredVisible = !isMasteredVisible
            binding.rvMasteredCards.visibility = if (isMasteredVisible) View.VISIBLE else View.GONE
            binding.btnToggleMastered.text = if (isMasteredVisible) "👁 Ẩn bớt" else "👁 Hiện lại"
        }

        binding.tvHowToStudy.setOnClickListener {
            Toast.makeText(this, "Chạm vào thẻ để lật xem định nghĩa. Vuốt hoặc bấm mũi tên để chuyển thẻ tiếp theo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadCards() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.getCardsByDeckId(deckId)
                if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                    cardsList.clear()
                    cardsList.addAll(res.body()!!)
                    populateStudyData()
                } else {
                    fallbackDefaultCards()
                }
            } catch (_: Exception) {
                fallbackDefaultCards()
            }
        }
    }

    private fun fallbackDefaultCards() {
        cardsList.clear()
        cardsList.addAll(
            listOf(
                CardResponse(1, deckId, "be any catering provided", "được cung cấp bất kỳ dịch vụ ăn uống nào", "phrase", "/bi:/ /'eni/ /'keit(e)rin/ /pre'vaidid/"),
                CardResponse(2, deckId, "accommodate", "cung cấp chỗ ở, đáp ứng nhu cầu", "verb", "/əˈkɒm.ə.deɪt/"),
                CardResponse(3, deckId, "venue hire", "thuê địa điểm tổ chức sự kiện", "phrase", "/ˈven.juː haɪər/"),
                CardResponse(4, deckId, "keep health is...", "tránh xa các vấn đề sức khỏe", "phrase", "/kiːp helθ/"),
                CardResponse(5, deckId, "Foster team...", "Nuôi dưỡng tinh thần đồng đội & gắn kết thành viên", "sentence", "/ˈfɒs.tər tiːm/"),
                CardResponse(6, deckId, "Relieve stress...", "Giảm căng thẳng và ức chế thần kinh", "phrase", "/rɪˈliːv stres ænd ˈtenʃn/")
            )
        )
        populateStudyData()
    }

    private fun populateStudyData() {
        currentIndex = 0
        displayCurrentCard()

        // Unlearned (excluding the first one)
        val unlearned = if (cardsList.size > 1) cardsList.subList(1, cardsList.size) else emptyList()
        unlearnedAdapter.updateData(unlearned)
        binding.tvUnlearnedCount.text = "${cardsList.size - 1}/${cardsList.size}"

        // Mastered (the first one)
        val mastered = if (cardsList.isNotEmpty()) listOf(cardsList.first()) else emptyList()
        masteredAdapter.updateData(mastered)
        binding.tvMasteredCount.text = "1/${cardsList.size}"
    }

    private fun getCurrentCard(): CardResponse? {
        return if (cardsList.isNotEmpty() && currentIndex in cardsList.indices) {
            cardsList[currentIndex]
        } else null
    }

    private fun displayCurrentCard() {
        val card = getCurrentCard() ?: return
        isFrontShowing = true
        binding.layoutCardFront.visibility = View.VISIBLE
        binding.layoutCardBack.visibility = View.GONE

        // Front Face
        binding.tvCardTerm.text = card.term
        binding.tvCardWordType.text = "${card.wordType ?: "phrase"} •"
        binding.tvCardIpa.text = card.ipaPronunciation ?: "/.../"

        // Back Face
        binding.tvCardDefinition.text = card.definition
        binding.tvCardExample.text = "Ví dụ: Example context for '${card.term}'"

        // Pager indicator
        binding.tvCardPagination.text = "Thẻ ${currentIndex + 1} / ${cardsList.size}"

        val user = com.example.laptrinhungdungdidong.data.session.SessionManager(this).getUser()
        if (user?.autoPlayAudio != false) {
            ttsHelper.speak(card.term)
        }
    }

    private fun flipCard() {
        isFrontShowing = !isFrontShowing
        if (isFrontShowing) {
            binding.layoutCardFront.visibility = View.VISIBLE
            binding.layoutCardBack.visibility = View.GONE
        } else {
            binding.layoutCardFront.visibility = View.GONE
            binding.layoutCardBack.visibility = View.VISIBLE
        }
    }

    private fun showCardDetails(card: CardResponse) {
        val idx = cardsList.indexOfFirst { it.id == card.id }
        if (idx != -1) {
            currentIndex = idx
            displayCurrentCard()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
    }
}
