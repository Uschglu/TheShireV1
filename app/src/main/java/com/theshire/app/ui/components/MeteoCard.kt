package com.theshire.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.MeteoData
import com.theshire.app.data.PhaseLune
import com.theshire.app.ui.theme.CouleursApp

/**
 * Carte météo affichant les informations de température, humidité,
 * vent, et éventuellement la phase de lune.
 */
@Composable
fun MeteoCard(
    meteo: MeteoData?,
    ville: String,
    estConnecte: Boolean,
    phaseLune: PhaseLune? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "🌦️ Météo à $ville",
                fontWeight = FontWeight.Bold,
                color = CouleursApp.TexteFonce
            )
            if (phaseLune != null) {
                Text(
                    "${phaseLune.emoji} ${phaseLune.nom}",
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (meteo != null) {
                Text("🌡️ ${meteo.temperature}°C", color = CouleursApp.TexteFonce)
                Text("☁️ ${meteo.description}", color = CouleursApp.TexteFonce)
                Text("💧 ${meteo.humidite}%", color = CouleursApp.TexteFonce)
                Text("🌬️ ${meteo.vent} m/s", color = CouleursApp.TexteFonce)
            } else {
                Text("Météo indisponible", color = CouleursApp.TexteFonce)
            }
        }
    }
}

/**
 * Retourne l'emoji météo correspondant à la description.
 */
fun getEmojiMeteo(meteo: MeteoData?): String = when {
    meteo == null -> "🌤️"
    meteo.description.contains("pluie", true) -> "🌧️"
    meteo.description.contains("nuage", true) -> "☁️"
    meteo.description.contains("soleil", true) ||
        meteo.description.contains("clair", true) -> "☀️"
    meteo.description.contains("neige", true) -> "❄️"
    meteo.description.contains("orage", true) -> "⛈️"
    else -> "🌤️"
}
