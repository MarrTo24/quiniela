package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiniela.app.data.models.AdvanceMethod
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(
    matchId: String,
    vm: QuinielaViewModel,
    onBack: () -> Unit
) {
    val match = vm.getMatch(matchId) ?: return
    val result = vm.getResult(matchId)
    val prediction = vm.getPrediction(matchId)
    val score = vm.getScore(matchId)
    val isAdmin by vm.isAdminMode.collectAsState()
    // Solo el admin puede ver los pronósticos de todos los jugadores
    val allPredictions = if (isAdmin) vm.getAllPredictionsForMatch(matchId) else emptyList()

    Scaffold(
        topBar = { QuinielaTopBar("Puntuación del partido", onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabecera del partido
            MatchHeader(match.homeFlag, match.homeTeam, match.awayFlag, match.awayTeam, match.round)

            Spacer(modifier = Modifier.height(16.dp))

            // Resultado real
            if (result != null) {
                InfoCard {
                    SectionLabel("RESULTADO REAL")
                    ScoreBox(result.homeScore, result.awayScore, modifier = Modifier.align(Alignment.CenterHorizontally))
                    if (result.isDraw && result.advancingTeam != null && result.advanceMethod != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${result.advancingTeam} avanzó en ${result.advanceMethod.label()}",
                            color = GreenAccent,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mi pronóstico y puntos
            if (prediction != null && score != null) {
                InfoCard {
                    SectionLabel("TU PRONÓSTICO")
                    ScoreBox(prediction.homeScore, prediction.awayScore, modifier = Modifier.align(Alignment.CenterHorizontally))
                    if (prediction.isDraw && prediction.advancingTeam != null && prediction.advanceMethod != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${prediction.advancingTeam} · ${prediction.advanceMethod.label()}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    SectionLabel("DESGLOSE DE PUNTOS")

                    ScoreRow(
                        label = when {
                            score.exactScore -> "Marcador exacto en 90'"
                            score.correctOutcome -> "Resultado correcto en 90'"
                            else -> "Resultado incorrecto"
                        },
                        points = score.basePoints,
                        positive = score.basePoints > 0
                    )

                    if (result?.isDraw == true) {
                        ScoreRow(
                            label = if (score.bonusEarned) "Bonus: método de desempate ✓" else "Bonus: método de desempate ✗",
                            points = score.bonusPoints,
                            positive = score.bonusEarned
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "${score.total} ${if (score.total == 1) "punto" else "puntos"}",
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                }
            } else if (prediction == null) {
                EmptyState(message = "No registraste un pronóstico para este partido.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pronósticos de todos los participantes
            if (allPredictions.isNotEmpty()) {
                SectionLabel("PRONÓSTICOS DE TODOS")
                Spacer(modifier = Modifier.height(8.dp))
                allPredictions.forEach { (user, pred) ->
                    val userScore = vm.getScore(matchId, user.id)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = "${pred.homeScore} – ${pred.awayScore}" +
                                            if (pred.isDraw && pred.advancingTeam != null) " · ${pred.advancingTeam}" else "",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                                if (pred.isDraw && pred.advanceMethod != null) {
                                    Text(
                                        text = pred.advanceMethod.label(),
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            if (userScore != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${userScore.total}",
                                        color = if (userScore.total > 0) GreenAccent else Color.White.copy(alpha = 0.3f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text("pts", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScoreRow(label: String, points: Int, positive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (positive) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (positive) GreenAccent else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Text(text = label, color = Color.White.copy(alpha = if (positive) 0.9f else 0.5f), fontSize = 14.sp)
        }
        Text(
            text = "+$points",
            color = if (positive) GoldAccent else Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

private fun AdvanceMethod.label(): String = when (this) {
    AdvanceMethod.EXTRA_TIME -> "Tiempos extra"
    AdvanceMethod.PENALTIES -> "Penales"
}
