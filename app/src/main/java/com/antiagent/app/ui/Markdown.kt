package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private sealed class Block {
    data class Heading(val level: Int, val text: String) : Block()
    data class Code(val code: String) : Block()
    data class Bullet(val items: List<String>) : Block()
    data class Num(val items: List<String>) : Block()
    data class Quote(val text: String) : Block()
    data class Para(val text: String) : Block()
}

private fun parseMarkdown(src: String): List<Block> {
    val lines = src.replace("\r\n", "\n").split("\n")
    val blocks = mutableListOf<Block>()
    val para = StringBuilder()

    fun flushPara() {
        if (para.isNotBlank()) {
            blocks.add(Block.Para(para.toString().trim()))
            para.clear()
        }
    }

    val orderedRegex = Regex("^\\d+[.)]\\s+(.*)$")
    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val t = raw.trim()
        when {
            t.startsWith("```") -> {
                flushPara()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    code.append(lines[i]).append("\n")
                    i++
                }
                blocks.add(Block.Code(code.toString().trimEnd()))
            }
            t.startsWith("### ") -> {
                flushPara()
                blocks.add(Block.Heading(3, t.removePrefix("### ")))
            }
            t.startsWith("## ") -> {
                flushPara()
                blocks.add(Block.Heading(2, t.removePrefix("## ")))
            }
            t.startsWith("# ") -> {
                flushPara()
                blocks.add(Block.Heading(1, t.removePrefix("# ")))
            }
            t.startsWith("- ") || t.startsWith("* ") || t.startsWith("• ") -> {
                flushPara()
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val tt = lines[i].trim()
                    if (tt.startsWith("- ") || tt.startsWith("* ") || tt.startsWith("• ")) {
                        items.add(tt.substring(2))
                        i++
                    } else {
                        break
                    }
                }
                if (items.isNotEmpty()) blocks.add(Block.Bullet(items))
                continue
            }
            orderedRegex.matches(t) -> {
                flushPara()
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val m = orderedRegex.find(lines[i].trim())
                    if (m != null) {
                        items.add(m.groupValues[1])
                        i++
                    } else {
                        break
                    }
                }
                if (items.isNotEmpty()) blocks.add(Block.Num(items))
                continue
            }
            t.startsWith("> ") -> {
                flushPara()
                blocks.add(Block.Quote(t.removePrefix("> ")))
            }
            t.isBlank() -> flushPara()
            t == "---" || t == "***" -> flushPara()
            else -> {
                if (para.isNotEmpty()) para.append("\n")
                para.append(t)
            }
        }
        i++
    }
    flushPara()
    return blocks
}

fun mdInline(src: String, color: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < src.length) {
        when {
            src.startsWith("**", i) -> {
                val end = src.indexOf("**", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                        append(src.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }
            src[i] == '`' -> {
                val end = src.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = color.copy(alpha = 0.10f),
                            color = color
                        )
                    ) {
                        append(src.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('`')
                    i++
                }
            }
            src.startsWith("~~", i) -> {
                val end = src.indexOf("~~", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = color)) {
                        append(src.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append("~~")
                    i += 2
                }
            }
            src[i] == '*' && i + 1 < src.length && src[i + 1] != ' ' -> {
                val end = src.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = color)) {
                        append(src.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append('*')
                    i++
                }
            }
            else -> {
                append(src[i])
                i++
            }
        }
    }
}

@Composable
fun Markdown(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdown(text) }
    Column(modifier) {
        for (block in blocks) {
            when (block) {
                is Block.Heading -> Text(
                    text = mdInline(block.text, color),
                    fontSize = when (block.level) {
                        1 -> 21.sp
                        2 -> 19.sp
                        else -> 17.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                is Block.Code -> CodeBlock(block.code)
                is Block.Bullet -> Column {
                    for (item in block.items) {
                        Row(Modifier.padding(vertical = 1.dp)) {
                            Text("•  ", color = color)
                            Text(
                                text = mdInline(item, color),
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is Block.Num -> Column {
                    for ((idx, item) in block.items.withIndex()) {
                        Row(Modifier.padding(vertical = 1.dp)) {
                            Text("${idx + 1}.  ", color = color)
                            Text(
                                text = mdInline(item, color),
                                color = color,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is Block.Quote -> Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = mdInline(block.text, color),
                        fontStyle = FontStyle.Italic,
                        color = color
                    )
                }
                is Block.Para -> Text(
                    text = mdInline(block.text, color),
                    color = color,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    val clipboard = LocalClipboardManager.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 2.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
                    Text("Copy", fontSize = 12.sp)
                }
            }
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }
    }
}
