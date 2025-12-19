package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.FollowRequest
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationDetailViewModel : ViewModel() {

    private val _notificationState = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val notificationState: StateFlow<NotificationState> = _notificationState.asStateFlow()

    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed.asStateFlow()

    fun fetchNotificationDetails(notificationId: String) {
        viewModelScope.launch {
            _notificationState.value = NotificationState.Loading
            try {
                val response = RetrofitClient.apiService.getNotificationById(notificationId)
                if (response.isSuccessful && response.body() != null) {
                    val notification = response.body()!!
                    _isFollowed.value = notification.isFollowed
                    _notificationState.value = NotificationState.Success(notification)
                } else {
                    _notificationState.value = NotificationState.Error("Bildirim bulunamadı")
                }
            } catch (e: Exception) {
                _notificationState.value = NotificationState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun toggleFollowNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                if (_isFollowed.value) {
                    val response = RetrofitClient.apiService.unfollowReport(notificationId)
                    if (response.isSuccessful) {
                        _isFollowed.value = false
                        updateNotificationFollowState(false)
                    }
                } else {
                    val response = RetrofitClient.apiService.followReport(FollowRequest(notificationId))
                    if (response.isSuccessful) {
                        _isFollowed.value = true
                        updateNotificationFollowState(true)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun updateNotificationFollowState(followed: Boolean) {
        val currentState = _notificationState.value
        if (currentState is NotificationState.Success) {
            _notificationState.value = NotificationState.Success(
                currentState.notification.copy(isFollowed = followed)
            )
        }
    }

    sealed class NotificationState {
        object Loading : NotificationState()
        data class Success(val notification: Notification) : NotificationState()
        data class Error(val message: String) : NotificationState()
    }
}