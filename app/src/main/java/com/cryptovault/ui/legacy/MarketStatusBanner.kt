package com.cryptovault.ui.legacy

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cryptovault.R

/**
 * Componente legacy: integración de un layout XML dentro de Compose.
 *
 * Usa AndroidView para inflar el layout XML (layout_market_status.xml)
 * y actualizarlo de forma reactiva cuando cambian los parámetros.
 *
 * Este patrón es necesario cuando:
 * - Se migra gradualmente de XML a Compose.
 * - Se usan componentes de terceros sin versión Compose.
 */
@Composable
fun MarketStatusBanner(
    totalCryptos: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        // factory: se ejecuta UNA vez para crear la View
        factory = { context ->
            LayoutInflater.from(context).inflate(
                R.layout.layout_market_status,
                null,
                false
            ) as LinearLayout
        },
        // update: se ejecuta cada vez que cambian los parámetros de Compose
        update = { view ->
            val tvStatus = view.findViewById<TextView>(R.id.tvMarketStatus)
            val tvCount = view.findViewById<TextView>(R.id.tvCryptoCount)
            val indicator = view.findViewById<View>(R.id.statusIndicator)

            if (isLoading) {
                tvStatus.text = "Sincronizando..."
                tvCount.text = "Actualizando precios"
                // Cambiar indicador a naranja durante la carga
                (indicator.background as? GradientDrawable)?.setColor(0xFFFFB74D.toInt())
            } else {
                tvStatus.text = "Mercado Activo"
                tvCount.text = "$totalCryptos criptomonedas"
                // Indicador verde cuando está listo
                (indicator.background as? GradientDrawable)?.setColor(0xFF00C853.toInt())
            }
        }
    )
}
