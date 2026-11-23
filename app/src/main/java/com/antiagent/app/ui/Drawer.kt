package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.Personas
import com.antiagent.app.data.Projects

@Composable
fun DrawerContent(vm: AppViewModel, close: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val query = vm.searchQuery.trim()
    var showNewProject by remember { mutableStateOf(false) }
    var confirmDeleteProject by remember { mutableStateOf(false) }

    val chats = remember(vm.listVersion, query, vm.activeProject) {
        vm.conversations
            .filter { c ->
                (vm.activeProject == Projects.ALL || c.projectId == vm.activeProject) &&
                    (query.isBlank() ||
                        c.title.contains(query, ignoreCase = true) ||
                        c.messages.any { it.text.contains(query, ignoreCase = true) })
            }
            .sortedByDescending { it.updatedAt }
    }

    ModalDrawerSheet(drawerContainerColor = colors.surface) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 22.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AgentLogo(size = 42.dp, corner = 13.dp, letterSize = 21)
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Anti Agent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(s.formModel, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }

        Row(
            Modifier.padding(start = 20.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AgentStatusChip(if (vm.sending) "Agent working…" else "Agent online", working = vm.sending)
            Spacer(Modifier.size(8.dp))
            Text("${vm.models.size} models", fontSize = 10.sp, color = colors.onSurfaceVariant)
        }

        Text(
            s.projects,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 26.dp, bottom = 2.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ProjectChip(s.allProjects, vm.activeProject == Projects.ALL) {
                vm.switchProject(Projects.ALL)
            }
            vm.projects.forEach { p ->
                ProjectChip(p.name, vm.activeProject == p.id) { vm.switchProject(p.id) }
            }
            ProjectChip("+ " + s.newProject, false) { showNewProject = true }
        }

        Row(Modifier.padding(horizontal = 16.dp)) {
            Text(
                s.projectHint,
                fontSize = 10.5.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (vm.activeProject != "chat" && vm.activeProject != Projects.ALL) {
                Text(
                    s.deleteProject,
                    fontSize = 10.5.sp,
                    color = colors.error,
                    modifier = Modifier.clickable { confirmDeleteProject = true }
                )
            }
        }

        OutlinedTextField(
            value = vm.searchQuery,
            onValueChange = { vm.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text(s.searchChats, fontSize = 12.5.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        NavigationDrawerItem(
            label = { Text(s.newChat) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = {
                vm.newChat()
                vm.screen = "chat"
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outline.copy(alpha = 0.4f))

        Text(
            s.chats,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 26.dp, bottom = 4.dp)
        )

        if (chats.isEmpty()) {
            Text(
                if (vm.conversations.isEmpty()) s.noChats else s.noResults,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 26.dp, top = 6.dp, bottom = 6.dp)
            )
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(items = chats, key = { it.id }) { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.openChat(c.id)
                                vm.searchQuery = ""
                                close()
                            }
                            .padding(start = 26.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = c.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (c.id == vm.currentId) colors.primary else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { vm.deleteChat(c.id) }, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = s.deleteChat,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))

        SectionLabel(
            "Agent tools",
            modifier = Modifier.padding(start = 26.dp, top = 10.dp, bottom = 6.dp)
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTile(Icons.Filled.Public, s.browser, vm.screen == "browser", Modifier.weight(1f)) {
                vm.screen = "browser"; close()
            }
            ToolTile(Icons.Filled.Terminal, s.terminal, vm.screen == "terminal", Modifier.weight(1f)) {
                vm.screen = "terminal"; close()
            }
            ToolTile(Icons.Filled.Extension, s.tools, vm.screen == "tools", Modifier.weight(1f)) {
                vm.screen = "tools"; close()
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTile(Icons.Filled.Analytics, s.usage, vm.screen == "usage", Modifier.weight(1f)) {
                vm.screen = "usage"; close()
            }
            ToolTile(Icons.Filled.Notes, s.notesTitle, vm.screen == "notes", Modifier.weight(1f)) {
                vm.screen = "notes"; close()
            }
            ToolTile(Icons.Filled.Settings, s.settingsTitle, vm.screen == "settings", Modifier.weight(1f)) {
                vm.screen = "settings"; close()
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showNewProject) {
        NewProjectDialog(vm, onClose = { showNewProject = false })
    }
    if (confirmDeleteProject) {
        AlertDialog(
            onDismissRequest = { confirmDeleteProject = false },
            title = { Text(s.deleteProject) },
            text = { Text(s.deleteProjectConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProject(vm.activeProject)
                    confirmDeleteProject = false
                }) { Text(s.delete, color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteProject = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun ProjectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            maxLines = 1,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun NewProjectDialog(vm: AppViewModel, onClose: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(vm.modelId) }
    var agent by remember { mutableStateOf("general") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.newProject) },
        text = {
            Column {
                Text(s.projectHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.projectName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(s.pickAgent, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Personas.all.forEach { p ->
                        ProjectChip(s.agentName(p.id), agent == p.id) { agent = p.id }
                        Spacer(Modifier.size(4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(s.pickModel, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Column(
                    Modifier
                        .heightIn(max = 190.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    vm.models.forEach { m ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { model = m.id }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${m.label} · ${m.providerLabel}",
                                fontSize = 12.sp,
                                color = if (m.id == model) colors.primary else colors.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.addProject(name, model, agent)
                onClose()
            }) { Text(s.create) }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text(s.cancel) }
        }
    )
}
