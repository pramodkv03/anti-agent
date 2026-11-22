package com.antiagent.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.antiagent.app.AppViewModel
import com.antiagent.app.data.ChatMessage
import com.antiagent.app.data.Personas
import com.antiagent.app.util.FileUtil
import com.antiagent.app.util.ImageUtil
import com.antiagent.app.voice.Speaker
import com.antiagent.app.voice.VoiceInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: AppViewModel, onMenu: () -> Unit) {
    val s = LocalStr.current
    val ctx = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var showModels by remember { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var showDeleteChat by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

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

    val beginVoice: () -> Unit = {
        listening = true
        voice.start(
            onText = { text ->
                vm.draft = (vm.draft + " " + text).trim()
                listening = false
            },
            onError = { _ ->
                vm.error = s.voiceError
                listening = false
            }
        )
    }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) beginVoice() else vm.error = s.micPermission
    }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
                if (b64 != null) vm.pendingImage = b64 else vm.error = s.photoError
            }
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = cameraUri
        if (ok && uri != null) {
            scope.launch {
                val b64 = withContext(Dispatchers.IO) { ImageUtil.encode(ctx, uri) }
                if (b64 != null) vm.pendingImage = b64 else vm.error = s.photoError
            }
        }
    }

    val launchCamera: () -> Unit = {
        try {
            val dir = File(ctx.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "cap_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        } catch (_: Exception) {
            vm.error = s.photoError
        }
    }

    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = withContext(Dispatchers.IO) { FileUtil.readText(ctx, uri) }
                when {
                    res == null -> vm.error = s.fileError
                    res.second == null -> vm.error = s.fileTooBig
                    else -> vm.attachFile(res.first, res.second ?: "")
                }
            }
        }
    }

    val conv = vm.conversations.firstOrNull { it.id == vm.currentId }
    val rows = conv?.messages?.toList() ?: emptyList()
    val modelsSnapshot = remember(vm.modelsVersion) { vm.models }
    val total = rows.size + if (vm.sending) 1 else 0
    val listState = rememberLazyListState()

    LaunchedEffect(total, vm.streamingText.length, vm.chatErrorKind) {
        val target = total + if (vm.chatErrorKind != null) 1 else 0
        if (target > 0) {
            try {
                listState.scrollToItem(target - 1)
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
                                color = colors.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 210.dp)
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
                    IconButton(onClick = { vm.screen = "browser" }) {
                        Icon(Icons.Filled.Public, contentDescription = s.browser)
                    }
                    TextButton(onClick = { vm.screen = "usage" }) {
                        Text("Usage", fontSize = 11.sp)
                    }
                    IconButton(onClick = { vm.newChat() }) {
                        Icon(Icons.Filled.Add, contentDescription = s.newChat)
                    }
                    IconButton(onClick = { showDeleteChat = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = s.deleteChat)
                    }
                    IconButton(onClick = { vm.screen = "settings" }) {
                        Icon(Icons.Filled.Settings, contentDescription = s.settingsTitle)
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
                onAttach = { showAttach = true },
                onMic = {
                    if (listening) {
                        voice.stop()
                        listening = false
                    } else if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        beginVoice()
                    } else {
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStop = { vm.stop() }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            PersonaRow(vm)
            Box(Modifier.weight(1f)) {
                if (rows.isEmpty() && !vm.sending && vm.chatErrorKind == null) {
                    EmptyState(
                        needsKey = vm.geminiKey.isBlank() && vm.openRouterKey.isBlank(),
                        onPick = { suggestion -> vm.draft = suggestion },
                        onSettings = { vm.screen = "settings" }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(count = rows.size, key = { it }) { index ->
                            val message = rows[index]
                            MessageRow(
                                message = message,
                                isLastAssistant = !vm.sending &&
                                    index == rows.lastIndex &&
                                    message.role == "assistant",
                                onSpeak = { speaker.speak(it) },
                                onRegenerate = { vm.regenerate() }
                            )
                        }
                        if (vm.fallbackNotice.isNotBlank()) {
                            item(key = "fallback") {
                                Text(
                                    vm.fallbackNotice,
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (vm.sending) {
                            item(key = "streaming") {
                                if (vm.streamingText.isBlank()) {
                                    TypingBubble()
                                } else {
                                    MessageRow(
                                        message = ChatMessage("assistant", vm.streamingText),
                                        isLastAssistant = false,
                                        onSpeak = {},
                                        onRegenerate = {}
                                    )
                                }
                            }
                        }
                        val errorKind = vm.chatErrorKind
                        if (errorKind != null) {
                            item(key = "error") {
                                ErrorBubble(
                                    kind = errorKind,
                                    detail = vm.chatErrorDetail,
                                    onRetry = { vm.retry() },
                                    onSettings = { vm.screen = "settings" }
                                )
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
            title = { Text(s.chooseModel) },
            text = {
                Column(
                    Modifier
                        .heightIn(max = 390.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    var lastProvider = ""
                    modelsSnapshot.forEach { m ->
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
                                    showModels = false
                                }
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
                TextButton(onClick = { showModels = false }) { Text(s.close) }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.connect(vm.activeModel.provider)
                    showModels = false
                }) { Text(s.refreshModels) }
            }
        )
    }

    if (showAttach) {
        AlertDialog(
            onDismissRequest = { showAttach = false },
            title = { Text(s.attach) },
            text = {
                Column {
                    AttachRow(Icons.Filled.PhotoCamera, s.camera) {
                        showAttach = false
                        launchCamera()
                    }
                    AttachRow(Icons.Filled.Image, s.gallery) {
                        showAttach = false
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    AttachRow(Icons.Filled.Description, s.file) {
                        showAttach = false
                        pickFile.launch(arrayOf("text/*", "application/json", "application/xml"))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttach = false }) { Text(s.cancel) }
            }
        )
    }

    if (showDeleteChat) {
        AlertDialog(
            onDismissRequest = { showDeleteChat = false },
            title = { Text(s.deleteChat) },
            text = { Text(s.deleteChatConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteChat = false
                    vm.deleteChat(vm.currentId)
                }) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChat = false }) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun AttachRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PersonaRow(vm: AppViewModel) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Personas.all.forEach { p ->
            val selected = vm.persona == p.id
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) colors.primary else colors.surfaceVariant)
                    .clickable { vm.selectPersona(p.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    s.agentName(p.id),
                    fontSize = 12.sp,
                    color = if (selected) colors.onPrimary else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorBubble(
    kind: String,
    detail: String?,
    onRetry: () -> Unit,
    onSettings: () -> Unit
) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val headline = when (kind) {
        "no_key_gemini" -> s.errorNoKeyGemini
        "no_key_openrouter" -> s.errorNoKeyOpenRouter
        "empty" -> s.errorEmpty
        "image_unsupported" -> s.imageNotSupported
        else -> s.errorFailed
    }
    val extra = if (kind == "image_unsupported") s.imageNotSupportedBody else null
    Surface(
        color = colors.error.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(s.errorTitle, fontWeight = FontWeight.Bold, color = colors.error, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(headline, fontSize = 13.sp, color = colors.onSurface)
            if (extra != null) {
                Spacer(Modifier.height(4.dp))
                Text(extra, fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
            if (detail != null && kind == "failed") {
                Spacer(Modifier.height(6.dp))
                Text(s.detail, fontSize = 11.sp, color = colors.onSurfaceVariant)
                Text(
                    detail,
                    fontSize = 10.5.sp,
                    color = colors.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 90.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
            Row(Modifier.padding(top = 6.dp)) {
                TextButton(onClick = onRetry) { Text(s.retry) }
                if (kind.startsWith("no_key") || kind == "failed" || kind == "image_unsupported") {
                    TextButton(onClick = onSettings) { Text(s.openSettings) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(needsKey: Boolean, onPick: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val suggestions = listOf(s.sug1, s.sug2, s.sug3, s.sug4)
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
            Text(s.emptyTagline, fontSize = 13.sp, color = colors.onSurfaceVariant)
            Text(s.formModel, fontSize = 11.sp, color = colors.onSurfaceVariant)
            if (needsKey) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary)
                        .clickable(onClick = onSettings)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        s.addFreeKey,
                        color = colors.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { text ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .clickable { onPick(text) }
                            .padding(12.dp)
                    ) {
                        Text(text, fontSize = 13.sp, color = colors.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingBubble() {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = colors.surfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
        ) {
            Text(
                s.thinking,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    isLastAssistant: Boolean,
    onSpeak: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    val s = LocalStr.current
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
                    Text(s.copy, fontSize = 11.sp)
                }
                TextButton(
                    onClick = { onSpeak(message.text) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(s.readAloud, fontSize = 11.sp)
                }
                if (isLastAssistant) {
                    TextButton(
                        onClick = onRegenerate,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(s.regenerate, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(
    vm: AppViewModel,
    listening: Boolean,
    onAttach: () -> Unit,
    onMic: () -> Unit,
    onStop: () -> Unit
) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, tonalElevation = 3.dp) {
        Column(
            Modifier
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (vm.pendingImage != null || vm.pendingFileName != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val b64 = vm.pendingImage
                    if (b64 != null) {
                        val bmp = remember(b64) { ImageUtil.decode(b64) }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = s.photoAttached,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(s.photoAttached, fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    } else {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            vm.pendingFileName ?: s.fileAttached,
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 220.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { vm.clearAttachment() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = s.remove,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                IconButton(onClick = onAttach) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = s.attach,
                        tint = colors.onSurfaceVariant
                    )
                }
                TextField(
                    value = vm.draft,
                    onValueChange = { vm.draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(s.askHint, fontSize = 14.sp, maxLines = 1)
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
                IconButton(onClick = {
                    if (vm.activeModel.provider != "gemini" && !vm.webSearch) {
                        vm.error = s.webNeedsGemini
                    } else {
                        vm.changeWebSearch(!vm.webSearch)
                    }
                }) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = s.webSearch,
                        tint = if (vm.webSearch) colors.primary else colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMic) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = s.listening,
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
                            contentDescription = s.stop,
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
                            contentDescription = s.send,
                            tint = colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}
