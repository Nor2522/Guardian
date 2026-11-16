package com.nordev.guardian.ui.screens.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.UsageRepository
import com.nordev.guardian.domain.model.Schedule
import com.nordev.guardian.domain.model.ScheduleMode
import com.nordev.guardian.domain.schedule.ScheduleWindow
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianCard
import com.nordev.guardian.ui.components.GuardianPrimaryButton
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.IconBadge
import com.nordev.guardian.ui.components.SectionHeader
import com.nordev.guardian.ui.theme.GuardianSpacing
import java.time.DayOfWeek
import java.util.UUID

private const val DEFAULT_START_MINUTE = 22 * 60
private const val DEFAULT_END_MINUTE = 7 * 60
private const val MAX_SCHEDULE_NAME_LENGTH = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    onOpenSettings: () -> Unit,
    schedules: List<Schedule>,
    onSaveSchedule: (Schedule) -> Unit,
    onDeleteSchedule: (String) -> Unit,
) {
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Schedule?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { GuardianTopBar(title = stringResource(R.string.tab_schedules), onSettings = onOpenSettings) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.schedules_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            GuardianPrimaryButton(
                label = stringResource(R.string.schedules_add),
                onClick = { editing = null; editorOpen = true },
                icon = Icons.Outlined.Add,
            )
            Spacer(Modifier.height(32.dp))
            SectionHeader(
                title = stringResource(R.string.section_schedules),
                count = schedules.size.toString(),
            )
            if (schedules.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.empty_schedules_title),
                    body = stringResource(R.string.empty_schedules_body),
                    actionLabel = stringResource(R.string.schedules_add),
                    onAction = { editing = null; editorOpen = true },
                )
            } else {
                schedules.forEach { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        schedules = schedules,
                        onEdit = { editing = schedule; editorOpen = true },
                        onToggle = { onSaveSchedule(schedule.copy(enabled = !schedule.enabled)) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (editorOpen) {
        ScheduleEditor(
            initial = editing,
            onSave = { schedule ->
                onSaveSchedule(schedule)
                editorOpen = false
                editing = null
            },
            onDelete = { scheduleId ->
                onDeleteSchedule(scheduleId)
                editorOpen = false
                editing = null
            },
            onDismiss = { editorOpen = false; editing = null },
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    schedules: List<Schedule>,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
) {
    val chipColor = if (schedule.mode == ScheduleMode.PAUSE_ALL) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val chipContentColor = if (schedule.mode == ScheduleMode.PAUSE_ALL) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val overlap = schedules.any { other ->
        other.id != schedule.id && other.enabled && ScheduleWindow.overlaps(other, schedule)
    }

    GuardianCard(modifier = Modifier.padding(bottom = GuardianSpacing.sm)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(GuardianSpacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(
                    icon = Icons.Outlined.Shield,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(Modifier.width(GuardianSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = scheduleSummary(schedule),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(GuardianSpacing.sm))
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = { onToggle() },
                )
            }
            Spacer(Modifier.height(GuardianSpacing.md))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GuardianSpacing.sm),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = chipColor,
                    contentColor = chipContentColor,
                ) {
                    Text(
                        text = scheduleModeLabel(mode = schedule.mode),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                if (overlap) {
                    Text(
                        text = stringResource(R.string.schedules_overlaps, schedule.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleEditor(
    initial: Schedule?,
    onSave: (Schedule) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "Bedtime") }
    var days by remember { mutableStateOf(initial?.days ?: setOf(DayOfWeek.MONDAY)) }
    var startMinute by remember { mutableStateOf(initial?.startMinute ?: DEFAULT_START_MINUTE) }
    var endMinute by remember { mutableStateOf(initial?.endMinute ?: DEFAULT_END_MINUTE) }
    var mode by remember { mutableStateOf(initial?.mode ?: ScheduleMode.PAUSE_ALL) }
    var allowedApps by remember { mutableStateOf(initial?.allowedApps ?: emptySet<String>()) }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }
    var deleteArmed by remember { mutableStateOf(false) }
    var studyPickerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val repository = remember { UsageRepository(context) }
    val appLabels = remember { repository.launchableAppLabels().toMap() }

    val validationError = when {
        name.isBlank() || name.length > MAX_SCHEDULE_NAME_LENGTH -> stringResource(R.string.schedules_editor_error_name)
        days.isEmpty() -> stringResource(R.string.schedules_editor_error_days)
        endMinute == startMinute -> stringResource(R.string.schedules_editor_error_times)
        mode == ScheduleMode.STUDY && allowedApps.isEmpty() -> stringResource(R.string.schedules_editor_error_study)
        else -> null
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.schedules_editor_new_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.schedules_editor_name)) },
                singleLine = true,
                supportingText = { Text(text = stringResource(R.string.schedules_editor_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.schedules_editor_days),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            DayPicker(days = days) { day ->
                days = if (days.contains(day)) days - day else days + day
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = startMinute.toString(),
                    onValueChange = { text ->
                        text.toIntOrNull()?.let { parsed ->
                            if (parsed in 0..1439) startMinute = parsed
                        }
                    },
                    label = { Text(text = stringResource(R.string.schedules_editor_starts)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = endMinute.toString(),
                    onValueChange = { text ->
                        text.toIntOrNull()?.let { parsed ->
                            if (parsed in 0..1439) endMinute = parsed
                        }
                    },
                    label = { Text(text = stringResource(R.string.schedules_editor_ends)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = editorSummary(days, startMinute, endMinute),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = mode == ScheduleMode.PAUSE_ALL,
                    onClick = { mode = ScheduleMode.PAUSE_ALL },
                    label = { Text(text = stringResource(R.string.schedules_editor_mode_pause_all)) },
                )
                FilterChip(
                    selected = mode == ScheduleMode.STUDY,
                    onClick = { mode = ScheduleMode.STUDY },
                    label = { Text(text = stringResource(R.string.schedules_editor_mode_study)) },
                )
            }
            Spacer(Modifier.height(8.dp))
            if (mode == ScheduleMode.STUDY) {
                Text(
                    text = stringResource(R.string.schedules_editor_study_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                if (allowedApps.isEmpty()) {
                    Text(
                        text = stringResource(R.string.schedules_editor_study_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    allowedApps.sorted().forEach { packageName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Text(
                                text = appLabels[packageName] ?: packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { allowedApps = allowedApps - packageName },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = stringResource(R.string.cd_clear),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { studyPickerOpen = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.schedules_editor_study_add))
                }
                if (studyPickerOpen) {
                    AppPickerSheet(
                        apps = repository.launchableAppLabels(),
                        limits = emptyMap(),
                        onSelect = { packageName, _ ->
                            allowedApps = allowedApps + packageName
                            studyPickerOpen = false
                        },
                        onDismiss = { studyPickerOpen = false },
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.schedules_editor_enabled))
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                )
            }
            if (validationError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = validationError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (validationError == null) {
                        val schedule = initial?.copy(
                            name = name.trim(),
                            days = days,
                            startMinute = startMinute,
                            endMinute = endMinute,
                            mode = mode,
                            allowedApps = allowedApps,
                            enabled = enabled,
                        ) ?: Schedule(
                            id = "sched-${UUID.randomUUID()}",
                            name = name.trim(),
                            days = days,
                            startMinute = startMinute,
                            endMinute = endMinute,
                            mode = mode,
                            allowedApps = allowedApps,
                            enabled = enabled,
                        )
                        onSave(schedule)
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.schedules_editor_save))
            }
            if (initial != null) {
                TextButton(onClick = { deleteArmed = true }) {
                    Text(
                        text = stringResource(R.string.schedules_editor_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (deleteArmed && initial != null) {
        AlertDialog(
            onDismissRequest = { deleteArmed = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteArmed = false
                        onDelete(initial.id)
                    },
                ) {
                    Text(text = stringResource(R.string.schedules_editor_delete_confirm))
                }
            },
            title = { Text(text = stringResource(R.string.schedules_editor_delete_title)) },
            text = { Text(text = stringResource(R.string.schedules_editor_delete_body, initial.name)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayPicker(
    days: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = days.contains(day),
                onClick = { onToggle(day) },
                label = { Text(text = dayShortLabel(day)) },
            )
        }
    }
}

private fun editorSummary(days: Set<DayOfWeek>, startMinute: Int, endMinute: Int): String {
    val dayLabels = DayOfWeek.values().filter { days.contains(it) }.joinToString(", ") { dayFullName(it) }
    val endLabel = "%02d:%02d".format(endMinute / 60, endMinute % 60)
    return if (endMinute > startMinute) {
        "$dayLabels ${formatMinute(startMinute)} to $endLabel"
    } else {
        "$dayLabels ${formatMinute(startMinute)} to $endLabel, ends the next day"
    }
}

private fun scheduleSummary(schedule: Schedule): String = editorSummary(schedule.days, schedule.startMinute, schedule.endMinute)

private fun formatMinute(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

private fun dayShortLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Mo"
    DayOfWeek.TUESDAY -> "Tu"
    DayOfWeek.WEDNESDAY -> "We"
    DayOfWeek.THURSDAY -> "Th"
    DayOfWeek.FRIDAY -> "Fr"
    DayOfWeek.SATURDAY -> "Sa"
    DayOfWeek.SUNDAY -> "Su"
}

private fun dayFullName(day: DayOfWeek): String = day.name.lowercase().replaceFirstChar { it.uppercaseChar() }

@Composable
private fun scheduleModeLabel(mode: ScheduleMode): String =
    if (mode == ScheduleMode.PAUSE_ALL) {
        stringResource(R.string.schedules_editor_mode_pause_all)
    } else {
        stringResource(R.string.schedules_editor_mode_study)
    }
