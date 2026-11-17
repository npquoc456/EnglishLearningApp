package com.example.laptrinhungdungdidong.ui.study

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.laptrinhungdungdidong.data.api.ApiClient
import com.example.laptrinhungdungdidong.data.models.*
import com.example.laptrinhungdungdidong.data.session.SessionManager
import com.example.laptrinhungdungdidong.databinding.ActivityStudyBinding
import com.example.laptrinhungdungdidong.ui.adapters.StudyCardAdapter
import com.example.laptrinhungdungdidong.utils.FlashcardRecorder
import com.example.laptrinhungdungdidong.utils.TtsHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudyBinding
    private lateinit var ttsHelper: TtsHelper
    private lateinit var recorder: FlashcardRecorder
    private lateinit var unlearnedAdapter: StudyCardAdapter
    private lateinit var masteredAdapter: StudyCardAdapter
    private var deckId = -1L
    private var mode = "REVIEW"
    private val allCards = mutableListOf<CardResponse>()
    private val cardsList = mutableListOf<CardResponse>()
    private val progress = mutableMapOf<Long, CardProgressResponse>()
    private val ratedCards = linkedMapOf<Long, String>()
    private var currentIndex = 0
    private var isFrontShowing = true
    private var savingReview = false
    private var savingSession = false
    private var isUnlearnedVisible = true
    private var isMasteredVisible = true
    private val startedAt = SystemClock.elapsedRealtime()
    private val recordings = mutableMapOf<Long, File>()
    private var activeRecording: File? = null
    private val requestMic = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
        else Toast.makeText(this, "Cần cấp quyền micro để ghi âm", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.studyTopBar.updatePadding(top = bars.top)
            binding.studyScrollView.updatePadding(bottom = bars.bottom)
            insets
        }
        deckId = intent.getLongExtra("DECK_ID", -1L)
        binding.tvStudyDeckTitle.text = intent.getStringExtra("DECK_TITLE") ?: "Bộ thẻ"
        binding.tvStudyStreak.text =
            (SessionManager(this).getUser()?.currentStreak ?: 0).toString() + " ngày"
        ttsHelper = TtsHelper(this)
        recorder = FlashcardRecorder(this)
        binding.btnStudyBack.setOnClickListener { finishSession() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finishSession() }
        })
        setupRecyclerViews()
        setupListeners()
        loadCards()
    }

    private fun setupRecyclerViews() {
        unlearnedAdapter = StudyCardAdapter(emptyList(),
            onAudioClick = { ttsHelper.speak(it.term) },
            onCardClick = { showCardDetails(it) })
        masteredAdapter = StudyCardAdapter(emptyList(),
            onAudioClick = { ttsHelper.speak(it.term) },
            onCardClick = { showCardDetails(it) })
        binding.rvUnlearnedCards.layoutManager = LinearLayoutManager(this)
        binding.rvUnlearnedCards.adapter = unlearnedAdapter
        binding.rvMasteredCards.layoutManager = LinearLayoutManager(this)
        binding.rvMasteredCards.adapter = masteredAdapter
    }

    private fun setupListeners() {
        binding.cardFlashcard.setOnClickListener { flipCard() }
        binding.btnFlipCard.setOnClickListener { flipCard() }
        binding.btnFlipBack.setOnClickListener { flipCard() }
        binding.btnCardAudio.setOnClickListener { getCurrentCard()?.let { ttsHelper.speak(it.term) } }
        binding.btnCardMic.setOnClickListener {
            when {
                recorder.isRecording -> stopRecording()
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> startRecording()
                else -> requestMic.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        binding.btnPlaybackRecording.setOnClickListener {
            val file = getCurrentCard()?.let { recordings[it.id] }
            if (file != null && file.exists()) {
                try {
                    recorder.play(file) { binding.tvRecordingStatus.text = "Đã phát xong" }
                    binding.tvRecordingStatus.text = "Đang phát bản ghi..."
                } catch (e: Exception) {
                    Toast.makeText(this, "Không phát được: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnRateAgain.setOnClickListener { rateCard("AGAIN") }
        binding.btnRateHard.setOnClickListener { rateCard("HARD") }
        binding.btnRateGood.setOnClickListener { rateCard("GOOD") }
        binding.btnRateEasy.setOnClickListener { rateCard("EASY") }
        binding.btnNextCard.setOnClickListener {
            if (currentIndex < cardsList.lastIndex) { currentIndex++; displayCurrentCard() }
        }
        binding.btnPrevCard.setOnClickListener {
            if (currentIndex > 0) { currentIndex--; displayCurrentCard() }
        }
        binding.btnShuffleCards.setOnClickListener {
            if (cardsList.isNotEmpty()) { cardsList.shuffle(); currentIndex = 0; displayCurrentCard() }
        }
        binding.tabLearn.setOnClickListener { mode = "LEARN"; applyMode() }
        binding.tabReview.setOnClickListener { mode = "REVIEW"; applyMode() }
        binding.tabDictation.setOnClickListener {
            Toast.makeText(this, "Chức năng nghe chép chưa triển khai", Toast.LENGTH_SHORT).show()
        }
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
            AlertDialog.Builder(this).setTitle("Học bằng SRS")
                .setMessage("Quên: sau 10 phút.\nKhó: sau 1 ngày.\nNhớ: sau 1–3 ngày, tăng dần.\nRất dễ: sau 4 ngày hoặc lâu hơn.\n\nChạm micro để bắt đầu/dừng ghi âm, bấm Nghe ghi âm để phát lại.")
                .setPositiveButton("Đã hiểu", null).show()
        }
        binding.btnProgressView.setOnClickListener {
            Toast.makeText(this, "Đã đánh giá ${ratedCards.size} thẻ trong phiên", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCards() {
        val user = SessionManager(this).getUser()
        if (user == null || deckId <= 0) { showEmptyState("Vui lòng đăng nhập và chọn bộ thẻ"); return }
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.getCardsByDeckId(deckId)
                if (!res.isSuccessful) {
                    showEmptyState("Không tải được thẻ (HTTP ${res.code()})"); return@launch
                }
                allCards.clear()
                allCards.addAll(res.body().orEmpty())
                val p = ApiClient.service.getCardProgress(user.id, deckId)
                if (p.isSuccessful) {
                    progress.clear()
                    p.body().orEmpty().forEach { progress[it.cardId] = it }
                }
                applyMode()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { showEmptyState("Không kết nối được máy chủ") }
        }
    }

    private fun applyMode() {
        if (recorder.isRecording) stopRecording()
        cardsList.clear()
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        cardsList.addAll(if (mode == "LEARN") allCards else allCards.filter { card ->
            val next = progress[card.id]?.nextReviewAt
            next == null || next.take(19) <= now
        })
        currentIndex = 0
        binding.tabLearn.alpha = if (mode == "LEARN") 1f else 0.6f
        binding.tabReview.alpha = if (mode == "REVIEW") 1f else 0.6f
        if (cardsList.isEmpty()) {
            showEmptyState(if (allCards.isEmpty()) "Bộ thẻ chưa có từ vựng"
            else "Chưa đến hạn ôn. Chọn tab Học để xem tất cả thẻ.")
        } else displayCurrentCard()
        refreshLists()
    }

    private fun showEmptyState(msg: String) {
        binding.tvCardTerm.text = msg
        binding.tvCardWordType.text = ""
        binding.tvCardIpa.text = ""
        binding.tvCardDefinition.text = ""
        binding.tvCardExample.text = ""
        binding.tvCardPagination.text = "Thẻ 0 / 0"
        binding.btnPlaybackRecording.isEnabled = false
        setRatingEnabled(false)
        unlearnedAdapter.updateData(emptyList())
        masteredAdapter.updateData(emptyList())
        binding.tvUnlearnedCount.text = "0"
        binding.tvMasteredCount.text = "0"
    }

    private fun refreshLists() {
        val mastered = allCards.filter { progress[it.id]?.status == "MASTERED" }
        val notMastered = allCards.filterNot { progress[it.id]?.status == "MASTERED" }
        masteredAdapter.updateData(mastered)
        unlearnedAdapter.updateData(notMastered)
        binding.tvUnlearnedCount.text = "${notMastered.size}/${allCards.size}"
        binding.tvMasteredCount.text = "${mastered.size}/${allCards.size}"
    }

    private fun getCurrentCard(): CardResponse? = cardsList.getOrNull(currentIndex)

    private fun displayCurrentCard() {
        if (recorder.isRecording) stopRecording()
        val card = getCurrentCard() ?: return
        isFrontShowing = true
        binding.layoutCardFront.visibility = View.VISIBLE
        binding.layoutCardBack.visibility = View.GONE
        binding.tvCardTerm.text = card.term
        binding.tvCardWordType.text = (card.wordType ?: "từ vựng") + " •"
        binding.tvCardIpa.text = card.ipaPronunciation ?: ""
        binding.tvCardDefinition.text = card.definition
        binding.tvCardExample.visibility = View.GONE
        binding.tvCardPagination.text = "Thẻ ${currentIndex + 1} / ${cardsList.size}"
        binding.tvNextReview.text = progress[card.id]?.nextReviewAt?.let {
            "Lịch ôn tiếp: " + it.replace('T', ' ').take(16)
        } ?: "Quên: 10 phút • Khó: 1 ngày • Nhớ: 1–3 ngày • Rất dễ: 4 ngày"
        binding.btnPlaybackRecording.isEnabled = recordings[card.id]?.exists() == true
        binding.tvRecordingStatus.text = if (binding.btnPlaybackRecording.isEnabled) "Có bản ghi để nghe lại"
            else "Chạm micro để thu âm"
        setRatingEnabled(!savingReview && !savingSession && !ratedCards.containsKey(card.id))
        if (SessionManager(this).getUser()?.autoPlayAudio != false) ttsHelper.speak(card.term)
    }

    private fun flipCard() {
        if (cardsList.isEmpty()) return
        isFrontShowing = !isFrontShowing
        binding.layoutCardFront.visibility = if (isFrontShowing) View.VISIBLE else View.GONE
        binding.layoutCardBack.visibility = if (isFrontShowing) View.GONE else View.VISIBLE
    }

    private fun showCardDetails(card: CardResponse) {
        val index = cardsList.indexOfFirst { it.id == card.id }
        if (index >= 0) { currentIndex = index; displayCurrentCard() }
        else Toast.makeText(this, "Thẻ này chưa đến hạn ôn, chọn tab Học", Toast.LENGTH_SHORT).show()
    }

    private fun startRecording() {
        val card = getCurrentCard() ?: return
        val file = File(cacheDir, "review_${card.id}_${System.currentTimeMillis()}.m4a")
        try {
            recorder.start(file)
            activeRecording = file
            binding.tvRecordingStatus.text = "● Đang ghi âm. Chạm micro để dừng"
            binding.btnPlaybackRecording.isEnabled = false
        } catch (e: Exception) {
            Toast.makeText(this, "Không ghi âm được: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        val file = activeRecording
        val card = getCurrentCard()
        val success = recorder.stop()
        activeRecording = null
        if (success && file != null && file.length() > 0 && card != null) {
            recordings.put(card.id, file)?.delete()
            binding.btnPlaybackRecording.isEnabled = true
            binding.tvRecordingStatus.text = "Đã ghi âm, bấm ▶ để nghe lại"
        } else {
            file?.delete()
            binding.tvRecordingStatus.text = "Ghi âm quá ngắn. Hãy thử lại"
        }
    }

    private fun setRatingEnabled(value: Boolean) {
        binding.btnRateAgain.isEnabled = value
        binding.btnRateHard.isEnabled = value
        binding.btnRateGood.isEnabled = value
        binding.btnRateEasy.isEnabled = value
    }

    private fun rateCard(rating: String) {
        val card = getCurrentCard() ?: return
        val user = SessionManager(this).getUser() ?: return
        if (savingReview || savingSession) return
        savingReview = true
        setRatingEnabled(false)
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.reviewCard(CardReviewRequest(user.id, card.id, rating))
                val saved = res.body()
                if (!res.isSuccessful || saved == null) {
                    Toast.makeText(this@StudyActivity, "Chưa lưu được độ khó (HTTP ${res.code()})", Toast.LENGTH_LONG).show()
                    return@launch
                }
                progress[card.id] = saved
                ratedCards[card.id] = rating
                refreshLists()
                val next = cardsList.indices.firstOrNull { !ratedCards.containsKey(cardsList[it].id) }
                if (next == null) {
                    savingReview = false
                    finishSession()
                } else {
                    currentIndex = next
                    displayCurrentCard()
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { Toast.makeText(this@StudyActivity, "Mất kết nối, chưa lưu độ khó", Toast.LENGTH_LONG).show() }
            finally {
                savingReview = false
                if (!savingSession && !isFinishing) setRatingEnabled(getCurrentCard()?.let { !ratedCards.containsKey(it.id) } == true)
            }
        }
    }

    private fun finishSession() {
        if (savingSession || savingReview) return
        if (ratedCards.isEmpty()) { finish(); return }
        val user = SessionManager(this).getUser() ?: return
        savingSession = true
        setRatingEnabled(false)
        lifecycleScope.launch {
            try {
                val seconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt().coerceAtLeast(1)
                val request = StudySessionCreateRequest(
                    userId = user.id, deckId = deckId, studyMode = mode,
                    totalCards = ratedCards.size,
                    correctCards = ratedCards.values.count { it != "AGAIN" },
                    durationSeconds = seconds
                )
                val res = ApiClient.service.createSession(request)
                if (res.isSuccessful) {
                    Toast.makeText(this@StudyActivity, "Đã lưu lịch sử học", Toast.LENGTH_SHORT).show()
                    finish()
                } else showSaveError("Máy chủ trả về HTTP ${res.code()}")
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { showSaveError(e.message ?: "Mất kết nối") }
            finally {
                savingSession = false
                if (!isFinishing) setRatingEnabled(cardsList.isNotEmpty())
            }
        }
    }

    private fun showSaveError(reason: String) {
        if (isFinishing) return
        AlertDialog.Builder(this).setTitle("Chưa lưu được lịch sử")
            .setMessage(reason + "\nĐộ khó từng thẻ đã lưu. Bạn muốn thử lại?")
            .setPositiveButton("Thử lại") { _, _ -> finishSession() }
            .setNegativeButton("Thoát") { _, _ -> finish() }
            .show()
    }

    override fun onStop() {
        if (::recorder.isInitialized && recorder.isRecording) stopRecording()
        super.onStop()
    }

    override fun onDestroy() {
        if (::recorder.isInitialized) recorder.release()
        recordings.values.forEach { it.delete() }
        activeRecording?.delete()
        if (::ttsHelper.isInitialized) ttsHelper.shutdown()
        super.onDestroy()
    }
}
