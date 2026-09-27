package com.thechatters.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thechatters.app.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrainAiMessage(
    val id: String,
    val sender: String, // "user" or "brain"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BrainAiMode {
    CHAT,
    SUMMARIZE,
    TRANSLATE,
    SMART_REPLY
}

class BrainAiViewModel(private val geminiService: GeminiService) : ViewModel() {
    private val _currentMode = MutableStateFlow(BrainAiMode.CHAT)
    val currentMode = _currentMode.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<BrainAiMessage>>(
        listOf(
            BrainAiMessage(
                id = "m1",
                sender = "brain",
                text = "Hello! I am Brain AI 🧠 powered by Gemini.\n\nI can:\n• 📝 Summarize long group chats\n• 🌍 Translate messages to Siswati, French, etc.\n• 💡 Suggest contextual smart replies\n• ✍️ Draft messages with custom tone\n\nHow can I help you today?"
            )
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Summarize State
    private val _summaryResult = MutableStateFlow<String?>(null)
    val summaryResult = _summaryResult.asStateFlow()

    // Translate State
    private val _targetLanguage = MutableStateFlow("Siswati")
    val targetLanguage = _targetLanguage.asStateFlow()

    private val _translationResult = MutableStateFlow<String?>(null)
    val translationResult = _translationResult.asStateFlow()

    fun setMode(mode: BrainAiMode) {
        _currentMode.value = mode
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    fun sendMessage() {
        val prompt = _inputText.value.trim()
        if (prompt.isBlank() || _isLoading.value) return

        val userMsg = BrainAiMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "user",
            text = prompt
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val response = geminiService.generateContent(prompt)
            val brainMsg = BrainAiMessage(
                id = "brain_${System.currentTimeMillis()}",
                sender = "brain",
                text = response
            )
            _chatMessages.value = _chatMessages.value + brainMsg
            _isLoading.value = false
        }
    }

    fun summarizeText(textToSummarize: String) {
        if (textToSummarize.isBlank() || _isLoading.value) return
        _isLoading.value = true
        _summaryResult.value = null

        viewModelScope.launch {
            val prompt = "Please summarize the following chat conversation into key points, action items, and general mood:\n\n$textToSummarize"
            val result = geminiService.generateContent(prompt)
            _summaryResult.value = result
            _isLoading.value = false
        }
    }

    fun translateText(textToTranslate: String, language: String = _targetLanguage.value) {
        if (textToTranslate.isBlank() || _isLoading.value) return
        _isLoading.value = true
        _translationResult.value = null

        viewModelScope.launch {
            val prompt = "Translate the following chat message into $language with accurate tone and natural phrasing:\n\n\"$textToTranslate\""
            val result = geminiService.generateContent(prompt)
            _translationResult.value = result
            _isLoading.value = false
        }
    }

    fun generateSmartReplies(lastMessage: String, onGenerated: (List<String>) -> Unit) {
        viewModelScope.launch {
            val prompt = "Suggest 3 short, natural chat replies (one casual, one polite, one enthusiastic with emoji) for: \"$lastMessage\". Return only the 3 lines."
            val result = geminiService.generateContent(prompt)
            val lines = result.lines()
                .map { it.trim().removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("-").trim() }
                .filter { it.isNotBlank() }
                .take(3)
            if (lines.isNotEmpty()) {
                onGenerated(lines)
            } else {
                onGenerated(listOf("👍 Got it!", "Sounds good!", "Awesome! 🎉"))
            }
        }
    }
}
