package com.quiniela.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quiniela.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { QuinielaTopBar("Reglas de puntuación", onBack) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            RuleSection(
                emoji = "⏱",
                title = "Marcador en 90 minutos",
                content = "El pronóstico principal siempre es el marcador al final del tiempo regular."
            ) {
                RuleItem(points = "3 pts", description = "Marcador exacto en 90'")
                RuleItem(points = "1 pt", description = "Acierta ganador o empate en 90', pero no el marcador exacto")
                RuleItem(points = "0 pts", description = "Falla el resultado en 90'")
            }

            RuleSection(
                emoji = "🥅",
                title = "Bonus por método de desempate",
                content = "Solo aplica si el partido terminó empatado en tiempo regular. Debes haber pronosticado empate."
            ) {
                RuleItem(points = "+1 pt", description = "Aciertas el método: tiempos extra O penales")
                RuleItem(points = "+0 pts", description = "Fallas el método o no pronosticaste empate")
            }

            InfoCard {
                Text(
                    text = "Máximo por partido",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildAnnotatedString {
                        append("4 puntos: ")
                        withStyle(SpanStyle(color = Color.White.copy(alpha = 0.7f))) {
                            append("3 por marcador exacto + 1 por método de desempate")
                        }
                    },
                    color = GoldAccent,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "EJEMPLOS",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            ExampleCard(
                label = "Ejemplo A — 4 puntos",
                prediction = "Japón 1-1 Países Bajos · Japón en penales",
                result = "Japón 1-1 Países Bajos · Japón avanza en penales",
                breakdown = "3 pts (marcador exacto) + 1 pt (penales ✓)",
                total = "4 pts"
            )

            ExampleCard(
                label = "Ejemplo B — 2 puntos",
                prediction = "Japón 0-0 Países Bajos · Japón en penales",
                result = "Japón 1-1 Países Bajos · Japón avanza en penales",
                breakdown = "1 pt (empate correcto, marcador incorrecto) + 1 pt (penales ✓)",
                total = "2 pts"
            )

            ExampleCard(
                label = "Ejemplo C — 3 puntos",
                prediction = "Japón 1-1 Países Bajos · Japón en tiempos extra",
                result = "Japón 1-1 Países Bajos · Japón avanza en penales",
                breakdown = "3 pts (marcador exacto) + 0 pts (método incorrecto: eran penales)",
                total = "3 pts"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RuleSection(
    emoji: String,
    title: String,
    content: String,
    items: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$emoji  $title",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            items()
        }
    }
}

@Composable
private fun RuleItem(points: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = GreenFieldLight.copy(alpha = 0.3f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = points,
                color = GreenAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(text = description, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
    }
}

@Composable
private fun ExampleCard(
    label: String,
    prediction: String,
    result: String,
    breakdown: String,
    total: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LabeledRow("Tu pronóstico", prediction)
            LabeledRow("Resultado real", result)
            LabeledRow("Desglose", breakdown)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Total: $total", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$label:", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}
