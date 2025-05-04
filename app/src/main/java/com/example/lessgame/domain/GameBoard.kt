package com.example.lessgame.domain

import kotlin.math.abs
import kotlin.random.Random

class GameBoard private constructor(
    val size: Int,
    private val tiles: List<Tile>,
    private val pieces: MutableMap<Coordinate, Piece>
) {
    fun tileAt(x: Int, y: Int) = tiles[y * size + x]

    /** Devuelve la pieza en la coordenada o null si no existe. */
    fun pieceAt(c: Coordinate) = pieces[c]

    /** Devuelve la colección de todas las piezas vivas en el tablero. */
    fun allPieces(): Collection<Piece> = pieces.values

    companion object {
        private val DIRS = listOf(0 to -1, 1 to 0, 0 to 1, -1 to 0)
        private fun oppositeBit(bit: Int) = when (bit) {
            1 -> 4; 2 -> 8; 4 -> 1; 8 -> 2; else -> 0
        }

        /** función para generar el tablero con 4–8 muros aleatorios y piezas en las esquinas */
        fun initial(): GameBoard {
            val size = 6
            val tiles = MutableList(size * size) { Tile(0) }
            val numWalls = Random.nextInt(4, 9)
            var placed = 0

            while (placed < numWalls) {
                val x = Random.nextInt(1, size - 1)
                val y = Random.nextInt(1, size - 1)
                val dir = Random.nextInt(4)
                val bit = 1 shl dir
                val nx = x + DIRS[dir].first
                val ny = y + DIRS[dir].second
                val opp = oppositeBit(bit)
                val idx = y * size + x
                val nidx = ny * size + nx

                if (tiles[idx].mask and bit != 0) continue

                tiles[idx] = Tile(tiles[idx].mask or bit)
                if (Random.nextFloat() < 0.25f) {
                    tiles[nidx] = Tile(tiles[nidx].mask or opp)
                }
                placed++
            }

            val pcs = mutableMapOf<Coordinate, Piece>()
            listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1).forEach { (xx, yy) ->
                pcs[Coordinate.of(xx, yy)] = Piece(Player.Black, Coordinate.of(xx, yy))
            }
            listOf(5 to 5, 4 to 5, 5 to 4, 4 to 4).forEach { (xx, yy) ->
                pcs[Coordinate.of(xx, yy)] = Piece(Player.White, Coordinate.of(xx, yy))
            }
            return GameBoard(size, tiles, pcs)
        }
    }

    /** Valida un salto entre dos casillas y devuelve el coste (1–3) o null si no es legal*/
    fun validateJump(from: Coordinate, to: Coordinate): Int? {
        if (to.x !in 0 until size || to.y !in 0 until size) return null
        if (pieceAt(to) != null) return null

        val dx = to.x - from.x
        val dy = to.y - from.y
        val ax = abs(dx)
        val ay = abs(dy)

        if (ax + ay == 1) {
            val walls = wallsBetween(from, to)
            return 1 + walls
        }

        if ((ax == 2 && dy == 0) || (ay == 2 && dx == 0)) {
            val mid = Coordinate.of(from.x + dx / 2, from.y + dy / 2)
            val hasPiece = pieceAt(mid) != null
            val totalWalls = wallsBetween(from, mid) + wallsBetween(mid, to)
            return when {
                hasPiece && totalWalls == 0     -> 1
                !hasPiece && totalWalls in 1..2 -> 1 + totalWalls
                else                             -> null
            }
        }

        return null
    }

    /** Cuenta cuántos muros (0..2) hay entre dos casillas adyacentes */
    private fun wallsBetween(a: Coordinate, b: Coordinate): Int {
        val dx = b.x - a.x
        val dy = b.y - a.y
        if (abs(dx) + abs(dy) != 1) return 0
        val bit = when {
            dx == 1  -> 2
            dx == -1 -> 8
            dy == 1  -> 4
            else     -> 1
        }
        val tA = tileAt(a.x, a.y)
        val tB = tileAt(b.x, b.y)
        var cnt = 0
        if (tA.mask and bit != 0) cnt++
        if (tB.mask and oppositeBit(bit) != 0) cnt++
        return cnt
    }

    /** Mueve una pieza a la nueva coordenada */
    fun movePiece(piece: Piece, dest: Coordinate) {
        pieces.remove(piece.coordinate)
        pieces[dest] = piece.copy(coordinate = dest)
    }

    /** Comprueba si todas las piezas están puntod e meta */
    fun hasAllInGoal(player: Player): Boolean {
        val goal = if (player == Player.Black)
            setOf(4 to 4, 4 to 5, 5 to 4, 5 to 5)
        else
            setOf(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        return goal.all { (x, y) ->
            pieceAt(Coordinate.of(x, y))?.player == player
        }
    }
}
