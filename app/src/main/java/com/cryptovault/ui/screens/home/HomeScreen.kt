package com.cryptovault.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cryptovault.ui.components.CryptoListItem
import com.cryptovault.ui.legacy.MarketStatusBanner

/**
 * Pantalla principal de la aplicación.
 *
 * Muestra un listado de criptomonedas en un LazyColumn optimizado.
 * Incluye barra de búsqueda, pull-to-refresh y el banner legacy (AndroidView).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cryptos by viewModel.cryptos.collectAsState()
    val currency by viewModel.baseCurrency.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchVisible by remember { mutableStateOf(false) }

    val currencySymbol = if (currency == "eur") "€" else "$"

    // Filtrar por búsqueda
    val filteredCryptos = if (uiState.searchQuery.isBlank()) {
        cryptos
    } else {
        cryptos.filter {
            it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.symbol.contains(uiState.searchQuery, ignoreCase = true)
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CryptoVault",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Favoritos"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Barra de búsqueda (animada) ──
            AnimatedVisibility(visible = isSearchVisible) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar criptomoneda...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // ── Banner legacy (AndroidView con XML) ──
            MarketStatusBanner(
                totalCryptos = cryptos.size,
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Contenido principal ──
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Loading inicial (sin datos cacheados)
                    uiState.isLoading && cryptos.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Sin resultados de búsqueda
                    filteredCryptos.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                        Text(
                            text = "No se encontraron resultados para \"${uiState.searchQuery}\"",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Lista de criptomonedas (LazyColumn)
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredCryptos,
                                key = { it.id } // Clave única para optimización
                            ) { crypto ->
                                CryptoListItem(
                                    crypto = crypto,
                                    currencySymbol = currencySymbol,
                                    onItemClick = { onNavigateToDetail(crypto.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(crypto.id) }
                                )
                            }
                        }
                    }
                }

                // Indicador de loading superpuesto (cuando ya hay datos)
                if (uiState.isLoading && cryptos.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
