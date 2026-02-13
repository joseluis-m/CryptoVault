package com.cryptovault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptovault.domain.repository.CurrencyPreferences
import com.cryptovault.domain.model.Crypto
import com.cryptovault.domain.repository.CryptoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la UI de la pantalla Home.
 *
 * Sigue el patrón UDF (Unidirectional Data Flow):
 * - El ViewModel expone un StateFlow<HomeUiState> inmutable.
 * - La UI observa este estado y se recompone automáticamente.
 * - Las acciones del usuario llegan al ViewModel como llamadas a funciones.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: CryptoRepository,
    private val preferencesManager: CurrencyPreferences
) : ViewModel() {

    // ── Estado mutable interno ──
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ── Moneda base observada desde DataStore ──
    val baseCurrency: StateFlow<String> = preferencesManager.baseCurrencyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "usd"
        )

    /**
     * Lista de criptomonedas que cambia reactivamente cuando:
     * 1. La base de datos se actualiza (Room Flow).
     * 2. La moneda base cambia (DataStore Flow).
     *
     * flatMapLatest cancela la colección anterior cuando la moneda cambia.
     */
    val cryptos: StateFlow<List<Crypto>> = baseCurrency
        .flatMapLatest { currency ->
            repository.getAllCryptos(currency)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshData()
    }

    /**
     * Refresca los datos desde la API.
     * Si falla, actualiza el estado con el error pero los datos cacheados siguen visibles.
     */
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.refreshCryptos()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al actualizar: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Alterna el favorito de una criptomoneda.
     */
    fun toggleFavorite(cryptoId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(cryptoId)
        }
    }

    /**
     * Actualiza la query de búsqueda.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Limpia el mensaje de error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Factory para crear el ViewModel con dependencias.
     * Necesario porque usamos inyección manual (sin Hilt).
     */
    class Factory(
        private val repository: CryptoRepository,
        private val preferencesManager: CurrencyPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, preferencesManager) as T
        }
    }
}
