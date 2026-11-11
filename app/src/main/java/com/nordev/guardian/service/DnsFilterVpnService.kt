package com.nordev.guardian.service

import android.net.VpnService

// Declared so the permissions step can raise the system VPN consent. The DNS
// proxy arrives in Step 8; this service captures nothing until then.
class DnsFilterVpnService : VpnService()
