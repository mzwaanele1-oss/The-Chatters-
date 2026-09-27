package com.thechatters.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thechatters.app.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [User] entity.
 * Defines complete CRUD (Create, Read, Update, Delete) operations for local Room persistence.
 */
@Dao
interface UserDao {

    // ==========================================
    // CREATE
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserIfNotExists(user: User): Long

    // ==========================================
    // READ
    // ==========================================

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdOnce(userId: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY name ASC")
    fun getOnlineUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchUsers(query: String): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int

    // ==========================================
    // UPDATE
    // ==========================================

    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updateUsers(users: List<User>)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateUserPresence(userId: String, isOnline: Boolean, lastSeen: Long = System.currentTimeMillis())

    @Query("UPDATE users SET bio = :bio WHERE id = :userId")
    suspend fun updateUserBio(userId: String, bio: String)

    @Query("UPDATE users SET name = :name, bio = :bio WHERE id = :userId")
    suspend fun updateUserProfile(userId: String, name: String, bio: String)

    // ==========================================
    // DELETE
    // ==========================================

    @Delete
    suspend fun deleteUserEntity(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
