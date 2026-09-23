package com.theshire.app.ui.screens.jardin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.CultureEntity
import com.theshire.app.data.GraineEntity
import com.theshire.app.data.GraineRepository
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.data.VarieteRepository
import com.theshire.app.ui.components.VarieteSelectionDialog
import com.theshire.app.ui.theme.CouleursApp

/**
 * Résultat du dialogue de plantation : indique la source choisie.
 */
data class ChoixPlantation(
    val legumeNom: String,
    val varieteNom: String?,
    val emoji: String,
    val sourceStock: String,
    val sourceStockId: Long?
)

/**
 * Dialogue unifié pour planter une culture.
 *
 * Étapes :
 *  1. Choix de la variété (via VarieteSelectionDialog)
 *  2. Choix de la source :
 *     - 🌰 Depuis un semis (si dispo)
 *     - 🌿 Depuis un jeune plant (si dispo)
 *     - 🫘 Graine directe (si dispo)
 *     - 🚫 Aucune (plant offert/trouvé)
 *
 * ⚠️ V1 simplifiée : on prend le premier semis/plant/graine disponible.
 *    On ne propose pas une liste fine de variétés spécifiques.
 *
 * @param legumeNom Nom du légume à planter
 * @param emoji Emoji du légume
 * @param onDismiss Callback si l'utilisateur annule
 * @param onValider Callback avec le ChoixPlantation retenu
 */
@Composable
fun DialogPlanterCulture(
    legumeNom: String,
    emoji: String,
    onDismiss: () -> Unit,
    onValider: (ChoixPlantation) -> Unit
) {
    val context = LocalContext.current
    val jeunePlantRepository = remember { JeunePlantRepository(context) }
    val graineRepository = remember { GraineRepository(context) }
    val varieteRepository = remember { VarieteRepository(context) }
    
    // Étape actuelle : 1 = variété, 2 = source
    var etape by remember { mutableStateOf(1) }
    
    // Sélection finale
    var varieteChoisie by remember { mutableStateOf<String?>(null) }
    
    // Utilisation directe des flows pour filtrer par légume
    val tousPlants by jeunePlantRepository.jeunesPlantsActifs.collectAsState(initial = emptyList())
    val toutesGraines by graineRepository.grainesActives.collectAsState(initial = emptyList())
    
    // Filtrer par légume
    // ⚠️ CORRECTION : le champ s'appelle `estActif` sur JeunePlantEntity
    val plantsDuLegume = tousPlants.filter {
        it.legumeNom.equals(legumeNom, ignoreCase = true) && it.estActif
    }
    val semisDuLegume = plantsDuLegume.filter {
        it.categorie == JeunePlantEntity.CATEGORIE_SEMIS
    }
    val jeunesPlantsDuLegume = plantsDuLegume.filter {
        it.categorie == JeunePlantEntity.CATEGORIE_JEUNE_PLANT
    }
    // Graines : on garde `estActif` (c'est bien le nom dans GraineEntity)
    val grainesDuLegume = toutesGraines.filter {
        it.legumeNom.equals(legumeNom, ignoreCase = true) && it.estActif && it.quantite > 0
    }
    
    // ============================================================
    // ÉTAPE 1 : choix de la variété
    // ============================================================
    if (etape == 1) {
        VarieteSelectionDialog(
            legumeNom = legumeNom,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
                // nomComplet peut être "Tomate" ou "Tomate (Marmande)"
                val variete = if (nomComplet.contains("(")) {
                    nomComplet.substringAfter("(").substringBefore(")").trim()
                } else {
                    null
                }
                varieteChoisie = variete
                etape = 2
            },
            onDismiss = onDismiss
        )
        return
    }
    
    // ============================================================
    // ÉTAPE 2 : choix de la source
    // ============================================================
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "D'où vient ce plant ?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "$emoji $legumeNom${if (varieteChoisie != null) " ($varieteChoisie)" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "En mode réel, la source choisie sera décrémentée du stock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ----- 🌰 Depuis un semis -----
                    if (semisDuLegume.isNotEmpty()) {
                        item {
                            CarteSource(
                                emoji = "🌰",
                                titre = "Depuis un semis",
                                sousTitre = "${semisDuLegume.sumOf { it.quantite }} disponible(s)",
                                couleur = CouleursApp.VertPale,
                                onClick = {
                                    val semis = semisDuLegume.first()
                                    onValider(
                                        ChoixPlantation(
                                            legumeNom = semis.legumeNom,
                                            varieteNom = semis.varieteNom ?: varieteChoisie,
                                            emoji = semis.emoji,
                                            sourceStock = CultureEntity.SOURCE_SEMIS,
                                            sourceStockId = semis.id
                                        )
                                    )
                                }
                            )
                        }
                    }
                    
                    // ----- 🌿 Depuis un jeune plant -----
                    if (jeunesPlantsDuLegume.isNotEmpty()) {
                        item {
                            CarteSource(
                                emoji = "🌿",
                                titre = "Depuis un jeune plant",
                                sousTitre = "${jeunesPlantsDuLegume.sumOf { it.quantite }} disponible(s)",
                                couleur = CouleursApp.VertPale,
                                onClick = {
                                    val plant = jeunesPlantsDuLegume.first()
                                    onValider(
                                        ChoixPlantation(
                                            legumeNom = plant.legumeNom,
                                            varieteNom = plant.varieteNom ?: varieteChoisie,
                                            emoji = plant.emoji,
                                            sourceStock = CultureEntity.SOURCE_JEUNE_PLANT,
                                            sourceStockId = plant.id
                                        )
                                    )
                                }
                            )
                        }
                    }
                    
                    // ----- 🫘 Graine directe -----
                    if (grainesDuLegume.isNotEmpty()) {
                        item {
                            CarteSource(
                                emoji = "🫘",
                                titre = "Graine semée directe",
                                sousTitre = "${grainesDuLegume.sumOf { it.quantite }} graine(s) disponible(s)",
                                couleur = CouleursApp.VertPale,
                                onClick = {
                                    val graine = grainesDuLegume.first()
                                    onValider(
                                        ChoixPlantation(
                                            legumeNom = graine.legumeNom,
                                            varieteNom = graine.varieteNom ?: varieteChoisie,
                                            emoji = graine.emoji,
                                            sourceStock = CultureEntity.SOURCE_GRAINE,
                                            sourceStockId = graine.id
                                        )
                                    )
                                }
                            )
                        }
                    }
                    
                    // ----- 🚫 Aucune source -----
                    item {
                        CarteSource(
                            emoji = "🚫",
                            titre = "Aucune source",
                            sousTitre = "Plant offert, trouvé, origine inconnue",
                            couleur = CouleursApp.Blanc,
                            onClick = {
                                onValider(
                                    ChoixPlantation(
                                        legumeNom = legumeNom,
                                        varieteNom = varieteChoisie,
                                        emoji = emoji,
                                        sourceStock = CultureEntity.SOURCE_AUCUNE,
                                        sourceStockId = null
                                    )
                                )
                            }
                        )
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
}

/**
 * Carte d'une source dans le dialogue de plantation.
 */
@Composable
private fun CarteSource(
    emoji: String,
    titre: String,
    sousTitre: String,
    couleur: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = couleur),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titre,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    sousTitre,
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
            }
            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.VertPrincipal
            )
        }
    }
}
