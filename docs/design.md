# Guardian design

This document is the visual and interaction contract for Guardian version 1. It pairs with `docs/plan.md`. The values here are decisions, not suggestions. Change them only when a phone test shows a real problem, and record the reason in the commit.

The design targets Android phones from 320 dp wide, Android 8.0 to Android 16, light and dark themes, and 200 percent font scaling.

## Design goals

1. Calm authority. The app sets limits without shame, threats, or spy aesthetics.
2. Honest protection. The child and the parent can always see whether protection is on and what it can see.
3. Two-speed use. The child gets a simple status view. The parent gets dense controls behind a PIN.
4. Visible failure. A missing permission looks like a problem, not like normal operation.
5. Native Android. Material 3 structure, system back, system time format, system font scaling, and TalkBack from the first screen.

The test for every screen is simple. A tired parent should understand it in ten seconds, and a child reading it should not feel accused.

## Brand direction

The working name is Guardian. The wordmark is the name set in `titleLarge` with medium weight. There is no custom logotype in version 1.

The app icon is an adaptive icon with a deep teal background and a white shield with a check cutout. The monochrome layer uses the shield silhouette. Do not use an eye, keyhole, padlock, camera, or detective motif. The icon should read as protection, not surveillance.

The notification icon is the same shield silhouette in white. It must remain legible at 24 dp.

The splash screen uses the shield on `surface` with no animation. On Android 12 and newer, the system splash uses the adaptive icon foreground.

There are no illustrations, cartoons, stock photos, or character mascots in version 1. A single shield in a tonal circle carries onboarding and empty states. Color, type, spacing, and copy carry the brand.

Voice rules:

- Write to the parent as "you". Write about the phone, not about the child's character.
- Use short sentences. One idea per sentence.
- Name the rule and the time. "Apps are paused until 07:00" beats "It is past bedtime".
- Say "paused", "limit reached", and "needs attention". Do not say "addicted", "detox", "lockdown", "caught", "spy", or "bad".
- Never promise perfect blocking. Say what the filter can and cannot do.
- No exclamation marks. No emoji. No fear marketing.
- Buttons start with a verb. "Add limit", "Save changes", "Turn on filter".

Examples:

| Avoid | Write |
| --- | --- |
| Screen time is getting out of hand | Here is today's screen time |
| Your child has been blocked | Instagram is paused for today |
| Parental lockdown active | Guardian is on |
| Protect your child from the internet | Blocks adult sites and sites you add |
| No internet until morning | Apps are paused until 07:00 |
| Permission denied | Website filter needs attention |

## Color

Guardian uses fixed light and dark color schemes. Dynamic color is off in version 1. The status colors carry meaning, and the brand should look the same on every phone. The theme setting offers System, Light, and Dark, with System as the default.

The schemes are not pure black or pure white. `surface` is a soft green-tinted neutral in light mode and a near-black green in dark mode. This keeps long reading comfortable and keeps tonal layers visible.

### Light scheme

| Role | Value | Use |
| --- | --- | --- |
| primary | `#006A60` | Primary actions, selected states, links, progress |
| onPrimary | `#FFFFFF` | Text and icons on primary |
| primaryContainer | `#9EF2E4` | Selected navigation indicator, tonal icon circles |
| onPrimaryContainer | `#00201C` | Text and icons on primaryContainer |
| secondary | `#4A635E` | Supporting actions and secondary labels |
| onSecondary | `#FFFFFF` | Text and icons on secondary |
| secondaryContainer | `#CDE8E1` | Study mode chips and secondary tonal areas |
| onSecondaryContainer | `#06201B` | Text and icons on secondaryContainer |
| tertiary | `#5D5B7D` | Schedule accents and time related controls |
| onTertiary | `#FFFFFF` | Text and icons on tertiary |
| tertiaryContainer | `#E3DFFF` | Bedtime and schedule tonal areas |
| onTertiaryContainer | `#1A1738` | Text and icons on tertiaryContainer |
| error | `#B3261E` | Destructive actions and failures |
| onError | `#FFFFFF` | Text and icons on error |
| errorContainer | `#F9DEDC` | Destructive confirmation surfaces |
| onErrorContainer | `#410E0B` | Text and icons on errorContainer |
| background | `#F4FBF8` | Window background |
| onBackground | `#161D1B` | Text on background |
| surface | `#F4FBF8` | Base surface |
| onSurface | `#161D1B` | Primary text and icons |
| surfaceVariant | `#DAE5E1` | Inactive tracks and subtle fills |
| onSurfaceVariant | `#3F4946` | Secondary text and icons |
| outline | `#6F7976` | Input borders, icon outlines, non-text separators |
| outlineVariant | `#BFC9C5` | Dividers and card borders when needed |
| surfaceDim | `#D4DCD8` | Dimmed surface, rarely used |
| surfaceBright | `#F4FBF8` | Bright surface, rarely used |
| surfaceContainerLowest | `#FFFFFF` | Sheets above cards |
| surfaceContainerLow | `#EEF6F2` | Cards |
| surfaceContainer | `#E8F0EC` | Bottom navigation, grouped sections |
| surfaceContainerHigh | `#E2EAE6` | Dialogs and menus |
| surfaceContainerHighest | `#DCE4E0` | Highest tonal layer |
| inverseSurface | `#2B322F` | Snackbars |
| inverseOnSurface | `#ECF2EF` | Text on snackbars |
| inversePrimary | `#82D5C8` | Action text on snackbars |
| scrim | `#000000` | Dialog and sheet scrim at 32 percent |

### Dark scheme

| Role | Value | Use |
| --- | --- | --- |
| primary | `#82D5C8` | Primary actions, selected states, links, progress |
| onPrimary | `#003731` | Text and icons on primary |
| primaryContainer | `#005048` | Selected navigation indicator, tonal icon circles |
| onPrimaryContainer | `#9EF2E4` | Text and icons on primaryContainer |
| secondary | `#B1CCC5` | Supporting actions and secondary labels |
| onSecondary | `#1C3530` | Text and icons on secondary |
| secondaryContainer | `#334B46` | Study mode chips and secondary tonal areas |
| onSecondaryContainer | `#CDE8E1` | Text and icons on secondaryContainer |
| tertiary | `#C6C1EA` | Schedule accents and time related controls |
| onTertiary | `#2F2C4D` | Text and icons on tertiary |
| tertiaryContainer | `#454263` | Bedtime and schedule tonal areas |
| onTertiaryContainer | `#E3DFFF` | Text and icons on tertiaryContainer |
| error | `#F2B8B5` | Destructive actions and failures |
| onError | `#601410` | Text and icons on error |
| errorContainer | `#8C1D18` | Destructive confirmation surfaces |
| onErrorContainer | `#F9DEDC` | Text and icons on errorContainer |
| background | `#0E1513` | Window background |
| onBackground | `#DDE4E1` | Text on background |
| surface | `#0E1513` | Base surface |
| onSurface | `#DDE4E1` | Primary text and icons |
| surfaceVariant | `#3F4946` | Inactive tracks and subtle fills |
| onSurfaceVariant | `#BEC9C5` | Secondary text and icons |
| outline | `#899390` | Input borders, icon outlines, non-text separators |
| outlineVariant | `#3F4946` | Dividers and card borders when needed |
| surfaceDim | `#0E1513` | Dimmed surface, rarely used |
| surfaceBright | `#343B39` | Bright surface, rarely used |
| surfaceContainerLowest | `#090F0E` | Sheets above cards |
| surfaceContainerLow | `#1A211F` | Cards |
| surfaceContainer | `#1E2523` | Bottom navigation, grouped sections |
| surfaceContainerHigh | `#232A28` | Dialogs and menus |
| surfaceContainerHighest | `#2D3432` | Highest tonal layer |
| inverseSurface | `#DDE4E1` | Snackbars |
| inverseOnSurface | `#2B322F` | Text on snackbars |
| inversePrimary | `#006A60` | Action text on snackbars |
| scrim | `#000000` | Dialog and sheet scrim at 48 percent |

