package com.quiniela.app.domain

import com.quiniela.app.data.models.AdvanceMethod
import com.quiniela.app.data.models.MatchResult
import com.quiniela.app.data.models.Prediction
import com.quiniela.app.data.models.Score

/**
 * Lógica de puntuación aislada de la UI.
 * Entrada: pronóstico + resultado real. Salida: Score con desglose.
 *
 * Reglas:
 *  - 3 pts → marcador exacto en 90'
 *  - 1 pt  → acierta ganador/empate en 90' pero no el marcador exacto
 *  - 0 pts → falla el resultado en 90'
 *  - +1 pt → si hubo empate en 90' y el usuario acertó el método de desempate
 */
object ScoringEngine {

    fun calculate(prediction: Prediction, result: MatchResult): Score {
        val exactScore = prediction.homeScore == result.homeScore &&
                prediction.awayScore == result.awayScore

        val correctOutcome = when {
            exactScore -> true
            // Ambos pronosticaron empate (aunque distinto marcador)
            prediction.isDraw && result.isDraw -> true
            // Ambos indican mismo ganador sin empate
            !prediction.isDraw && !result.isDraw &&
                    winnerOf(prediction.homeScore, prediction.awayScore) ==
                    winnerOf(result.homeScore, result.awayScore) -> true
            else -> false
        }

        val basePoints = when {
            exactScore -> 3
            correctOutcome -> 1
            else -> 0
        }

        // Bonus solo si el partido se definió por método tras empate en 90'
        val bonusEarned = result.isDraw &&
                prediction.isDraw &&
                prediction.advanceMethod != null &&
                result.advanceMethod != null &&
                prediction.advanceMethod == result.advanceMethod &&
                prediction.advancingTeam == result.advancingTeam

        val bonusPoints = if (bonusEarned) 1 else 0

        return Score(
            matchId = prediction.matchId,
            userId = prediction.userId,
            basePoints = basePoints,
            bonusPoints = bonusPoints,
            exactScore = exactScore,
            correctOutcome = correctOutcome,
            bonusEarned = bonusEarned
        )
    }

    // "home" o "away"
    private fun winnerOf(home: Int, away: Int): String =
        if (home > away) "home" else "away"
}
