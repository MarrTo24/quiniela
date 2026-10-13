package com.quiniela.app.data.models

import java.time.LocalDateTime

enum class MatchStatus { PENDING, PREDICTION_SAVED, RESULT_LOADED, CLOSED }
enum class AdvanceMethod { EXTRA_TIME, PENALTIES }

data class User(
    val id: String = "",
    val name: String = "",
    val isAdmin: Boolean = false
)

data class Match(
    val id: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val homeFlag: String = "",
    val awayFlag: String = "",
    val scheduledAt: LocalDateTime = LocalDateTime.now(),
    val round: String = "",
    val status: MatchStatus = MatchStatus.PENDING
)

data class Prediction(
    val matchId: String = "",
    val userId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val advancingTeam: String? = null,
    val advanceMethod: AdvanceMethod? = null
) {
    val isDraw: Boolean get() = homeScore == awayScore
}

data class MatchResult(
    val matchId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val advancingTeam: String? = null,
    val advanceMethod: AdvanceMethod? = null
) {
    val isDraw: Boolean get() = homeScore == awayScore
}

data class Score(
    val matchId: String,
    val userId: String,
    val basePoints: Int,
    val bonusPoints: Int,
    val total: Int = basePoints + bonusPoints,
    val exactScore: Boolean,
    val correctOutcome: Boolean,
    val bonusEarned: Boolean
)

data class StandingsEntry(
    val user: User,
    val totalPoints: Int,
    val exactScores: Int,
    val bonusHits: Int,
    val position: Int
)
