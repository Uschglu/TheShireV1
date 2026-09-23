package com.theshire.app.ui.screens.jardin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.CultureEntity
import com.theshire.app.data.CultureRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch

/**
 * Dialogue de récolte d'une culture.
 * 
 * Demande le poids récolté en kg, puis :
 *  - Crée une RecolteEntity (via CultureRepository.terminerAvecRecolte)
 *  - Marque la culture comme terminée (estActive = false, dateRecolteReelle = now)
 * 
 * La culture disparaît ensuite de la grille / de la liste des emplacements,
 * et la récolte apparaît dans le 4e onglet Stocks.
 */
@Composable
fun DialogRecolterCulture(
    culture: CultureEntity,
    repository: CultureRepository,
    onDismiss: () -> Unit,
    onRecolte: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var poidsSaisi by remember { mutableStateOf("") }
    var notesSaisies by remember { mutableStateOf("") }
    
    val poidsValide = parsePoids(poidsSaisi)?.let { it > 0 } == true
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🌾 Récolter",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${culture.emoji} ${culture.legumeNom}" +
                        if (culture.varieteNom != null) " (${culture.varieteNom})" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    culture.libelleLocalisation(),
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = poidsSaisi,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                        val separateurs = filtered.count { it == ',' || it == '.' }
                        if (separateurs <= 1) {
                            poidsSaisi = filtered
                        }
                    },
                    label = { Text("Poids récolté (kg)") },
                    placeholder = { Text("Ex : 1,250") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    suffix = { Text("kg") }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "💡 Utilise la virgule pour les grammes (ex : 1,250 = 1 kg 250 g)",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = notesSaisies,
                    onValueChange = { notesSaisies = it },
                    label = { Text("Notes (optionnel)") },
                    placeholder = { Text("Ex : Belle récolte, quelques limaces…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val poids = parsePoids(poidsSaisi) ?: 0.0
                    if (poids > 0) {
                        scope.launch {
                            repository.terminerAvecRecolte(
                                cultureId = culture.id,
                                poidsKg = poids,
                                notes = notesSaisies.takeIf { it.isNotBlank() }
                            )
                            onRecolte()
                        }
                    }
                },
                enabled = poidsValide,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Récolter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.VertPrincipal)
            }
        }
    )
}

/**
 * Parse une saisie de poids en kg depuis une chaîne.
 * Accepte la virgule ou le point comme séparateur décimal.
 */
private fun parsePoids(input: String): Double? {
    if (input.isBlank()) return null
    val normalise = input.replace(",", ".")
    return normalise.toDoubleOrNull()
}
