package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGreenOnline
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun JarvisTopBar(
    onStartVoiceCall: () -> Unit,
    onOpenHistory: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_alpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = JarvisSurface,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Title & Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Robot Avatar Emblem
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, JarvisCyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_jarvis_robot),
                            contentDescription = "JARVIS Robot",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "J.A.R.V.I.S.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = com.example.ui.theme.JarvisViolet,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(com.example.ui.theme.JarvisEmeraldContainer)
                                    .border(0.5.dp, com.example.ui.theme.JarvisEmerald, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "MULTILINGUAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.JarvisEmerald
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(JarvisGreenOnline.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALL LANGUAGES READY • SOFT VOICE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = JarvisTextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Actions: Call, History, Clear
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Call Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(JarvisCyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, JarvisCyanPrimary, RoundedCornerShape(20.dp))
                            .clickable { onStartVoiceCall() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("topbar_voice_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call with Jarvis",
                                tint = JarvisCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CALL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyanLight,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("topbar_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Saved history",
                            tint = JarvisTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("topbar_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear conversation",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // HUD Bottom divider line with cyan glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(JarvisBorderCyan)
                    .size(width = 0.dp, height = 1.dp)
            )
        }
    }
}
