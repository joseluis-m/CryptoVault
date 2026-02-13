package com.cryptovault.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cryptovault.CryptoVaultApp
import java.util.concurrent.TimeUnit

/**
 * Worker de WorkManager para sincronización de precios en segundo plano.
 *
 * Se ejecuta periódicamente (mínimo cada 15 minutos) para mantener
 * los precios actualizados incluso cuando la app no está en primer plano.
 *
 * Flujo:
 * 1. WorkManager programa la tarea con restricciones (requiere internet).
 * 2. El Worker obtiene el Repository del AppContainer.
 * 3. Llama a refreshCryptos() que actualiza Room.
 * 4. La próxima vez que la UI se abra, los datos estarán frescos.
 */
class SyncPricesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando sincronización de precios en segundo plano...")

            // Obtener el Repository desde el AppContainer
            val appContainer = (applicationContext as CryptoVaultApp).appContainer
            val repository = appContainer.cryptoRepository

            // Refrescar datos desde la API → Room
            repository.refreshCryptos()

            Log.d(TAG, "Sincronización completada exitosamente")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}")
            // Reintentar si falla (WorkManager reintentará con backoff exponencial)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncPricesWorker"
        private const val UNIQUE_WORK_NAME = "sync_crypto_prices"

        /**
         * Programa la tarea periódica de sincronización.
         *
         * Debe llamarse desde el Application.onCreate() o desde MainActivity.
         *
         * Configuración:
         * - Intervalo: 15 minutos (mínimo permitido por WorkManager)
         * - Restricción: requiere conexión a internet
         * - Política: KEEP (no duplicar si ya existe)
         * - Backoff: exponencial de 10 minutos en caso de error
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Solo con internet
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncPricesWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // No duplicar
                syncRequest
            )

            Log.d(TAG, "Tarea de sincronización programada cada 15 minutos")
        }
    }
}
