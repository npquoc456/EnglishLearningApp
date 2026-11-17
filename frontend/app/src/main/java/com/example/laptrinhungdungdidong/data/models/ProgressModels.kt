package com.example.laptrinhungdungdidong.data.models

data class CardReviewRequest(val userId: Long, val cardId: Long, val rating: String)
data class CardProgressResponse(
    val cardId: Long,
    val status: String,
    val repetitionCount: Int,
    val intervalDays: Int,
    val nextReviewAt: String?
)
data class DueCountResponse(val count: Long)

data class StudySummaryResponse(
    val total: Long,
    val mastered: Long,
    val learning: Long,
    val newCards: Long,
    val due: Long
)
