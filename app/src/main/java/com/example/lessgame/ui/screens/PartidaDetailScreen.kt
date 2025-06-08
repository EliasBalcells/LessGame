package com.example.lessgame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lessgame.data.local.entity.Partida
import com.example.lessgame.data.repository.PartidaRepository
import kotlinx.coroutines.launch

@Composable
fun PartidaDetailScreen(
    partidaId: Long,
    repo: PartidaRepository
) {
    var partida by remember { mutableStateOf<Partida?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(partidaId) {
        scope.launch { partida = repo.getById(partidaId) }
    }

    partida?.let { p ->
        val textColor = MaterialTheme.colorScheme.onSurface

        //scrollable
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Alias: ${p.alias}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Jugadores: ${p.playerCount}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Control de tiempo: ${if (p.timed) "Sí" else "No"}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Ganador: ${p.winner ?: "Empate"}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha: ${java.util.Date(p.timestamp)}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Log:\n${p.log}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
