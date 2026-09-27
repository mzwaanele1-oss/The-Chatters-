package com.thechatters.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thechatters.app.data.model.CallDirection
import com.thechatters.app.data.model.CallRecord
import com.thechatters.app.data.model.CallType
import com.thechatters.app.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveCallState(
    val contactName: String,
    val callType: CallType,
    val isConnected: Boolean = false,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isSpeakerOn: Boolean = true
)

class CallViewModel(private val repository: ChatRepository) : ViewModel() {
    val callRecords = repository.callRecords

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall = _activeCall.asStateFlow()

    private var callTimerJob: Job? = null

    fun startCall(contactName: String, callType: CallType) {
        _activeCall.value = ActiveCallState(
            contactName = contactName,
            callType = callType,
            isConnected = false,
            durationSeconds = 0,
            isVideoEnabled = callType == CallType.VIDEO
        )

        // Simulate WebRTC signaling & peer connection
        viewModelScope.launch {
            delay(1500) // Ringing
            _activeCall.value = _activeCall.value?.copy(isConnected = true)
            startCallTimer()
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCall.value != null && _activeCall.value?.isConnected == true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun switchCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun endCall() {
        val current = _activeCall.value
        callTimerJob?.cancel()
        if (current != null) {
            repository.logCall(
                contactName = current.contactName,
                callType = current.callType,
                direction = CallDirection.OUTGOING,
                durationSeconds = current.durationSeconds
            )
        }
        _activeCall.value = null
    }
}
