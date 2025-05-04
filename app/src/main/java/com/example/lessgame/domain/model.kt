package com.example.lessgame.domain

enum class Player { White, Black }

/** Empaqueta una coordenada (x,y) en un único Int: high 16 bits = x, low 16 bits = y*/
@JvmInline
value class Coordinate(val packed: Int) {
    val x: Int get() = packed shr 16
    val y: Int get() = packed and 0xFFFF
    override fun toString() = "($x,$y)"
    companion object {
        fun of(x: Int, y: Int): Coordinate = Coordinate((x shl 16) or (y and 0xFFFF))
    }
}

/** Ficha en tablero, con jugador y ubicación. */
data class Piece(
    val player: Player,
    val coordinate: Coordinate
)

/** Una casilla con máscara de 4 bits para muros:*/
data class Tile(val mask: Int) {
    fun hasWallUp()    = mask and 1  != 0
    fun hasWallRight() = mask and 2  != 0
    fun hasWallDown()  = mask and 4  != 0
    fun hasWallLeft()  = mask and 8  != 0
}
