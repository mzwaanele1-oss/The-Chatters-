package com.thechatters.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.thechatters.app.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _countryCode = MutableStateFlow("+268")
    val countryCode = _countryCode.asStateFlow()

    private val _phoneNumber = MutableStateFlow("76123456")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _otpCode = MutableStateFlow("")
    val otpCode = _otpCode.asStateFlow()

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent = _isOtpSent.asStateFlow()

    private val _resendCountdown = MutableStateFlow(60)
    val resendCountdown = _resendCountdown.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private var timerJob: Job? = null

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number.filter { it.isDigit() }
    }

    fun setOtpCode(code: String) {
        if (code.length <= 6) {
            _otpCode.value = code
        }
    }

    fun sendOtp() {
        val phone = _phoneNumber.value
        if (phone.length < 7) {
            _errorMessage.value = "Please enter a valid phone number"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Emulate sending Firebase OTP
            delay(1000)
            _isLoading.value = false
            _isOtpSent.value = true
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _resendCountdown.value = 60
        timerJob = viewModelScope.launch {
            while (_resendCountdown.value > 0) {
                delay(1000)
                _resendCountdown.value -= 1
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val code = _otpCode.value
        if (code.length < 6) {
            _errorMessage.value = "Please enter all 6 digits"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
            _isAuthenticated.value = true
            repository.updateProfile("Mzwandile M.", "Coding on The Chatters 🚀 | Eswatini")
            onSuccess()
        }
    }

    fun quickDemoLogin(onSuccess: () -> Unit) {
        _phoneNumber.value = "76123456"
        _otpCode.value = "123456"
        _isAuthenticated.value = true
        onSuccess()
    }
}
