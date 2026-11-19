package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nordev.guardian.BuildConfig
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.GuardianCard
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.nav.ThemeMode
import com.nordev.guardian.ui.openPrivacyPolicy
import com.nordev.guardian.ui.theme.GuardianSizes
import com.nordev.guardian.ui.theme.GuardianSpacing

// Version 1 ships English with in-app summaries at Settings;
// uninstall protection and PIN entries build at Step 9 and are omitted here.
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onOpenRemoval: () -> Unit,
) {
    var showPrivacySummary by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { GuardianTopBar(title = stringResource(R.string.settings_title), onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GuardianSpacing.gutter),
        ) {
            Spacer(Modifier.height(GuardianSpacing.sm))
            SectionHeader(
                title = stringResource(R.string.settings_theme),
                modifier = Modifier.semantics { heading() },
            )
            GuardianCard {
                Column(modifier = Modifier.padding(vertical = GuardianSpacing.xs)) {
                    ThemeRadioRow(ThemeMode.System, R.string.theme_system, themeMode, onThemeChange)
                    ThemeRadioRow(ThemeMode.Light, R.string.theme_light, themeMode, onThemeChange)
                    ThemeRadioRow(ThemeMode.Dark, R.string.theme_dark, themeMode, onThemeChange)
                }
            }
            Spacer(Modifier.height(GuardianSpacing.xl))
            SectionHeader(
                title = stringResource(R.string.settings_privacy),
                modifier = Modifier.semantics { heading() },
            )
            GuardianCard {
                SettingRow(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.settings_privacy),
                    onClick = {
                        if (!openPrivacyPolicy(context)) showPrivacySummary = true
                    },
                )
            }
            Spacer(Modifier.height(GuardianSpacing.xl))
            SectionHeader(
                title = stringResource(R.string.settings_data),
                modifier = Modifier.semantics { heading() },
            )
            GuardianCard {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(GuardianSpacing.cardPadding),
                ) {
                    IconBadge(
                        icon = Icons.Outlined.Storage,
                        size = GuardianSizes.iconBadge,
                        iconSize = 22.dp,
                    )
                    Spacer(Modifier.width(GuardianSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_data_storage),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(GuardianSpacing.xs))
                        Text(
                            text = stringResource(R.string.settings_data_reinstall),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(GuardianSpacing.xl))
            GuardianCard(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenRemoval)
                        .heightIn(min = 64.dp)
                        .padding(GuardianSpacing.cardPadding),
                ) {
                    IconBadge(
                        icon = Icons.Outlined.Warning,
                        size = GuardianSizes.iconBadge,
                        iconSize = 22.dp,
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        halo = false,
                    )
                    Spacer(Modifier.width(GuardianSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_remove_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(GuardianSpacing.xxs))
                        Text(
                            text = stringResource(R.string.settings_remove_body),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.width(GuardianSpacing.xs))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            }
            Spacer(Modifier.height(GuardianSpacing.xl))
            SectionHeader(
                title = stringResource(R.string.settings_section_about),
                modifier = Modifier.semantics { heading() },
            )
            GuardianCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = GuardianSpacing.cardPadding),
                ) {
                    Text(
                        text = stringResource(R.string.settings_version),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(GuardianSpacing.sm))
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(GuardianSpacing.xl))
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
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(GuardianSpacing.cardPadding),
    ) {
        IconBadge(
            icon = icon,
            size = GuardianSizes.iconBadge,
            iconSize = 22.dp,
        )
        Spacer(Modifier.width(GuardianSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(GuardianSpacing.xs))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .padding(horizontal = GuardianSpacing.cardPadding),
    ) {
        RadioButton(
            selected = mode == selectedMode,
            onClick = null,
        )
        Spacer(Modifier.width(GuardianSpacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
