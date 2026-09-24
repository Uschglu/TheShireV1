package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.data.ModePreferences
import com.theshire.app.data.RecolteEntity
import com.theshire.app.data.RecolteRepository
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Onglet "Récoltes" de l'écran Stocks (4e onglet).
 * 
 * Affiche la liste des récoltes enregistrées (poids en kg) :
 *  - Récoltes automatiques (générées quand on "Récolte" une culture)
 *  - Récoltes manuelles (marché, don, cueillette sauvage)
 * 
 * ⚠️ Les récoltes affichées sont filtrées selon le mode actif :
 *    - Mode projection → uniquement les récoltes "projetées"
 *    - Mode réel       → uniquement les récoltes "réelles"
 * 
 * En haut : un résumé du poids total récolté (pour le mode actif).
 */
@Composable
fun OngletRecoltes() {
    val context = LocalContext.current
    val repository = remember { RecolteRepository(context) }
    
    // Mode actif (lu une fois pour l'affichage du badge, mis à jour à chaque recomposition)
    val modeReel = ModePreferences.estModeReel(context)
    
    // On utilise les flows filtrés par mode
    val recoltes by repository.recoltesDuModeActif()
        .collectAsState(initial = emptyList())
    val poidsTotal by repository.poidsTotalDuModeActif()
        .collectAsState(initial = 0.0)
    
    var showAjoutDialog by remember { mutableStateOf(false) }
    var recolteSelectionnee by remember { mutableStateOf<RecolteEntity?>(null) }
    
    // Fiche détail d'une récolte
    if (recolteSelectionnee != null) {
        FicheRecolte(
            recolte = recolteSelectionnee!!,
            repository = repository,
            onBack = { recolteSelectionnee = null }
        )
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (recoltes.isEmpty()) {
            // Écran vide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🥕", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucune récolte",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CouleursApp.TexteFonce
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (modeReel) {
                        "Les récoltes réelles apparaissent ici quand tu récoltes une culture en mode réel. Tu peux aussi en ajouter manuellement."
                    } else {
                        "Les récoltes projetées apparaissent ici quand tu simules une récolte. Bascule en mode réel pour enregistrer tes vraies récoltes."
                    },
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
                // Résumé : poids total (du mode actif)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🏆 Total récolté",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CouleursApp.VertPrincipal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BadgeMode(modeReel = modeReel)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                String.format("%.3f", poidsTotal).replace(".", ",") + " kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.TexteFonce
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${recoltes.size} récolte(s) ${if (modeReel) "réelle(s)" else "projetée(s)"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Liste des récoltes
                items(recoltes, key = { it.id }) { recolte ->
                    CarteRecolte(
                        recolte = recolte,
                        onClick = { recolteSelectionnee = recolte }
                    )
                }
            }
        }
        
        // FAB d'ajout manuel
        FloatingActionButton(
            onClick = { showAjoutDialog = true },
            containerColor = CouleursApp.VertClair,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = LayoutConstantes.PADDING_BAS_FAB)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter une récolte")
        }
    }
    
    // Dialogue d'ajout manuel
    if (showAjoutDialog) {
        DialogAjoutRecolte(
            repository = repository,
            onDismiss = { showAjoutDialog = false },
            onRecolteAjoutee = { showAjoutDialog = false }
        )
    }
}

/**
 * Petit badge indiquant le mode actif (projection ou réel).
 */
@Composable
private fun BadgeMode(modeReel: Boolean) {
    val (texte, couleur) = if (modeReel) {
        "🌱 Réel" to CouleursApp.VertPrincipal
    } else {
        "🧪 Projection" to CouleursApp.TexteFonce.copy(alpha = 0.6f)
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = couleur.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            texte,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = couleur,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Carte d'une récolte dans la liste.
 */
@Composable
fun CarteRecolte(
    recolte: RecolteEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.FRANCE) }
    
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
            Text(recolte.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Ligne 1 : Nom + variété
                val titreComplet = if (recolte.varieteNom != null) {
                    "${recolte.legumeNom} (${recolte.varieteNom})"
                } else {
                    recolte.legumeNom
                }
                Text(
                    titreComplet,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // Ligne 2 : poids (en gros)
                Text(
                    "⚖️ ${recolte.poidsTexte()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                
                // Ligne 3 : source (auto / manuel) + mode discret
                val sourceTexte = if (recolte.estLieeAUneCulture()) "🌱 Récolté au jardin" else "🛒 Ajout manuel"
                val modeTexte = if (recolte.estProjection) " · 🧪" else " · 🌱"
                Text(
                    sourceTexte + modeTexte,
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                )
                
                // Ligne 4 : date
                Text(
                    "Le ${dateFormat.format(Date(recolte.dateRecolte))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.5f)
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
