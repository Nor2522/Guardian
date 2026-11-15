# Third-party material

## Adult domain seed list

File: `app/src/main/assets/adult-domains.txt`

| Field | Value |
| --- | --- |
| Version | 2026.11 |
| Domains | 19 |
| Source | Written for Guardian from public adult domain names |
| License | The compilation is part of Guardian and ships with the app. Domain names are facts, not creative work. |
| SHA-256 | `b7ee0fae219f2b9874b60a5cf5e3cdda01419fa9a43fc51f41ac22878d8c6e74` |
| Read only | The parsed list is never written by the app; only the version and count are shown. |

This seed proves the filter pipeline end to end. Before a store release the
list is replaced or extended with a maintained list that has a clear license,
and this file records the new source, version, count, and hash. Version 1 does
not fetch list updates.

## Libraries

The app uses AndroidX, Compose, Navigation, DataStore, and JUnit through the
Gradle version catalog. No third-party network, DNS, or block-list library is
bundled.
