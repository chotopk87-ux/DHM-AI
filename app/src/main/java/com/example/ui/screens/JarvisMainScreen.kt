package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JarvisTopBar
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextMuted
import com.example.viewmodel.AppTab
import com.example.viewmodel.JarvisViewModel

@Composable
fun JarvisMainScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isCallActive by viewModel.isCallActive.collectAsState()

    // Handle Back Press
    BackHandler(enabled = currentTab != AppTab.CHAT || isCallActive) {
        if (isCallActive) {
            viewModel.endVoiceCall()
        }
        viewModel.switchTab(AppTab.CHAT)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground),
        topBar = {
            if (currentTab != AppTab.VOICE_CALL) {
                JarvisTopBar(
                    onStartVoiceCall = {
                        viewModel.startVoiceCall()
                        viewModel.switchTab(AppTab.VOICE_CALL)
                    },
                    onOpenHistory = {
                        viewModel.switchTab(AppTab.HISTORY)
                    },
                    onClearChat = {
                        viewModel.clearChat()
                    }
                )
            }
        },
        bottomBar = {
            if (currentTab != AppTab.VOICE_CALL) {
                JarvisBottomNavBar(
                    currentTab = currentTab,
                    onSelectTab = { tab ->
                        if (tab == AppTab.VOICE_CALL) {
                            viewModel.startVoiceCall()
                        }
                        viewModel.switchTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.CHAT -> ChatScreen(viewModel = viewModel)
                AppTab.VOICE_CALL -> VoiceCallScreen(
                    viewModel = viewModel,
                    onEndCall = { viewModel.switchTab(AppTab.CHAT) }
                )
                AppTab.VISION_SOLVER -> VisionSolverScreen(viewModel = viewModel)
                AppTab.STUDIO -> StudioScreen(viewModel = viewModel)
                AppTab.HISTORY -> SavedHistoryScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun JarvisBottomNavBar(
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = JarvisBorderCyan),
        containerColor = JarvisSurface,
        tonalElevation = 8.dp
    ) {
        // 1. Chat (Royal Violet)
        NavigationBarItem(
            selected = currentTab == AppTab.CHAT,
            onClick = { onSelectTab(AppTab.CHAT) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Chat",
                    fontSize = 10.sp,
                    fontWeight = if (currentTab == AppTab.CHAT) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.ui.theme.JarvisViolet,
                selectedTextColor = com.example.ui.theme.JarvisViolet,
                unselectedIconColor = JarvisTextMuted,
                unselectedTextColor = JarvisTextMuted,
                indicatorColor = com.example.ui.theme.JarvisVioletContainer
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        // 2. Voice Call (Warm Coral)
        NavigationBarItem(
            selected = currentTab == AppTab.VOICE_CALL,
            onClick = { onSelectTab(AppTab.VOICE_CALL) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.JarvisCoralContainer)
                        .border(1.dp, com.example.ui.theme.JarvisCoral, CircleShape),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = com.example.ui.theme.JarvisCoral,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            label = {
                Text(
                    text = "Call",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.example.ui.theme.JarvisCoral
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.ui.theme.JarvisCoral,
                selectedTextColor = com.example.ui.theme.JarvisCoral,
                unselectedIconColor = com.example.ui.theme.JarvisCoral,
                unselectedTextColor = com.example.ui.theme.JarvisCoral,
                indicatorColor = com.example.ui.theme.JarvisCoralContainer
            ),
            modifier = Modifier.testTag("nav_tab_call")
        )

        // 3. Vision Solver (Sky Blue)
        NavigationBarItem(
            selected = currentTab == AppTab.VISION_SOLVER,
            onClick = { onSelectTab(AppTab.VISION_SOLVER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = "Vision Solver",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Vision",
                    fontSize = 10.sp,
                    fontWeight = if (currentTab == AppTab.VISION_SOLVER) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.ui.theme.JarvisSky,
                selectedTextColor = com.example.ui.theme.JarvisSky,
                unselectedIconColor = JarvisTextMuted,
                unselectedTextColor = JarvisTextMuted,
                indicatorColor = com.example.ui.theme.JarvisSkyContainer
            ),
            modifier = Modifier.testTag("nav_tab_vision")
        )

        // 4. Creation Studio (Solar Amber)
        NavigationBarItem(
            selected = currentTab == AppTab.STUDIO,
            onClick = { onSelectTab(AppTab.STUDIO) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Creation Studio",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Studio",
                    fontSize = 10.sp,
                    fontWeight = if (currentTab == AppTab.STUDIO) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.ui.theme.JarvisAmber,
                selectedTextColor = com.example.ui.theme.JarvisAmber,
                unselectedIconColor = JarvisTextMuted,
                unselectedTextColor = JarvisTextMuted,
                indicatorColor = com.example.ui.theme.JarvisAmberContainer
            ),
            modifier = Modifier.testTag("nav_tab_studio")
        )

        // 5. History / Archives (Emerald)
        NavigationBarItem(
            selected = currentTab == AppTab.HISTORY,
            onClick = { onSelectTab(AppTab.HISTORY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Archives",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = {
                Text(
                    text = "Archives",
                    fontSize = 10.sp,
                    fontWeight = if (currentTab == AppTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.ui.theme.JarvisEmerald,
                selectedTextColor = com.example.ui.theme.JarvisEmerald,
                unselectedIconColor = JarvisTextMuted,
                unselectedTextColor = JarvisTextMuted,
                indicatorColor = com.example.ui.theme.JarvisEmeraldContainer
            ),
            modifier = Modifier.testTag("nav_tab_history")
        )
    }
}
