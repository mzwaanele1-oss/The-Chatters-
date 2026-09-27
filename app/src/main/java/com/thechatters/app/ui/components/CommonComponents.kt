package com.thechatters.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thechatters.app.ui.theme.AccentTeal
import com.thechatters.app.ui.theme.IndigoLight
import com.thechatters.app.ui.theme.IndigoPrimary
import com.thechatters.app.ui.theme.OnlineGreen

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 48.dp,
    avatarUrl: String? = null,
    isOnline: Boolean = false,
    showOnlineBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        // Generate consistent gradient per name
        val hash = name.hashCode()
        val gradient = when (kotlin.math.abs(hash) % 4) {
            0 -> Brush.linearGradient(listOf(IndigoPrimary, IndigoLight))
            1 -> Brush.linearGradient(listOf(Color(0xFF0D9488), AccentTeal))
            2 -> Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E)))
            else -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.4f).sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }

        if (showOnlineBadge && isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun StatusRingAvatar(
    name: String,
    size: Dp = 56.dp,
    hasStory: Boolean = true,
    isSeen: Boolean = false,
    onClick: () -> Unit
) {
    val ringColor = if (isSeen) Color.Gray else IndigoPrimary
    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = if (hasStory) 2.5.dp else 0.dp,
                color = if (hasStory) ringColor else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        UserAvatar(
            name = name,
            size = size - 6.dp,
            showOnlineBadge = false
        )
    }
}