### Status colors

These roles sit outside the Material 3 color scheme. Put them in `GuardianStatusColors` and provide them through a CompositionLocal.

| Role | Light | Dark | Use |
| --- | --- | --- | --- |
| success | `#1E6B3A` | `#8ED6A0` | Healthy status text and icons |
| successContainer | `#A5F3B8` | `#005228` | Healthy status fills |
| onSuccessContainer | `#00210F` | `#A5F3B8` | Text and icons on successContainer |
| warning | `#745B00` | `#E8C34A` | Attention text and icons |
| warningContainer | `#FFE08A` | `#574400` | Attention fills |
| onWarningContainer | `#241A00` | `#FFE08A` | Text and icons on warningContainer |

Usage rules:

- Success marks a healthy protection state. Do not use green for generic saves or confirmations. Snackbars use `inverseSurface`.
- Warning marks incomplete protection, a limit that has been reached, or an expiring session. It does not replace error.
- Error is reserved for destructive actions and failed operations, never for a normal limit.
- A switch uses `primary`, not success. Green switches look like a second kind of state.
- Status is always icon plus text. Color alone never carries meaning.

### Contrast check

Computed with the WCAG relative luminance formula on 5 November 2026. Normal text needs 4.5:1, large text and UI boundaries need 3:1.

| Pair | Light | Dark |
| --- | --- | --- |
| onSurface on surface | 16.32:1 | 14.32:1 |
| onSurfaceVariant on surface | 8.88:1 | 10.88:1 |
| primary on surface | 6.19:1 | 10.83:1 |
| onPrimary on primary | 6.50:1 | 7.73:1 |
| onPrimaryContainer on primaryContainer | 13.30:1 | 7.26:1 |
| secondary on surface | 6.18:1 | 10.85:1 |
| onSecondaryContainer on secondaryContainer | 13.19:1 | 7.26:1 |
| tertiary on surface | 6.14:1 | 10.76:1 |
| success on surface | 6.21:1 | 10.81:1 |
| onSuccessContainer on successContainer | 13.16:1 | 7.20:1 |
| warning on surface | 6.17:1 | 10.87:1 |
| onWarningContainer on warningContainer | 13.32:1 | 7.29:1 |
| error on surface | 6.23:1 | 10.83:1 |
| onError on error | 6.54:1 | 7.66:1 |
| outline on surface | 4.28:1 | 5.85:1 |

`outline` passes the 3:1 UI requirement but not the 4.5:1 text requirement in light mode. Use `outline` for borders and non-text icons only. Use `onSurfaceVariant` for secondary text.

### System bars

Use edge to edge. The status bar and navigation bar are transparent, and the content draws behind them with insets. Use dark system bar icons in the light scheme and light icons in the dark scheme. Never draw an opaque status bar that breaks the surface. The pause screen keeps both bars visible.

## Typography

Version 1 uses the system font family through `FontFamily.Default`. The platform font stack covers Latin, Greek, Cyrillic, and Arabic through system fallback without shipping font files. A custom brand font can wait until it earns its download size.

Use weights 400 and 500 only. Material 3 hierarchy comes from size and spacing, not from bold weight. Never use all caps.

| Token | Size and line height | Weight | Use |
| --- | --- | --- | --- |
| displaySmall | 36 sp / 44 sp | 400 | Today's total, status total |
| headlineLarge | 32 sp / 40 sp | 400 | Onboarding page title |
| headlineMedium | 28 sp / 36 sp | 400 | Pause screen title |
| headlineSmall | 24 sp / 32 sp | 400 | Child status hero |
| titleLarge | 22 sp / 28 sp | 500 | Top app bar, dialog title |
| titleMedium | 16 sp / 24 sp | 500 | Card title, row title |
| bodyLarge | 16 sp / 24 sp | 400 | Body copy, inputs |
| bodyMedium | 14 sp / 20 sp | 400 | Supporting copy, list detail |
| labelLarge | 14 sp / 20 sp | 500 | Buttons, navigation labels |
| labelMedium | 12 sp / 16 sp | 500 | Chips, captions, time labels |

Rules:

- Body text is at least 16 sp. Body 14 sp is only for supporting text that repeats information available nearby.
- Use tabular figures for durations and times. In Compose, set `fontFeatureSettings = "tnum"` on time styles.
- Do not shrink text to fit. Let the container grow, wrap, or scroll.
- At most three type sizes on one screen. More than that reads as noise.
- Use locale-aware number and time formatting. Do not hardcode AM, PM, or 24-hour text.
- Format durations as `45 min`, `1 h`, `1 h 20 min`, and `Less than 1 min`. Do not show `0 min` when there is no usage. Show `None today`.

## Spacing, shape, elevation

Spacing follows a 4 dp grid.

| Token | Value | Use |
| --- | --- | --- |
| space1 | 4 dp | Icon and label gap inside chips |
| space2 | 8 dp | Tight stacks, badge padding |
| space3 | 12 dp | Related rows, input gaps |
| space4 | 16 dp | Screen gutter, card padding, standard gap |
| space5 | 24 dp | Section gap, dialog padding |
| space6 | 32 dp | Large section gap, onboarding rhythm |
| space7 | 48 dp | Empty state breathing room |

Layout rules:

- Screen gutter is 16 dp. Use 24 dp above 600 dp width.
- Content width is capped at 640 dp and centered on larger screens.
- Section gap is 24 dp. Card padding is 16 dp.
- A one-line list row is at least 56 dp tall. A two-line row is at least 72 dp.
- Primary buttons are at least 48 dp tall and grow with their label. Secondary text buttons keep a 48 dp touch target even when the visual height is smaller.
- The bottom navigation bar is at least 80 dp plus the system inset. It grows if the font scale needs more room. Never clip a label.
- The top app bar is at least 64 dp plus the status bar inset. It grows with the title at large font scales.
- Do not use fixed heights for text containers. Font scaling must be able to grow them.
- Respect `WindowInsets`. No content sits under the status bar, navigation bar, or keyboard.

Shapes:

| Token | Value | Use |
| --- | --- | --- |
| extraSmall | 4 dp | Small badges, progress ends |
| small | 8 dp | Chips, compact fields |
| medium | 12 dp | Inputs, grouped rows |
| large | 16 dp | Cards, sheets, large buttons |
| extraLarge | 28 dp | Dialogs, bottom sheet top corners |

