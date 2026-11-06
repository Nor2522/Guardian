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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.PinField

// First-run rule: with no PIN, the gate never exposes its field and routes
// straight to onboarding. The PIN is held in memory until hashed storage
// arrives at Step 3, so process death counts as a first run again.
@Composable
fun GateScreen(
    storedPin: String?,
    onExitToOnboardingWelcome: () -> Unit,
    onUnlocked: () -> Unit,
) {
    LaunchedEffect(storedPin) {
        if (storedPin == null) onExitToOnboardingWelcome()
    }
    if (storedPin == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
        return
    }
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
                var pin by remember { mutableStateOf("") }
                var wrongPin by remember { mutableStateOf(false) }
                PinField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        wrongPin = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = wrongPin,
                    errorMessage = if (wrongPin) stringResource(R.string.gate_error_wrong) else null,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (pin == storedPin) {
                            onUnlocked()
                        } else {
                            pin = ""
                            wrongPin = true
                        }
                    },
                    enabled = pin.length == 6,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.gate_unlock),
                        style = MaterialTheme.typography.labelLarge,
                    )
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
