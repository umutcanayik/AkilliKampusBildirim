package com.tufanpirihan.akillikampusbildirim.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String = "",
    val email: String = "",
    @SerializedName("full_name") val fullName: String = "",
    val department: String = "",
    val role: String = "user",
    @SerializedName("notification_prefs") val notificationPrefs: List<String> = emptyList(),
    @SerializedName("followed_reports") val followedReports: List<String> = emptyList(),
    @SerializedName("created_at") val createdAt: String = ""
)