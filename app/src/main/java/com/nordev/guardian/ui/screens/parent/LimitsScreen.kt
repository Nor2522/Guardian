package com.nordev.guardian.ui.screens.parent

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.nordev.guardian.R
import com.nordev.guardian.data.usage.UsageRepository
import com.nordev.guardian.data.usage.UsageSnapshot
import com.nordev.guardian.ui.components.EmptyState
import com.nordev.guardian.ui.components.GuardianTopBar
import com.nordev.guardian.ui.components.SectionHeader

private const val MIN_LIMIT_MINUTES = 5
private const val MAX_LIMIT_MINUTES = 720
private const val DEFAULT_EDITOR_MINUTES = 15
private val presetMinutes = listOf(15, 30, 45, 60, 90, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(
    onOpenSettings: () -> Unit,
    limits: Map<String, Int>,
    onEditLimit: (String, Int) -> Unit,
    usage: UsageSnapshot? = null,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    var editingPackage by remember { mutableStateOf<String?>(null) }
    var editorMinutes by remember { mutableStateOf(DEFAULT_EDITOR_MINUTES) }
    val context = LocalContext.current
    val repository = remember { UsageRepository(context) }

    Scaffold(
        topBar = {
            GuardianTopBar(
                title = stringResource(R.string.tab_limits),
                onSettings = onOpenSettings,
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
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.limits_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = { pickerOpen = true },
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
            SectionHeader(
                title = stringResource(R.string.section_apps_limits),
                count = limits.keys.size.toString(),
            )
            if (limits.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.empty_limits_title),
                    body = stringResource(R.string.empty_limits_body),
                    actionLabel = stringResource(R.string.limits_choose),
                    onAction = { pickerOpen = true },
                )
            } else {
                limits.entries.sortedBy { it.key }.forEach { (packageName, limitMinutes) ->
                    LimitRow(
                        packageName = packageName,
                        limitMinutes = limitMinutes,
                        usedMillis = usage?.apps?.firstOrNull { it.packageName == packageName }?.millis ?: 0L,
                        onEdit = {
                            editingPackage = packageName
                            editorMinutes = limitMinutes
                        },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickerOpen) {
        AppPickerSheet(
            apps = repository.launchableAppLabels(),
            limits = limits,
            onSelect = { packageName, minutes ->
                editingPackage = packageName
                editorMinutes = minutes
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }

    val editingPackageValue = editingPackage
    if (editingPackageValue != null) {
        LimitEditor(
            packageName = editingPackageValue,
            initialMinutes = editorMinutes,
            onSave = { minutes ->
                onEditLimit(editingPackageValue, minutes)
                editingPackage = null
            },
            onDismiss = { editingPackage = null },
        )
    }
}

@Composable
private fun LimitRow(
    packageName: String,
    limitMinutes: Int,
    usedMillis: Long,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    val label = remember(packageName) { appLabel(context, packageName) ?: packageName }
    val usedMinutes = (usedMillis / 60_000L).toInt()
    val reached = usedMillis >= limitMinutes * 60_000L
    val fraction = (usedMillis / (limitMinutes * 60_000L).toFloat()).coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            val icon = remember(packageName) { appIcon(context, packageName) }
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.limits_used_of, usedMinutes, limitMinutes),
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(4.dp),
            )
        }
        if (reached) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = stringResource(R.string.limits_reached),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(
    apps: List<Pair<String, String>>,
    limits: Map<String, Int>,
    onSelect: (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val trimmedQuery = query.trim()
    val filtered = remember(apps, trimmedQuery) {
        apps
            .filter { it.second.contains(trimmedQuery, ignoreCase = true) }
            .sortedBy { it.second.lowercase() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.limits_choose),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.size(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(text = stringResource(R.string.limits_search)) },
            trailingIcon = {
                if (trimmedQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.cd_clear),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = { query = "" }),
                    )
                }
            },
        )
        Spacer(Modifier.size(8.dp))
        if (filtered.isEmpty()) {
            Text(
                text = stringResource(R.string.limits_no_match, trimmedQuery.ifEmpty { stringResource(R.string.limits_choose) }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            filtered.forEach { (packageName, label) ->
                val hasLimit = limits.containsKey(packageName)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (hasLimit) Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow) else Modifier)
                        .clickable { onSelect(packageName, limits[packageName] ?: DEFAULT_EDITOR_MINUTES) }
                        .padding(vertical = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Android,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LimitEditor(
    packageName: String,
    initialMinutes: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var minutes by remember { mutableStateOf(initialMinutes.coerceIn(MIN_LIMIT_MINUTES, MAX_LIMIT_MINUTES)) }
    val context = LocalContext.current
    val resolvedLabel = remember(packageName) { appLabel(context, packageName) ?: packageName }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.limits_editor_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = minutes.toString(),
                onValueChange = { text ->
                    text.toIntOrNull()?.let { minutes = it.coerceIn(MIN_LIMIT_MINUTES, MAX_LIMIT_MINUTES) }
                },
                label = { Text(text = stringResource(R.string.limits_editor_minutes)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text(text = stringResource(R.string.limits_editor_minutes_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.limits_editor_summary, resolvedLabel, minutes),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                presetMinutes.forEach { preset ->
                    FilterChip(
                        selected = minutes == preset,
                        onClick = { minutes = preset },
                        modifier = Modifier.height(32.dp),
                        label = { Text(text = stringResource(presetLabel(preset))) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(minutes) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.limits_editor_save))
            }
            TextButton(onClick = { onSave(0) }) {
                Text(text = stringResource(R.string.limits_editor_remove))
            }
        }
    }
}

private fun presetLabel(preset: Int): Int = when (preset) {
    15 -> R.string.limits_preset_15
    30 -> R.string.limits_preset_30
    45 -> R.string.limits_preset_45
    60 -> R.string.limits_preset_60
    90 -> R.string.limits_preset_90
    else -> R.string.limits_preset_120
}

private fun appLabel(context: Context, packageName: String): String? = runCatching {
    context.packageManager.getApplicationInfo(packageName, 0).loadLabel(context.packageManager).toString()
}.getOrNull()

private fun appIcon(context: Context, packageName: String): Bitmap? = runCatching {
    context.packageManager.getApplicationInfo(packageName, 0).loadIcon(context.packageManager).toBitmap(96, 96)
}.getOrNull()
