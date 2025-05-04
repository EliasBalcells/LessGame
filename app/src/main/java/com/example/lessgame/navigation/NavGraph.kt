package com.example.lessgame.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lessgame.ui.screens.*
import com.example.lessgame.ui.viewmodel.LessGameViewModel

/** Configura el grafo de navegación, asociando cada ruta con su pantalla correspondiente */
@Composable
fun NavGraph(navController: NavHostController) {
    val vm: LessGameViewModel = viewModel()

    NavHost(
        navController    = navController,
        startDestination = NavDest.Menu.route
    ) {
        composable(NavDest.Menu.route)   { MenuScreen  (navController) }
        composable(NavDest.Config.route) { ConfigScreen(navController, vm) }
        composable(NavDest.Game.route)   { GameScreen  (navController, vm) }
        composable(NavDest.Result.route) { ResultScreen(navController, vm) }
        composable(NavDest.Help.route)   { HelpScreen  (navController) }
    }
}
