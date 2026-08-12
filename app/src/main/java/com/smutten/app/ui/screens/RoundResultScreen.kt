package com.smutten.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smutten.app.viewmodel.GameViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun RoundResultPhase(viewModel: GameViewModel, onContinue: () -> Unit) {
    val uiState = viewModel.uiState
    val result = uiState.roundHistory.lastOrNull() ?: return
    val isFinalRound = uiState.roundNumber >= uiState.totalRounds

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Round ${result.roundNumber} result",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Dice: ${result.diceValues.joinToString(" + ")} = ${result.target} g target",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            result.playerData.forEach { data ->
                val player = uiState.players.find { it.id == data.playerId }
                val lost = data.playerId in result.loserIds
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(player?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                            if (lost) {
                                Text("Lost this round 😬", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text("Drank: ${formatGrams(data.consumedGrams)} g (target ${result.target} g)")
                        Text("Off by: ${formatGrams(abs(data.consumedGrams - result.target))} g")
                    }
                }
            }
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(if (isFinalRound) "See Final Result" else "Next Round")
        }
    }
}

private fun formatGrams(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else rounded.toString()
}
