package com.example.lessgame.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lessgame.data.local.dao.PartidaDao
import com.example.lessgame.data.local.entity.Partida

@Database(entities = [Partida::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partidaDao(): PartidaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lessgame.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
