package com.example.lessgame.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore("settings")

object DataStoreManager {
    private val ALIAS_KEY       = stringPreferencesKey("alias")

    private val PLAYER_COUNT_KEY   = intPreferencesKey    ("player_count")
    private val TIMED_KEY          = booleanPreferencesKey("timed")
    private val SECONDS_KEY        = intPreferencesKey    ("seconds")
    private val CONFIGURED_KEY     = booleanPreferencesKey("configured")

    suspend fun setAlias        (context: Context, alias: String) =
        context.dataStore.edit { it[ALIAS_KEY] = alias }

    suspend fun setPlayerCount  (context: Context, value: Int)  =
        context.dataStore.edit { it[PLAYER_COUNT_KEY] = value }

    suspend fun setTimed        (context: Context, value: Boolean) =
        context.dataStore.edit { it[TIMED_KEY] = value }

    suspend fun setSeconds      (context: Context, value: Int?)  =
        context.dataStore.edit { it[SECONDS_KEY] = value as Int }

    suspend fun setConfigured   (context: Context, value: Boolean) =
        context.dataStore.edit { it[CONFIGURED_KEY] = value }

    fun getAlias(context: Context): Flow<String> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[ALIAS_KEY] ?: "Jugador" }

    fun getPlayerCount(context: Context): Flow<Int> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[PLAYER_COUNT_KEY] ?: 2 }

    fun getTimed(context: Context): Flow<Boolean> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[TIMED_KEY] == true }

    fun getSeconds(context: Context): Flow<Int> =
        context.dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[SECONDS_KEY] ?: 60 }

}
