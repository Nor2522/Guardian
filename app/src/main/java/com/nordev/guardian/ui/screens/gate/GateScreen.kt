package com.nordev.guardian.ui.screens.gate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
        return
    }

    val remainingSeconds = ((lockedUntil - now + 999) / 1000).coerceAtLeast(0L).toInt()
    val locked = remainingSeconds > 0

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                }
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
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
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
                Spacer(Modifier.height(16.dp))
                Button(
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
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    if (checking) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.gate_checking),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.gate_unlock),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
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