Buttons are fully rounded pills. Cards use 16 dp. Dialogs use 28 dp. A card at the edge of a group uses 16 dp on the outer corners and 4 dp on the inner corners only when rows are visually joined. In version 1, prefer separate cards over joined lists.

Elevation is tonal, not shadowed. Cards use `surfaceContainerLow`, navigation uses `surfaceContainer`, dialogs use `surfaceContainerHigh`. Shadows are reserved for the pause screen only if the host makes a floating surface necessary. Avoid elevation as decoration.

## Layout and navigation

The app has two modes.

Child mode is public. It opens to the status screen. It shows today's total screen time, active rules, protection checks, and a button to parent settings. It never shows per-app usage or editable controls.

Parent mode is private. It starts behind the PIN gate and uses four bottom tabs. Settings is a separate screen reached from the top app bar.

```
Cold start
  Status (no PIN)
    Parent settings -> PIN gate -> Parent shell
                                  Today | Limits | Schedules | Web
                                  Top bar -> Settings

Notification tap -> Status
Block screen -> Why is this paused? sheet
```

Routes:

| Route | Mode | Purpose |
| --- | --- | --- |
| `status` | Child | Public status and entry to parent mode |
| `gate` | Shared | Six digit PIN unlock |
| `onboarding/welcome` | Setup | Promise and limits |
| `onboarding/privacy` | Setup | What Guardian can and cannot see |
| `onboarding/pin` | Setup | Create and confirm the PIN |
| `onboarding/permissions` | Setup | One permission at a time |
| `onboarding/disclosure/{type}` | Setup | Prominent disclosure and consent |
| `onboarding/done` | Setup | Final checklist |
| `parent/today` | Parent | Today, 7 days, per-app usage |
| `parent/today/app/{package}` | Parent | App usage and limit |
| `parent/limits` | Parent | Limited apps |
| `parent/limits/picker` | Parent | Choose a launchable app |
| `parent/limits/edit/{package}` | Parent | Create or edit a limit |
| `parent/schedules` | Parent | Schedule list |
| `parent/schedules/edit/{id}` | Parent | Create or edit a schedule |
| `parent/web` | Parent | Filter status and custom domains |
| `parent/settings` | Parent | PIN, permissions, appearance, removal |
| `parent/settings/permissions` | Parent | Permission status and fixes |
| `parent/settings/remove` | Parent | Turn off protection and uninstall |

Navigation rules:

- Cold start always shows status. It never restores directly into parent mode.
- "Parent settings" opens the gate. A valid session opens the last parent tab. There is no silent bypass.
- The parent session locks after 5 minutes idle, when the app goes to the background, when the screen turns off, and on process death. The plan defines this. The gate covers the previous screen with an opaque surface. Set `FLAG_SECURE` on parent screens, or disable the recents screenshot on API 33 and newer, so the recents preview does not show parent content. Verify the recents preview on the phone at Step 3.
- If the session locks during an edit, keep the draft in memory and restore it after unlock. Use saved state so process death does not discard a draft either.
- System back at a parent tab root returns to status. Back from status exits the app. Back from onboarding goes one step back. Back from an editor asks before discarding changes.
- Tapping the selected tab scrolls its list to the top. Tapping another tab preserves each tab's scroll position.
- The gate and the pause screen are the only full screen surfaces without a top app bar.
- Do not add a floating action button. The bottom navigation plus a FAB crowds a phone screen. Use a full width tonal button at the top of a list for "Add".
- Predictive back is enabled. No custom back handling beyond navigation and unsaved change dialogs.

Bottom navigation:

| Tab | Icon | Label |
| --- | --- | --- |
| 1 | `Icons.Outlined.Today` / filled when selected | Today |
| 2 | `Icons.Outlined.Timer` / filled when selected | Limits |
| 3 | `Icons.Outlined.CalendarMonth` / filled when selected | Schedules |
| 4 | `Icons.Outlined.Public` / filled when selected | Web |

Labels are always visible. The active indicator uses `primaryContainer` with `onPrimaryContainer` content. Inactive content uses `onSurfaceVariant`. Each item has a 48 dp minimum target and a TalkBack label of the destination name.

Top app bar:

- Tab roots show the screen name in `titleLarge` and one settings icon with the content description "Settings".
- Detail screens show a back arrow and the screen name. Long names ellipsize with the full name in the accessibility label.
- Editors use a close or back arrow on the left and no text action on the right. The primary action lives in a sticky bottom bar.
- The status screen uses a brand row with the shield and "Guardian". It has no settings icon. The parent entry is a visible button in the content.

## Screens

### 1. Status

The status screen is the face of the app. It is readable by the child and useful to the parent.

Layout, top to bottom:

1. Brand row with a 28 dp shield and "Guardian" in `titleLarge`.
2. State hero. A tonal circle with a shield and check, then "Guardian is on" in `headlineSmall`. If an enabled check is broken, the circle uses `warningContainer`, the title becomes "Protection needs attention", and a one-line explanation follows.
3. Today card. Label "Screen time today" and the total in `displaySmall`. An "Estimated" chip appears when the usage ledger has a gap, with a help action that opens an explanation sheet.
4. Active rules. Up to three lines, each with a small icon. Examples are "Bedtime at 20:30", "Instagram limit 45 min", and "Website filter on". If there are more, add "and 2 more" as plain text. Do not show a full per-app list.
5. Protection checks. Three rows with icon, label, state, and an optional one-line reason. Rows are "App limits and schedules", "Website filter", and "Uninstall protection". State text is "On", "Off", or "Needs attention". If a row needs attention, the row explains the effect in plain words. Each row that needs attention has a "Review" action that opens the gate, not system settings.
6. Parent settings button. Filled tonal, full width, lock icon, label "Parent settings".
7. Footer. "Guardian does not send your data to a server. No account. No ads." in `bodyMedium` and `onSurfaceVariant`.

Status model. Each check has a parent choice and a system reality.

| Parent choice | System reality | Row state | Hero effect |
| --- | --- | --- | --- |
| On | Working | On | None |
| On | Missing or broken | Needs attention | Warning |
| Off | Not applicable | Off | None |
| Not set up yet | Onboarding incomplete | Hidden | Finish setup |

Hero icon by state. On uses a shield with a check in `successContainer`. Attention uses a warning triangle in `warningContainer`. Off uses a shield outline in `surfaceContainer`. Setup uses a shield with a plus in `primaryContainer`.

An off check is neutral, not a failure. A parent who turns off the website filter should not get a permanent warning badge. A check that is on but broken is a real problem and updates the notification. A feature that has not shipped yet is hidden, not shown as off. This keeps the Step 2 skeleton honest before the filter and Device Admin exist.

Behavior:

- The hero is healthy when every check the parent turned on is working. It becomes "Guardian is off" when no checks are on. It becomes "Finish setting up Guardian" when onboarding is incomplete.
- There is no pull to refresh. Local state updates on resume and on a slow tick. Do not add a refresh control for local data.
- If setup is incomplete, the primary button becomes "Finish setup". It opens the gate, then onboarding.
- Each "Review" action passes through the gate, then opens the matching fix screen or the affected tab. The child sees a PIN prompt and stops.
- Do not list every app with a limit. Show at most three active rules and a count for the rest.
- The status screen is excluded from the block engine and is always reachable.

Hero copy:

