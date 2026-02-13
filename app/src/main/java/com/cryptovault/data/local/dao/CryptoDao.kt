package com.cryptovault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cryptovault.data.local.entity.CryptoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) de Room para la tabla de criptomonedas.
 *
 * Puntos clave:
 * - Las queries con Flow permiten observación reactiva desde el ViewModel.
 * - UPSERT (INSERT + ON_CONFLICT_REPLACE) mantiene los datos actualizados.
 * - La query getAllCryptos() es la base del patrón Offline-First:
 *   Room emite automáticamente cuando los datos cambian.
 */
@Dao
interface CryptoDao {

    /**
     * Obtiene todas las criptomonedas ordenadas por ranking.
     * Retorna Flow → la UI se actualiza automáticamente cuando Room inserta/actualiza datos.
     */
    @Query("SELECT * FROM cryptos ORDER BY rank ASC")
    fun getAllCryptos(): Flow<List<CryptoEntity>>

    /**
     * Obtiene solo las criptos marcadas como favoritas.
     */
    @Query("SELECT * FROM cryptos WHERE isFavorite = 1 ORDER BY rank ASC")
    fun getFavorites(): Flow<List<CryptoEntity>>

    /**
     * Obtiene una cripto por su ID (para la pantalla de detalle).
     */
    @Query("SELECT * FROM cryptos WHERE id = :cryptoId")
    fun getCryptoById(cryptoId: String): Flow<CryptoEntity?>

    /**
     * Inserta o actualiza una lista de criptos.
     * IGNORE para no sobreescribir el campo isFavorite del usuario.
     * Por eso usamos updatePrices() aparte.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cryptos: List<CryptoEntity>)

    /**
     * Actualiza los precios y datos de mercado sin tocar el campo isFavorite.
     * Esta es la clave del Offline-First: actualizamos solo los datos de la API.
     */
    @Query("""
        UPDATE cryptos SET 
            currentPriceUsd = :priceUsd,
            currentPriceEur = :priceEur,
            marketCap = :marketCap,
            priceChangePercentage24h = :priceChange24h,
            rank = :rank,
            high24h = :high24h,
            low24h = :low24h,
            totalVolume = :totalVolume,
            imageUrl = :imageUrl,
            lastUpdated = :lastUpdated
        WHERE id = :id
    """)
    suspend fun updatePrices(
        id: String,
        priceUsd: Double,
        priceEur: Double,
        marketCap: Double,
        priceChange24h: Double,
        rank: Int,
        high24h: Double,
        low24h: Double,
        totalVolume: Double,
        imageUrl: String,
        lastUpdated: Long
    )

    /**
     * Alterna el estado de favorito de una criptomoneda.
     */
    @Query("UPDATE cryptos SET isFavorite = NOT isFavorite WHERE id = :cryptoId")
    suspend fun toggleFavorite(cryptoId: String)

    /**
     * Verifica si existen datos en la tabla (para saber si es la primera carga).
     */
    @Query("SELECT COUNT(*) FROM cryptos")
    suspend fun getCount(): Int
}
