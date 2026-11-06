package com.nordev.guardian

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.nordev.guardian.ui.nav.GuardianNavHost
import com.nordev.guardian.ui.nav.ThemeMode
import com.nordev.guardian.ui.theme.GuardianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Theme choice is in-memory for now.
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
            var secure by remember { mutableStateOf(false) }
            LaunchedEffect(secure) {
                if (secure) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            GuardianTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                },
            ) {
                GuardianNavHost(
                    themeMode = themeMode,
                    onThemeChange = { themeMode = it },
                    onSecureChange = { secure = it },
                )
            }
        }
    }
}
