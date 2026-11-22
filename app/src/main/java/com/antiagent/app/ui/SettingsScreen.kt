package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.Models
import com.antiagent.app.data.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var showGemini by remember { mutableStateOf(false) }
    var showOpenRouter by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(top = 8.dp, bottom = 40.dp)
        ) {
            Text(
                "Free AI keys",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "App bilkul free hai. AI keys bhi free tier ki hain — koi paisa nahi lagega.",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = vm.geminiKey,
                onValueChange = { vm.setGeminiKey(it) },
                label = { Text("Gemini API key") },
                placeholder = { Text("AIza...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showGemini) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showGemini = !showGemini }) {
                        Icon(
                            if (showGemini) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Show key"
                        )
                    }
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Kaise lein: aistudio.google.com/apikey kholo → Google se sign in → Create API key → Copy → yahan paste karo.",
                fontSize = 11.5.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = vm.openRouterKey,
                onValueChange = { vm.setOpenRouterKey(it) },
                label = { Text("OpenRouter API key") },
                placeholder = { Text("sk-or-v1-...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showOpenRouter) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showOpenRouter = !showOpenRouter }) {
                        Icon(
                            if (showOpenRouter) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Show key"
                        )
                    }
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Kaise lein: openrouter.ai/keys kholo → sign in → Create key → Copy → yahan paste karo. (Free models ke liye kaafi hai.)",
                fontSize = 11.5.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Models.all.forEach { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { vm.setModel(m.id) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(m.label, fontSize = 14.sp, color = colors.onSurface)
                        Text(
                            m.providerLabel,
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
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

            Spacer(Modifier.height(20.dp))
            Text(
                "System prompt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "AI ka behaviour yahan se badlo.",
                fontSize = 11.5.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = vm.systemPrompt,
                onValueChange = { vm.setSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Temperature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        "Kam = serious, zyada = creative (${"%.1f".format(vm.temperature)})",
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Slider(
                value = vm.temperature,
                onValueChange = { vm.setTemperature(it) },
                valueRange = 0f..1.5f,
                steps = 14
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Jawab bol kar sunao",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        "Har naye jawab ko phone ki awaaz me sunna",
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(checked = vm.autoSpeak, onCheckedChange = { vm.setAutoSpeak(it) })
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Themes.choices.forEach { (id, label) ->
                    val selected = vm.theme == id
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colors.primary else colors.surfaceVariant)
                            .clickable { vm.setTheme(id) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            color = if (selected) colors.onPrimary else colors.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Default prompt reset",
                fontSize = 12.sp,
                color = colors.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { vm.setSystemPrompt(Prefs.DEFAULT_SYSTEM_PROMPT) }
                    .padding(8.dp)
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Anti Agent v1.0\nFree • no subscription • keys sirf aapke phone me save hoti hain.",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}
