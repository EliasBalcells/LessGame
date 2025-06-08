package com.example.lessgame.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lessgame.data.repository.PartidaRepository

@Composable
fun PartidasListScreen(
    repo: PartidaRepository,
    onSelect: (Long) -> Unit
) {
    val partidas by repo.partidasFlow.collectAsState(initial = emptyList())

    LazyColumn {
        items(partidas) { p ->
            ListItem(
                modifier = Modifier
                    .clickable { onSelect(p.id) },
                headlineContent = { Text(p.alias) },
                supportingContent = {
                    val resultado = when {
                        p.isDraw             -> "Empate"
                        p.winner != null     -> "Ganador: ${p.winner}"
                        else                  -> "Sin resultado"
                    }
                    Text("Resultado: $resultado")
                }
            )
            HorizontalDivider(thickness = 1.dp)
        }
    }
}

