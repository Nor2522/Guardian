# Hello screen

## Sub-features

- Cold launch lands on `MainActivity` with the Guardian hello copy visible.
- The app survives a force-stop and relaunch with no crash output.

## How to get to it (user POV)

Nor taps the Guardian launcher icon and sees a centered "Guardian" title with "Hello, Guardian runs." below it on a light or dark background. No login, no permissions, nothing else yet.

## Driving it with adb

```bash
adb -s RF8X60FV0TD shell am force-stop com.nordev.guardian
adb -s RF8X60FV0TD logcat -c
adb -s RF8X60FV0TD shell am start -n com.nordev.guardian/.MainActivity
sleep 3
adb -s RF8X60FV0TD logcat -d -s AndroidRuntime:E Guardian:V
adb -s RF8X60FV0TD shell dumpsys window displays | grep -E "mCurrentFocus|mFocusedApp"
adb -s RF8X60FV0TD shell pidof com.nordev.guardian
```

Pass when the logcat dump is empty, `mFocusedApp` names `com.nordev.guardian/.MainActivity`, and a pid prints.

## Gotchas

- adb proves the process, the focused window, and crash-freedom. It does not prove the words on screen. Ask Nor: "unlock the phone and confirm Guardian shows the hello text." Keep that an open question in the report.
- The phone is often locked. `mCurrentFocus` then reads `NotificationShade` even with Guardian in front. Judge by `mFocusedApp`, never by focus alone.
- `am start` on a warm instance prints a "currently running top-most instance" warning. That is a reuse signal, not a launch; force-stop first.
