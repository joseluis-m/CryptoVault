package com.cryptovault.data.remote.api

import com.cryptovault.data.remote.dto.CoinDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz Retrofit para la API de CoinGecko.
 *
 * Documentación: https://www.coingecko.com/en/api/documentation
 *
 * Usamos el endpoint /coins/markets que devuelve una lista paginada
 * con precios, market cap, volumen y cambios porcentuales.
 *
 * Se pide en USD y EUR por separado para soportar cambio de moneda base.
 */
interface CoinGeckoApi {

    /**
     * Obtiene la lista de criptomonedas con datos de mercado.
     *
     * @param vsCurrency Moneda base ("usd" o "eur")
     * @param order Orden de los resultados (por defecto: market_cap_desc)
     * @param perPage Número de resultados por página
     * @param page Número de página
     * @param sparkline Incluir datos de sparkline (no los necesitamos)
     */
    @GET("coins/markets")
    suspend fun getCoinsMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinDto>
}
