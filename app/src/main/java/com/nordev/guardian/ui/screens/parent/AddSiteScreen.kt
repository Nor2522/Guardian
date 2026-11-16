package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.domain.web.DomainMatcher
import com.nordev.guardian.domain.web.DomainName
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.StickyActionBar

// The normalized value is shown before saving, and the shipped adult list is
// read only: a duplicate there gets a message, never a second row.
@Composable
fun AddSiteScreen(
    existingDomains: Set<String>,
    adultDomains: Set<String>,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val normalized = remember(input) { DomainName.normalize(input) }
    val adultMatcher = remember(adultDomains) { DomainMatcher(adultDomains) }
    val alreadyOnAdultList = normalized != null && adultMatcher.blocks(normalized)
    val duplicate = normalized != null && normalized in existingDomains
    val showInvalid = attempted && normalized == null
    val canSave = normalized != null && !duplicate && !alreadyOnAdultList
    // Only a valid value that duplicates an existing rule disables Save. An
    // empty or malformed value keeps it tappable so the inline error can show.
    val saveEnabled = !(normalized != null && (duplicate || alreadyOnAdultList))

    fun save() {
        attempted = true
        val value = normalized
        if (value != null && !duplicate && !alreadyOnAdultList) onSave(value)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.web_add_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            StickyActionBar(
                primaryLabel = stringResource(R.string.web_add_save),
                onPrimary = { save() },
                primaryEnabled = saveEnabled,
                modifier = Modifier.imePadding(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    attempted = false
                },
                label = { Text(text = stringResource(R.string.web_add_label)) },
                singleLine = true,
                isError = showInvalid || duplicate,
                supportingText = {
                    Text(
                        text = when {
                            showInvalid -> stringResource(R.string.web_add_error)
                            duplicate -> stringResource(R.string.web_add_duplicate)
                            else -> stringResource(R.string.web_add_helper)
                        },
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (canSave) save() }),
                modifier = Modifier.fillMaxWidth(),
            )
            if (alreadyOnAdultList) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.web_add_adult_duplicate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (normalized != null && !duplicate && !alreadyOnAdultList) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.web_add_preview,
                        DomainName.display(normalized),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
