package com.antiagent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antiagent.app.ui.AntiAgentTheme
import com.antiagent.app.ui.ChatScreen
import com.antiagent.app.ui.DrawerContent
import com.antiagent.app.ui.NotesScreen
import com.antiagent.app.ui.SettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            AntiAgentTheme(vm.theme) {
                AppRoot(vm)
            }
        }
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
