package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TermLine(val text: String, val command: Boolean = false, val error: Boolean = false)

private const val AI_SYSTEM =
    "You are a shell-like assistant inside a safe Android console. Answer the command with a " +
        "short, plain-text result (no markdown, no preamble). If it is a calculation, give the " +
        "number. If it is a question, answer in one or two lines."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val lines = remember { mutableStateListOf<TermLine>() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    if (lines.isEmpty()) {
        lines.add(TermLine("Anti Agent Console — safe local commands. Type 'help'.", error = false))
        lines.add(TermLine(s.terminalHint))
    }

    fun print(line: TermLine) {
        lines.add(line)
    }

    fun runCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isBlank()) return
        print(TermLine("$ ${cmd}", command = true))
        input = ""
        val parts = cmd.split(Regex("\\s+"), limit = 2)
        val name = parts[0].lowercase(Locale.getDefault())
        val arg = parts.getOrNull(1) ?: ""
        when (name) {
            "help", "?" -> print(TermLine(
                "help | date | echo <text> | calc <expr> | notes [add <text>] | " +
                    "ls | clear | ai <prompt> | send <prompt>"
            ))
            "date", "time" -> print(TermLine(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
            "echo" -> print(TermLine(arg))
            "clear", "cls" -> lines.clear()
            "ls" -> {
                print(TermLine("projects/  notes/  chats/  browser/  tools/"))
                print(TermLine("${vm.projects.size} projects · ${vm.notes.size} notes · ${vm.conversations.size} chats"))
            }
            "notes" -> {
                if (arg.startsWith("add ")) {
                    vm.addNote(arg.removePrefix("add "))
                    print(TermLine("Note added. (${vm.notes.size} total)"))
                } else {
                    if (vm.notes.isEmpty()) print(TermLine("(no notes)"))
                    else vm.notes.takeLast(10).forEach { print(TermLine("· ${it.text}")) }
                }
            }
            "calc" -> {
                val result = try {
                    evalExpr(arg)
                } catch (e: Exception) {
                    "error: ${e.message}"
                }
                print(TermLine(result, error = result.startsWith("error")))
            }
            "ai" -> {
                if (arg.isBlank()) {
                    print(TermLine("usage: ai <prompt>", error = true))
                } else if (!vm.hasAnyKey()) {
                    print(TermLine("Add an API key in Settings first.", error = true))
                } else {
                    busy = true
                    scope.launch {
                        try {
                            print(TermLine(vm.aiAction(AI_SYSTEM, arg).take(2000)))
                        } catch (e: Exception) {
                            print(TermLine(e.message ?: "failed", error = true))
                        } finally {
                            busy = false
                        }
                    }
                }
            }
            "send" -> {
                if (arg.isBlank()) print(TermLine("usage: send <prompt>", error = true))
                else {
                    vm.sendFromExternal(arg)
                    print(TermLine("Sent to chat."))
                }
            }
            else -> print(TermLine("command not found: $name — type 'help'", error = true))
        }
    }

    Scaffold(
        containerColor = Color(0xFF07090D),
        topBar = {
            TopAppBar(
                title = { Text(s.terminal, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
                },
                actions = {
                    TextButton(onClick = { lines.clear() }) { Text(s.clearOutput) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(vertical = 8.dp)
            ) {
                lines.forEach { line ->
                    Text(
                        line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        color = when {
                            line.error -> Color(0xFFFF6B6B)
                            line.command -> Color(0xFF22D3EE)
                            else -> Color(0xFFD6DEE8)
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("$ ", color = Color(0xFF22D3EE), fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = { Text(s.commandHint, fontSize = 12.sp) },
                    singleLine = true,
                    enabled = !busy,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    keyboardActions = KeyboardActions(onDone = { runCommand(input) }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                TextButton(enabled = !busy, onClick = { runCommand(input) }) {
                    Text(if (busy) "…" else s.send)
                }
            }
        }
    }
}

/** Tiny safe arithmetic evaluator: digits, + - * / ( ) and decimals only. */
private fun evalExpr(expr: String): String {
    if (expr.isBlank()) return "0"
    if (!expr.matches(Regex("[0-9+\\-*/(). ]+"))) throw IllegalArgumentException("only math allowed")
    val value = evaluate(expr)
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}

private fun evaluate(input: String): Double {
    val s = input.replace(" ", "")
    var pos = 0
    fun factor(): Double {
        if (pos >= s.length) throw IllegalArgumentException("incomplete")
        if (s[pos] == '(') {
            pos++
            val v = expr()
            if (pos >= s.length || s[pos] != ')') throw IllegalArgumentException("missing )")
            pos++
            return v
        }
        var sign = 1.0
        while (pos < s.length && (s[pos] == '-' || s[pos] == '+')) {
            if (s[pos] == '-') sign *= -1
            pos++
        }
        val start = pos
        while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
        if (start == pos) throw IllegalArgumentException("number expected")
        return sign * s.substring(start, pos).toDouble()
    }
    fun term(): Double {
        var v = factor()
        while (pos < s.length && (s[pos] == '*' || s[pos] == '/')) {
            val op = s[pos++]
            val r = factor()
            v = if (op == '*') v * r else v / r
        }
        return v
    }
    fun expr(): Double {
        var v = term()
        while (pos < s.length && (s[pos] == '+' || s[pos] == '-')) {
            val op = s[pos++]
            val r = term()
            v = if (op == '+') v + r else v - r
        }
        return v
    }
    return expr()
}
