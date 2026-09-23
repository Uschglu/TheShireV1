@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

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
import androidx.compose.material.icons.filled.Delete
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
import com.theshire.app.data.RecolteEntity
import com.theshire.app.data.RecolteRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fiche détaillée d'une récolte.
 * 
 * Permet :
 *  - De voir toutes les infos (légume, poids, date, notes, source)
 *  - De modifier le poids (dialogue)
 *  - De modifier les notes
 *  - De supprimer la récolte
 */
@Composable
fun FicheRecolte(
    recolte: RecolteEntity,
    repository: RecolteRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var recolteActuelle by remember { mutableStateOf(recolte) }
    
    var showModificationPoidsDialog by remember { mutableStateOf(false) }
    var showModificationNotesDialog by remember { mutableStateOf(false) }
    var showSuppressionDialog by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE) }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${recolteActuelle.emoji} ${recolteActuelle.legumeNom}",
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
                actions = {
                    IconButton(onClick = { showSuppressionDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
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
            // ===== En-tête : emoji + nom + poids =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(recolteActuelle.emoji, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        recolteActuelle.legumeNom,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (recolteActuelle.varieteNom != null) {
                        Text(
                            "Variété : ${recolteActuelle.varieteNom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "⚖️ ${recolteActuelle.poidsTexte()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal
                    )
                }
            }
            
            // ===== Poids (modifier) =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "⚖️ Poids récolté",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showModificationPoidsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Modifier le poids")
                    }
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
                    
                    LigneInfoRecolte(
                        "Date de récolte",
                        dateFormat.format(Date(recolteActuelle.dateRecolte))
                    )
                    LigneInfoRecolte(
                        "Source",
                        if (recolteActuelle.estLieeAUneCulture()) "🌱 Récolté au jardin"
                        else "🛒 Ajout manuel"
                    )
                    LigneInfoRecolte(
                        "Ajouté le",
                        dateFormat.format(Date(recolteActuelle.dateAjout))
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
                    
                    if (recolteActuelle.notes.isNullOrBlank()) {
                        Text(
                            "Aucune note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            recolteActuelle.notes!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showModificationNotesDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (recolteActuelle.notes.isNullOrBlank()) "Ajouter une note" else "Modifier la note")
                    }
                }
            }
            
            // ===== Bouton supprimer =====
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showSuppressionDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supprimer cette récolte")
            }
        }
    }
    
    // ===== Dialogue : modification du poids =====
    if (showModificationPoidsDialog) {
        var nouveauPoids by remember {
            mutableStateOf(
                String.format("%.3f", recolteActuelle.poidsKg).replace(".", ",")
            )
        }
        
        AlertDialog(
            onDismissRequest = { showModificationPoidsDialog = false },
            title = { Text("Modifier le poids", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Nouveau poids en kg :",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nouveauPoids,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                            val separateurs = filtered.count { it == ',' || it == '.' }
                            if (separateurs <= 1) {
                                nouveauPoids = filtered
                            }
                        },
                        label = { Text("Poids (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        suffix = { Text("kg") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val poids = nouveauPoids.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (poids > 0) {
                            scope.launch {
                                repository.mettreAJourPoids(recolteActuelle.id, poids)
                                recolteActuelle = recolteActuelle.copy(poidsKg = poids)
                            }
                        }
                        showModificationPoidsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModificationPoidsDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue : modification des notes =====
    if (showModificationNotesDialog) {
        var nouvellesNotes by remember {
            mutableStateOf(recolteActuelle.notes ?: "")
        }
        
        AlertDialog(
            onDismissRequest = { showModificationNotesDialog = false },
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
                            repository.mettreAJourNotes(recolteActuelle.id, notesFinales)
                            recolteActuelle = recolteActuelle.copy(notes = notesFinales)
                        }
                        showModificationNotesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModificationNotesDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue : confirmation de suppression =====
    if (showSuppressionDialog) {
        AlertDialog(
            onDismissRequest = { showSuppressionDialog = false },
            title = { Text("Supprimer ?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Voulez-vous vraiment supprimer cette récolte ?\n\n" +
                    "Si elle provient d'une culture, la culture reste marquée comme " +
                    "récoltée (l'historique est conservé)."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.supprimerRecolte(recolteActuelle)
                            showSuppressionDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuppressionDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
}

/**
 * Une ligne "info" : libellé + valeur.
 */
@Composable
private fun LigneInfoRecolte(label: String, valeur: String) {
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
