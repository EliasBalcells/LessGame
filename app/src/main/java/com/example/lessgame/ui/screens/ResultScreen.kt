package com.example.lessgame.ui.screens

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.domain.Player
import com.example.lessgame.navigation.NavDest
import com.example.lessgame.ui.viewmodel.LessGameViewModel
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Muestra la pantalla de resultados:
 * Fecha y hora de finalización
 * Historial de movimientos (log)
 * Resultado final (victoria, derrota o empate)
 * Formulario para enviar el log por e-mail
 * Botones para nueva partida o salir
 */
@Composable
fun ResultScreen(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    val context       = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape   = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val dateTime      = remember { java.time.LocalDateTime.now() }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")

    var email by remember { mutableStateOf("") }

    val resultText = when {
        vm.isDraw                -> "¡Empate!"
        vm.winner == Player.White -> "¡Has ganado!"
        vm.winner == Player.Black -> "Perdiste..."
        else                      -> ""
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RESULTADOS PARTIDA") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLandscape) {
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Día y hora")
                        OutlinedTextField(
                            value = dateTime.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Valores del Log")
                        val scroll = rememberScrollState()
                        OutlinedTextField(
                            value = vm.logText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scroll),
                            maxLines = Int.MAX_VALUE
                        )
                    }
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Resultado")
                        OutlinedTextField(
                            value = resultText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("E-mail destinatario")
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (email.isNotBlank()) {
                                val subject = "Resultados LESS ${dateTime.format(dateFormatter)}"
                                val body    = vm.logText
                                val intent  = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, body)
                                }
                                context.startActivity(Intent.createChooser(intent, "Enviar e-mail…"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Enviar e-mail") }

                    Button(
                        onClick = {
                            nav.navigate(NavDest.Menu.route) {
                                popUpTo(NavDest.Menu.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Nueva partida") }

                    Button(
                        onClick = { (context as? androidx.activity.ComponentActivity)?.finish() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Salir") }
                }

            } else {
                Column(
                    Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text("Día y hora")
                    OutlinedTextField(
                        value = dateTime.format(dateFormatter),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Valores del Log")
                    val scroll = rememberScrollState()
                    OutlinedTextField(
                        value = vm.logText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 150.dp)
                            .verticalScroll(scroll),
                        maxLines = Int.MAX_VALUE
                    )
                    Text("Resultado")
                    OutlinedTextField(
                        value = resultText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("E-mail destinatario")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    val subject =
                                        "Resultados LESS ${dateTime.format(dateFormatter)}"
                                    val body = vm.logText
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:".toUri()
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                                        putExtra(Intent.EXTRA_SUBJECT, subject)
                                        putExtra(Intent.EXTRA_TEXT, body)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            "Enviar e-mail…"
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Enviar e-mail") }

                        Button(
                            onClick = {
                                nav.navigate(NavDest.Menu.route) {
                                    popUpTo(NavDest.Menu.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Nueva partida") }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { (context as? androidx.activity.ComponentActivity)?.finish() },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.6f)

                    ) { Text("Salir") }

                }
            }
        }
    }
}
