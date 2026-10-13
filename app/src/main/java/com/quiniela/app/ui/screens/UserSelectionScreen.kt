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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quiniela.app.data.models.User
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@Composable
fun UserSelectionScreen(
    vm: QuinielaViewModel,
    onUserSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    val users by vm.users.collectAsState()
    // Participantes = todos excepto el admin
    val participants = users.filter { !it.isAdmin }

    var showRegisterDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    // Nombre del usuario que acabamos de registrar; null cuando no hay registro pendiente.
    // Evita que el LaunchedEffect navegue automáticamente al abrir la pantalla.
    var pendingRegistrationName by remember { mutableStateOf<String?>(null) }

    fun register() {
        when {
            newName.isBlank() -> nameError = "Escribe tu nombre para continuar."
            newName.trim().length < 2 -> nameError = "El nombre debe tener al menos 2 letras."
            participants.any { it.name.equals(newName.trim(), ignoreCase = true) } ->
                nameError = "Ya existe alguien con ese nombre. Elige otro."
            else -> {
                val trimmed = newName.trim()
                vm.addUser(trimmed)
                pendingRegistrationName = trimmed
                showRegisterDialog = false
                newName = ""
                nameError = ""
            }
        }
    }

    // Refresca datos al entrar a la pantalla (cubre el caso de cambiar de usuario)
    LaunchedEffect(Unit) { vm.refresh() }

    // Solo navega cuando hubo un registro explícito y el usuario nuevo aparece en la lista
    LaunchedEffect(participants, pendingRegistrationName) {
        val pending = pendingRegistrationName ?: return@LaunchedEffect
        val newUser = participants.firstOrNull { it.name.equals(pending, ignoreCase = true) }
        if (newUser != null) {
            vm.selectRegularUser(newUser, rememberMe)
            pendingRegistrationName = null
            onUserSelected()
        }
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
            Spacer(modifier = Modifier.height(52.dp))

            Text("⚽", fontSize = 44.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quiniela · Mundial 2026",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (participants.isEmpty()) "¡Sé el primero en unirte!" else "¿Quién eres?",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Botón principal: Registrarme ─────────────────────────────
            Button(
                onClick = { showRegisterDialog = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenFieldLight),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (participants.isEmpty()) "Registrarme para jugar" else "Soy nuevo · Registrarme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (participants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                    Text("o selecciona tu perfil", color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de participantes registrados
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(participants, key = { it.id }) { user ->
                        ParticipantCard(
                            user = user,
                            onClick = {
                                vm.selectRegularUser(user, rememberMe)
                                onUserSelected()
                            }
                        )
                    }
                    // Admin siempre visible al final
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                        Spacer(modifier = Modifier.height(4.dp))
                        AdminCard(onClick = onAdminSelected)
                    }
                }
            } else {
                // Estado vacío: solo el admin existe (siempre visible)
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(8.dp))
                AdminCard(onClick = onAdminSelected)
            }

            // "Recordarme" — solo para participantes
            if (participants.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = GreenAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Recordarme en este dispositivo",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ─── Diálogo de registro ───────────────────────────────────────────
        if (showRegisterDialog) {
            Dialog(onDismissRequest = { showRegisterDialog = false; newName = ""; nameError = "" }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("👋", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¿Cómo te llamas?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Ingresa el nombre que verán los demás.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it; nameError = "" },
                            label = { Text("Tu nombre") },
                            placeholder = { Text("Ej: Carlos, María, El Profe…") },
                            singleLine = true,
                            isError = nameError.isNotEmpty(),
                            supportingText = if (nameError.isNotEmpty()) {
                                { Text(nameError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = GreenAccent,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showRegisterDialog = false; newName = ""; nameError = "" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Cancelar") }
                            Button(
                                onClick = {
                                    register()
                                    // Si no hay error, el usuario quedó creado; actualizamos la lista
                                    // La navegación se hace en LaunchedEffect abajo
                                },
                                enabled = newName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenFieldLight),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("¡Entrar!", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ParticipantCard(user: User, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenFieldLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = user.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.25f))
        }
    }
}

@Composable
private fun AdminCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = GoldAccent.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Administrador", color = GoldAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Acceso con PIN · gestiona partidos y resultados", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
            }
            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
    }
}
