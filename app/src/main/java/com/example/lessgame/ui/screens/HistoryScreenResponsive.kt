package com.example.lessgame.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.lessgame.data.repository.PartidaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenResponsive(
    nav: NavHostController,     // controller raíz
    repo: PartidaRepository
) {
    val windowSize  = rememberWindowSize()
    val orientation = LocalConfiguration.current.orientation

    // NavController interno para la navegación lista → detalle
    val innerNav = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta de partidas") },
                navigationIcon = {
                    IconButton(onClick = {
                        // 1) Si innerNav tiene donde retroceder, lo hace
                        // 2) Si no (está en lista), retrocede en el nav raíz
                        if (!innerNav.navigateUp()) {
                            nav.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (windowSize == WindowSize.Compact) {
                // Smartphone
                NavHost(
                    navController    = innerNav,
                    startDestination = "list",
                    modifier         = Modifier.fillMaxSize()
                ) {
                    composable("list") {
                        PartidasListScreen(repo) { id ->
                            innerNav.navigate("detail/$id")
                        }
                    }
                    composable(
                        route = "detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        PartidaDetailScreen(id, repo)
                    }
                }
            } else {
                //Tablet
                val partidas by repo.partidasFlow.collectAsState(initial = emptyList())
                var selectedId by remember { mutableLongStateOf(partidas.firstOrNull()?.id ?: 0L) }

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            PartidasListScreen(repo) { id ->
                                selectedId = id
                            }
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                        )
                        Box(Modifier.weight(1f)) {
                            PartidaDetailScreen(selectedId, repo)
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f)) {
                            PartidasListScreen(repo) { id ->
                                selectedId = id
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                        )
                        Box(Modifier.weight(1f)) {
                            PartidaDetailScreen(selectedId, repo)
                        }
                    }
                }
            }
        }
    }
}
