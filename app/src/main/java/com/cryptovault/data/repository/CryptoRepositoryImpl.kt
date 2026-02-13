package com.cryptovault.data.repository

import com.cryptovault.data.local.dao.CryptoDao
import com.cryptovault.data.local.entity.CryptoEntity
import com.cryptovault.data.remote.api.CoinGeckoApi
import com.cryptovault.data.remote.dto.CoinDto
import com.cryptovault.domain.model.Crypto
import com.cryptovault.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del Repository con patrón OFFLINE-FIRST.
 *
 * ══════════════════════════════════════════════════════════
 * FLUJO DE DATOS (Single Source of Truth):
 * ══════════════════════════════════════════════════════════
 *
 * 1. La UI observa SIEMPRE la base de datos local (Room) mediante Flow.
 * 2. Cuando se llama a refreshCryptos():
 *    a. Se pide a la API (CoinGecko) los datos más recientes.
 *    b. Se mapean los DTOs a Entities.
 *    c. Se insertan/actualizan en Room.
 *    d. Room notifica automáticamente a los Flows → la UI se actualiza.
 * 3. Si no hay internet, la UI muestra los datos cacheados sin crashear.
 *
 * La UI NUNCA recibe datos directos de la API.
 * Room es la ÚNICA fuente de verdad (SSoT).
 * ══════════════════════════════════════════════════════════
 */
class CryptoRepositoryImpl(
    private val api: CoinGeckoApi,
    private val dao: CryptoDao
) : CryptoRepository {

    /**
     * Obtiene todas las criptos desde la BD local y las mapea al modelo de dominio.
     * El parámetro currency determina qué precio mostrar.
     */
    override fun getAllCryptos(currency: String): Flow<List<Crypto>> {
        return dao.getAllCryptos().map { entities ->
            entities.map { it.toDomain(currency) }
        }
    }

    /**
     * Obtiene solo las favoritas, mapeadas al modelo de dominio.
     */
    override fun getFavorites(currency: String): Flow<List<Crypto>> {
        return dao.getFavorites().map { entities ->
            entities.map { it.toDomain(currency) }
        }
    }

    /**
     * Obtiene una cripto específica por ID.
     */
    override fun getCryptoById(cryptoId: String, currency: String): Flow<Crypto?> {
        return dao.getCryptoById(cryptoId).map { entity ->
            entity?.toDomain(currency)
        }
    }

    /**
     * MÉTODO CLAVE del Offline-First:
     * 1. Llama a la API para obtener datos frescos en USD y EUR.
     * 2. Inserta las nuevas criptos (si no existían).
     * 3. Actualiza los precios de las existentes SIN tocar isFavorite.
     *
     * Si la API falla (sin internet), lanza la excepción para que
     * el ViewModel la capture y muestre un mensaje al usuario.
     * Pero los datos cacheados siguen visibles gracias al Flow de Room.
     */
    override suspend fun refreshCryptos() {
        // Paso 1: Obtener datos de la API en ambas monedas
        val coinsUsd = api.getCoinsMarkets(vsCurrency = "usd", perPage = 50)
        val coinsEur = api.getCoinsMarkets(vsCurrency = "eur", perPage = 50)

        // Paso 2: Crear un mapa de precios EUR indexado por ID para lookup rápido
        val eurPriceMap = coinsEur.associateBy { it.id }

        // Paso 3: Mapear DTOs a Entities y guardar en Room
        val entities = coinsUsd.map { dto ->
            dto.toEntity(eurPrice = eurPriceMap[dto.id]?.currentPrice ?: 0.0)
        }

        // Paso 4: Insertar nuevas criptos (IGNORE si ya existen)
        dao.insertAll(entities)

        // Paso 5: Actualizar precios de las existentes
        entities.forEach { entity ->
            dao.updatePrices(
                id = entity.id,
                priceUsd = entity.currentPriceUsd,
                priceEur = entity.currentPriceEur,
                marketCap = entity.marketCap,
                priceChange24h = entity.priceChangePercentage24h,
                rank = entity.rank,
                high24h = entity.high24h,
                low24h = entity.low24h,
                totalVolume = entity.totalVolume,
                imageUrl = entity.imageUrl,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    /**
     * Alterna favorito directamente en la BD.
     */
    override suspend fun toggleFavorite(cryptoId: String) {
        dao.toggleFavorite(cryptoId)
    }

    // ══════════════════════════════════════════════════════════
    // FUNCIONES DE MAPEO (Mapper functions)
    // ══════════════════════════════════════════════════════════

    /**
     * Mapea un DTO de la API a una Entity de Room.
     * Los valores nulos de la API se reemplazan por valores por defecto.
     */
    private fun CoinDto.toEntity(eurPrice: Double): CryptoEntity {
        return CryptoEntity(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = image,
            currentPriceUsd = currentPrice ?: 0.0,
            currentPriceEur = eurPrice,
            marketCap = marketCap ?: 0.0,
            priceChangePercentage24h = priceChangePercentage24h ?: 0.0,
            rank = marketCapRank ?: 0,
            high24h = high24h ?: 0.0,
            low24h = low24h ?: 0.0,
            totalVolume = totalVolume ?: 0.0,
            isFavorite = false // No modificar, insertAll usa IGNORE
        )
    }

    /**
     * Mapea una Entity de Room al modelo de dominio.
     * Selecciona el precio correcto según la moneda base.
     */
    private fun CryptoEntity.toDomain(currency: String): Crypto {
        return Crypto(
            id = id,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            currentPrice = if (currency == "eur") currentPriceEur else currentPriceUsd,
            marketCap = marketCap,
            priceChangePercentage24h = priceChangePercentage24h,
            rank = rank,
            high24h = high24h,
            low24h = low24h,
            totalVolume = totalVolume,
            isFavorite = isFavorite
        )
    }
}
