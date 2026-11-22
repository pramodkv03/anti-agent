package com.antiagent.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Notes & Tasks") },
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
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Naya note ya task likho…", fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        vm.addNote(text)
                        text = ""
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }

            Spacer(Modifier.height(10.dp))

            if (vm.notes.isEmpty()) {
                Text(
                    "Abhi koi note nahi hai.\nYahan apne tasks aur ideas likho — sab phone me hi save rehta hai.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(items = vm.notes, key = { it.id }) { note ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = note.done,
                                onCheckedChange = { vm.toggleNote(note.id) }
                            )
                            Text(
                                text = note.text,
                                fontSize = 14.sp,
                                color = if (note.done) colors.onSurfaceVariant else colors.onSurface,
                                textDecoration = if (note.done) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { vm.deleteNote(note.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete note",
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
