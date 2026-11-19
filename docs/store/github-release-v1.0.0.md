# Guardian 1.0.0

Guardian is a parental-control app for one Android phone. Everything runs on that
phone: there is no Guardian account, no Guardian server, and no ads or analytics.

**This is the first public release.** It is the same build that is being prepared
for Google Play.

## What is in 1.0.0

- **Daily app limits** — choose the daily time per app; when it runs out, Guardian
  shows a pause screen the child cannot dismiss on their own.
- **Bedtime and study schedules** — block everything except the apps you allow,
  with rules that can cross midnight and overlap.
- **Website filter** — a built-in adult-domain list plus the domains you add,
  resolved over encrypted DNS (DNS over TLS) through CleanBrowsing's free Family
  filter. The tunnel carries DNS only; no other traffic is routed or inspected.
- **Screen time** — today and the last seven days, per app.
- **Parent PIN** — required to change limits, open the guardian screens, or remove
  the app. Stored only as a salted PBKDF2 hash.
- **Uninstall protection** — optional Device Admin so removing Guardian needs the
  PIN.
- **Always-on notice** — a persistent notification that proves protection is
  running.

## Requirements

- Android 8.0 (API 26) or newer.
- A phone the child uses. Guardian is set up and controlled by a parent.
- Network access for the website filter. When CleanBrowsing cannot be reached,
  lookups fail instead of silently falling back to an unfiltered resolver.

## Install from this release

1. Download `Guardian-1.0.0.apk` below.
2. On the phone, open the downloaded file. When Android asks, allow your browser
   or file manager to install unknown apps. This is a one-time switch in Settings
   and can be turned back off afterwards.
3. Play Protect may warn that the app was not downloaded from Google Play. Choose
   to install anyway after checking the fingerprint below.
4. Open Guardian and follow setup: set a parent PIN, then grant the checks you
   want — Usage access for screen time, Accessibility so limits notice which app
   is open, a local VPN for the website filter, notifications for the ongoing
   notice, and Device Admin if you want uninstall protection. The app explains
   each one before the system screen opens.
5. Open the parent screens with the PIN and set your first limit or schedule.

**Updating:** Android will install this over an earlier Guardian signed with the
same key and keep the PIN, limits, and usage data. A build signed with a different
key cannot be installed over it; Android will refuse with "App not installed".

## Verify the download

```
sha256sum Guardian-1.0.0.apk
4477d391d299d4d36e1c1ef1385e0f35beb61f69f943bd62da0d5b8c9749f559
```

The APK is signed with APK Signature Scheme v2 by the release key:

```
Signer certificate SHA-256:
BB:8E:AA:B0:27:F9:29:FA:B4:1B:B1:86:B0:4D:29:58:07:62:8D:CE:4E:E4:B6:50:75:1C:8C:F5:D9:77:A2:69
```

The same certificate signs the `.aab` asset, so a Google Play install and a
GitHub install come from the same key.

## Assets

| Asset | Size | Purpose |
| --- | --- | --- |
| `Guardian-1.0.0.apk` | 14,266,049 bytes | Install on a phone (Android 8.0+) |
| `Guardian-1.0.0.aab` | 13,736,624 bytes | Google Play only — archival copy of the bundle uploaded to Play; it cannot be installed by hand |

## Privacy

Guardian keeps app usage, limits, schedules, and the PIN on the phone. The only
data that leaves the device is the domain names the phone looks up while the
website filter is on; they go over encrypted DNS to CleanBrowsing's Family filter.
Guardian keeps no browsing history and sends nothing to the developer. It does not
read messages, photos, or what is typed, and does not use the camera, microphone,
or location.

Full policy: https://nor2522.github.io/Guardian/site/

## Known limitations

- A DNS filter cannot see inside HTTPS. Apps and browsers that use their own
  encrypted DNS, or connect to a hardcoded IP address, can bypass it.
- Android's Quick Settings can turn the VPN off without the app's guard.
- When CleanBrowsing is unreachable, blocked and allowed names both fail rather
  than resolving unfiltered.
- No filter is perfect. Guardian does not promise that.

## Who this is for

Guardian is a monitoring app for parents, used on a child's phone. It shows a
persistent notification while protection runs and does not hide itself. It is not
for monitoring another adult.
