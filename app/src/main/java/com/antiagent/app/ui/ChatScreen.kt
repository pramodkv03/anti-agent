package com.antiagent.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.ChatMessage
import com.antiagent.app.data.Models
import com.antiagent.app.util.ImageUtil
import com.antiagent.app.voice.Speaker
import com.antiagent.app.voice.VoiceInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: AppViewModel, onMenu: () -> Unit) {
    val ctx = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var showModels by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }

    val voice = remember { VoiceInput(ctx) }
    val speaker = remember { Speaker(ctx) }
    DisposableEffect(Unit) {
        onDispose {
            voice.destroy()
            speaker.shutdown()
        }
    }

    LaunchedEffect(vm.lastAnswer) {
        if (vm.autoSpeak && vm.lastAnswer.isNotBlank()) speaker.speak(vm.lastAnswer)
    }

    LaunchedEffect(vm.error) {
        val msg = vm.error
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.error = null
        }
    }

    fun beginListening() {
        listening = true
        voice.start(
            onText = { text ->
                vm.draft = (vm.draft + " " + text).trim()
                listening = false
            },
            onError = { msg ->
                vm.error = msg
                listening = false
            }
        )
    }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) beginListening() else vm.error = "Mic permission chahiye (Settings > Apps > Anti Agent)"
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
                if (b64 != null) {
                    vm.pendingImage = b64
                } else {
                    vm.error = "Photo padh nahi paya, doosri photo try karo"
                }
            }
        }
    }

    val conv = vm.conversations.firstOrNull { it.id == vm.currentId }
    val rows = conv?.messages?.toList() ?: emptyList()
    val total = rows.size + if (vm.sending) 1 else 0
    val listState = rememberLazyListState()

    LaunchedEffect(total, vm.streamingText.length) {
        if (total > 0) {
            try {
                listState.scrollToItem(total - 1)
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                title = {
                    Column(Modifier.clickable { showModels = true }) {
                        Text(
                            "Anti Agent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                vm.activeModel.label,
                                fontSize = 11.sp,
                                color = colors.primary
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = colors.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.newChat() }) {
                        Icon(Icons.Filled.Add, contentDescription = "New chat")
                    }
                    IconButton(onClick = { vm.screen = "settings" }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface,
                    actionIconContentColor = colors.onSurface
                )
            )
        },
        bottomBar = {
            Composer(
                vm = vm,
                listening = listening,
                onPickImage = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onMic = {
                    if (listening) {
                        voice.stop()
                        listening = false
                    } else if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        beginListening()
                    } else {
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStop = { vm.stop() }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (rows.isEmpty() && !vm.sending) {
                EmptyState(onPick = { s -> vm.draft = s })
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(count = rows.size, key = { it }) { index ->
                        MessageRow(rows[index], onSpeak = { speaker.speak(it) })
                    }
                    if (vm.sending) {
                        item(key = "streaming") {
                            if (vm.streamingText.isBlank()) {
                                TypingBubble()
                            } else {
                                MessageRow(ChatMessage("assistant", vm.streamingText), onSpeak = {})
                            }
                        }
                    }
                }
            }
        }
    }

    if (showModels) {
        AlertDialog(
            onDismissRequest = { showModels = false },
            title = { Text("Model chuno") },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Models.all.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.selectModel(m.id)
                                    showModels = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(m.label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    m.providerLabel,
                                    style = MaterialTheme.typography.bodySmall,
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
                }
            },
            confirmButton = {
                TextButton(onClick = { showModels = false }) { Text("Band karo") }
            }
        )
    }
}

@Composable
private fun EmptyState(onPick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val suggestions = listOf(
        "Instagram reel ke liye caption likho",
        "Mujhe Python seekhne ka 30 din ka plan banao",
        "Aaj ke liye 5 healthy breakfast ideas",
        "Photo bhej kar poocho: isme kya hai?"
    )
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("A", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Anti Agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                "Aapka free AI agent",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { s ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .clickable { onPick(s) }
                            .padding(12.dp)
                    ) {
                        Text(s, fontSize = 13.sp, color = colors.onSurface)
                    }
                }
            }
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
                "Soch raha hoon…",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageRow(message: ChatMessage, onSpeak: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    val isUser = message.role == "user"

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) colors.primary else colors.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                message.image?.let { b64 ->
                    val bmp = remember(b64) { ImageUtil.decode(b64) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Photo",
                            modifier = Modifier
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
                val textColor = if (isUser) colors.onPrimary else colors.onSurface
                if (isUser) {
                    Text(
                        message.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    SelectionContainer {
                        Markdown(message.text, color = textColor)
                    }
                }
            }
        }

        if (!isUser && message.text.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { clipboard.setText(AnnotatedString(message.text)) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Copy", fontSize = 11.sp)
                }
                TextButton(
                    onClick = { onSpeak(message.text) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Suno", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun Composer(
    vm: AppViewModel,
    listening: Boolean,
    onPickImage: () -> Unit,
    onMic: () -> Unit,
    onStop: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, tonalElevation = 3.dp) {
        Column(
            Modifier
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            vm.pendingImage?.let { b64 ->
                val bmp = remember(b64) { ImageUtil.decode(b64) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Selected photo",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Photo attach hai",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { vm.pendingImage = null }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove photo",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = onPickImage) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = "Photo bhejo",
                        tint = colors.onSurfaceVariant
                    )
                }
                TextField(
                    value = vm.draft,
                    onValueChange = { vm.draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Kuch bhi poocho…", fontSize = 14.sp, maxLines = 1)
                    },
                    maxLines = 6,
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        disabledContainerColor = colors.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface)
                )
                IconButton(onClick = onMic) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Bol kar likho",
                        tint = if (listening) colors.error else colors.onSurfaceVariant
                    )
                }
                if (vm.sending) {
                    FilledIconButton(
                        onClick = onStop,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.error)
                    ) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = "Roko",
                            tint = colors.onError
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = { vm.send() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary)
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Bhejo",
                            tint = colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}
