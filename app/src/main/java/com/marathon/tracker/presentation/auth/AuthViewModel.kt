package com.marathon.tracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marathon.tracker.auth.StravaAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val stravaAuthManager: StravaAuthManager,
) : ViewModel() {

    fun handleStravaCallback(code: String) {
        viewModelScope.launch {
            stravaAuthManager.handleCallback(code)
        }
    }
}
