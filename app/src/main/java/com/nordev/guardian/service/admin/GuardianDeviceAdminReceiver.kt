package com.nordev.guardian.service.admin

import android.app.admin.DeviceAdminReceiver

// Being an active admin is the whole mechanism: Android refuses to uninstall a
// package with one. No wipe, lock, or password policies are requested, so this
// receiver overrides nothing.
class GuardianDeviceAdminReceiver : DeviceAdminReceiver()
