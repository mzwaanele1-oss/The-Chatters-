package com.thechatters.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thechatters.app.data.model.Message
import com.thechatters.app.data.model.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [Message] entity.
 * Defines complete CRUD (Create, Read, Update, Delete) operations for local Room offline caching.
 */
@Dao
interface MessageDao {

    // ==========================================
    // CREATE
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>): List<Long>

    // ==========================================
    // READ
    // ==========================================

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChatOnce(chatId: String): List<Message>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessageForChat(chatId: String): Flow<Message?>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND (text LIKE '%' || :query || '%' OR mediaName LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchMessagesInChat(chatId: String, query: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchAllMessages(query: String): Flow<List<Message>>

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND senderId != :currentUserId AND status != 'READ'")
    fun getUnreadCountForChat(chatId: String, currentUserId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND senderId != :currentUserId AND status != 'READ'")
    suspend fun getUnreadCountOnce(chatId: String, currentUserId: String): Int

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessagesCount(): Int

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    // ==========================================
    // UPDATE
    // ==========================================

    @Update
    suspend fun updateMessage(message: Message)

    @Update
    suspend fun updateMessages(messages: List<Message>)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    @Query("UPDATE messages SET status = 'READ' WHERE chatId = :chatId AND senderId != :currentUserId")
    suspend fun markMessagesAsRead(chatId: String, currentUserId: String)

    // ==========================================
    // DELETE
    // ==========================================

    @Delete
    suspend fun deleteMessageEntity(message: Message)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
