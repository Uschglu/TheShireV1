package com.theshire.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.getEmojiCategorie
import com.theshire.app.ui.theme.CouleursApp

/**
 * Carte cliquable représentant un légume dans la bibliothèque.
 * Affiche un emoji, le nom, la catégorie et un bouton supprimer.
 */
@Composable
fun LegumeCard(
    legume: LegumeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                getEmojiCategorie(legume.categorie),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    legume.nom,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce
                )
                Text(
                    "${legume.categorie} - ${legume.difficulte}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Carte de conservation affichant le nom du légume et ses
 * méthodes de conservation (séchage, lacto, conserves, congélation).
 */
@Composable
fun ConservationCard(legume: LegumeEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                legume.nom,
                fontWeight = FontWeight.Bold,
                color = CouleursApp.VertPrincipal,
                style = MaterialTheme.typography.titleLarge
            )
            Text(legume.conservation, color = CouleursApp.TexteFonce)
        }
    }
}
