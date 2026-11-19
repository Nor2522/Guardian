# Store graphics

| File | Size | Source |
| --- | --- | --- |
| `icon-512.png` | 512 × 512 | rendered from `app/src/main/res/drawable/ic_launcher_foreground.xml` over `ic_launcher_background.xml` |
| `feature-graphic-1024x500.png` | 1024 × 500 | hand-written SVG, teal `#005048`, shield left, wordmark and tagline right |
| `screenshots/*.png` | 1080 × 2140 | `adb exec-out screencap -p` on the SM-A155F, cropped |

## Icon

The launcher icon is a vector: a full-bleed `#005048` square and a white shield
with a check cut out of it. The store icon is that vector rendered at 512 px with
`rsvg-convert`, so it matches what the phone shows. Play masks the corners
itself; no rounding is baked in.

The shield occupies about 40 % of the width. That is the real launcher icon, so
the listing stays honest about it. If the art is ever enlarged, change
`ic_launcher_foreground.xml` first and re-render, rather than shipping a store
icon that differs from the phone.

## Feature graphic

Plain SVG, rendered with `rsvg-convert -w 1024 -h 500`. Tagline: "Parental
control that stays on the phone." Keep text inside the middle 80 % so cropped
placements still read.

## Screenshots

Taken from the 1.0.0 debug build, the same build Nor runs daily. Installing the
release build would mean uninstalling the app and losing the PIN, limits, and
filter state, so the debug build stands in; it differs only in `Log` lines.

Parent screens carry `FLAG_SECURE`, which turns screenshots black. For this
capture only, `MainActivity.kt` had `if (secure)` temporarily forced false, the
build was installed, the screens captured, and the patch reverted and rebuilt
before anything was committed. Nothing in the repo or the shipped APK skips
`FLAG_SECURE`.

The two Today screens were captured with sample data. `UsageRepository.load()`
had a temporary early return that built four invented apps (Learning Lab, Pixel
Paint, Story Time, Music Box) with letter icons and 42 minutes today over a
510-minute week; the patch was reverted and the resulting APK is byte-identical
to the committed build. Guardian's stored usage, PIN, limits, and filter were
never touched, and no real app name or real minutes appear in any screenshot.

Each capture is 1080 × 2340; the top 130 px (status bar) and bottom 70 px (nav
gesture area) are cropped, leaving 1080 × 2140. That satisfies Play's rule that
the long edge is at most twice the short edge, and removes the clock, battery,
and notification icons.

| File | Screen |
| --- | --- |
| `01-today-child.png` | child Today: protection on, screen time, three checks |
| `02-parent-today.png` | parent Today: screen time, seven-day chart, apps today |
| `03-limits.png` | Limits: an app limit with its progress bar |
| `04-schedules.png` | Schedules: a bedtime schedule |
| `05-web.png` | Web: filter on, blocked sites, adult list version |
| `06-settings.png` | Settings: theme, privacy policy row, data note, removal |

Limits, Schedules, Web, and Settings were captured against the live configuration
and contain no personal data. If that configuration ever changes, only the two
Today files need replacing.
