package com.thechatters.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.thechatters.app.data.model.CallType
import com.thechatters.app.data.repository.ChatRepository
import com.thechatters.app.ui.theme.IndigoPrimary
import com.thechatters.app.viewmodel.BrainAiViewModel
import com.thechatters.app.viewmodel.CallViewModel
import com.thechatters.app.viewmodel.ChatViewModel
import com.thechatters.app.viewmodel.StatusViewModel

enum class MainTab {
    CHATS,
    GROUPS,
    STATUS,
    CALLS,
    PROFILE,
    BRAIN_AI
}

enum class ActiveSubScreen {
    NONE,
    CHAT_DETAIL,
    STICKER_MAKER
}

@Composable
fun MainScreen(
    repository: ChatRepository,
    chatViewModel: ChatViewModel,
    statusViewModel: StatusViewModel,
    callViewModel: CallViewModel,
    brainAiViewModel: BrainAiViewModel
) {
    var selectedTab by remember { mutableStateOf(MainTab.CHATS) }
    var activeSubScreen by remember { mutableStateOf(ActiveSubScreen.NONE) }

    val activeChatId by chatViewModel.activeChatId.collectAsState()
    val allChats by chatViewModel.allChats.collectAsState()
    val totalUnread = allChats.sumOf { it.unreadCount }

    // Handle back button navigation
    BackHandler(enabled = activeSubScreen != ActiveSubScreen.NONE || selectedTab != MainTab.CHATS) {
        if (activeSubScreen != ActiveSubScreen.NONE) {
            if (activeSubScreen == ActiveSubScreen.CHAT_DETAIL) {
                chatViewModel.closeChat()
            }
            activeSubScreen = ActiveSubScreen.NONE
        } else if (selectedTab != MainTab.CHATS) {
            selectedTab = MainTab.CHATS
        }
    }

    when (activeSubScreen) {
        ActiveSubScreen.CHAT_DETAIL -> {
            ChatDetailScreen(
                viewModel = chatViewModel,
                onNavigateBack = {
                    chatViewModel.closeChat()
                    activeSubScreen = ActiveSubScreen.NONE
                },
                onOpenStickerMaker = {
                    activeSubScreen = ActiveSubScreen.STICKER_MAKER
                },
                onStartCall = { contact, type ->
                    callViewModel.startCall(contact, type)
                },
                onOpenBrainAi = {
                    selectedTab = MainTab.BRAIN_AI
                    activeSubScreen = ActiveSubScreen.NONE
                }
            )
        }
        ActiveSubScreen.STICKER_MAKER -> {
            StickerMakerScreen(
                onNavigateBack = {
                    activeSubScreen = if (activeChatId != null) ActiveSubScreen.CHAT_DETAIL else ActiveSubScreen.NONE
                },
                onStickerCreated = { stickerBitmap ->
                    if (activeChatId != null) {
                        chatViewModel.sendSticker(stickerBitmap)
                        activeSubScreen = ActiveSubScreen.CHAT_DETAIL
                    } else {
                        // send to first chat
                        val firstChat = allChats.firstOrNull()?.id ?: "chat_sipho"
                        chatViewModel.openChat(firstChat)
                        chatViewModel.sendSticker(stickerBitmap)
                        activeSubScreen = ActiveSubScreen.CHAT_DETAIL
                    }
                }
            )
        }
        ActiveSubScreen.NONE -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // 1. Chats Tab
                        NavigationBarItem(
                            selected = selectedTab == MainTab.CHATS,
                            onClick = { selectedTab = MainTab.CHATS },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalUnread > 0) {
                                            Badge(containerColor = IndigoPrimary) {
                                                Text("$totalUnread")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "Chats")
                                }
                            },
                            label = { Text("Chats") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(alpha = 0.2f))
                        )

                        // 2. Groups Tab
                        NavigationBarItem(
                            selected = selectedTab == MainTab.GROUPS,
                            onClick = { selectedTab = MainTab.GROUPS },
                            icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                            label = { Text("Groups") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(alpha = 0.2f))
                        )

                        // 3. Status Tab
                        NavigationBarItem(
                            selected = selectedTab == MainTab.STATUS,
                            onClick = { selectedTab = MainTab.STATUS },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Circle,
                                        contentDescription = "Status",
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            label = { Text("Status") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(alpha = 0.2f))
                        )

                        // 4. Calls Tab
                        NavigationBarItem(
                            selected = selectedTab == MainTab.CALLS,
                            onClick = { selectedTab = MainTab.CALLS },
                            icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                            label = { Text("Calls") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(alpha = 0.2f))
                        )

                        // 5. Profile Tab
                        NavigationBarItem(
                            selected = selectedTab == MainTab.PROFILE,
                            onClick = { selectedTab = MainTab.PROFILE },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(alpha = 0.2f))
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        MainTab.CHATS -> {
                            ChatsScreen(
                                chatViewModel = chatViewModel,
                                statusViewModel = statusViewModel,
                                onChatSelected = { chatId ->
                                    chatViewModel.openChat(chatId)
                                    activeSubScreen = ActiveSubScreen.CHAT_DETAIL
                                },
                                onCreateGroupClick = {
                                    selectedTab = MainTab.GROUPS
                                },
                                onOpenStickerMaker = {
                                    activeSubScreen = ActiveSubScreen.STICKER_MAKER
                                },
                                onOpenProfile = {
                                    selectedTab = MainTab.PROFILE
                                },
                                onOpenBrainAi = {
                                    selectedTab = MainTab.BRAIN_AI
                                }
                            )
                        }
                        MainTab.GROUPS -> {
                            GroupsScreen(
                                viewModel = chatViewModel,
                                onGroupSelected = { groupId ->
                                    chatViewModel.openChat(groupId)
                                    activeSubScreen = ActiveSubScreen.CHAT_DETAIL
                                }
                            )
                        }
                        MainTab.STATUS -> {
                            StatusScreen(
                                viewModel = statusViewModel,
                                onReplyToStory = { contactName, replyText ->
                                    // Send direct reply message to contact
                                    val targetChat = allChats.find { it.name.contains(contactName, ignoreCase = true) }
                                    if (targetChat != null) {
                                        chatViewModel.openChat(targetChat.id)
                                        chatViewModel.sendMessage("Replied to your status: \"$replyText\"")
                                        activeSubScreen = ActiveSubScreen.CHAT_DETAIL
                                    }
                                }
                            )
                        }
                        MainTab.CALLS -> {
                            CallsScreen(
                                viewModel = callViewModel,
                                onStartCall = { contact, type ->
                                    callViewModel.startCall(contact, type)
                                }
                            )
                        }
                        MainTab.PROFILE -> {
                            ProfileScreen(
                                repository = repository,
                                onNavigateBack = { selectedTab = MainTab.CHATS }
                            )
                        }
                        MainTab.BRAIN_AI -> {
                            BrainAiScreen(
                                viewModel = brainAiViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

