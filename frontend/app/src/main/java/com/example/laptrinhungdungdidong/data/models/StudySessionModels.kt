package com.example.laptrinhungdungdidong.data.models

data class StudySessionResponse(
    val id: Long,
    val userId: Long,
    val deckId: Long?,
    val studyMode: String,
    val totalCards: Int,
    val correctCards: Int,
    val durationSeconds: Int,
    val studiedAt: String?
)

data class StudySessionCreateRequest(
    val userId: Long,
    val deckId: Long?,
    val studyMode: String,
    val totalCards: Int,
    val correctCards: Int,
    val durationSeconds: Int,
    val studiedAt: String? = null
)
