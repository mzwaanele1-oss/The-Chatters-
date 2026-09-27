package com.thechatters.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thechatters.app.data.model.StatusStory
import com.thechatters.app.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusViewModel(private val repository: ChatRepository) : ViewModel() {
    val statusStories = repository.statusStories

    private val _activeStoryIndex = MutableStateFlow<Int?>(null)
    val activeStoryIndex = _activeStoryIndex.asStateFlow()

    private val _storyProgress = MutableStateFlow(0f)
    val storyProgress = _storyProgress.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private var storyTimerJob: Job? = null

    fun openStoryViewer(index: Int) {
        _activeStoryIndex.value = index
        startStoryTimer()
    }

    fun closeStoryViewer() {
        storyTimerJob?.cancel()
        _activeStoryIndex.value = null
        _storyProgress.value = 0f
    }

    fun pauseStory() {
        _isPaused.value = true
    }

    fun resumeStory() {
        _isPaused.value = false
    }

    fun nextStory() {
        val current = _activeStoryIndex.value ?: return
        val stories = statusStories.value
        if (current < stories.lastIndex) {
            _activeStoryIndex.value = current + 1
            startStoryTimer()
        } else {
            closeStoryViewer()
        }
    }

    fun previousStory() {
        val current = _activeStoryIndex.value ?: return
        if (current > 0) {
            _activeStoryIndex.value = current - 1
            startStoryTimer()
        } else {
            startStoryTimer()
        }
    }

    private fun startStoryTimer() {
        storyTimerJob?.cancel()
        _storyProgress.value = 0f
        storyTimerJob = viewModelScope.launch {
            val totalSteps = 100
            for (step in 1..totalSteps) {
                while (_isPaused.value) {
                    delay(50)
                }
                delay(50) // 5 seconds per story slide
                _storyProgress.value = step / 100f
            }
            nextStory()
        }
    }

    fun createTextStatus(text: String, colorHex: Long) {
        if (text.isNotBlank()) {
            repository.addStatus(textContent = text, colorHex = colorHex)
        }
    }

    fun createPhotoStatus(photoUri: String, caption: String) {
        repository.addStatus(textContent = caption, colorHex = 0xFF000000, mediaUrl = photoUri)
    }
}
