package com.example.dat068_tentamina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp


@Composable
fun RichTextToolbar(richTextState: RichTextState) {
    Row(
        modifier = Modifier
            .width(1000.dp)                                                         // Adjust width
            .background(Color(0xFF1E3A8A), shape = RoundedCornerShape(8.dp)) // Toolbar background
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Alignment buttons
        ToolbarIconButton(Icons.Filled.FormatAlignLeft) {
            richTextState.toggleParagraphStyle(
                ParagraphStyle(textAlign = TextAlign.Left)
            )
        }
        ToolbarIconButton(Icons.Filled.FormatAlignCenter) {
            richTextState.toggleParagraphStyle(
                ParagraphStyle(textAlign = TextAlign.Center)
            )
        }
        ToolbarIconButton(Icons.Filled.FormatAlignRight) {
            richTextState.toggleParagraphStyle(
                ParagraphStyle(textAlign = TextAlign.Right)
            )
        }
        ToolbarIconButton(Icons.Filled.FormatAlignJustify) {
            richTextState.toggleParagraphStyle(
                ParagraphStyle(textAlign = TextAlign.Justify)
            )
        }

        // Formatting buttons
        ToolbarIconButton(Icons.Filled.FormatBold) {
            richTextState.toggleSpanStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold
                )
            )
        }
        ToolbarIconButton(Icons.Filled.FormatItalic) {
            richTextState.toggleSpanStyle(
                SpanStyle(
                    fontStyle = FontStyle.Italic
                )
            )
        }
        ToolbarIconButton(Icons.Filled.FormatUnderlined) {
            richTextState.toggleSpanStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline
                )
            )
        }
        ToolbarIconButton(Icons.Filled.FormatStrikethrough) {
            richTextState.toggleSpanStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough)
            )
        }

        // Headings (H1, H2, H3)
        ToolbarTextButton("H1") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n# ") }
        ToolbarTextButton("H2") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n## ") }
        ToolbarTextButton("H3") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n### ") }

        // List & Code Formatting
        ToolbarIconButton(Icons.Filled.FormatListBulleted) { richTextState.toggleUnorderedList() }
        ToolbarIconButton(Icons.Filled.FormatListNumbered) { richTextState.toggleOrderedList() }
    }
}
    @Composable
    fun ToolbarIconButton(icon: ImageVector, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(45.dp)              // Adjust height
                .width(60.dp),              // Adjust width
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1E3A8A),
                modifier = Modifier.size(24.dp)
            )
        }
    }

@Composable
fun ToolbarTextButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(45.dp)
            .width(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1E3A8A)
        )
    ) {
        androidx.compose.material3.Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            maxLines = 1
        )
    }
}
