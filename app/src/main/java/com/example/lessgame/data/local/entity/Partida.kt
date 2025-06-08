package com.example.lessgame.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val playerCount: Int,
    val timed: Boolean,
    val winner: String?,
    val isDraw: Boolean,
    val log: String,
    val timestamp: Long = System.currentTimeMillis()
)
