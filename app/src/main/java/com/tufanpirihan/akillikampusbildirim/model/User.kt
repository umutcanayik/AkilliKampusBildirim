package com.tufanpirihan.akillikampusbildirim.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("ID") val id: String = "",
    @SerializedName("FullName") val fullName: String = "",
    @SerializedName("Email") val email: String = "",
    @SerializedName("Role") val role: String = "USER",
    @SerializedName("Department") val department: String = "",
    @SerializedName("CreatedAt") val createdAt: String = ""
)

enum class UserRole {
    USER,
    ADMIN
}