package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessageEntity
import com.example.ui.components.MarkdownCodeViewer
import com.example.ui.components.VoiceInputBar
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisAmberContainer
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCoral
import com.example.ui.theme.JarvisCoralContainer
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisEmeraldContainer
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisRose
import com.example.ui.theme.JarvisRoseContainer
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSkyContainer
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisTextWhite
import com.example.ui.theme.JarvisViolet
import com.example.ui.theme.JarvisVioletContainer
import com.example.viewmodel.JarvisViewModel

@Composable
fun ChatScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.chatInputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.streamingResponse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedBitmap by viewModel.selectedImageBitmap.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val userAvatarBitmap by viewModel.userAvatarBitmap.collectAsState()
    val isAutoVoiceEnabled by viewModel.isAutoVoiceEnabled.collectAsState()

    val listState = rememberLazyListState()

    // Photo picker for attachments (multimodal queries)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = com.example.util.BitmapUtil.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.selectImage(bitmap)
            }
        }
    }

    // Photo picker specifically for changing User Profile Avatar ("we can change our image")
    val userAvatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = com.example.util.BitmapUtil.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.setUserAvatar(bitmap)
            }
        }
    }

    // Auto-scroll on new message or generation start
    LaunchedEffect(messages.size, isGenerating) {
        val totalItems = messages.size + (if (isGenerating) 1 else 0)
        if (totalItems > 0) {
            listState.scrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        // Quick Prompt Suggestions Chips & User Avatar Customizer (when chat is empty or fresh)
        if (messages.isEmpty() && !isGenerating) {
            JarvisWelcomeBanner(
                userAvatar = userAvatarBitmap,
                onChangeUserAvatar = {
                    userAvatarPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSelectPrompt = { prompt ->
                    viewModel.updateChatInput(prompt)
                }
            )
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    userAvatar = userAvatarBitmap,
                    onChangeUserAvatar = {
                        userAvatarPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSpeak = {
                        viewModel.speakText(message.text)
                    }
                )
            }

            // Real-time Streaming message placeholder
            if (isGenerating && streamingText.isNotEmpty()) {
                item(key = "streaming_bubble") {
                    StreamingMessageItem(
                        streamingText = streamingText
                    )
                }
            } else if (isGenerating) {
                item(key = "loading_bubble") {
                    JarvisThinkingIndicator()
                }
            }
        }

        // Error Banner if network/attestation error occurred
        AnimatedVisibility(visible = errorMessage != null) {
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = JarvisRedAlert.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRedAlert)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = JarvisRedAlert,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = JarvisRedAlert,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "DISMISS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JarvisViolet,
                            modifier = Modifier
                                .clickable { viewModel.clearErrorMessage() }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom Voice & Text Input Bar
        VoiceInputBar(
            inputText = inputText,
            onInputTextChange = { viewModel.updateChatInput(it) },
            onSendMessage = { viewModel.sendChatMessage() },
            onSelectImage = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onToggleVoiceInput = { viewModel.toggleVoiceListening() },
            isListening = isListening,
            isLoading = isGenerating,
            selectedImageBitmap = selectedBitmap,
            onRemoveImage = { viewModel.selectImage(null) },
            isAutoVoiceEnabled = isAutoVoiceEnabled,
            onToggleAutoVoice = { viewModel.toggleAutoVoice() }
        )
    }
}

