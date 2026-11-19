# Permission and policy declarations

Every sensitive capability the app uses, what the reviewer will ask, and the
answer with the in-app evidence behind it. Sources: Play policy pages listed at
the end; checked 19 November 2026.

## 1. AccessibilityService (declaration required)

Guardian is **not** an accessibility tool. It does not set
`isAccessibilityTool`, so the prominent-disclosure path applies and the
Accessibility declaration form is mandatory for every version code.

| Console question | Answer |
| --- | --- |
| Why does the app need the Accessibility API? | **App functionality** |
| Does the app collect and/or share personal or sensitive data using the accessibility capabilities? | **No** |

What the service actually does: it reads the foreground package name so app
limits, schedules, and the pause screen can react, and it matches the system VPN
and Device Admin screens by package and class only so the parent PIN gates them.
It never reads screen text, never performs gestures, never captures the screen,
and stores no accessibility event.

In-app disclosure, already built:

- `onboarding/permissions` → Accessibility row → `AccessibilityDisclosure`
  screen with its own text, its own consent checkbox, and an "Open Accessibility
  settings" action that does nothing until the box is ticked.
- The same disclosure is reachable from Status → `Review` and from Settings →
  permissions, so the flow can be replayed.

Video script for the declaration (record on the SM-A155F, 60 to 90 seconds):

1. Open Guardian cold, from the launcher.
2. Walk Status → `Review` → Accessibility to show the full disclosure, scrolling
   slowly enough to read every line.
3. Without ticking the box, tap the action: the app refuses and says to tick the
   box first.
4. Tick the box, tap the action, land on Android's Accessibility screen, enable
   Guardian, confirm the system dialog.
5. Return to Guardian and show the row now reads `On`.
6. Show the core use: open a limited app, hit the limit, show the pause screen.
7. Leave and re-enter the disclosure to show it can be replayed.

## 2. isMonitoringTool metadata flag (required, code change pending)

The Malware policy treats a parental-control app that watches another person's
device as a monitoring app. It must declare itself:

```xml
<meta-data android:name="isMonitoringTool" android:value="child_monitoring" />
```

Status: **not yet in the manifest.** Add it in `AndroidManifest.xml` inside
`<application>` before the release build.

Monitoring-app requirements and where each is met:

| Requirement | Where |
| --- | --- |
| Not presented as spying or secret surveillance | Store copy, in-app copy: parent sets it up on the child's phone, with the child's knowledge |
| No hidden or cloaked tracking | Ongoing notification, status screen, and the privacy page name every capability |
| Persistent notification whenever it runs | Low-importance ongoing "Guardian is on" notice; `problem_notice_*` card warns when Android hides it |
| Unique, identifying icon | Guardian shield, one notification channel |
| Monitoring disclosed in the store description | `docs/store/listing.md`, disclosure paragraph |
| No route to non-Play builds | No update check, no external APK link, no self-updater |

## 3. VpnService

| Console question | Answer |
| --- | --- |
| Core functionality | Local DNS filter for parental controls, on this device only |
| Remote VPN or proxy service | None. There is no Guardian server and no exit node |
| Traffic inspection | None. The tunnel routes only `10.111.222.2/32` and `fd00:1111:2222::2/128`, the two DNS addresses |
| Data collection | No query log, no browsing history, no counters per site |
| User control | One switch, off tears the tunnel down; the ongoing notice is the visible proof |

Foreground service type is `specialUse` with the subtype property explaining the
filter; the parent can always turn it off and the service declares no other
traffic path.

## 4. Usage access (PACKAGE_USAGE_STATS)

Reads foreground time per app for screen-time totals and limits. No declaration
form exists for this special access, but the disclosure rule applies and is met:
the Permissions screen has a separate Usage access disclosure with a consent
checkbox before the system screen opens. Data stays in app-private storage and
is never transmitted.

## 5. Device Admin (highest review risk)

Used only so removing Guardian requires the parent PIN. The policy line to watch
is Elevated Privilege Abuse: "don't write code that prevents a user's app from
being uninstalled."

How Guardian stays inside that line:

- The parent turns Device Admin on themselves, from a dedicated disclosure.
- Removal stays possible: Settings → Deactivate, or Guardian's own remove flow,
  both gated by the parent PIN (a 60 second grace after a correct PIN).
- Nothing is hidden: the admin is visible in system settings, the Status screen
  reports when it is off, and the disclosure says what it does.
- No other device policies are requested. No lock, no wipe, no password rules.

If review pushes back, the fallback is to ship without Device Admin in the first
release and keep the PIN grace only. That is a product call for Nor.

## 6. Notifications

`POST_NOTIFICATIONS` powers the ongoing notice. Denied is a supported state: the
filter keeps running and both Status and Today show the notice card with a
`Show notice` action that opens the app's notification settings.

## 7. Reboot

`RECEIVE_BOOT_COMPLETED` restores the filter only while the stored switch is on
and VPN consent still exists. No background consent prompt, no hidden start after
the parent turned the filter off.

## Sources

- Use of the AccessibilityService API, https://support.google.com/googleplay/android-developer/answer/10964491
- Use of the isMonitoringTool flag, https://support.google.com/googleplay/android-developer/answer/12955211
- Malware policy, including Stalkerware and Elevated Privilege Abuse, https://support.google.com/googleplay/android-developer/answer/9888380
- User data policy, prominent disclosure, https://support.google.com/googleplay/android-developer/answer/10144311
- Sensitive permissions and APIs, including VPN, https://support.google.com/googleplay/android-developer/answer/16558241
