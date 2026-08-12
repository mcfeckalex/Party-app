package com.smutten.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smutten.app.viewmodel.GameViewModel

@Composable
fun GameOverPhase(
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit,
    onNewPlayers: () -> Unit
) {
    val uiState = viewModel.uiState
    val losers = uiState.players.filter { it.id in uiState.overallLoserIds }
    val loserNames = losers.joinToString(" & ") { it.name }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏁", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "$loserNames must finish their drink!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text("Rounds lost", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        uiState.players.forEach { player ->
            val losses = uiState.roundLossCounts[player.id] ?: 0
            Text("${player.name}: $losses / ${uiState.totalRounds}")
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Play Again (same players)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onNewPlayers, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("New Players")
        }
    }
}
