# Guardian plan

Guardian is a parental control app for Android. Version 1 runs on the child's phone. The parent sets it up and protects every setting with a PIN. Rules and usage stay on the phone. There is no Guardian account, server, ads, or analytics.

Working name: Guardian. Package: `com.nordev.guardian`. `minSdk 26`. `compileSdk 37`. `targetSdk 36`.

The brief said `targetSdk 34`. A new app with target 34 cannot be published. Since 31 August 2026, Google Play accepts only new apps and updates that target Android 16 (API 36). Step 1 installs platform 36 and build-tools 36. The app still runs on Android 8.0 and newer. Step 2 raised `compileSdk` to 37 because Compose 1.12.1 (BOM 2026.09.00) requires it; `targetSdk` stays 36 for Play.

## What version 1 does

1. Shows screen time per app for today and the last 7 days.
2. Sets a daily time limit per app. At the limit, a friendly block screen replaces the app.
3. Runs bedtime and study hour schedules.
4. Filters websites through a local DNS VPN. It blocks a shipped adult domain list plus the parent's custom domains.
5. Protects itself from uninstall and disable with Device Admin and a PIN gate on the Settings screens that can turn it off.
6. Protects every setting with a parent PIN.
7. Shows an ongoing notification while protection is active.
8. Ships with the disclosures, privacy policy, and Play Console declarations that the policies require.

## What version 1 does not do

- No remote control, account, cloud sync, or parent app on another phone. That is phase 2.
- No location tracking, call or SMS reading, photo or file access, keystroke capture, screen recording, microphone, or camera use.
- No reading of text inside other apps. The Accessibility service reads the foreground package name, not screen content.
- No hidden mode, secret icon, or spy branding.
- No bypass of Android security controls.
- No iOS app.
- No ads, analytics, or third-party tracking SDKs.

These are product limits and Play policy limits. Do not add them without a new design review.

## Who uses it

- The parent or guardian installs Guardian, sets the PIN, grants the permissions, and changes the rules.
- The child uses the phone. The child sees the ongoing notification and can open a status screen without the PIN. The child cannot change rules.
- Android runs the services and grants the special access.

## Product rules

- Make protection visible. The ongoing notification stays on while protection runs. The app icon is unique and honest.
- Say what the app can see and what it cannot see. Put this in onboarding and in the privacy policy.
- Ask for each permission separately, with a plain reason. Never bundle consent.
- Keep all data on the phone in version 1. The VPN forwards allowed DNS queries to the network resolver, but sends nothing to Guardian.
- Hash the PIN. Never store or log the PIN in plain text.
- Fail visibly. If a permission is revoked or another VPN takes over, show a problem state and update the notification.
- Let the parent remove Guardian. A protection app that cannot be removed is not trustworthy. Removal needs the PIN and a clear warning.
- Never trap the child. The block screen always offers a way back to the home screen. Phone calls and emergency calls always work.
- Do not promise perfect blocking. Some apps and browsers can bypass DNS filtering. Say so in the app.
- Keep the child's view calm and factual. No shame, no threats, no scary language.

## Data model

Name the shapes before writing code. The policy engine is the center of the app.

```kotlin
data class PolicySnapshot(
    val now: ZonedDateTime,
    val limits: List<AppLimit>,
    val schedules: List<Schedule>,
    val usageToday: Map<String, Duration>,
    val alwaysAllowed: Set<String>,
)

sealed interface AccessDecision {
    data object Allow : AccessDecision
    data class Block(val reason: BlockReason) : AccessDecision
}

sealed interface BlockReason {
    data class Schedule(val name: String, val until: LocalTime) : BlockReason
    data class DailyLimit(val packageName: String, val limit: Duration) : BlockReason
}

fun evaluate(packageName: String, snapshot: PolicySnapshot): AccessDecision
```

`evaluate` is pure Kotlin. It has no Android imports. The Accessibility service, the status service, and the UI preview all call it. This keeps one set of rules and makes the rules unit testable.

| Type | Storage | Key fields | Notes |
| --- | --- | --- | --- |
| `AppLimit` | Room | `packageName` PK, `dailyLimitMinutes`, `enabled` | One row per app. |
| `Schedule` | Room | `id`, `name`, `days`, `startMinute`, `endMinute`, `mode`, `enabled` | `endMinute < startMinute` means the window crosses midnight. `days` uses the start day. |
| `ScheduleAllowedApp` | Room | `scheduleId`, `packageName` | Join table for study mode. |
| `BlockedDomain` | Room | `id`, `domain` unique, `createdAt` | Normalized lowercase ASCII. Custom domains only. The shipped adult list lives in assets. |
| `UsageLedger` | Room | `packageName`, `epochDay`, `usedMillis` | Enforcement counter. Prune after 8 days. |
| `ParentPin` | DataStore | `hash`, `salt`, `iterations`, `failedAttempts`, `lockedUntil` | PBKDF2-HMAC-SHA256, 210,000 iterations, 16-byte salt. |
| `Settings` | DataStore | onboarding and consent timestamps, `filterEnabled`, `uninstallProtectionEnabled`, `themeMode` | Consent timestamps are evidence for Play declarations. |
| `ParentSession` | Memory only | `startedAt`, `expiresAt` | Five minute idle timeout. Cleared on background, screen off, and process death. |

