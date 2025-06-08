package com.example.lessgame.ui.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.min

enum class WindowSize { Compact, Medium, Expanded }

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberWindowSize(): WindowSize {
    val config = LocalConfiguration.current
    val smallestDp = min(config.screenWidthDp, config.screenHeightDp)
    return when {
        smallestDp < 600  -> WindowSize.Compact
        smallestDp < 840  -> WindowSize.Medium
        else              -> WindowSize.Expanded
    }
}
