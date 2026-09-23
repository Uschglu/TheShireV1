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
 * ⚠️ NOUVEAU : si `varieteDejaChoisie` est fourni (non null), on saute
 * l'étape de sélection de variété et on va directement à la sélection
 * de la source. Ça évite la double demande.
 *
 * @param legumeNom Nom du légume
 * @param emoji Emoji du légume
 * @param varieteDejaChoisie Si non null, on saute l'étape variété
 * @param onDismiss Callback si annulation
 * @param onValider Callback avec le ChoixPlantation retenu
 */
@Composable
fun DialogPlanterCulture(
    legumeNom: String,
    emoji: String,
    varieteDejaChoisie: String? = null,
    onDismiss: () -> Unit,
    onValider: (ChoixPlantation) -> Unit
) {
    val context = LocalContext.current
    val jeunePlantRepository = remember { JeunePlantRepository(context) }
    val graineRepository = remember { GraineRepository(context) }
    val varieteRepository = remember { VarieteRepository(context) }
    
    // Si la variété est déjà choisie, on démarre directement à l'étape 2
    var etape by remember { mutableStateOf(if (varieteDejaChoisie != null) 2 else 1) }
    var varieteChoisie by remember { mutableStateOf(varieteDejaChoisie) }
    
    val tousPlants by jeunePlantRepository.jeunesPlantsActifs.collectAsState(initial = emptyList())
    val toutesGraines by graineRepository.grainesActives.collectAsState(initial = emptyList())
    
    val plantsDuLegume = tousPlants.filter {
        it.legumeNom.equals(legumeNom, ignoreCase = true) && it.estActif
    }
    val semisDuLegume = plantsDuLegume.filter {
        it.categorie == JeunePlantEntity.CATEGORIE_SEMIS
    }
    val jeunesPlantsDuLegume = plantsDuLegume.filter {
        it.categorie == JeunePlantEntity.CATEGORIE_JEUNE_PLANT
    }
    val grainesDuLegume = toutesGraines.filter {
        it.legumeNom.equals(legumeNom, ignoreCase = true) && it.estActif && it.quantite > 0
    }
    
    // ÉTAPE 1 : choix de la variété (seulement si pas déjà fournie)
    if (etape == 1) {
        VarieteSelectionDialog(
            legumeNom = legumeNom,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
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
    
    // ÉTAPE 2 : choix de la source
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
