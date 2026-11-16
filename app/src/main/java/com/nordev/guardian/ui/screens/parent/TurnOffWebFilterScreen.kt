package com.nordev.guardian.ui.screens.parent

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// The switch never turns the filter off on its own. A correct PIN here is the
// only in-app way to stop it; the same PIN remains the parent's session PIN.
@Composable
fun TurnOffWebFilterScreen(
    pinRepository: ParentPinRepository,
    onBack: () -> Unit,
    onTurnOff: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var pin by rememberSaveable { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(TurnOffError.None) }
    var lockedUntil by rememberSaveable { mutableLongStateOf(0L) }
    var now by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

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
        error = TurnOffError.None
        val candidate = pin
        scope.launch {
            when (val result = pinRepository.verify(candidate)) {
                PinVerifyResult.Success -> {
                    pin = ""
                    try {
                        onTurnOff()
                        checking = false
                        onBack()
                    } catch (_: Exception) {
                        error = TurnOffError.Failed
                        checking = false
                    }
                }
                PinVerifyResult.Wrong -> {
                    pin = ""
                    error = TurnOffError.Wrong
                    checking = false
                }
                is PinVerifyResult.Locked -> {
                    pin = ""
                    error = TurnOffError.Locked
                    lockedUntil = System.currentTimeMillis() + result.remainingMillis
                    checking = false
                }
                PinVerifyResult.NoPin -> {
                    pin = ""
                    error = TurnOffError.NoPin
                    checking = false
                }
                PinVerifyResult.Failure -> {
                    pin = ""
                    error = TurnOffError.Failed
                    checking = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.web_turn_off_screen_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            StickyActionBar(
                primaryLabel = if (checking) {
                    stringResource(R.string.gate_checking)
                } else {
                    stringResource(R.string.web_turn_off_confirm)
                },
                onPrimary = { confirm() },
                primaryEnabled = pin.length == 6 && !checking && !locked,
                primaryDestructive = true,
                secondaryLabel = stringResource(R.string.web_turn_off_keep),
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
                text = stringResource(R.string.web_turn_off_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            PinField(
                value = pin,
                onValueChange = {
                    pin = it
                    if (error == TurnOffError.Wrong) error = TurnOffError.None
                },
                label = stringResource(R.string.remove_pin_label),
                isError = error == TurnOffError.Wrong || error == TurnOffError.NoPin ||
                    error == TurnOffError.Failed,
                errorMessage = when (error) {
                    TurnOffError.Wrong -> stringResource(R.string.gate_error_wrong)
                    TurnOffError.NoPin -> stringResource(R.string.remove_no_pin)
                    TurnOffError.Failed -> stringResource(R.string.remove_failed)
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
                text = stringResource(R.string.web_turn_off_settings_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class TurnOffError { None, Wrong, Locked, NoPin, Failed }
