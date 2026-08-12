package com.smutten.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.smutten.app.model.GamePhase
import com.smutten.app.model.GameUiState
import com.smutten.app.model.Player
import com.smutten.app.model.PlayerRoundData
import com.smutten.app.model.RoundResult
import kotlin.math.abs
import kotlin.random.Random

/**
 * A round is lost by whoever's actual grams-drunk deviates most from the dice target.
 * The overall loser is whoever loses the most of the 3 rounds; ties break on total
 * deviation across all rounds.
 */
class GameViewModel : ViewModel() {

    var uiState by mutableStateOf(GameUiState())
        private set

    private var nextPlayerId = 0

    fun addPlayer(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val player = Player(id = nextPlayerId++, name = trimmed)
        uiState = uiState.copy(players = uiState.players + player)
    }

    fun removePlayer(playerId: Int) {
        uiState = uiState.copy(players = uiState.players.filterNot { it.id == playerId })
    }

    fun startGame() {
        if (uiState.players.size < 2) return
        uiState = uiState.copy(
            phase = GamePhase.DICE_ROLL,
            roundNumber = 1,
            roundHistory = emptyList(),
            roundLossCounts = emptyMap(),
            overallLoserIds = emptySet()
        )
    }

    fun rollDice() {
        val values = List(5) { Random.nextInt(1, 7) }
        uiState = uiState.copy(
            diceValues = values,
            target = values.sum(),
            phase = GamePhase.WEIGH_BEFORE,
            activePlayerIndex = 0,
            weighBefore = emptyMap(),
            weighAfter = emptyMap()
        )
    }

    fun submitWeighBefore(weightGrams: Double) {
        val player = uiState.players.getOrNull(uiState.activePlayerIndex) ?: return
        val updated = uiState.weighBefore + (player.id to weightGrams)
        val isLast = uiState.activePlayerIndex >= uiState.players.lastIndex
        uiState = uiState.copy(
            weighBefore = updated,
            activePlayerIndex = if (isLast) 0 else uiState.activePlayerIndex + 1,
            phase = if (isLast) GamePhase.DRINK else uiState.phase
        )
    }

    fun confirmDrinkDone() {
        uiState = uiState.copy(phase = GamePhase.WEIGH_AFTER, activePlayerIndex = 0)
    }

    fun submitWeighAfter(weightGrams: Double) {
        val player = uiState.players.getOrNull(uiState.activePlayerIndex) ?: return
        val updated = uiState.weighAfter + (player.id to weightGrams)
        val isLast = uiState.activePlayerIndex >= uiState.players.lastIndex
        if (isLast) {
            finishRound(updated)
        } else {
            uiState = uiState.copy(
                weighAfter = updated,
                activePlayerIndex = uiState.activePlayerIndex + 1
            )
        }
    }

    private fun finishRound(weighAfter: Map<Int, Double>) {
        val playerData = uiState.players.map { p ->
            PlayerRoundData(
                playerId = p.id,
                weightBeforeGrams = uiState.weighBefore[p.id] ?: 0.0,
                weightAfterGrams = weighAfter[p.id] ?: 0.0
            )
        }
        val maxDeviation = playerData.maxOf { it.deviationFrom(uiState.target) }
        val loserIds = playerData
            .filter { abs(it.deviationFrom(uiState.target) - maxDeviation) < 0.5 }
            .map { it.playerId }
            .toSet()

        val result = RoundResult(
            roundNumber = uiState.roundNumber,
            diceValues = uiState.diceValues,
            target = uiState.target,
            playerData = playerData,
            loserIds = loserIds
        )

        val updatedLossCounts = uiState.roundLossCounts.toMutableMap()
        loserIds.forEach { id -> updatedLossCounts[id] = (updatedLossCounts[id] ?: 0) + 1 }

        uiState = uiState.copy(
            weighAfter = weighAfter,
            roundHistory = uiState.roundHistory + result,
            roundLossCounts = updatedLossCounts,
            phase = GamePhase.ROUND_RESULT
        )
    }

    fun proceedAfterRoundResult() {
        if (uiState.roundNumber >= uiState.totalRounds) {
            uiState = uiState.copy(
                phase = GamePhase.GAME_OVER,
                overallLoserIds = determineOverallLosers()
            )
        } else {
            uiState = uiState.copy(
                roundNumber = uiState.roundNumber + 1,
                phase = GamePhase.DICE_ROLL,
                diceValues = emptyList(),
                target = 0,
                weighBefore = emptyMap(),
                weighAfter = emptyMap(),
                activePlayerIndex = 0
            )
        }
    }

    private fun determineOverallLosers(): Set<Int> {
        val maxLosses = uiState.roundLossCounts.values.maxOrNull() ?: return emptySet()
        val candidates = uiState.roundLossCounts.filterValues { it == maxLosses }.keys
        if (candidates.size <= 1) return candidates

        val totalDeviation = candidates.associateWith { playerId ->
            uiState.roundHistory.sumOf { round ->
                round.playerData.find { it.playerId == playerId }?.deviationFrom(round.target) ?: 0.0
            }
        }
        val maxDeviation = totalDeviation.values.max()
        return totalDeviation.filterValues { abs(it - maxDeviation) < 0.5 }.keys
    }

    fun playAgainSamePlayers() {
        uiState = uiState.copy(
            phase = GamePhase.DICE_ROLL,
            roundNumber = 1,
            diceValues = emptyList(),
            target = 0,
            weighBefore = emptyMap(),
            weighAfter = emptyMap(),
            activePlayerIndex = 0,
            roundHistory = emptyList(),
            roundLossCounts = emptyMap(),
            overallLoserIds = emptySet()
        )
    }

    fun resetToHome() {
        uiState = GameUiState()
        nextPlayerId = 0
    }
}
