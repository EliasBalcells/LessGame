package com.example.lessgame.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.data.datastore.DataStoreManager
import com.example.lessgame.navigation.NavDest
import com.example.lessgame.ui.viewmodel.LessGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    val context = LocalContext.current

    val count   by DataStoreManager.getPlayerCount(context).collectAsState(initial = 2)
    val timed   by DataStoreManager.getTimed(context).collectAsState(initial = false)
    val seconds by DataStoreManager.getSeconds(context).collectAsState(initial = 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Less Game") },
                actions = {
                    IconButton(onClick = { nav.navigate(NavDest.Config.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement   = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                //reinicia la partida con los valores de configuración
                vm.configure(count, timed, seconds)
                nav.navigate(NavDest.Game.route)
            }) {
                Text("Nueva partida")
            }
            Button(onClick = { nav.navigate(NavDest.Help.route) }) {
                Text("Ayuda")
            }
            Button(onClick = { nav.navigate(NavDest.Partidas.route) }) {
                Text("Consulta de partidas")
            }
            Button(onClick = { (nav.context as? ComponentActivity)?.finish() }) {
                Text("Salir")
            }
        }
    }
}
