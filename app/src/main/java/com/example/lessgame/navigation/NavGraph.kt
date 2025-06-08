package com.example.lessgame.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lessgame.data.repository.PartidaRepository
import com.example.lessgame.ui.screens.*
import com.example.lessgame.ui.viewmodel.LessGameViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val vm: LessGameViewModel = viewModel()
    val context = LocalContext.current
    val repo = remember { PartidaRepository(context) }

    NavHost(
        navController = navController,
        startDestination = NavDest.Menu.route
    ) {
        //Menú principal
        composable(NavDest.Menu.route) {
            MenuScreen(navController, vm)
        }

        //Configuración
        composable(NavDest.Config.route) {
            ConfigScreen(navController, vm)
        }

        // Juego
        composable(NavDest.Game.route) {
            GameScreenResponsive(navController, vm)
        }

        //Resultados
        composable(NavDest.Result.route) {
            ResultScreen(navController, vm)
        }

        //Ayuda
        composable(NavDest.Help.route) {
            HelpScreen(navController)
        }

        //Consulta de partidas
        composable(NavDest.Partidas.route) {
             HistoryScreenResponsive(navController, repo)
        }
    }
}
