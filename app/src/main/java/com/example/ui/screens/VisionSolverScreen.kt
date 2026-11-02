package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.viewmodel.JarvisViewModel

@Composable
fun VisionSolverScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visionBitmap by viewModel.visionBitmap.collectAsState()
    val visionPrompt by viewModel.visionPrompt.collectAsState()
    val visionResult by viewModel.visionResult.collectAsState()
    val isAnalyzing by viewModel.isVisionAnalyzing.collectAsState()

    // Photo picker (Zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = com.example.util.BitmapUtil.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.setVisionBitmap(bitmap)
            }
        }
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setVisionBitmap(bitmap)
        }
    }

    val presets = listOf(
        Triple(
            "Solve Math / Science",
            Icons.Default.Functions,
            "Please solve this math or science problem step-by-step. Show all formulas, reasoning, and final answer."
        ),
        Triple(
            "Analyze Code Screenshot",
            Icons.Default.Code,
            "Extract all code from this screenshot, identify bugs, explain how it works, and provide the fixed code."
        ),
        Triple(
            "Diagram / Architecture",
            Icons.Default.Science,
            "Analyze this diagram or architecture. Explain its components, data flow, and key relationships."
        ),
        Triple(
            "General Inspection",
            Icons.Default.Visibility,
            "Describe everything in this image in detail, identify objects, translate text, and answer any implied question."
        )
    )

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
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(com.example.ui.theme.JarvisSurface)
                    .border(1.5.dp, com.example.ui.theme.JarvisSky, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_jarvis_robot),
                    contentDescription = "JARVIS Robot",
                    modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "JARVIS VISION SOLVER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = com.example.ui.theme.JarvisSky,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Upload homework, screenshot, diagram or photo to solve instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Preview & Upload Buttons Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (visionBitmap != null) JarvisCyanPrimary else JarvisBorderCyan, RoundedCornerShape(12.dp)),
            color = JarvisSurfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (visionBitmap != null) {
                    Image(
                        bitmap = visionBitmap!!.asImageBitmap(),
                        contentDescription = "Target image to solve",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("vision_pick_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = JarvisCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (visionBitmap == null) "Select Photo" else "Change Photo",
                            color = JarvisCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("vision_capture_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = JarvisGoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Take Photo",
                            color = JarvisGoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Solver Presets
        Text(
            text = "SOLVER PRESETS",
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
            items(presets) { (title, icon, prompt) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisBorderCyan, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setVisionPrompt(prompt) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = JarvisCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Prompt input
        OutlinedTextField(
            value = visionPrompt,
            onValueChange = { viewModel.setVisionPrompt(it) },
            label = { Text("Instructions / Question about Image", color = JarvisCyanLight) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vision_prompt_input"),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Solve Action Button
        Button(
            onClick = { viewModel.solveVisionQuestion() },
            enabled = visionBitmap != null && !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("vision_solve_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = JarvisCyanPrimary,
                disabledContainerColor = JarvisSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JARVIS ANALYZING IMAGE...",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = if (visionBitmap != null) Color.Black else JarvisTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SOLVE WITH JARVIS AI",
                    color = if (visionBitmap != null) Color.Black else JarvisTextMuted,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solution Result Card
        if (visionResult.isNotEmpty() || isAnalyzing) {
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
                            text = "SOLUTION BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanLight,
                            letterSpacing = 1.sp
                        )
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = JarvisCyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MarkdownCodeViewer(
                        text = visionResult,
                        onSpeak = { viewModel.speakText(visionResult) }
                    )
                }
            }
        }
    }
}
