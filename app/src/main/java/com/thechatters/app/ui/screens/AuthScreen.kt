package com.thechatters.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.ui.theme.IndigoPrimary
import com.thechatters.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    val countryCode by viewModel.countryCode.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val otpCode by viewModel.otpCode.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val resendCountdown by viewModel.resendCountdown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("auth_screen"),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF4F46E5)),
                contentAlignment = Alignment.Center
            ) {
                Text("C", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(20.dp))
            Text(if (!isOtpSent) "Enter your phone number" else "Verify Phone Number", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(if (!isOtpSent) "The Chatters will send a Firebase OTP SMS to verify your account. Defaulting to Eswatini (+268)." else "Enter the 6-digit code sent to $countryCode $phoneNumber", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            if (!isOtpSent) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF4F46E5).copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🇸🇿", fontSize = 20.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(countryCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        BasicTextField(value = phoneNumber, onValueChange = { viewModel.setPhoneNumber(it) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f).testTag("phone_number_input"), decorationBox = { inner -> if (phoneNumber.isEmpty()) Text("7612 3456", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.titleMedium); inner() })
                    }
                }
                if (errorMessage != null) { Spacer(Modifier.height(12.dp)); Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.sendOtp() }, modifier = Modifier.fillMaxWidth().height(52.dp).testTag("send_otp_button"), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary), enabled = !isLoading && phoneNumber.isNotBlank()) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp) else Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { viewModel.quickDemoLogin(onAuthSuccess) }, modifier = Modifier.fillMaxWidth().height(48.dp).testTag("demo_login_button"), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Instant Demo Login (+268)", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (i in 0 until 6) {
                        val digit = otpCode.getOrNull(i)?.toString() ?: ""; val isFocused = otpCode.length == i
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(if (isFocused) 2.dp else 1.dp, if (isFocused) IndigoPrimary else Color.Transparent, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text(digit, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                BasicTextField(value = otpCode, onValueChange = { viewModel.setOtpCode(it) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, modifier = Modifier.fillMaxWidth().height(1.dp).testTag("otp_input"))
                if (errorMessage != null) { Spacer(Modifier.height(8.dp)); Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.verifyOtp(onAuthSuccess) }, modifier = Modifier.fillMaxWidth().height(52.dp).testTag("verify_otp_button"), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary), enabled = !isLoading && otpCode.length == 6) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp) else Text("Verify Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (resendCountdown > 0) Text("Resend code in ${resendCountdown}s", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) else TextButton(onClick = { viewModel.sendOtp() }) { Text("Resend SMS", color = IndigoPrimary, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.setOtpCode("123456"); viewModel.verifyOtp(onAuthSuccess) }) { Text("Auto-fill Test Code (123456)", color = IndigoPrimary) }
            }
        }
    }
}
