# Privacy policy page (published)

Live at **https://nor2522.github.io/Guardian/site/**, served from
`docs/site/index.html` by GitHub Pages on `main` / `/docs` in
`https://github.com/Nor2522/Guardian`. Verified 19 November 2026: loads without a
login, valid certificate, no requests besides the page itself.

## Landing page

The marketing page lives one level up, at `docs/index.html`, and is served at
**https://nor2522.github.io/Guardian/**. It reuses the app's palette from
`docs/design.md`, has no scripts and no external assets, and pulls the six
screenshots from `docs/store/graphics/screenshots/`. Keep the claims on it in
step with this policy: if the policy changes what leaves the phone, the landing
page's FAQ and limitations sections change with it.

The page is self-contained: no scripts, no fonts, no external assets, light and
dark from the system setting. `docs/.nojekyll` stops Jekyll from processing the
design documents that share the folder.

## Contact address

The policy page, `docs/store/privacy-policy.md`, and `docs/store/listing.md` all
carry the same address, `noraliadan252@gmail.com`, in the page footer, the
policy's Contact section, and the Play listing's contact email. If it changes,
update all three together. Nothing else in the page is a placeholder.

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
