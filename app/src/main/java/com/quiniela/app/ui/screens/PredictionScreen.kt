package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiniela.app.data.models.AdvanceMethod
import com.quiniela.app.data.models.Prediction
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    matchId: String,
    vm: QuinielaViewModel,
    onBack: () -> Unit
) {
    val match = vm.getMatch(matchId) ?: return
    val currentUser by vm.currentUser.collectAsState()
    val existing = vm.getPrediction(matchId)

    // Bloquear si faltan menos de 15 min para el partido o ya comenzó
    val now = LocalDateTime.now()
    val deadline = match.scheduledAt.minusMinutes(15)
    if (!now.isBefore(deadline)) {
        Scaffold(
            topBar = { QuinielaTopBar("Pronóstico", onBack) },
            containerColor = BackgroundDark
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🔒", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pronósticos cerrados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (now.isBefore(match.scheduledAt))
                        "El partido está por comenzar.\nLos pronósticos cierran 15 min antes del inicio."
                    else
                        "El partido ya comenzó.\nEspera a que el administrador cargue el resultado.",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        return
    }

    var homeScore by remember { mutableStateOf(existing?.homeScore?.toString() ?: "") }
    var awayScore by remember { mutableStateOf(existing?.awayScore?.toString() ?: "") }
    var advancingTeam by remember { mutableStateOf(existing?.advancingTeam ?: "") }
    var advanceMethod by remember { mutableStateOf(existing?.advanceMethod) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val homeInt = homeScore.toIntOrNull()
    val awayInt = awayScore.toIntOrNull()
    val isDraw = homeInt != null && awayInt != null && homeInt == awayInt

    Scaffold(
        topBar = { QuinielaTopBar("Pronóstico", onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Cabecera del partido
            MatchHeader(match.homeFlag, match.homeTeam, match.awayFlag, match.awayTeam, match.round)

            Spacer(modifier = Modifier.height(24.dp))

            // Instrucción
            SectionLabel("MARCADOR AL MINUTO 90")
            Text(
                text = "Ingresa el marcador que esperas al final del tiempo regular.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Entrada de marcador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScoreInput(
                    label = match.homeTeam,
                    flag = match.homeFlag,
                    value = homeScore,
                    onValueChange = { homeScore = it.filter { c -> c.isDigit() }.take(2); saved = false },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "–",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                ScoreInput(
                    label = match.awayTeam,
                    flag = match.awayFlag,
                    value = awayScore,
                    onValueChange = { awayScore = it.filter { c -> c.isDigit() }.take(2); saved = false },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sección de empate (aparece solo si el marcador es empate)
            if (isDraw) {
                Spacer(modifier = Modifier.height(24.dp))
                DrawSection(
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    selectedTeam = advancingTeam,
                    selectedMethod = advanceMethod,
                    onTeamSelected = { advancingTeam = it; saved = false },
                    onMethodSelected = { advanceMethod = it; saved = false }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Error
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1010)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF9A9A))
                        Text(text = errorMessage, color = Color(0xFFEF9A9A), fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Confirmación
            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3A1F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Pronóstico guardado correctamente",
                        color = GreenAccent,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    showError = false
                    val h = homeScore.toIntOrNull()
                    val a = awayScore.toIntOrNull()

                    when {
                        h == null || a == null -> {
                            errorMessage = "Ingresa el marcador de ambos equipos."
                            showError = true
                        }
                        h == a && advancingTeam.isBlank() -> {
                            errorMessage = "Pronosticaste empate. Debes seleccionar qué equipo avanza."
                            showError = true
                        }
                        h == a && advanceMethod == null -> {
                            errorMessage = "Selecciona cómo avanza el equipo: tiempos extra o penales."
                            showError = true
                        }
                        else -> {
                            vm.savePrediction(
                                Prediction(
                                    matchId = matchId,
                                    userId = currentUser?.id ?: return@Button,
                                    homeScore = h,
                                    awayScore = a,
                                    advancingTeam = if (h == a) advancingTeam else null,
                                    advanceMethod = if (h == a) advanceMethod else null
                                )
                            )
                            saved = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenFieldLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar pronóstico", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ScoreInput(
    label: String,
    flag: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = flag, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = GreenAccent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )
    }
}

@Composable
private fun DrawSection(
    homeTeam: String,
    awayTeam: String,
    selectedTeam: String,
    selectedMethod: AdvanceMethod?,
    onTeamSelected: (String) -> Unit,
    onMethodSelected: (AdvanceMethod) -> Unit
) {
    InfoCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(18.dp).padding(top = 2.dp)
            )
            Text(
                text = "Como pronosticaste empate en 90 minutos, selecciona quién avanza y cómo avanza.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("¿QUIÉN AVANZA?")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TeamChip(
                team = homeTeam,
                selected = selectedTeam == homeTeam,
                onClick = { onTeamSelected(homeTeam) },
                modifier = Modifier.weight(1f)
            )
            TeamChip(
                team = awayTeam,
                selected = selectedTeam == awayTeam,
                onClick = { onTeamSelected(awayTeam) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("¿CÓMO AVANZA?")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MethodChip(
                label = "⏱ Tiempos extra",
                selected = selectedMethod == AdvanceMethod.EXTRA_TIME,
                onClick = { onMethodSelected(AdvanceMethod.EXTRA_TIME) },
                modifier = Modifier.weight(1f)
            )
            MethodChip(
                label = "🥅 Penales",
                selected = selectedMethod == AdvanceMethod.PENALTIES,
                onClick = { onMethodSelected(AdvanceMethod.PENALTIES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeamChip(team: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) GreenFieldLight else SurfaceVariantDark
    val border = if (selected) GreenAccent else Color.White.copy(alpha = 0.15f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = team,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MethodChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) GoldAccent.copy(alpha = 0.15f) else SurfaceVariantDark
    val border = if (selected) GoldAccent else Color.White.copy(alpha = 0.15f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) GoldAccent else Color.White.copy(alpha = 0.6f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MatchHeader(homeFlag: String, homeTeam: String, awayFlag: String, awayTeam: String, round: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(text = round, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = homeFlag, fontSize = 36.sp)
                Text(text = homeTeam, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Text(text = "VS", color = Color.White.copy(alpha = 0.3f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = awayFlag, fontSize = 36.sp)
                Text(text = awayTeam, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}
