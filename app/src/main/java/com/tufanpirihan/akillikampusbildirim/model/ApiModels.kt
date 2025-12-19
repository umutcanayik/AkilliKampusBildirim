package com.tufanpirihan.akillikampusbildirim.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val department: String
)

data class RegisterResponse(
    val message: String,
    val user: User
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
    val imageUrl: String? = null
)

data class FollowRequest(
    val reportId: String
)