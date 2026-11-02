package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.MarkdownCodeViewer
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGoldAccent
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.viewmodel.JarvisViewModel
import com.example.viewmodel.StudioMode

@Composable
fun StudioScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.studioMode.collectAsState()
    val topic by viewModel.studioTopic.collectAsState()
    val details by viewModel.studioDetails.collectAsState()
    val language by viewModel.studioLanguage.collectAsState()
    val tone by viewModel.studioTone.collectAsState()
    val result by viewModel.studioResult.collectAsState()
    val isGenerating by viewModel.isStudioGenerating.collectAsState()

    val studioTabs = listOf(
        StudioMode.ESSAY to ("Essay" to Icons.Default.EditNote),
        StudioMode.STORY to ("Stories" to Icons.Default.AutoStories),
        StudioMode.CODING to ("Coding" to Icons.Default.Code),
        StudioMode.IMAGE_PROMPT to ("Images" to Icons.Default.Image)
    )

    val tonesForMode = when (mode) {
        StudioMode.ESSAY -> listOf("Analytical & Academic", "Persuasive & Bold", "Visionary & Tech", "Philosophical", "Executive Summary")
        StudioMode.STORY -> listOf("Sci-Fi Cyberpunk", "Iron Man / Tech Thriller", "Cosmic Mystery", "Dystopian Noir", "Epic Fantasy")
        StudioMode.CODING -> listOf("Clean Architecture", "High Performance", "Beginner Friendly", "Algorithmic Precision")
        StudioMode.IMAGE_PROMPT -> listOf("Photorealistic 8K", "Cyberpunk Neon", "Cinematic 35mm", "Stark Holographic HUD", "Concept Art")
    }

    val programmingLanguages = listOf("Python", "Kotlin", "TypeScript", "C++", "Rust", "Swift", "Go", "SQL", "HTML/CSS")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.JarvisSurface)
                    .border(1.5.dp, com.example.ui.theme.JarvisAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_jarvis_robot),
                    contentDescription = "JARVIS Robot",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "JARVIS CREATION STUDIO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.JarvisAmber,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Generate essays, stories, production code, and image prompts",
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Tab Row
        TabRow(
            selectedTabIndex = studioTabs.indexOfFirst { it.first == mode },
            containerColor = JarvisSurface,
            contentColor = JarvisCyanPrimary,
            indicator = { tabPositions ->
                val currentIndex = studioTabs.indexOfFirst { it.first == mode }
                if (currentIndex >= 0 && currentIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentIndex]),
                        color = JarvisCyanPrimary
                    )
                }
            }
        ) {
            studioTabs.forEach { (studioModeItem, info) ->
                val isSelected = mode == studioModeItem
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setStudioMode(studioModeItem) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = info.second,
                                contentDescription = null,
                                tint = if (isSelected) JarvisCyanPrimary else JarvisTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = info.first,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) JarvisCyanLight else JarvisTextMuted
                            )
                        }
                    },
                    modifier = Modifier.testTag("studio_tab_${studioModeItem.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Coding language selector if in CODING mode
        if (mode == StudioMode.CODING) {
            Text(
                text = "TARGET LANGUAGE",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisCyanLight,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(programmingLanguages) { lang ->
                    val isSelected = language == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) JarvisCyanPrimary else JarvisSurfaceElevated)
                            .border(1.dp, if (isSelected) JarvisCyanLight else JarvisBorderCyan, RoundedCornerShape(8.dp))
                            .clickable { viewModel.updateStudioLanguage(lang) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = lang,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else JarvisTextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tone & Style Chips
        Text(
            text = if (mode == StudioMode.CODING) "ARCHITECTURAL STYLE" else if (mode == StudioMode.IMAGE_PROMPT) "AESTHETIC STYLE" else "TONE & ATMOSPHERE",
            style = MaterialTheme.typography.labelSmall,
            color = JarvisCyanLight,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tonesForMode) { toneItem ->
                val isSelected = tone == toneItem
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) JarvisCyanPrimary.copy(alpha = 0.2f) else JarvisSurfaceElevated)
                        .border(1.dp, if (isSelected) JarvisCyanPrimary else JarvisBorderCyan, RoundedCornerShape(16.dp))
                        .clickable { viewModel.updateStudioTone(toneItem) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = toneItem,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) JarvisCyanLight else JarvisTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Topic / Prompt input
        OutlinedTextField(
            value = topic,
            onValueChange = { viewModel.updateStudioTopic(it) },
            label = {
                Text(
                    when (mode) {
                        StudioMode.ESSAY -> "Essay Topic / Argument (e.g., Future of Artificial General Intelligence)"
                        StudioMode.STORY -> "Story Premise (e.g., Tony Stark investigates an alien probe in orbit)"
                        StudioMode.CODING -> "Algorithm / Application Feature to Code"
                        StudioMode.IMAGE_PROMPT -> "Visual Concept / Scene to Generate"
                    },
                    color = JarvisCyanLight
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("studio_topic_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyanPrimary,
                unfocusedBorderColor = JarvisBorderCyan,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                focusedContainerColor = JarvisSurfaceElevated,
                unfocusedContainerColor = JarvisSurfaceElevated
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Details / Constraints input
        OutlinedTextField(
            value = details,
            onValueChange = { viewModel.updateStudioDetails(it) },
            label = {
                Text(
                    "Additional Specifications / Key Points (Optional)",
                    color = JarvisTextMuted
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("studio_details_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyanPrimary,
                unfocusedBorderColor = JarvisBorderCyan,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                focusedContainerColor = JarvisSurfaceElevated,
                unfocusedContainerColor = JarvisSurfaceElevated
            ),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Generate Action Button
        Button(
            onClick = { viewModel.generateStudioCreation() },
            enabled = topic.isNotBlank() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("studio_generate_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = JarvisCyanPrimary,
                disabledContainerColor = JarvisSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JARVIS SYNTHESIZING...",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (topic.isNotBlank()) Color.Black else JarvisTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE ${mode.name.replace("_", " ")}",
                    color = if (topic.isNotBlank()) Color.Black else JarvisTextMuted,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result Card
        if (result.isNotEmpty() || isGenerating) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, JarvisCyanPrimary, RoundedCornerShape(12.dp)),
                color = JarvisSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENERATED OUTPUT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanLight,
                            letterSpacing = 1.sp
                        )
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = JarvisCyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MarkdownCodeViewer(
                        text = result,
                        onSpeak = if (mode != StudioMode.CODING) {
                            { viewModel.speakText(result) }
                        } else null
                    )
                }
            }
        }
    }
}
