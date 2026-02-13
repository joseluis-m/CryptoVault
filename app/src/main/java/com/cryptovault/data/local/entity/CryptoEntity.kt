package com.cryptovault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa una criptomoneda en la base de datos local.
 *
 * Esta es la "Single Source of Truth" (SSoT): la UI observa esta tabla
 * mediante Flow, y el Repository se encarga de actualizarla desde la API.
 *
 * IMPORTANTE: Esta clase NO se expone directamente a la UI.
 * Se mapea al modelo de dominio [com.cryptovault.domain.model.Crypto].
 */
@Entity(tableName = "cryptos")
data class CryptoEntity(
    @PrimaryKey
    val id: String,                    // ej: "bitcoin"
    val symbol: String,                // ej: "btc"
    val name: String,                  // ej: "Bitcoin"
    val imageUrl: String,              // URL del icono
    val currentPriceUsd: Double,       // Precio en USD
    val currentPriceEur: Double,       // Precio en EUR
    val marketCap: Double,             // Capitalización de mercado
    val priceChangePercentage24h: Double, // Cambio % 24h
    val rank: Int,                     // Ranking por market cap
    val high24h: Double,               // Máximo 24h (USD)
    val low24h: Double,                // Mínimo 24h (USD)
    val totalVolume: Double,           // Volumen total 24h
    val isFavorite: Boolean = false,   // Marcado como favorito por el usuario
    val lastUpdated: Long = System.currentTimeMillis() // Timestamp de última actualización
)
