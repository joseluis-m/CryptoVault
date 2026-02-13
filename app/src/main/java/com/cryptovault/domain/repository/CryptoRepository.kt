package com.cryptovault.domain.repository

import com.cryptovault.domain.model.Crypto
import kotlinx.coroutines.flow.Flow

/**
 * Contrato (interfaz) del Repository en la capa de dominio.
 *
 * Define QUÉ operaciones están disponibles, sin especificar CÓMO se implementan.
 * La implementación real está en la capa de datos (CryptoRepositoryImpl).
 *
 * Beneficios:
 * - Inversión de dependencias: la UI depende de esta interfaz, no de la implementación.
 * - Testabilidad: podemos crear un FakeRepository para tests.
 * - Flexibilidad: cambiar de CoinGecko a otra API sin tocar la UI.
 */
interface CryptoRepository {

    /**
     * Obtiene todas las criptomonedas como Flow (observación reactiva).
     * @param currency Moneda base ("usd" o "eur")
     */
    fun getAllCryptos(currency: String): Flow<List<Crypto>>

    /**
     * Obtiene las criptomonedas favoritas.
     * @param currency Moneda base
     */
    fun getFavorites(currency: String): Flow<List<Crypto>>

    /**
     * Obtiene una criptomoneda por su ID.
     * @param cryptoId ID de la cripto (ej: "bitcoin")
     * @param currency Moneda base
     */
    fun getCryptoById(cryptoId: String, currency: String): Flow<Crypto?>

    /**
     * Refresca los datos desde la API y actualiza la base de datos local.
     * Lanza excepción si no hay conexión (la UI debe manejar el error).
     */
    suspend fun refreshCryptos()

    /**
     * Alterna el estado de favorito de una criptomoneda.
     */
    suspend fun toggleFavorite(cryptoId: String)
}
