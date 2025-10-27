package com.satlock.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.satlock.domain.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users ORDER BY name ASC")
    suspend fun getAllUsersSync(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Query("UPDATE users SET isFavorite = :isFavorite, pendingSync = :pendingSync WHERE id = :userId")
    suspend fun updateFavoriteStatus(userId: Int, isFavorite: Boolean, pendingSync: Boolean)

    @Query("SELECT * FROM users WHERE pendingSync = 1")
    suspend fun getPendingSyncUsers(): List<User>

    @Query("UPDATE users SET pendingSync = 0")
    suspend fun clearPendingSync()

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}