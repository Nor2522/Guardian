# Publishing the privacy policy on GitHub Pages

The page is `docs/site/index.html`, self-contained: no scripts, no fonts, no
requests to anywhere, light and dark from the system setting.

## One-time setup

1. Create a **public** GitHub repository named `guardian-privacy`. A private
   repository cannot serve Pages on a free account, and Play needs the URL to
   load without a login.
2. Put `index.html` at the repository root and push.
3. Repository → Settings → Pages → Source: *Deploy from a branch* → Branch:
   `main`, folder `/ (root)` → Save.
4. Wait for the first deployment (about a minute). The URL is
   `https://<account>.github.io/guardian-privacy/`.

## Before pushing

Replace `[CONTACT EMAIL]` with a real, monitored address. Play review and users
will both use it. Do not use a no-reply address that bounces.

## After the URL exists

Give the URL to the app so the in-app buttons stop opening the summary dialog and
open the page instead:

1. `docs/plan.md` Appendix A step 11 records the URL.
2. `PrivacyScreen.kt` and the Settings privacy row open
   `Intent(ACTION_VIEW, Uri.parse(PRIVACY_URL))` with a fallback to the summary
   dialog if no browser handles it.
3. The URL goes into the Play listing and the Data safety form unchanged.

## Updating the policy

Edit `docs/store/privacy-policy.md` first, copy the change into `index.html`,
bump the effective date on both, commit, push. The in-app summary text in
`strings.xml` must match the same facts.

## Checks

- The page loads with no console errors and no network requests besides itself.
- Text contrast passes AA in both themes (foreground `#1a1c1e` on `#fdfcff`, and
  `#e4e2e6` on `#121316`).
- The page reads correctly at 200 % text zoom on a phone.
- The contact link opens a mail app.