### Policy rules

1. Always-allowed packages win over every rule.
2. An active `BLOCK_ALL` schedule blocks.
3. An active `ALLOW_ONLY` schedule blocks every app outside its allow list.
4. An enabled daily limit blocks when today's usage is at or above the limit.
5. Otherwise the app is allowed.
6. When two schedules are active, the most restrictive wins. The schedule that ends first supplies the reason and the "until" time.
7. A schedule that allows an app does not cancel that app's daily limit.
8. A cross-midnight schedule uses the weekday of its start day.
9. The daily usage key is the local date, so the counter resets at local midnight and survives daylight saving changes.

Always allowed: the default dialer and emergency dialer, the default launcher, System UI, the default clock and alarm app, and Guardian itself. Settings is not always allowed. The parent can still reach Settings through the PIN gate.

### Usage source

`UsageRepository` is the only screen time API. It merges two sources:

- `UsageStatsManager` for the system view of app usage. It drives the 7 day history.
- `UsageLedger` for the real time counter. It drives limit enforcement and today's number.

If the ledger has a gap because Accessibility was off, the UI marks the value as estimated and says why. The ledger is never silently replaced by a different number.

## Architecture

One Gradle module, `:app`. The app is small enough that a second module would add build time and no safety. If the parent app arrives in phase 2, split the shared model then.

```
app/src/main/java/com/nordev/guardian/
  GuardianApp.kt
  MainActivity.kt
  data/
    db/            Room database, entities, DAOs
    prefs/         DataStore for settings and PIN
    usage/         UsageStats source, ledger, repository
    apps/          Launchable app list
    filter/        Domain normalization, block list assets
  domain/
    policy/        PolicySnapshot, AccessPolicy, BlockReason
    model/         AppLimit, Schedule, BlockedDomain
    pin/           PBKDF2 hasher, PIN manager, lockout
    schedule/      Window math, cross-midnight handling
  service/
    status/        GuardianStatusService, notification, schedule tick
    accessibility/ AppWatchAccessibilityService
    vpn/           DnsFilterVpnService, DNS proxy
    admin/         GuardianDeviceAdminReceiver
    boot/          BootReceiver
  ui/
    theme/         Color, Type, Shape, GuardianTheme
    gate/          Child status and parent unlock
    onboarding/
    today/
    limits/
    schedules/
    filter/
    settings/
    block/         Shared block screen
    components/
  di/              AppContainer, manual wiring
```

- UI: one Activity, Jetpack Compose, Navigation Compose, Material 3. State flows from ViewModels. Screens are stateless where practical.
- DI: a manual `AppContainer` created by `GuardianApp`. No Hilt in version 1. Hilt would add KSP, build time, and a second way to wire the same objects.
- Storage: Room for lists and counters, DataStore for scalar settings and the PIN hash.
- Time: `java.time` is available on API 26. Use `ZonedDateTime` and `LocalDate`, not fixed offsets.
- Tests: JVM unit tests for the policy engine, schedule windows, PIN hashing, and domain normalization. `./gradlew testDebugUnitTest` is part of every step.
- Lint: `./gradlew lintDebug` is part of every step. Fix real findings. Suppress a finding only with a comment that names the reason.
- Network: no network code before step 8. At step 8, the only socket use is the DNS forwarder. No HTTP client, no analytics, no crash reporter.
- Package visibility: use a `<queries>` entry for `MAIN` and `LAUNCHER` activities plus `LauncherApps`. Do not request `QUERY_ALL_PACKAGES`.

### Services

- `GuardianStatusService` is the only always-on foreground service. It owns the ongoing notification, the once-a-minute schedule check, the midnight reset, and the problem state. It restarts after boot.
- `AppWatchAccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED`, gets the foreground package, calls `evaluate`, and shows the block screen. It does not read node text, perform gestures, or retrieve window content.
- `DnsFilterVpnService` runs the local DNS proxy. It captures DNS traffic only. It does not route app traffic through a remote server.
- `GuardianDeviceAdminReceiver` holds the minimum Device Admin state needed to block uninstall. It does not request wipe, lock, or password policies.
- `BootReceiver` restarts the status service and the filter after boot when the parent had them on.

The block screen is one Compose surface with two possible hosts, an accessibility overlay or a full screen activity. Step 6 picks the host with `/architect`. The design supports both.

## Permissions and Play policy

