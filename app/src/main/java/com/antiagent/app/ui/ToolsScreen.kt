package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.util.Mcp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.tools, fontWeight = FontWeight.Bold) },
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
            // ------------------------------------------------ built-in tools
            Text(s.localTools, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Text("Toggles change what the AI is told it can use.", fontSize = 11.5.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            ToolToggle(s.toolTime, "time", vm)
            ToolToggle(s.toolCalc, "calc", vm)
            ToolToggle(s.toolNotes, "notes", vm)
            ToolToggle(s.toolWeb, "web", vm)
            ToolToggle(s.toolBrowser, "browser", vm)

            Spacer(Modifier.height(22.dp))

            // -------------------------------------------------- MCP servers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(s.externalTools, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Text(
                        "Connect your own MCP server over HTTP. Keys stay on this phone.",
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Button(onClick = { showAdd = true }) { Text(s.addServer, fontSize = 11.5.sp) }
            }
            Spacer(Modifier.height(8.dp))

            if (vm.mcpServers.isEmpty()) {
                Text(s.noServers, fontSize = 12.sp, color = colors.onSurfaceVariant)
            } else {
                vm.mcpServers.forEach { server ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(server.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                                Text(server.url, fontSize = 10.5.sp, color = colors.onSurfaceVariant, maxLines = 1)
                            }
                            Switch(
                                checked = server.enabled,
                                onCheckedChange = { vm.toggleMcpServer(server.id) }
                            )
                        }
                        Row {
                            TextButton(onClick = {
                                scope.launch {
                                    testResult = "Testing ${server.name}…"
                                    try {
                                        testResult = Mcp.ping(server.url)
                                    } catch (e: Exception) {
                                        testResult = "Failed: " + (e.message ?: "unknown")
                                    }
                                }
                            }) { Text(s.testServer, fontSize = 11.5.sp) }
                            TextButton(onClick = { vm.deleteMcpServer(server.id) }) {
                                Text(s.delete, fontSize = 11.5.sp, color = colors.error)
                            }
                        }
                    }
                }
            }

            if (testResult.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    testResult,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Extensions run through built-in tools and optional MCP servers. " +
                    "A full desktop-style extension store needs a backend, which this free, " +
                    "offline app intentionally does not use.",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(s.addServer) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(s.serverName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text(s.serverUrl) },
                        placeholder = { Text("https://my-server.example/mcp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addMcpServer(name, url)
                    showAdd = false
                }) { Text(s.add) }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun ToolToggle(label: String, id: String, vm: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = id in vm.enabledTools, onCheckedChange = { vm.toggleTool(id) })
    }
}
