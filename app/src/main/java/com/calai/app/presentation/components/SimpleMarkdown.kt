package com.calai.app.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Hiển thị markdown tối giản trong câu trả lời của AI: **đậm**, *nghiêng*, tiêu đề (#), gạch đầu dòng (* / - ) thành "•".
 * Dấu đánh dấu không còn lộ ra màn hình (trước đây hiện nguyên "***").
 */
fun markdownToAnnotated(raw: String): AnnotatedString = buildAnnotatedString {
    val lines = raw.replace("\r\n", "\n").split("\n")
    lines.forEachIndexed { index, original ->
        var line = original.trimEnd()
        val heading = Regex("""^#{1,6}\s+(.*)$""").matchEntire(line)
        val bullet = Regex("""^\s*[*\-+]\s+(.*)$""").matchEntire(line)
        when {
            heading != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { appendInline(heading.groupValues[1]) }
            bullet != null -> { append("•  "); appendInline(bullet.groupValues[1]) }
            else -> appendInline(line)
        }
        if (index != lines.lastIndex) append("\n")
    }
}

private fun AnnotatedString.Builder.appendInline(text: String) {
    // ***đậm nghiêng***, **đậm**, *nghiêng*, __đậm__ ; ký tự thừa không khớp cặp bị bỏ.
    val pattern = Regex("""\*\*\*(.+?)\*\*\*|\*\*(.+?)\*\*|__(.+?)__|\*(.+?)\*""")
    var last = 0
    for (m in pattern.findAll(text)) {
        if (m.range.first > last) append(stripStray(text.substring(last, m.range.first)))
        when {
            m.groups[1] != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) { append(m.groupValues[1]) }
            m.groups[2] != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[2]) }
            m.groups[3] != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[3]) }
            else -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.groupValues[4]) }
        }
        last = m.range.last + 1
    }
    if (last < text.length) append(stripStray(text.substring(last)))
}

private fun stripStray(s: String): String = s.replace("**", "").replace("__", "")
