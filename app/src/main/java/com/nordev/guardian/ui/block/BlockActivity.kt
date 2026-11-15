package com.nordev.guardian.ui.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nordev.guardian.domain.block.AccessDecision
import com.nordev.guardian.domain.block.BlockReason
import com.nordev.guardian.ui.components.BlockScreen
import com.nordev.guardian.ui.nav.BlockState
import com.nordev.guardian.ui.theme.GuardianTheme

// Launched by the accessibility service over the app it just paused. It lives in
// its own empty-affinity task so it never joins Guardian's parent navigation,
// and it finishes itself as soon as the service clears the block. Back is
// ignored; Home always works.
class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )
        setContent {
            GuardianTheme {
                val packageName = BlockState.blockedPackage
                val reason = BlockState.blockedReason
                val context = LocalContext.current

                LaunchedEffect(packageName) {
                    if (packageName == null) finish()
                }

                if (packageName != null) {
                    val appName = remember(packageName) {
                        runCatching {
                            context.packageManager.getApplicationInfo(packageName, 0)
                                .loadLabel(context.packageManager)
                                .toString()
                        }.getOrNull()?.takeIf { it.isNotBlank() } ?: packageName
                    }
                    BlockScreen(
                        decision = AccessDecision.Blocked(
                            reason ?: BlockReason.DailyLimit(packageName, 0),
                        ),
                        appName = appName,
                        onGoHome = {
                            BlockState.clear()
                            startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_HOME)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                            finish()
                        },
                        onWhyPaused = { },
                    )
                }
            }
        }
    }
}
