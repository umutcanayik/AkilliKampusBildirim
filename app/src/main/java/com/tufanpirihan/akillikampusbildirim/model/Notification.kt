package com.tufanpirihan.akillikampusbildirim.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("ID") val id: String = "",
    @SerializedName("UserID") val userId: String = "",
    @SerializedName("Title") val title: String = "",
    @SerializedName("Description") val description: String = "",
    @SerializedName("Type") val type: String = "",
    @SerializedName("Status") val status: String = "AÇIK",
    @SerializedName("Location") val location: Location? = null,
    @SerializedName("CreatedAt") val createdAt: String = "",
    @SerializedName("ImageUrl") val imageUrl: String? = null,
    @SerializedName("IsFollowed") val isFollowed: Boolean = false
)

enum class NotificationType(val displayName: String) {
    HEALTH("Sağlık"),
    SECURITY("Güvenlik"),
    ENVIRONMENT("Çevre"),
    LOST_FOUND("Kayıp-Bulundu"),
    TECHNICAL("Teknik Arıza")
}

enum class NotificationStatus(val displayName: String) {
    OPEN("Açık"),
    IN_PROGRESS("İnceleniyor"),
    RESOLVED("Çözüldü")
}

data class Location(
    @SerializedName("Lat") val latitude: Double = 0.0,
    @SerializedName("Lng") val longitude: Double = 0.0
)