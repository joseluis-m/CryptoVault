package com.cryptovault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cryptovault.ui.navigation.CryptoNavGraph
import com.cryptovault.ui.theme.CryptoVaultTheme
import com.cryptovault.worker.SyncPricesWorker

/**
 * Activity única de la aplicación (Single Activity Pattern).
 *
 * Responsabilidades:
 * 1. Configurar el tema Material 3.
 * 2. Crear el NavController.
 * 3. Inyectar el AppContainer al NavGraph.
 * 4. Programar el WorkManager para sincronización en segundo plano.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programar sincronización en segundo plano
        SyncPricesWorker.schedule(applicationContext)

        // Obtener el contenedor de dependencias
        val appContainer = (application as CryptoVaultApp).appContainer

        setContent {
            CryptoVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CryptoNavGraph(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }
}
