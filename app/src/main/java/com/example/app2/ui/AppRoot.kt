package com.example.app2.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app2.TodoApp
import com.example.app2.ui.auth.LoginScreen
import com.example.app2.ui.theme.TodoistTheme
import kotlinx.coroutines.launch

/**
 * App 入口：先看有没有登录态，再决定进登录页还是主界面。
 * 主题模式在这里统一持有（登录页也要跟着深浅色走），不再塞进待办 ViewModel。
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as TodoApp
    val scope = rememberCoroutineScope()

    var themeMode by remember { mutableIntStateOf(app.prefs.themeMode) }
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val session by app.auth.session.collectAsStateWithLifecycle()

    TodoistTheme(darkTheme = dark) {
        // 让状态栏 / 导航栏图标的明暗跟着主题走
        val view = LocalView.current
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }

        val current = session
        if (current == null) {
            LoginScreen()
        } else {
            TodoistRoot(
                session = current,
                themeMode = themeMode,
                onThemeChange = { mode ->
                    app.prefs.themeMode = mode
                    themeMode = mode
                },
                onSignOut = { scope.launch { app.auth.signOut() } }
            )
        }
    }
}
