package com.cryptovault

import android.app.Application
import com.cryptovault.di.AppContainer

/**
 * Application class que inicializa el contenedor de dependencias.
 *
 * IMPORTANTE: Registrar en AndroidManifest.xml con android:name=".CryptoVaultApp"
 */
class CryptoVaultApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
