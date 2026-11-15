package com.example.laptrinhungdungdidong.data.models

data class DeckResponse(
    val id: Long,
    val userId: Long,
    val title: String,
    val categoryTag: String?,
    val visibility: String?,
    val totalCards: Int?,
    val lastStudiedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class DeckCreateRequest(
    val userId: Long,
    val title: String,
    val categoryTag: String?,
    val visibility: String = "PUBLIC"
)

data class DeckUpdateRequest(
    val title: String,
    val categoryTag: String? = "Từ vựng",
    val visibility: String? = "PUBLIC"
)
