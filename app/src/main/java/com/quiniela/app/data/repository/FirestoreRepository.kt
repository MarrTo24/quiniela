package com.quiniela.app.data.repository

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiniela.app.data.models.*
import com.quiniela.app.domain.ScoringEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val TAG = "Firestore"

class FirestoreRepository {

    private val db = Firebase.firestore

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val usersFlow: StateFlow<List<User>> = _users.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matchesFlow: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    private val _results = MutableStateFlow<List<MatchResult>>(emptyList())

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        listeners += db.collection("users")
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.e(TAG, "users error: ${e.message}"); return@addSnapshotListener }
                _users.value = snap?.documents?.mapNotNull { it.toUser() } ?: emptyList()
                Log.d(TAG, "users updated: ${_users.value.size} docs")
            }

        listeners += db.collection("matches")
            .orderBy("scheduledAt")
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.e(TAG, "matches error: ${e.message}"); return@addSnapshotListener }
                _matches.value = snap?.documents?.mapNotNull { it.toMatch() } ?: emptyList()
                Log.d(TAG, "matches updated: ${_matches.value.size} docs")
            }

        listeners += db.collection("predictions")
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.e(TAG, "predictions error: ${e.message}"); return@addSnapshotListener }
                _predictions.value = snap?.documents?.mapNotNull { it.toPrediction() } ?: emptyList()
            }

        listeners += db.collection("results")
            .addSnapshotListener { snap, e ->
                if (e != null) { Log.e(TAG, "results error: ${e.message}"); return@addSnapshotListener }
                _results.value = snap?.documents?.mapNotNull { it.toResult() } ?: emptyList()
            }
    }

    fun cleanup() = listeners.forEach { it.remove() }

    // ─── Inicialización ──────────────────────────────────────────────────────

    suspend fun initOrSeed() {
        val adminExists = db.collection("users").document("admin").get().await().exists()
        if (!adminExists) {
            db.collection("users").document("admin").set(
                mapOf("name" to "Administrador", "isAdmin" to true)
            ).await()
            seedMatches()
        }
    }

    private suspend fun seedMatches() {
        fun epoch(month: Int, day: Int, hour: Int): Long =
            LocalDateTime.of(2026, month, day, hour, 0).toEpochSecond(ZoneOffset.UTC)

        fun matchMap(round: String, epoch: Long) = mapOf(
            "homeTeam" to "Por definir", "homeFlag" to "🏳",
            "awayTeam" to "Por definir", "awayFlag" to "🏳",
            "scheduledAt" to epoch, "round" to round, "status" to "PENDING"
        )

        val r32 = "Ronda de 32"; val r16 = "Octavos de Final"
        val qf = "Cuartos de Final"; val sf = "Semifinal"

        val matches = mapOf(
            // Ronda de 32
            "r32_01" to matchMap(r32, epoch(7,1,13)), "r32_02" to matchMap(r32, epoch(7,1,16)),
            "r32_03" to matchMap(r32, epoch(7,1,19)), "r32_04" to matchMap(r32, epoch(7,1,22)),
            "r32_05" to matchMap(r32, epoch(7,2,13)), "r32_06" to matchMap(r32, epoch(7,2,16)),
            "r32_07" to matchMap(r32, epoch(7,2,19)), "r32_08" to matchMap(r32, epoch(7,2,22)),
            "r32_09" to matchMap(r32, epoch(7,3,13)), "r32_10" to matchMap(r32, epoch(7,3,16)),
            "r32_11" to matchMap(r32, epoch(7,3,19)), "r32_12" to matchMap(r32, epoch(7,3,22)),
            "r32_13" to matchMap(r32, epoch(7,4,13)), "r32_14" to matchMap(r32, epoch(7,4,16)),
            "r32_15" to matchMap(r32, epoch(7,4,19)), "r32_16" to matchMap(r32, epoch(7,4,22)),
            // Octavos de Final
            "r16_01" to matchMap(r16, epoch(7,6,14)), "r16_02" to matchMap(r16, epoch(7,6,19)),
            "r16_03" to matchMap(r16, epoch(7,7,14)), "r16_04" to matchMap(r16, epoch(7,7,19)),
            "r16_05" to matchMap(r16, epoch(7,8,14)), "r16_06" to matchMap(r16, epoch(7,8,19)),
            "r16_07" to matchMap(r16, epoch(7,9,14)), "r16_08" to matchMap(r16, epoch(7,9,19)),
            // Cuartos de Final
            "qf_01" to matchMap(qf, epoch(7,12,15)), "qf_02" to matchMap(qf, epoch(7,12,20)),
            "qf_03" to matchMap(qf, epoch(7,13,15)), "qf_04" to matchMap(qf, epoch(7,13,20)),
            // Semifinales
            "sf_01" to matchMap(sf, epoch(7,15,19)), "sf_02" to matchMap(sf, epoch(7,16,19)),
            // Tercer lugar y Final
            "tp_01" to matchMap("Tercer Lugar", epoch(7,18,19)),
            "fin_01" to matchMap("Final", epoch(7,19,18))
        )

        // Firestore batch limit = 500 ops; 32 matches caben en un solo batch
        val batch = db.batch()
        matches.forEach { (id, data) ->
            batch.set(db.collection("matches").document(id), data)
        }
        batch.commit().await()
    }

    // ─── Usuarios ────────────────────────────────────────────────────────────

    suspend fun addUser(name: String): String {
        val id = "u_${System.currentTimeMillis()}"
        db.collection("users").document(id).set(
            mapOf("name" to name, "isAdmin" to false)
        ).await()
        Log.d(TAG, "addUser OK: $id ($name)")
        return id
    }

    suspend fun getUserById(id: String): User? =
        db.collection("users").document(id).get().await().toUser()

    suspend fun deleteUser(userId: String) {
        val batch = db.batch()
        // Eliminar pronósticos del usuario
        db.collection("predictions").whereEqualTo("userId", userId).get().await()
            .documents.forEach { batch.delete(it.reference) }
        batch.delete(db.collection("users").document(userId))
        batch.commit().await()
    }

    // ─── Partidos ────────────────────────────────────────────────────────────

    suspend fun createMatch(match: Match) {
        db.collection("matches").document(match.id).set(match.toMap()).await()
        Log.d(TAG, "createMatch OK: ${match.id} (${match.homeTeam} vs ${match.awayTeam})")
    }

    suspend fun updateMatch(match: Match) {
        db.collection("matches").document(match.id).set(match.toMap()).await()
    }

    fun getMatch(matchId: String): Match? =
        _matches.value.firstOrNull { it.id == matchId }

    // ─── Pronósticos ─────────────────────────────────────────────────────────

    fun getPrediction(matchId: String, userId: String): Prediction? =
        _predictions.value.firstOrNull { it.matchId == matchId && it.userId == userId }

    suspend fun savePrediction(prediction: Prediction) {
        val docId = "${prediction.matchId}_${prediction.userId}"
        db.collection("predictions").document(docId).set(prediction.toMap()).await()
        refreshMatchStatus(prediction.matchId)
    }

    fun getAllPredictionsForMatch(matchId: String): List<Pair<User, Prediction>> =
        _users.value.mapNotNull { user ->
            getPrediction(matchId, user.id)?.let { user to it }
        }

    // ─── Resultados ──────────────────────────────────────────────────────────

    fun getResult(matchId: String): MatchResult? =
        _results.value.firstOrNull { it.matchId == matchId }

    suspend fun saveResult(result: MatchResult) {
        db.collection("results").document(result.matchId).set(result.toMap()).await()
        refreshMatchStatus(result.matchId)
    }

    suspend fun deleteResult(matchId: String) {
        db.collection("results").document(matchId).delete().await()
        refreshMatchStatus(matchId)
    }

    // ─── Puntuación ──────────────────────────────────────────────────────────

    fun getScore(matchId: String, userId: String): Score? {
        val prediction = getPrediction(matchId, userId) ?: return null
        val result = getResult(matchId) ?: return null
        return ScoringEngine.calculate(prediction, result)
    }

    fun getStandings(): List<StandingsEntry> {
        val matchIds = _matches.value.map { it.id }
        return _users.value
            .filter { !it.isAdmin }
            .map { user ->
                var total = 0; var exact = 0; var bonus = 0
                for (mid in matchIds) {
                    val s = getScore(mid, user.id) ?: continue
                    total += s.total
                    if (s.exactScore) exact++
                    if (s.bonusEarned) bonus++
                }
                StandingsEntry(user, total, exact, bonus, 0)
            }
            .sortedByDescending { it.totalPoints }
            .mapIndexed { i, e -> e.copy(position = i + 1) }
    }

    // ─── Helpers internos ────────────────────────────────────────────────────

    private suspend fun refreshMatchStatus(matchId: String) {
        val hasPredictions = _predictions.value.any { it.matchId == matchId }
        val hasResult = _results.value.any { it.matchId == matchId }
        val newStatus = when {
            hasResult -> MatchStatus.RESULT_LOADED.name
            hasPredictions -> MatchStatus.PREDICTION_SAVED.name
            else -> MatchStatus.PENDING.name
        }
        db.collection("matches").document(matchId).update("status", newStatus).await()
    }

    // ─── Serialización Firestore ──────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        val d = data ?: return null
        return User(
            id = id,
            name = d["name"] as? String ?: return null,
            isAdmin = d["isAdmin"] as? Boolean ?: false
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMatch(): Match? {
        val d = data ?: return null
        return try {
            val epochSecs = when (val v = d["scheduledAt"]) {
                is Long -> v
                is Number -> v.toLong()
                else -> 0L
            }
            Match(
                id = id,
                homeTeam = d["homeTeam"] as? String ?: "Por definir",
                homeFlag = d["homeFlag"] as? String ?: "🏳",
                awayTeam = d["awayTeam"] as? String ?: "Por definir",
                awayFlag = d["awayFlag"] as? String ?: "🏳",
                scheduledAt = LocalDateTime.ofEpochSecond(epochSecs, 0, ZoneOffset.UTC),
                round = d["round"] as? String ?: "",
                status = MatchStatus.valueOf(d["status"] as? String ?: "PENDING")
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPrediction(): Prediction? {
        val d = data ?: return null
        return try {
            Prediction(
                matchId = d["matchId"] as? String ?: return null,
                userId = d["userId"] as? String ?: return null,
                homeScore = (d["homeScore"] as? Long)?.toInt() ?: 0,
                awayScore = (d["awayScore"] as? Long)?.toInt() ?: 0,
                advancingTeam = d["advancingTeam"] as? String,
                advanceMethod = (d["advanceMethod"] as? String)
                    ?.let { runCatching { AdvanceMethod.valueOf(it) }.getOrNull() }
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toResult(): MatchResult? {
        val d = data ?: return null
        return try {
            MatchResult(
                matchId = id,
                homeScore = (d["homeScore"] as? Long)?.toInt() ?: 0,
                awayScore = (d["awayScore"] as? Long)?.toInt() ?: 0,
                advancingTeam = d["advancingTeam"] as? String,
                advanceMethod = (d["advanceMethod"] as? String)
                    ?.let { runCatching { AdvanceMethod.valueOf(it) }.getOrNull() }
            )
        } catch (e: Exception) { null }
    }

    private fun Match.toMap(): Map<String, Any?> = mapOf(
        "homeTeam" to homeTeam, "homeFlag" to homeFlag,
        "awayTeam" to awayTeam, "awayFlag" to awayFlag,
        "scheduledAt" to scheduledAt.toEpochSecond(ZoneOffset.UTC),
        "round" to round, "status" to status.name
    )

    private fun Prediction.toMap(): Map<String, Any?> = mapOf(
        "matchId" to matchId, "userId" to userId,
        "homeScore" to homeScore, "awayScore" to awayScore,
        "advancingTeam" to advancingTeam, "advanceMethod" to advanceMethod?.name
    )

    private fun MatchResult.toMap(): Map<String, Any?> = mapOf(
        "matchId" to matchId,
        "homeScore" to homeScore, "awayScore" to awayScore,
        "advancingTeam" to advancingTeam, "advanceMethod" to advanceMethod?.name
    )
}
