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

// The "Open settings" action on the problem card doubles as the empty state's
// action: Usage access is the only fix that produces screen time here.
@Composable
fun TodayScreen(
    onOpenSettings: () -> Unit,
) {
    Scaffold(topBar = { GuardianTopBar(title = stringResource(R.string.tab_today), onSettings = onOpenSettings) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            ProblemCard(
                severity = ProblemSeverity.Blocking,
                title = stringResource(R.string.today_problem_title),
                body = stringResource(R.string.today_problem_body),
                actionLabel = stringResource(R.string.action_open_settings),
                onAction = onOpenSettings,
            )
            Spacer(Modifier.height(32.dp))
            SectionHeader(title = stringResource(R.string.section_apps_today))
            EmptyState(
                icon = Icons.Outlined.Shield,
                title = stringResource(R.string.empty_today),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
