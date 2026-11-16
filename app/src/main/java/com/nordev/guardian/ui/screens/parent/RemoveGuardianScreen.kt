package com.nordev.guardian.ui.screens.parent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PinVerifyResult
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.PinField
import com.nordev.guardian.ui.components.StickyActionBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// The PIN is asked again here even in an unlocked session. After a correct PIN
// the app turns its own protection off and hands the last step to Android's
// uninstall screen, so a canceled uninstall leaves a visible, weak state rather
// than a hidden one.
@Composable
fun RemoveGuardianScreen(
    pinRepository: ParentPinRepository,
    onBack: () -> Unit,
    onTurnOffProtection: suspend () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pin by rememberSaveable { mutableStateOf("") }
    var checking by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(RemovalError.None) }
    var lockedUntil by rememberSaveable { mutableLongStateOf(0L) }
    var now by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var protectionOff by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(lockedUntil) {
        while (lockedUntil > System.currentTimeMillis()) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
        now = System.currentTimeMillis()
    }

    val remainingSeconds = ((lockedUntil - now + 999) / 1000).coerceAtLeast(0L).toInt()
    val locked = remainingSeconds > 0

    fun confirm() {
        if (checking || locked || pin.length != 6) return
        checking = true
        error = RemovalError.None
        scope.launch {
            when (val result = pinRepository.verify(pin)) {
                PinVerifyResult.Success -> {
                    pin = ""
                    protectionOff = true
                    onTurnOffProtection()
                    val uninstall = Intent(
                        Intent.ACTION_DELETE,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(uninstall) }
                    checking = false
                }
                PinVerifyResult.Wrong -> {
                    pin = ""
                    error = RemovalError.Wrong
                    checking = false
                }
                is PinVerifyResult.Locked -> {
                    pin = ""
                    error = RemovalError.Locked
                    lockedUntil = System.currentTimeMillis() + result.remainingMillis
                    checking = false
                }
                PinVerifyResult.NoPin -> {
                    error = RemovalError.NoPin
                    checking = false
                }
                PinVerifyResult.Failure -> {
                    error = RemovalError.Failed
                    checking = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.remove_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            StickyActionBar(
                primaryLabel = stringResource(R.string.remove_action),
                onPrimary = { confirm() },
                primaryEnabled = pin.length == 6 && !checking && !locked,
                primaryDestructive = true,
                secondaryLabel = stringResource(R.string.remove_keep),
                onSecondary = onBack,
                modifier = Modifier.imePadding(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.remove_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            PinField(
                value = pin,
                onValueChange = {
                    pin = it
                    if (error == RemovalError.Wrong) error = RemovalError.None
                },
                label = stringResource(R.string.remove_pin_label),
                isError = error == RemovalError.Wrong || error == RemovalError.NoPin ||
                    error == RemovalError.Failed,
                errorMessage = when (error) {
                    RemovalError.Wrong -> stringResource(R.string.gate_error_wrong)
                    RemovalError.NoPin -> stringResource(R.string.remove_no_pin)
                    RemovalError.Failed -> stringResource(R.string.remove_failed)
                    else -> null
                },
                enabled = !checking && !locked,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            if (locked) {
                Text(
                    text = stringResource(R.string.gate_locked, remainingSeconds),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            } else {
                Text(
                    text = stringResource(R.string.remove_pin_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.remove_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (protectionOff) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.remove_protection_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class RemovalError { None, Wrong, Locked, NoPin, Failed }
