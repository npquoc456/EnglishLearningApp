package com.example.laptrinhungdungdidong.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhungdungdidong.data.models.StudySessionResponse
import com.example.laptrinhungdungdidong.databinding.ItemStudySessionBinding

class StudySessionAdapter(
    private var sessions: List<StudySessionResponse>
) : RecyclerView.Adapter<StudySessionAdapter.SessionViewHolder>() {

    fun updateData(newSessions: List<StudySessionResponse>) {
        this.sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemStudySessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position], position)
    }

    override fun getItemCount(): Int = sessions.size

    inner class SessionViewHolder(private val binding: ItemStudySessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: StudySessionResponse, pos: Int) {
            binding.tvSessionTitle.text = session.deckTitle ?: "Ôn tập tổng hợp"
            val raw = session.studiedAt
            binding.tvSessionTime.text = if (raw.isNullOrBlank()) "Chưa có thời gian"
                else raw.replace('T', ' ').take(16) + " • " + when (session.studyMode) {
                    "REVIEW" -> "Ôn tập"
                    "LEARN" -> "Học"
                    else -> "Nghe chép"
                }

            val total = session.totalCards
            val correct = session.correctCards
            val percent = if (total > 0) (correct * 100) / total else 0

            binding.tvAccuracyBadge.text = if (percent == 100) "★ 100%" else "$percent% đúng"

            val mins = session.durationSeconds / 60
            val secs = session.durationSeconds % 60
            binding.tvDuration.text = "${mins} phút ${secs} giây"

            binding.pbSession.progress = percent
            binding.tvFinishedCards.text = "Đã hoàn thành $total thẻ"
            binding.tvCorrectFraction.text = if (percent == 100) "$correct / $total thẻ tuyệt đối" else "$correct / $total thẻ đúng"
        }
    }
}
