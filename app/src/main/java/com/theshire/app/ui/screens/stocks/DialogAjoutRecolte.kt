package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LegumeRepository
import com.theshire.app.data.RecolteEntity
import com.theshire.app.data.RecolteRepository
import com.theshire.app.data.getEmojiCategorie
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch

/**
 * Dialogue d'ajout manuel d'une récolte.
 * 
 * Utilisé pour enregistrer des récoltes qui ne proviennent pas d'une culture
 * suivie dans l'app (marché, don, cueillette sauvage, etc.).
 * 
 * Étapes :
 *  1. Choix du légume (avec recherche)
 *  2. Saisie du poids en kg + notes optionnelles
 */
@Composable
fun DialogAjoutRecolte(
    repository: RecolteRepository,
    onDismiss: () -> Unit,
    onRecolteAjoutee: () -> Unit
) {
    val context = LocalContext.current
    val legumeRepository = remember { LegumeRepository(context) }
    val scope = rememberCoroutineScope()
    
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    
    // Étape 1 = choix légume, Étape 2 = poids
    var etape by remember { mutableStateOf(1) }
    var legumeChoisi by remember { mutableStateOf<LegumeEntity?>(null) }
    var poidsSaisi by remember { mutableStateOf("") }
    var notesSaisies by remember { mutableStateOf("") }
    
    // Charge les légumes prédéfinis si la base est vide
    LaunchedEffect(Unit) {
        legumeRepository.ajouterLegumesPredefinis()
    }
    
    // ============================================================
    // ÉTAPE 1 : choix du légume
    // ============================================================
    if (etape == 1) {
        var searchQuery by remember { mutableStateOf("") }
        
        val legumesFiltres = legumes
            .filter { searchQuery.isEmpty() || it.nom.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.nom }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "🥕 Nouvelle récolte",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                    Text(
                        "Choisis le légume que tu as récolté.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("🔍 Rechercher...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
                    ) {
                        items(legumesFiltres, key = { it.id }) { legume ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        legumeChoisi = legume
                                        etape = 2
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    getEmojiCategorie(legume.categorie),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        legume.nom,
                                        fontWeight = FontWeight.Bold,
                                        color = CouleursApp.TexteFonce
                                    )
                                    Text(
                                        legume.categorie,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CouleursApp.VertPrincipal
                                    )
                                }
                                Text(
                                    "›",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CouleursApp.VertPrincipal
                                )
                            }
                            HorizontalDivider(color = CouleursApp.VertPale)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
        return
    }
    
    // ============================================================
    // ÉTAPE 2 : saisie du poids + notes
    // ============================================================
    val legume = legumeChoisi ?: return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "⚖️ Poids récolté",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${getEmojiCategorie(legume.categorie)} ${legume.nom}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = poidsSaisi,
                    onValueChange = { input ->
                        // Accepte les chiffres et une seule virgule ou point
                        val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                        // Empêche plusieurs séparateurs
                        val separateurs = filtered.count { it == ',' || it == '.' }
                        if (separateurs <= 1) {
                            poidsSaisi = filtered
                        }
                    },
                    label = { Text("Poids (kg)") },
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
                    placeholder = { Text("Ex : Marché, don de mamie...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val poids = parsePoids(poidsSaisi)
                    if (poids != null && poids > 0) {
                        scope.launch {
                            val recolte = RecolteEntity(
                                legumeNom = legume.nom,
                                varieteNom = null,
                                emoji = getEmojiCategorie(legume.categorie),
                                poidsKg = poids,
                                dateRecolte = System.currentTimeMillis(),
                                cultureId = null, // Ajout manuel
                                notes = notesSaisies.takeIf { it.isNotBlank() }
                            )
                            repository.ajouterRecolte(recolte)
                            onRecolteAjoutee()
                        }
                    }
                },
                enabled = parsePoids(poidsSaisi)?.let { it > 0 } == true,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                etape = 1
                legumeChoisi = null
                poidsSaisi = ""
                notesSaisies = ""
            }) {
                Text("Retour", color = CouleursApp.VertPrincipal)
            }
        }
    )
}

/**
 * Parse une saisie de poids en kg depuis une chaîne.
 * Accepte la virgule ou le point comme séparateur décimal.
 * 
 * Ex :
 *  "1,250" → 1.25
 *  "1.250" → 1.25
 *  "2"     → 2.0
 *  ""      → null
 */
private fun parsePoids(input: String): Double? {
    if (input.isBlank()) return null
    val normalise = input.replace(",", ".")
    return normalise.toDoubleOrNull()
}
