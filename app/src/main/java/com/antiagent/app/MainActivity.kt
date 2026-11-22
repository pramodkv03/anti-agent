package com.antiagent.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antiagent.app.data.ChatMessage
import com.antiagent.app.ui.AntiAgentTheme
import com.antiagent.app.ui.LocalStr
import com.antiagent.app.ui.Markdown
import com.antiagent.app.ui.Str
import com.antiagent.app.ui.Themes
import com.antiagent.app.util.FileUtil
import com.antiagent.app.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // One-time key import from the assistant (keys never enter the APK or repo).
        // Stored only in this app's private SharedPreferences.
        val g = intent.getStringExtra("aa_gemini")
        val o = intent.getStringExtra("aa_openrouter")
        if (!g.isNullOrBlank() || !o.isNullOrBlank()) {
            val p = com.antiagent.app.data.Prefs(this)
            if (!g.isNullOrBlank()) p.geminiKey = g.trim()
            if (!o.isNullOrBlank()) p.openRouterKey = o.trim()
            intent.removeExtra("aa_gemini")
            intent.removeExtra("aa_openrouter")
        }

        setContent {
            val vm: AppViewModel = viewModel()
            AntiAgentTheme(vm.theme) {
                CompositionLocalProvider(
                    LocalStr provides remember(vm.language) { Str(vm.language) }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AntiGemApp(vm)
                    }
                }
            }
        }
    }
}

