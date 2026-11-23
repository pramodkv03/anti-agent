package com.antiagent.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antiagent.app.AppViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    val today = rememberToday()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(s.usage, fontWeight = FontWeight.Bold) },
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
            Text(s.usage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Text(s.usageHint, fontSize = 11.5.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            // ------------------------------------------ OpenRouter balance
            Text(s.balance, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            val info = vm.keyInfoOpenRouter
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.refreshKeyInfo() }) {
                    Text(if (info.isBlank()) s.refreshBalance else s.refreshBalance, fontSize = 12.sp)
                }
            }
            when {
                vm.openRouterKey.isBlank() -> Text(s.noBalance, fontSize = 12.sp, color = colors.onSurfaceVariant)
                info.isBlank() -> Text("…", fontSize = 12.sp, color = colors.onSurfaceVariant)
                info.startsWith("ERROR:") -> Text(
                    info.removePrefix("ERROR:"),
                    fontSize = 11.5.sp,
                    color = colors.error
                )
                else -> {
                    val parsed = remember(info) { parseOrInfo(info) }
                    BalanceBox(
                        used = parsed.first,
                        limit = parsed.second,
                        left = parsed.third,
                        raw = parsed.fourth
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))
            Spacer(Modifier.height(12.dp))

            // ------------------------------------------ local counters
            Text(s.today + " · " + today, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(6.dp))

            val rows = vm.usage.filter { it.day == today }.sortedByDescending { it.requests + it.inputTokens + it.outputTokens }
            val allRows = vm.usage.sortedByDescending { it.requests + it.inputTokens + it.outputTokens }
            val totalReq = allRows.sumOf { it.requests }
            val totalIn = allRows.sumOf { it.inputTokens }
            val totalOut = allRows.sumOf { it.outputTokens }

            StatLine(s.requests, totalReq.toString(), s.today + ": " + rows.sumOf { it.requests })
            StatLine(s.tokensIn, formatNum(totalIn), s.today + ": " + formatNum(rows.sumOf { it.inputTokens }))
            StatLine(s.tokensOut, formatNum(totalOut), s.today + ": " + formatNum(rows.sumOf { it.outputTokens }))

            Spacer(Modifier.height(14.dp))
            Text(s.allTime + " · " + s.projects, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            if (allRows.isEmpty()) {
                Text("—", fontSize = 12.sp, color = colors.onSurfaceVariant)
            } else {
                allRows.forEach { row ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            row.key,
                            fontSize = 11.5.sp,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            "${row.provider} · ${s.requests}: ${row.requests} · " +
                                "${s.tokensIn}: ${formatNum(row.inputTokens)} · " +
                                "${s.tokensOut}: ${formatNum(row.outputTokens)}",
                            fontSize = 10.5.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(s.geminiQuotaNote, fontSize = 11.sp, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = { vm.resetUsage() }) {
                Text(s.resetUsage, fontSize = 12.sp, color = colors.error)
            }
        }
    }
}

@Composable
private fun rememberToday(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun formatNum(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fk".format(n / 1_000.0)
    else -> n.toString()
}

/** used, limit, remaining, extra label */
private fun parseOrInfo(raw: String): Quadruple<String, String, String, String> {
    return try {
        val root = JSONObject(raw)
        val d = root.optJSONObject("data") ?: root
        val used = d.optString("usage", "—")
        val limit = d.optString("limit", "—")
        val label = d.optString("label", "")
        val left = if (used != "—" && limit != "—" && limit != "null") {
            val u = used.toDoubleOrNull()
            val l = limit.toDoubleOrNull()
            if (u != null && l != null) "%.4f".format(l - u) else "—"
        } else if (limit == "null" || limit == "—") "Unlimited (free tier)" else "—"
        Quadruple(used, limit, left, label)
    } catch (_: Exception) {
        Quadruple("—", "—", "—", raw.take(180))
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun BalanceBox(used: String, limit: String, left: String, raw: String) {
    val s = LocalStr.current
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(raw, fontSize = 11.sp, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(s.balanceUsed, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(used, fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(s.balanceLimit, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(limit, fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(s.balanceLeft, fontSize = 10.sp, color = colors.onSurfaceVariant)
                Text(left, fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, sub: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, fontSize = 15.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
            Text(sub, fontSize = 10.sp, color = colors.onSurfaceVariant)
        }
    }
}
