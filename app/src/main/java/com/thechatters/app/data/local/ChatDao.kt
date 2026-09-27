package com.thechatters.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thechatters.app.data.model.Chat
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [Chat] entity.
 * Defines complete CRUD (Create, Read, Update, Delete) operations for conversations.
 */
@Dao
interface ChatDao {

    // ==========================================
    // CREATE
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<Chat>): List<Long>

    // ==========================================
    // READ
    // ==========================================

    @Query("SELECT * FROM chats ORDER BY lastMessageTimestamp DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): Chat?

    @Query("SELECT * FROM chats WHERE isGroup = 1 ORDER BY lastMessageTimestamp DESC")
    fun getGroupChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE isGroup = 0 ORDER BY lastMessageTimestamp DESC")
    fun getDirectChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chats WHERE name LIKE '%' || :query || '%' OR lastMessage LIKE '%' || :query || '%' ORDER BY lastMessageTimestamp DESC")
    fun searchChats(query: String): Flow<List<Chat>>

    @Query("SELECT COUNT(*) FROM chats")
    suspend fun getChatsCount(): Int

    // ==========================================
    // UPDATE
    // ==========================================

    @Update
    suspend fun updateChat(chat: Chat)

    @Update
    suspend fun updateChats(chats: List<Chat>)

    // ==========================================
    // DELETE
    // ==========================================

    @Delete
    suspend fun deleteChatEntity(chat: Chat)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()
}
