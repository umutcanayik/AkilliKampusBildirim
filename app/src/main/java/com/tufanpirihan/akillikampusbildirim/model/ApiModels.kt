package com.tufanpirihan.akillikampusbildirim.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val uid: String,
    val message: String
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val password: String,
    val department: String
)

data class RegisterResponse(
    val message: String,
    @SerializedName("user_id") val userId: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

data class ForgotPasswordRequest(
    val email: String
)

data class CreateNotificationRequest(
    val title: String,
    val description: String,
    val type: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("user_id") val userId: String? = null
)

data class FollowRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("report_id") val reportId: String
)

data class ProfileResponse(
    @SerializedName("full_name") val fullName: String,
    val email: String,
    val department: String,
    val role: String
)

data class UpdateProfileRequest(
    val department: String?,
    @SerializedName("notification_prefs") val notificationPrefs: List<String>?
)