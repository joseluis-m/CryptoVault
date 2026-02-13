package com.cryptovault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cryptovault.domain.model.Crypto
import com.cryptovault.ui.theme.PriceDown
import com.cryptovault.ui.theme.PriceUp
import java.text.NumberFormat
import java.util.Locale

/**
 * Componente reutilizable para un elemento de la lista de criptomonedas.
 *
 * Usa Coil (AsyncImage) para cargar las imágenes de forma eficiente.
 * Muestra: icono, nombre, símbolo, precio actual, cambio % 24h y botón favorito.
 */
@Composable
fun CryptoListItem(
    crypto: Crypto,
    currencySymbol: String,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priceColor = if (crypto.priceChangePercentage24h >= 0) PriceUp else PriceDown
    val priceFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = if (crypto.currentPrice < 1) 6 else 2
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ranking
            Text(
                text = "${crypto.rank}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp)
            )

            // Icono de la criptomoneda (Coil AsyncImage)
            AsyncImage(
                model = crypto.imageUrl,
                contentDescription = "${crypto.name} icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y símbolo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = crypto.symbol.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Precio y cambio %
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencySymbol${priceFormat.format(crypto.currentPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "%+.2f%%",
                        crypto.priceChangePercentage24h
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = priceColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Botón de favorito
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (crypto.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (crypto.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (crypto.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
