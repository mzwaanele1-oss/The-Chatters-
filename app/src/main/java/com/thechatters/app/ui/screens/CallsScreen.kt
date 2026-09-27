package com.thechatters.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.data.model.CallDirection
import com.thechatters.app.data.model.CallRecord
import com.thechatters.app.data.model.CallType
import com.thechatters.app.ui.components.UserAvatar
import com.thechatters.app.ui.theme.IndigoPrimary
import com.thechatters.app.ui.theme.MissedCallRed
import com.thechatters.app.ui.theme.OnlineGreen
import com.thechatters.app.viewmodel.ActiveCallState
import com.thechatters.app.viewmodel.CallViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    viewModel: CallViewModel,
    onStartCall: (String, CallType) -> Unit
) {
    val callRecords by viewModel.callRecords.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    var showStartCallDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calls (WebRTC)", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showStartCallDialog = true },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("start_call_fab")
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Start Call")
            }
        }
    ) { innerPadding ->
        if (callRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Call History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Start encrypted WebRTC voice or video calls.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(callRecords, key = { it.id }) { call ->
                    CallHistoryItem(
                        call = call,
                        onRedial = { onStartCall(call.contactName, call.callType) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Active WebRTC Call overlay
        if (activeCall != null) {
            ActiveWebRtcCallScreen(
                callState = activeCall!!,
                onMuteToggle = { viewModel.toggleMute() },
                onVideoToggle = { viewModel.toggleVideo() },
                onSwitchCamera = { viewModel.switchCamera() },
                onSpeakerToggle = { viewModel.toggleSpeaker() },
                onEndCall = { viewModel.endCall() }
            )
        }

        if (showStartCallDialog) {
            StartCallDialog(
                onDismiss = { showStartCallDialog = false },
                onCallSelected = { contact, type ->
                    showStartCallDialog = false
                    onStartCall(contact, type)
                }
            )
        }
    }
}

@Composable
fun CallHistoryItem(
    call: CallRecord,
    onRedial: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRedial)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = call.contactName, size = 48.dp, showOnlineBadge = false)
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (call.direction == CallDirection.MISSED) MissedCallRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (call.direction) {
                    CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                    CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                    CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                }
                val iconColor = when (call.direction) {
                    CallDirection.MISSED -> MissedCallRed
                    else -> OnlineGreen
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                Text(
                    text = "${sdf.format(Date(call.timestamp))}${if (call.durationSeconds > 0) " (${call.durationSeconds}s)" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onRedial) {
            Icon(
                imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Redial",
                tint = IndigoPrimary
            )
        }
    }
}

@Composable
fun ActiveWebRtcCallScreen(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("active_call_screen")
    ) {
        // Video Stream Background (Simulated WebRTC stream or local camera)
        if (callState.callType == CallType.VIDEO && callState.isVideoEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    UserAvatar(name = callState.contactName, size = 96.dp, showOnlineBadge = false)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("WebRTC Encrypted Peer Stream", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            // Draggable / Floating picture-in-picture local camera preview
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
                    .size(100.dp, 140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF334155))
                    .border(2.dp, IndigoPrimary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("You", color = Color.White, fontSize = 11.sp)
                }
            }
        } else {
            // Audio call center layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(name = callState.contactName, size = 110.dp, showOnlineBadge = false)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = callState.contactName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (callState.isConnected) formatDuration(callState.durationSeconds) else "Calling...",
                    color = if (callState.isConnected) OnlineGreen else Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Top info header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (callState.callType == CallType.VIDEO) "WebRTC Video Call" else "WebRTC Voice Call",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (callState.isConnected) formatDuration(callState.durationSeconds) else "Connecting...",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HD SECURE",
                    color = OnlineGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Bottom Call Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            CallControlButton(
                icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (callState.isMuted) "Unmute" else "Mute",
                isActive = callState.isMuted,
                onClick = onMuteToggle
            )

            // Video Toggle (for video call)
            if (callState.callType == CallType.VIDEO) {
                CallControlButton(
                    icon = if (callState.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    label = "Video",
                    isActive = !callState.isVideoEnabled,
                    onClick = onVideoToggle
                )

                // Flip Camera
                CallControlButton(
                    icon = Icons.Default.Cameraswitch,
                    label = "Flip",
                    isActive = false,
                    onClick = onSwitchCamera
                )
            } else {
                // Speaker
                CallControlButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    isActive = callState.isSpeakerOn,
                    onClick = onSpeakerToggle
                )
            }

            // End Call Button (Red)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MissedCallRed)
                    .clickable(onClick = onEndCall)
                    .testTag("end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp)
    }
}

@Composable
fun StartCallDialog(
    onDismiss: () -> Unit,
    onCallSelected: (String, CallType) -> Unit
) {
    val contacts = listOf("Sipho Dlamini", "Nomsa Khumalo", "Thabo Simelane", "Zanele Vilakati")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start WebRTC Call", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                contacts.forEach { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(contact, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Row {
                            IconButton(onClick = { onCallSelected(contact, CallType.AUDIO) }) {
                                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = IndigoPrimary)
                            }
                            IconButton(onClick = { onCallSelected(contact, CallType.VIDEO) }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = IndigoPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
}