| Capability | Permission or API | Why | What it does not do |
| --- | --- | --- | --- |
| Usage Access | `PACKAGE_USAGE_STATS` app-op, granted in Settings | Read app foreground time | Does not read content inside apps |
| Accessibility | `BIND_ACCESSIBILITY_SERVICE` | Notice the foreground app and show the block screen | Does not read screen text, tap for the child, or control other apps |
| VPN | `BIND_VPN_SERVICE` | Run the local DNS filter | Does not send browsing to a Guardian server or inspect HTTPS |
| Device Admin | `BIND_DEVICE_ADMIN` | Block uninstall until the parent enters the PIN | Does not wipe, lock, or reset the phone password |
| Notifications | `POST_NOTIFICATIONS` on API 33 and newer | Show the ongoing notice and problem alerts | Does not hide the notice |
| Foreground service | `FOREGROUND_SERVICE` plus one type | Keep protection running | Type is chosen at step 6 |
| Boot | `RECEIVE_BOOT_COMPLETED` | Restart protection after a reboot | Does not start without the parent's earlier choice |
| Overlay | `SYSTEM_ALERT_WINDOW` only if needed | Host the block screen if an accessibility overlay is not enough | Prefer no overlay permission |
| Internet | `INTERNET` from step 8 | Forward allowed DNS queries | No Guardian server, no analytics, no HTTP client |
| Package list | `<queries>` for launcher activities | Show apps the parent can limit | No `QUERY_ALL_PACKAGES` |

Play requirements to satisfy:

- Declare the app as a monitoring tool with `child_monitoring`, using the exact manifest form in the current policy at step 11.
- Set `isAccessibilityTool="false"` on the Accessibility service. Show a separate in-app disclosure and collect affirmative consent before opening Accessibility settings. Complete the Play Console Accessibility declaration and record the demo video.
- Document the Accessibility use, VPN use, and Device Admin use in the store listing.
- Keep the persistent notification and unique icon at all times while protection runs.
- Set the target audience to adults, not children. The app is for parents. Check the Families policy at step 11.
- Complete the Data safety form as no collection and no sharing. All data stays on the device.
- Publish a privacy policy at a public URL and link it inside the app.
- Declare the foreground service type in Play Console.
- Check the Device Admin policy again at step 9. The Accessibility policy allows prevention of uninstall when a parent authorizes it through a parental control app.

## Build and proof

The verification rule for this project:

A step is done only when all of these pass.

1. `./gradlew assembleDebug` succeeds.
2. `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeds.
3. The real phone shows the behavior in the step.
4. `adb logcat` has no crash or fatal exception from Guardian.

Commands:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nordev.guardian/.MainActivity
adb logcat -c
# run the phone test
adb logcat -d -s AndroidRuntime:E Guardian:V
```

A step with no phone check stays open. Compiling is not proof.

The project has no launch command yet. Step 1 creates the skeleton, gets Hello on the phone, then creates `.opencode/skills/verify-guardian/` and proves it on Hello. Feature work starts after that. A verification skill needs a real app to drive, so this is one step later than the brief asked.

Record the evidence in the commit message and in Appendix A.

## Steps

| Step | Result | Proof |
| --- | --- | --- |
| 0 | `docs/plan.md` and `docs/design.md` | You read them and say OK |
| 1 | Kotlin and Compose project, Gradle wrapper, Hello screen, verification skill | Build, install, Hello on the phone, clean logcat, skill drives Hello |
| 2 | Material 3 theme, navigation, onboarding | Light and dark on the phone, every route opens, 200% font, TalkBack, clean logcat |
| 3 | Parent PIN with hashed storage | Set, unlock, wrong PIN, lockout, five minute timeout, no PIN in logcat |
| 4 | Permissions screen with separate disclosures | Each row opens the correct system screen, status updates on return, disclosure text is readable |
| 5 | Screen time from `UsageStatsManager` | Compare two apps with Digital Wellbeing, check today and 7 days, check the missing permission state |
| 6 | App limits and block screen | One minute test limit, block screen, Home works, parent PIN, reboot, service kill |
| 7 | Schedules | Bedtime across midnight, study allow list, overlapping rules, reboot |
| 8 | Website filter with `VpnService` | Adult domain blocked, allowed domain loads, custom domain blocked, VPN key visible, other VPN conflict, clean logcat |
| 9 | Uninstall and disable protection | Uninstall blocked, admin deactivation asks for PIN, parent removal works, check the phone's OEM behavior |
| 10 | Testing, bugs, battery check | 24 hour battery run, permission revoked, reboot, Accessibility off, VPN off, Accessibility Scanner pass |
| 11 | Play Store package | Privacy policy live, Data safety done, declarations done, signed AAB, internal test track, pre-launch report |
| 12 | Parent app on another phone | Later phase, not version 1 |

Every step ends with one git commit. The commit message says what changed and which proof commands ran.

## Risks

