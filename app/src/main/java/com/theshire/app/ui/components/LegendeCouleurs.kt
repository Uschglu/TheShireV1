package com.theshire.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.ui.theme.CouleursApp

/**
 * Carte affichant la légende des couleurs du potager
 * (bonne association, neutre, mauvaise).
 */
@Composable
fun LegendeCouleurs() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Légende des couleurs",
                fontWeight = FontWeight.Bold,
                color = CouleursApp.TexteFonce
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(CouleursApp.BonneAssociation)
                        .border(1.dp, CouleursApp.VertPrincipal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    " Bonne association",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(CouleursApp.NeutreAssociation)
                        .border(1.dp, CouleursApp.VertPrincipal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    " Association neutre",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(CouleursApp.MauvaiseAssociation)
                        .border(1.dp, CouleursApp.VertPrincipal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    " Mauvaise association",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "🌱 Les couleurs tiennent compte des carrés voisins (m² adjacents)",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.VertPrincipal,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "🔍 Pincez à deux doigts sur une planche pour zoomer",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.Terracotta,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "👆 Case centrale : choisir entre remplir tout le m² ou une seule case",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.Terracotta
            )
        }
    }
}

/**
 * Carte d'information générique avec un titre et un contenu.
 * Utilisée dans les écrans de détail (légume, adventice, variété).
 */
@Composable
fun InfoCard(titre: String, contenu: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                titre,
                fontWeight = FontWeight.Bold,
                color = CouleursApp.VertPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(contenu, color = CouleursApp.TexteFonce)
        }
    }
}
