package com.tufanpirihan.akillikampusbildirim.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tufanpirihan.akillikampusbildirim.model.Notification
import com.tufanpirihan.akillikampusbildirim.model.NotificationType
import com.tufanpirihan.akillikampusbildirim.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())
    private val _followedIds = MutableStateFlow<Set<String>>(emptySet())

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<NotificationType?>(null)
    val selectedFilter: StateFlow<NotificationType?> = _selectedFilter.asStateFlow()

    private val _showOnlyOpen = MutableStateFlow(false)
    val showOnlyOpen: StateFlow<Boolean> = _showOnlyOpen.asStateFlow()

    private val _showOnlyFollowed = MutableStateFlow(false)
    val showOnlyFollowed: StateFlow<Boolean> = _showOnlyFollowed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchNotifications()
        fetchFollowedReports()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.getReports()
                if (response.isSuccessful) {
                    _allNotifications.value = response.body() ?: emptyList()
                    applyFilters()
                } else {
                    _errorMessage.value = "Bildirimler yüklenemedi"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Bağlantı hatası"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchFollowedReports() {
        val userId = RetrofitClient.getUserId() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getFollowedReports(userId)
                if (response.isSuccessful) {
                    val followedReports = response.body() ?: emptyList()
                    _followedIds.value = followedReports.map { it.id }.toSet()
                    applyFilters()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateFilter(type: NotificationType?) {
        _selectedFilter.value = type
        applyFilters()
    }

    fun toggleShowOnlyOpen() {
        _showOnlyOpen.value = !_showOnlyOpen.value
        applyFilters()
    }

    fun toggleShowOnlyFollowed() {
        _showOnlyFollowed.value = !_showOnlyFollowed.value
        if (_showOnlyFollowed.value) {
            fetchFollowedReports()
        }
        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = _allNotifications.value.filter { notification ->
            val matchesQuery = _searchQuery.value.isEmpty() ||
                    notification.title.contains(_searchQuery.value, ignoreCase = true) ||
                    notification.description.contains(_searchQuery.value, ignoreCase = true)

            val matchesType = _selectedFilter.value == null ||
                    matchNotificationType(notification.type, _selectedFilter.value!!)

            val matchesOpen = !_showOnlyOpen.value ||
                    notification.status.uppercase() == "AÇIK"

            val matchesFollowed = !_showOnlyFollowed.value ||
                    _followedIds.value.contains(notification.id)

            matchesQuery && matchesType && matchesOpen && matchesFollowed
        }

        _notifications.value = filteredList.sortedByDescending { it.createdAt }
    }

    private fun matchNotificationType(type: String, filter: NotificationType): Boolean {
        val normalizedType = type.uppercase()
        return when (filter) {
            NotificationType.HEALTH -> normalizedType == "SAĞLIK" || normalizedType == "HEALTH"
            NotificationType.SECURITY -> normalizedType == "GÜVENLİK" || normalizedType == "SECURITY"
            NotificationType.ENVIRONMENT -> normalizedType == "ÇEVRE" || normalizedType == "ENVIRONMENT"
            NotificationType.LOST_FOUND -> normalizedType == "KAYIP-BULUNDU" || normalizedType == "LOST_FOUND"
            NotificationType.TECHNICAL -> normalizedType == "TEKNİK ARIZA" || normalizedType == "TECHNICAL"
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = null
        _showOnlyOpen.value = false
        _showOnlyFollowed.value = false
        applyFilters()
    }

    fun refreshFollowed() {
        fetchFollowedReports()
    }
}