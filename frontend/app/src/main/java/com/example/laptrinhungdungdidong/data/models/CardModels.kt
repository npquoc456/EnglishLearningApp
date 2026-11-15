package com.example.laptrinhungdungdidong.data.models

data class CardResponse(
    val id: Long,
    val deckId: Long,
    val term: String,
    val definition: String,
    val wordType: String? = null,
    val ipaPronunciation: String? = null,
    val audioUrl: String? = null,
    val orderIndex: Int? = null,
    val createdAt: String? = null
)

data class CardCreateRequest(
    val deckId: Long,
    val term: String,
    val definition: String,
    val wordType: String? = null,
    val ipaPronunciation: String? = null,
    val audioUrl: String? = null,
    val orderIndex: Int? = null
)
