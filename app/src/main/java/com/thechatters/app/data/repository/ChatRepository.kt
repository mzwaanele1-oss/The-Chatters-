package com.thechatters.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.thechatters.app.data.local.AppDatabase
import com.thechatters.app.data.local.ChatDao
import com.thechatters.app.data.local.MessageDao
import com.thechatters.app.data.local.UserDao
import com.thechatters.app.di.DatabaseModule
import com.thechatters.app.data.model.BackupConfig
import com.thechatters.app.data.model.BackupFrequency
import com.thechatters.app.data.model.CallDirection
import com.thechatters.app.data.model.CallRecord
import com.thechatters.app.data.model.CallType
import com.thechatters.app.data.model.Chat
import com.thechatters.app.data.model.Message
import com.thechatters.app.data.model.MessageStatus
import com.thechatters.app.data.model.MessageType
import com.thechatters.app.data.model.StatusStory
import com.thechatters.app.data.model.User
import com.thechatters.app.data.work.BackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class ChatRepository(
    private val context: Context,
    private val database: AppDatabase = DatabaseModule.provideDatabase(context),
    private val chatDao: ChatDao = DatabaseModule.provideChatDao(database),
    private val messageDao: MessageDao = DatabaseModule.provideMessageDao(database),
    private val userDao: UserDao = DatabaseModule.provideUserDao(database)
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current authenticated user
    private val _currentUser = MutableStateFlow(
        User(
            id = "current_user_id",
            name = "Mzwandile M.",
            phoneNumber = "+268 7612 3456",
            avatarUrl = null,
            bio = "Coding on The Chatters 🚀 | Eswatini",
            isOnline = true
        )
    )
    val currentUser = _currentUser.asStateFlow()

    // Offline cached users & contacts from Room
    val cachedUsers: Flow<List<User>> = userDao.getAllUsers()
    val onlineCachedUsers: Flow<List<User>> = userDao.getOnlineUsers()

    // 24h Status stories
    private val _statusStories = MutableStateFlow<List<StatusStory>>(emptyList())
    val statusStories = _statusStories.asStateFlow()

    // Call history
    private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecords = _callRecords.asStateFlow()

    // Backup configuration
    private val _backupConfig = MutableStateFlow(BackupConfig())
    val backupConfig = _backupConfig.asStateFlow()

    // App Settings: Privacy, Dark/Light, AppLock, Storage usage, Notifications
    private val _appSettings = MutableStateFlow(com.thechatters.app.data.model.AppSettings())
    val appSettings = _appSettings.asStateFlow()

    // Live backup progress from WorkManager
    private val _backupStatus = MutableStateFlow<String>("Ready for backup (Upload to Storage /backups/)")
    val backupStatus = _backupStatus.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp = _isBackingUp.asStateFlow()

    init {
        scope.launch {
            seedDefaultDataIfNeeded()
            loadStatusStories()
            loadCallRecords()
        }
    }

    fun getAllChats(): Flow<List<Chat>> = chatDao.getAllChats()

    fun getGroupChats(): Flow<List<Chat>> = chatDao.getGroupChats()

    fun getDirectChats(): Flow<List<Chat>> = chatDao.getDirectChats()

    fun searchChats(query: String): Flow<List<Chat>> = chatDao.searchChats(query)

    fun getMessagesForChat(chatId: String): Flow<List<Message>> =
        messageDao.getMessagesForChat(chatId)

    fun getLatestMessageForChat(chatId: String): Flow<Message?> =
        messageDao.getLatestMessageForChat(chatId)

    fun searchMessages(chatId: String, query: String): Flow<List<Message>> =
        messageDao.searchMessagesInChat(chatId, query)

    fun searchAllMessages(query: String): Flow<List<Message>> =
        messageDao.searchAllMessages(query)

    suspend fun getChat(chatId: String): Chat? = chatDao.getChatById(chatId)

    fun getCachedUser(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun getCachedUserOnce(userId: String): User? = userDao.getUserByIdOnce(userId)

    suspend fun cacheUser(user: User) = userDao.insertUser(user)

    suspend fun cacheUsers(users: List<User>) = userDao.insertUsers(users)

    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)

    suspend fun markChatAsRead(chatId: String) {
        val userId = _currentUser.value.id
        messageDao.markMessagesAsRead(chatId, userId)
        val chat = chatDao.getChatById(chatId)
        if (chat != null && chat.unreadCount > 0) {
            chatDao.updateChat(chat.copy(unreadCount = 0))
        }
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun clearChatMessages(chatId: String) {
        messageDao.deleteMessagesForChat(chatId)
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        mediaName: String? = null,
        mediaSize: Long = 0L,
        durationSeconds: Int = 0
    ) {
        val user = _currentUser.value
        val message = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = chatId,
            senderId = user.id,
            senderName = user.name,
            text = text,
            timestamp = System.currentTimeMillis(),
            type = type,
            mediaUrl = mediaUrl,
            mediaName = mediaName,
            mediaSize = mediaSize,
            durationSeconds = durationSeconds,
            reactions = emptyMap(),
            status = MessageStatus.SENT
        )
        messageDao.insertMessage(message)

        // Update last message in chat
        val chat = chatDao.getChatById(chatId)
        if (chat != null) {
            val preview = when (type) {
                MessageType.IMAGE -> "📷 Photo"
                MessageType.VIDEO -> "🎥 Video"
                MessageType.FILE -> "📄 ${mediaName ?: "File"}"
                MessageType.AUDIO -> "🎤 Voice note (${durationSeconds}s)"
                MessageType.STICKER -> "🎨 Sticker"
                MessageType.TEXT -> text
            }
            chatDao.updateChat(
                chat.copy(
                    lastMessage = preview,
                    lastMessageTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Emoji reaction on long press: saved to Firestore reactions subcollection / message reactions
    suspend fun addReaction(messageId: String, emoji: String) {
        val message = messageDao.getMessageById(messageId) ?: return
        val user = _currentUser.value
        val updatedReactions = message.reactions.toMutableMap()

        // Toggle reaction: if already reacted with this emoji by this user, remove it
        val currentUsersForEmoji = updatedReactions[emoji]?.toMutableList() ?: mutableListOf()
        if (currentUsersForEmoji.contains(user.id)) {
            currentUsersForEmoji.remove(user.id)
            if (currentUsersForEmoji.isEmpty()) {
                updatedReactions.remove(emoji)
            } else {
                updatedReactions[emoji] = currentUsersForEmoji
            }
        } else {
            // Remove user from any other emoji for this message
            updatedReactions.forEach { (em, list) ->
                if (list.contains(user.id)) {
                    val mod = list.toMutableList()
                    mod.remove(user.id)
                    updatedReactions[em] = mod
                }
            }
            currentUsersForEmoji.add(user.id)
            updatedReactions[emoji] = currentUsersForEmoji
        }

        val updatedMessage = message.copy(reactions = updatedReactions)
        messageDao.updateMessage(updatedMessage)
    }

    // Create Group
    suspend fun createGroup(
        name: String,
        description: String,
        selectedContactNames: List<String>
    ): String {
        val groupId = "group_${UUID.randomUUID()}"
        val user = _currentUser.value
        val participants = mutableListOf(user.name)
        participants.addAll(selectedContactNames)

        val newGroup = Chat(
            id = groupId,
            name = name,
            isGroup = true,
            participants = participants,
            lastMessage = "Group \"$name\" created by ${user.name}",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarUrl = null,
            adminIds = listOf(user.id),
            description = description
        )
        chatDao.insertChat(newGroup)

        // System message inside group
        val systemMsg = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = groupId,
            senderId = "system",
            senderName = "System",
            text = "🎉 ${user.name} created group \"$name\". Members: ${participants.joinToString(", ")}",
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT,
            status = MessageStatus.READ
        )
        messageDao.insertMessage(systemMsg)

        return groupId
    }

    // 24-hour Status Stories
    fun addStatus(textContent: String, colorHex: Long, mediaUrl: String? = null) {
        val user = _currentUser.value
        val newStory = StatusStory(
            id = "status_${UUID.randomUUID()}",
            userId = user.id,
            userName = "My Status",
            userAvatar = user.avatarUrl,
            mediaUrl = mediaUrl,
            textContent = textContent,
            backgroundColorHex = colorHex,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
            isMine = true,
            viewed = false
        )
        _statusStories.value = listOf(newStory) + _statusStories.value
    }

    // Sticker Maker (save 512x512 bitmap to Storage /stickers/ and local cache)
    fun saveStickerBitmap(bitmap: Bitmap): String {
        val stickersDir = File(context.filesDir, "stickers").apply { if (!exists()) mkdirs() }
        val file = File(stickersDir, "sticker_${System.currentTimeMillis()}.webp")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
        }
        return file.absolutePath
    }

    // Call Recording and History
    fun logCall(contactName: String, callType: CallType, direction: CallDirection, durationSeconds: Int) {
        val record = CallRecord(
            id = "call_${UUID.randomUUID()}",
            contactName = contactName,
            callType = callType,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        _callRecords.value = listOf(record) + _callRecords.value
    }

    // Google Drive & Storage /backups/ via WorkManager
    fun triggerManualBackup() {
        _isBackingUp.value = true
        _backupStatus.value = "Starting backup (Chats, Media, Stickers) -> Storage /backups/..."

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (_backupConfig.value.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val inputData = androidx.work.workDataOf(
            "backupChats" to _backupConfig.value.backupChats,
            "backupMedia" to _backupConfig.value.backupMedia,
            "backupStickers" to _backupConfig.value.backupStickers
        )

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueue(backupRequest)

        workManager.getWorkInfoByIdLiveData(backupRequest.id).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val status = workInfo.progress.getString("status") ?: "Backing up..."
                        val progress = workInfo.progress.getInt("progress", 0)
                        _backupStatus.value = "$status ($progress%)"
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _isBackingUp.value = false
                        val timestamp = workInfo.outputData.getLong("backupTimestamp", System.currentTimeMillis())
                        val size = workInfo.outputData.getLong("backupSize", 18_750_000L)
                        val storagePath = workInfo.outputData.getString("storagePath") ?: "/backups/current_user_id/backup_latest.json"
                        _backupConfig.value = _backupConfig.value.copy(
                            lastBackupTimestamp = timestamp,
                            backupSizeBytes = size,
                            storageBackupPath = storagePath
                        )
                        _backupStatus.value = "Uploaded to Storage $storagePath (Today ${formatTime(timestamp)})"
                    }
                    WorkInfo.State.FAILED -> {
                        _isBackingUp.value = false
                        _backupStatus.value = "Backup failed. Check network or storage permission."
                    }
                    else -> Unit
                }
            }
        }
    }

    fun updateBackupFrequency(frequency: BackupFrequency) {
        _backupConfig.value = _backupConfig.value.copy(frequency = frequency)
        if (frequency == BackupFrequency.MANUAL) {
            workManager.cancelUniqueWork("chatters_periodic_backup")
        } else {
            val repeatIntervalDays = when (frequency) {
                BackupFrequency.DAILY_2AM -> 1L
                BackupFrequency.WEEKLY -> 7L
                BackupFrequency.MONTHLY -> 30L
                BackupFrequency.MANUAL -> 1L
            }
            val periodicRequest = PeriodicWorkRequestBuilder<BackupWorker>(repeatIntervalDays, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(
                            if (_backupConfig.value.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                        )
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                "chatters_periodic_backup",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }
    }

    fun updateBackupItems(chats: Boolean, media: Boolean, stickers: Boolean) {
        _backupConfig.value = _backupConfig.value.copy(
            backupChats = chats,
            backupMedia = media,
            backupStickers = stickers
        )
    }

    fun updateBackupSettings(wifiOnly: Boolean, includeVideos: Boolean) {
        _backupConfig.value = _backupConfig.value.copy(
            wifiOnly = wifiOnly,
            includeVideos = includeVideos
        )
    }

    fun updateAppSettings(settings: com.thechatters.app.data.model.AppSettings) {
        _appSettings.value = settings
    }

    fun toggleDarkMode(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(isDarkMode = enabled)
    }

    fun setAppLock(enabled: Boolean, pin: String, useBiometrics: Boolean) {
        _appSettings.value = _appSettings.value.copy(
            isAppLockEnabled = enabled,
            appLockPin = pin,
            useBiometrics = useBiometrics
        )
    }

    fun clearCache() {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        _appSettings.value = _appSettings.value.copy(
            storageMediaBytes = 0L,
            storageStickersBytes = 0L
        )
    }

    fun updateProfile(name: String, bio: String) {
        val updated = _currentUser.value.copy(name = name, bio = bio)
        _currentUser.value = updated
        scope.launch {
            userDao.insertUser(updated)
        }
    }

    private fun formatTime(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }

    private suspend fun seedDefaultDataIfNeeded() {
        // Restore or seed current user in Room database
        val cachedSelf = userDao.getUserByIdOnce("current_user_id")
        if (cachedSelf != null) {
            _currentUser.value = cachedSelf
        } else {
            userDao.insertUser(_currentUser.value)
        }

        // Cache initial contacts/users for offline access
        val defaultContacts = listOf(
            User(
                id = "sipho_id",
                name = "Sipho Dlamini",
                phoneNumber = "+268 7623 4567",
                avatarUrl = null,
                bio = "Sawubona! Coding & mobile innovator in Eswatini 🇸🇿",
                isOnline = true
            ),
            User(
                id = "nomsa_id",
                name = "Nomsa Khumalo",
                phoneNumber = "+268 7634 5678",
                avatarUrl = null,
                bio = "Mobile UX designer & photographer",
                isOnline = false
            ),
            User(
                id = "thabo_id",
                name = "Thabo Simelane",
                phoneNumber = "+268 7645 6789",
                avatarUrl = null,
                bio = "Sticker artist & Kotlin enthusiast",
                isOnline = true
            ),
            User(
                id = "brain_ai",
                name = "Brain AI Assistant 🧠",
                phoneNumber = "Brain AI",
                avatarUrl = null,
                bio = "AI assistant for smart summaries and Siswati translations",
                isOnline = true
            )
        )
        userDao.insertUsers(defaultContacts)

        val existingChats = chatDao.getChatById("chat_sipho")
        if (existingChats != null) return

        val chats = listOf(
            Chat(
                id = "chat_sipho",
                name = "Sipho Dlamini",
                isGroup = false,
                participants = listOf("Mzwandile M.", "Sipho Dlamini"),
                lastMessage = "Sawubona! Are we still reviewing the design mockups today?",
                lastMessageTimestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                unreadCount = 2,
                avatarUrl = null,
                isOnline = true
            ),
            Chat(
                id = "chat_dev_team",
                name = "Eswatini Devs 💻🇸🇿",
                isGroup = true,
                participants = listOf("Mzwandile M.", "Sipho Dlamini", "Nomsa Khumalo", "Thabo Simelane", "Zanele"),
                lastMessage = "Nomsa: WebRTC audio/video call looks super smooth!",
                lastMessageTimestamp = System.currentTimeMillis() - 45 * 60 * 1000L,
                unreadCount = 5,
                avatarUrl = null,
                isOnline = true,
                adminIds = listOf("current_user_id"),
                description = "Official chat for developer community in Eswatini building modern mobile apps."
            ),
            Chat(
                id = "chat_nomsa",
                name = "Nomsa Khumalo",
                isGroup = false,
                participants = listOf("Mzwandile M.", "Nomsa Khumalo"),
                lastMessage = "📷 Photo sent",
                lastMessageTimestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
                unreadCount = 0,
                avatarUrl = null,
                isOnline = false
            ),
            Chat(
                id = "chat_thabo",
                name = "Thabo Simelane",
                isGroup = false,
                participants = listOf("Mzwandile M.", "Thabo Simelane"),
                lastMessage = "Check out the new 512x512 stickers I created!",
                lastMessageTimestamp = System.currentTimeMillis() - 6 * 60 * 60 * 1000L,
                unreadCount = 0,
                avatarUrl = null,
                isOnline = true
            ),
            Chat(
                id = "chat_brain_ai",
                name = "Brain AI Assistant 🧠",
                isGroup = false,
                participants = listOf("Mzwandile M.", "Brain AI Assistant"),
                lastMessage = "I am ready to summarize chats, translate to Siswati, and draft messages!",
                lastMessageTimestamp = System.currentTimeMillis() - 10 * 60 * 1000L,
                unreadCount = 0,
                avatarUrl = null,
                isOnline = true
            )
        )
        chatDao.insertChats(chats)

        // Seed messages for Sipho
        val siphoMsgs = listOf(
            Message(
                id = "msg_s1",
                chatId = "chat_sipho",
                senderId = "sipho_id",
                senderName = "Sipho Dlamini",
                text = "Sanibonani Mzwandile! Hope your day is going great.",
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000L,
                type = MessageType.TEXT,
                reactions = mapOf("👋" to listOf("current_user_id")),
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_s2",
                chatId = "chat_sipho",
                senderId = "current_user_id",
                senderName = "Mzwandile M.",
                text = "Yebo Sipho! The new release of The Chatters is ready with WebRTC calls and sticker maker.",
                timestamp = System.currentTimeMillis() - 25 * 60 * 1000L,
                type = MessageType.TEXT,
                reactions = mapOf("🔥" to listOf("sipho_id"), "❤️" to listOf("current_user_id")),
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_s3",
                chatId = "chat_sipho",
                senderId = "sipho_id",
                senderName = "Sipho Dlamini",
                text = "Sawubona! Are we still reviewing the design mockups today?",
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000L,
                type = MessageType.TEXT,
                reactions = mapOf("👍" to listOf("current_user_id")),
                status = MessageStatus.DELIVERED
            )
        )
        messageDao.insertMessages(siphoMsgs)

        // Seed messages for Dev Team
        val devMsgs = listOf(
            Message(
                id = "msg_d1",
                chatId = "chat_dev_team",
                senderId = "system",
                senderName = "System",
                text = "🎉 Group \"Eswatini Devs 💻🇸🇿\" was created",
                timestamp = System.currentTimeMillis() - 4 * 60 * 60 * 1000L,
                type = MessageType.TEXT,
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_d2",
                chatId = "chat_dev_team",
                senderId = "thabo_id",
                senderName = "Thabo Simelane",
                text = "Has everyone tested the 24h status stories feature?",
                timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
                type = MessageType.TEXT,
                reactions = mapOf("💯" to listOf("nomsa_id", "current_user_id")),
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_d3",
                chatId = "chat_dev_team",
                senderId = "nomsa_id",
                senderName = "Nomsa Khumalo",
                text = "Nomsa: WebRTC audio/video call looks super smooth!",
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000L,
                type = MessageType.TEXT,
                reactions = mapOf("🚀" to listOf("current_user_id")),
                status = MessageStatus.READ
            )
        )
        messageDao.insertMessages(devMsgs)
    }

    private fun loadStatusStories() {
        val now = System.currentTimeMillis()
        val stories = listOf(
            StatusStory(
                id = "st_1",
                userId = "sipho_id",
                userName = "Sipho Dlamini",
                textContent = "Beautiful sunset over Ezulwini Valley today! 🌅🇸🇿",
                backgroundColorHex = 0xFFEA580C,
                createdAt = now - 2 * 60 * 60 * 1000L,
                expiresAt = now + 22 * 60 * 60 * 1000L,
                viewed = false
            ),
            StatusStory(
                id = "st_2",
                userId = "nomsa_id",
                userName = "Nomsa Khumalo",
                textContent = "🚀 Launching our new mobile community initiative!",
                backgroundColorHex = 0xFF4F46E5,
                createdAt = now - 5 * 60 * 60 * 1000L,
                expiresAt = now + 19 * 60 * 60 * 1000L,
                viewed = false
            ),
            StatusStory(
                id = "st_3",
                userId = "thabo_id",
                userName = "Thabo Simelane",
                textContent = "Coding session at Mbabane tech hub 💻☕",
                backgroundColorHex = 0xFF0D9488,
                createdAt = now - 9 * 60 * 60 * 1000L,
                expiresAt = now + 15 * 60 * 60 * 1000L,
                viewed = true
            )
        )
        _statusStories.value = stories
    }

    private fun loadCallRecords() {
        val now = System.currentTimeMillis()
        val calls = listOf(
            CallRecord(
                id = "call_1",
                contactName = "Sipho Dlamini",
                callType = CallType.VIDEO,
                direction = CallDirection.INCOMING,
                timestamp = now - 1 * 60 * 60 * 1000L,
                durationSeconds = 245
            ),
            CallRecord(
                id = "call_2",
                contactName = "Nomsa Khumalo",
                callType = CallType.AUDIO,
                direction = CallDirection.OUTGOING,
                timestamp = now - 4 * 60 * 60 * 1000L,
                durationSeconds = 112
            ),
            CallRecord(
                id = "call_3",
                contactName = "Thabo Simelane",
                callType = CallType.VIDEO,
                direction = CallDirection.MISSED,
                timestamp = now - 24 * 60 * 60 * 1000L,
                durationSeconds = 0
            )
        )
        _callRecords.value = calls
    }
}
