package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.nav.ThemeMode

// Version 1 ships English with in-app summaries at Settings;
// uninstall protection and PIN entries build at Step 9 and are omitted here.
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    var showPrivacySummary by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = { GuardianTopBar(title = stringResource(R.string.settings_title), onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            SectionHeader(
                title = stringResource(R.string.settings_theme),
                modifier = Modifier.semantics { heading() },
            )
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ThemeRadioRow(ThemeMode.System, R.string.theme_system, themeMode, onThemeChange)
                    ThemeRadioRow(ThemeMode.Light, R.string.theme_light, themeMode, onThemeChange)
                    ThemeRadioRow(ThemeMode.Dark, R.string.theme_dark, themeMode, onThemeChange)
                }
            }
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = stringResource(R.string.settings_privacy),
                modifier = Modifier.semantics { heading() },
            )
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacySummary = true }
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_privacy),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = stringResource(R.string.settings_data),
                modifier = Modifier.semantics { heading() },
            )
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.settings_data_storage),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_data_reinstall),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = stringResource(R.string.settings_section_about),
                modifier = Modifier.semantics { heading() },
            )
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_version_value),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (showPrivacySummary) {
        AlertDialog(
            onDismissRequest = { showPrivacySummary = false },
            title = { Text(text = stringResource(R.string.privacy_summary_title)) },
            text = { Text(text = stringResource(R.string.privacy_summary_body)) },
            confirmButton = {
                TextButton(onClick = { showPrivacySummary = false }) {
                    Text(text = stringResource(R.string.dialog_close))
                }
            },
        )
    }
}

@Composable
private fun ThemeRadioRow(
    mode: ThemeMode,
    labelRes: Int,
    selectedMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    val label = stringResource(labelRes)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = mode == selectedMode,
                role = Role.RadioButton,
                onClick = { onThemeChange(mode) },
            )
            .semantics { contentDescription = label }
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
    ) {
        RadioButton(
            selected = mode == selectedMode,
            onClick = null,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
        )
    }
}
