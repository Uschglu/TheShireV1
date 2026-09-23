@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.jardin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.CultureEntity
import com.theshire.app.data.CultureRepository
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Fiche détaillée d'une culture active.
 * 
 * Permet :
 *  - De voir toutes les infos (légume, variété, localisation, source, mode, dates)
 *  - De récolter (génère une RecolteEntity + clôture la culture)
 *  - De retirer sans récolter (plant mort, arraché, erreur de plantation…)
 *  - De retirer tout le m² si les 9 cases ont la même plante (cas "remplir m²")
 *  - De modifier les notes
 */
@Composable
fun FicheCulture(
    culture: CultureEntity,
    repository: CultureRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var cultureActuelle by remember { mutableStateOf(culture) }
    
    var showRecolteDialog by remember { mutableStateOf(false) }
    var showRetraitDialog by remember { mutableStateOf(false) }
    var showRetraitM2Dialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    
    // Nombre de cases identiques dans le carré (pour détecter un m² entier)
    var nombreCasesIdentiques by remember { mutableStateOf(1) }
    
    // Charger le nombre de cases identiques au démarrage
    LaunchedEffect(culture.id) {
        if (culture.typeEmplacement == CultureEntity.TYPE_PLEINE_TERRE &&
            culture.carreId != null) {
            nombreCasesIdentiques = repository.compterCasesIdentiques(culture)
        }
    }
    
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE) }
    
    val joursDepuisPlantation = remember(cultureActuelle.datePlantation) {
        val diff = System.currentTimeMillis() - cultureActuelle.datePlantation
        TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }
    
    // Détection "m² entier" : au moins 4 cases identiques (les 9 normalement)
    val peutViderM2 = cultureActuelle.typeEmplacement == CultureEntity.TYPE_PLEINE_TERRE &&
                      cultureActuelle.carreId != null &&
                      nombreCasesIdentiques >= 4 &&
                      cultureActuelle.estActive
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${cultureActuelle.emoji} ${cultureActuelle.legumeNom}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CouleursApp.VertPrincipal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== En-tête =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(cultureActuelle.emoji, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        cultureActuelle.legumeNom,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (cultureActuelle.varieteNom != null) {
                        Text(
                            "Variété : ${cultureActuelle.varieteNom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        cultureActuelle.libelleLocalisation(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CouleursApp.VertPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "🌱 $joursDepuisPlantation jour(s) de croissance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                    )
                }
            }
            
            // ===== Bouton Récolter (principal) =====
            if (cultureActuelle.estActive) {
                Button(
                    onClick = { showRecolteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)
                ) {
                    Text("🌾 Récolter cette culture", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // ===== Bouton Retirer sans récolter (secondaire, bien visible) =====
            if (cultureActuelle.estActive) {
                OutlinedButton(
                    onClick = { showRetraitDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "🗑️ Retirer sans récolter (libérer la case)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // ===== Bouton Retirer tout le m² (si détecté) =====
            if (peutViderM2) {
                OutlinedButton(
                    onClick = { showRetraitM2Dialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "🗑️ Retirer tout le m² ($nombreCasesIdentiques cases)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // ===== Informations =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📋 Informations",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LigneInfoCulture(
                        "Plante",
                        "${cultureActuelle.emoji} ${cultureActuelle.legumeNom}" +
                            if (cultureActuelle.varieteNom != null) " (${cultureActuelle.varieteNom})" else ""
                    )
                    LigneInfoCulture(
                        "Localisation",
                        cultureActuelle.libelleLocalisation()
                    )
                    if (peutViderM2) {
                        LigneInfoCulture(
                            "Remplissage",
                            "🌱 $nombreCasesIdentiques cases identiques"
                        )
                    }
                    LigneInfoCulture(
                        "Source",
                        cultureActuelle.libelleSource()
                    )
                    LigneInfoCulture(
                        "Mode",
                        if (cultureActuelle.estProjection) "🌱 Projection" else "🌳 Réel"
                    )
                    LigneInfoCulture(
                        "Quantité",
                        "${cultureActuelle.quantite} plant(s)"
                    )
                    LigneInfoCulture(
                        "Date de plantation",
                        dateFormat.format(Date(cultureActuelle.datePlantation))
                    )
                    LigneInfoCulture(
                        "Statut",
                        if (cultureActuelle.estActive) "✅ En croissance" else "🏁 Terminée"
                    )
                }
            }
            
            // ===== Notes =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📝 Notes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (cultureActuelle.notes.isNullOrBlank()) {
                        Text(
                            "Aucune note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            cultureActuelle.notes!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (cultureActuelle.notes.isNullOrBlank()) "Ajouter une note"
                            else "Modifier la note"
                        )
                    }
                }
            }
            
            // ===== Spacer pour ne pas être collé à la barre de navigation =====
            Spacer(modifier = Modifier.height(LayoutConstantes.PADDING_BAS_FAB))
        }
    }
    
    // ===== Dialogue : récolter =====
    if (showRecolteDialog) {
        DialogRecolterCulture(
            culture = cultureActuelle,
            repository = repository,
            onDismiss = { showRecolteDialog = false },
            onRecolte = {
                showRecolteDialog = false
                onBack()
            }
        )
    }
    
    // ===== Dialogue : retrait simple (une case) =====
    if (showRetraitDialog) {
        AlertDialog(
            onDismissRequest = { showRetraitDialog = false },
            title = { Text("Retirer cette culture ?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "La culture sera marquée comme terminée et la case " +
                    "sera libérée. Aucune récolte ne sera enregistrée dans tes Stocks.\n\n" +
                    "Utilise cette option si la plante est morte, arrachée, ou si tu " +
                    "t'es trompé en la plantant."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.terminerSansRecolte(cultureActuelle.id)
                            showRetraitDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retirer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRetraitDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue : retrait de tout le m² =====
    if (showRetraitM2Dialog) {
        AlertDialog(
            onDismissRequest = { showRetraitM2Dialog = false },
            title = { Text("Retirer tout le m² ?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Les $nombreCasesIdentiques cases contenant " +
                    "${cultureActuelle.legumeNom}" +
                    if (cultureActuelle.varieteNom != null) " (${cultureActuelle.varieteNom})" else "" +
                    " seront libérées.\n\n" +
                    "Aucune récolte ne sera enregistrée dans tes Stocks.\n\n" +
                    "Utilise cette option si tu veux vider tout le m² d'un coup."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val carreId = cultureActuelle.carreId
                            if (carreId != null) {
                                val identiques = repository.getCulturesIdentiquesDansCarre(
                                    carreId = carreId,
                                    legumeNom = cultureActuelle.legumeNom,
                                    varieteNom = cultureActuelle.varieteNom,
                                    datePlantation = cultureActuelle.datePlantation
                                )
                                repository.terminerSansRecolteMultiple(identiques.map { it.id })
                            }
                            showRetraitM2Dialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retirer tout le m²")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRetraitM2Dialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue : notes =====
    if (showNotesDialog) {
        var nouvellesNotes by remember {
            mutableStateOf(cultureActuelle.notes ?: "")
        }
        
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Notes", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nouvellesNotes,
                    onValueChange = { nouvellesNotes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notesFinales = nouvellesNotes.takeIf { it.isNotBlank() }
                        scope.launch {
                            repository.mettreAJourNotes(cultureActuelle.id, notesFinales)
                            cultureActuelle = cultureActuelle.copy(notes = notesFinales)
                        }
                        showNotesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
}

@Composable
private fun LigneInfoCulture(label: String, valeur: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
        )
        Text(
            valeur,
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce,
            fontWeight = FontWeight.Medium
        )
    }
}
