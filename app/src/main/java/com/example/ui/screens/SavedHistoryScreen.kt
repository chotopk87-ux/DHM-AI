package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreationEntity
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.MarkdownCodeViewer
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGoldAccent
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedHistoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val creations by viewModel.savedCreations.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "ESSAY", "STORY", "CODE", "VISION", "IMAGE_PROMPT")

    val filteredCreations = remember(creations, selectedFilter) {
        if (selectedFilter == "ALL") {
            creations
        } else {
            creations.filter { it.type.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
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
                    .border(1.5.dp, com.example.ui.theme.JarvisEmerald, CircleShape),
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
                    text = "SAVED ARCHIVES & CREATIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.JarvisEmerald,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "${creations.size} archived documents in local Stark memory",
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) JarvisCyanPrimary else JarvisSurfaceElevated)
                        .border(1.dp, if (isSelected) JarvisCyanLight else JarvisBorderCyan, RoundedCornerShape(16.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.Black else JarvisTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredCreations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No saved creations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisTextMuted
                    )
                    Text(
                        text = "Generate essays, stories, or code to automatically archive them here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JarvisTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCreations, key = { it.id }) { item ->
                    CreationCard(
                        creation = item,
                        onDelete = { viewModel.deleteCreation(item.id) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(item.title, item.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onSpeak = {
                            viewModel.speakText(item.content)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreationCard(
    creation: CreationEntity,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val dateString = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(creation.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(12.dp)),
        color = JarvisSurfaceElevated
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JarvisCyanPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = creation.type.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanLight
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = JarvisTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = JarvisCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy content",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete creation",
                            tint = JarvisRedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = creation.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = JarvisGoldAccent
            )

            Spacer(modifier = Modifier.height(6.dp))

            MarkdownCodeViewer(text = creation.content)
        }
    }
}