| State | Title | Body |
| --- | --- | --- |
| Healthy | Guardian is on | Protection is running for the checks you turned on. |
| One issue | Protection needs attention | The website filter stopped. Adult sites can load until it is back on. |
| Several issues | Protection needs attention | 2 checks need attention. Tap Review to fix them. |
| All off | Guardian is off | No protection checks are on. |
| Setup incomplete | Finish setting up Guardian | 3 checks are left before protection is complete. |

Today card copy:

| State | Label | Value |
| --- | --- | --- |
| No usage yet | Screen time today | None today |
| Estimated | Screen time today | 2 h 15 min, estimated |

### 2. Parent gate

Layout:

- Centered column, max width 400 dp, 24 dp gutter.
- A 48 dp lock icon in a `surfaceContainer` circle.
- Title "Parent settings" in `headlineMedium`.
- Body "Enter your 6 digit PIN." in `bodyLarge`.
- A real PIN field with `KeyboardType.NumberPassword`, six digits, masked. Do not draw a custom keypad. The system keyboard handles accessibility, paste, and password managers.
- Six small dots below the field show progress. TalkBack reads "PIN, 3 of 6 digits entered", never the digits.
- A full width "Unlock" button, enabled at six digits. Auto-submit is off so screen reader users can review the entry.
- A text button "Forgot PIN?" appears only after the recovery decision. If there is no recovery path, omit it rather than showing a dead end. Never put a bypass hint at the gate.
- A subtle footer "Guardian locks after 5 minutes." in `bodyMedium`.

States:

- Wrong PIN. The field clears, focus returns to the field, and an inline error appears. Copy "That PIN is not correct. Try again." Announce it through a live region. A short shake runs only when animations are enabled.
- Lockout. The field and button are disabled. Copy "Too many tries. Try again in 1 minute." with a visible countdown that updates once per second. TalkBack announces the wait at 30 seconds, 10 seconds, and the end, not every second. Do not reveal how many attempts remain.
- Session locked. Title "Guardian is locked", body "Enter your PIN to continue where you left off." The previous screen stays hidden behind the gate.
- Session warning. At 30 seconds before the idle lock, show a snackbar "Guardian will lock in 30 seconds." Any tap or keypress extends the session. Do not use a modal.
- First run. If no PIN exists, route to onboarding instead of showing the gate.
- Hashing. The button shows a small progress indicator and "Checking" while PBKDF2 runs. Do not block the main thread. Do not log the PIN or the hash.

Security copy rule. Never place a hint, a recovery answer, or a "skip" action on the gate. State the length once in the instruction, and never show the digits.

### 3. Onboarding

Five steps with a progress label "Step 2 of 5" and a thin linear progress bar. The top app bar has a back arrow and no other action. The primary button is full width at the bottom, above the system inset.

Step 1, welcome:

- Title "A calmer phone, with clear limits"
- Body "Guardian sets daily app limits, bedtime and study hours, and a website filter for this phone."
- Body "Guardian keeps your rules and usage on this phone. It has no account and no server."
- Primary "Get started"

Step 2, privacy:

- Title "What Guardian can see"
- Three short blocks with icons.
- "It can see which app is open and for how long."
- "It can check website domain names on this phone when the filter is on. Allowed lookups go to your network's DNS resolver, not to Guardian."
- "It cannot read messages, photos, passwords, or what you type. It does not use the microphone or camera."
- A link "Read the privacy policy" opens the privacy page. Until the public URL exists at Step 11, it opens the in-app summary. Never ship a dead link.
- Primary "I understand"
- Do not offer a "Read later" action on this step. The privacy policy stays reachable from Settings. Consent for sensitive permissions happens later, separately.

Step 3, PIN:

- Title "Create a parent PIN"
- Body "This PIN protects every setting. You will need it to change limits or remove Guardian."
- Two fields, "PIN" and "Confirm PIN", six digits each, masked, number keyboard.
- Reject all-same digits and simple sequences such as 123456. Copy "Choose a less predictable PIN."
- Warning card in `warningContainer`. "There is no PIN reset in this version. Keep this PIN somewhere safe." If Nor adds a recovery code before Step 3, replace this with the recovery code step.
- Primary "Continue"
- Do not ask for a name, email, birthday, or child profile.

Step 4, permissions:

- Title "Turn on protection"
- Body "Android asks for each permission separately. You can skip a check and finish it later. That check stays off until you return."
- A list of permission cards. Each card shows icon, name, one-line reason, state, and "Set up".
- Order from least to most sensitive. Usage access, notifications, Accessibility, VPN filter, Device Admin.
- Each "Set up" opens the matching disclosure screen. The disclosure has a checkbox and a continue button. The checkbox is never pre-checked.
- After the system settings screen, returning to Guardian updates the row. The session may be locked. If so, the gate appears with "Guardian is locked. Your setup progress is saved."
- Primary "Continue" at the bottom. Secondary text "Finish later" opens a dialog that names the missing checks. It does not pretend protection is complete.

Step 5, done:

- Title "Guardian is ready" when all three checks are on. Otherwise "Guardian is partly set up".
- A checklist with the same states as the status screen.
- If checks are missing, body "2 of 3 checks are on. You can finish the rest in Permissions.", primary "Finish setup" returns to step 4, and secondary "Open status" is available.
- If all checks pass, primary "Open Guardian" goes to status.
- Body "You can change everything later in Parent settings."
- Do not celebrate with confetti. A calm check icon is enough.

Permission disclosure screens are full screens, not bottom sheets. Play policy requires a prominent in-app disclosure that is not buried. The disclosure body must not hide behind an expander. Scrolling is allowed on a 320 dp wide phone at 200 percent font, but the consent control always comes after the body and never floats over it. The consent control is a checkbox plus a button that names the action, for example "Open Accessibility settings". A "Not now" text button is always available.

Draft disclosure copy:

| Permission | Disclosure |
| --- | --- |
| Usage access | "Guardian reads how long apps are in the foreground so it can show screen time and enforce daily limits. It does not read what you do inside apps. Android opens a settings screen where you grant this access." |
| Notifications | "Android requires a visible notice while Guardian protects the phone. The notice shows whether protection is on and opens the status screen. It does not show app names or personal data." |
| Accessibility | "Guardian uses Accessibility to notice which app is open. When a limit or schedule applies, Guardian shows a pause screen instead. Guardian does not read text on the screen, watch taps, type for you, or control other apps. You can turn this off at any time in Android Settings." |
| VPN filter | "Guardian uses a local VPN to check website domain names against its block list. The check happens on this phone. Guardian does not send your browsing to a Guardian server. Allowed domain lookups go to your network's DNS resolver. Guardian does not inspect page content and does not hide your IP address. Android shows a VPN key while the filter runs." |
| Device Admin | "Guardian uses Device Admin so the app cannot be uninstalled without your PIN. It does not wipe the phone, lock the screen, or change your password. You can remove Guardian later with your PIN." |

Consent text under each checkbox. "I understand what this permission does and I want to turn it on." The consent timestamp is stored in DataStore. Do not reuse one consent for another permission.

### 4. Today

Parent tab. Top app bar "Today" with the settings icon.

Layout:

