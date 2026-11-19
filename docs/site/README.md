# Privacy policy page (published)

Live at **https://nor2522.github.io/Guardian/site/**, served from
`docs/site/index.html` by GitHub Pages on `main` / `/docs` in
`https://github.com/Nor2522/Guardian`. Verified 19 November 2026: loads without a
login, valid certificate, no requests besides the page itself.

The page is self-contained: no scripts, no fonts, no external assets, light and
dark from the system setting. `docs/.nojekyll` stops Jekyll from processing the
design documents that share the folder.

## Before Play submission

Replace `[CONTACT EMAIL]` in `index.html` (and in `docs/store/privacy-policy.md`
and `docs/store/listing.md`) with the official address once Nor provides it.
Nothing else in the page is a placeholder.

## In the app

`R.string.privacy_policy_url` holds the same URL. Both the onboarding link
(`PrivacyScreen.kt`) and the Settings row (`SettingsScreen.kt`) call
`openPrivacyPolicy(context)`, which opens a browser and falls back to the in-app
summary dialog only when no browser accepts the intent.

If the URL ever changes, update three places together: this file, the string
resource, and the Play listing entry.

## Updating the policy

Edit `docs/store/privacy-policy.md` first, copy the change into `index.html`,
bump the effective date on both, commit, push. The in-app summary text in
`strings.xml` must state the same facts.

## Checks

- Page loads with no console errors and no requests besides itself.
- Contrast passes AA in both themes.
- Reads correctly at 200 % text zoom on a phone.
- The contact link opens a mail app.
