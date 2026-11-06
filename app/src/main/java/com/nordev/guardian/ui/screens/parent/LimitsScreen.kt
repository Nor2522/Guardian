package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.SectionHeader

@Composable
fun LimitsScreen(
    onOpenSettings: () -> Unit,
) {
    Scaffold(topBar = { GuardianTopBar(title = stringResource(R.string.tab_limits), onSettings = onOpenSettings) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.limits_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = {
                    // The app picker arrives at Step 6.
                },
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.limits_choose),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = stringResource(R.string.section_apps_limits))
            EmptyState(
                icon = Icons.Outlined.Shield,
                title = stringResource(R.string.empty_limits_title),
                body = stringResource(R.string.empty_limits_body),
                actionLabel = stringResource(R.string.limits_choose),
                onAction = {
                    // The app picker arrives at Step 6.
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