1. Date line, "Wednesday, 5 November", in `bodyMedium` and `onSurfaceVariant`.
2. Hero card. "Screen time today" label, total in `displaySmall`, and an "Estimated" chip if needed. Do not add a comparison to yesterday. Comparison invites judgment and adds noise.
3. Week card. Title "Last 7 days", a 7 bar chart, and the weekly total in `titleMedium`. Bars use `primary` for today and `primaryContainer` for other days. No grid lines. Day labels use two letters, for example "Mo". Order follows the locale's first day of the week.
4. Section "Apps today" with a count. Rows are sorted by time descending.
5. App row. App icon 40 dp, name in `titleMedium`, duration in `bodyMedium`, and a limit line. If a limit exists, show "35 of 45 min" and a 4 dp progress bar. If the limit is reached, add a warning chip "Limit reached" with an icon. If there is no limit, show "No limit" in `onSurfaceVariant`.
6. Tap an app row to open the app detail.

States:

- Usage access missing. Replace the hero and chart with a `ProblemCard` that explains the missing access and offers "Open settings". Do not show zeros as if they were real.
- No usage today. Hero shows "None today". The app list shows an empty state, "No screen time recorded yet today."
- Loading. Local reads are fast. Show the previous content with a subtle progress bar at the top, not a full screen spinner. On first load, show a skeleton only if the read takes longer than 300 ms.
- Estimated. Keep one number. Add the chip and a help action. The sheet explains that the monitoring service was off, so Guardian filled the gap from Android's usage data. Never show two competing totals.

Chart accessibility. The chart has a single text alternative with the weekly total and each day's value. Each bar is not focusable. The chart is not interactive in version 1.

### 5. App detail

Header with app icon 48 dp and the app name in `headlineSmall`. Do not show the package name. It is noise for a parent.

Layout:

1. Two metric cards side by side, "Today" and "This week". Values in `headlineMedium`. On narrow screens or at 200 percent font, stack them.
2. Limit card. If a limit exists, show the limit in `titleMedium`, the usage line, and a progress bar. Actions "Edit limit" and a text button "Remove limit". Removing asks for confirmation and names the app.
3. If no limit exists, body "No daily limit. This app can run until a schedule pauses it." Primary "Set daily limit".
4. A small explainer, "Limits reset at midnight. Schedules can still pause the app." in `bodyMedium`.
5. If the app is always allowed, it should not reach this screen from the limits flow. If it is opened from usage history, show an info card "This app is always allowed. Limits do not apply." and hide the limit actions.

Limit editor:

- Title "Daily limit" with the app name in the supporting line.
- Preset chips, 15 min, 30 min, 45 min, 1 h, 1 h 30 min, 2 h.
- A custom field with a number keyboard, minutes, 5 to 720. Supporting text "5 minutes to 12 hours".
- A live summary, "Instagram pauses after 45 minutes each day."
- Save in a sticky bottom bar. Back with changes asks "Discard changes?"
- Enabling and disabling uses a switch labeled "Limit this app". The switch saves with the form, not immediately, so a mistaken tap is reversible before Save.
- No time-of-day limit in version 1. Schedules cover time windows.

### 6. Limits

Top app bar "Limits" with settings.

Layout:

1. Intro "Daily limits reset at midnight." in `bodyMedium`.
2. Full width tonal button "Choose an app" with a plus icon.
3. A section "Apps with limits" and a count.
4. Limit row. App icon, name, "35 of 45 min", progress bar, and a warning chip when reached. Tap opens the editor.
5. Empty state. Shield icon in a tonal circle, title "No app limits yet", body "Choose an app and set a daily limit. Limits reset at midnight.", action "Choose an app".

App picker:

- Search field at the top with a clear action. Search matches app names, case insensitive, locale aware.
- Alphabetical list of launchable apps. Exclude Guardian, the default launcher, the dialer, System UI, and the clock. Keep apps that already have a limit in the list, show a check, and open their editor instead of creating a duplicate.
- Each row is at least 56 dp, with a 40 dp icon and the app name.
- Empty search state, "No apps match "x"." with a clear search action.
- Loading apps can take a moment on first launch. Show a centered progress indicator with the label "Loading apps".

Rules:

- One limit per package. The package name is the key.
- A limit of 0 is invalid. The minimum is 5 minutes.
- Deleting a limit keeps the usage history. It only removes the rule.
- Do not offer a "block always" toggle. Schedules cover blocking windows, and an always-blocked app is a support problem, not a feature.

### 7. Schedules

Top app bar "Schedules" with settings.

Layout:

1. Intro "Schedules pause apps at set times." in `bodyMedium`.
2. Full width tonal button "Add schedule".
3. Schedule cards. Each card has a 40 dp tonal icon circle, the name in `titleMedium`, the days and time in `bodyMedium`, a mode chip, and a switch with the content description "Bedtime enabled". Tapping the card opens the editor. The switch saves immediately.
4. Mode chip. "Pause all apps" uses `tertiaryContainer`. "Study time" uses `secondaryContainer`.
5. Overlap note. If two enabled schedules overlap, show a small info line on the later card, "Overlaps Bedtime. The stricter schedule applies." This is informational and does not block saving.
6. Empty state. Title "No schedules yet", body "Add bedtime or study hours. Apps pause automatically during a schedule.", action "Add schedule".

Schedule editor:

- Top app bar with back and title "New schedule" or the schedule name.
- Name field, 1 to 40 characters, default "Bedtime" for a new schedule.
- Day selector. Seven chips in a wrap layout, labels "Mo Tu We Th Fr Sa Su". The full day name is the accessibility label. At least one day is required.
- Time range. Two fields, "Starts" and "Ends", each opens the Material time picker. Use the system 12 or 24 hour format. A live summary below, "Monday to Friday, 20:30 to 07:00, ends the next day."
- Mode selector. Two segmented buttons, "Pause all apps" and "Study time". Study time reveals an allowed apps section.
- Allowed apps for study. A list with add and remove. Empty state "No study apps yet. Add the apps that should stay open." At least one app is required for study mode. The child can still use the dialer, launcher, clock, and Guardian.
- Enabled switch. New schedules are enabled by default.
- Save in a sticky bottom bar. Back with changes asks to discard.
- Delete on an existing schedule at the bottom, in error color, with a confirmation that names the schedule and says "Apps will follow the remaining rules."
- Validation. End time equal to start time is invalid. Copy "Start and end times must be different." A cross-midnight range is valid and labeled "Ends the next day". The equal-time rule already prevents a 24 hour range, so no separate all-day warning is needed.

### 8. Web

Top app bar "Web" with settings.

Layout:

