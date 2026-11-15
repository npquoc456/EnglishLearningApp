package com.example.laptrinhungdungdidong.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhungdungdidong.data.models.DeckResponse
import com.example.laptrinhungdungdidong.databinding.ItemDeckBinding

class DeckAdapter(
    private var decks: List<DeckResponse>,
    private val onDeckClick: (DeckResponse) -> Unit,
    private val onStudyClick: (DeckResponse) -> Unit,
    private val onMoreClick: (View, DeckResponse) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    fun updateData(newDecks: List<DeckResponse>) {
        this.decks = newDecks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(decks[position])
    }

    override fun getItemCount(): Int = decks.size

    inner class DeckViewHolder(private val binding: ItemDeckBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(deck: DeckResponse) {
            binding.tvDeckTitle.text = deck.title
            binding.tvCategoryTag.text = deck.categoryTag ?: "Từ vựng"

            val isPublic = (deck.visibility ?: "PUBLIC") == "PUBLIC"
            binding.tvVisibility.text = if (isPublic) "🌐 Công khai" else "🔒 Riêng tư"

            val count = deck.totalCards ?: 0
            binding.tvCardCount.text = "$count từ"

            // Status message & progress calculation
            val progressPercent = if (count > 0) ((bindingAdapterPosition + 1) * 20).coerceIn(20, 100) else 0
            binding.pbDeckProgress.progress = progressPercent

            when {
                progressPercent >= 100 -> {
                    binding.tvStatus.text = "Hoàn thành xuất sắc"
                    binding.btnStudyNow.visibility = View.GONE
                }
                progressPercent > 50 -> {
                    binding.tvStatus.text = "Tiến độ ghi nhớ"
                    binding.btnStudyNow.visibility = View.VISIBLE
                }
                else -> {
                    binding.tvStatus.text = "Chưa bắt đầu học đợt này"
                    binding.btnStudyNow.visibility = View.GONE
                }
            }

            binding.tvLastStudied.text = if (deck.lastStudiedAt != null) {
                "🕒 Lần học cuối: 2 ngày trước"
            } else {
                "🕒 Chưa ôn tập"
            }

            binding.tvCreator.text = "👤 Hoàng Minh"

            binding.root.setOnClickListener { onDeckClick(deck) }
            binding.btnStudyNow.setOnClickListener { onStudyClick(deck) }
            binding.btnMore.setOnClickListener { view -> onMoreClick(view, deck) }
        }
    }
}
