package com.theshire.app.ui.screens.semis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Carte d'un semis dans la liste de l'onglet Semis (Jardin).
 *
 * Affiche :
 *  - Emoji + nom du légume (+ variété)
 *  - Quantité
 *  - Étape actuelle (emoji + libellé, coloré)
 *  - Barre de progression du cycle
 *  - Jours depuis le semis
 */
@Composable
fun CarteSemis(
    semis: JeunePlantEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.FRANCE) }
    val emojiEtape = JeunePlantEtapes.emoji(semis.stade)
    val couleurEtape = couleurPourEtape(semis.stade)
    val progression = JeunePlantEtapes.progression(semis.stade)

    // Calcul du nombre de jours depuis le semis (ou l'ajout)
    val joursDepuisSemis: Long? = remember(semis.dateSemis, semis.dateAjout) {
        val reference = semis.dateSemis ?: semis.dateAjout
        if (reference > 0) {
            val diff = System.currentTimeMillis() - reference
            TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(semis.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                // Ligne 1 : nom + variété
                val titreComplet = if (semis.varieteNom != null) {
                    "${semis.legumeNom} (${semis.varieteNom})"
                } else {
                    semis.legumeNom
                }
                Text(
                    titreComplet,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )

                // Ligne 2 : quantité + jours
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "🌱 ${semis.quantite} plant(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.VertPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                    if (joursDepuisSemis != null) {
                        Text(
                            "  •  $joursDepuisSemis j",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Ligne 3 : étape actuelle
                Text(
                    "$emojiEtape ${semis.stade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = couleurEtape,
                    fontWeight = FontWeight.Bold
                )

                // Ligne 4 : barre de progression
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progression },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = couleurEtape,
                    trackColor = CouleursApp.VertPale
                )

                // Ligne 5 : date de semis si connue
                if (semis.dateSemis != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Semé le ${dateFormat.format(Date(semis.dateSemis))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.VertPrincipal
            )
        }
    }
}

/**
 * Retourne la couleur associée à une étape du cycle.
 */
fun couleurPourEtape(etape: String): Color {
    return when (etape) {
        JeunePlantEtapes.SEMIS -> Color(0xFF8D6E63)         // Brun
        JeunePlantEtapes.LEVEE -> CouleursApp.VertClair      // Vert clair
        JeunePlantEtapes.REPIQUE -> CouleursApp.VertPrincipal
        JeunePlantEtapes.REMPOTE -> CouleursApp.VertPrincipal
        JeunePlantEtapes.PRET_A_PLANTER -> Color(0xFF66BB6A) // Vert franc
        JeunePlantEtapes.ENDURCI -> CouleursApp.Terracotta
        JeunePlantEtapes.PLANTE -> CouleursApp.Terracotta
        else -> CouleursApp.VertPrincipal
    }
}
