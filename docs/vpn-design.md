# DNS filter design

Step 8 answers the questions in `plan.md` Appendix B before the code lands.
Scope: version 1 filters domain names only. It does not proxy TCP, inspect
pages, or hide the phone's address.

## What runs

`DnsFilterVpnService` is a `VpnService` with a DNS-only tunnel:

- Tunnel interface addresses `10.111.222.1/32` and `fd00:1111:2222::1/128`.
- The DNS servers advertised to the phone are `10.111.222.2` and
  `fd00:1111:2222::2`. They differ from the interface addresses on purpose: an
  address owned by the interface is delivered locally by the kernel, so a query
  to it would never appear on the tunnel file descriptor.
- Routes cover only the two DNS server addresses, so all other traffic stays on
  the phone's normal network path.
- `addDisallowedApplication(packageName)` keeps Guardian's own upstream
  sockets out of its tunnel.
- The service runs as a foreground service with type `specialUse` and the
  manifest property "Local DNS filter for parental controls. Blocks sites on
  this phone and resolves other names through CleanBrowsing's public Family
  filter. No browsing history is kept." `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_SPECIAL_USE`, and `INTERNET` are declared. `INTERNET` is
  needed on every API level, including 26 to 28, because the forwarder opens a
  TLS connection to CleanBrowsing.

## Upstream: CleanBrowsing Family

The parent's single "Filter websites" switch is also the CleanBrowsing switch.
There is no second control and no DNS setup step.

- Resolver: CleanBrowsing's free Family filter, DNS over TLS (RFC 7858) on port
  853 to `family-filter-dns.cleanbrowsing.org` (`185.228.168.168`,
  `185.228.169.168`, `2a0d:2a00:1::`, `2a0d:2a00:2::`). No account, no dashboard,
  no custom lists on the free tier.
- Transport: TLS with SNI and hostname verification against the resolver's
  certificate. One connection is reused; a failed exchange is retried once from
  a fresh socket, then the query answers SERVFAIL.
- No fallback to the phone's resolver. Falling back would keep the parent's
  switch green while nothing is being filtered, which is the one failure this
  design refuses. A blocked network therefore breaks name resolution instead of
  quietly unfiltering the phone, and the Web tab says so.
- The built-in adult list and the parent's custom domains are still answered
  locally, before any network step, so they keep working with CleanBrowsing down.
- Reachability: a probe query runs when the tunnel starts and when the Web tab
  is shown. Its result drives the "CleanBrowsing is not reachable" card.
- Privacy: CleanBrowsing sees the domain names the phone resolves. Guardian
  stores no query log, and no browsing data goes to a Guardian server. The free
  Family filter also blocks VPN and proxy domains, blocks mixed-content sites
  such as Reddit, and forces SafeSearch on Google, Bing, and YouTube.
- Custom domains stay as a local layer. They are not sent to CleanBrowsing, and
  the free tier could not accept them anyway.

## Packet path

1. An app resolves a name and sends a UDP query to `10.111.222.2:53` or
   `[fd00:1111:2222::2]:53`. That packet enters the tunnel.
2. A reader thread parses the IPv4 or IPv6 header, then UDP, then the DNS
   question. No other protocol is answered.
3. If the name matches the shipped adult list or a custom domain, the service
   writes back an NXDOMAIN reply from the same address and port. The app sees
   a normal "name does not exist" answer and shows its own error page.
4. Otherwise the query is sent to CleanBrowsing over DNS over TLS. The socket
   connects to the resolver address directly, so no name lookup is needed to
   start filtering, and it leaves through the physical network because the
   tunnel routes only the two DNS addresses. Timeout 4 seconds, then SERVFAIL
   so the app fails rather than hangs.
5. The reply is wrapped in a fresh IPv4 or IPv6 header with the client's
   address and port, with correct header and UDP checksums, and written back
   to the tunnel.

The parser is hand written for IPv4, IPv6, UDP, and the DNS question. A packet
library would add size and a supply-chain dependency for about 300 lines of
fixed-offset work.

## Rules

- Only destination port 53 to the tunnel addresses is handled. Anything else
  read from the tunnel is dropped and counted.
- The query name is parsed, matched, then discarded. Guardian stores no query
  log, no browsing history, and no per-site counters.
- A DNS reply larger than the tunnel MTU of 1500 is dropped; the client retries
  and the normal path still applies.
- Upstream answers are not cached. Every allowed lookup is a fresh DoT exchange,
  so an answer is never older than what CleanBrowsing returned. Apps and
  browsers cache their own repeats; a local cache would need TTL parsing and
  could serve a stale address after a resolver change.

## Another VPN

Android allows one VPN at a time. The service cannot establish while another
VPN holds the tunnel, and `establish()` returns null. The app also enumerates
`ConnectivityManager.allNetworks` for a `TRANSPORT_VPN` network that is not the
one this service created; when found, the Web tab shows the "Another VPN is
on" problem instead of a green "on" state. Turning our filter on while another
VPN is active fails visibly and points at system VPN settings.

## Stopping

Android keeps a `VpnService` bound while the tunnel exists, so `stopService`
never reaches `onDestroy` and the interface keeps filtering. Turning the switch
off therefore sends an explicit `ACTION_STOP` to the running service, which
closes the tunnel descriptor, removes the foreground notification, and stops
itself. With no tunnel there is no notification: the Web tab's off card is the
only "off" signal, not a lingering foreground notification.

## System settings guard

While the filter is on, the accessibility service also watches for the system
VPN settings screen. It matches package and class only:
`com.android.settings.Settings$VpnSettingsActivity` on the SM-A155F, plus the
AOSP `VpnSettings` fragment suffix. The guard host asks for the parent PIN; a
correct PIN grants 60 seconds, then the guard returns. Back is ignored and Home
works. This is a best-effort layer, not a system lock: Quick Settings can still
disconnect the VPN, and the guard needs Accessibility. The Web help card states
both limits.

## What this cannot do

- Browsers and apps that use DNS over HTTPS, DNS over TLS, or their own
  resolver talk to a different address and bypass the tunnel. Guardian does not
  inspect HTTPS and cannot see page content.
- Hardcoded IP addresses never ask DNS and are not blocked.
- A custom app with its own network stack can do the same.
- Android Quick Settings can disconnect the VPN without passing the settings
  guard. Guardian cannot password-lock that system control.
- With Accessibility off, the VPN settings guard is not active. The OS still
  shows the weaker state on the status screen.
- A network that blocks or breaks port 853 stops name resolution while the
  filter is on. That is deliberate: falling back to the phone's resolver would
  remove the filter without telling the parent.
- CleanBrowsing over-blocks by design in Family mode (VPN and proxy domains,
  mixed-content sites such as Reddit). The parent cannot tune that on the free
  tier; turning the filter off is the only switch.
- Play and the app copy must say "does not promise perfect blocking".

## Failure states

| State | Detection | UI |
| --- | --- | --- |
| Filter off by parent | stored switch | off card, limits keep working |
| VPN revoked or stopped | service destroyed, `prepare()` non-null | problem card, Fix filter |
| Another VPN active | VPN network not ours | problem card, Open VPN settings |
| CleanBrowsing unreachable (no network, port 853 blocked, certificate failure) | DoT connect, handshake, or read failure, probe failure | SERVFAIL answers and the Web tab replaces the green card with "CleanBrowsing is not reachable" |
| Service killed by the system | foreground notification gone | status re-checks on resume |