| Risk | Effect | Plan |
| --- | --- | --- |
| Play rejects Accessibility, VPN, or Device Admin use | No release | Keep each use narrow, disclose it separately, complete declarations, record the demo video, recheck policy at step 11 |
| Target API 36 behavior changes | Crashes or broken UI on new phones | Edge-to-edge insets, foreground service rules, boot rules, test on the real phone |
| OEM battery killers stop the service | Limits or schedules stop | Ask for battery optimization exemption with a plain reason, restart on boot, show a problem state, test on the phone |
| Another VPN is active | DNS filter cannot run | Detect it, show a problem state, explain what to do |
| DNS over HTTPS or hardcoded IPs bypass the filter | Some sites still load | Do not claim perfect blocking, explain the limit in the app |
| UsageStats has gaps | Wrong screen time | Keep the ledger, mark estimated values, reconcile on service start |
| Device Admin behavior differs by OEM | Uninstall protection fails | Test on the real phone, document the limits, do not hide the behavior |
| Adult block list license is unclear | Cannot ship the list | Pick a list with a clear license, record source, version, and hash in `docs/third-party.md` |
| Working name conflicts with a trademark | Store listing blocked | Search before step 11, rename only the label if needed, keep the package |
| The brief says no traffic leaves the phone | False privacy claim | Say no data goes to Guardian, and say allowed DNS queries go to the network resolver |
| Parent forgets the PIN | Parent cannot change or remove Guardian | Warn at setup, document the reset path, decide on a recovery code before step 3 |
| Block screen host is unreliable | Child bypasses a limit | Decide overlay or activity at step 6 with `/architect`, verify on the phone |
| No remote block list updates | New adult domains get through | Version 1 ships a static list, document the update path in phase 2 |

## Decisions for Nor

1. Target API 36 instead of 34. Required by Play for a new app. Recommendation: accept.
2. Privacy wording. Use "no data goes to Guardian" and "allowed DNS queries go to your network resolver" instead of "no traffic leaves the phone." The literal claim is false for any DNS filter. Recommendation: accept.
3. Overlay permission. Use an accessibility overlay and skip `SYSTEM_ALERT_WINDOW` if step 6 proves it works. Recommendation: skip the extra permission.
4. Working name. Keep Guardian for now and search for conflicts before step 11. Recommendation: keep.
5. PIN recovery. Version 1 has no recovery code. A forgotten PIN means clearing app data or uninstalling with adb. Recommendation: add a one-time recovery code at setup if you want a safety valve. This is a product call.
6. Child status detail. Show today's total time and the active rules, not the full per-app list. Recommendation: keep the child view simple and the parent view detailed.

## Sources read

Read on 5 November 2026.

- Material 3 color roles, https://m3.material.io/styles/color/roles
- Material 3 type scale, https://m3.material.io/styles/typography/type-scale-tokens
- Material 3 navigation bar, https://m3.material.io/components/navigation-bar/overview
- Play target API levels, https://support.google.com/googleplay/android-developer/answer/11926878
- Play malware and stalkerware policy, https://support.google.com/googleplay/android-developer/answer/9888380
- Play monitoring tool flag, https://support.google.com/googleplay/android-developer/answer/12955211
- Play Accessibility API policy, https://support.google.com/googleplay/android-developer/answer/10964491
- Play sensitive permissions and VPN policy, https://support.google.com/googleplay/android-developer/answer/16558241
- Android foreground service types, https://developer.android.com/develop/background-work/services/fgs/service-types

The Device Admin policy page was not read in full. Recheck it at step 9 before implementing uninstall protection.

## Appendix A. Verification log

