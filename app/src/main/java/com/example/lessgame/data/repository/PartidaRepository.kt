package com.example.lessgame.data.repository

import android.content.Context
import com.example.lessgame.data.local.dao.PartidaDao
import com.example.lessgame.data.local.db.AppDatabase
import com.example.lessgame.data.local.entity.Partida
import kotlinx.coroutines.flow.Flow

class PartidaRepository(context: Context) {
    private val dao: PartidaDao = AppDatabase.getInstance(context).partidaDao()
    val partidasFlow: Flow<List<Partida>> = dao.getAll()

    suspend fun save(partida: Partida) = dao.insert(partida)
    suspend fun getById(id: Long): Partida? = dao.getById(id)
}
