package com.example.lessgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.lessgame.navigation.NavGraph
import com.example.lessgame.ui.theme.LessGameTheme

/**Inicia la aplicación: aplica el tema y arranca el punto de entrada compose*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LessGameTheme {
                AppEntry()
            }
        }
    }
}

/** Composable raíz que crea el NavController y lanza el NavGraph.*/
@Composable
private fun AppEntry() {
    val navController = rememberNavController()
    NavGraph(navController)
}