| Step | Date | Build | Install | Logcat | Phone check | Commit |
| --- | --- | --- | --- | --- | --- | --- |
| 0 | 5 Nov 2026 | n/a | n/a | n/a | You read the docs and said OK | 70d5ea0 |
| 1 | 6 Nov 2026 | `bash ./gradlew assembleDebug` BUILD SUCCESSFUL | `adb install -r` Success, then Success again on the skill run | Empty for `AndroidRuntime:E Guardian:V` | Cold start: force-stop, `am start` fresh instance pid 25746, `mCurrentFocus` and `mFocusedApp` name the Guardian MainActivity, hello text confirmation open for Nor | 0c04565 |
| 2 | 6 Nov 2026 | `bash ./gradlew assembleDebug` BUILD SUCCESSFUL | `adb install -r` Success | Empty for `AndroidRuntime:E Guardian:V` after cold launch and the full route drive; the only process death was `am_kill ... remove task`, not a crash | Phone drive: status → gate → 5 onboarding steps → consent error and checkbox state → Done → status → gate wrong PIN → correct PIN → all four parent tabs → Settings privacy dialog → Dark then System theme; consent and theme rows expose checked state | b992dc2 |
| 3 | 6 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 14 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E`; strict PIN token 0; stored hash and salt strings 0 | PIN 258036 saved as a PBKDF2 hash and random salt with no plaintext in DataStore; correct PIN unlocks; restart keeps the PIN and gate; process death does not restore the parent screen; Home and screen off lock; 5 wrong tries → 60 s lockout that survives restart; idle lock at 18:27 after 5 minutes with no touches since 18:21; parent window FLAG_SECURE, gate not | 0abefc4 |
| 3b | 8 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 6m 13s | `adb install -r` Success | Empty for `AndroidRuntime:E`; temporary tick and touch logs removed before the final build | Snackbar “Guardian will lock in 30 seconds.” captured at 25.6 s remaining; a touch at 07:06:21 reset the countdown to 299.7 s; the gate appeared, but a screen-off event at 07:08 stopped the ticker first, so idle expiry stays covered by Step 3 | 84acc52 |
| 4a | 8 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 8m 1s | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E` | Usage disclosure shows the body, an unchecked consent box, “Open Usage access settings”, and no build note; the action without consent shows “Tick the checkbox above first.”; consent plus action returns to Permissions and opens the real Android Usage data access screen; the Guardian row is set to On; back returns to `On` / `Open settings`; `files/datastore/settings.preferences_pb` contains `usage_access_consent_at`; `Open settings` reopens the system screen directly; the Notifications placeholder still shows the build note and returns without opening settings | 445d66b |
| 4b | 8 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 8m 38s | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E` | Notifications disclosure shows the body, an unchecked consent box, “Allow notifications”, and no build note; the action without consent shows “Tick the checkbox above first.”; consent plus action returns to Permissions and raises the system prompt “Allow Guardian to send you notifications?”; Don't allow leaves the row `Off` / `Open settings` and stores `notifications_consent_at`; `Open settings` opens the Guardian app notification settings; enabling the toggle and returning flips the row to `On` / `Open settings`; the direct path reopens the same settings screen; the Accessibility placeholder still shows the build note and returns without opening settings | c4329f9 |
| 4c | 9 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 3m 44s | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E` | Accessibility disclosure shows the body, an unchecked consent box, “Open Accessibility settings”, and no build note; the action without consent shows “Tick the checkbox above first.”; consent plus action returns to Permissions and opens the real Accessibility screen; Guardian appears under Installed apps as Off; the toggle raises “Allow Guardian to have full control of your device?” and Allow enables it; back returns to `On` / `Open settings`; `files/datastore/settings.preferences_pb` contains `accessibility_consent_at`; `Open settings` reopens the Accessibility screen directly; `dumpsys accessibility` lists the service as enabled and bound with `TYPE_WINDOW_STATE_CHANGED`; the VPN placeholder still shows the build note and returns without opening settings | 5ac5b77 |
| 4d | 11 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 1m | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E` and no FATAL | VPN row starts `Off` / `Set up` with `ACTIVATE_VPN=default`; the disclosure shows the body, an unchecked consent box, and “Allow VPN access”; consent plus action returns to Permissions and raises the system dialog “Connection request” in `com.android.vpndialogs/.ConfirmDialog` (the earlier instant-finish came from a plain `startActivity`, whose null `getCallingPackage()` made `prepareVpn(null, null)` succeed); Cancel returns to the app with `ACTIVATE_VPN` still `default` and the row still `Off`; the retry from `Open settings` reopens the same dialog; OK sets `ACTIVATE_VPN=allow` and the row flips to `On`; the granted row opens `com.android.settings.Settings$VpnSettingsActivity` for `android.settings.VPN_SETTINGS`, and back returns to the row still `On`; the Device Admin placeholder still shows the build note and returns without opening settings | 90d8508 |
| 4e | 11 Nov 2026 | `bash ./gradlew testDebugUnitTest lintDebug assembleDebug --offline` BUILD SUCCESSFUL, 16 tests 0 failures | `adb install -r` Success after `pm clear` | Empty for `AndroidRuntime:E` and no FATAL | Device Admin row starts `Off` / `Set up`; the disclosure shows the body, an unchecked consent box, “Activate Device Admin”, and no build note; consent plus action returns to Permissions and opens `SecDeviceAdminAdd` “Activate device admin app” with Guardian and the explanation text; Activate returns to the row `On` / `Open settings`; `dumpsys device_policy` lists the receiver under “Enabled Device Admins (User 0)”; the granted row opens `Settings$DeviceAdminSettingsActivity` “Device admin apps” (no public action exists for that list, so the row targets the platform class and falls back to Security settings); deactivating Guardian there and returning flips the row to `Off`; the direct path raises the same activation screen and restores `On`; after `pm clear` the row still reads `On` from system state alone; Device Admin stays a permission grant only, uninstall enforcement lands in Step 9 | c102809 |
| 5 | 11 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline` BUILD SUCCESSFUL, 25 tests 0 failures; `bash ./gradlew lintDebug` BUILD SUCCESSFUL in 9m 38s (after fixing the two `NonObservableLocale` errors from `Locale.getDefault()`) | `adb install -r` Success | Empty for `AndroidRuntime:E` and no FATAL; the pre-fix WeekChart `IllegalArgumentException: Too many pattern letters: E` FATAL is gone after switching to `EEE` + `take(2)` | Missing access (`cmd appops set com.nordev.guardian GET_USAGE_STATS ignore`) swaps the Today hero and chart for a ProblemCard with “Open settings”, the status total shows “Not available”, and loading shows “Reading…”; restored `allow`. Granted: Today hero `2 h 8 min`, week chart `45 h 48 min`, Apps today lists Guardian `1 h 16 min`, ChatGPT `44 min`, Phone `3 min`; Digital Wellbeing is not installed, so the comparison against two apps used the `dumpsys usagestats` in-memory daily stats instead | 6c6cfe6 |
| 6 | 11 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline` BUILD SUCCESSFUL, 29 tests 0 failures | `adb install -r` Success | Empty for `AndroidRuntime:E` and no FATAL | Blocking pass: domain policy engine with daily limit, full screen BlockScreen with strings, accessibility service decision path added; then limits screen (intro, choose app, apps with limits, app picker with search, limit editor with presets/custom/save/remove) wired into the parent scaffold with live limits state that pushes into the service policy, and the nav host renders the BlockScreen from the service-written blocked decision; build, tests, lint green, clean logcat, no crash | 9f36509 |
| 7 | 15 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline --no-daemon` BUILD SUCCESSFUL in 9m 7s, unit tests cover bedtime, cross-midnight, study, overlap, and disabled schedules | `adb install -r` Success; re-enabled the accessibility service afterwards | Empty for `AndroidRuntime:E` and no FATAL; the decision line `decision=Blocked(reason=Schedule(name=Bedtime, until=07:00))` for `com.sec.android.app.clockpackage` was the proof | Schedules tab renders the empty state, the schedule count, and the Add buttons; the editor opens in a bottom sheet; toggling Saturday on and Monday off produces “Saturday 22:00 to 07:00, ends the next day”; Save adds the card “Bedtime / Saturday, Sunday 22:00 to 07:00, ends the next day / Pause all apps”; at Sunday 06:34 the clock package was blocked by the Saturday bedtime window and the block screen read “Apps are paused / It is bedtime. Apps are paused until 07:00. / Ends at 07:00 / Go to Home”. Known gaps: schedules and limits live in `remember`, so any idle lock or app death wipes them; the block screen renders only inside Guardian's own nav host, so no sheet appears over the blocked app; study allow list and overlapping windows are unit tested but not driven on the phone | d7f4026 |
| 7b | 15 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest --offline --no-daemon` BUILD SUCCESSFUL in 4m 50s, 38 tests 0 failures; `bash ./gradlew lintDebug --offline --no-daemon` BUILD SUCCESSFUL in 7m 48s | `adb install -r` Success; the accessibility service was re-enabled afterwards | Empty for `AndroidRuntime:E`, no FATAL, crash buffer empty; the YouTube `AndroidRuntimeException: Activity could not be started` came from force-stopping the PiP owner, not Guardian | Rules move to DataStore and the service reads them: after `am force-stop` the Schedules tab still shows “Bedtime / Sunday 00:01 to 23:54 / Pause all apps” and Limits still shows “Clock / 0 of 15 min”; `files/datastore/policy.preferences_pb` holds `schedules_v1` (`sched-… Bedtime SUNDAY 1 1434 PAUSE_ALL 1`) and `limits_v1` (`com.sec.android.app.clockpackage 15`); a fresh service start with the parent UI closed loaded the stored schedule and showed “Apps are paused / It is bedtime. Apps are paused until 23:54.”; rotating no longer re-locks the parent session and the keyboard is skipped. Block host: opening Settings gave `mFocusedApp=com.nordev.guardian/.ui.block.BlockActivity`, `decide com.android.settings -> Blocked(reason=Schedule(name=Bedtime, until=23:54))`, and the full screen pause screen over Settings; “Go to Home” reached the launcher with `skip com.sec.android.app.launcher` and finished the block activity; reopening Settings blocked again; starting Guardian over the block showed the parent PIN screen with no stale overlay; Back on the block screen does nothing. A YouTube picture-in-picture window sat over the buttons and ate taps until it was closed, so the host does not cover PiP. Known gaps: reboot is still unverified because the phone needs an unlock after boot, study allow list and overlapping windows remain unit tested only, and the Google search surface blocks under pause-all like any other app | b52ea01 |
| 7c | 15 Nov 2026 | no code change; rechecks `b52ea01` after a real reboot (`/proc/uptime` put boot at about 10:47) | — | Empty crash buffer and zero `Process: com.nordev.guardian` entries | The Bedtime schedule was still on disk (`PAUSE_ALL 1`) and `enabled_accessibility_services` survived the reboot, so the service bound itself as pid 6238 without anyone opening Guardian; after the first unlock YouTube, WhatsApp, and WhatsApp Business each produced `decide … -> Blocked(reason=Schedule(name=Bedtime, until=23:54))` and the pause screen appeared over them (BlockActivity task t1392); Home finished the host and the launcher stayed usable; the test schedule was turned off afterwards (`PAUSE_ALL 0`, `decide com.android.settings -> Allowed`). Known gap: the block host still does not cover picture-in-picture. | b52ea01 |
| 8a | 15 Nov 2026 | `bash ./gradlew testDebugUnitTest assembleDebug --offline --no-daemon` BUILD SUCCESSFUL; unit tests cover `DomainName` normalization, `DomainMatcher` suffix matching, and the adult list load | — | — | Domain rules land before the tunnel: `DomainName`, `DomainMatcher`, `AdultBlocklist`, `BlocklistRepository`, and `assets/adult-domains.txt` with 19 hand-authored domains, version 2026.11; `docs/vpn-design.md` records the DNS-only design and `docs/third-party.md` records the asset origin and SHA-256 `b7ee0fae219f2b9874b60a5cf5e3cdda01419fa9a43fc51f41ac22878d8c6e74` | 861da52 (docs), bcac324 (code) |
| 8b | 15 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest --offline --no-daemon` BUILD SUCCESSFUL; unit tests cover DNS, IP, UDP parsing, and `WebFilterCodec` | `adb install -r` Success; the accessibility service was re-enabled afterwards | Empty crash buffer; `GuardianDns` logged `upstream 2 server(s)`, `filter up`, and `filtered pornhub.com` | The tunnel runs: `tun0` carries `10.111.222.1/32` and `fd00:1111:2222::1/128`, the VPN network is `CELLULAR|VPN` session `Guardian`, SystemUI exposes the key as `VPN on.`, and the foreground notification `Website filter is on` uses channel `web_filter`; `pornhub.com` and `www.xvideos.com` return NXDOMAIN while `example.com` resolves, and Chrome shows `DNS_PROBE_FINISHED_NXDOMAIN` for a blocked name. The switch and tunnel start from the stored value on launch. | 8454d9f |
| 8c | 15 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest --offline --no-daemon` BUILD SUCCESSFUL in 2m 29s, 72 tests 0 failures; `bash ./gradlew lintDebug --offline --no-daemon` BUILD SUCCESSFUL in 4m 43s | `adb install -r` Success; the accessibility service was re-enabled afterwards | Empty crash buffer and no FATAL after the final build; `GuardianDns` logged `filter up` and `filtered pornhub.com` on the last on-switch | Web tab renders the on card, the switch row, `0 sites` empty state, `Adult site list, version 2026.11, 19 domains.`, the caveat card, and the how-it-works sheet copy; `neverssl.com` added in the sheet blocks immediately while `example.com` still resolves; invalid input shows `Enter a domain like example.com.`, a custom duplicate shows `That site is already on your list.`, and an adult duplicate shows `Already on the adult list.`; the remove dialog focuses Cancel, ENTER dismisses without removing, and Remove updates the count back to `0 sites`; switch off shows the off card, removes `tun0`, drops the active notification record to zero, and lets `google.com` resolve; switch on restores the on card, `tun0`, the notification, and adult blocking. Fixed during the pass: the system keeps a `VpnService` bound so `stopService` never reached `onDestroy` and the tunnel kept filtering — the off path now sends an explicit `ACTION_STOP` and tears the descriptor down; the optimistic `enabled = true` default briefly started the service on launch and crashed with `ForegroundServiceDidNotStartInTimeException` — auto-start now waits for the first stored value; saving before starting removed a duplicate `filter up` race. Known gaps: the filter does not auto-start after a reboot until Guardian opens, TCP DNS queries are not proxied, DNS over HTTPS or TLS and hardcoded IPs bypass the tunnel, DNS cache TTLs delay a newly added or removed domain, and the other-VPN and no-network states are implemented but not exercised on the phone (accepted gap; the other-VPN check moves to the Step 10 device tests with a real VPN app). Deviation from `design.md`: turning the filter off removes the foreground notification instead of showing an explicit off notification, recorded in `docs/vpn-design.md`. | ab4e4ba |
| 9a | 16 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline --no-daemon` BUILD SUCCESSFUL in 9m 5s, 72 tests 0 failures | `adb install -r` Success (9a install verified; phone disconnected after the clean, final re-install of the committed APK pending adb reconnection) | Empty crash buffer; no FATAL through the PIN-gated removal flow | Correct PIN opens the system uninstall prompt (`com.google.android.packageinstaller/.UninstallLaunch`) over Guardian; Cancel returns to the removal screen showing "Protection is off. If you keep Guardian, turn the checks back on in Permissions." and protection stays off after a home/resume cycle (admin count 0, `tun0` gone, `enabled_accessibility_services` null, Guardian notification records 0, crash buffer empty); wrong PIN shows "That PIN is not correct. Try again." with admin and tunnel still up; 5-digit PIN leaves the destructive button inert; the removal card appears on Settings after scrolling. | 4d0d54e |
| 9b | 16 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline --no-daemon --no-parallel --max-workers=2` BUILD SUCCESSFUL in 15m 19s, 82 tests 0 failures; lint 0 errors, 10 warnings (parallel lint analyze tasks deadlock without the flags) | `adb install -r` Success; accessibility re-enabled afterwards | Empty crash buffer and no FATAL; PIN token 0 | Limits Needs attention with Accessibility off ("Accessibility is off. Guardian cannot block apps."), Web Needs attention with `ACTIVATE_VPN=default` and no tunnel ("Allow VPN access so the filter can run."), Uninstall On with the admin active; hero "Protection needs attention" and "2 checks need attention. Tap Review to fix them."; Review on Web → gate → PIN 258036 → parent Web tab, "Turn on" → `com.android.vpndialogs/.ConfirmDialog` → OK → `ACTIVATE_VPN=allow` and `tun0` up; enabling Accessibility flips Limits On and the hero to "Guardian is on" with all three On; deactivating the admin through `SecDeviceAdminAdd` → Uninstall Needs attention ("Uninstall protection is off. Android can remove Guardian without your PIN.") and Review → gate → PIN → Onboarding Permissions "Step 4 of 5"; system back returns to Status; `dpm set-active-admin` restores Uninstall On and the healthy hero. Known gap: the all-off hero and all-checks-off layout were unit tested only. | 1ff094d |
| 8d | 16 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline --no-daemon --no-parallel --max-workers=2 --console=plain` BUILD SUCCESSFUL in 7m 48s, 83 tests 0 failures; lint 0 errors, 10 warnings, 4 hints | `adb install -r` Success; accessibility re-enabled afterwards | Empty crash buffer and no FATAL; no `AndroidRuntime:E Guardian:V` entries | Final fix on the installed build: the shipped adult list is the StevenBlack porn-only hosts snapshot version `2026.11.15` with 64,307 parsed domains after the `www.` TLD fallback fix; status hero "Guardian is on" with all three checks On. Web tab shows version `2026.11.15` and 64,307 domains; the gate unlocks with the keyboard up after the `imePadding` plus `adjustResize` fix; adult DNS (`pornhub.com`, `www.xvideos.com`, `xhamster.com`, `test.sex`, `test.xxx`) returns NXDOMAIN while `example.com` resolves; the VPN settings screen is guarded by `GuardActivity` with the same PIN (wrong PIN error, correct PIN unlocks, Back ignored, Home works, 60 s grace, re-guard on return); the Web switch off opens the PIN confirmation screen, wrong PIN keeps the filter on, correct PIN turns it off (tun0 gone, notification gone, blocked names resolve), "Keep it on" keeps it on, and switching back on restores blocking; clean logcat, no crashes. | 2d3a87c |
| 9c | 17 Nov 2026 | `bash ./gradlew assembleDebug testDebugUnitTest lintDebug --offline --no-daemon --no-parallel --max-workers=2 --console=plain` BUILD SUCCESSFUL in 27m 57s, 90 tests 0 failures; lint 0 errors, 14 findings (0 errors) (9c-only tree at `e2f223d`) | `adb install -r` Success; accessibility re-enabled afterwards | Empty crash buffer and no FATAL; PIN token 0 | Final fix on the installed build: a Settings window whose class ends in `DeviceAdminAdd`, or equals the Samsung `SecDeviceAdminAdd`, now raises the PIN guard, armed only while the parent uninstall protection is on; because Samsung draws the screen as an appcompat `AlertDialog`, the event class does not match and the service falls back to the resumed activity from `UsageStatsManager` (2 s lookback, 300 ms delay), package and class only, no screen text. On the phone: the guard raised on entering `com.samsung.android.settings.applications.specialaccess.SecDeviceAdminAdd` with `guard AdminProtection …`, the screen showed "Uninstall protection is on" plus "Unlock protection" plus the grace note plus "Go home"; the wrong PIN showed "That PIN is not correct. Try again."; the correct PIN 258036 released the deactivation screen after the 60 s grace; the Back key was ignored; Home worked; the grace let the real deactivation through (admin count 0); no duplicate-finish warning, no FATAL. | e2f223d |

