package com.example.lessgame.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.data.datastore.DataStoreManager
import com.example.lessgame.data.local.entity.Partida
import com.example.lessgame.data.repository.PartidaRepository
import com.example.lessgame.domain.Coordinate
import com.example.lessgame.domain.Player
import com.example.lessgame.navigation.NavDest
import com.example.lessgame.ui.viewmodel.LessGameViewModel
import com.example.lessgame.ui.viewmodel.MoveResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min

enum class BorderSide { Left, Top, Right, Bottom }

private fun Modifier.borderSides(width: Dp, color: Color, sides: List<BorderSide>) =
    this.then(
        Modifier.drawBehind {
            val stroke = width.toPx()
            if (BorderSide.Left   in sides) drawLine(color, Offset(0f, 0f), Offset(0f, size.height), stroke)
            if (BorderSide.Top    in sides) drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), stroke)
            if (BorderSide.Right  in sides) drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
            if (BorderSide.Bottom in sides) drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), stroke)
        }
    )

/**
 * Pantalla principal del juego:
 * Observa cambios de estado (tablero y temporizador) y navega a resultados cuando se cumple FINISHED o el tiempo llega a cero
 * Dibuja una cuadrícula 6×6 con muros y fichas, adaptando tamaño al dispositivo.
 *  Permite seleccionar origen y destino, y muestra toasts en caso de movimiento inválido o exceso de coste.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    val context = LocalContext.current
    val repo    = PartidaRepository(context)
    val scope   = rememberCoroutineScope()

    val version by remember { vm::boardVersion }
    val pieces  by remember(version) { derivedStateOf { vm.board.allPieces() } }
    val finished = vm.isFinished
    val timeUp   = (vm.millisLeft ?: Long.MAX_VALUE) <= 0L

    LaunchedEffect(finished, timeUp) {
        if (finished || timeUp) {
            //guarda la partida
            val alias = DataStoreManager.getAlias(context).first()
            scope.launch {
                repo.save(
                    Partida(
                        alias        = alias,
                        playerCount  = vm.playerCount,
                        timed        = (vm.millisLeft != null),
                        winner       = vm.winner?.name,
                        isDraw       = vm.isDraw,
                        log          = vm.logText
                    )
                )
            }
            //reatardo para que se guarde la partida antes de navegar
            delay(350)
            nav.navigate(NavDest.Result.route) {
                popUpTo(NavDest.Menu.route) { inclusive = false }
            }
        }
    }

    var selected by remember { mutableStateOf<Coordinate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Turno: ${vm.current.name}") },
                actions = {
                    Text(
                        text   = vm.millisLeft?.let { "${it / 1000}s" } ?: "∞",
                        color  = if (vm.millisLeft != null) Color.Red else Color.Blue,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val boardSize = vm.board.size
            val boardSide = maxWidth.coerceAtMost(maxHeight)
            val cellSize  = boardSide / boardSize

            LazyVerticalGrid(
                columns = GridCells.Fixed(boardSize),
                modifier = Modifier.size(boardSide)
            ) {
                items(boardSize * boardSize) { index ->
                    val x     = index % boardSize
                    val y     = index / boardSize
                    val coord = Coordinate.of(x, y)
                    val piece = pieces.find { it.coordinate == coord }
                    val tile  = vm.board.tileAt(x, y)

                    val sideList = buildList<BorderSide> {
                        if (x in listOf(2, 4)) add(BorderSide.Left)
                        if (y in listOf(2, 4)) add(BorderSide.Top)
                        if (x == boardSize - 1) add(BorderSide.Right)
                        if (y == boardSize - 1) add(BorderSide.Bottom)
                    }

                    Box(
                        Modifier
                            .size(cellSize)
                            .border(0.5.dp, Color(0xFFB0B0B0))
                            .borderSides(2.dp, Color.Black, sideList)
                            .drawBehind {
                                val s = min(size.width, size.height)
                                val w = s * .14f
                                val r = CornerRadius(w / 1.3f)
                                if (tile.hasWallUp())
                                    drawRoundRect(Color(0xFF808080), Offset.Zero, Size(s, w), r)
                                if (tile.hasWallDown())
                                    drawRoundRect(Color(0xFF808080), Offset(0f, s - w), Size(s, w), r)
                                if (tile.hasWallLeft())
                                    drawRoundRect(Color(0xFF808080), Offset.Zero, Size(w, s), r)
                                if (tile.hasWallRight())
                                    drawRoundRect(Color(0xFF808080), Offset(s - w, 0f), Size(w, s), r)
                            }
                            .clickable {
                                if (piece != null && piece.player == Player.White) {
                                    selected = coord
                                } else if (selected != null) {
                                    val result = vm.tryMove(selected!!, coord)
                                    when (result) {
                                        MoveResult.SUCCESS    -> { /* no-op */ }
                                        MoveResult.INVALID    ->
                                            Toast.makeText(context, "Movimiento no válido", Toast.LENGTH_SHORT)
                                                .show()
                                        MoveResult.OVERBUDGET ->
                                            Toast.makeText(context, "Coste demasiado alto", Toast.LENGTH_SHORT)
                                                .show()
                                    }
                                    selected = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (piece != null) {
                            val circleColor = if (piece.player == Player.White) Color.White else Color.Black
                            Canvas(Modifier.size(cellSize * .6f)) {
                                drawCircle(circleColor)
                                drawCircle(Color.DarkGray, style = Stroke(width = size.minDimension * .08f))
                                if (piece.player == Player.White)
                                    drawCircle(
                                        Color(0x22000000),
                                        radius = size.minDimension / 2 + 2,
                                        center = center + Offset(1f, 1f)
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}
