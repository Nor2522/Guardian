package com.nordev.guardian.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nordev.guardian.ui.theme.GuardianTheme

// Previews may use literals for copy that has no resource yet (row state text).
@Preview(showBackground = true)
@Composable
private fun GuardianTopBarLightPreview() {
    GuardianTheme {
        GuardianTopBar(title = "Today", onBack = null, onSettings = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GuardianTopBarDarkPreview() {
    GuardianTheme {
        GuardianTopBar(title = "Settings", onBack = {}, onSettings = null)
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun GuardianTopBarFontScalePreview() {
    GuardianTheme {
        GuardianTopBar(title = "Schedules, quiet weeks, and long names", onBack = {}, onSettings = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusHeroHealthyLightPreview() {
    GuardianTheme {
        StatusHero(
            state = StatusHeroState.Healthy,
            title = "Guardian is on",
            body = "Protection is running for the checks you turned on.",
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusHeroAttentionDarkPreview() {
    GuardianTheme {
        StatusHero(
            state = StatusHeroState.Attention,
            title = "Protection needs attention",
            body = "2 checks need attention. Tap Review to fix them.",
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun StatusHeroSetupFontScalePreview() {
    GuardianTheme {
        StatusHero(
            state = StatusHeroState.Setup,
            title = "Finish setting up Guardian",
            body = "3 checks are left before protection is complete.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricCardLightPreview() {
    GuardianTheme {
        MetricCard(
            label = "Screen time today",
            value = "None today",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MetricCardChipDarkPreview() {
    GuardianTheme {
        MetricCard(
            label = "Screen time today",
            value = "2 h 15 min",
            chipLabel = "Estimated",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun MetricCardFontScalePreview() {
    GuardianTheme {
        MetricCard(
            label = "Screen time today",
            value = "1 h 20 min",
            chipLabel = "Estimated",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProtectionRowOnLightPreview() {
    GuardianTheme {
        ProtectionRow(
            icon = Icons.Outlined.Timelapse,
            title = "App limits and schedules",
            stateText = "On",
            state = ProtectionState.On,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProtectionRowOffDarkPreview() {
    GuardianTheme {
        ProtectionRow(
            icon = Icons.Outlined.Shield,
            title = "Uninstall protection",
            stateText = "Off",
            state = ProtectionState.Off,
            reason = "Guardian can be removed without the PIN.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun ProtectionRowAttentionFontScalePreview() {
    GuardianTheme {
        ProtectionRow(
            icon = Icons.Outlined.Timelapse,
            title = "App limits and schedules",
            stateText = "Needs attention",
            state = ProtectionState.Attention,
            reason = "Usage access is missing, so limits are not enforced.",
            actionLabel = "Review",
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProblemCardBlockingLightPreview() {
    GuardianTheme {
        ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = "Screen time is paused",
            body = "Guardian cannot read app time without Usage access. Daily limits are not being enforced.",
            actionLabel = "Open settings",
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProblemCardInformationalDarkPreview() {
    GuardianTheme {
        ProblemCard(
            severity = ProblemSeverity.Informational,
            title = "Website filter is off",
            body = "Adult sites and your blocked sites can load. App limits and schedules keep working.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun ProblemCardFontScalePreview() {
    GuardianTheme {
        ProblemCard(
            severity = ProblemSeverity.Blocking,
            title = "App limits are not working",
            body = "Guardian cannot notice which app is open. Limits, schedules, and the pause screen need Accessibility.",
            actionLabel = "Turn on",
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateLightPreview() {
    GuardianTheme {
        EmptyState(
            icon = Icons.Outlined.Shield,
            title = "No app limits yet",
            body = "Choose an app and set a daily limit. Limits reset at midnight.",
            actionLabel = "Choose an app",
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyStateTitleOnlyDarkPreview() {
    GuardianTheme {
        EmptyState(
            icon = Icons.Outlined.Shield,
            title = "No screen time recorded yet today.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun EmptyStateFontScalePreview() {
    GuardianTheme {
        EmptyState(
            icon = Icons.Outlined.Shield,
            title = "No schedules yet",
            body = "Add bedtime or study hours. Apps pause automatically during a schedule.",
            actionLabel = "Add schedule",
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinFieldLightPreview() {
    GuardianTheme {
        PinField(
            value = "",
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinFieldErrorDarkPreview() {
    GuardianTheme {
        PinField(
            value = "321",
            onValueChange = {},
            isError = true,
            errorMessage = "That PIN is not correct. Try again.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun PinFieldFontScalePreview() {
    GuardianTheme {
        PinField(
            value = "445",
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderLightPreview() {
    GuardianTheme {
        SectionHeader(
            title = "Apps today",
            count = "3",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StickyActionBarLightPreview() {
    GuardianTheme {
        StickyActionBar(
            primaryLabel = "Get started",
            onPrimary = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StickyActionBarSecondaryDarkPreview() {
    GuardianTheme {
        StickyActionBar(
            primaryLabel = "Finish setup",
            onPrimary = {},
            secondaryLabel = "Open status",
            onSecondary = {},
        )
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun StickyActionBarFontScalePreview() {
    GuardianTheme {
        StickyActionBar(
            primaryLabel = "I understand what this permission does",
            onPrimary = {},
            secondaryLabel = "Not now",
            onSecondary = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuardianCardLightPreview() {
    GuardianTheme {
        GuardianCard(modifier = Modifier.padding(16.dp)) {
            IconBadge(icon = Icons.Outlined.Shield)
            Spacer(Modifier.height(12.dp))
            StatusPill(text = "Protection on", tone = GuardianTone.Success)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GuardianCardDarkPreview() {
    GuardianTheme {
        GuardianCard(modifier = Modifier.padding(16.dp)) {
            IconBadge(icon = Icons.Outlined.Public, size = 56.dp)
            Spacer(Modifier.height(12.dp))
            StatusPill(text = "Needs attention", tone = GuardianTone.Attention)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GuardianBackgroundLightPreview() {
    GuardianTheme {
        GuardianBackground {
            Column(modifier = Modifier.padding(24.dp)) {
                IconBadge(icon = Icons.Outlined.Timelapse)
                Spacer(Modifier.height(16.dp))
                GuardianPrimaryButton(label = "Get started", onClick = {})
                Spacer(Modifier.height(16.dp))
                GuardianProgressBar(progress = 0.62f)
                Spacer(Modifier.height(16.dp))
                StepProgress(step = 3)
            }
        }
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun GuardianPrimaryButtonFontScalePreview() {
    GuardianTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GuardianPrimaryButton(label = "I understand what this permission does", onClick = {})
            Spacer(Modifier.height(12.dp))
            GuardianPrimaryButton(label = "Saving", onClick = {}, loading = true)
            Spacer(Modifier.height(12.dp))
            GuardianPrimaryButton(label = "Turn off protection", onClick = {}, destructive = true)
        }
    }
}
