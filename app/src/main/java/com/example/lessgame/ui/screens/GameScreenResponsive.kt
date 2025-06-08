package com.example.lessgame.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lessgame.ui.viewmodel.LessGameViewModel

@Composable
private fun GameLogPanel(vm: LessGameViewModel) {
    val logEntries by vm.playLog.collectAsState(initial = emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        logEntries.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun GameScreenResponsive(
    nav: NavHostController,
    vm: LessGameViewModel
) {
    val windowSize = rememberWindowSize()
    //smartphone
    if (windowSize == WindowSize.Compact) {
        GameScreen(nav, vm)
        return
    }

    //tablet
    val orientation = LocalConfiguration.current.orientation
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(2f)) { GameScreen(nav, vm) }
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Box(Modifier.weight(1f)) { GameLogPanel(vm) }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(2f)) { GameScreen(nav, vm) }
            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
            )
            Box(Modifier.weight(1f)) { GameLogPanel(vm) }
        }
    }
}
