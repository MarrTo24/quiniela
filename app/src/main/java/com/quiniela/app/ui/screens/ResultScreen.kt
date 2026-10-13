package com.quiniela.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiniela.app.data.models.AdvanceMethod
import com.quiniela.app.data.models.MatchResult
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    matchId: String,
    vm: QuinielaViewModel,
    onBack: () -> Unit
) {
    val isAdmin by vm.isAdminMode.collectAsState()

    // Guardia: solo admin puede cargar resultados
    if (!isAdmin) {
        Scaffold(topBar = { QuinielaTopBar("Sin acceso", onBack) }, containerColor = BackgroundDark) { p ->
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                EmptyState("Solo el administrador puede cargar resultados.")
            }
        }
        return
    }

    val match = vm.getMatch(matchId) ?: return
    val existing = vm.getResult(matchId)

    // Solicitar permiso de notificaciones (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* silencioso si niegan */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !vm.notificationPermissionGranted()) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
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
        topBar = { QuinielaTopBar("Cargar resultado real", onBack) },
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

            MatchHeader(match.homeFlag, match.homeTeam, match.awayFlag, match.awayTeam, match.round)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Solo el administrador debe cargar este resultado.",
                color = GoldAccent.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("RESULTADO EN 90 MINUTOS")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResultScoreInput(
                    label = match.homeTeam,
                    flag = match.homeFlag,
                    value = homeScore,
                    onValueChange = { homeScore = it.filter { c -> c.isDigit() }.take(2); saved = false },
                    modifier = Modifier.weight(1f)
                )
                Text("–", color = Color.White.copy(alpha = 0.5f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                ResultScoreInput(
                    label = match.awayTeam,
                    flag = match.awayFlag,
                    value = awayScore,
                    onValueChange = { awayScore = it.filter { c -> c.isDigit() }.take(2); saved = false },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isDraw) {
                Spacer(modifier = Modifier.height(24.dp))
                InfoCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Text(
                            "El partido terminó empatado. Registra quién avanzó y cómo.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionLabel("¿QUIÉN AVANZÓ?")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TeamChipResult(
                            team = match.homeTeam,
                            selected = advancingTeam == match.homeTeam,
                            onClick = { advancingTeam = match.homeTeam; saved = false },
                            modifier = Modifier.weight(1f)
                        )
                        TeamChipResult(
                            team = match.awayTeam,
                            selected = advancingTeam == match.awayTeam,
                            onClick = { advancingTeam = match.awayTeam; saved = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionLabel("¿CÓMO AVANZÓ?")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MethodChipResult(
                            label = "⏱ Tiempos extra",
                            selected = advanceMethod == AdvanceMethod.EXTRA_TIME,
                            onClick = { advanceMethod = AdvanceMethod.EXTRA_TIME; saved = false },
                            modifier = Modifier.weight(1f)
                        )
                        MethodChipResult(
                            label = "🥅 Penales",
                            selected = advanceMethod == AdvanceMethod.PENALTIES,
                            onClick = { advanceMethod = AdvanceMethod.PENALTIES; saved = false },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1010)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF9A9A))
                        Text(errorMessage, color = Color(0xFFEF9A9A), fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3A1F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Resultado guardado. Los puntos se calcularon automáticamente.",
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
                        h == null || a == null -> { errorMessage = "Ingresa el marcador de ambos equipos."; showError = true }
                        h == a && advancingTeam.isBlank() -> { errorMessage = "Selecciona qué equipo avanzó."; showError = true }
                        h == a && advanceMethod == null -> { errorMessage = "Selecciona cómo avanzó el equipo."; showError = true }
                        else -> {
                            vm.saveResult(
                                MatchResult(
                                    matchId = matchId,
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
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar resultado real", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ResultScoreInput(label: String, flag: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
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
                color = GoldAccent
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = GoldAccent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp)
        )
    }
}

@Composable
private fun TeamChipResult(team: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GreenFieldLight else SurfaceVariantDark,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = team, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun MethodChipResult(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GoldAccent.copy(alpha = 0.25f) else SurfaceVariantDark,
            contentColor = if (selected) GoldAccent else Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
