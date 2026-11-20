# Play listing copy

Draft for the store entry. Character limits are Play's; the counts are checked
below.

## App name (30 max)

`Guardian: parental controls` — 27 characters.

## Short description (80 max)

`App limits, bedtime, and a site filter that run on this phone.` — 62 characters.

## Full description (4000 max)

Guardian is a parental-control app for one Android phone. It runs on that phone,
with no account, no Guardian server, and no ads.

**What it does**

- Daily app limits. Pick the time per app; when it runs out, Guardian shows a
  pause screen the child cannot dismiss alone.
- Bedtime and study schedules. Block everything except the apps you allow, with
  rules that can cross midnight and overlap.
- Website filter. A built-in adult-domain list plus the domains you add, resolved
  over encrypted DNS through CleanBrowsing's free Family filter.
- Screen time. Today and the last seven days, per app.
- A parent PIN. Changing limits, opening the guardian screens, or removing the app
  asks for it.
- An always-on notification that proves protection is running.

**How it is set up**

A parent installs Guardian on the child's phone and turns on the checks it needs:
Usage access for screen time, Accessibility so limits can notice which app is
open, a local VPN for the website filter, and notifications for the ongoing
notice. Each one has its own explanation inside the app before the system screen
opens, and each one can be turned off again in Android settings.

**What Guardian can see**

Which app is open and for how long, and the domain names the phone looks up while
the filter is on. Blocked names are answered on the phone; every other lookup goes
over encrypted DNS to CleanBrowsing's Family filter. Guardian keeps no browsing
history and sends nothing to the developer. It does not read messages, photos,
passwords, or what is typed, and it does not use the camera, the microphone, or
location.

**What it cannot do**

- Apps and browsers that use their own encrypted DNS, or connect to a hardcoded IP
  address, can bypass a DNS filter. Guardian does not inspect HTTPS and cannot see
  page content.
- Android's Quick Settings can turn the VPN off without the app's guard.
- The filter needs network access to a DNS resolver. When CleanBrowsing cannot be
  reached, lookups fail honestly instead of falling back.
- No filter is perfect. Guardian does not promise that.

**Monitoring disclosure**

Guardian is a monitoring app for parents: it reports app usage and enforces the
limits a parent sets on a child's phone. It shows a persistent notification while
protection runs and never hides what it does. It is not for monitoring another
adult.

Privacy policy: https://nor2522.github.io/Guardian/site/

## Category and tags

- Category: Parenting (alternative: Tools)
- Tags: parental controls, screen time, app limits
- Contact email: `noraliadan252@gmail.com`
- Website: https://nor2522.github.io/Guardian/site/

## Graphics

| Asset | Spec | File |
| --- | --- | --- |
| App icon | 512 × 512 PNG, 32-bit | `graphics/icon-512.png` |
| Feature graphic | 1024 × 500 PNG | `graphics/feature-graphic-1024x500.png` |
| Phone screenshots | 2 to 8, max 2:1, min 320 px | `graphics/screenshots/`, six files at 1080 × 2140 |
| Tablet screenshots | optional | skip, phone-only app |

`graphics/README.md` records how each file was made. The icon is the launcher
art with the adaptive background filled in, so it matches the icon on the phone.

Screenshots came from the 1.0.0 debug build on the SM-A155F, which is what Nor
keeps installed: swapping to the release build would delete the configured PIN,
limits, and filter. The debug build differs only in log lines. Cropping the top
130 px and bottom 70 px drops the status bar and satisfies Play's 2:1 rule.

The Today screens were captured with sample data: four invented apps and 42
minutes today. No real app name or real usage appears in any screenshot.

## App content answers

| Section | Answer |
| --- | --- |
| Privacy policy | https://nor2522.github.io/Guardian/site/ |
| Ads | No |
| In-app purchases | No |
| App access | All features testable after setup. Reviewers need the setup path: see the reviewer notes below |
| Content rating | Complete the questionnaire; expected Everyone, no user-generated content, no sharing |
| Target audience | 18+ (parents and guardians); not designed for children |
| News app | No |
| COVID-19 apps | No |
| Data safety | See `data-safety.md` |
| Government app | No |
| Financial features | None |
| Health | None |

## Reviewer notes (paste into App access)

Guardian needs four user-granted capabilities before its features are visible.
The release build can be driven through all of them:

1. On first launch, choose any PIN and store it; the PIN is local to the device.
2. Open Status → Review to reach each permission with its disclosure.
3. Usage access: Android Settings → Special access → Usage data access →
   Guardian → allow.
4. Accessibility: disclosure screen → tick consent → open settings → Guardian →
   on.
5. Notifications: allow when prompted.
6. Website filter: Web tab → Filter websites → accept the VPN consent dialog.
7. Device admin is optional and only gates removal; leaving it off still shows
   every feature.

A demo video covering the same flow is linked in the Accessibility declaration.
