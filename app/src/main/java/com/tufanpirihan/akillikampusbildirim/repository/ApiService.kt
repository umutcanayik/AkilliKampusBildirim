package com.tufanpirihan.akillikampusbildirim.repository

import com.tufanpirihan.akillikampusbildirim.model.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @GET("profile")
    suspend fun getUserProfile(): Response<User>

    @PUT("update-profile")
    suspend fun updateProfile(@Body user: User): Response<User>

    @GET("get-reports")
    suspend fun getReports(): Response<List<Notification>>

    @GET("reports/{id}")
    suspend fun getNotificationById(@Path("id") id: String): Response<Notification>

    @POST("reports")
    suspend fun createNotification(@Body request: CreateNotificationRequest): Response<Notification>

    @Multipart
    @POST("reports")
    suspend fun createNotificationWithImage(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<Notification>

    @POST("follow")
    suspend fun followReport(@Body request: FollowRequest): Response<ApiResponse<Unit>>

    @DELETE("follow/{reportId}")
    suspend fun unfollowReport(@Path("reportId") reportId: String): Response<ApiResponse<Unit>>

    @GET("get-follow")
    suspend fun getFollowedReports(): Response<List<Notification>>
}