package com.example.laptrinhungdungdidong.data.api

import com.example.laptrinhungdungdidong.data.models.*
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {
    @GET("api/progress/user/{userId}/deck/{deckId}")
    suspend fun getCardProgress(@Path("userId") userId: Long,
                                @Path("deckId") deckId: Long): Response<List<CardProgressResponse>>

    @GET("api/progress/user/{userId}/summary")
    suspend fun getStudySummary(@Path("userId") userId: Long): Response<StudySummaryResponse>

    @GET("api/progress/user/{userId}/due-count")
    suspend fun getDueCount(@Path("userId") userId: Long): Response<DueCountResponse>

    @POST("api/progress/review")
    suspend fun reviewCard(@Body request: CardReviewRequest): Response<CardProgressResponse>

    @Multipart
    @PUT("api/users/{id}/avatar")
    suspend fun uploadAvatar(@Path("id") id: Long,
                             @Part image: MultipartBody.Part): Response<Unit>

    @GET("api/users/{id}/avatar")
    suspend fun getAvatar(@Path("id") id: Long): Response<ResponseBody>


    // --- Users ---
    @POST("api/users/login")
    suspend fun login(@Body request: UserLoginRequest): Response<UserResponse>

    @POST("api/users/register")
    suspend fun register(@Body request: UserRegisterRequest): Response<UserResponse>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body request: UserUpdateRequest): Response<UserResponse>

    // --- Decks ---
    @GET("api/decks")
    suspend fun getAllDecks(): Response<List<DeckResponse>>

    @GET("api/decks/search")
    suspend fun searchDecks(@Query("keyword") keyword: String?): Response<List<DeckResponse>>

    @GET("api/decks/{id}")
    suspend fun getDeckById(@Path("id") id: Long): Response<DeckResponse>

    @GET("api/decks/user/{userId}")
    suspend fun getDecksByUserId(@Path("userId") userId: Long): Response<List<DeckResponse>>

    @POST("api/decks")
    suspend fun createDeck(@Body request: DeckCreateRequest): Response<DeckResponse>

    @PUT("api/decks/{id}")
    suspend fun updateDeck(@Path("id") id: Long, @Body request: DeckUpdateRequest): Response<DeckResponse>

    @DELETE("api/decks/{id}")
    suspend fun deleteDeck(@Path("id") id: Long): Response<Unit>

    // --- Cards ---
    @GET("api/cards")
    suspend fun getAllCards(): Response<List<CardResponse>>

    @GET("api/cards/deck/{deckId}")
    suspend fun getCardsByDeckId(@Path("deckId") deckId: Long): Response<List<CardResponse>>

    @POST("api/cards")
    suspend fun createCard(@Body request: CardCreateRequest): Response<CardResponse>

    @DELETE("api/cards/{id}")
    suspend fun deleteCard(@Path("id") id: Long): Response<Unit>

    // --- Study Sessions ---
    @GET("api/study-sessions")
    suspend fun getAllSessions(): Response<List<StudySessionResponse>>

    @GET("api/study-sessions/user/{userId}")
    suspend fun getSessionsByUserId(@Path("userId") userId: Long): Response<List<StudySessionResponse>>

    @POST("api/study-sessions")
    suspend fun createSession(@Body request: StudySessionCreateRequest): Response<StudySessionResponse>
}
