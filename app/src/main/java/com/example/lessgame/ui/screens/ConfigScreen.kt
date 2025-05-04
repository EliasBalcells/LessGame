package com.example.lessgame.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.navigation.NavDest
import com.example.lessgame.ui.viewmodel.LessGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    var playerCount by rememberSaveable { mutableIntStateOf(2) }
    var timed       by rememberSaveable { mutableStateOf(false) }
    var seconds     by rememberSaveable { mutableStateOf("60") }

    val configuration = LocalConfiguration.current
    val isLandscape   = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(topBar = { TopAppBar(title = { Text("Configuración") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Número de jugadores (De momento solo 2)")
            Row {
                listOf(2, 4).forEach { num ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (playerCount == num),
                                onClick  = { playerCount = num }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (playerCount == num),
                            onClick  = { playerCount = num }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("$num")
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = timed, onCheckedChange = { timed = it })
                Spacer(Modifier.width(8.dp))
                Text("Control de tiempo")
            }

            if (timed && isLandscape) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { seconds = it.filter(Char::isDigit).take(3) },
                        label = { Text("Segundos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    )
                    Button(
                        onClick = {
                            val s = seconds.toIntOrNull()?.coerceAtLeast(1) ?: 60
                            vm.configure(playerCount, timed, s)
                            nav.navigate(NavDest.Game.route)
                        },
                        modifier = Modifier
                            .height(56.dp)
                    ) {
                        Text("Jugar")
                    }
                }
            } else {
                if (timed) {
                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { seconds = it.filter(Char::isDigit).take(3) },
                        label = { Text("Segundos por partida") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val s = seconds.toIntOrNull()?.coerceAtLeast(1) ?: 60
                        vm.configure(playerCount, timed, s)
                        nav.navigate(NavDest.Game.route)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Jugar")
                }
            }
        }
    }
}