package com.thechatters.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thechatters.app.data.model.Chat
import com.thechatters.app.data.model.Message
import com.thechatters.app.data.model.MessageType
import com.thechatters.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ChatFilter {
    ALL,
    UNREAD,
    GROUPS
}

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(ChatFilter.ALL)
    val currentFilter = _currentFilter.asStateFlow()

    val allChats: StateFlow<List<Chat>> = combine(
        repository.getAllChats(),
        _searchQuery,
        _currentFilter
    ) { chats, query, filter ->
        chats.filter { chat ->
            val matchesQuery = query.isBlank() || chat.name.contains(query, ignoreCase = true) ||
                    chat.lastMessage.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> chat.unreadCount > 0
                ChatFilter.GROUPS -> chat.isGroup
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline cached users and contacts
    val cachedUsers: StateFlow<List<com.thechatters.app.data.model.User>> = repository.cachedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat in Detail View
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeChat = _activeChat.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<Message>>(emptyList())
    val activeMessages = _activeMessages.asStateFlow()

    // Emoji Reaction Picker state (holds the message ID user is reacting to)
    private val _reactingMessageId = MutableStateFlow<String?>(null)
    val reactingMessageId = _reactingMessageId.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ChatFilter) {
        _currentFilter.value = filter
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch {
            repository.markChatAsRead(chatId)
            _activeChat.value = repository.getChat(chatId)
            repository.getMessagesForChat(chatId).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun searchChatMessages(chatId: String, query: String) =
        repository.searchMessages(chatId, query)

    fun closeChat() {
        _activeChatId.value = null
        _activeChat.value = null
        _activeMessages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(chatId = chatId, text = text, type = MessageType.TEXT)
        }
    }

    fun sendMediaMessage(type: MessageType, mediaUrl: String?, mediaName: String?, mediaSize: Long) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = if (type == MessageType.STICKER) "🎨 Sticker" else (mediaName ?: "Attachment"),
                type = type,
                mediaUrl = mediaUrl,
                mediaName = mediaName,
                mediaSize = mediaSize
            )
        }
    }

    fun sendSticker(bitmap: Bitmap) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            val localPath = repository.saveStickerBitmap(bitmap)
            repository.sendMessage(
                chatId = chatId,
                text = "🎨 Sticker",
                type = MessageType.STICKER,
                mediaUrl = localPath,
                mediaName = "Sticker 512x512",
                mediaSize = 120_000L
            )
        }
    }

    fun showReactionPicker(messageId: String) {
        _reactingMessageId.value = messageId
    }

    fun hideReactionPicker() {
        _reactingMessageId.value = null
    }

    fun addReactionToMessage(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(messageId, emoji)
            _reactingMessageId.value = null
        }
    }

    fun createGroup(name: String, description: String, selectedMembers: List<String>, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val groupId = repository.createGroup(name, description, selectedMembers)
            onCreated(groupId)
        }
    }
}
