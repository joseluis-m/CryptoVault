package com.cryptovault.di

import android.content.Context
import com.cryptovault.data.local.CryptoDatabase
import com.cryptovault.data.local.datastore.PreferencesManager
import com.cryptovault.data.remote.RetrofitClient
import com.cryptovault.data.repository.CryptoRepositoryImpl
import com.cryptovault.domain.repository.CryptoRepository
import com.cryptovault.domain.repository.CurrencyPreferences

/**
 * Contenedor de inyección de dependencias MANUAL.
 *
 * Se usa inyección manual en lugar de Hilt por claridad educativa.
 * En un proyecto real, se usaría Hilt o Koin.
 *
 * Este objeto centraliza la creación de todas las dependencias,
 * asegurando que se comparten las mismas instancias (singleton).
 */
class AppContainer(context: Context) {

    // ── Base de datos ──
    private val database = CryptoDatabase.getInstance(context)
    private val cryptoDao = database.cryptoDao()

    // ── Red ──
    private val api = RetrofitClient.api

    // ── Repository (Offline-First) ──
    val cryptoRepository: CryptoRepository = CryptoRepositoryImpl(
        api = api,
        dao = cryptoDao
    )

    // ── DataStore ──
    val preferencesManager: CurrencyPreferences = PreferencesManager(context)
}
