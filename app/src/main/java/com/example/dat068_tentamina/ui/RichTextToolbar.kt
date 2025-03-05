package com.example.dat068_tentamina.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState


@Composable
fun RichTextToolbar(richTextState: RichTextState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray) // Toolbar background
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Alignment buttons
        ToolbarButton("⬅") { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left)) }
        ToolbarButton("⬆") { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) }
        ToolbarButton("➡") { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right)) }
        ToolbarButton("⤴") { richTextState.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Justify)) }

        // Formatting buttons
        ToolbarButton("B") { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
        ToolbarButton("I") { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
        ToolbarButton("U") { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        ToolbarButton("T̶") { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }

        // Headings (H1, H2, H3)
        ToolbarButton("H1") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n# ") }
        ToolbarButton("H2") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n## ") }
        ToolbarButton("H3") { richTextState.setMarkdown(richTextState.toMarkdown() + "\n### ") }

        // List & Code Formatting
        ToolbarButton("• List") { richTextState.toggleUnorderedList() }
        ToolbarButton("1. List") { richTextState.toggleOrderedList() }

    }
}

    @Composable
    fun ToolbarButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .padding(2.dp) // Reduce padding
                .height(36.dp) // Smaller height
                .width(50.dp), // Uniform button width
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5068A9)) // Blue background
        ) {
            Text(text, color = Color.White, fontSize = 12.sp) // Smaller text
        }
    }
