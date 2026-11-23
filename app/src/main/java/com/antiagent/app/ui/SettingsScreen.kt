package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.Models
import com.antiagent.app.data.Personas
import com.antiagent.app.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    var showModels by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.settingsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 48.dp)
        ) {
            // ------------------------------------------------------ workspace
            SectionTitle(s.workspace, s.workspaceHint)
            NavRow(s.usage, "usage", vm)
            NavRow(s.browser, "browser", vm)
            NavRow(s.terminal, "terminal", vm)
            NavRow(s.tools, "tools", vm)
            NavRow(s.notesTitle, "notes", vm)

            Spacer(Modifier.height(14.dp))

            // ---------------------------------------------------------- keys
            SectionTitle(s.connections, s.connectionsHint)

            ProviderBlock(
                s = s,
                label = s.geminiKey,
                value = vm.geminiKey,
                placeholder = "AIza…",
                help = s.geminiHelp,
                changed = { vm.changeGeminiKey(it) },
                status = vm.statusGemini,
                testing = vm.testingProvider == "gemini",
                onTest = { vm.testProvider("gemini") },
                onClear = { vm.clearKey("gemini") }
            )
            if (vm.geminiKey.trim().startsWith("sk-or-")) {
                Hint(s.keyLooksOpenRouter, colors.error)
            }

            Spacer(Modifier.height(14.dp))

            ProviderBlock(
                s = s,
                label = s.openRouterKey,
                value = vm.openRouterKey,
                placeholder = "sk-or-v1-…",
                help = s.openRouterHelp,
                changed = { vm.changeOpenRouterKey(it) },
                status = vm.statusOpenRouter,
                testing = vm.testingProvider == "openrouter",
                onTest = { vm.testProvider("openrouter") },
                onClear = { vm.clearKey("openrouter") }
            )
            if (vm.openRouterKey.trim().startsWith("AIza")) {
                Hint(s.keyLooksGemini, colors.error)
            }

            // --------------------------------------------------------- models
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.model)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .clickable { showModels = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(vm.activeModel.label, fontSize = 14.sp, color = colors.onSurface)
                    Text(
                        "${vm.activeModel.providerLabel} • ${vm.models.size} ${s.modelsLoaded}",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(s.chooseModel, fontSize = 12.sp, color = colors.primary)
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { vm.connect(vm.activeModel.provider) }) {
                Text(
                    if (vm.testingProvider.isNotEmpty()) s.testing else s.refreshModels,
                    fontSize = 12.sp
                )
            }

            // --------------------------------------------------------- agents
            Spacer(Modifier.height(18.dp))
            SectionTitle(s.agentsLabel, s.agentsHint)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Personas.all.forEach { p ->
                    ChoiceChip(
                        label = s.agentName(p.id),
                        selected = vm.persona == p.id,
                        onClick = { vm.selectPersona(p.id) }
                    )
                }
            }

            // ------------------------------------------------------- language
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.language, s.languageHint)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChoiceChip(s.english, vm.language == "en") { vm.changeLanguage("en") }
                ChoiceChip(s.hindi, vm.language == "hi") { vm.changeLanguage("hi") }
                ChoiceChip(s.hinglish, vm.language == "hinglish") { vm.changeLanguage("hinglish") }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle(s.replyLanguage, s.replyLanguageHint)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChoiceChip(s.autoLang, vm.replyLang == "auto") { vm.changeReplyLang("auto") }
                ChoiceChip(s.english, vm.replyLang == "en") { vm.changeReplyLang("en") }
                ChoiceChip(s.hindi, vm.replyLang == "hi") { vm.changeReplyLang("hi") }
                ChoiceChip(s.hinglish, vm.replyLang == "hinglish") { vm.changeReplyLang("hinglish") }
            }

            // --------------------------------------------------- system prompt
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.systemPrompt, s.systemPromptHint)
            OutlinedTextField(
                value = vm.systemPrompt,
                onValueChange = { vm.changeSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.onSurface)
            )
            TextButton(onClick = { vm.changeSystemPrompt(Prefs.DEFAULT_SYSTEM_PROMPT) }) {
                Text(s.resetPrompt, fontSize = 12.sp)
            }

            // ------------------------------------------------------- creativity
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    s.creativity,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "%.1f".format(vm.temperature),
                    fontSize = 12.sp,
                    color = colors.primary
                )
            }
            Slider(
                value = vm.temperature,
                onValueChange = { vm.changeTemperature(it) },
                valueRange = 0f..1.5f,
                steps = 14
            )

            // --------------------------------------------------------- speech
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.autoSpeak,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(s.autoSpeakHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
                }
                Switch(checked = vm.autoSpeak, onCheckedChange = { vm.changeAutoSpeak(it) })
            }

            // ---------------------------------------------------------- themes
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.themeLabel)
            Themes.choices.forEach { (id, label) ->
                val selected = vm.theme == id
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { vm.changeTheme(id) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeDot(id))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(label, fontSize = 14.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
                    if (selected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------------------ data
            Spacer(Modifier.height(22.dp))
            SectionTitle(s.dataSection, s.savedOnPhone)
            Text(
                s.clearChats,
                fontSize = 13.sp,
                color = colors.error,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { confirmClear = true }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // ----------------------------------------------------- diagnostics
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.diagnostics,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(s.diagnosticsHint, fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
                TextButton(onClick = { clipboard.setText(AnnotatedString(vm.diagnosticsText())) }) {
                    Text(s.copy, fontSize = 12.sp)
                }
            }
            val log = vm.diagnosticsText().ifBlank { s.noDiagnostics }
            Text(
                log,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .verticalScroll(rememberScrollState())
                    .padding(9.dp)
            )

            // ----------------------------------------------------------- about
            Spacer(Modifier.height(20.dp))
            SectionTitle(s.aboutSection)
            Text(s.about, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
        }
    }

    if (showModels) {
        ModelDialog(vm, onClose = { showModels = false })
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(s.clearChats) },
            text = { Text(s.clearChatsConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    vm.clearAllChats()
                }) { Text(s.delete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun NavRow(label: String, target: String, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .clickable { vm.screen = target }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.5.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        Text("›", fontSize = 20.sp, color = colors.onSurfaceVariant)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SectionTitle(title: String, hint: String? = null) {
    val colors = MaterialTheme.colorScheme
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface
    )
    if (!hint.isNullOrBlank()) {
        Text(hint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Hint(text: String, color: Color) {
    Text(
        text,
        fontSize = 11.sp,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ProviderBlock(
    s: Str,
    label: String,
    value: String,
    placeholder: String,
    help: String,
    changed: (String) -> Unit,
    status: String,
    testing: Boolean,
    onTest: () -> Unit,
    onClear: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = changed,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) s.hide else s.show
                )
            }
        }
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onTest, enabled = !testing) {
            Text(if (testing) s.testing else s.test, fontSize = 12.sp)
        }
        TextButton(onClick = onClear) {
            Text(s.clearKey, fontSize = 12.sp, color = colors.onSurfaceVariant)
        }
    }
    Text(s.testHint, fontSize = 10.5.sp, color = colors.onSurfaceVariant)
    Text(help, fontSize = 11.sp, color = colors.onSurfaceVariant)

    if (status.isNotBlank()) {
        val isError = status.startsWith("ERROR:")
        val color = when {
            isError -> colors.error
            status.startsWith("OK") || status.startsWith("CONNECTED") -> colors.primary
            else -> colors.onSurfaceVariant
        }
        Text(
            statusText(status, s),
            fontSize = 11.5.sp,
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.10f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

private fun statusText(status: String, s: Str): String = when {
    status == "PASTE_KEY" -> s.pasteKeyFirst
    status == "TESTING" -> s.testing
    status.startsWith("CONNECTED:") -> {
        val n = status.removePrefix("CONNECTED:").toIntOrNull() ?: 0
        "${s.connectedN} • $n ${s.modelsLoaded}"
    }
    status == "OK" || status.startsWith("OK:") -> s.testOk
    status.startsWith("ERROR:") -> s.testFailed + " " + status.removePrefix("ERROR:").take(220)
    else -> status
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ModelDialog(vm: AppViewModel, onClose: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.chooseModel) },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                var lastProvider = ""
                vm.models.forEach { m ->
                    if (m.provider != lastProvider) {
                        lastProvider = m.provider
                        Text(
                            m.providerLabel,
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                        HorizontalDivider(color = colors.outline.copy(alpha = 0.3f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.selectModel(m.id)
                                onClose()
                            }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            m.label,
                            fontSize = 13.sp,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (m.id == vm.modelId) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text(s.close) }
        },
        dismissButton = {
            TextButton(onClick = { vm.connect(vm.activeModel.provider) }) {
                Text(s.refreshModels, fontSize = 12.sp)
            }
        }
    )
}

private fun themeDot(id: String): Color = when (id) {
    Themes.NEBULA -> Color(0xFFA78BFA)
    Themes.CARBON -> Color(0xFF34D399)
    Themes.OCEAN -> Color(0xFF38BDF8)
    Themes.AURORA -> Color(0xFF2DD4BF)
    Themes.SUNSET -> Color(0xFFFB7185)
    Themes.DAYLIGHT -> Color(0xFF6D28D9)
    Themes.SAND -> Color(0xFFB45309)
    else -> Color(0xFF8B5CF6)
}
