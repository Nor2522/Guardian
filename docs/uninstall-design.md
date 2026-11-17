# Uninstall protection design

Step 9 keeps Guardian on the phone until a parent removes it. No remote server,
no hidden behavior, and a visible path out for the parent.

## Mechanism

An active Device Admin is the whole mechanism: Android refuses to uninstall a
package that has one. The app requests no `<uses-policies>` entries, so the
admin cannot wipe, lock, reset the password, or hide the launcher icon. The
receiver is a plain `DeviceAdminReceiver`.

The child's uninstall path is therefore:

1. Long press Guardian, choose Uninstall, or open Settings > Apps > Guardian.
2. Android refuses, or offers to deactivate the admin and uninstall.
3. The deactivation screen is `DeviceAdminAdd` on AOSP and
   `com.samsung.android.settings.applications.specialaccess.SecDeviceAdminAdd`
   on One UI (measured on the SM-A155F).
4. Guardian catches that screen and asks for the parent PIN.

Android still shows every step. Nothing is intercepted silently and no screen
text is read; the guard matches the window's package and class only.

## Parent removal path

Settings > Remove Guardian (`parent/settings/remove`):

- Title "Remove Guardian", the body from `design.md`, primary "Turn off and
  remove", secondary "Keep Guardian".
- The PIN is always required here, even in an unlocked parent session.
- On success: `removeActiveAdmin`, stop the DNS filter and the accessibility
  service, then `ACTION_DELETE` for the package. The system uninstall screen
  does the rest and Android deletes the app data with the package.
- If the parent cancels the system uninstall, Guardian stays installed with
  protection off and the status screen says so. That is a truthful state, not
  an error.

## Guard behavior

- Trigger: a window from a Settings package (`com.android.settings` or
  `com.samsung.android.settings`) whose class ends in `DeviceAdminAdd` or equals
  the Samsung class above. The guard is armed only while the parent's own
  "uninstall protection" switch is on, so a parent who turned it off is not
  gated.
- Class source: the window class decides first. On the SM-A155F the Samsung
  deactivation screen is an appcompat dialog, so the accessibility event reports
  `androidx.appcompat.app.AlertDialog` and the activity name never appears.
  When the window class does not match, the service asks `UsageStatsManager`
  for the activity that resumed in that package during the last two seconds and
  matches that class instead. It is package and class information only: no view
  is read, no text is compared, and no gesture is performed.
- Host: the existing block activity, in a dedicated admin-guard mode. It keeps
  its own empty task, ignores Back, and lets Home work.
- Copy: "Uninstall protection is on" / "Enter your parent PIN to change it.
  Turning it off lets the child remove Guardian without the parent PIN." The
  unlock button reads "Unlock protection", and the grace note under it reads
  "After a correct PIN, protection stays unlocked for 60 seconds." The PIN pad
  is the existing gate, including the wrong-try lockout.
- Correct PIN: set a 60 second grace timestamp, close the host, and return to
  Settings. The guard ignores the screen while the grace lasts, so the parent
  can finish deactivating or uninstalling.
- Wrong PIN, lockout, or Back/Home: the host stays or exits to Home, and the
  Settings screen is not usable underneath.
- Guardian's own package is always allowed so the guard cannot block itself.

## Failure states

| State | Detection | UI |
| --- | --- | --- |
| Admin inactive | `DevicePolicyManager.isAdminActive` false | status check "Uninstall protection" shows a problem row and the Permissions row shows Off |
| Accessibility off | service not bound | status problem row explains the guard needs Accessibility; the OS still blocks uninstall, but deactivation is not PIN-gated |
| Another admin app | system list | not Guardian's concern; the guard only matches the screens, not the admin list |
| Device owner or managed profile | `isDeviceOwnerApp` | out of scope for version 1; the app never provisions one |

## What this cannot do

- With Accessibility turned off, a child who reaches Android Settings can
  deactivate the admin. The OS still requires that visible step, and the status
  screen reports the weaker state.
- Safe mode, recovery, and `adb uninstall` (debug builds) bypass the admin. The
  release build has no debug flag.
- An OEM that hides or renames the deactivation screen can slip past the guard.
  The class list is a constant with a recorded measurement per OEM, and a miss
  fails open to the system screen rather than trapping the child.
- Device Admin is a sensitive permission. The Play listing must declare it and
  the disclosure copy stays as written in `design.md`.

## Verification plan

Measured on the SM-A155F: the Samsung deactivation screen refuses any intent
that carries `FLAG_ACTIVITY_NEW_TASK` and finishes itself (`SecDeviceAdminAdd:
Cannot start ADD_DEVICE_ADMIN as a new task`). `adb shell am start` always sets
that flag, so the screen is verified from inside Settings, never from adb. The
same screen also hides its activity name from Accessibility, which is why the
guard resolves the class from UsageStats as described above.

- Admin active, `adb shell pm uninstall com.nordev.guardian` fails with the
  device-admin error.
- Uninstall from Settings reaches the Samsung deactivation screen, the guard
  host appears, a wrong PIN keeps it, and the correct PIN lets the parent
  continue.
- The parent removal screen requires the PIN even in an unlocked session and
  reaches the system uninstall confirmation.
- Cancel the system uninstall: Guardian stays, protection is off, and the status
  screen says so.
- Accessibility off: deactivation is not gated and the status problem row shows
  the weaker state.
- Clean logcat and crash buffer through every path.
