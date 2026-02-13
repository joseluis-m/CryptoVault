package com.cryptovault.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptovault.domain.repository.CurrencyPreferences
import com.cryptovault.domain.model.Crypto
import com.cryptovault.domain.repository.CryptoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val repository: CryptoRepository,
    private val preferencesManager: CurrencyPreferences
) : ViewModel() {

    val baseCurrency: StateFlow<String> = preferencesManager.baseCurrencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "usd")

    val favorites: StateFlow<List<Crypto>> = baseCurrency
        .flatMapLatest { currency -> repository.getFavorites(currency) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(cryptoId: String) {
        viewModelScope.launch { repository.toggleFavorite(cryptoId) }
    }

    class Factory(
        private val repository: CryptoRepository,
        private val preferencesManager: CurrencyPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(repository, preferencesManager) as T
        }
    }
}
