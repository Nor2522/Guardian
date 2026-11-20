# Guardian privacy policy (draft to host)

Status: published and verified. The in-app "Read the privacy policy" button and
the Settings row open this page.

Published at: https://nor2522.github.io/Guardian/site/
Contact: `noraliadan252@gmail.com`
Effective date: 19 November 2026

---

## Guardian privacy policy

Guardian is a parental-control app for one phone. It runs entirely on that
phone. There is no Guardian account, no Guardian server, and no Guardian
analytics.

### What stays on the phone

- Which app is open and for how long, kept as a local usage ledger.
- App limits, schedules, and the website filter switch.
- Blocked website domains.
- The parent PIN, stored only as a salted PBKDF2 hash. The PIN itself is never
  stored and never leaves the phone.
- App settings and onboarding choices.

Uninstalling Guardian, or clearing its data, deletes all of it. Because there is
no account, there is nothing to delete on a server.

### What leaves the phone

When the website filter is on, and only then, the phone sends the domain names
it looks up over encrypted DNS (DNS over TLS, port 853) to CleanBrowsing's free
Family filter. This is how the filter works: blocked names are answered on the
phone, and every other name has to be resolved by a resolver. CleanBrowsing is a
third-party service with its own privacy policy at
https://cleanbrowsing.org/privacy. Guardian does not send page content, the
contents of messages or photos, keystrokes, or any identifier for you or the
phone. Guardian keeps no browsing history and no query log, and writes no
domain names to disk.

If the filter is off, Guardian sends nothing.

### What Guardian cannot see

Guardian does not read messages, photos, files, passwords, or what is typed. It
does not use the microphone, the camera, or location. It does not collect
contacts, calendars, or health data. It has no advertising SDK and no crash or
analytics SDK.

### Permissions and why each exists

- **Usage access.** Reads how long each app was in the foreground so daily
  limits and screen-time totals work. The data stays in the app's private
  storage.
- **Accessibility.** Detects which app is in the foreground so limits, schedules,
  and the pause screen can react, and watches for the system VPN and Device
  Admin screens so the parent PIN gates them. It does not read screen text,
  capture the screen, or perform gestures.
- **VPN (VpnService).** Runs the local DNS filter. The tunnel carries only the
  two DNS addresses on this phone. Guardian does not route or inspect other
  traffic and does not use a remote VPN server.
- **Device admin.** Makes removing Guardian require the parent PIN.
- **Notifications.** Shows the ongoing "Guardian is on" notice, which is the
  visible proof that protection is running.
- **Run at startup.** Restores the filter after a reboot or an app update while
  the parent's switch is still on.

### Children's privacy

Guardian is set up and controlled by a parent or guardian. It collects no
personal data from the child and sends none to the developer. The only data
leaving the phone is the domain-name lookup described above, which any DNS
resolver would receive.

### Changes

If a future version changes what leaves the phone, this page is updated before
that version ships and the in-app text is updated with it.

### Contact

Questions about this policy: `noraliadan252@gmail.com`.
