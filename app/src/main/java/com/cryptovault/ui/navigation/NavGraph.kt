package com.cryptovault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cryptovault.di.AppContainer
import com.cryptovault.ui.screens.detail.DetailScreen
import com.cryptovault.ui.screens.detail.DetailViewModel
import com.cryptovault.ui.screens.favorites.FavoritesScreen
import com.cryptovault.ui.screens.favorites.FavoritesViewModel
import com.cryptovault.ui.screens.home.HomeScreen
import com.cryptovault.ui.screens.home.HomeViewModel
import com.cryptovault.ui.screens.settings.SettingsScreen
import com.cryptovault.ui.screens.settings.SettingsViewModel

/**
 * Rutas de navegación de la aplicación.
 */
object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{cryptoId}"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"

    fun detailRoute(cryptoId: String) = "detail/$cryptoId"
}

/**
 * Grafo de navegación principal.
 *
 * Usa Navigation Compose para navegar entre pantallas con argumentos.
 * Los ViewModels se crean con sus Factory, inyectando dependencias manualmente.
 */
@Composable
fun CryptoNavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // ── Pantalla Home ──
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    repository = appContainer.cryptoRepository,
                    preferencesManager = appContainer.preferencesManager
                )
            )
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { cryptoId ->
                    navController.navigate(Routes.detailRoute(cryptoId))
                },
                onNavigateToFavorites = {
                    navController.navigate(Routes.FAVORITES)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        // ── Pantalla Detalle (con argumento cryptoId) ──
        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("cryptoId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cryptoId = backStackEntry.arguments?.getString("cryptoId") ?: return@composable

            val viewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.Factory(
                    repository = appContainer.cryptoRepository,
                    preferencesManager = appContainer.preferencesManager,
                    cryptoId = cryptoId
                )
            )
            DetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Pantalla Favoritos ──
        composable(Routes.FAVORITES) {
            val viewModel: FavoritesViewModel = viewModel(
                factory = FavoritesViewModel.Factory(
                    repository = appContainer.cryptoRepository,
                    preferencesManager = appContainer.preferencesManager
                )
            )
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateToDetail = { cryptoId ->
                    navController.navigate(Routes.detailRoute(cryptoId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Pantalla Configuración ──
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    preferencesManager = appContainer.preferencesManager
                )
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
