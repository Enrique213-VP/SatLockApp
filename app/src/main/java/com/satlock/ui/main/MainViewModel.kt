package com.satlock.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.satlock.data.local.AppDatabase
import com.satlock.data.remote.RetrofitClient
import com.satlock.data.repository.UserRepository
import com.satlock.domain.User
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    val allUsers: LiveData<List<User>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable: LiveData<Boolean> = _isNetworkAvailable

    private val _filteredUsers = MutableLiveData<List<User>>()
    val filteredUsers: LiveData<List<User>> = _filteredUsers

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(
            RetrofitClient.apiService,
            database.userDao(),
            application
        )
        allUsers = repository.allUsers
        checkNetworkAndLoadData()
    }

    private fun checkNetworkAndLoadData() {
        viewModelScope.launch {
            val hasNetwork = repository.isNetworkAvailable()
            _isNetworkAvailable.value = hasNetwork

            val userCount = repository.getUserCount()

            if (userCount == 0 && hasNetwork) {
                refreshUsers()
            } else if (userCount > 0 && hasNetwork) {
                syncPendingChanges()
            }
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.refreshUsers()
            _isLoading.value = false

            result.onSuccess {
                _errorMessage.value = ""
                _isNetworkAvailable.value = true
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error desconocido"
                _isNetworkAvailable.value = repository.isNetworkAvailable()
            }
        }
    }

    fun toggleFavorite(userId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(userId)

            if (!repository.isNetworkAvailable()) {
                _syncStatus.value = SyncStatus.PENDING
            }
        }
    }

    fun syncPendingChanges() {
        viewModelScope.launch {
            if (!repository.hasPendingSync()) {
                return@launch
            }

            _syncStatus.value = SyncStatus.SYNCING
            val result = repository.syncPendingChanges()

            result.onSuccess {
                _syncStatus.value = SyncStatus.SYNCED
            }.onFailure {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    fun checkNetworkStatus() {
        viewModelScope.launch {
            val hasNetwork = repository.isNetworkAvailable()
            val previousStatus = _isNetworkAvailable.value ?: false
            _isNetworkAvailable.value = hasNetwork

            if (!previousStatus && hasNetwork) {
                syncPendingChanges()
            }
        }
    }

    fun filterUsers(query: String) {
        viewModelScope.launch {
            val users = allUsers.value ?: emptyList()

            if (query.isEmpty()) {
                _filteredUsers.value = users
            } else {
                val filtered = users.filter { user ->
                    user.name.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true) ||
                            user.address.city.contains(query, ignoreCase = true) ||
                            user.phone.contains(query, ignoreCase = true)
                }
                _filteredUsers.value = filtered
            }
        }
    }

    fun clearFilter() {
        _filteredUsers.value = allUsers.value
    }
}

enum class SyncStatus {
    IDLE,
    PENDING,
    SYNCING,
    SYNCED,
    ERROR
}