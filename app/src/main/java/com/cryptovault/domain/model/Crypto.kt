package com.cryptovault.domain.model

/**
 * Modelo de dominio de una Criptomoneda.
 *
 * IMPORTANTE: Este modelo NO tiene anotaciones de Room, Gson, ni ningún
 * framework. Es un POJO limpio que representa el concepto de negocio.
 *
 * La capa de datos (Entity/DTO) se mapea a este modelo antes de llegar
 * a la capa de UI. Esto asegura la separación de capas de Clean Architecture.
 */
data class Crypto(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: Double,
    val marketCap: Double,
    val priceChangePercentage24h: Double,
    val rank: Int,
    val high24h: Double,
    val low24h: Double,
    val totalVolume: Double,
    val isFavorite: Boolean
)
