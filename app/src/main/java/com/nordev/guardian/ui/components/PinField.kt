package com.nordev.guardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R

// Masks input and reports only how many digits are entered, never the digits.
// The dots row carries the accessible state; the system keyboard stays in charge
// of filling, pasting, and password helpers.
@Composable
fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.pin_field_cd),
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val digitCount = 6
    val stateText = stringResource(R.string.gate_pin_state, value.length)
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { next -> onValueChange(next.filter { it.isDigit() }.take(digitCount)) },
            label = { Text(text = label) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 16.dp)
                .semantics { stateDescription = stateText },
        ) {
            repeat(digitCount) { index ->
                val filled = index < value.length
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}
