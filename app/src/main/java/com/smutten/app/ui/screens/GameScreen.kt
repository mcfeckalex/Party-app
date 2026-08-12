package com.smutten.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smutten.app.model.GamePhase
import com.smutten.app.ui.components.DiceFace
import com.smutten.app.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel, onExitToHome: () -> Unit) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle(uiState.phase, uiState.roundNumber, uiState.totalRounds)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (uiState.phase) {
                GamePhase.SETUP -> {
                    Text("Set up players first.")
                }
                GamePhase.DICE_ROLL -> DiceRollPhase(
                    roundNumber = uiState.roundNumber,
                    totalRounds = uiState.totalRounds,
                    onRollFinished = { viewModel.rollDice() }
                )
                GamePhase.WEIGH_BEFORE -> {
                    val player = uiState.players.getOrNull(uiState.activePlayerIndex)
                    if (player != null) {
                        WeighPhase(
                            playerName = player.name,
                            playerNumber = uiState.activePlayerIndex + 1,
                            totalPlayers = uiState.players.size,
                            target = uiState.target,
                            instruction = "Weigh ${player.name}'s drink now, before drinking.",
                            onSubmit = { grams -> viewModel.submitWeighBefore(grams) }
                        )
                    }
                }
                GamePhase.DRINK -> DrinkPhase(
                    target = uiState.target,
                    onDone = { viewModel.confirmDrinkDone() }
                )
                GamePhase.WEIGH_AFTER -> {
                    val player = uiState.players.getOrNull(uiState.activePlayerIndex)
                    if (player != null) {
                        WeighPhase(
                            playerName = player.name,
                            playerNumber = uiState.activePlayerIndex + 1,
                            totalPlayers = uiState.players.size,
                            target = uiState.target,
                            instruction = "Weigh ${player.name}'s drink again to check the result.",
                            onSubmit = { grams -> viewModel.submitWeighAfter(grams) }
                        )
                    }
                }
                GamePhase.ROUND_RESULT -> RoundResultPhase(
                    viewModel = viewModel,
                    onContinue = { viewModel.proceedAfterRoundResult() }
                )
                GamePhase.GAME_OVER -> GameOverPhase(
                    viewModel = viewModel,
                    onPlayAgain = { viewModel.playAgainSamePlayers() },
                    onNewPlayers = onExitToHome
                )
            }
        }
    }
}

private fun topBarTitle(phase: GamePhase, roundNumber: Int, totalRounds: Int): String =
    if (phase == GamePhase.GAME_OVER) "Game Over" else "Round $roundNumber of $totalRounds"

@Composable
private fun DiceRollPhase(
    roundNumber: Int,
    totalRounds: Int,
    onRollFinished: () -> Unit
) {
    var isRolling by remember { mutableStateOf(false) }
    var displayValues by remember { mutableStateOf(List(5) { 1 }) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            repeat(10) {
                displayValues = List(5) { Random.nextInt(1, 7) }
                delay(70)
            }
            onRollFinished()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Round $roundNumber of $totalRounds", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Text("Roll the dice!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            displayValues.forEach { value ->
                DiceFace(value = value, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = { isRolling = true }, enabled = !isRolling) {
            Text(if (isRolling) "Rolling…" else "Roll 5 Dice")
        }
    }
}

@Composable
private fun DrinkPhase(target: Int, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🍹", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Everyone drinks $target grams!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "All players drink now, aiming for exactly $target grams from their glass.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("We're done drinking — weigh again")
        }
    }
}

@Composable
private fun WeighPhase(
    playerName: String,
    playerNumber: Int,
    totalPlayers: Int,
    target: Int,
    instruction: String,
    onSubmit: (Double) -> Unit
) {
    var weightText by remember(playerName, instruction) { mutableStateOf("") }
    val weight = weightText.toDoubleOrNull()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Player $playerNumber of $totalPlayers", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Text(playerName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(instruction, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text("Target this round: $target g", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Weight (grams)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { weight?.let(onSubmit) },
            enabled = weight != null && weight >= 0.0
        ) {
            Text("Confirm")
        }
    }
}
