package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quiniela.app.data.models.User
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@Composable
fun HomeScreen(
    vm: QuinielaViewModel,
    onNavigateMatches: () -> Unit,
    onNavigateStandings: () -> Unit,
    onNavigateRules: () -> Unit,
    onSwitchUser: () -> Unit
) {
    val currentUser by vm.currentUser.collectAsState()
    val isAdmin by vm.isAdminMode.collectAsState()
    val users by vm.users.collectAsState()
    val participants = users.filter { !it.isAdmin }
    val standings = vm.getStandings()
    val myStanding = if (!isAdmin) standings.firstOrNull { it.user.id == currentUser?.id } else null

    var showParticipants by remember { mutableStateOf(false) }

    // Refresca cuando cambia el usuario activo (login del admin o de un jugador)
    LaunchedEffect(currentUser?.id) { vm.refresh() }

    // Refresca participantes justo cuando el admin abre el diálogo
    LaunchedEffect(showParticipants) {
        if (showParticipants) vm.refresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Text(text = "⚽", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Quiniela\nEliminatoria",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Text(text = "Mundial 2026", fontSize = 15.sp, color = GoldAccent, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de usuario activo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdmin) GoldAccent.copy(alpha = 0.12f) else SurfaceVariantDark
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hola, ${currentUser?.name ?: "—"} 👋",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            if (isAdmin) {
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "ADMIN",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isAdmin)
                                "Puedes cargar resultados y gestionar la quiniela"
                            else if (myStanding != null)
                                "Posición ${myStanding.position}° · ${myStanding.totalPoints} pts"
                            else
                                "Aún no tienes puntos registrados",
                            color = if (isAdmin) GoldAccent.copy(alpha = 0.8f) else GreenAccent,
                            fontSize = 12.sp
                        )
                    }
                    // Puntos o icono admin
                    if (!isAdmin && myStanding != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${myStanding.totalPoints}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Text("pts", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                        }
                    } else if (isAdmin) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldAccent.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menú principal
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HomeMenuButton(
                    icon = Icons.Default.Sports,
                    title = "Partidos",
                    subtitle = if (isAdmin) "Ver partidos y cargar resultados" else "Ver y pronosticar partidos",
                    onClick = onNavigateMatches,
                    isPrimary = true
                )
                HomeMenuButton(
                    icon = Icons.Default.Leaderboard,
                    title = "Tabla de posiciones",
                    subtitle = "Ver el ranking de la quiniela",
                    onClick = onNavigateStandings
                )
                HomeMenuButton(
                    icon = Icons.Default.MenuBook,
                    title = "Reglas de puntuación",
                    subtitle = "Cómo se calculan los puntos",
                    onClick = onNavigateRules
                )
                if (isAdmin) {
                    HomeMenuButton(
                        icon = Icons.Default.Group,
                        title = "Gestionar participantes",
                        subtitle = "${participants.size} participante${if (participants.size != 1) "s" else ""} registrado${if (participants.size != 1) "s" else ""}",
                        onClick = { showParticipants = true }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón cambiar usuario
            TextButton(
                onClick = onSwitchUser,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Cambiar usuario",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }
        }
    }

    // ── Diálogo gestión de participantes (solo admin) ──────────────────────────
    if (showParticipants) {
        ParticipantsDialog(
            participants = participants,
            onDelete = { vm.deleteUser(it.id) },
            onDismiss = { showParticipants = false }
        )
    }
}

@Composable
private fun ParticipantsDialog(
    participants: List<User>,
    onDelete: (User) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingDelete by remember { mutableStateOf<User?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Cabecera
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Participantes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

                if (participants.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aún no hay participantes registrados.", color = Color.White.copy(alpha = 0.45f), fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(participants, key = { it.id }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GreenFieldLight.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.name.take(1).uppercase(),
                                        color = GreenAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    user.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { pendingDelete = user },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonRemove,
                                        contentDescription = "Eliminar",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Confirmación de eliminación
    pendingDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = SurfaceDark,
            title = { Text("¿Eliminar participante?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminará a ${user.name} y todos sus pronósticos. Esta acción no se puede deshacer.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { onDelete(user); pendingDelete = null }) {
                    Text("Eliminar", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
private fun HomeMenuButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isPrimary) GreenFieldLight else SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) GoldAccent else GreenAccent,
                modifier = Modifier.size(26.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}