1. Status card. Healthy state uses `successContainer` with a shield icon, "Website filter is on", and body "Adult sites and your blocked sites are filtered on this phone." If the parent turned the filter off, use `surfaceContainerLow` with an info icon, "Website filter is off", and body "Adult sites and your blocked sites can load. App limits and schedules keep working." If a permission was revoked or the VPN stopped, use `warningContainer` and the problem copy. If another VPN is active, show that problem instead of a false "on" state.
2. Filter switch row. Label "Filter websites", supporting text "Uses a local DNS check. Does not inspect page content." The switch is `primary`.
3. Custom sites section. Title "Blocked sites" and a count. Rows show the domain in `bodyLarge`, with a remove icon button that has the content description "Remove example.com". Remove asks for confirmation. Empty state "No custom sites. Guardian blocks its adult site list."
4. Add site. A full width outlined button "Add a site" opens a small screen with a text field, helper "Enter a domain like example.com. Subdomains are included. Do not include https:// or a path.", and Save. Inline errors for an invalid or duplicate domain. Normalize to lowercase ASCII and strip a leading `www.` only if the stored list expects it. Show the normalized value before saving.
5. Adult site list info. "Adult site list, version 2026.11, 12,345 domains." The version and count come from the asset metadata. A "How filtering works" text button opens a sheet.
6. Limits explainer. Keep the key caveat visible, not behind an expander. A `surfaceContainerLow` card with the title "What the filter cannot do" and body "Some apps and browsers use their own DNS or a direct connection and can bypass the filter. Guardian does not inspect HTTPS traffic and cannot see page content. It does not promise perfect blocking."
7. Problem states. VPN off, another VPN active, or no network. The status card is replaced by the matching `ProblemCard` with one action.

Turning the filter off:

- Confirmation dialog. Title "Turn off the website filter?" Body "Adult sites and your blocked sites will load until you turn it back on. App limits and schedules keep working." Confirm action "Turn off" in error color, cancel focused by default.
- After turning it off, the status screen shows the filter as Off and the notification says the filter is off. Do not leave a green "Website filter is on" state.

Domain rules:

- Accept a bare domain or a full URL and normalize it. Strip a leading `www.` before saving because the stored domain already covers its subdomains. Reject spaces, a missing dot, a path-only value, and values longer than 253 characters.
- A stored domain blocks that domain and its subdomains. Enter `example.com` to block all of it, or `ads.example.com` to block only that subdomain. Say this in the helper text.
- Store punycode for international domains. Display the Unicode form when available.
- The shipped adult list is read only. The parent cannot edit or delete it. A custom domain that duplicates the shipped list is allowed but shown as "Already on the adult list." Do not create a duplicate row.

### 9. Settings

Top app bar "Settings" with back. Grouped cards with section headers in `titleMedium`.

Parent PIN:

- "Change PIN" opens a flow. Current PIN, new PIN, confirm new PIN. Current PIN is required even with an active session. Copy "Enter your current PIN to change it."
- "Lock now" clears the session and returns to the gate. This is useful when handing the phone back.

Protection:

- "Permissions" opens the permission list with states and fixes.
- "Uninstall protection" shows the Device Admin state and opens the disclosure and system screen. Copy "Requires your PIN to turn off or uninstall Guardian."
- "Website filter" opens the Web tab. Optional shortcut.

Appearance:

- "Theme" with three options, System, Light, Dark, as a radio list. The current choice is marked with a check, not color alone.
- No dynamic color option in version 1.

Privacy:

- "Privacy policy" opens the public page when it exists, otherwise the in-app summary. Never a dead link.
- "What Guardian can see" opens the in-app privacy summary from onboarding.
- "Data on this phone" explains that limits, schedules, domains, usage, and the PIN hash stay on the device, and that clearing app data deletes them. It also says that allowed DNS lookups go to the network resolver when the filter is on. No export in version 1.

About:

- Version and build number.
- Open source licenses.
- "Remove Guardian" at the bottom in a destructive card. It opens the removal screen.

Do not add rate prompts, share actions, social links, or a newsletter.

Removal screen, final behavior at Step 9:

- Title "Remove Guardian".
- Body "This turns off app limits, schedules, the website filter, and uninstall protection. The PIN, rules, and usage history on this phone are deleted."
- Primary destructive action "Turn off and remove".
- Confirmation always requires the PIN. The session alone is not enough for removal.
- Deactivate Device Admin, stop the services, and open the system uninstall screen. Android removes the app data with the uninstall. If the parent cancels the uninstall, the app remains with protection off and shows a clear status.
- A secondary "Keep Guardian" returns to Settings.

### 10. Permissions

Top app bar "Permissions" with back.

Layout:

1. If any enabled check is missing a permission, a `ProblemCard` at the top, "Protection is incomplete", "2 permissions need attention."
2. Rows in this order, Usage access, Notifications, Accessibility, Website filter, Uninstall protection, Battery optimization.
3. Each row has a 40 dp tonal icon, title, one-line reason, status chip, and action. Status is "On", "Off", or "Needs attention". The action is "Open settings" or "Turn on".
4. A "Why Guardian needs this" expandable for each row, with two or three sentences and a link to the privacy policy.
5. Sensitive rows open the same disclosure screen used in onboarding. Consent is recorded separately for each permission. If consent exists but the system permission is off, do not show the disclosure again. Show the reason and the system action.
6. Battery optimization is optional but recommended. Copy "Some phones stop background apps. Allow Guardian to stay active so limits and schedules keep working." Action "Open battery settings". Do not claim the app is exempt until the system says so.
7. Notifications on Android 13 and newer use the system permission dialog after the in-app explanation. If denied, the row explains that the ongoing notice may be hidden and protection may stop.

Status is always derived from the system on resume. Never cache an "On" state that the system has revoked.

### 11. Pause screen

The pause screen replaces a blocked app. It is one Compose surface with two possible hosts, an accessibility overlay or a full screen activity. Step 6 chooses the host with `/architect`. The design is the same for both.

Layout:

- Full screen `surface` background. Do not hide the status bar or the navigation bar. The child must always see the clock, battery, and system controls.
- 24 dp gutter, max width 480 dp, vertically centered when space allows, scrollable when the font is large or the screen is short.
- A 72 dp circle in `primaryContainer` with the blocked app icon at 40 dp, or a shield icon when the block comes from a schedule. Do not use the app's full color as the background.
- Title in `headlineMedium`. The app name is allowed. "Instagram is paused" or "Apps are paused".
- Body in `bodyLarge`. Name the reason and the time.
- A small `surfaceContainerLow` card with the reset fact. "Starts again tomorrow" for a daily limit. "Ends at 07:00" or "Ends tomorrow at 07:00" for a schedule.
- Primary button "Go to Home" with a home icon. Full width, 48 dp.
- Secondary text button "Why is this paused?" opens a bottom sheet with the rule and a short explanation. The sheet says a parent can change it in Guardian. It does not show a PIN field or a bypass.
- Supporting line "Calls and the clock still work." in `bodyMedium`, always visible.
- No countdown to the second. A minute-level countdown adds pressure and battery work. Show the end time.
- No "Ask for more time" button in version 1. There is no remote approval flow.

Copy:

| Reason | Title | Body | Reset card |
| --- | --- | --- | --- |
| Daily limit | Instagram is paused | You reached today's 45 minute limit. | Starts again tomorrow |
| Bedtime, pause all | Apps are paused | It is bedtime. Apps are paused until 07:00. | Ends at 07:00 |
| Study time, allow only | It is study time | Only your study apps are open until 17:00. | Ends at 17:00 |
| Study time, app not allowed | Instagram is paused | It is study time. This app opens again at 17:00. | Ends at 17:00 |
| Overlapping rules | Apps are paused | A schedule is active until 07:00. | Ends at 07:00 |

Behavior:

