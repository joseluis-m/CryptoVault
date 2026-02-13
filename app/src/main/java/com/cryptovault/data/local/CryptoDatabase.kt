package com.cryptovault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cryptovault.data.local.dao.CryptoDao
import com.cryptovault.data.local.entity.CryptoEntity

/**
 * Base de datos Room de la aplicación.
 *
 * Patrón Singleton para evitar múltiples instancias
 * (costoso en recursos y puede causar problemas de concurrencia).
 */
@Database(
    entities = [CryptoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {

    abstract fun cryptoDao(): CryptoDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getInstance(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    "crypto_vault_db"
                )
                    .fallbackToDestructiveMigration() // En producción usaríamos migraciones
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
