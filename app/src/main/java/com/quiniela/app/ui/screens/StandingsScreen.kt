package com.quiniela.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.quiniela.app.data.models.StandingsEntry
import com.quiniela.app.ui.theme.*
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    vm: QuinielaViewModel,
    onBack: () -> Unit
) {
    val standings = remember { vm.getStandings() }
    val currentUser by vm.currentUser.collectAsState()

    Scaffold(
        topBar = { QuinielaTopBar("Tabla de posiciones", onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        if (standings.isEmpty()) {
            EmptyState(
                message = "Aún no hay puntos calculados.\nCompleta partidos para ver el ranking.",
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                // Encabezado de columnas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.width(32.dp))
                    Text("Participante", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Pts", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                    Text("Exac.", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Text("Bonus", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(standings, key = { _, e -> e.user.id }) { _, entry ->
                        StandingRow(
                            entry = entry,
                            isCurrentUser = entry.user.id == currentUser?.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingRow(entry: StandingsEntry, isCurrentUser: Boolean) {
    val backgroundColor = when {
        isCurrentUser -> GreenFieldLight.copy(alpha = 0.25f)
        else -> SurfaceDark
    }
    val positionColor = when (entry.position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.4f)
    }
    val positionEmoji = when (entry.position) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "${entry.position}"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                if (entry.position <= 3) {
                    Text(text = positionEmoji, fontSize = 18.sp)
                } else {
                    Text(
                        text = positionEmoji,
                        color = positionColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Nombre
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.user.name + if (isCurrentUser) " (tú)" else "",
                    color = Color.White,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            // Puntos totales
            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "${entry.totalPoints}",
                    color = if (entry.totalPoints > 0) GoldAccent else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Marcadores exactos
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (entry.exactScores > 0) {
                    BadgeCount(count = entry.exactScores, color = GreenAccent)
                } else {
                    Text("–", color = Color.White.copy(alpha = 0.25f), fontSize = 14.sp)
                }
            }

            // Bonus
            Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                if (entry.bonusHits > 0) {
                    BadgeCount(count = entry.bonusHits, color = GoldAccent)
                } else {
                    Text("–", color = Color.White.copy(alpha = 0.25f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun BadgeCount(count: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
