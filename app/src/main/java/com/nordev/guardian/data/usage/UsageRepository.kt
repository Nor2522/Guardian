package com.nordev.guardian.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.nordev.guardian.data.permissions.UsageAccessChecker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayUsage(val date: LocalDate, val millis: Long)

data class AppUsage(
    val packageName: String,
    val label: String,
    val millis: Long,
    val icon: Bitmap?,
)

data class UsageSnapshot(
    val accessGranted: Boolean,
    val todayMillis: Long,
    val weekMillis: Long,
    val days: List<DayUsage>,
    val apps: List<AppUsage>,
    // No monitoring service runs yet, so there is no ledger gap to fill.
    // Step 6 starts the service that can set this.
    val estimated: Boolean = false,
)

class UsageRepository(private val context: Context) {
    private var cachedApps: Map<String, AppEntry>? = null
    private val iconCache = mutableMapOf<String, Bitmap>()

    fun load(): UsageSnapshot {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val dates = UsageDays.lastSevenDays(today)
        val emptyDays = dates.map { DayUsage(it, 0L) }
        if (!UsageAccessChecker(context).isGranted()) {
            return UsageSnapshot(
                accessGranted = false,
                todayMillis = 0L,
                weekMillis = 0L,
                days = emptyDays,
                apps = emptyList(),
            )
        }

        val apps = launchableApps()
        val perDay: Map<LocalDate, MutableMap<String, Long>> =
            dates.associateWith { mutableMapOf() }
        val manager = context.getSystemService(UsageStatsManager::class.java)
        if (manager != null) {
            collectForegroundTime(manager, dates.first(), now, zone, perDay)
        }

        val days = dates.map { date ->
            val day = perDay.getValue(date).filterKeys { it in apps }
            DayUsage(date, day.values.sum())
        }
        val todayByPackage = perDay.getValue(today).filterKeys { it in apps }
        val appsToday = todayByPackage
            .mapNotNull { (packageName, millis) ->
                apps[packageName]?.let { entry ->
                    AppUsage(
                        packageName = packageName,
                        label = entry.label,
                        millis = millis,
                        icon = appIcon(packageName, entry.appInfo),
                    )
                }
            }
            .sortedWith(compareByDescending<AppUsage> { it.millis }.thenBy { it.label })

        return UsageSnapshot(
            accessGranted = true,
            todayMillis = todayByPackage.values.sum(),
            weekMillis = days.sumOf { it.millis },
            days = days,
            apps = appsToday,
        )
    }

    // queryEvents reports every activity, so track resumed activities per package.
    // A package stays foreground until its last resumed activity pauses; closing on
    // every ACTIVITY_PAUSED would drop time when an app moves between its own screens.
    // The query starts one day before the window so a span crossing midnight still
    // contributes its after-midnight part; earlier slices are simply not in perDay.
    private fun collectForegroundTime(
        manager: UsageStatsManager,
        windowStartDate: LocalDate,
        now: Long,
        zone: ZoneId,
        perDay: Map<LocalDate, MutableMap<String, Long>>,
    ) {
        val queryStart = windowStartDate.minusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val events = manager.queryEvents(queryStart, now) ?: return
        val event = UsageEvents.Event()
        val resumed = mutableMapOf<String, MutableSet<String>>()
        var currentPackage: String? = null
        var currentStart = 0L

        fun addInterval(packageName: String, startMillis: Long, endMillis: Long) {
            UsageDays.splitAcrossDays(startMillis, endMillis, zone).forEach { slice ->
                perDay[slice.date]?.let { day ->
                    day[packageName] = (day[packageName] ?: 0L) + slice.millis
                }
            }
        }

        fun close(atMillis: Long) {
            val packageName = currentPackage ?: return
            if (atMillis > currentStart) addInterval(packageName, currentStart, atMillis)
            currentPackage = null
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val className = event.className ?: packageName
                    resumed.getOrPut(packageName) { mutableSetOf() }.add(className)
                    if (currentPackage != packageName) {
                        close(event.timeStamp)
                        currentPackage = packageName
                        currentStart = event.timeStamp
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val className = event.className ?: packageName
                    resumed[packageName]?.remove(className)
                    if (currentPackage == packageName && resumed[packageName].isNullOrEmpty()) {
                        close(event.timeStamp)
                    }
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_SHOWN,
                UsageEvents.Event.DEVICE_SHUTDOWN -> close(event.timeStamp)
            }
        }
        close(now)
    }

    // Parent-facing list of launchable apps for the limits picker. Labels only;
    // icons load lazily on the screens that show them.
    fun launchableAppLabels(): List<Pair<String, String>> =
        launchableApps()
            .map { (packageName, entry) -> packageName to entry.label }
            .sortedBy { it.second.lowercase() }

    private fun launchableApps(): Map<String, AppEntry> {
        val packageManager = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val hidden = homePackages(packageManager) + SYSTEM_UI
        val apps = mutableMapOf<String, AppEntry>()
        packageManager.queryIntentActivities(launchIntent, 0).forEach { resolved ->
            val packageName = resolved.activityInfo.packageName
            if (packageName in hidden || packageName == ANDROID) return@forEach
            val appInfo = resolved.activityInfo.applicationInfo
            val label = appInfo.loadLabel(packageManager).toString()
            if (label.isNotBlank()) {
                apps.putIfAbsent(packageName, AppEntry(label, appInfo))
            }
        }
        cachedApps = apps
        return apps
    }

    // Icons cost far more than labels, so load them only for the apps that have
    // usage today and keep them for the next resume. This keeps the status total
    // from waiting on every installed app.
    private fun appIcon(packageName: String, appInfo: ApplicationInfo): Bitmap? {
        iconCache[packageName]?.let { return it }
        val icon = runCatching {
            appInfo.loadIcon(context.packageManager).toBitmap(96, 96)
        }.getOrNull()
        if (icon != null) iconCache[packageName] = icon
        return icon
    }

    private fun homePackages(packageManager: PackageManager): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(homeIntent, 0)
            .map { it.activityInfo.packageName }
            .toSet()
    }

    private data class AppEntry(val label: String, val appInfo: ApplicationInfo)

    private companion object {
        const val SYSTEM_UI = "com.android.systemui"
        const val ANDROID = "android"
    }
}