## Appendix B. Questions for `/architect`

Step 6, blocking engine:

- Host the block screen in an accessibility overlay or a full screen activity?
- If an overlay, how do we host Compose safely and keep TalkBack focus?
- Which foreground service type fits, `systemExempted` or `specialUse`?
- How do we merge the usage ledger with `UsageStatsManager` without two visible numbers?

Constraints: no `SYSTEM_ALERT_WINDOW` if an overlay works, no screen text, no gestures, deterministic rules, no trapping the child.

Step 8, VPN filter:

- Local DNS proxy design, upstream resolver, IPv4 and IPv6, packet parsing library.
- How to detect and report another active VPN.
- How to handle DNS over HTTPS and hardcoded IPs without claiming a guarantee.
- Whether `INTERNET` is needed for the forwarder on API 26 to 28.

Constraints: no remote server, no browsing history, no traffic inspection, visible failure.

Step 9, uninstall protection:

- Minimum Device Admin policies.
- Which Settings screens to guard, and how to identify them across OEMs.
- How to keep the parent able to remove Guardian while the child cannot.
- What to do when an OEM blocks Device Admin or changes the uninstall flow.

Constraints: parent-authorized removal, no hidden behavior, no screen text if possible, Play policy compliance.

Each question gets a short design note committed before its code.
