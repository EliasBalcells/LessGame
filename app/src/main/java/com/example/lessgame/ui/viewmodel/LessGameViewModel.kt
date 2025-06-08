package com.example.lessgame.ui.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lessgame.data.datastore.DataStoreManager
import com.example.lessgame.domain.Coordinate
import com.example.lessgame.domain.GameBoard
import com.example.lessgame.domain.Player
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.random.Random

enum class MoveResult { SUCCESS, INVALID, OVERBUDGET }

class LessGameViewModel(
    application: Application,
    private val saved: SavedStateHandle
) : AndroidViewModel(application) {

    private val appContext = application

    // —————————————————————————————————————————————————————————————————
    // Estado de juego
    var board by mutableStateOf(GameBoard.initial()); private set
    var boardVersion by mutableIntStateOf(0); private set
    var playerCount by mutableIntStateOf(2); private set
    var current by mutableStateOf(Player.White); private set
    var millisLeft by mutableStateOf<Long?>(null); private set
    var winner by mutableStateOf<Player?>(null); private set
    var isDraw by mutableStateOf(false); private set

    /** Indica si la partida ha terminado por victoria o empate */
    val isFinished: Boolean
        get() = (winner != null) || isDraw

    private var partialMoves = 0
    private var timer: CountDownTimer? = null
    private val _playLog = MutableStateFlow<List<String>>(emptyList())
    val playLog: StateFlow<List<String>> = _playLog

    /** Texto completo del log, útil para la pantalla de resultado */
    val logText: String
        get() = _playLog.value.joinToString(separator = "\n")

    init {
        viewModelScope.launch {
            // Carga los ajustes guardados (o valores por defecto)
            val numPlayers = DataStoreManager.getPlayerCount(appContext).first()
            val timed      = DataStoreManager.getTimed(appContext).first()
            val seconds    = DataStoreManager.getSeconds(appContext).first()
            // Aplica la configuración y arranca la partida
            configure(numPlayers, timed, seconds)
        }
    }

    /**
     * Configura una nueva partida:
     * - Reinicia el _playLog_ para arrancar limpio
     * - Reinicia tablero, temporizador y turnos
     * - Escritura de la línea inicial
     */
    fun configure(
        numPlayers: Int = 2,
        timed: Boolean = false,
        seconds: Int? = 60
    ) {
        _playLog.value = emptyList()
        _playLog.value = listOf("Inicio ${LocalDateTime.now()} – $numPlayers jugadores")
        playerCount = numPlayers
        board = GameBoard.initial()
        boardVersion++
        current = Player.White
        partialMoves = 0
        saved["FINISHED"] = false
        saved["TIME"] = false
        timer?.cancel()
        millisLeft = if (timed) seconds!! * 1_000L else null
        winner = null
        isDraw = false

        if (timed) startTimer(seconds!!)
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
        recordLog("Humano: $from → $to ($cost)")

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

    /** Turno de la IA (Black): usa la heurística */
    private fun iaTurn() {
        var spent = 0
        while (spent < 3) {
            val move = findBestMove(spent) ?: break
            board.movePiece(move.piece, move.dest)
            boardVersion++
            spent += move.cost
            recordLog("IA: ${move.piece.coordinate} → ${move.dest} (${move.cost})")
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
        recordLog("Fin por objetivo: ${current.name}")
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
        recordLog("Fin por tiempo; ganador=${winner?.name ?: "Empate"}")
    }

    /** Añade una línea al log y dispara el StateFlow */
    private fun recordLog(line: String) {
        _playLog.value = _playLog.value + line
    }

    private data class IaMove(
        val piece: com.example.lessgame.domain.Piece,
        val dest: Coordinate,
        val cost: Int,
        val score: Int
    )
}