- Back and the Home button always leave the pause screen. Never intercept the system Home gesture.
- Opening recents does not bypass the rule. On return, the accessibility service evaluates again and shows the pause screen if the rule still applies.
- The pause screen appears once per transition into a blocked app, not on every frame. Announce the title once through a live region. Do not repeat the announcement when the screen recomposes.
- If the rule ends while the pause screen is visible, dismiss it and let the child return to the app. If the app is no longer in the foreground, simply dismiss.
- If the parent removes the limit while the child waits, the pause screen dismisses on the next evaluation.
- The pause screen must never appear over the dialer, the emergency dialer, the launcher, System UI, the clock, or Guardian. The policy engine enforces this before the UI.
- If two rules apply, show the most restrictive reason and its end time, as the policy engine decides.
- TalkBack focus starts on the title, then the body, then "Go to Home", then "Why is this paused?". The sheet keeps focus while open and returns focus to "Why is this paused?" on close.
- At 200 percent font, the content scrolls. Keep the Home button in a sticky bottom area above the system inset, and let the text scroll above it. A child must never have to hunt for the exit.

### 12. Problems and notifications

A `ProblemCard` has a severity, icon, title, body, and one action. It appears at the top of the status screen, the Today screen, and the affected tab. The same problem appears in the notification text.

Severities:

- Blocking. Protection is not working. Use `warningContainer` for the card, not error. Reserve error for destructive actions and crashes. The icon is a warning triangle. The action is a fix.
- Informational. A setting is off but protection is not broken. Use `surfaceContainerLow` and an info icon.
- Healthy. Use success only in the status hero and check rows.

Problem table:

| Problem | Title | Body | Action | Severity |
| --- | --- | --- | --- | --- |
| Usage access off | Screen time is paused | Guardian cannot read app time without Usage access. Daily limits are not being enforced. | Open settings | Blocking |
| Accessibility off | App limits are not working | Guardian cannot notice which app is open. Limits, schedules, and the pause screen need Accessibility. | Turn on | Blocking |
| VPN off | Website filter is off | Android stopped the filter. Adult sites and your blocked sites can load. | Fix filter | Blocking |
| Another VPN active | Another VPN is on | Android allows one VPN at a time. Turn off the other VPN to use the website filter. | Open VPN settings | Blocking |
| Notification permission off | Protection notice is hidden | Android can hide the ongoing notice. Protection may stop without it. | Show notice | Warning |
| Battery optimization on | Protection may stop | This phone may stop Guardian in the background. Allow it to run without battery limits. | Open battery settings | Warning |
| Device Admin off | Uninstall protection is off | Guardian can be removed without the PIN. | Turn on | Warning |
| Service stopped | Protection is stopped | Android stopped Guardian. Open the app to restart protection. | Restart | Blocking |
| Setup incomplete | Finish setting up Guardian | 3 checks are left before protection is complete. | Finish setup | Warning |

The notification is the always-on proof of protection.

- One channel, "Protection status", importance LOW, no sound, no vibration. It is ongoing while protection runs.
- Small icon is the shield silhouette. Color is the system's notification accent, not a custom color.
- Normal title "Guardian is on". Normal text "Tap to see status." If a check is off by choice, name it in the text, for example "Guardian is on. Website filter off." Do not list app names or the child's name on the lock screen.
- Problem title "Protection needs attention". Problem text names one problem, for example "Website filter stopped. Tap to fix." A chosen off state does not use the warning title.
- The notification updates in place. Use `setOnlyAlertOnce`. Do not create a second notification for each problem.
- Tapping opens the status screen. A problem notification may open the status screen with the matching problem expanded. Never open a system settings screen directly from the notification.
- Do not add a "Turn off" action. The parent turns protection off inside Settings.
- The notification remains after a reboot when protection restarts. If protection cannot restart, show a problem notification instead of a false "on" state.
- The channel name and description are visible in Android settings. Use "Guardian protection status" and "Shows whether app limits, schedules, and the website filter are active."

### 13. Dialogs, sheets, and feedback

- Confirmation dialogs are for destructive actions only. Saving, navigating, and turning a normal switch on do not need a dialog.
- Destructive dialog actions put "Cancel" first and focus it by default. The destructive action uses `error`. The title names the object, for example "Remove the Instagram limit?".
- Bottom sheets are for explanations and the "Why is this paused?" details. Forms are full screens.
- Snackbars confirm a completed action, for example "Limit saved." or "Site removed." They appear above the navigation bar and system inset. One line, no action unless an undo exists. In version 1, deletion uses a confirmation instead of undo.
- No toast messages.
- Loading local data uses a top progress bar or a skeleton after 300 ms. Do not flash a spinner for a Room read.
- Empty states always have a title, one sentence, and one action. They never blame the user.
- Inline errors sit under the field, use `error`, and are announced. Do not use a dialog for a validation error.
- Save buttons stay enabled. Validate on tap, show the first error, and move focus to it. A disabled button with no explanation is a dead end.

## Components

Build these once in `ui/components` and reuse them. Each component owns its accessibility semantics.

| Component | Contract |
| --- | --- |
| `GuardianTopBar` | Title, optional back, optional settings action. Title ellipsizes, full name in the accessibility label. |
| `StatusHero` | Healthy, attention, and setup states. Icon plus title plus body. Announces state changes once. |
| `MetricCard` | Label, value, optional chip. Uses tabular figures. Stacks at 200 percent font. |
| `ProtectionRow` | Icon, label, state text, optional reason, optional action. State is never color only. |
| `WeekChart` | Seven bars, locale day order, one text alternative. Not focusable per bar. |
| `AppUsageRow` | Icon, name, duration, limit progress, reached chip. Minimum 72 dp for two lines. |
| `LimitProgress` | Determinate bar with `progressBarRangeInfo` and a text equivalent. |
| `ScheduleCard` | Name, days, time range, mode chip, enabled switch. Whole card opens the editor except the switch. |
| `DomainRow` | Domain, optional "On adult list" label, remove action with a named content description. |
| `PermissionRow` | Title, reason, status, action. Status is derived from the system on resume. |
| `DisclosureScreen` | Full screen, prominent body, unchecked consent, named continue action, "Not now". |
| `ProblemCard` | Severity, title, body, one action. Blocking problems use warning, not error. |
| `EmptyState` | Tonal icon circle, title, one sentence, one action. |
| `PinField` | Six digit masked field, progress dots, accessible count, error state, clear on error. |
| `ConfirmDialog` | Title, body, cancel first, destructive action in error color. |
| `DaySelector` | Wrap layout, two letter labels, full day names for TalkBack, 48 dp targets. |
| `TimeRangeField` | Start and end, system time format, cross-midnight summary. |
| `ModeSelector` | Two segmented buttons, selected state announced, no color-only selection. |
| `SectionHeader` | Title and optional count or action. `titleMedium`. |
| `InlineError` | Error text under a field, announced once. |
| `StickyActionBar` | One primary action above the system inset, optional secondary text action. |

Component rules:

- Minimum touch target is 48 by 48 dp. WCAG 2.2 AA requires 24 by 24 CSS pixels as a minimum, and Android guidance is 48 dp. Guardian uses the stricter Android value.
- Icon-only buttons need a content description that names the object, not just the verb. "Remove example.com", not "Remove".
- Switches use `Role.Switch` with an `on` state and a state description. The row label names the setting.
- Progress bars expose a percentage or a time range. A bar with no text alternative is decoration.
- Cards are not clickable unless they navigate. If a card contains a switch and a tap target, separate the two semantics clearly.
- Selected states use shape, icon, and text, not only color.
- Long app names use one line with ellipsis in dense rows and two lines elsewhere. The full name is always in the accessibility label.

