package com.nordev.guardian.ui.guard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.ui.nav.GuardState
import com.nordev.guardian.ui.theme.GuardianTheme

// Its own empty-affinity task, never in recents, shown over the system screen
// the accessibility service guarded. Back is ignored; Home always works.
class GuardActivity : ComponentActivity() {
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
                val kind = GuardState.kind
                val pinRepository = remember { ParentPinRepository(applicationContext) }

                // onUnlocked and onGoHome both clear the kind and finish, so
                // this only covers a guard that lost its kind while hidden.
                LaunchedEffect(kind) {
                    if (kind == null && !isFinishing) finish()
                }

                if (kind != null) {
                    GuardScreen(
                        kind = kind,
                        pinRepository = pinRepository,
                        onUnlocked = {
                            GuardState.grantGrace(kind)
                            finish()
                        },
                        onGoHome = {
                            GuardState.clear()
                            startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_HOME)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                            finish()
                        },
                    )
                }
            }
        }
    }
}
