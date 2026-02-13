package com.cryptovault.ui.screens.detail

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

/**
 * ViewModel para la pantalla de detalle de una criptomoneda.
 *
 * Recibe el cryptoId como parámetro (inyectado desde el NavGraph)
 * y observa los datos de esa cripto desde Room.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val repository: CryptoRepository,
    private val preferencesManager: CurrencyPreferences,
    private val cryptoId: String
) : ViewModel() {

    val baseCurrency: StateFlow<String> = preferencesManager.baseCurrencyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "usd"
        )

    /**
     * Observa la criptomoneda seleccionada desde la BD local.
     * Se actualiza automáticamente si los precios cambian o si cambia la moneda base.
     */
    val crypto: StateFlow<Crypto?> = baseCurrency
        .flatMapLatest { currency ->
            repository.getCryptoById(cryptoId, currency)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(cryptoId)
        }
    }

    class Factory(
        private val repository: CryptoRepository,
        private val preferencesManager: CurrencyPreferences,
        private val cryptoId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(repository, preferencesManager, cryptoId) as T
        }
    }
}
