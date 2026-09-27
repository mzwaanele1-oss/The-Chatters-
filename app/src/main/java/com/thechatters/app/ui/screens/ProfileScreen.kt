package com.thechatters.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddToDrive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.data.model.BackupFrequency
import com.thechatters.app.data.repository.ChatRepository
import com.thechatters.app.ui.components.UserAvatar
import com.thechatters.app.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: ChatRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val backupConfig by repository.backupConfig.collectAsState()
    val backupStatus by repository.backupStatus.collectAsState()
    val isBackingUp by repository.isBackingUp.collectAsState()
    val appSettings by repository.appSettings.collectAsState()

    var selectedSectionTab by remember { mutableIntStateOf(0) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAppLockDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = IndigoPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Profile Card Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(name = currentUser.name, size = 68.dp, showOnlineBadge = true)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                                .clickable { showEditProfileDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser.phoneNumber,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser.bio,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier.testTag("open_qr_button")
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "My QR Code", tint = IndigoPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-navigation tabs: Settings vs Google Backup
            TabRow(
                selectedTabIndex = selectedSectionTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSectionTab == 0,
                    onClick = { selectedSectionTab = 0 },
                    text = { Text("Settings", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedSectionTab == 1,
                    onClick = { selectedSectionTab = 1 },
                    text = { Text("Cloud Backup", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedSectionTab == 0) {
                // ==================== SETTINGS TAB ====================
                Text("App & Privacy Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Privacy Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacy", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Last Seen & Online", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Visible to: ${appSettings.privacyLastSeen}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = {
                                val next = if (appSettings.privacyLastSeen == "Everyone") "My Contacts" else "Everyone"
                                repository.updateAppSettings(appSettings.copy(privacyLastSeen = next))
                            }) {
                                Text(appSettings.privacyLastSeen)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Read Receipts (Blue Ticks)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Show when you have read messages", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = appSettings.privacyReadReceipts,
                                onCheckedChange = { repository.updateAppSettings(appSettings.copy(privacyReadReceipts = it)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Appearance & Dark/Light Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dark / Light Theme", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(if (appSettings.isDarkMode) "Dark theme enabled" else "Light theme enabled", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = appSettings.isDarkMode,
                            onCheckedChange = { repository.toggleDarkMode(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. AppLock PIN / Biometric (OFF by default)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("AppLock PIN / Biometric", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("Default: OFF. Protect with PIN/Fingerprint", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = appSettings.isAppLockEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        showAppLockDialog = true
                                    } else {
                                        repository.setAppLock(false, "", false)
                                    }
                                },
                                modifier = Modifier.testTag("applock_switch")
                            )
                        }

                        if (appSettings.isAppLockEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AppLock active (PIN: **** • Biometrics enabled)", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Storage Usage Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Storage Usage Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text("105.1 MB", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        StorageItemRow(label = "Chats & Messages", sizeStr = "24.5 MB", percentage = 0.23f, barColor = IndigoPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        StorageItemRow(label = "Photos & Videos", sizeStr = "68.2 MB", percentage = 0.65f, barColor = Color(0xFF06B6D4))
                        Spacer(modifier = Modifier.height(8.dp))
                        StorageItemRow(label = "Stickers (/stickers/)", sizeStr = "12.4 MB", percentage = 0.12f, barColor = Color(0xFFF59E0B))

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                repository.clearCache()
                                Toast.makeText(context, "Cache and temporary media cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Cache")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Notifications Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Message & Group Notifications", fontSize = 14.sp)
                            Switch(
                                checked = appSettings.notificationsEnabled,
                                onCheckedChange = { repository.updateAppSettings(appSettings.copy(notificationsEnabled = it)) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sound & In-app Alert", fontSize = 14.sp)
                            Switch(
                                checked = appSettings.notificationSound,
                                onCheckedChange = { repository.updateAppSettings(appSettings.copy(notificationSound = it)) }
                            )
                        }
                    }
                }
            } else {
                // ==================== CLOUD BACKUP TAB ====================
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddToDrive, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Sign-In & WorkManager Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Automated WorkManager exports JSON payload of selected items and uploads to Storage /backups/.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Google Account Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Google Account", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(backupConfig.driveAccount, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Status: Connected (OAuth2 verified)", fontSize = 11.sp, color = Color(0xFF16A34A))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF34A853).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Drive Ready", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Backup Items Selection Checkboxes
                Text("Choose Backup Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = backupConfig.backupChats,
                                onCheckedChange = { repository.updateBackupItems(it, backupConfig.backupMedia, backupConfig.backupStickers) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Chats Database", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Conversations, messages, reactions history", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = backupConfig.backupMedia,
                                onCheckedChange = { repository.updateBackupItems(backupConfig.backupChats, it, backupConfig.backupStickers) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Media Content", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Photos, voice notes, and videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = backupConfig.backupStickers,
                                onCheckedChange = { repository.updateBackupItems(backupConfig.backupChats, backupConfig.backupMedia, it) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Custom Stickers (512x512)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Saved packages in /stickers/ storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule via WorkManager
                Text("Schedule via WorkManager", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        BackupFrequencyOption(
                            label = "Daily (Runs every day at 2:00 AM)",
                            selected = backupConfig.frequency == BackupFrequency.DAILY_2AM,
                            onSelect = { repository.updateBackupFrequency(BackupFrequency.DAILY_2AM) }
                        )
                        BackupFrequencyOption(
                            label = "Weekly (Runs once every 7 days)",
                            selected = backupConfig.frequency == BackupFrequency.WEEKLY,
                            onSelect = { repository.updateBackupFrequency(BackupFrequency.WEEKLY) }
                        )
                        BackupFrequencyOption(
                            label = "Monthly (Runs once every 30 days)",
                            selected = backupConfig.frequency == BackupFrequency.MONTHLY,
                            onSelect = { repository.updateBackupFrequency(BackupFrequency.MONTHLY) }
                        )
                        BackupFrequencyOption(
                            label = "Manual (Only when I tap Back Up Now)",
                            selected = backupConfig.frequency == BackupFrequency.MANUAL,
                            onSelect = { repository.updateBackupFrequency(BackupFrequency.MANUAL) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wi-Fi Only & Video Options
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Back up over Wi-Fi only", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = backupConfig.wifiOnly,
                                onCheckedChange = { repository.updateBackupSettings(it, backupConfig.includeVideos) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Videos in Cloud Backup", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = backupConfig.includeVideos,
                                onCheckedChange = { repository.updateBackupSettings(backupConfig.wifiOnly, it) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Backup Progress & Storage Upload Button Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = backupStatus,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isBackingUp) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                        )

                        if (isBackingUp) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp), color = IndigoPrimary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { repository.triggerManualBackup() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("backup_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                enabled = !isBackingUp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isBackingUp) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Backing up...")
                                } else {
                                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Back Up Now")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Checking latest backup JSON in Storage /backups/...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore")
                            }
                        }
                    }
                }
            }
        }

        // QR Code Dialog
        if (showQrDialog) {
            QrCodeDialog(
                userName = currentUser.name,
                phoneNumber = currentUser.phoneNumber,
                onDismiss = { showQrDialog = false }
            )
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentName = currentUser.name,
                currentBio = currentUser.bio,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, bio ->
                    repository.updateProfile(name, bio)
                    showEditProfileDialog = false
                }
            )
        }

        // AppLock PIN Setup Dialog
        if (showAppLockDialog) {
            AppLockPinDialog(
                onDismiss = { showAppLockDialog = false },
                onSetPin = { pin ->
                    repository.setAppLock(true, pin, true)
                    showAppLockDialog = false
                    Toast.makeText(context, "AppLock PIN activated", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun BackupFrequencyOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 13.sp)
    }
}

@Composable
fun StorageItemRow(label: String, sizeStr: String, percentage: Float, barColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(sizeStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun QrCodeDialog(
    userName: String,
    phoneNumber: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("My QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Scan to chat on The Chatters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vector-styled simulated QR Code matrix card
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (row in 0 until 7) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val isCorner = (row < 2 && (col < 2 || col > 4)) || (row > 4 && col < 2)
                                    val isCenter = row in 2..4 && col in 2..4
                                    val isFilled = isCorner || isCenter || ((row * 7 + col) % 3 == 0)

                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(if (isCorner) 4.dp else 2.dp))
                                            .background(if (isFilled) Color.Black else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }

                    // Logo badge in center of QR
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share QR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Status / About") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), bio.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AppLockPinDialog(
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set AppLock PIN", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a 4-digit security PIN to protect The Chatters when minimized:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                    label = { Text("4-Digit PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pin.length == 4) onSetPin(pin) },
                enabled = pin.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Enable PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
