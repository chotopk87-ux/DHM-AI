package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGoldAccent
import com.example.ui.theme.JarvisGreenOnline
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.viewmodel.JarvisViewModel

@Composable
fun VoiceCallScreen(
    viewModel: JarvisViewModel,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCallActive by viewModel.isCallActive.collectAsState()
    val durationSeconds by viewModel.callDurationSeconds.collectAsState()
    val isMuted by viewModel.isCallMuted.collectAsState()
    val statusText by viewModel.callStatusText.collectAsState()
    val transcripts by viewModel.callTranscripts.collectAsState()

    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val audioRms by viewModel.voiceManager.audioRms.collectAsState()

    val transcriptListState = rememberLazyListState()

    // Pulse animation for call HUD
    val infiniteTransition = rememberInfiniteTransition(label = "call_hud")
    val hudGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_glow"
    )

    LaunchedEffect(transcripts.size) {
        if (transcripts.isNotEmpty()) {
            transcriptListState.animateScrollToItem(transcripts.size - 1)
        }
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top HUD Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isCallActive) JarvisGreenOnline else JarvisRedAlert)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALL LANGUAGES LIVE LINK ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.ui.theme.JarvisEmerald,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "J.A.R.V.I.S.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = JarvisCyanPrimary,
                letterSpacing = 3.sp
            )

            Text(
                text = formattedDuration,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = JarvisTextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSpeaking) JarvisCyanLight else if (isListening) JarvisGoldAccent else JarvisTextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Center: Robot Visualizer Core with Dynamic Audio Halo Rings
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Concentric HUD outer pulse ring
            Box(
                modifier = Modifier
                    .size((235 + (audioRms.coerceIn(0f, 10f) * 4)).dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        if (isSpeaking) JarvisCyanPrimary.copy(alpha = hudGlow)
                        else if (isListening) JarvisGoldAccent.copy(alpha = hudGlow)
                        else JarvisBorderCyan.copy(alpha = 0.35f),
                        CircleShape
                    )
            )

            // Inner HUD ring
            Box(
                modifier = Modifier
                    .size((200 + (audioRms.coerceIn(0f, 10f) * 2)).dp)
                    .clip(CircleShape)
                    .border(
                        1.dp,
                        if (isSpeaking) JarvisCyanLight.copy(alpha = 0.6f)
                        else if (isListening) JarvisGoldAccent.copy(alpha = 0.6f)
                        else JarvisBorderCyan.copy(alpha = 0.25f),
                        CircleShape
                    )
            )

            // Robot Avatar Picture
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .clip(CircleShape)
                    .background(JarvisCyanDark)
                    .border(
                        3.dp,
                        if (isSpeaking) JarvisCyanPrimary else if (isListening) JarvisGoldAccent else JarvisCyanPrimary.copy(alpha = 0.7f),
                        CircleShape
                    ),
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
        }

        // Live Spoken Subtitle / Transcription HUD Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, JarvisBorderCyan.copy(alpha = hudGlow), RoundedCornerShape(14.dp)),
            color = JarvisSurfaceElevated.copy(alpha = 0.85f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE SPEECH TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = JarvisCyanLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isSpeaking || isListening) JarvisCyanPrimary else JarvisTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSpeaking) "JARVIS TRANSMITTING" else if (isListening) "RECEIVING USER AUDIO" else "STANDBY",
                            fontSize = 9.sp,
                            color = JarvisTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    state = transcriptListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transcripts) { (speaker, text) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "$speaker: ",
                                fontWeight = FontWeight.Bold,
                                color = if (speaker == "JARVIS") JarvisCyanPrimary else JarvisGoldAccent,
                                fontSize = 12.sp
                            )
                            Text(
                                text = text,
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Call Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Button
            IconButton(
                onClick = { viewModel.toggleCallMute() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) JarvisRedAlert.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                    .border(1.dp, if (isMuted) JarvisRedAlert else JarvisBorderCyan, CircleShape)
                    .testTag("call_mute_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = if (isMuted) JarvisRedAlert else JarvisCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // End Call Button
            FloatingActionButton(
                onClick = {
                    viewModel.endVoiceCall()
                    onEndCall()
                },
                containerColor = JarvisRedAlert,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(68.dp)
                    .testTag("call_end_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Voice Call",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Push-to-Talk / Force Listen Button
            IconButton(
                onClick = {
                    if (isSpeaking) {
                        viewModel.voiceManager.stopSpeaking()
                    }
                    viewModel.toggleVoiceListening()
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isListening) JarvisGoldAccent.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                    .border(1.dp, if (isListening) JarvisGoldAccent else JarvisBorderCyan, CircleShape)
                    .testTag("call_listen_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Tap to speak directly",
                    tint = if (isListening) JarvisGoldAccent else JarvisCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
