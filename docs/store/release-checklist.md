# Release build and internal track

Ordered checklist for Step 11's build half. Each step says what proves it.

## 1. Version

`app/build.gradle.kts` currently has `versionCode = 1`, `versionName = "0.1.0"`.
Decide before the first upload: Play never lets a versionCode be reused on the
same track. For the first internal build, `1` / `1.0.0` is fine; bump
`versionCode` on every later upload.

## 2. Upload keystore

Create it once, outside the repo, and keep two backups:

```
keytool -genkeypair -v \
  -keystore ~/keys/guardian-upload.jks \
  -alias guardian-upload \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Guardian, O=NorDev, C=SO"
```

Do not commit the keystore, the password, or `keystore.properties`. Add both to
`.gitignore` before the first signed build.

## 3. Signing config

`app/build.gradle.kts` reads the keystore from `keystore.properties` (or
environment variables) when the file exists, and leaves the release build
unsigned otherwise, so a fresh clone still builds.

```
storeFile=...
storePassword=...
keyAlias=guardian-upload
keyPassword=...
```

Then:

```
bash ./gradlew bundleRelease --offline --no-daemon --no-parallel --max-workers=2 --console=plain
```

Output: `app/build/outputs/bundle/release/app-release.aab`.

Verify before uploading: `jarsigner -verify` on the AAB, `bundletool
build-apks --local-testing` plus `adb install-multiple` on the SM-A155F, and one
real-phone pass over Status, limits, schedules, filter, pause screen, and the
parent PIN. A debug-only bug that survives into release is exactly what the
pre-launch report will find later, more slowly.

## 4. Enroll in Play App Signing

The first upload must opt in. Keep the upload key in the local backup; if it is
lost, the reset request needs a new upload certificate.

## 5. Internal testing track

1. Create the app entry with the package name `com.nordev.guardian`.
2. Upload the AAB to Internal testing.
3. Add Nor's account as a tester, install from the internal link, not adb.
4. Confirm the installed build reports the same version and works with the same
   data as the debug pass.

For a personal (not organization) developer account created recently, Google
requires a closed test with a minimum number of testers who stay opted in for
two continuous weeks before production access is granted. Check the number shown
in the console for this account; planning assumption is 12 testers for 14 days.

## 6. Pre-launch report

Runs automatically on the internal release. Read it for:

- Crashes and ANRs on the test devices.
- Accessibility issues the crawler can see.
- The screenshot set, which also catches layout problems on other densities.
- Warnings about the Accessibility, VPN, or Device Admin use.

Fix anything real, bump `versionCode`, upload again.

## 7. Before production

- Privacy policy URL live, and the in-app `Read the privacy policy` button opens
  it (code change: replace the summary dialog with an intent to the URL).
- Data safety submitted and consistent with `data-safety.md`.
- Accessibility declaration submitted with the video.
- Monitoring disclosure present in the description.
- `isMonitoringTool` metadata in the manifest of every uploaded version code.
- Content rating questionnaire completed.
- Store graphics uploaded.
- Nor has tested the CleanBrowsing-unreachable case by hand (the one gap left in
  Appendix A).

## 8. Rollout

Internal → closed → production staged at 20 %, watching the Play vitals for
crashes and ANRs for a day before going to 100 %.
