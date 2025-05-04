package com.example.lessgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.navigation.NavDest

@OptIn(ExperimentalMaterial3Api::class)
/** Muestra los botones para redirigir de pantalla */
@Composable
fun MenuScreen(
    nav: NavHostController,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Less Game") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { nav.navigate(NavDest.Config.route) }) {
                Text("Nueva partida")
            }
            Button(onClick = { nav.navigate(NavDest.Help.route) }) {
                Text("Ayuda")
            }
            Button(onClick = { (nav.context as? androidx.activity.ComponentActivity)?.finish() }) {
                Text("Salir")
            }
        }
    }
}
