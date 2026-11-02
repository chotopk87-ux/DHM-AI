package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisEmeraldContainer
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextWhite

@Composable
fun VoiceInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSelectImage: () -> Unit,
    onToggleVoiceInput: () -> Unit,
    isListening: Boolean,
    isLoading: Boolean,
    selectedImageBitmap: Bitmap?,
    onRemoveImage: () -> Unit,
    isAutoVoiceEnabled: Boolean,
    onToggleAutoVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = JarvisSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attached image thumbnail preview if present
            if (selectedImageBitmap != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, JarvisCyanPrimary, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = selectedImageBitmap.asImageBitmap(),
                        contentDescription = "Selected image preview",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { onRemoveImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attached image",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach image button
                IconButton(
                    onClick = onSelectImage,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("attach_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image or photo for analysis",
                        tint = if (selectedImageBitmap != null) JarvisCyanPrimary else JarvisTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text field
                TextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, if (isListening) JarvisCyanPrimary else JarvisBorderCyan, RoundedCornerShape(24.dp))
                        .testTag("chat_input_textfield"),
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening in any language..." else "Ask anything in any language or speak...",
                            fontSize = 13.sp,
                            color = if (isListening) JarvisCyanLight else JarvisTextMuted
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = JarvisSurfaceVariant,
                        unfocusedContainerColor = JarvisSurfaceVariant,
                        disabledContainerColor = JarvisSurfaceVariant,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        cursorColor = JarvisCyanPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Auto-Voice Answer Toggle Button (Answer in Voice: ON / OFF)
                IconButton(
                    onClick = onToggleAutoVoice,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isAutoVoiceEnabled) JarvisEmeraldContainer else JarvisSurfaceVariant)
                        .border(
                            1.dp,
                            if (isAutoVoiceEnabled) JarvisEmerald else JarvisBorderCyan,
                            CircleShape
                        )
                        .testTag("auto_voice_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isAutoVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (isAutoVoiceEnabled) "Voice answers enabled (soft speech)" else "Voice answers disabled",
                        tint = if (isAutoVoiceEnabled) JarvisEmerald else JarvisTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Voice Mic Button (Voice Recognition)
                IconButton(
                    onClick = onToggleVoiceInput,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isListening) JarvisRedAlert.copy(alpha = 0.15f) else JarvisSurfaceVariant)
                        .border(
                            1.dp,
                            if (isListening) JarvisRedAlert else JarvisBorderCyan,
                            CircleShape
                        )
                        .testTag("voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop listening" else "Speak with voice recognition",
                        tint = if (isListening) JarvisRedAlert else JarvisCyanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send Button / Loading Spinner
                IconButton(
                    onClick = onSendMessage,
                    enabled = !isLoading && (inputText.isNotBlank() || selectedImageBitmap != null),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() || selectedImageBitmap != null) JarvisCyanPrimary else JarvisSurfaceVariant
                        )
                        .testTag("send_message_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = JarvisCyanPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message to Jarvis",
                            tint = if (inputText.isNotBlank() || selectedImageBitmap != null) JarvisTextWhite else JarvisTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
