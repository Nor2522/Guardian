# Third-party material

## Adult domain list

File: `app/src/main/assets/adult-domains.txt`

| Field | Value |
| --- | --- |
| Source | StevenBlack/hosts, porn-only alternate |
| Source URL | https://github.com/StevenBlack/hosts/blob/master/alternates/porn-only/hosts |
| Source snapshot | 15 November 2026 (the file's own date header) |
| Fetched | 16 November 2026 |
| License | MIT for the StevenBlack compilation; the upstream lists keep the licenses recorded in the project README |
| Source rows | 76,806 normalized hostnames |
| Parsed domains | 64,307 unique domains after removing duplicate `www.` rows and applying Guardian's domain normalization; the two whole adult TLDs (`sex`, `xxx`) are kept |
| Asset SHA-256 | `5965a945b04694c7dd4d5a3ef4fb47d360cfb07011c84ce2ce4550cc0cc73613` |
| Raw source SHA-256 | `a75e8aec01c50bde58bbb765922443e1ae2a6d618480cbdc2c5b5716fde0cfda` |

The asset keeps the source rows in sorted order with one hostname per line and a
short metadata header. The parser lowercases, strips a leading `www.`, removes
duplicates, and rejects entries that are not a normal domain chain. The app
shows the version (`2026.11.15`) and the parsed domain count. Version 1 does not
fetch list updates; the next app build replaces the asset and this table.

The source is a hosts file. Only its `0.0.0.0 <hostname>` rows were copied;
comment, blank, and non-host rows were dropped. The domain names are facts; the
compilation is MIT-licensed. No code from the project is bundled.

## Libraries

The app uses AndroidX, Compose, Navigation, DataStore, and JUnit through the
Gradle version catalog. No third-party network, DNS, or block-list library is
bundled.