## Motion

- Standard transitions are 200 ms with `FastOutSlowInEasing`.
- The pause screen fades in over 150 ms. No bounce, scale, or slide from the edge.
- Progress bars animate over 300 ms when the value changes. Snap when the system animation scale is off.
- The PIN error may shake for 200 ms. The inline error and live announcement are the real feedback.
- No looping animation, pulsing shield, confetti, or celebratory motion.
- Respect `Settings.Global.ANIMATOR_DURATION_SCALE`. If animations are off, all transitions become instant.
- Motion never carries information that is not also in text.

## Accessibility

The bar is WCAG 2.2 Level AA plus Android platform guidance.

Text and color:

- Normal text meets 4.5:1. Large text and UI boundaries meet 3:1. The palette table records the measured pairs.
- Secondary text uses `onSurfaceVariant`, never `outline`.
- Text scales to 200 percent without clipping, overlap, or horizontal scrolling. Layouts grow and scroll instead.
- Do not disable font scaling with fixed `sp` overrides or `maxLines` on essential copy.

Touch and input:

- Every target is at least 48 by 48 dp. Adjacent targets have at least 8 dp between them.
- No gesture is the only way to do something. No swipe to delete, no long press to edit, no drag to reorder.
- All buttons are reachable with a switch access or keyboard. Focus order follows visual order.
- Focus is visible with a 2 dp `primary` outline or an equivalent tonal change.
- Sticky action bars and the keyboard do not obscure the focused field. Scroll the field into view.

Screen readers:

- Every control has a label, a role, and a state. Decorative icons are hidden with `contentDescription = null`.
- The pause screen uses a pane title. TalkBack focuses the title first, the body second, and the exit action.
- The PIN field announces "PIN, 1 of 6 digits entered" and never the digits. Errors are announced through a live region.
- The chart has one summary plus one description per day. It is not a focusable bar chart.
- Status changes are announced once. Do not announce every second of a countdown.
- Dialogs and sheets trap focus, have a labelled close action, and return focus to the opener.
- Headings use `heading()` semantics so TalkBack can jump between sections.

Time and errors:

- The parent session gives a 30 second warning before it locks while the screen is visible. The warning is text and a live announcement. It does not steal focus.
- If a session locks during an edit, the draft is restored after unlock. Do not discard data.
- Errors say what happened and what to do. No codes, no "invalid input" without a reason.
- A timeout or lockout message states the wait time in plain words.

Safety:

- The pause screen never hides the status bar, the navigation bar, or the system Home control.
- Calls and emergency calls are never blocked by Guardian. The always-allowed policy is evaluated before any UI.
- The child status screen never requires a PIN.
- Parent data is never visible behind a locked gate. The gate surface is opaque.

## Design QA

Run this checklist at Step 2 and at every UI step after it. A UI step is not done until the checklist passes on the real phone.

| Check | Pass condition |
| --- | --- |
| Contrast | Every text pair meets 4.5:1, or 3:1 for large text and UI. Check both themes with Accessibility Scanner or a contrast tool. |
| Font scaling | Set display size and font size to the largest setting. Every screen scrolls, no text clips, no action is unreachable. |
| TalkBack | Complete onboarding, set a limit, add a schedule, add a domain, unlock the gate, and read the status screen with TalkBack only. Every control is named and every state is announced. |
| Touch targets | Every action has a 48 dp target. No two destructive actions sit next to each other. |
| Themes | Light and dark both look intentional. No pure black or pure white flash on launch. |
| Pause screen | Home, Back, recents, calls, and TalkBack all work. The exit is visible without scrolling at 200 percent font. |
| PIN gate | Wrong PIN, lockout, session timeout, rotation, process death, and draft restore all behave. No PIN appears in logcat or screenshots. |
| Empty states | Every list has a useful empty state with one action. No blank screens. |
| Long content | Long app names, 40 character schedule names, long domains, and 12/24 hour formats do not break layout. |
| Insets | No content under the status bar, navigation bar, keyboard, or cutout. Gesture and 3 button navigation both work. |
| Narrow screen | 320 dp width is usable. No horizontal scroll. |
| Color independence | Turn on grayscale or use a color blindness simulator. Every status is still readable. |
| Reduced motion | Turn animations off. No transition depends on motion, and the PIN error still shows text. |
| Notification | Collapsed and expanded notice are readable, show no personal data, and open status on tap. |
| Charts | TalkBack reads the weekly summary and each day. Sighted and screen reader users get the same facts. |
| Destructive actions | Cancel is focused first, the object is named, and the result is explained before the action. |
| Strings | No hardcoded user-facing text. No unlabelled icon buttons. No placeholder lorem ipsum. |

## Open design questions

1. PIN recovery. The gate and the setup warning depend on the Step 3 decision. If a one-time recovery code is added, the setup step gains a third field and a "Store this code" screen. The gate gains a "Use recovery code" action. Do not design a hint or a security question.
2. Parent session during permission handoff. The plan locks the session when the app goes to the background. That means a PIN re-entry after every system settings trip during onboarding. If this feels too heavy on the phone, consider a short handoff grace that is visible and expires. Do not weaken the lock silently.
3. Pause screen host. An accessibility overlay and a full screen activity differ in system bar behavior, animation, and recents appearance. Step 6 decides with `/architect`. The layout above fits both.
4. Settings guard. Step 9 decides whether Guardian shows a PIN screen over Settings or uses another guard. Reuse `PinField` and the gate copy.
5. Localization. Version 1 ships English. All strings live in resources, layouts use start and end, and icons mirror where direction matters. Adding Arabic later should not require a redesign. Confirm the store languages before Step 11.

## Handoff to Step 2

- Create `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, and `Theme.kt` from the tables above.
- Do not enable dynamic color. Do not add a custom font.
- Add `GuardianStatusColors` with a CompositionLocal and preview both themes.
- Build the status screen, gate, onboarding shell, and the four tab destinations with real empty states.
- Use `enableEdgeToEdge`, `Scaffold`, `NavigationBar`, and `TopAppBar`. Handle insets explicitly.
- Put every string in `strings.xml` from the first commit.
- Add a preview gallery with light, dark, and 200 percent font variants for the shared components.
- Use the shield adaptive icon placeholder. Final icon polish can wait for Step 11.
- The phone proof for Step 2 is the plan's build, install, and logcat check, plus this design QA table.

## Sources

Read on 5 November 2026.

- Material 3 color roles, https://m3.material.io/styles/color/roles
- Material 3 type scale, https://m3.material.io/styles/typography/type-scale-tokens
- Material 3 navigation bar, https://m3.material.io/components/navigation-bar/overview
- WCAG 2.2 target size minimum, https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html
- WCAG 2.2 quick reference, https://www.w3.org/WAI/WCAG22/quickref/
- Android accessibility guidance, https://developer.android.com/guide/topics/ui/accessibility
- Android edge-to-edge guidance, https://developer.android.com/develop/ui/views/layout/edge-to-edge
