package com.parth.aquasense.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Composable that renders markdown-formatted text with support for:
 * - **bold text**
 * - Bullet points (-, •, *)
 * - Numbered lists (1., 2., etc.)
 * - Headers (#, ##, ###, ####, etc.)
 * - Markdown tables
 * - Horizontal rules (---)
 * - LaTeX notation cleanup
 *
 * Use this for rendering text from Gemini API and other AI-generated content.
 */
@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    val annotatedString = buildAnnotatedString {
        val lines = text.split('\n')
        var skipNextNewline = false
        var inTable = false

        lines.forEachIndexed { lineIndex, line ->
            val trimmedLine = line.trim()

            // Skip empty lines but preserve one blank line
            if (trimmedLine.isEmpty()) {
                if (!skipNextNewline) {
                    append('\n')
                    skipNextNewline = true
                }
                inTable = false
                return@forEachIndexed
            }

            skipNextNewline = false

            // Detect markdown table separator line (e.g., | :--- | :--- |)
            val tableSeparator = Regex("^\\|\\s*:?-+:?\\s*\\|")
            if (tableSeparator.containsMatchIn(trimmedLine)) {
                // Skip table separator lines
                return@forEachIndexed
            }

            // Detect markdown table row (e.g., | Cell 1 | Cell 2 |)
            val tableRow = Regex("^\\|(.+)\\|$")
            val tableMatch = tableRow.find(trimmedLine)

            // Handle headers (# Header, ## Header, ### Header, #### Header, etc.)
            val headerPattern = Regex("^(#{1,6})\\s+(.+)$")
            val headerMatch = headerPattern.find(trimmedLine)

            // Handle horizontal rules (--- or ***)
            val hrPattern = Regex("^[-*]{3,}$")
            val hrMatch = hrPattern.find(trimmedLine)

            // Handle bullet points (-, •, *, or - with extra spaces)
            val bulletPattern = Regex("^[-•*]\\s+(.+)$")
            val bulletMatch = bulletPattern.find(trimmedLine)

            // Handle numbered lists (1., 2., etc.)
            val numberedPattern = Regex("^(\\d+)\\.\\s+(.+)$")
            val numberedMatch = numberedPattern.find(trimmedLine)

            when {
                hrMatch != null -> {
                    // Skip horizontal rules or render as line break
                    append('\n')
                }
                tableMatch != null -> {
                    // Parse and format table rows
                    val cells = tableMatch.groupValues[1]
                        .split('|')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    if (cells.isNotEmpty()) {
                        // Format as simple list with bullets
                        if (!inTable) {
                            // First row (header) - make it bold
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(cells.joinToString(" • "))
                            }
                            inTable = true
                        } else {
                            // Data rows
                            append("  • ")
                            cells.forEachIndexed { index, cell ->
                                processFormattedText(cell)
                                if (index < cells.size - 1) {
                                    append(" • ")
                                }
                            }
                        }
                    }
                }
                headerMatch != null -> {
                    // Add header with bold
                    inTable = false
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        processFormattedText(headerMatch.groupValues[2])
                    }
                }
                bulletMatch != null -> {
                    // Add bullet point with proper spacing
                    inTable = false
                    append("  •  ")
                    processFormattedText(bulletMatch.groupValues[1])
                }
                numberedMatch != null -> {
                    // Add numbered list item
                    inTable = false
                    append("  ${numberedMatch.groupValues[1]}.  ")
                    processFormattedText(numberedMatch.groupValues[2])
                }
                else -> {
                    // Process normal line with formatting
                    inTable = false
                    processFormattedText(trimmedLine)
                }
            }

            // Add line break except for last line
            if (lineIndex < lines.size - 1) {
                append('\n')
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color
    )
}

/**
 * Helper function to process formatted text (bold, italic, inline code) within AnnotatedString.Builder
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.processFormattedText(text: String) {
    // First, clean up LaTeX notation
    val cleanedText = cleanLatexNotation(text)

    // Process bold text (**text**)
    val boldPattern = Regex("""\*\*(.+?)\*\*""")
    var currentIndex = 0
    val matches = boldPattern.findAll(cleanedText).toList()

    if (matches.isEmpty()) {
        // No formatting, just append the text
        append(cleanedText)
        return
    }

    matches.forEach { match ->
        // Add text before the match
        if (match.range.first > currentIndex) {
            append(cleanedText.substring(currentIndex, match.range.first))
        }

        // Add bold text (without the ** markers)
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }

        currentIndex = match.range.last + 1
    }

    // Add remaining text after last match
    if (currentIndex < cleanedText.length) {
        append(cleanedText.substring(currentIndex))
    }
}

/**
 * Clean LaTeX mathematical notation and convert to readable text
 */
private fun cleanLatexNotation(text: String): String {
    var result = text

    // Remove simple $text$ patterns first (most common case)
    result = result.replace(Regex("""\$([^$]+)\$"""), "$1")

    // Remove $ delimiters and \text{} wrappers
    result = result.replace(Regex("""\\text\{([^}]+)\}"""), "$1")

    // Handle subscripts like _3, _2 -> convert to regular numbers
    result = result.replace(Regex("""_\{?(\d+)\}?"""), "$1")

    // Handle superscripts like ^2, ^3 -> convert to regular numbers
    result = result.replace(Regex("""\^\{?(\d+)\}?"""), "$1")

    // Common chemical formulas - clean up remaining LaTeX commands
    result = result.replace("""\\text\{""", "")
    result = result.replace(Regex("""\\[a-zA-Z]+"""), "")  // Remove LaTeX commands like \ce, \mathrm, etc.

    // Remove any remaining $ delimiters (in case of nested or malformed LaTeX)
    result = result.replace("$", "")

    // Clean up extra braces
    result = result.replace("{", "")
    result = result.replace("}", "")

    return result
}

