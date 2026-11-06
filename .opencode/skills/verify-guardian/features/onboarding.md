# Onboarding

## Sub-features

- Welcome, privacy, PIN, permissions, per-permission disclosures, and the final
  checklist all open from the status screen.
- The PIN step rejects fewer than six digits, weak PINs (repeated, ascending,
  descending), and a mismatched confirmation.
- A valid PIN is kept in memory for this process and makes the gate real.
- Each permission row opens its own disclosure. The consent row is a checkbox
  to TalkBack: it exposes checked state and its label.
- "Finish later" and "Not now" leave setup incomplete without pretending a
  check is on.
- The final checklist lists all three checks as Off and offers "Open status".

## How to get to it (user POV)

From the status screen, Nor taps "Finish setup". With no PIN yet the gate routes
straight to "Step 1 of 5"; the five steps then lead to the checklist. After the
PIN is set, "Finish setup" opens the real gate instead.

## Driving it with adb

Start from a cold launch, then:

1. Status "Finish setup" at `(540, 1180)`.
2. Welcome "Get started" at `(540, 2185)`.
3. Privacy "I understand" at `(540, 2185)`.
4. PIN: tap `(540, 860)`, type `258036`, tap `(540, 1130)`, type `258036`,
   hide the keyboard, tap "Continue" at `(540, 2185)`.
5. Permissions "Continue" at `(540, 2027)`.
6. Checklist "Open status" at `(540, 2185)`.

Dump the tree after each step and assert the step number and title:

```bash
adb -s RF8X60FV0TD shell uiautomator dump /sdcard/g.xml
adb -s RF8X60FV0TD pull /sdcard/g.xml /tmp/opencode/g.xml
grep -o 'text="Step 1 of 5"' /tmp/opencode/g.xml
```

The disclosure check:

- Open "Set up" on Usage access at `(900, 1002)`.
- The consent row at `(540, 800)` must dump as
  `checkable="true" checked="false"` and, after a tap, `checked="true"`.
- Tapping the action without consent shows "Tick the checkbox above first."

The PIN check:

- With a PIN in memory, "Finish setup" shows "Parent settings".
- Wrong PIN `000000` clears the field and shows
  "That PIN is not correct. Try again."
- Correct PIN `258036` opens the parent tabs.

## Gotchas

- The PIN is memory-only in Step 2. Reinstalling or force-stopping starts the
  gate at the first-run redirect again. That is expected until Step 3.
- The keyboard shifts tap targets. After typing, hide it with Back only when
  `dumpsys input_method` reports `mInputShown=true`; a second Back pops the
  current screen.
- Helpers: no script yet; the coordinates above are the helper while the tab
  bar is still new.
