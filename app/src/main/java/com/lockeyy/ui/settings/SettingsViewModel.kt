package com.lockeyy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockeyy.core.PinManager
import com.lockeyy.core.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val pinManager: PinManager
) : ViewModel() {

    val isBiometricEnabled = settingsManager.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val relockTimeout = settingsManager.relockTimeout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val hasPin = viewModelScope.launch { pinManager.hasPin() } // This needs to be a StateFlow properly, or checked on load

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setBiometricEnabled(enabled)
        }
    }

    fun updatePin(newPin: String) {
        viewModelScope.launch {
            pinManager.savePin(newPin)
        }
    }

    fun setRelockTimeout(timeout: Long) {
        viewModelScope.launch {
            settingsManager.setRelockTimeout(timeout)
        }
    }
}
