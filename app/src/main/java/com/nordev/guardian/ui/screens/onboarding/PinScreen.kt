package com.nordev.guardian.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.domain.pin.PinPolicy
import com.nordev.guardian.ui.components.PinField
import com.nordev.guardian.ui.theme.LocalGuardianStatusColors
import kotlinx.coroutines.launch

// Validates the two entries, hashes the PIN off the main thread, and keeps the
// button in Saving until DataStore confirms the write.
@Composable
fun PinScreen(
    onBack: () -> Unit,
    repository: ParentPinRepository,
    onContinue: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val status = LocalGuardianStatusColors.current
    val enterSix = stringResource(R.string.onboarding_pin_enter_six)
    val weakPin = stringResource(R.string.onboarding_pin_weak)
    val mismatch = stringResource(R.string.onboarding_pin_mismatch)
    val saveFailed = stringResource(R.string.onboarding_pin_save_error)

    OnboardingShell(
        stepNumber = 3,
        onBack = onBack,
        primaryLabel = if (saving) {
            stringResource(R.string.onboarding_pin_saving)
        } else {
            stringResource(R.string.onboarding_continue)
        },
        onPrimary = {
            saveError = null
            when {
                pin.length != 6 -> {
                    pinError = enterSix
                    confirmError = null
                }
                PinPolicy.isWeak(pin) -> {
                    pinError = weakPin
                    confirmError = null
                }
                confirm.length != 6 -> {
                    pinError = null
                    confirmError = enterSix
                }
                confirm != pin -> {
                    pinError = null
                    confirmError = mismatch
                }
                else -> {
                    pinError = null
                    confirmError = null
                    val chosen = pin
                    saving = true
                    scope.launch {
                        val result = runCatching { repository.setPin(chosen) }
                        saving = false
                        if (result.isSuccess) {
                            pin = ""
                            confirm = ""
                            onContinue()
                        } else {
                            saveError = saveFailed
                        }
                    }
                }
            }
        },
        primaryEnabled = !saving,
    ) {
        Text(
            text = stringResource(R.string.onboarding_pin_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_pin_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        PinField(
            value = pin,
            onValueChange = { pin = it },
            label = stringResource(R.string.onboarding_pin_label),
            isError = pinError != null,
            errorMessage = pinError,
            enabled = !saving,
        )
        Spacer(Modifier.height(12.dp))
        PinField(
            value = confirm,
            onValueChange = { confirm = it },
            label = stringResource(R.string.onboarding_pin_confirm_label),
            isError = confirmError != null,
            errorMessage = confirmError,
            enabled = !saving,
        )
        if (saveError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = saveError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        Spacer(Modifier.height(24.dp))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = status.warningContainer,
            contentColor = status.onWarningContainer,
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = status.warning,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_pin_no_reset),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_pin_reinstall),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
