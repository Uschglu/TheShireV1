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
import com.theshire.app.data.GraineEntity
import com.theshire.app.data.GraineRepository
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Onglet "Graines" de l'écran Stocks.
 * 
 * Contient :
 * - Liste des graines en stock (avec quantité, fournisseur, date)
 * - FAB d'ajout (via DialogAjoutGraine)
 * - Fiche détaillée d'une graine (via FicheGraine)
 * 
 * Indicateurs visuels :
 * - ⚠️ si quantité < 5
 * - 🔴 si quantité = 0 (inactive)
 */
@Composable
fun OngletGraines() {
    val context = LocalContext.current
    val repository = remember { GraineRepository(context) }
    
    val graines by repository.grainesActives.collectAsState(initial = emptyList())
    
    var showAjoutDialog by remember { mutableStateOf(false) }
    var graineSelectionnee by remember { mutableStateOf<GraineEntity?>(null) }
    
    // Si une graine est sélectionnée, afficher sa fiche détaillée
    if (graineSelectionnee != null) {
        FicheGraine(
            graine = graineSelectionnee!!,
            repository = repository,
            onBack = { graineSelectionnee = null }
        )
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (graines.isEmpty()) {
            // Écran vide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🫘", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucune graine",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CouleursApp.TexteFonce
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cliquez sur + pour ajouter les graines que vous possédez.",
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
                        "${graines.size} sachet(s) de graines",
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(graines, key = { it.id }) { graine ->
                    CarteGraine(
                        graine = graine,
                        onClick = { graineSelectionnee = graine }
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
            Icon(Icons.Default.Add, contentDescription = "Ajouter des graines")
        }
    }
    
    // Dialogue d'ajout
    if (showAjoutDialog) {
        DialogAjoutGraine(
            repository = repository,
            onDismiss = { showAjoutDialog = false },
            onGraineAjoutee = { showAjoutDialog = false }
        )
    }
}

/**
 * Carte d'une graine dans la liste.
 * 
 * Affiche :
 * - Emoji + nom + variété
 * - Quantité (avec ⚠️ si < 5)
 * - Fournisseur + année
 * - Date d'ajout
 */
@Composable
fun CarteGraine(
    graine: GraineEntity,
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
            Text(graine.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Ligne 1 : Nom + variété
                val titreComplet = if (graine.varieteNom != null) {
                    "${graine.legumeNom} (${graine.varieteNom})"
                } else {
                    graine.legumeNom
                }
                Text(
                    titreComplet,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // Ligne 2 : Quantité
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val emojiQuantite = when {
                        graine.quantite == 0 -> "🔴"
                        graine.quantite < 5 -> "⚠️"
                        else -> "🫘"
                    }
                    Text(
                        "$emojiQuantite ${graine.quantite} graine(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            graine.quantite == 0 -> MaterialTheme.colorScheme.error
                            graine.quantite < 5 -> CouleursApp.Terracotta
                            else -> CouleursApp.VertPrincipal
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Ligne 3 : Fournisseur + année
                val details = buildString {
                    if (graine.fournisseur != null) append(graine.fournisseur)
                    if (graine.anneeRecolte != null) {
                        if (isNotEmpty()) append(" · ")
                        append(graine.anneeRecolte)
                    }
                }
                if (details.isNotEmpty()) {
                    Text(
                        details,
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                    )
                }
                
                // Ligne 4 : Date d'ajout
                Text(
                    "Ajouté le ${dateFormat.format(Date(graine.dateAjout))}",
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
