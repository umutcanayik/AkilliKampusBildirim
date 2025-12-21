package com.tufanpirihan.akillikampusbildirim.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.CreateNotificationRequest
import com.tufanpirihan.akillikampusbildirim.model.NotificationType
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CreateNotificationViewModel : ViewModel() {

    private val _notificationState = MutableStateFlow<CreateNotificationState>(CreateNotificationState.Initial)
    val notificationState: StateFlow<CreateNotificationState> = _notificationState.asStateFlow()

    fun createNotification(
        title: String,
        description: String,
        type: NotificationType,
        latitude: Double?,
        longitude: Double?,
        imageUri: Uri?,
        context: Context
    ) {
        if (title.isBlank()) {
            _notificationState.value = CreateNotificationState.Error("Başlık boş olamaz")
            return
        }
        if (description.isBlank()) {
            _notificationState.value = CreateNotificationState.Error("Açıklama boş olamaz")
            return
        }

        val userId = RetrofitClient.getUserId()
        if (userId == null) {
            _notificationState.value = CreateNotificationState.Error("Oturum hatası, tekrar giriş yapın")
            return
        }

        viewModelScope.launch {
            _notificationState.value = CreateNotificationState.Loading

            try {
                if (imageUri != null) {
                    val imageFile = uriToFile(imageUri, context)
                    createNotificationWithImage(title, description, type, latitude, longitude, userId, imageFile)
                } else {
                    createNotificationWithoutImage(title, description, type, latitude, longitude, userId)
                }
            } catch (e: Exception) {
                _notificationState.value = CreateNotificationState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    private suspend fun createNotificationWithoutImage(
        title: String,
        description: String,
        type: NotificationType,
        latitude: Double?,
        longitude: Double?,
        userId: String
    ) {
        val request = CreateNotificationRequest(
            title = title,
            description = description,
            type = type.displayName,
            latitude = latitude,
            longitude = longitude,
            userId = userId
        )

        val response = RetrofitClient.apiService.createNotification(request)
        if (response.isSuccessful) {
            _notificationState.value = CreateNotificationState.Success("Bildirim başarıyla oluşturuldu")
        } else {
            _notificationState.value = CreateNotificationState.Error("Bildirim oluşturulamadı")
        }
    }

    private suspend fun createNotificationWithImage(
        title: String,
        description: String,
        type: NotificationType,
        latitude: Double?,
        longitude: Double?,
        userId: String,
        imageFile: File?
    ) {
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type.displayName.toRequestBody("text/plain".toMediaTypeOrNull())
        val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudeBody = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeBody = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestFile)
        }

        val response = RetrofitClient.apiService.createNotificationWithImage(
            titleBody, descriptionBody, typeBody, userIdBody, latitudeBody, longitudeBody, imagePart
        )

        if (response.isSuccessful) {
            _notificationState.value = CreateNotificationState.Success("Bildirim başarıyla oluşturuldu")
        } else {
            _notificationState.value = CreateNotificationState.Error("Bildirim oluşturulamadı")
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    fun resetState() {
        _notificationState.value = CreateNotificationState.Initial
    }

    sealed class CreateNotificationState {
        object Initial : CreateNotificationState()
        object Loading : CreateNotificationState()
        data class Success(val message: String) : CreateNotificationState()
        data class Error(val message: String) : CreateNotificationState()
    }
}