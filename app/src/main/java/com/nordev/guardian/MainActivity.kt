package com.nordev.guardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
            // Theme choice is in-memory this step. Persists at Step 3.
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.System) }
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
                )
            }
        }
    }
}
