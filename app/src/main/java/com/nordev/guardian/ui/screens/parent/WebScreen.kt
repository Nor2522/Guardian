package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.ProblemCard
import com.nordev.guardian.ui.components.ProblemSeverity
import com.nordev.guardian.ui.components.SectionHeader

// Nothing ships before Step 8, so the card reports the honest off state and
// the caveat card keeps the filter's limits visible instead of behind a sheet.
@Composable
fun WebScreen(
    onOpenSettings: () -> Unit,
) {
    Scaffold(topBar = { GuardianTopBar(title = stringResource(R.string.tab_web), onSettings = onOpenSettings) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            ProblemCard(
                severity = ProblemSeverity.Informational,
                title = stringResource(R.string.web_filter_off_title),
                body = stringResource(R.string.web_filter_off_body),
            )
            Spacer(Modifier.height(24.dp))
            ProblemCard(
                severity = ProblemSeverity.Informational,
                title = stringResource(R.string.web_help_title),
                body = stringResource(R.string.web_help_body),
            )
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = stringResource(R.string.section_blocked_sites))
            EmptyState(
                icon = Icons.Outlined.Shield,
                title = stringResource(R.string.empty_web),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
