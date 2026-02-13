package com.cryptovault.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para las preferencias de moneda.
 *
 * Extraer esta interfaz del PreferencesManager permite:
 * 1. Testabilidad: crear fakes en tests sin necesitar Context.
 * 2. Inversión de dependencias: los ViewModels dependen de la abstracción.
 */
interface CurrencyPreferences {
    val baseCurrencyFlow: Flow<String>
    suspend fun setBaseCurrency(currency: String)
}
