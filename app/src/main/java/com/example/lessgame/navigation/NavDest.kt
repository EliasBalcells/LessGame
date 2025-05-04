package com.example.lessgame.navigation

/** Rutas de navegación */
enum class NavDest(val route: String) {
    Menu    ("menu"),        // pantalla inicial
    Config  ("config"),
    Game    ("game"),
    Result  ("result"),
    Help    ("help")
}
