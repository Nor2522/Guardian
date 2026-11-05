# Guardian plan

Guardian is a parental control app for Android. Version 1 runs on the child's phone. The parent sets it up and protects every setting with a PIN. Rules and usage stay on the phone. There is no Guardian account, server, ads, or analytics.

Working name: Guardian. Package: `com.nordev.guardian`. `minSdk 26`. `compileSdk 36`. `targetSdk 36`.

The brief said `targetSdk 34`. A new app with target 34 cannot be published. Since 31 August 2026, Google Play accepts only new apps and updates that target Android 16 (API 36). Step 1 installs platform 36 and build-tools 36. The app still runs on Android 8.0 and newer.

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
| 0 | 5 Nov 2026 | n/a | n/a | n/a | Waiting for Nor to read the docs | pending |

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
