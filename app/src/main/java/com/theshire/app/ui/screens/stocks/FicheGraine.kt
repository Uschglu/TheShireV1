@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.GraineEntity
import com.theshire.app.data.GraineRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fiche détaillée d'une graine.
 * 
 * Permet :
 * - De voir toutes les infos (quantité, fournisseur, dates, notes)
 * - D'ajuster la quantité (+ / −)
 * - De supprimer la graine
 */
@Composable
fun FicheGraine(
    graine: GraineEntity,
    repository: GraineRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // État local pour suivre la graine à jour (après modif quantité)
    var graineActuelle by remember { mutableStateOf(graine) }
    
    // États pour les dialogues
    var showSuppressionDialog by remember { mutableStateOf(false) }
    var showModificationQuantiteDialog by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE) }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${graineActuelle.emoji} ${graineActuelle.legumeNom}",
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
            // === En-tête : emoji + nom + variété ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(graineActuelle.emoji, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        graineActuelle.legumeNom,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (graineActuelle.varieteNom != null) {
                        Text(
                            "Variété : ${graineActuelle.varieteNom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // === Quantité (avec boutons + / −) ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "🫘 Quantité en stock",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bouton −
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    if (graineActuelle.quantite > 0) {
                                        repository.decrementerQuantite(graineActuelle.id, 1)
                                        val maj = graineActuelle.copy(
                                            quantite = graineActuelle.quantite - 1
                                        )
                                        graineActuelle = maj
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CouleursApp.VertPale
                            )
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        
                        // Quantité actuelle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${graineActuelle.quantite}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    graineActuelle.quantite == 0 -> MaterialTheme.colorScheme.error
                                    graineActuelle.quantite < 5 -> CouleursApp.Terracotta
                                    else -> CouleursApp.VertPrincipal
                                }
                            )
                            Text(
                                "graine(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                        }
                        
                        // Bouton +
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    repository.incrementerQuantite(graineActuelle.id, 1)
                                    val maj = graineActuelle.copy(
                                        quantite = graineActuelle.quantite + 1,
                                        estActif = true
                                    )
                                    graineActuelle = maj
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CouleursApp.VertPale
                            )
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Bouton "Modifier la quantité" (pour les gros changements)
                    OutlinedButton(
                        onClick = { showModificationQuantiteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Modifier manuellement")
                    }
                }
            }
            
            // === Informations détaillées ===
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
                    
                    InfoLigne("Année de récolte", graineActuelle.anneeRecolte?.toString())
                    InfoLigne("Fournisseur", graineActuelle.fournisseur)
                    InfoLigne(
                        "Date d'achat",
                        graineActuelle.dateAchat?.let { dateFormat.format(Date(it)) }
                    )
                    InfoLigne(
                        "Ajouté le",
                        dateFormat.format(Date(graineActuelle.dateAjout))
                    )
                    InfoLigne("Statut", if (graineActuelle.estActif) "✅ Actif" else "❌ Terminé")
                }
            }
            
            // === Notes ===
            if (!graineActuelle.notes.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text("📝", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Notes",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                graineActuelle.notes!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }
            
            // === Bouton supprimer en bas ===
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
                Text("Supprimer ce sachet")
            }
        }
    }
    
    // === Dialogue : modification manuelle de la quantité ===
    if (showModificationQuantiteDialog) {
        var nouvelleQuantite by remember {
            mutableStateOf(graineActuelle.quantite.toString())
        }
        
        AlertDialog(
            onDismissRequest = { showModificationQuantiteDialog = false },
            title = { Text("Modifier la quantité", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Combien de graines reste-t-il dans le sachet ?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nouvelleQuantite,
                        onValueChange = { nouvelleQuantite = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantité") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = nouvelleQuantite.toIntOrNull() ?: 0
                        scope.launch {
                            val maj = graineActuelle.copy(
                                quantite = q,
                                estActif = q > 0
                            )
                            repository.mettreAJourGraine(maj)
                            graineActuelle = maj
                        }
                        showModificationQuantiteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModificationQuantiteDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // === Dialogue : confirmation de suppression ===
    if (showSuppressionDialog) {
        AlertDialog(
            onDismissRequest = { showSuppressionDialog = false },
            title = { Text("Supprimer ?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Voulez-vous vraiment supprimer ce sachet de graines ? Cette action est définitive.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.supprimerGraine(graineActuelle)
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
 * Petite ligne d'information "Clé : Valeur".
 * Affiche "—" si la valeur est null ou vide.
 */
@Composable
fun InfoLigne(cle: String, valeur: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            cle,
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
        )
        Text(
            valeur ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (valeur != null) CouleursApp.TexteFonce else CouleursApp.TexteFonce.copy(alpha = 0.4f)
        )
    }
}
