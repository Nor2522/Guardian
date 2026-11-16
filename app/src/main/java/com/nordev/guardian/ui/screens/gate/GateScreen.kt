package com.nordev.guardian.ui.screens.gate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.prefs.ParentPinRepository
import com.nordev.guardian.data.prefs.PinVerifyResult
import com.nordev.guardian.ui.components.GuardianPrimaryButton
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.PinField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// First-run rule: with no stored PIN, the gate never exposes its field and
// routes straight to onboarding. Lockout state is read from DataStore, so a
// restart does not clear the wait.
@Composable
fun GateScreen(
    repository: ParentPinRepository,
    onExitToOnboardingWelcome: () -> Unit,
    onUnlocked: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var hasPin by remember { mutableStateOf(false) }
    var lockedUntil by remember { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var checking by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var wrongPin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val state = repository.state()
        hasPin = state.hasPin
        lockedUntil = state.lockedUntil
        now = System.currentTimeMillis()
        loading = false
        if (!state.hasPin) onExitToOnboardingWelcome()
    }

    LaunchedEffect(lockedUntil) {
        while (lockedUntil > System.currentTimeMillis()) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
        now = System.currentTimeMillis()
    }

    if (loading || !hasPin) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val remainingSeconds = ((lockedUntil - now + 999) / 1000).coerceAtLeast(0L).toInt()
    val locked = remainingSeconds > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                IconBadge(
                    icon = Icons.Outlined.Lock,
                    size = 96.dp,
                    iconSize = 42.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.gate_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gate_prompt),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                PinField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        wrongPin = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = wrongPin,
                    errorMessage = if (wrongPin) stringResource(R.string.gate_error_wrong) else null,
                    enabled = !checking && !locked,
                )
                if (locked) {
                    Spacer(Modifier.height(8.dp))
                    val announce = remainingSeconds == 30 ||
                        remainingSeconds == 10 ||
                        remainingSeconds == 1
                    Text(
                        text = stringResource(R.string.gate_locked, remainingSeconds),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = if (announce) {
                            Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        } else {
                            Modifier
                        },
                    )
                }
                Spacer(Modifier.height(20.dp))
                GuardianPrimaryButton(
                    label = if (checking) {
                        stringResource(R.string.gate_checking)
                    } else {
                        stringResource(R.string.gate_unlock)
                    },
                    onClick = {
                        val candidate = pin
                        checking = true
                        wrongPin = false
                        scope.launch {
                            when (val result = repository.verify(candidate)) {
                                is PinVerifyResult.Success -> {
                                    pin = ""
                                    onUnlocked()
                                }
                                is PinVerifyResult.Locked -> {
                                    pin = ""
                                    lockedUntil = System.currentTimeMillis() + result.remainingMillis
                                    now = System.currentTimeMillis()
                                }
                                is PinVerifyResult.Wrong -> {
                                    pin = ""
                                    wrongPin = true
                                }
                                is PinVerifyResult.Failure -> {
                                    pin = ""
                                    wrongPin = true
                                }
                                is PinVerifyResult.NoPin -> {
                                    pin = ""
                                    hasPin = false
                                    onExitToOnboardingWelcome()
                                }
                            }
                            checking = false
                        }
                    },
                    enabled = pin.length == 6 && !checking && !locked,
                    loading = checking,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Text(
            text = stringResource(R.string.gate_footer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }
}
