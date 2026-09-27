package com.thechatters.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    AUDIO,
    STICKER
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["timestamp"])
    ]
)
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val mediaName: String? = null,
    val mediaSize: Long = 0L,
    val durationSeconds: Int = 0,
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of userIds/names
    val status: MessageStatus = MessageStatus.SENT
)

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val name: String,
    val isGroup: Boolean = false,
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val adminIds: List<String> = emptyList(),
    val description: String = ""
)

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["phoneNumber"])
    ]
)
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarUrl: String? = null,
    val bio: String = "Hey there! I am using The Chatters.",
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
)

data class Reaction(
    val id: String,
    val messageId: String,
    val userId: String,
    val userName: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StatusStory(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val mediaUrl: String? = null,
    val textContent: String = "",
    val backgroundColorHex: Long = 0xFF4F46E5,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val isMine: Boolean = false,
    val viewed: Boolean = false
)

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

data class CallRecord(
    val id: String,
    val contactName: String,
    val contactAvatar: String? = null,
    val callType: CallType,
    val direction: CallDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

enum class BackupFrequency {
    DAILY_2AM,
    WEEKLY,
    MONTHLY,
    MANUAL
}

data class BackupConfig(
    val frequency: BackupFrequency = BackupFrequency.DAILY_2AM,
    val lastBackupTimestamp: Long = 0L,
    val backupSizeBytes: Long = 18_750_000L, // ~18.7 MB
    val wifiOnly: Boolean = true,
    val includeVideos: Boolean = true,
    val backupChats: Boolean = true,
    val backupMedia: Boolean = true,
    val backupStickers: Boolean = true,
    val isGoogleSignedIn: Boolean = true,
    val driveAccount: String = "mzwaanele1@gmail.com",
    val storageBackupPath: String = "/backups/current_user_id/backup_latest.json"
)

data class AppSettings(
    val isDarkMode: Boolean = true,
    val isAppLockEnabled: Boolean = false, // OFF by default as required
    val appLockPin: String = "",
    val useBiometrics: Boolean = false,
    val privacyLastSeen: String = "Everyone",
    val privacyReadReceipts: Boolean = true,
    val privacyProfilePhoto: String = "My Contacts",
    val notificationsEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val storageChatsBytes: Long = 24_500_000L,
    val storageMediaBytes: Long = 68_200_000L,
    val storageStickersBytes: Long = 12_400_000L
)

