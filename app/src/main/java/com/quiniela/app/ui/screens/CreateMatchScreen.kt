package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import com.quiniela.app.data.models.Match
import com.quiniela.app.data.models.MatchStatus
import com.quiniela.app.data.models.WCTeam
import com.quiniela.app.data.models.WorldCupTeams
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

val ROUNDS = listOf("Ronda de 32", "Octavos de Final", "Cuartos de Final", "Semifinal", "Tercer Lugar", "Final")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMatchScreen(
    matchId: String?,            // null = crear nuevo, non-null = editar existente
    vm: QuinielaViewModel,
    onBack: () -> Unit
) {
    val isEditing = matchId != null
    val existing = if (isEditing) vm.getMatch(matchId!!) else null

    var homeTeam by remember { mutableStateOf(existing?.homeTeam ?: "") }
    var homeFlag by remember { mutableStateOf(existing?.homeFlag ?: "") }
    var awayTeam by remember { mutableStateOf(existing?.awayTeam ?: "") }
    var awayFlag by remember { mutableStateOf(existing?.awayFlag ?: "") }
    var round by remember { mutableStateOf(existing?.round ?: ROUNDS.first()) }
    var hour by remember { mutableStateOf(existing?.scheduledAt?.hour?.toString()?.padStart(2, '0') ?: "18") }
    var minute by remember { mutableStateOf(existing?.scheduledAt?.minute?.toString()?.padStart(2, '0') ?: "00") }

    val initialDate = existing?.scheduledAt?.toLocalDate() ?: LocalDate.now().plusDays(3)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showHomePicker by remember { mutableStateOf(false) }
    var showAwayPicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: initialDate

    Scaffold(
        topBar = {
            QuinielaTopBar(if (isEditing) "Editar partido" else "Crear partido", onBack)
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ─── Ronda ───────────────────────────────────────────────────────
            SectionLabel("FASE / RONDA")
            RoundSelector(selected = round, onSelect = { round = it; saved = false })

            // ─── Equipos ─────────────────────────────────────────────────────
            SectionLabel("EQUIPOS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeamSelector(
                    label = "Equipo local",
                    teamName = homeTeam,
                    flag = homeFlag,
                    onClick = { showHomePicker = true },
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VS", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                TeamSelector(
                    label = "Equipo visitante",
                    teamName = awayTeam,
                    flag = awayFlag,
                    onClick = { showAwayPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // ─── Fecha ───────────────────────────────────────────────────────
            SectionLabel("FECHA Y HORA")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón fecha
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }

                // Hora y minuto
                OutlinedTextField(
                    value = hour,
                    onValueChange = { v -> hour = v.filter { it.isDigit() }.take(2); saved = false },
                    label = { Text("HH") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = GreenAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(72.dp)
                )
                Text(":", color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = minute,
                    onValueChange = { v -> minute = v.filter { it.isDigit() }.take(2); saved = false },
                    label = { Text("MM") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = GreenAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.width(72.dp)
                )
            }

            // ─── Errores / confirmación ───────────────────────────────────────
            if (showError.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1010)), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(18.dp))
                        Text(showError, color = Color(0xFFEF9A9A), fontSize = 13.sp)
                    }
                }
            }
            if (saved) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3A1F)), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        "✓ Partido ${if (isEditing) "actualizado" else "creado"} correctamente",
                        color = GreenAccent,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ─── Botón guardar ────────────────────────────────────────────────
            Button(
                onClick = {
                    showError = ""
                    val h = hour.toIntOrNull()
                    val m = minute.toIntOrNull()
                    when {
                        homeTeam.isBlank() -> showError = "Selecciona el equipo local."
                        awayTeam.isBlank() -> showError = "Selecciona el equipo visitante."
                        homeTeam == awayTeam -> showError = "Los dos equipos no pueden ser el mismo."
                        h == null || h !in 0..23 -> showError = "Ingresa una hora válida (0-23)."
                        m == null || m !in 0..59 -> showError = "Ingresa minutos válidos (0-59)."
                        else -> {
                            val dateTime = LocalDateTime.of(
                                selectedDate.year, selectedDate.month, selectedDate.dayOfMonth, h, m
                            )
                            val match = Match(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                homeTeam = homeTeam,
                                awayTeam = awayTeam,
                                homeFlag = homeFlag,
                                awayFlag = awayFlag,
                                scheduledAt = dateTime,
                                round = round,
                                status = existing?.status ?: MatchStatus.PENDING
                            )
                            if (isEditing) vm.updateMatch(match) else vm.createMatch(match)
                            saved = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) GoldAccent.copy(alpha = 0.85f) else GreenFieldLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isEditing) "Actualizar partido" else "Crear partido",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEditing) Color.Black else Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─── Diálogos ─────────────────────────────────────────────────────────────

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false; saved = false }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(containerColor = SurfaceDark)
            )
        }
    }

    if (showHomePicker) {
        TeamPickerDialog(
            title = "Equipo local",
            excludeTeam = awayTeam,
            onSelect = { team ->
                homeTeam = team.name
                homeFlag = team.flag
                showHomePicker = false
                saved = false
            },
            onDismiss = { showHomePicker = false }
        )
    }

    if (showAwayPicker) {
        TeamPickerDialog(
            title = "Equipo visitante",
            excludeTeam = homeTeam,
            onSelect = { team ->
                awayTeam = team.name
                awayFlag = team.flag
                showAwayPicker = false
                saved = false
            },
            onDismiss = { showAwayPicker = false }
        )
    }
}

