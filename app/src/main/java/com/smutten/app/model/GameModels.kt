package com.smutten.app.model

import kotlin.math.abs

data class Player(
    val id: Int,
    val name: String
)

enum class GamePhase {
    SETUP,
    DICE_ROLL,
    WEIGH_BEFORE,
    DRINK,
    WEIGH_AFTER,
    ROUND_RESULT,
    GAME_OVER
}

data class PlayerRoundData(
    val playerId: Int,
    val weightBeforeGrams: Double,
    val weightAfterGrams: Double
) {
    val consumedGrams: Double get() = weightBeforeGrams - weightAfterGrams

    fun deviationFrom(target: Int): Double = abs(consumedGrams - target)
}

data class RoundResult(
    val roundNumber: Int,
    val diceValues: List<Int>,
    val target: Int,
    val playerData: List<PlayerRoundData>,
    val loserIds: Set<Int>
)

data class GameUiState(
    val players: List<Player> = emptyList(),
    val phase: GamePhase = GamePhase.SETUP,
    val roundNumber: Int = 1,
    val totalRounds: Int = 3,
    val diceValues: List<Int> = emptyList(),
    val target: Int = 0,
    val weighBefore: Map<Int, Double> = emptyMap(),
    val weighAfter: Map<Int, Double> = emptyMap(),
    val activePlayerIndex: Int = 0,
    val roundHistory: List<RoundResult> = emptyList(),
    val roundLossCounts: Map<Int, Int> = emptyMap(),
    val overallLoserIds: Set<Int> = emptySet()
)
