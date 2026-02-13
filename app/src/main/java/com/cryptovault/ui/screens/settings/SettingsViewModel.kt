package com.cryptovault.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptovault.domain.repository.CurrencyPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de configuración.
 * Gestiona la preferencia de moneda base mediante DataStore.
 */
class SettingsViewModel(
    private val preferencesManager: CurrencyPreferences
) : ViewModel() {

    val baseCurrency: StateFlow<String> = preferencesManager.baseCurrencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "usd")

    fun setBaseCurrency(currency: String) {
        viewModelScope.launch {
            preferencesManager.setBaseCurrency(currency)
        }
    }

    class Factory(
        private val preferencesManager: CurrencyPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesManager) as T
        }
    }
}
