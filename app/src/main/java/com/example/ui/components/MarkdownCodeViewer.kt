package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisCodeBackground
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGoldAccent
import com.example.ui.theme.JarvisTextPrimary
import androidx.compose.runtime.remember

@Composable
fun MarkdownCodeViewer(
    text: String,
    modifier: Modifier = Modifier,
    onSpeak: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val blocks = remember(text) { parseContentBlocks(text) }

    Column(modifier = modifier) {
        if (onSpeak != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("tts_speak_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Read aloud with Jarvis voice",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Text -> {
                    FormattedTextBlock(content = block.content)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                is ContentBlock.Code -> {
                    CodeCardBlock(
                        language = block.language,
                        code = block.code,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("JARVIS Code", block.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FormattedTextBlock(content: String) {
    val lines = content.split("\n")
    Column {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("### ")) {
                Text(
                    text = trimmed.removePrefix("### "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyanLight,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            } else if (trimmed.startsWith("## ")) {
                Text(
                    text = trimmed.removePrefix("## "),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyanPrimary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 3.dp)
                )
            } else if (trimmed.startsWith("# ")) {
                Text(
                    text = trimmed.removePrefix("# "),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = JarvisCyanPrimary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
                    Text(
                        text = "• ",
                        color = JarvisGoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatInlineMarkdown(trimmed.substring(2)),
                        color = JarvisTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            } else if (trimmed.isNotEmpty()) {
                Text(
                    text = formatInlineMarkdown(trimmed),
                    color = JarvisTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CodeCardBlock(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(10.dp)),
        color = JarvisCodeBackground
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1424))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.ifBlank { "CODE" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyanLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = JarvisCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code content with horizontal scrolling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFC9D1D9)
                )
            }
        }
    }
}

private sealed interface ContentBlock {
    data class Text(val content: String) : ContentBlock
    data class Code(val language: String, val code: String) : ContentBlock
}

private fun parseContentBlocks(raw: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = raw.split("\n")
    val textBuilder = StringBuilder()
    var inCodeBlock = false
    var currentLanguage = ""
    val codeBuilder = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                // Ending code block
                blocks.add(ContentBlock.Code(currentLanguage, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                currentLanguage = ""
                inCodeBlock = false
            } else {
                // Starting code block
                if (textBuilder.isNotBlank()) {
                    blocks.add(ContentBlock.Text(textBuilder.toString().trimEnd()))
                    textBuilder.clear()
                }
                currentLanguage = line.trim().removePrefix("```").trim()
                inCodeBlock = true
            }
        } else {
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n")
            } else {
                textBuilder.append(line).append("\n")
            }
        }
    }

    if (inCodeBlock && codeBuilder.isNotBlank()) {
        blocks.add(ContentBlock.Code(currentLanguage, codeBuilder.toString().trimEnd()))
    } else if (textBuilder.isNotBlank()) {
        blocks.add(ContentBlock.Text(textBuilder.toString().trimEnd()))
    }

    return blocks
}

private fun formatInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldIndex = remaining.indexOf("**")
            if (boldIndex != -1) {
                append(remaining.substring(0, boldIndex))
                val endIndex = remaining.indexOf("**", boldIndex + 2)
                if (endIndex != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = JarvisCyanLight)) {
                        append(remaining.substring(boldIndex + 2, endIndex))
                    }
                    remaining = remaining.substring(endIndex + 2)
                } else {
                    append(remaining.substring(boldIndex))
                    break
                }
            } else {
                append(remaining)
                break
            }
        }
    }
}
