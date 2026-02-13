package com.cryptovault

import com.cryptovault.domain.model.Crypto
import com.cryptovault.domain.repository.CryptoRepository
import com.cryptovault.domain.repository.CurrencyPreferences
import com.cryptovault.ui.screens.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test unitario del HomeViewModel.
 *
 * Usa Test Doubles (Fakes) para aislar el ViewModel de dependencias
 * externas (red, base de datos, DataStore).
 *
 * Se verifica:
 * 1. Estado inicial correcto (loading = true).
 * 2. Estado tras refresh exitoso (loading = false, sin error).
 * 3. Estado tras refresh fallido (error message presente).
 * 4. Los datos se emiten correctamente desde el repositorio.
 * 5. La búsqueda actualiza el estado.
 * 6. clearError() limpia el mensaje de error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCryptoRepository
    private lateinit var fakePreferences: FakeCurrencyPreferences
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        // Reemplazar el dispatcher Main para evitar errores en tests
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCryptoRepository()
        fakePreferences = FakeCurrencyPreferences()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(fakeRepository, fakePreferences)
    }

    // ── Test 1: Estado inicial ──
    @Test
    fun initialStateShouldBeLoading() = runTest {
        viewModel = createViewModel()
        assertTrue("El estado inicial debe ser loading", viewModel.uiState.value.isLoading)
        assertNull("No debe haber error inicial", viewModel.uiState.value.errorMessage)
    }

    // ── Test 2: Refresh exitoso ──
    @Test
    fun afterSuccessfulRefreshLoadingShouldBeFalseWithNoError() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle() // Ejecutar todas las corrutinas pendientes

        assertFalse("Loading debe ser false tras refresh", viewModel.uiState.value.isLoading)
        assertNull("No debe haber error tras refresh exitoso", viewModel.uiState.value.errorMessage)
    }

    // ── Test 3: Refresh fallido ──
    @Test
    fun afterFailedRefreshErrorMessageShouldBeSet() = runTest {
        fakeRepository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse("Loading debe ser false tras error", viewModel.uiState.value.isLoading)
        assertNotNull("Debe haber mensaje de error", viewModel.uiState.value.errorMessage)
        assertTrue(
            "El error debe contener el mensaje de la excepción",
            viewModel.uiState.value.errorMessage!!.contains("Fake network error")
        )
    }

    // ── Test 4: Datos del repositorio ──
    @Test
    fun cryptosListShouldEmitDataFromRepository() = runTest {
        viewModel = createViewModel()
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.cryptos.collect {}
        }
        advanceUntilIdle()

        val cryptos = viewModel.cryptos.value
        assertEquals("Debe haber 2 criptos", 2, cryptos.size)
        assertEquals("La primera debe ser bitcoin", "bitcoin", cryptos[0].id)
        assertEquals("La segunda debe ser ethereum", "ethereum", cryptos[1].id)
    }

    // ── Test 5: Búsqueda ──
    @Test
    fun searchQueryShouldUpdateUiState() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("eth")
        assertEquals("eth", viewModel.uiState.value.searchQuery)
    }

    // ── Test 6: Limpiar error ──
    @Test
    fun clearErrorShouldResetErrorMessageToNull() = runTest {
        fakeRepository.shouldFail = true
        viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        viewModel.clearError()
        assertNull("El error debe ser null tras clearError", viewModel.uiState.value.errorMessage)
    }

    // ══════════════════════════════════════════════════════════
    // FAKES (Test Doubles)
    // ══════════════════════════════════════════════════════════

    /**
     * Fake del CryptoRepository.
     * Retorna datos predefinidos sin necesidad de red ni base de datos.
     */
    private class FakeCryptoRepository : CryptoRepository {
        var shouldFail = false

        private val fakeCryptos = listOf(
            Crypto(
                id = "bitcoin", symbol = "btc", name = "Bitcoin",
                imageUrl = "https://example.com/btc.png",
                currentPrice = 65000.0, marketCap = 1200000000000.0,
                priceChangePercentage24h = 2.5, rank = 1,
                high24h = 66000.0, low24h = 64000.0,
                totalVolume = 30000000000.0, isFavorite = false
            ),
            Crypto(
                id = "ethereum", symbol = "eth", name = "Ethereum",
                imageUrl = "https://example.com/eth.png",
                currentPrice = 3500.0, marketCap = 400000000000.0,
                priceChangePercentage24h = -1.2, rank = 2,
                high24h = 3600.0, low24h = 3400.0,
                totalVolume = 15000000000.0, isFavorite = false
            )
        )

        override fun getAllCryptos(currency: String): Flow<List<Crypto>> = flowOf(fakeCryptos)
        override fun getFavorites(currency: String): Flow<List<Crypto>> =
            flowOf(fakeCryptos.filter { it.isFavorite })
        override fun getCryptoById(cryptoId: String, currency: String): Flow<Crypto?> =
            flowOf(fakeCryptos.find { it.id == cryptoId })
        override suspend fun refreshCryptos() {
            if (shouldFail) throw RuntimeException("Fake network error")
        }
        override suspend fun toggleFavorite(cryptoId: String) { /* no-op */ }
    }

    /**
     * Fake de CurrencyPreferences.
     * Emite "usd" por defecto sin necesitar Context ni DataStore.
     */
    private class FakeCurrencyPreferences : CurrencyPreferences {
        private val _baseCurrency = MutableStateFlow("usd")
        override val baseCurrencyFlow: Flow<String> = _baseCurrency
        override suspend fun setBaseCurrency(currency: String) {
            _baseCurrency.value = currency
        }
    }
}
