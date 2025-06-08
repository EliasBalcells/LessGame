package com.example.lessgame.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.lessgame.data.local.entity.Partida
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {
    @Insert suspend fun insert(partida: Partida)
    @Query("SELECT * FROM partidas ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Partida>>
    @Query("SELECT * FROM partidas WHERE id = :id")
    suspend fun getById(id: Long): Partida?
}
