package com.example.laptrinhungdungdidong.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhungdungdidong.data.models.CardResponse
import com.example.laptrinhungdungdidong.databinding.ItemUrgentCardBinding

class UrgentCardAdapter(
    private var cards: List<CardResponse>,
    private val onPracticeClick: (CardResponse) -> Unit,
    private val onAudioClick: (CardResponse) -> Unit
) : RecyclerView.Adapter<UrgentCardAdapter.UrgentCardViewHolder>() {

    fun updateData(newCards: List<CardResponse>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrgentCardViewHolder {
        val binding = ItemUrgentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UrgentCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UrgentCardViewHolder, position: Int) {
        holder.bind(cards[position], position)
    }

    override fun getItemCount(): Int = cards.size

    inner class UrgentCardViewHolder(private val binding: ItemUrgentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardResponse, pos: Int) {
            binding.tvTerm.text = card.term
            binding.tvWordType.text = card.wordType ?: "phrase"
            binding.tvDefinition.text = card.definition

            if (!card.ipaPronunciation.isNullOrEmpty()) {
                binding.tvPhonetic.visibility = View.VISIBLE
                binding.tvPhonetic.text = card.ipaPronunciation
            } else {
                binding.tvPhonetic.visibility = View.GONE
            }

            when (pos % 3) {
                0 -> {
                    binding.tvForgetCount.text = "↻ Quên 3 lần"
                    binding.tvDeckName.text = "Bộ: Sports & Fitness"
                }
                1 -> {
                    binding.tvForgetCount.text = "↻ Quên 2 lần"
                    binding.tvDeckName.text = "Bộ: Sports & Fitness"
                }
                else -> {
                    binding.tvForgetCount.text = "⏱ Ôn muộn 1 ngày"
                    binding.tvDeckName.text = "Bộ: Daily Life"
                }
            }

            binding.btnPractice.setOnClickListener { onPracticeClick(card) }
            binding.btnAudio.setOnClickListener { onAudioClick(card) }
        }
    }
}
