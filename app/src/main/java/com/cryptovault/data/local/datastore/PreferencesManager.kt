package com.cryptovault.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cryptovault.domain.repository.CurrencyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gestor de preferencias del usuario usando Jetpack DataStore.
 *
 * Almacena configuraciones simples como la moneda base (USD/EUR).
 * DataStore reemplaza a SharedPreferences con soporte nativo para
 * corrutinas y Flow, lo que permite observar cambios reactivamente.
 *
 * Implementa CurrencyPreferences para permitir testing con fakes.
 */

// Extensión para crear el DataStore (singleton por delegación)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "crypto_vault_preferences"
)

class PreferencesManager(private val context: Context) : CurrencyPreferences {

    companion object {
        private val CURRENCY_KEY = stringPreferencesKey("base_currency")
        const val DEFAULT_CURRENCY = "usd"
    }

    /**
     * Flow que emite la moneda base actual.
     * La UI observa este Flow para reaccionar a cambios en la configuración.
     */
    override val baseCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: DEFAULT_CURRENCY
    }

    /**
     * Guarda la moneda base seleccionada por el usuario.
     * Al modificar DataStore, baseCurrencyFlow emite automáticamente el nuevo valor.
     */
    override suspend fun setBaseCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency
        }
    }
}
