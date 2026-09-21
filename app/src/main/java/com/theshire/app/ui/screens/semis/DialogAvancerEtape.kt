package com.theshire.app.ui.screens.semis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.ui.theme.CouleursApp

/**
 * Dialogue de confirmation d'avancement d'étape.
 *
 * Affiche l'étape actuelle, l'étape cible, et la description de la nouvelle étape.
 */
@Composable
fun DialogAvancerEtape(
    etapeActuelle: String,
    etapeCible: String,
    onDismiss: () -> Unit,
    onConfirmer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🌿 Étape suivante",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Actuelle
                Text(
                    "Étape actuelle",
                    style = MaterialTheme.typography.labelMedium,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        JeunePlantEtapes.emoji(etapeActuelle),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        etapeActuelle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CouleursApp.TexteFonce,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Flèche
                Text(
                    "⬇️",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cible
                Text(
                    "Nouvelle étape",
                    style = MaterialTheme.typography.labelMedium,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        JeunePlantEtapes.emoji(etapeCible),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        etapeCible,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CouleursApp.VertPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        JeunePlantEtapes.description(etapeCible),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmer,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.VertPrincipal)
            }
        }
    )
}
