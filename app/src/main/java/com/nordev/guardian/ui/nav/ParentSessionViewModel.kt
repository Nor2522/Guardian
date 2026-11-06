package com.nordev.guardian.ui.nav

import androidx.lifecycle.ViewModel

// Keeps the parent session across rotation; a new process starts locked.
class ParentSessionViewModel : ViewModel() {
    val session = ParentSession()
}
