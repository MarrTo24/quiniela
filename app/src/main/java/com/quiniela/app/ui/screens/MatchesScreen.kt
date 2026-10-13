package com.quiniela.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiniela.app.data.models.Match
import com.quiniela.app.data.models.MatchStatus
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    vm: QuinielaViewModel,
    onPredictClick: (String) -> Unit,
    onResultClick: (String) -> Unit,
    onScoreClick: (String) -> Unit,
    onEditMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit,
    onBack: () -> Unit
) {
    val matches by vm.matches.collectAsState()
    val isAdmin by vm.isAdminMode.collectAsState()
    val currentUser by vm.currentUser.collectAsState()

    // Refresca cuando cambia el usuario activo (evita datos obsoletos al cambiar sesión)
    LaunchedEffect(currentUser?.id) { vm.refresh() }

    Scaffold(
        topBar = { QuinielaTopBar("Partidos", onBack) },
        containerColor = BackgroundDark,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onCreateMatchClick,
                    containerColor = GreenFieldLight,
                    contentColor = GoldAccent
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear partido")
                }
            }
        }
    ) { padding ->
        if (matches.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⚽", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isAdmin) "Aún no hay partidos.\nToca + para crear el primer partido."
                           else "Aún no hay partidos disponibles.\nEl administrador los cargará pronto.",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = if (isAdmin) 88.dp else 24.dp  // espacio para el FAB
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        vm = vm,
                        isAdmin = isAdmin,
                        currentUserId = currentUser?.id,
                        onPredictClick = { onPredictClick(match.id) },
                        onResultClick = { onResultClick(match.id) },
                        onDeleteResult = { vm.deleteResult(match.id) },
                        onScoreClick = { onScoreClick(match.id) },
                        onEditMatchClick = { onEditMatchClick(match.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: Match,
    vm: QuinielaViewModel,
    isAdmin: Boolean,
    currentUserId: String?,
    onPredictClick: () -> Unit,
    onResultClick: () -> Unit,
    onDeleteResult: () -> Unit,
    onScoreClick: () -> Unit,
    onEditMatchClick: () -> Unit
) {
    val prediction = currentUserId?.let { vm.getPrediction(match.id, it) }
    val result = vm.getResult(match.id)
    val score = currentUserId?.let { vm.getScore(match.id, it) }
    val formatter = DateTimeFormatter.ofPattern("dd MMM · HH:mm")

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera: ronda + estado + editar (admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(match.round, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(match.status)
                    // Admin siempre puede editar datos del partido
                    if (isAdmin) {
                        IconButton(onClick = onEditMatchClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Editar partido",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Equipos + marcador central
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = match.homeFlag.ifEmpty { "🏳" }, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(match.homeTeam, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        result != null -> {
                            ScoreBox(result.homeScore, result.awayScore)
                            if (result.isDraw && result.advancingTeam != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Avanza ${result.advancingTeam}", color = GreenAccent, fontSize = 11.sp)
                            }
                        }
                        prediction != null -> {
                            ScoreBox(prediction.homeScore, prediction.awayScore)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tu pronóstico", color = StatusSaved, fontSize = 11.sp)
                            if (prediction.isDraw && prediction.advancingTeam != null) {
                                Text("Avanza ${prediction.advancingTeam}", color = StatusSaved.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }
                        else -> {
                            Text(
                                "vs",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = match.awayFlag.ifEmpty { "🏳" }, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(match.awayTeam, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = match.scheduledAt.format(formatter),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Puntos cuando hay resultado
            if (score != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PointsBadge(score.basePoints, "marcador")
                    PointsBadge(score.bonusPoints, "bonus")
                    PointsBadge(score.total, "total")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de acción — admin sin restricción; jugadores con cierre 15 min antes
            if (isAdmin) {
                var showDeleteConfirm by remember { mutableStateOf(false) }

                Button(
                    onClick = onResultClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (result != null) "Actualizar resultado" else "Cargar resultado",
                        fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.SemiBold
                    )
                }
                if (result != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onScoreClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver puntuación", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Borrar resultado", fontSize = 12.sp)
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            containerColor = SurfaceDark,
                            title = { Text("¿Borrar resultado?", color = Color.White, fontWeight = FontWeight.Bold) },
                            text = {
                                Text(
                                    "Se eliminará el resultado de ${match.homeTeam} vs ${match.awayTeam}. " +
                                    "Los jugadores podrán volver a pronosticar.",
                                    color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { onDeleteResult(); showDeleteConfirm = false }) {
                                    Text("Borrar", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        )
                    }
                }
            } else {
                val now = LocalDateTime.now()
                val cutoff = match.scheduledAt.minusMinutes(15)
                when {
                    result != null -> Button(
                        onClick = onScoreClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusResult),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver puntuación detallada", fontSize = 13.sp)
                    }
                    now.isBefore(cutoff) -> Button(
                        onClick = onPredictClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenFieldLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (prediction != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null, modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (prediction != null) "Editar pronóstico" else "Pronosticar", fontSize = 12.sp)
                    }
                    now.isBefore(match.scheduledAt) -> Text(
                        "🔒 Pronósticos cerrados · el partido está por comenzar",
                        color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    else -> Text(
                        "⏱ Partido en curso · esperando resultado",
                        color = GoldAccent.copy(alpha = 0.7f), fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
