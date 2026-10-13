package com.quiniela.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quiniela.app.data.models.*
import com.quiniela.app.data.preferences.PreferencesManager
import com.quiniela.app.data.repository.FirestoreRepository
import com.quiniela.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class QuinielaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FirestoreRepository()
    private val prefs = PreferencesManager(application)

    // Expone los StateFlows de Firestore directamente al UI
    val users: StateFlow<List<User>> = repo.usersFlow
    val matches: StateFlow<List<Match>> = repo.matchesFlow

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    init {
        viewModelScope.launch {
            // Sembrar datos iniciales — con timeout para no bloquear si no hay red
            runCatching {
                withTimeoutOrNull(15_000) { repo.initOrSeed() }
            }
            // Restaurar sesión recordada
            val savedId = prefs.rememberedUserId
            if (savedId != null) {
                runCatching {
                    val saved = repo.getUserById(savedId)
                    if (saved != null && !saved.isAdmin) _currentUser.value = saved
                }
            }
            _isReady.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        repo.cleanup()
    }

    // ─── Sesión ───────────────────────────────────────────────────────────────

    fun selectRegularUser(user: User, remember: Boolean = false) {
        _currentUser.value = user
        _isAdminMode.value = false
        prefs.rememberedUserId = if (remember) user.id else null
    }

    fun validateAdminPin(pin: String): Boolean {
        if (pin != prefs.adminPin) return false
        // Usa el admin de Firestore si ya cargó; de lo contrario, un User de respaldo
        _currentUser.value = users.value.firstOrNull { it.isAdmin }
            ?: User(id = "admin", name = "Administrador", isAdmin = true)
        _isAdminMode.value = true
        return true
    }

    fun changeAdminPin(newPin: String) { prefs.adminPin = newPin }

    fun switchUser() {
        _currentUser.value = null
        _isAdminMode.value = false
        prefs.clearRememberedUser()
    }

    // ─── Usuarios ────────────────────────────────────────────────────────────

    fun addUser(name: String) {
        viewModelScope.launch { repo.addUser(name.trim()) }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch { repo.deleteUser(userId) }
    }

    // ─── Partidos ────────────────────────────────────────────────────────────

    fun createMatch(match: Match) {
        viewModelScope.launch { repo.createMatch(match) }
    }

    fun updateMatch(match: Match) {
        viewModelScope.launch { repo.updateMatch(match) }
    }

    fun getMatch(matchId: String): Match? = repo.getMatch(matchId)

    // ─── Pronósticos ──────────────────────────────────────────────────────────

    fun getPrediction(matchId: String, userId: String? = null): Prediction? {
        val uid = userId ?: _currentUser.value?.id ?: return null
        return repo.getPrediction(matchId, uid)
    }

    fun savePrediction(prediction: Prediction) {
        viewModelScope.launch { repo.savePrediction(prediction) }
    }

    // ─── Resultados ──────────────────────────────────────────────────────────

    fun getResult(matchId: String): MatchResult? = repo.getResult(matchId)

    fun saveResult(result: MatchResult) {
        viewModelScope.launch {
            repo.saveResult(result)
            val match = repo.getMatch(result.matchId) ?: return@launch
            NotificationHelper.showResultLoaded(
                context = getApplication(),
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                homeScore = result.homeScore,
                awayScore = result.awayScore
            )
        }
    }

    fun deleteResult(matchId: String) {
        viewModelScope.launch { repo.deleteResult(matchId) }
    }

    // ─── Puntuación ──────────────────────────────────────────────────────────

    fun getScore(matchId: String, userId: String? = null): Score? {
        val uid = userId ?: _currentUser.value?.id ?: return null
        return repo.getScore(matchId, uid)
    }

    fun getStandings(): List<StandingsEntry> = repo.getStandings()

    fun getAllPredictionsForMatch(matchId: String): List<Pair<User, Prediction>> {
        return if (_isAdminMode.value) repo.getAllPredictionsForMatch(matchId)
        else emptyList()
    }

    // No-op: Firestore listeners mantienen datos actualizados automáticamente
    fun refresh() {}

    fun notificationPermissionGranted(): Boolean =
        NotificationHelper.hasPermission(getApplication())
}
