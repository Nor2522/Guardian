package com.nordev.guardian.ui.usage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nordev.guardian.data.usage.UsageRepository
import com.nordev.guardian.data.usage.UsageSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// One snapshot is shared by the status screen and the parent Today tab, so both
// show the same number and a permission grant only has to refresh once.
class UsageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsageRepository(application)
    private val _state = MutableStateFlow<UsageSnapshot?>(null)
    val state: StateFlow<UsageSnapshot?> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) { repository.load() }
            _state.value = snapshot
        }
    }
}
