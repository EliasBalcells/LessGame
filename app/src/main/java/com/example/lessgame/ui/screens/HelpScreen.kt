package com.example.lessgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    nav: NavHostController,
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Ayuda") },
            navigationIcon = {
                IconButton(onClick = { nav.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
            }
        )
    }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            Text(
                "Objetivo: Llevar tus 4 fichas a la esquina contraria.\n\n" +
                        "En tu turno dispones de hasta 3 puntos de movimiento.\n" +
                        "• Saltos rectos de casillas cuestan 1 punto por movimiento.\n" +
                        "• Saltos por encima de una ficha (blanca o negra) cuesta 1 punto.\n" +
                        "• Saltos por encima de un muro cuestan 2 puntos.\n" +
                        "• Saltos por encima de un doble muro cuestan 3 puntos.\n" +
                        "• Saltos en diagonal no son validos.\n" +
                        "Cuando gastes los 3 puntos o termines antes, pasa el turno.\n\n" +
                        "Gana quien primero coloque todas sus fichas en la meta.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
