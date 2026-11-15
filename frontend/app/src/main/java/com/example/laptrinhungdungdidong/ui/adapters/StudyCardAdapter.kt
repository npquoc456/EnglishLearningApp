package com.example.laptrinhungdungdidong.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.laptrinhungdungdidong.data.models.CardResponse
import com.example.laptrinhungdungdidong.databinding.ItemCardStudyBinding

class StudyCardAdapter(
    private var cards: List<CardResponse>,
    private val onAudioClick: (CardResponse) -> Unit,
    private val onCardClick: (CardResponse) -> Unit
) : RecyclerView.Adapter<StudyCardAdapter.CardViewHolder>() {

    fun updateData(newCards: List<CardResponse>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardStudyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    inner class CardViewHolder(private val binding: ItemCardStudyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardResponse) {
            binding.tvTerm.text = card.term
            val wordType = card.wordType ?: "phrase"
            val ipa = card.ipaPronunciation ?: ""
            binding.tvWordTypeIpa.text = if (ipa.isNotEmpty()) "$wordType • $ipa" else wordType
            binding.tvDefinition.text = card.definition

            binding.btnAudio.setOnClickListener { onAudioClick(card) }
            binding.btnLookUp.setOnClickListener { onCardClick(card) }
            binding.root.setOnClickListener { onCardClick(card) }
        }
    }
}
