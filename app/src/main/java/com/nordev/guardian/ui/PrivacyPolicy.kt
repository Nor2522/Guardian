package com.nordev.guardian.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.nordev.guardian.R

// Play wants a policy a user can actually reach. A phone with no browser would
// dead-end a plain link, so the callers fall back to the in-app summary when
// nothing can open the page. Returns true when a browser took the link.
fun openPrivacyPolicy(context: Context): Boolean {
    val uri = context.getString(R.string.privacy_policy_url).toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
