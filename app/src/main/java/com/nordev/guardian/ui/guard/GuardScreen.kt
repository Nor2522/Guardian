package com.nordev.guardian.ui.guard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PinVerifyResult
import com.nordev.guardian.ui.components.GuardianBackground
import com.nordev.guardian.ui.components.GuardianPrimaryButton
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.PinField
import com.nordev.guardian.ui.nav.GuardKind
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Shown over a system screen whose package and class the accessibility service
// recognized. No screen text is read. Back is ignored; Home always works, and
// returning to the guarded screen brings the guard back.
@Composable
fun GuardScreen(
    kind: GuardKind,
    pinRepository: ParentPinRepository,
    onUnlocked: () -> Unit,
    onGoHome: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var pin by rememberSaveable { mutableStateOf("") }
    var checking by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(GuardError.None) }
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
    val title = when (kind) {
        GuardKind.WebFilterSettings -> stringResource(R.string.guard_web_title)
    }
    val body = when (kind) {
        GuardKind.WebFilterSettings -> stringResource(R.string.guard_web_body)
    }

    fun confirm() {
        if (checking || locked || pin.length != 6) return
        checking = true
        error = GuardError.None
        val candidate = pin
        scope.launch {
            when (val result = pinRepository.verify(candidate)) {
                PinVerifyResult.Success -> {
                    pin = ""
                    onUnlocked()
                }
                PinVerifyResult.Wrong -> {
                    pin = ""
                    error = GuardError.Wrong
                    checking = false
                }
                is PinVerifyResult.Locked -> {
                    pin = ""
                    error = GuardError.Locked
                    lockedUntil = System.currentTimeMillis() + result.remainingMillis
                    checking = false
                }
                PinVerifyResult.NoPin -> {
                    pin = ""
                    error = GuardError.NoPin
                    checking = false
                }
                PinVerifyResult.Failure -> {
                    pin = ""
                    error = GuardError.Failed
                    checking = false
                }
            }
        }
    }

    GuardianBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = GuardianSpacing.xl, vertical = GuardianSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconBadge(
                    icon = Icons.Outlined.Lock,
                    size = 88.dp,
                    iconSize = 40.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(GuardianSpacing.lg))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        heading()
                        paneTitle = title
                    },
                )
                Spacer(Modifier.height(GuardianSpacing.sm))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(GuardianSpacing.xl))
                PinField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        if (error == GuardError.Wrong) error = GuardError.None
                    },
                    label = stringResource(R.string.guard_pin_label),
                    isError = error == GuardError.Wrong || error == GuardError.NoPin ||
                        error == GuardError.Failed,
                    errorMessage = when (error) {
                        GuardError.Wrong -> stringResource(R.string.gate_error_wrong)
                        GuardError.NoPin -> stringResource(R.string.remove_no_pin)
                        GuardError.Failed -> stringResource(R.string.remove_failed)
                        else -> null
                    },
                    enabled = !checking && !locked,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(GuardianSpacing.sm))
                if (locked) {
                    Text(
                        text = stringResource(R.string.gate_locked, remainingSeconds),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                } else {
                    Text(
                        text = stringResource(R.string.guard_grace_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(GuardianSpacing.lg))
                GuardianPrimaryButton(
                    label = if (checking) {
                        stringResource(R.string.gate_checking)
                    } else {
                        stringResource(R.string.guard_unlock)
                    },
                    onClick = { confirm() },
                    enabled = pin.length == 6 && !checking && !locked,
                    loading = checking,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = onGoHome,
                    modifier = Modifier.heightIn(min = GuardianSizes.minTouch),
                ) {
                    Text(
                        text = stringResource(R.string.guard_go_home),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private enum class GuardError { None, Wrong, Locked, NoPin, Failed }
