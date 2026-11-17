package com.example.laptrinhungdungdidong.data.models

data class UserResponse(
    val id: Long,
    val fullName: String?,
    val email: String?,
    val avatarUrl: String?,
    val currentStreak: Int?,
    val lastStudyDate: String?,
    val defaultStudyDirection: String?,
    val dailyNewCardsLimit: Int?,
    val autoPlayAudio: Boolean?,
    val reminderTime: String?,
    val createdAt: String?,
    val accessToken: String? = null
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserRegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class UserUpdateRequest(
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val defaultStudyDirection: String? = null,
    val dailyNewCardsLimit: Int? = null,
    val autoPlayAudio: Boolean? = null,
    val reminderTime: String? = null
)
