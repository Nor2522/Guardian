package com.nordev.guardian.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nordev.guardian.data.permissions.NotificationAccessChecker
import kotlinx.coroutines.delay

// The always-on notice is Android's proof that protection is running. Its
// permission can be revoked at any time from settings, so every screen that
// promises protection watches it instead of trusting the setup flow.
@Composable
fun rememberNotificationsVisible(pollMillis: Long = 5_000L): Boolean {
    val context = LocalContext.current
    val checker = remember(context) { NotificationAccessChecker(context) }
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(checker) {
        while (true) {
            visible = checker.isGranted()
            delay(pollMillis)
        }
    }
    return visible
}
