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
  manifest property "Local DNS filter for parental controls. Checks domain
  names on this phone; does not send browsing data anywhere." `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_SPECIAL_USE`, and `INTERNET` are declared. `INTERNET` is
  needed on every API level, including 26 to 28, because the forwarder opens
  UDP sockets to the network resolver.

## Packet path

1. An app resolves a name and sends a UDP query to `10.111.222.2:53` or
   `[fd00:1111:2222::2]:53`. That packet enters the tunnel.
2. A reader thread parses the IPv4 or IPv6 header, then UDP, then the DNS
   question. No other protocol is answered.
3. If the name matches the shipped adult list or a custom domain, the service
   writes back an NXDOMAIN reply from the same address and port. The app sees
   a normal "name does not exist" answer and shows its own error page.
4. Otherwise the service forwards the query to the network resolver from
   `ConnectivityManager.getLinkProperties(activeNetwork).dnsServers`, skipping
   the tunnel's own addresses. The socket is `protect()`ed so it leaves through
   the real network. Timeout 4 seconds, then SERVFAIL so the app fails rather
   than hangs.
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
- Upstream answers are not cached.

## Another VPN

Android allows one VPN at a time. The service cannot establish while another
VPN holds the tunnel, and `establish()` returns null. The app also enumerates
`ConnectivityManager.allNetworks` for a `TRANSPORT_VPN` network that is not the
one this service created; when found, the Web tab shows the "Another VPN is
on" problem instead of a green "on" state. Turning our filter on while another
VPN is active fails visibly and points at system VPN settings.

## What this cannot do

- Browsers and apps that use DNS over HTTPS, DNS over TLS, or their own
  resolver talk to a different address and bypass the tunnel. Guardian does not
  inspect HTTPS and cannot see page content.
- Hardcoded IP addresses never ask DNS and are not blocked.
- A custom app with its own network stack can do the same.
- Play and the app copy must say "does not promise perfect blocking".

## Failure states

| State | Detection | UI |
| --- | --- | --- |
| Filter off by parent | stored switch | off card, limits keep working |
| VPN revoked or stopped | service destroyed, `prepare()` non-null | problem card, Fix filter |
| Another VPN active | VPN network not ours | problem card, Open VPN settings |
| No DNS server on the network | empty link properties | SERVFAIL answers, problem note |
| Service killed by the system | foreground notification gone | status re-checks on resume |