// ─── Componentes internos ─────────────────────────────────────────────────────

@Composable
private fun RoundSelector(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Fila 1: primeras 3 rondas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ROUNDS.take(3).forEach { r ->
                RoundChip(label = r, selected = selected == r, onClick = { onSelect(r) }, modifier = Modifier.weight(1f))
            }
        }
        // Fila 2: últimas 3 rondas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ROUNDS.drop(3).forEach { r ->
                RoundChip(label = r, selected = selected == r, onClick = { onSelect(r) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RoundChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) GreenFieldLight else SurfaceVariantDark
    val border = if (selected) GreenAccent else Color.White.copy(alpha = 0.12f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamSelector(
    label: String,
    teamName: String,
    flag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariantDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (flag.isNotEmpty()) flag else "🏳",
                    fontSize = 32.sp
                )
                Text(
                    text = if (teamName.isNotEmpty()) teamName else "Seleccionar",
                    color = if (teamName.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = if (teamName.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = GreenAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamPickerDialog(
    title: String,
    excludeTeam: String,
    onSelect: (WCTeam) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualFlag by remember { mutableStateOf("") }

    val filtered = WorldCupTeams.all.filter {
        it.name != excludeTeam &&
        (query.isBlank() || it.name.contains(query, ignoreCase = true) || it.confederation.contains(query, ignoreCase = true))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                // Búsqueda
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar equipo…", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Lista de equipos
                LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {

                    // ── Opción manual siempre al tope ────────────────────────
                    item {
                        Surface(
                            onClick = {
                                manualName = query.trim()
                                manualFlag = ""
                                showManualEntry = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = GreenFieldLight.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = GreenAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (query.isNotBlank()) "Usar \"$query\" con bandera personalizada"
                                               else "Escribir nombre y bandera manualmente",
                                        color = GreenAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Útil si la selección no está en la lista",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GreenAccent.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = 0.08f))
                    }

                    // ── Lista de equipos del catálogo ────────────────────────
                    items(filtered, key = { it.name }) { team ->
                        Surface(
                            onClick = { onSelect(team) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = team.flag, fontSize = 22.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(team.name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    if (team.confederation.isNotEmpty()) {
                                        Text(team.confederation, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }

    // ── Diálogo de entrada manual ─────────────────────────────────────────────
    if (showManualEntry) {
        AlertDialog(
            onDismissRequest = { showManualEntry = false },
            containerColor = SurfaceDark,
            title = {
                Text("Ingresar selección", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Nombre
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Nombre del país / equipo") },
                        placeholder = { Text("Ej: Noruega", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = GreenAccent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Bandera
                    OutlinedTextField(
                        value = manualFlag,
                        onValueChange = { manualFlag = it.take(8) },
                        label = { Text("Emoji de bandera") },
                        placeholder = { Text("🏳  Pega el emoji aquí", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = GreenAccent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "💡 Para la bandera: en Android mantén presionado en el campo y pega el emoji, o búscalo en el teclado de emojis.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualName.isNotBlank()) {
                            onSelect(WCTeam(
                                name = manualName.trim(),
                                flag = manualFlag.trim().ifEmpty { "🏳" },
                                confederation = ""
                            ))
                            showManualEntry = false
                        }
                    },
                    enabled = manualName.isNotBlank()
                ) {
                    Text("Confirmar", color = GreenAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualEntry = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}
