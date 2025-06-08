package com.example.lessgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.data.datastore.DataStoreManager
import com.example.lessgame.ui.viewmodel.LessGameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val storedAlias   by DataStoreManager.getAlias(context).collectAsState(initial = "Jugador")
    var alias         by rememberSaveable { mutableStateOf(storedAlias) }
    LaunchedEffect(storedAlias) { alias = storedAlias }

    val storedCount   by DataStoreManager.getPlayerCount(context).collectAsState(initial = 2)
    var playerCount   by rememberSaveable { mutableIntStateOf(storedCount) }
    LaunchedEffect(storedCount) { playerCount = storedCount }

    val storedTimed   by DataStoreManager.getTimed(context).collectAsState(initial = false)
    var timed         by rememberSaveable { mutableStateOf(storedTimed) }
    LaunchedEffect(storedTimed) { timed = storedTimed }

    val storedSeconds by DataStoreManager.getSeconds(context).collectAsState(initial = 60)
    var seconds       by rememberSaveable { mutableStateOf(storedSeconds.toString()) }
    LaunchedEffect(storedSeconds) { seconds = storedSeconds.toString() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configuración") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alias
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Alias") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Nº jugadores
            Text("Número de jugadores (2 o 4(no aplicado))")
            Row {
                listOf(2, 4).forEach { n ->
                    Row(
                        Modifier
                            .selectable(selected = playerCount == n, onClick = { playerCount = n })
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = playerCount == n, onClick = { playerCount = n })
                        Spacer(Modifier.width(4.dp))
                        Text("$n")
                    }
                }
            }

            // Control de tiempo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = timed, onCheckedChange = { timed = it })
                Spacer(Modifier.width(8.dp))
                Text("Control de tiempo")
            }

            // Segundos si activa
            if (timed) {
                OutlinedTextField(
                    value = seconds,
                    onValueChange = { input ->
                        seconds = input.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Segundos por partida") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val secs = seconds.toIntOrNull()?.coerceAtLeast(1) ?: 60
                    scope.launch {
                        DataStoreManager.setAlias(context, alias)
                        DataStoreManager.setPlayerCount(context, playerCount)
                        DataStoreManager.setTimed(context, timed)
                        DataStoreManager.setSeconds(context, secs)
                        DataStoreManager.setConfigured(context, true)
                    }
                    // Aplica en la VM
                    vm.configure(playerCount, timed, secs)
                    // Volver al menú principal
                    nav.popBackStack()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar")
            }
        }
    }
}
