package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.ProfileResponse
import com.tufanpirihan.akillikampusbildirim.model.UpdateProfileRequest
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _followedReports = MutableStateFlow<List<Notification>>(emptyList())
    val followedReports: StateFlow<List<Notification>> = _followedReports.asStateFlow()

    private val _notificationPrefs = MutableStateFlow<Set<String>>(emptySet())
    val notificationPrefs: StateFlow<Set<String>> = _notificationPrefs.asStateFlow()

    fun fetchProfile() {
        val userId = RetrofitClient.getUserId() ?: return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileState.Success(response.body()!!)
                } else {
                    _profileState.value = ProfileState.Error("Profil yüklenemedi")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun fetchFollowedReports() {
        val userId = RetrofitClient.getUserId() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getFollowedReports(userId)
                if (response.isSuccessful) {
                    _followedReports.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateProfile(department: String?, notificationPrefs: List<String>?) {
        val userId = RetrofitClient.getUserId() ?: return
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val response = RetrofitClient.apiService.updateProfile(
                    userId,
                    UpdateProfileRequest(department, notificationPrefs)
                )
                if (response.isSuccessful) {
                    _updateState.value = UpdateState.Success
                    fetchProfile()
                } else {
                    _updateState.value = UpdateState.Error("Güncelleme başarısız")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.localizedMessage ?: "Bağlantı hatası")
            }
        }
    }

    fun updateNotificationPrefs(prefs: Set<String>) {
        _notificationPrefs.value = prefs
        updateProfile(null, prefs.toList())
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Initial
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val profile: ProfileResponse) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class UpdateState {
        object Initial : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
}