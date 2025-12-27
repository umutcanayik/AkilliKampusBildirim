package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter.asStateFlow()

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    _allNotifications.value = response.body() ?: emptyList()
                    applyFilter()
                }
            } catch (e: Exception) {
            }
            _isLoading.value = false
        }
    }

    fun setFilter(type: String?) {
        _selectedFilter.value = type
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (_selectedFilter.value == null) {
            _allNotifications.value
        } else {
            _allNotifications.value.filter { it.type == _selectedFilter.value }
        }
        _notifications.value = filtered.filter {
            it.location != null && it.location.lat != 0.0 && it.location.lng != 0.0
        }
    }
}