package com.antiagent.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antiagent.app.ui.AntiAgentTheme
import com.antiagent.app.ui.ChatScreen
import com.antiagent.app.ui.DrawerContent
import com.antiagent.app.ui.LocalStr
import com.antiagent.app.ui.NotesScreen
import com.antiagent.app.ui.SettingsScreen
import com.antiagent.app.ui.Str
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            AntiAgentTheme(vm.theme) {
                CompositionLocalProvider(
                    LocalStr provides remember(vm.language) { Str(vm.language) }
                ) {
                    StatusBars()
                    AppRoot(vm)
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

@Composable
private fun StatusBars() {
    val colors = MaterialTheme.colorScheme
    val view = LocalView.current
    val light = colors.background.luminance() > 0.5f
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = colors.background.toArgb()
        window.navigationBarColor = colors.background.toArgb()
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = light
        controller.isAppearanceLightNavigationBars = light
    }
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = vm.screen != "chat" && !drawerState.isOpen) {
        vm.screen = "chat"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(vm) {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        when (vm.screen) {
            "settings" -> SettingsScreen(vm) { vm.screen = "chat" }
            "notes" -> NotesScreen(vm) { vm.screen = "chat" }
            else -> ChatScreen(vm) { scope.launch { drawerState.open() } }
        }
    }
}
