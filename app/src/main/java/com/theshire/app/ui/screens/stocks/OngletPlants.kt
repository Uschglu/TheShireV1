@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Onglet "Jeunes plants" de l'écran Stocks.
 * 
 * Contient :
 * - Liste de TOUS les plants et semis en stock (2 catégories confondues)
 * - Un badge discret sur chaque carte indique la catégorie (🌰 Semis / 🌿 Plant)
 * - FAB d'ajout (via DialogAjoutPlant)
 * - Fiche détaillée d'un plant (via FichePlant)
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
                contentPadding = PaddingValues(bottom = LayoutConstantes.PADDING_BAS_FAB)
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
        
        // FAB d'ajout — décalé au-dessus de la barre de navigation
        FloatingActionButton(
            onClick = { showAjoutDialog = true },
            containerColor = CouleursApp.VertClair,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = LayoutConstantes.PADDING_BAS_FAB)
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
 * Carte d'un jeune plant ou semis dans la liste.
 * 
 * Affiche :
 * - Emoji + nom + variété
 * - Badge discret indiquant la catégorie (🌰 Semis / 🌿 Plant)
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
    val emojiStade = JeunePlantEtapes.emoji(plant.stade)
    val couleurStade = couleurPourEtapeStock(plant.stade)
    
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
                // Ligne 1 : Nom + variété + badge catégorie
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val titreComplet = if (plant.varieteNom != null) {
                        "${plant.legumeNom} (${plant.varieteNom})"
                    } else {
                        plant.legumeNom
                    }
                    Text(
                        titreComplet,
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeCategorie(categorie = plant.categorie)
                }
                
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
 * Badge discret de catégorie, affiché à droite du nom.
 *  - "Semis"       → 🌰 sur fond brun clair
 *  - "JeunePlant"  → 🌿 sur fond vert clair
 */
@Composable
fun BadgeCategorie(categorie: String) {
    val (emoji, label, couleurFond, couleurTexte) = when (categorie) {
        JeunePlantEntity.CATEGORIE_JEUNE_PLANT -> Quadruple(
            "🌿",
            "Plant",
            CouleursApp.VertPale,
            CouleursApp.VertPrincipal
        )
        else -> Quadruple(
            "🌰",
            "Semis",
            Color(0xFFF0E6D8),
            Color(0xFF6D4C41)
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(couleurFond)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            "$emoji $label",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = couleurTexte
        )
    }
}

/**
 * Petit helper pour éviter de créer une data class juste pour un quadruple.
 * (Kotlin n'a pas de type natif Quadruple)
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Couleur associée à une étape du cycle de vie pour l'onglet Stocks.
 * (Nom distinct de la version Semis pour éviter les collisions d'import.)
 */
fun couleurPourEtapeStock(stade: String): Color {
    return when (stade) {
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
