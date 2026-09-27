package com.thechatters.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.data.model.StatusStory
import com.thechatters.app.ui.components.StatusRingAvatar
import com.thechatters.app.ui.components.UserAvatar
import com.thechatters.app.ui.theme.IndigoPrimary
import com.thechatters.app.viewmodel.StatusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    viewModel: StatusViewModel,
    onReplyToStory: (String, String) -> Unit
) {
    val statusStories by viewModel.statusStories.collectAsState()
    val activeStoryIndex by viewModel.activeStoryIndex.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val myStories = statusStories.filter { it.isMine }
    val contactStories = statusStories.filter { !it.isMine }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status (24h)", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Text Status", modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Status")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // My Status Section
            item {
                Text(
                    text = "My Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (myStories.isNotEmpty()) {
                                viewModel.openStoryViewer(statusStories.indexOf(myStories.first()))
                            } else {
                                showCreateDialog = true
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(name = "My Status", size = 56.dp, showOnlineBadge = false)
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (myStories.isEmpty()) "Tap to add status update" else "My Status (${myStories.size} updates)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (myStories.isEmpty()) "Disappears after 24 hours" else "Active for 24h",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Contact Stories
            items(contactStories, key = { it.id }) { story ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val idx = statusStories.indexOf(story)
                            viewModel.openStoryViewer(idx)
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusRingAvatar(
                        name = story.userName,
                        size = 54.dp,
                        hasStory = true,
                        isSeen = story.viewed,
                        onClick = {
                            val idx = statusStories.indexOf(story)
                            viewModel.openStoryViewer(idx)
                        }
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.userName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val remainingHours = ((story.expiresAt - System.currentTimeMillis()) / (1000 * 60 * 60)).coerceAtLeast(1)
                            Text(
                                text = "Today, ${sdf.format(Date(story.createdAt))} • Expires in ${remainingHours}h",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Full-screen Story Viewer
        if (activeStoryIndex != null && activeStoryIndex!! in statusStories.indices) {
            val story = statusStories[activeStoryIndex!!]
            val progress by viewModel.storyProgress.collectAsState()

            StatusViewerScreen(
                story = story,
                progress = progress,
                onClose = { viewModel.closeStoryViewer() },
                onNext = { viewModel.nextStory() },
                onPrevious = { viewModel.previousStory() },
                onPause = { viewModel.pauseStory() },
                onResume = { viewModel.resumeStory() },
                onReply = { replyText ->
                    onReplyToStory(story.userName, replyText)
                    viewModel.closeStoryViewer()
                }
            )
        }

        // Create Status Dialog
        if (showCreateDialog) {
            CreateStatusDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { text, colorHex ->
                    viewModel.createTextStatus(text, colorHex)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun StatusViewerScreen(
    story: StatusStory,
    progress: Float,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(story.backgroundColorHex))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPause()
                        tryAwaitRelease()
                        onResume()
                    },
                    onTap = { offset ->
                        if (offset.x < size.width / 3f) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with segmented progress indicator
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = story.userName, size = 36.dp, showOnlineBadge = false)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = story.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "24h Status", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Center Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
            }

            // Bottom Reply Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${story.userName}...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onReply(replyText)
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Reply", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CreateStatusDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long) -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    val colors = listOf(
        0xFF4F46E5, // Indigo
        0xFF0D9488, // Teal
        0xFFEA580C, // Orange
        0xFFE11D48, // Rose
        0xFF7C3AED, // Violet
        0xFF1E293B  // Slate
    )
    var selectedColor by remember { mutableLongStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New 24h Status", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(selectedColor))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        placeholder = { Text("Type your status...", color = Color.White.copy(alpha = 0.8f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Background Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textContent.isNotBlank()) {
                        onCreate(textContent, selectedColor)
                    }
                },
                enabled = textContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Post Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
