package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.EmergencyNotification
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.UpdateStatusRequest
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
            val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _emergencyState = MutableStateFlow<EmergencyState>(EmergencyState.Initial)
            val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    init {
        fetchAllNotifications()
    }

    fun fetchAllNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    _notifications.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {

            }
            _isLoading.value = false
        }
    }

    fun updateNotificationStatus(notificationId: String, newStatus: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val response = RetrofitClient.apiService.updateReportStatus(
                        notificationId,
                        UpdateStatusRequest(newStatus)
                )
                if (response.isSuccessful) {
                    _updateState.value = UpdateState.Success("Durum güncellendi")
                    fetchAllNotifications()
                    _updateState.value = UpdateState.Error("Güncelleme başarısız")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.localizedMessage ?: "Hata oluştu")
            }
        }
    }

    fun sendEmergencyNotification(title: String, message: String) {
        viewModelScope.launch {
            _emergencyState.value = EmergencyState.Loading
            try {
                val response = RetrofitClient.apiService.sendEmergencyNotification(
                        EmergencyNotification(title, message)
                )
                if (response.isSuccessful) {
                    _emergencyState.value = EmergencyState.Success("Acil durum bildirimi gönderildi")
                } else {
                    _emergencyState.value = EmergencyState.Error("Gönderim başarısız")
                }
            } catch (e: Exception) {
                _emergencyState.value = EmergencyState.Error(e.localizedMessage ?: "Hata oluştu")
            }
        }
    }

    fun resetEmergencyState() {
        _emergencyState.value = EmergencyState.Initial
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }

    sealed class UpdateState {
        object Initial : UpdateState()
        object Loading : UpdateState()
        data class Success(val message: String) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    sealed class EmergencyState {
        object Initial : EmergencyState()
        object Loading : EmergencyState()
        data class Success(val message: String) : EmergencyState()
        data class Error(val message: String) : EmergencyState()
    }
}