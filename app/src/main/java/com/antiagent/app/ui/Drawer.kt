package com.antiagent.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel

@Composable
fun DrawerContent(vm: AppViewModel, close: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val query = vm.searchQuery.trim()
    val chats = remember(vm.listVersion, query) {
        vm.conversations
            .filter { c ->
                query.isBlank() ||
                    c.title.contains(query, ignoreCase = true) ||
                    c.messages.any { it.text.contains(query, ignoreCase = true) }
            }
            .sortedByDescending { it.updatedAt }
    }

    ModalDrawerSheet(drawerContainerColor = colors.surface) {
        Column(Modifier.padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 10.dp)) {
            Text(
                "Anti Agent",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                s.formModel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = vm.searchQuery,
            onValueChange = { vm.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(s.searchChats, fontSize = 12.5.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(s.newChat) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = {
                vm.newChat()
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

        NavigationDrawerItem(
            label = { Text(s.notesTitle) },
            icon = { Icon(Icons.Filled.Notes, contentDescription = null) },
            selected = false,
            onClick = {
                vm.screen = "notes"
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        NavigationDrawerItem(
            label = { Text(s.settingsTitle) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            selected = false,
            onClick = {
                vm.screen = "settings"
                close()
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}
