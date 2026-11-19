# Data safety answers

Console path: Play Console → App content → Data safety. Every answer below is
derived from the shipped code, not from intent. Evidence column says where to
check it again before submitting.

## Section 1: overview

| Question | Answer | Why |
| --- | --- | --- |
| Does your app collect or share any of the required user data types? | **Yes** | Domain names of DNS lookups leave the phone to CleanBrowsing when the filter is on. |
| Is all of the user data collected by your app encrypted in transit? | **Yes** | Only transport is DNS over TLS, port 853, pinned host. No plaintext HTTP or UDP DNS. |
| Do you provide a way for users to request that their data be deleted? | **No** | There is no developer-side storage and no account. Uninstalling or clearing app data deletes everything Guardian holds. Say this in the explanation box. |
| Has your app been independently validated against a security standard? | **No** | No certification exists. |

Collected vs shared: Guardian itself stores nothing off-device, so the developer
does **not collect** data. It **shares** one type with a third party.

## Section 2: data types

One row only.

| Field | Value |
| --- | --- |
| Category | App activity |
| Data type | **Web browsing** |
| Collected by developer | No |
| Shared with third parties | Yes |
| Shared with | CleanBrowsing (free Family filter, `family-filter-dns.cleanbrowsing.org`) |
| Purpose | App functionality |
| Optional or required | Required (the website filter cannot resolve names without it) |
| Processed ephemerally | Yes |
| Encrypted in transit | Yes |

Why this type: the names the phone resolves are the only user data that leaves
the device. Page content, URLs beyond the hostname, and browsing history are
never seen or sent. Guardian keeps no query log.

No other data type applies. There is no analytics SDK, no crash SDK, no ads, no
account, no identifier, no location, no contacts, no messages, no files, no
microphone, no camera. `app/build.gradle.kts` shows the full dependency list;
`docs/third-party.md` names every third party.

## Section 3: security practices

| Question | Answer |
| --- | --- |
| Data encrypted in transit | Yes |
| Users can request deletion | No, with the explanation from Section 1 |
| Committed to Play Families policy | No, the app is for parents, not children |
| Independent security review | No |

## Recheck before submitting

1. `grep -rn "https://" app/src/main/java` still shows only the CleanBrowsing
   comment, no analytics or crash endpoint.
2. `docs/third-party.md` library list still matches `app/build.gradle.kts`.
3. The privacy policy URL is live and matches `docs/store/privacy-policy.md`.
4. No new permission appears in `AndroidManifest.xml` without a matching row in
   `permission-declarations.md`.