// ==========================================
// APP ROOT + NAVIGATION DRAWER
// ==========================================
@Composable
fun AntiGemApp(vm: AppViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeScreen by remember { mutableStateOf("chat") }
    var showModels by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = activeScreen != "chat" && !drawerState.isOpen) {
        activeScreen = "chat"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AntiGemDrawer(
                vm = vm,
                onSelectChat = { id ->
                    vm.openChat(id)
                    activeScreen = "chat"
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    vm.newChat()
                    activeScreen = "chat"
                    scope.launch { drawerState.close() }
                },
                onNavigate = { screen ->
                    activeScreen = screen
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (activeScreen) {
            "settings" -> SettingsScreen(
                vm = vm,
                isCarbon = vm.theme == Themes.CARBON,
                onToggleTheme = {
                    vm.changeTheme(if (vm.theme == Themes.CARBON) Themes.MIDNIGHT else Themes.CARBON)
                },
                onBack = { activeScreen = "chat" }
            )

            "notes" -> NotesScreen(vm = vm, onBack = { activeScreen = "chat" })

            else -> ChatScreen(
                vm = vm,
                onMenu = { scope.launch { drawerState.open() } },
                onModelClick = { showModels = true }
            )
        }
    }

    if (showModels) {
        ModelDialog(vm = vm, onDismiss = { showModels = false })
    }
}

@Composable
fun AntiGemDrawer(
    vm: AppViewModel,
    onSelectChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ModalDrawerSheet(drawerContainerColor = colors.surface) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Anti Agent",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "Your free AI agent",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = colors.primaryContainer,
            onClick = onNewChat
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = colors.onPrimaryContainer)
                Spacer(Modifier.width(12.dp))
                Text("New Chat", color = colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(
            Modifier.padding(vertical = 12.dp),
            color = colors.outline.copy(alpha = 0.2f)
        )

        Text(
            "Recent Chats",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
        )

        LazyColumn(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            items(vm.conversations, key = { it.id }) { c ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            c.title.ifBlank { "New chat" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = c.id == vm.currentId,
                    onClick = { onSelectChat(c.id) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        HorizontalDivider(color = colors.outline.copy(alpha = 0.2f))

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { onNavigate("notes") }) {
                Icon(Icons.Default.Notes, contentDescription = "Notes", tint = colors.onSurfaceVariant)
            }
            IconButton(onClick = { onNavigate("settings") }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colors.onSurfaceVariant)
            }
        }
    }
}

// ==========================================
// CHAT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: AppViewModel,
    onMenu: () -> Unit,
    onModelClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var showAttach by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) scope.launch {
            val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
            if (b64 != null) vm.pendingImage = b64 else vm.error = "Could not read that photo."
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) scope.launch {
            val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
            if (b64 != null) vm.pendingImage = b64 else vm.error = "Could not read that photo."
        }
    }

    val launchCamera = {
        try {
            val dir = File(ctx.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "cap_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        } catch (_: Exception) {
            vm.error = "Camera is not available."
        }
    }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            val res = withContext(Dispatchers.IO) { FileUtil.readText(ctx, uri) }
            if (res != null && res.second != null) {
                vm.attachFile(res.first, res.second ?: "")
            } else {
                vm.error = "Could not read that file."
            }
        }
    }

    val conv = vm.conversations.firstOrNull { it.id == vm.currentId }
    val messages = conv?.messages?.toList() ?: emptyList()
    val listState = rememberLazyListState()
    val total = messages.size + if (vm.sending) 1 else 0

    LaunchedEffect(total, vm.streamingText.length, vm.chatErrorKind) {
        if (total > 0) {
            try { listState.animateScrollToItem(total - 1) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(vm.error) {
        val msg = vm.error
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.error = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                title = {
                    Surface(
                        onClick = onModelClick,
                        shape = RoundedCornerShape(20.dp),
                        color = colors.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                vm.activeModel.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 220.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().imePadding(),
                color = colors.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttach = true }) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = colors.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = vm.draft,
                        onValueChange = { vm.draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...", fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceVariant,
                            unfocusedContainerColor = colors.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(Modifier.width(6.dp))

                    if (vm.sending) {
                        IconButton(
                            onClick = { vm.stop() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = colors.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                        }
                    } else {
                        IconButton(
                            onClick = { vm.send() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = colors.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = colors.onPrimary)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            items(messages) { msg ->
                ChatBubble(msg = msg)
            }

            if (vm.sending) {
                item {
                    if (vm.streamingText.isBlank()) {
                        TypingBubble()
                    } else {
                        ChatBubble(msg = ChatMessage("assistant", vm.streamingText))
                    }
                }
            }

            if (vm.chatErrorKind != null) {
                item { ErrorBubble(vm.chatErrorDetail ?: "Request failed", onRetry = { vm.retry() }) }
            }

            if (vm.fallbackNotice.isNotBlank()) {
                item {
                    Text(
                        vm.fallbackNotice,
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showAttach) {
        AlertDialog(
            onDismissRequest = { showAttach = false },
            title = { Text("Add to chat") },
            text = {
                Column {
                    AttachRow(Icons.Default.PhotoCamera, "Camera") {
                        showAttach = false
                        launchCamera()
                    }
                    AttachRow(Icons.Default.Image, "Gallery") {
                        showAttach = false
                        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    AttachRow(Icons.Default.Description, "Text file") {
                        showAttach = false
                        pickFile.launch(arrayOf("text/*", "application/json", "application/xml"))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAttach = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AttachRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ==========================================
// MESSAGE BUBBLE + CODE BLOCK
// ==========================================
@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) colors.primaryContainer else colors.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (msg.text.contains("```")) {
                    msg.text.split("```").forEachIndexed { idx, part ->
                        if (idx % 2 == 1) {
                            CodeChip(part.trim())
                        } else if (part.isNotBlank()) {
                            SelectionContainer {
                                Text(
                                    part.trim(),
                                    fontSize = 14.sp,
                                    color = if (isUser) colors.onPrimaryContainer else colors.onSurface
                                )
                            }
                        }
                    }
                } else {
                    SelectionContainer {
                        if (isUser) {
                            Text(msg.text, fontSize = 14.sp, color = colors.onPrimaryContainer)
                        } else {
                            Markdown(msg.text, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeChip(code: String) {
    val colors = MaterialTheme.colorScheme
    val cb = LocalClipboardManager.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = colors.background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { cb.setText(AnnotatedString(code)) }) {
                    Text("Copy", fontSize = 11.sp)
                }
            }
            Text(
                code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = colors.onSurface,
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
        ) {
            Text(
                "Thinking…",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorBubble(detail: String, onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.error.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Could not get a reply", fontWeight = FontWeight.Bold, color = colors.error, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                detail,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.heightIn(max = 100.dp).verticalScroll(rememberScrollState())
            )
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

// ==========================================
// MODEL PICKER
// ==========================================
@Composable
private fun ModelDialog(vm: AppViewModel, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose model") },
        text = {
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                var lastProvider = ""
                vm.models.forEach { m ->
                    if (m.provider != lastProvider) {
                        lastProvider = m.provider
                        item(key = "h_${m.provider}") {
                            Text(
                                m.providerLabel,
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }
                    }
                    item(key = m.id) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.selectModel(m.id); onDismiss() }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                m.label,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (m.id == vm.modelId) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = colors.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            TextButton(onClick = { vm.connect(vm.activeModel.provider) }) { Text("Refresh") }
        }
    )
}

// ==========================================
// NOTES SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: AppViewModel, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Notes & Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        }
    ) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a new note or task…", fontSize = 14.sp) },
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
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(Modifier.height(10.dp))

            if (vm.notes.isEmpty()) {
                Text(
                    "No notes yet. Add one above.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(vm.notes, key = { it.id }) { note ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = note.done, onCheckedChange = { vm.toggleNote(note.id) })
                            Text(
                                note.text,
                                fontSize = 14.sp,
                                color = if (note.done) colors.onSurfaceVariant else colors.onSurface,
                                textDecoration = if (note.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { vm.deleteNote(note.id) }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.Delete,
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

// ==========================================
// SETTINGS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: AppViewModel,
    isCarbon: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cb = LocalClipboardManager.current

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("APPEARANCE", fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Carbon theme", fontSize = 14.sp, modifier = Modifier.weight(1f))
                Switch(checked = isCarbon, onCheckedChange = { onToggleTheme() })
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = colors.outline.copy(alpha = 0.25f))

            Text("API KEYS", fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = vm.geminiKey,
                onValueChange = { vm.changeGeminiKey(it) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("Gemini API key") },
                placeholder = { Text("AIza…") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            KeyStatus(vm.statusGemini, vm.testingProvider == "gemini") {
                vm.testProvider("gemini")
            }

            OutlinedTextField(
                value = vm.openRouterKey,
                onValueChange = { vm.changeOpenRouterKey(it) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                label = { Text("OpenRouter API key") },
                placeholder = { Text("sk-or-v1-…") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            KeyStatus(vm.statusOpenRouter, vm.testingProvider == "openrouter") {
                vm.testProvider("openrouter")
            }

            Text(
                "One key unlocks every model of that provider. Free models cost nothing.",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = colors.outline.copy(alpha = 0.25f))

            Text("MODEL", fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold)
            Text(
                vm.activeModel.label,
                fontSize = 14.sp,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                "${vm.models.size} models loaded · tap the model name in chat to switch",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = colors.outline.copy(alpha = 0.25f))

            TextButton(onClick = {
                cb.setText(AnnotatedString(vm.diagnosticsText()))
            }) {
                Text("Copy diagnostics")
            }
            Text(
                "Diagnostics never include your API keys.",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KeyStatus(status: String, testing: Boolean, onTest: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onTest, enabled = !testing && status != "PASTE_KEY") {
            Text(if (testing) "Testing…" else "Test")
        }
        Text(
            when {
                status.isEmpty() -> "Not tested"
                status == "PASTE_KEY" -> "Add a key first"
                else -> status
            },
            fontSize = 11.sp,
            color = if (status.startsWith("OK") || status.startsWith("CONNECTED")) colors.primary else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
