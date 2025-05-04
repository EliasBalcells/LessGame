package com.example.lessgame.ui.viewmodel

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.lessgame.domain.*
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.random.Random

enum class MoveResult { SUCCESS, INVALID, OVERBUDGET }

class LessGameViewModel(
    private val saved: SavedStateHandle
) : ViewModel() {

    val handle: SavedStateHandle get() = saved
    var board by mutableStateOf(GameBoard.initial()); private set
    var boardVersion by mutableIntStateOf(0); private set
    var playerCount by mutableIntStateOf(2); private set
    var current by mutableStateOf(Player.White); private set
    var millisLeft by mutableStateOf<Long?>(null); private set
    var winner by mutableStateOf<Player?>(null); private set
    var isDraw by mutableStateOf(false); private set

    private var partialMoves = 0
    private val log = StringBuilder()
    private var timer: CountDownTimer? = null

    init {
        configure()
    }

    /**
     * Configura una nueva partida:
     * Reinicia tablero, contador y turnos
     * Establece número de jugadores, temporizador y limpia resultados anteriores
     */
    fun configure(
        numPlayers: Int = 2,
        timed: Boolean = false,
        seconds: Int = 60
    ) {
        playerCount = numPlayers
        board = GameBoard.initial()
        boardVersion++
        current = Player.White
        partialMoves = 0
        saved["FINISHED"] = false
        saved["TIME"] = false
        log.clear()
        log.appendLine("Inicio ${LocalDateTime.now()} – $numPlayers jugadores")
        if (timed) {
            millisLeft = seconds * 1_000L
            startTimer(seconds)
        } else {
            timer?.cancel()
            millisLeft = null
        }
        winner = null
        isDraw = false
    }

    private fun startTimer(seconds: Int) {
        timer?.cancel()
        timer = object : CountDownTimer(seconds * 1_000L, 1_000L) {
            override fun onTick(ms: Long) { millisLeft = ms }
            override fun onFinish() {
                millisLeft = 0L
                finishByTime()
            }
        }.also { it.start() }
    }

    /**
     * tryMove gestiona el intento de movimiento humano:
     * Valida salto y presupuesto de 3 puntos
     * Actualiza tablero, log y cede turno a la IA si corresponde
     */
    fun tryMove(from: Coordinate, to: Coordinate): MoveResult {
        val piece = board.pieceAt(from) ?: return MoveResult.INVALID
        if (piece.player != Player.White || current != Player.White) return MoveResult.INVALID
        val cost = board.validateJump(from, to) ?: return MoveResult.INVALID
        if (partialMoves + cost > 3) return MoveResult.OVERBUDGET

        board.movePiece(piece, to)
        boardVersion++
        partialMoves += cost
        log.appendLine("Humano: $from → $to ($cost)")

        if (board.hasAllInGoal(Player.White)) {
            finishByGoal()
            return MoveResult.SUCCESS
        }
        if (partialMoves >= 3) endHumanTurn()
        return MoveResult.SUCCESS
    }

    /** Finaliza turno humano y pasa a turno de IA */
    private fun endHumanTurn() {
        partialMoves = 0
        current = Player.Black
        iaTurn()
    }

    /** Turno de la IA (Black): Usa la heuristica */
    private fun iaTurn() {
        var spent = 0
        while (spent < 3) {
            val move = findBestMove(spent) ?: break
            board.movePiece(move.piece, move.dest)
            boardVersion++
            spent += move.cost
            log.appendLine("IA: ${move.piece.coordinate} → ${move.dest} (${move.cost})")
            if (board.hasAllInGoal(Player.Black)) {
                finishByGoal()
                return
            }
        }
        current = Player.White
    }

    /**
     * Busca el mejor salto legal para la IA:
     * Filtra piezas negras fuera de meta
     * Considera saltos de coste ≤ restante
     * Calcula f = 100·g + h y elige mínimo
     */
    private fun findBestMove(spent: Int): IaMove? {
        val goalCoords = setOf(
            Coordinate.of(4, 4),
            Coordinate.of(4, 5),
            Coordinate.of(5, 4),
            Coordinate.of(5, 5)
        )
        var best: IaMove? = null
        val rnd = Random(System.nanoTime())
        val deltas = listOf(
            0 to 1, 0 to 2, 0 to 3,
            1 to 0, 2 to 0, 3 to 0,
            -1 to 0, -2 to 0, -3 to 0,
            0 to -1, 0 to -2, 0 to -3
        )
        board.allPieces()
            .filter { it.player == Player.Black && it.coordinate !in goalCoords }
            .shuffled(rnd)
            .forEach { piece ->
                deltas.forEach { (dx, dy) ->
                    val dest = Coordinate.of(piece.coordinate.x + dx, piece.coordinate.y + dy)
                    val cost = board.validateJump(piece.coordinate, dest) ?: return@forEach
                    if (spent + cost > 3) return@forEach
                    val g = cost
                    val h = goalCoords.minOf { abs(dest.x - it.x) + abs(dest.y - it.y) }
                    val f = g * 100 + h
                    if (best == null || f < best!!.score) {
                        best = IaMove(piece, dest, cost, f)
                    }
                }
            }
        return best
    }

    private fun finishByGoal() {
        saved["FINISHED"] = true
        timer?.cancel()
        winner = current
        log.appendLine("Fin por objetivo: ${current.name}")
    }

    /**
     * Marca fin por tiempo:
     * Si queda pieza en zona inicial, esa persona pierde
     * Si no, gana quien tenga más piezas en meta rival
     * Empate si cuentan igual
     */
    private fun finishByTime() {
        saved["TIME"] = true
        timer?.cancel()

        val whiteStart = setOf(
            Coordinate.of(5,5), Coordinate.of(4,5),
            Coordinate.of(5,4), Coordinate.of(4,4)
        )
        val blackStart = setOf(
            Coordinate.of(0,0), Coordinate.of(1,0),
            Coordinate.of(0,1), Coordinate.of(1,1)
        )
        val whiteStuck = board.allPieces().any { it.player == Player.White && it.coordinate in whiteStart }
        val blackStuck = board.allPieces().any { it.player == Player.Black && it.coordinate in blackStart }

        winner = when {
            whiteStuck -> Player.Black
            blackStuck -> Player.White
            else -> {
                val whiteCount = board.allPieces().count { it.player == Player.White && it.coordinate in blackStart }
                val blackCount = board.allPieces().count { it.player == Player.Black && it.coordinate in whiteStart }
                when {
                    whiteCount > blackCount -> Player.White
                    blackCount > whiteCount -> Player.Black
                    else -> null
                }
            }
        }
        if (winner == null) isDraw = true
        log.appendLine("Fin por tiempo; ganador=${winner?.name ?: "Empate"}")
    }

    /** Texto del log para el result */
    val logText: String get() = log.toString()

    private data class IaMove(
        val piece: Piece,
        val dest: Coordinate,
        val cost: Int,
        val score: Int
    )
}
