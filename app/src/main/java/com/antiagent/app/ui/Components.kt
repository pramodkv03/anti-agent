package com.antiagent.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Gradient rounded logo mark used in the drawer, top bar and empty state. */
@Composable
fun AgentLogo(size: Dp = 40.dp, corner: Dp = 12.dp, letterSize: Int = 20) {
    val c = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(listOf(c.primary, c.secondary))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "A",
            fontSize = letterSize.sp,
            fontWeight = FontWeight.Black,
            color = c.onPrimary
        )
    }
}

@Composable
fun StatusDot(color: Color = MaterialTheme.colorScheme.primary, size: Dp = 7.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

/** Small pill that says "Agent online" or "Working…". */
@Composable
fun AgentStatusChip(text: String, working: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (working) c.primary.copy(alpha = 0.14f) else c.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusDot(color = if (working) c.secondary else c.primary)
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (working) c.primary else c.onSurfaceVariant
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        letterSpacing = 1.2f.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = modifier
    )
}

/** Capability pill, e.g. Web / Vision / Tools. */
@Composable
fun CapChip(icon: ImageVector, label: String, active: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) c.primary.copy(alpha = 0.15f) else c.surfaceVariant)
            .border(
                1.dp,
                if (active) c.primary.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = if (active) c.primary else c.onSurfaceVariant
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = if (active) c.primary else c.onSurfaceVariant
        )
    }
}

/** Compact tool tile for the drawer (icon over label). */
@Composable
fun ToolTile(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) c.primary.copy(alpha = 0.16f) else c.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) c.primary else c.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) c.primary else c.onSurfaceVariant
        )
    }
}

/** Large tappable card, used on the home/empty screen. */
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = c.surfaceVariant,
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp)) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.onSurface)
            Text(
                subtitle,
                fontSize = 10.5.sp,
                color = c.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Section card wrapper for settings-style grouped content. */
@Composable
fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = c.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            SectionLabel(title)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/** Row entry with a leading icon, used inside cards. */
@Composable
fun IconRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(c.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.onSurface)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) trailing() else if (onClick != null) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.onSurfaceVariant)
        }
    }
}
