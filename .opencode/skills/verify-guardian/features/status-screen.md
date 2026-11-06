# Status screen

## Sub-features

- Cold launch lands on the child-facing status screen with the Guardian title.
- The hero reports the honest setup state: "Finish setting up Guardian" with
  "3 checks are left before protection is complete."
- Screen time shows the empty state "None today" until Usage access ships.
- "Finish setup" opens the gate. With no PIN in memory, the gate routes to
  onboarding instead of showing a field.
- The privacy footer stays visible without scrolling.

## How to get to it (user POV)

Nor opens Guardian and sees the status screen, not a login. Nothing on this
screen needs the PIN. "Finish setup" is the only action; it leads to the gate,
which becomes a real PIN screen once a PIN has been set in this process.

## Driving it with adb

```bash
adb -s RF8X60FV0TD shell am force-stop com.nordev.guardian
adb -s RF8X60FV0TD logcat -c
adb -s RF8X60FV0TD shell am start -n com.nordev.guardian/.MainActivity
sleep 3
adb -s RF8X60FV0TD shell uiautomator dump /sdcard/g.xml
adb -s RF8X60FV0TD pull /sdcard/g.xml /tmp/opencode/g.xml
grep -o 'text="Finish setting up Guardian"' /tmp/opencode/g.xml
grep -o 'text="None today"' /tmp/opencode/g.xml
adb -s RF8X60FV0TD logcat -d -s AndroidRuntime:E Guardian:V
```

Pass when the two greps print, logcat is empty, and
`dumpsys window displays | grep mFocusedApp` names Guardian.

Tapping "Finish setup" at `(540, 1180)` on the 1080x2340 screen opens the gate.
After the PIN is set in the same process the gate shows its field; after a
cold start it redirects to onboarding.

## Gotchas

- The phone may sleep or lock between commands. `mFocusedApp` still names
  Guardian, but uiautomator dumps `com.android.systemui` and taps hit the lock
  screen. Ask Nor to unlock; never try to unlock with adb.
- `svc power stayon true` keeps the screen awake while USB is connected. Turn
  it back off with `svc power stayon false` after the run.
- The status screen hides protection rows and active rules until the checks
  ship. Do not report their absence as a bug in this step.
