package com.satlock.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import com.satlock.data.local.UserDao
import com.satlock.data.remote.ApiService
import com.satlock.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val context: Context
) {

    val allUsers: LiveData<List<User>> = userDao.getAllUsers()

    suspend fun refreshUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No hay conexión a internet"))
            }

            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()

                val existingUsers = userDao.getAllUsersSync()
                val userMap = existingUsers.associateBy { it.id }

                val updatedUsers = users.map { user ->
                    val existingUser = userMap[user.id]
                    user.copy(
                        isFavorite = existingUser?.isFavorite ?: false,
                        pendingSync = false
                    )
                }

                userDao.insertUsers(updatedUsers)
                Result.success(updatedUsers)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(userId: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        user?.let {
            val newFavoriteStatus = !it.isFavorite
            val hasPendingSync = !isNetworkAvailable()
            userDao.updateFavoriteStatus(userId, newFavoriteStatus, hasPendingSync)
        }
    }

    suspend fun syncPendingChanges(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No hay conexión"))
            }

            val pendingUsers = userDao.getPendingSyncUsers()

            if (pendingUsers.isEmpty()) {
                return@withContext Result.success(Unit)
            }

            delay(2000)

            // apiService.updateFavorites(pendingUsers)

            userDao.clearPendingSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    suspend fun hasPendingSync(): Boolean = withContext(Dispatchers.IO) {
        userDao.getPendingSyncUsers().isNotEmpty()
    }

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }
}