package com.cryptovault.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) que mapea la respuesta JSON de CoinGecko API.
 *
 * Endpoint: GET /coins/markets?vs_currency=usd&order=market_cap_desc
 *
 * IMPORTANTE: Este DTO es exclusivo de la capa de datos.
 * Se mapea a CryptoEntity antes de guardarse en Room,
 * y nunca se expone directamente a la UI.
 */
data class CoinDto(
    @SerializedName("id")
    val id: String,                         // "bitcoin"

    @SerializedName("symbol")
    val symbol: String,                     // "btc"

    @SerializedName("name")
    val name: String,                       // "Bitcoin"

    @SerializedName("image")
    val image: String,                      // URL del icono

    @SerializedName("current_price")
    val currentPrice: Double?,              // Precio actual

    @SerializedName("market_cap")
    val marketCap: Double?,                 // Capitalización

    @SerializedName("market_cap_rank")
    val marketCapRank: Int?,                // Ranking

    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double?,  // Cambio % 24h

    @SerializedName("high_24h")
    val high24h: Double?,                   // Máximo 24h

    @SerializedName("low_24h")
    val low24h: Double?,                    // Mínimo 24h

    @SerializedName("total_volume")
    val totalVolume: Double?                // Volumen 24h
)
