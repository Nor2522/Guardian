---
name: verify-guardian
description: Drive and verify the Guardian Android app on Nor's USB-connected phone (Samsung SM-A155F, serial RF8X60FV0TD) with adb and logcat. Use for phone verification in this repo. Build, install, launch, exercise a feature, and capture text evidence. Never screenshots.
---

# Verify Guardian

Guardian is an Android parental-control app. Verification happens on the real phone over USB. The phone belongs to Nor, and later steps put real child and parent data on it, so nothing here installs, uninstalls, or kills state without saying so.

## Environment facts

- One device. Serial `RF8X60FV0TD` (Samsung SM-A155F). The phone is Nor's real device and may be locked with a PIN while Android still runs visible activities behind it. Do not attempt to unlock it with adb.
- No emulator, no second device. Two verification runs use the same instance.
- The screen may show the lock screen (`NotificationShade`). `mFocusedApp` keeps the app even when the shade is up, so focus checks use `mFocusedApp`, and visual confirmation belongs to Nor.
- Android SDK lives at `~/Android/Sdk`. Java 17 is installed. Gradle is only available through the wrapper (`bash ./gradlew ...`, Gradle 9.7.0).
- The Gradle user home holds a pre-seeded Gradle 9.7.0 distribution. First dependency downloads needed retries on this connection: if network fetches fail, a manual resumable `curl -C -` loop is the recovery pattern for a distribution, and Gradle's own dependency download tolerates slow links thanks to `gradle.properties` timeouts.

## Launch

Build, install, then cold-start the app from zero.

```bash
cd /home/nor/Projects/guardian
bash ./gradlew assembleDebug --no-daemon --console=plain
adb -s RF8X60FV0TD install -r app/build/outputs/apk/debug/app-debug.apk
```

Success equals `Success` from adb and `BUILD SUCCESSFUL` from Gradle. Then start the app cold and wait for it to be usable:

```bash
adb -s RF8X60FV0TD shell am force-stop com.nordev.guardian
adb -s RF8X60FV0TD logcat -c
adb -s RF8X60FV0TD shell am start -n com.nordev.guardian/.MainActivity
sleep 3
```

Ready means all three: `adb -s RF8X60FV0TD shell pidof com.nordev.guardian` prints a pid, `adb -s RF8X60FV0TD shell dumpsys window displays | grep mFocusedApp` shows `com.nordev.guardian/.MainActivity`, and the logcat check in Evidence is empty. A `Warning: Activity not started, intent has been delivered to currently running top-most instance` line means the force-stop missed and the run reused a warm instance; force-stop again or record the warm start as a deviation.

Teardown for a run that launched the app:

```bash
adb -s RF8X60FV0TD shell am force-stop com.nordev.guardian
```

Never uninstall the package during verification. Uninstalling has to be a deliberate step Nor asks for, because it deletes Guardian data from the real phone.

## Doctor

One read-only check when anything looks off. All lines must print output.

```bash
adb devices -l | grep RF8X60FV0TD
adb -s RF8X60FV0TD shell pm path com.nordev.guardian
adb -s RF8X60FV0TD shell pidof com.nordev.guardian
```

Meaning: phone attached and authorized, package installed (a `package:` path prints), process running (a pid prints, empty means the app is not running, which is fine before Launch). If `adb devices` shows `unauthorized`, stop and ask Nor to look at the phone; nothing else here works while authorization is pending.

## Drive

Guardian has no browser or CLI surface. From Step 1 to the present, every check is an adb probe; Compose interaction via `adb shell input` arrives with the first screen that needs taps. Recipe for any feature:

1. Cold start as in Launch.
2. `adb -s RF8X60FV0TD logcat -c` right before the action, so captured logs belong to the drive.
3. Chain real user inputs (`adb shell input tap`, then texts or keyevents) for the feature under test. Use `adb -s RF8X60FV0TD shell dumpsys window displays | grep mFocusedApp` between taps to confirm which activity is in front.
4. Capture the resulting state with `dumpsys activity` probes after the drive, plus the logcat dump below.

For steps that need a judgment about what the screen shows (colors, layout, TalkBack), adb text probes are not enough. State in the proof which part adb proved and hand the visual confirmation question to Nor instead of "passing" it.

## Evidence

Capture proof into `/tmp/opencode/verify-guardian/<step>-<date>.log`. One file per verification run, and it must survive cleanup:

```bash
DEVOUT=/tmp/opencode/verify-guardian/hello-$(date +%F-%H%M).log
adb -s RF8X60FV0TD logcat -d -s AndroidRuntime:E Guardian:V > "$DEVOUT"
adb -s RF8X60FV0TD shell dumpsys window displays | grep -E "mCurrentFocus|mFocusedApp" >> "$DEVOUT"
adb -s RF8X60FV0TD shell pidof com.nordev.guardian >> "$DEVOUT"
```

Proof standards for this app:

- A behavior is proven when the real user path produced it on the phone: the install, the launch, the logcat after it, and the focused window all carry the feature that ran, not a placeholder.
- Crash-free is a hard gate for every run: `logcat -d -s AndroidRuntime:E Guardian:V` stays empty. A non-empty dump fails the run.
- Side effects show in their own probe (SharedPreferences through `dumpsys`, files through `run-as` when debuggable, the ledger tables through queries the app itself settles for in later steps), not by trusting an earlier run.
- Record any step Nor must look at visually as a named question in the report. Do not convert it into a pass by adb.

## Cleanup

- Kill only apps the run launched: `am force-stop com.nordev.guardian` for exactly what the run started, nothing else.
- Uninstall only on Nor's explicit instruction.
- Scratch inputs (taps, typed keys) need no cleanup; evidence files in `/tmp/opencode/verify-guardian/` are never removed by cleanup.

## Helpers

None to date. Every command is a one-liner shown above. When Step 2 adds navigation, add a helper that drives the tab bar, and show its invocation here.
