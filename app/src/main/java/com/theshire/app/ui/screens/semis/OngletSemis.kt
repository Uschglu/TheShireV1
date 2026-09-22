package com.theshire.app.ui.screens.semis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.data.ModePreferences
import com.theshire.app.ui.theme.CouleursApp

/**
 * Onglet "Semis" de l'écran Jardin.
 *
 * Suit le cycle de vie des semis en cours, du semis à la plantation.
 *
 * ⚠️ N'affiche QUE la catégorie "Semis" (les jeunes plants promus vont dans
 *    Stocks > Plants). Les semis sont en plus filtrés selon le mode actif :
 *  - Mode PROJECTION → semis créés en mode projection
 *  - Mode RÉEL      → semis créés en mode réel
 * 
 * L'onglet Plants de Stocks affiche tous les éléments, tous modes et
 * toutes catégories confondus.
 */
@Composable
fun OngletSemis() {
    val context = LocalContext.current
    val repository = remember { JeunePlantRepository(context) }

    // Mode actif (lu à chaque recomposition — suit les changements du switch)
    val modeReel = remember { mutableStateOf(ModePreferences.estModeReel(context)) }
    
    // Semis filtrés par mode actif + catégorie "Semis"
    val semis by repository
        .getSemisActifsFiltres(modeReel.value)
        .collectAsState(initial = emptyList())

    var showAjoutDialog by remember { mutableStateOf(false) }
    var semisSelectionne by remember { mutableStateOf<JeunePlantEntity?>(null) }

    // Si un semis est sélectionné, afficher sa fiche détaillée
    if (semisSelectionne != null) {
        FicheSemis(
            semis = semisSelectionne!!,
            repository = repository,
            onBack = { semisSelectionne = null }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (semis.isEmpty()) {
            // Écran vide — message adapté au mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (modeReel.value) "🌳" else "🌰",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (modeReel.value) "Aucun semis en mode réel" else "Aucun semis en projection",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CouleursApp.TexteFonce,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (modeReel.value) {
                        "Les semis créés en mode réel déduisent les graines du stock. Bascule en mode projection pour voir tes projections."
                    } else {
                        "Clique sur + pour enregistrer un nouveau semis en projection, sans impact sur tes stocks."
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
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        if (modeReel.value) {
                            "${semis.size} semis en mode réel"
                        } else {
                            "${semis.size} semis en projection"
                        },
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(semis, key = { it.id }) { s ->
                    CarteSemis(
                        semis = s,
                        onClick = { semisSelectionne = s }
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
            Icon(Icons.Default.Add, contentDescription = "Ajouter un semis")
        }
    }

    // Dialogue d'ajout
    if (showAjoutDialog) {
        DialogAjoutSemis(
            repository = repository,
            onDismiss = { showAjoutDialog = false },
            onSemisAjoute = {
                showAjoutDialog = false
                // Rafraîchir le mode actif au cas où il aurait changé
                modeReel.value = ModePreferences.estModeReel(context)
            }
        )
    }
}
