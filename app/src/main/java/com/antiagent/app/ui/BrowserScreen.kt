package com.antiagent.app.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.antiagent.app.AppViewModel
import kotlinx.coroutines.launch

private const val JS_SYSTEM =
    "You control a web page inside an Android WebView. Return ONLY a single line of valid " +
        "JavaScript to carry out the user's request on the current page. No explanation, no " +
        "markdown, no code fences. Use document.querySelector / getElementById / click() / " +
        "value assignment. Never navigate to a different origin or exfiltrate data."
private const val ASK_SYSTEM =
    "You are a helpful assistant. Answer the user's question using the page text provided. " +
        "Be concise and cite the page when useful."

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var address by remember { mutableStateOf("https://www.google.com") }
    var loadedUrl by remember { mutableStateOf("https://www.google.com") }
    var progress by remember { mutableStateOf(0) }
    var pageText by remember { mutableStateOf("") }
    var actionDraft by remember { mutableStateOf("") }
    var pendingJs by remember { mutableStateOf<String?>(null) }
    var asking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Keep a single real WebView instance across recompositions.
    val webView = remember {
        WebView(ctx.applicationContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 12; AntiAgent) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    pageText = ""
                    evaluateJavascript("document.body.innerText") { value ->
                        pageText = if (value == null) "" else
                            value.trim('"').replace("\\n", "\n").take(20000)
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress
                }
            }
            loadUrl(loadedUrl)
        }
    }

    fun navigate(raw: String) {
        val q = raw.trim()
        if (q.isBlank()) return
        address = q
        val url = when {
            q.contains(" ") && !q.contains(".") ->
                "https://duckduckgo.com/?q=" + android.net.Uri.encode(q)
            q.startsWith("http://") || q.startsWith("https://") -> q
            else -> "https://" + q
        }
        loadedUrl = url
        address = url
        pageText = ""
        webView.loadUrl(url)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(s.browser, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = s.close)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            webView.evaluateJavascript("history.back()") { navigate(loadedUrl) }
                        }) { Text("‹", fontSize = 20.sp) }
                        TextButton(onClick = { webView.reload() }) { Text("⟳", fontSize = 18.sp) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface,
                        titleContentColor = colors.onSurface,
                        navigationIconContentColor = colors.onSurface
                    )
                )
                if (progress in 1..99) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ------------------------------------------------------- address bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(s.address, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Button(onClick = { navigate(address) }) { Text(s.go) }
            }

            // ------------------------------------------------------------ webview
            AndroidView(
                factory = { webView },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            // ---------------------------------------------------------- ai actions
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.automation, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        webView.evaluateJavascript("document.body.innerText") { value ->
                            pageText = if (value == null) "" else
                                value.trim('"').replace("\\n", "\n").take(20000)
                        }
                        message = s.browserHint
                    }) { Text(s.readPage, fontSize = 11.5.sp) }
                }
                Text(s.automationHint, fontSize = 10.5.sp, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = actionDraft,
                        onValueChange = { actionDraft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. click the Login button", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Button(
                        enabled = actionDraft.isNotBlank() && !asking,
                        onClick = {
                            asking = true
                            message = ""
                            scope.launch {
                                try {
                                    val draft = vm.aiAction(JS_SYSTEM, actionDraft.trim())
                                    pendingJs = draft
                                        .removePrefix("```javascript")
                                        .removePrefix("```js")
                                        .removePrefix("```")
                                        .removeSuffix("```")
                                        .trim()
                                } catch (e: Exception) {
                                    message = e.message ?: "failed"
                                } finally {
                                    asking = false
                                }
                            }
                        }
                    ) {
                        Text(if (asking) "…" else s.runAction, fontSize = 11.5.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        if (pageText.isBlank()) {
                            message = s.browserHint
                        } else {
                            scope.launch {
                                vm.sendFromExternal(
                                    "Read this web page (${loadedUrl}):\n\n" +
                                        pageText.take(6000) +
                                        "\n\nQuestion: " +
                                        (actionDraft.trim().ifBlank { "Summarize this page." })
                                )
                                message = "Sent to chat."
                            }
                        }
                    }) { Text(s.askAboutPage, fontSize = 11.5.sp) }
                    Spacer(Modifier.weight(1f))
                    if (message.isNotBlank()) {
                        Text(message, fontSize = 10.5.sp, color = colors.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }

    pendingJs?.let { js ->
        AlertDialog(
            onDismissRequest = { pendingJs = null },
            title = { Text(s.confirmJs) },
            text = {
                Text(
                    js.take(600),
                    fontSize = 11.5.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = colors.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    webView.evaluateJavascript("(function(){ $js })()") { result ->
                        message = "Result: " + (result ?: "done")
                    }
                    pendingJs = null
                }) { Text(s.runAction) }
            },
            dismissButton = {
                TextButton(onClick = { pendingJs = null }) { Text(s.cancel) }
            }
        )
    }
}
