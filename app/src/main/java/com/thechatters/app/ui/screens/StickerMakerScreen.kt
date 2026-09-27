package com.thechatters.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerMakerScreen(
    onNavigateBack: () -> Unit,
    onStickerCreated: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var selectedEmoji by remember { mutableStateOf("🔥") }
    var selectedBorderColor by remember { mutableStateOf(Color.White) }
    var hasWhiteBorder by remember { mutableStateOf(true) }

    // Transform states for 512x512 crop
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var stickerText by remember { mutableStateOf("VIBES 🔥") }
    var showTextInput by remember { mutableStateOf(true) }

    val emojis = listOf("🔥", "😂", "❤️", "🇸🇿", "🚀", "🎉", "👑", "✨", "💯", "👀")

    // Activity launcher for image picking
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // In real Android or test environment, uri is selected
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sticker Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Export standard 512x512 WebP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scale = 1f
                        rotation = 0f
                        offset = Offset.Zero
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset or pick other
                            scale = 1.2f
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fit 512px")
                    }

                    Button(
                        onClick = {
                            // Generate 512x512 sticker bitmap with text
                            val stickerBitmap = create512x512StickerBitmap(
                                emoji = selectedEmoji,
                                hasBorder = hasWhiteBorder,
                                text = if (showTextInput) stickerText else ""
                            )
                            onStickerCreated(stickerBitmap)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("export_sticker_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Sticker", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 512x512 Crop Canvas Box (constrained to square display)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, IndigoPrimary, RoundedCornerShape(16.dp))
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotate ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            rotation += rotate
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background subtle checkerboard or gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF0F172A)
                        )
                )

                // The sticker graphic content (interactive)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .clip(CircleShape)
                        .background(if (hasWhiteBorder) Color(0xFF4F46E5) else Color.Transparent)
                        .border(if (hasWhiteBorder) 6.dp else 0.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedEmoji,
                            fontSize = if (showTextInput && stickerText.isNotBlank()) 64.sp else 80.sp
                        )
                        if (showTextInput && stickerText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stickerText.uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Dimension Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "512 x 512 px",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pinch to zoom • Drag to position • Rotate",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Emoji / Sticker Presets row
            Text(
                text = "Choose Sticker Graphic",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                emojis.take(5).forEach { emoji ->
                    FilterChip(
                        selected = selectedEmoji == emoji,
                        onClick = { selectedEmoji = emoji },
                        label = { Text(emoji, fontSize = 20.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                emojis.drop(5).forEach { emoji ->
                    FilterChip(
                        selected = selectedEmoji == emoji,
                        onClick = { selectedEmoji = emoji },
                        label = { Text(emoji, fontSize = 20.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input for sticker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Custom Text",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.Switch(
                    checked = showTextInput,
                    onCheckedChange = { showTextInput = it }
                )
            }

            if (showTextInput) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = stickerText,
                    onValueChange = { stickerText = it },
                    label = { Text("Sticker Text / Caption") },
                    placeholder = { Text("e.g. SWAZI VIBE 🇸🇿") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("VIBES 🔥", "SIYABONGA 🙏", "HEITA 👋", "CHILL 😎").forEach { quickText ->
                        androidx.compose.material3.SuggestionChip(
                            onClick = { stickerText = quickText },
                            label = { Text(quickText, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sticker Border style toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Die-Cut White Sticker Border",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.Switch(
                    checked = hasWhiteBorder,
                    onCheckedChange = { hasWhiteBorder = it }
                )
            }
        }
    }
}

// 512x512 Bitmap creation for WhatsApp / Telegram standard sticker format
fun create512x512StickerBitmap(emoji: String, hasBorder: Boolean, text: String = ""): Bitmap {
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Clear transparent
    canvas.drawColor(android.graphics.Color.TRANSPARENT)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = if (text.isNotBlank()) 200f else 240f
        textAlign = Paint.Align.CENTER
    }

    if (hasBorder) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#4F46E5")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 24f
        }
        canvas.drawCircle(256f, 256f, 210f, bgPaint)
        canvas.drawCircle(256f, 256f, 210f, borderPaint)
    }

    val yPos = if (text.isNotBlank()) 230f else (256f - ((paint.descent() + paint.ascent()) / 2))
    canvas.drawText(emoji, 256f, yPos, paint)

    if (text.isNotBlank()) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
        }
        val textStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawText(text.uppercase(), 256f, 420f, textStroke)
        canvas.drawText(text.uppercase(), 256f, 420f, textPaint)
    }

    return bitmap
}
