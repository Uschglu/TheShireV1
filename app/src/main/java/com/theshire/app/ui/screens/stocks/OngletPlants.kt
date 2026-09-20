@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.data.JeunePlantStades
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Onglet "Jeunes plants" de l'écran Stocks.
 * 
 * Contient :
 * - Liste des jeunes plants en stock (avec stade, quantité, dates)
 * - FAB d'ajout (via DialogAjoutPlant)
 * - Fiche détaillée d'un plant (via FichePlant)
 * 
 * Indicateurs visuels :
 * - Le stade est affiché avec un emoji et une couleur
 * - Les plants "Prêt à planter" sont mis en avant
 */
@Composable
fun OngletPlants() {
    val context = LocalContext.current
    val repository = remember { JeunePlantRepository(context) }
    
    val plants by repository.jeunesPlantsActifs.collectAsState(initial = emptyList())
    
    var showAjoutDialog by remember { mutableStateOf(false) }
    var plantSelectionne by remember { mutableStateOf<JeunePlantEntity?>(null) }
    
    // Si un plant est sélectionné, afficher sa fiche détaillée
    if (plantSelectionne != null) {
        FichePlant(
            plant = plantSelectionne!!,
            repository = repository,
            onBack = { plantSelectionne = null }
        )
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (plants.isEmpty()) {
            // Écran vide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🌱", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucun jeune plant",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CouleursApp.TexteFonce
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cliquez sur + pour ajouter les plants que vous avez semés ou achetés.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        "${plants.size} variété(s) de plants",
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(plants, key = { it.id }) { plant ->
                    CartePlant(
                        plant = plant,
                        onClick = { plantSelectionne = plant }
                    )
                }
            }
        }
        
        // FAB d'ajout
        FloatingActionButton(
            onClick = { showAjoutDialog = true },
            containerColor = CouleursApp.VertClair,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter un plant")
        }
    }
    
    // Dialogue d'ajout
    if (showAjoutDialog) {
        DialogAjoutPlant(
            repository = repository,
            onDismiss = { showAjoutDialog = false },
            onPlantAjoute = { showAjoutDialog = false }
        )
    }
}

/**
 * Carte d'un jeune plant dans la liste.
 * 
 * Affiche :
 * - Emoji + nom + variété
 * - Quantité
 * - Stade (avec emoji et couleur)
 * - Emplacement actuel
 * - Date (semis ou achat)
 */
@Composable
fun CartePlant(
    plant: JeunePlantEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.FRANCE) }
    val (emojiStade, couleurStade) = getStylePourStade(plant.stade)
    
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
            Text(plant.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Ligne 1 : Nom + variété
                val titreComplet = if (plant.varieteNom != null) {
                    "${plant.legumeNom} (${plant.varieteNom})"
                } else {
                    plant.legumeNom
                }
                Text(
                    titreComplet,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // Ligne 2 : Quantité
                Text(
                    "🌱 ${plant.quantite} plant(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                
                // Ligne 3 : Stade
                Text(
                    "$emojiStade ${plant.stade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = couleurStade,
                    fontWeight = FontWeight.Bold
                )
                
                // Ligne 4 : Emplacement
                if (!plant.emplacementActuel.isNullOrBlank()) {
                    Text(
                        "📍 ${plant.emplacementActuel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                    )
                }
                
                // Ligne 5 : Date
                val dateRef = plant.dateSemis ?: plant.dateAchat
                if (dateRef != null) {
                    val label = if (plant.dateSemis != null) "Semé le" else "Acheté le"
                    Text(
                        "$label ${dateFormat.format(Date(dateRef))}",
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
 * Retourne l'emoji et la couleur associés à un stade.
 */
fun getStylePourStade(stade: String): Pair<String, Color> {
    return when (stade) {
        JeunePlantStades.SEMIS -> Pair("🌰", Color(0xFF8D6E63))
        JeunePlantStades.REPIQUE -> Pair("🌿", CouleursApp.VertPrincipal)
        JeunePlantStades.PRET_A_PLANTER -> Pair("✅", Color(0xFF66BB6A))
        JeunePlantStades.ENDURCI -> Pair("💪", CouleursApp.Terracotta)
        else -> Pair("🌱", CouleursApp.VertPrincipal)
    }
}
