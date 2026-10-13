package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
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
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@Composable
fun AdminPinScreen(
    vm: QuinielaViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    val maxLen = 4

    fun tryPin() {
        if (pin.length == maxLen) {
            if (vm.validateAdminPin(pin)) onSuccess()
            else { error = true; pin = "" }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "🔐", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ingresa tu PIN",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Acceso de administrador",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Indicador de dígitos
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(maxLen) { i ->
                    val filled = i < pin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) GoldAccent
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .border(1.dp, if (filled) GoldAccent else Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }

            if (error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PIN incorrecto. Inténtalo de nuevo.",
                    color = Color(0xFFEF9A9A),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Teclado numérico
            val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
            val grid = keys.chunked(3)
            grid.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { key ->
                        when (key) {
                            "" -> Spacer(modifier = Modifier.size(80.dp))
                            "⌫" -> PinKey(
                                label = key,
                                isIcon = true,
                                onClick = {
                                    if (pin.isNotEmpty()) {
                                        pin = pin.dropLast(1)
                                        error = false
                                    }
                                }
                            )
                            else -> PinKey(
                                label = key,
                                onClick = {
                                    if (pin.length < maxLen) {
                                        pin += key
                                        error = false
                                        if (pin.length == maxLen) tryPin()
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showHint = !showHint }) {
                Text(
                    text = if (showHint) "PIN por defecto: 1234" else "¿Olvidaste tu PIN?",
                    color = GoldAccent.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("← Regresar", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PinKey(label: String, isIcon: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = SurfaceDark,
        modifier = Modifier.size(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isIcon) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Borrar",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