@Composable
private fun JarvisWelcomeBanner(
    userAvatar: Bitmap?,
    onChangeUserAvatar: () -> Unit,
    onSelectPrompt: (String) -> Unit
) {
    val quickPrompts = listOf(
        Triple("🌐 All Languages", "Ask in any language (Hindi, Spanish, French, Arabic, German, Bengali, Japanese...)", JarvisEmerald),
        Triple("⚡ Quantum Physics", "Explain Quantum Computing gently in simple everyday terms", JarvisSky),
        Triple("✍️ Creative Story", "Write a captivating, heartwarming sci-fi story about a robot and an astronomer", JarvisRose),
        Triple("💻 Clean Python Code", "Write a complete Python script with error handling and documentation", JarvisCoral),
        Triple("🎨 Cyber Visual Art", "Generate a vivid, photorealistic prompt for a futuristic crystal city", JarvisViolet),
        Triple("📐 Step-by-Step Math", "Solve step-by-step: How to calculate projectile range with air resistance", JarvisAmber)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Avatars Row: JARVIS Robot & Custom User Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // JARVIS Robot Picture Hero
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(2.5.dp, JarvisViolet, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_jarvis_robot),
                    contentDescription = "JARVIS Robot",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Profile Avatar with "Change Our Image" Button
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(JarvisVioletContainer)
                    .border(2.5.dp, JarvisCoral, CircleShape)
                    .clickable { onChangeUserAvatar() }
                    .testTag("welcome_change_user_avatar"),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatar != null) {
                    Image(
                        bitmap = userAvatar.asImageBitmap(),
                        contentDescription = "Your Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Your Image",
                            tint = JarvisCoral,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Set Photo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCoral
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Change avatar callout
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisCoralContainer)
                .border(1.dp, JarvisCoral.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onChangeUserAvatar() }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = JarvisCoral,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (userAvatar != null) "Change Your Image" else "Tap to Add Your Photo",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisCoral
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "JARVIS INTELLIGENCE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = JarvisViolet,
            letterSpacing = 1.5.sp
        )

        // Multilingual & Polite Subtitle Badge
        Row(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisEmeraldContainer)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = JarvisEmerald,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Understands & Speaks Every Language Softly and Politely",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = JarvisEmerald
            )
        }

        Text(
            text = "Ask any question in your language, upload photos to solve equations, converse via voice call, or generate essays and code.",
            style = MaterialTheme.typography.bodySmall,
            color = JarvisTextMuted,
            modifier = Modifier.padding(bottom = 12.dp),
            lineHeight = 17.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Horizontal Prompt Suggestions with Colorful Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { (badge, prompt, color) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(JarvisSurface)
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { onSelectPrompt(prompt) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 10.sp
                            )
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = JarvisTextPrimary,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    userAvatar: Bitmap?,
    onChangeUserAvatar: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // JARVIS Robot Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.5.dp, JarvisViolet, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_jarvis_robot),
                    contentDescription = "JARVIS Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message bubble
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 0.92f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .border(
                    1.dp,
                    if (isUser) JarvisViolet.copy(alpha = 0.25f) else JarvisBorderCyan,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                ),
            color = if (isUser) JarvisVioletContainer else JarvisSurfaceElevated,
            shadowElevation = if (isUser) 0.dp else 2.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else "J.A.R.V.I.S.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUser) JarvisViolet else JarvisViolet,
                        letterSpacing = 1.sp
                    )

                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisEmeraldContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MULTILINGUAL AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisEmerald
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Speak Button
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(JarvisSurfaceVariant)
                                    .clickable { onSpeak() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud softly",
                                    tint = JarvisViolet,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                MarkdownCodeViewer(
                    text = message.text,
                    onSpeak = null // Handled in header
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar (Custom photo from gallery or default with click-to-change)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JarvisCoralContainer)
                    .border(1.5.dp, JarvisCoral, CircleShape)
                    .clickable { onChangeUserAvatar() }
                    .testTag("user_avatar_chat_item"),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatar != null) {
                    Image(
                        bitmap = userAvatar.asImageBitmap(),
                        contentDescription = "Your Photo - Tap to change",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Your Avatar - Tap to set photo",
                        tint = JarvisCoral,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingMessageItem(
    streamingText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JarvisSurface)
                .border(1.5.dp, JarvisViolet, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_jarvis_robot),
                contentDescription = "JARVIS Streaming",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, JarvisViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = JarvisSurfaceElevated,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "J.A.R.V.I.S. (ANSWERING...)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = JarvisViolet,
                        letterSpacing = 1.sp
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = JarvisViolet
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                MarkdownCodeViewer(text = streamingText)
            }
        }
    }
}

@Composable
private fun JarvisThinkingIndicator() {
    Row(
        modifier = Modifier.padding(start = 44.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = JarvisViolet
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "JARVIS processing in your language...",
            style = MaterialTheme.typography.bodySmall,
            color = JarvisTextMuted,
            fontSize = 12.sp
        )
    }
}
