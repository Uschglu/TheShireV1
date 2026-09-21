@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.clickable
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
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fiche détaillée d'un jeune plant.
 * 
 * Permet :
 * - De voir toutes les infos (quantité, stade, dates, emplacement, notes)
 * - De changer le stade (menu déroulant)
 * - D'ajuster la quantité (+ / −)
 * - De marquer comme planté (retrait du stock actif)
 * - De supprimer le plant
 */
@Composable
fun FichePlant(
    plant: JeunePlantEntity,
    repository: JeunePlantRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // État local pour suivre le plant à jour
    var plantActuel by remember { mutableStateOf(plant) }
    
    // États des dialogues
    var showSuppressionDialog by remember { mutableStateOf(false) }
    var showModificationQuantiteDialog by remember { mutableStateOf(false) }
    var stadeMenuOuvert by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE) }
    val emojiStade = JeunePlantEtapes.emoji(plantActuel.stade)
    val couleurStade = couleurPourEtapeStock(plantActuel.stade)
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${plantActuel.emoji} ${plantActuel.legumeNom}",
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
                    Text(plantActuel.emoji, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        plantActuel.legumeNom,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (plantActuel.varieteNom != null) {
                        Text(
                            "Variété : ${plantActuel.varieteNom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // === Quantité ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "🌱 Quantité en stock",
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
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    if (plantActuel.quantite > 0) {
                                        repository.decrementerQuantite(plantActuel.id, 1)
                                        val maj = plantActuel.copy(
                                            quantite = plantActuel.quantite - 1
                                        )
                                        plantActuel = maj
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
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${plantActuel.quantite}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "plant(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                        }
                        
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    repository.incrementerQuantite(plantActuel.id, 1)
                                    val maj = plantActuel.copy(
                                        quantite = plantActuel.quantite + 1,
                                        estActif = true
                                    )
                                    plantActuel = maj
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
                    
                    OutlinedButton(
                        onClick = { showModificationQuantiteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Modifier manuellement")
                    }
                }
            }
            
            // === Stade (menu déroulant) ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📈 Stade de développement",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = "$emojiStade ${plantActuel.stade}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Stade actuel") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                IconButton(onClick = { stadeMenuOuvert = true }) {
                                    Text("▼", color = CouleursApp.VertPrincipal)
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { stadeMenuOuvert = true }
                        )
                        
                        DropdownMenu(
                            expanded = stadeMenuOuvert,
                            onDismissRequest = { stadeMenuOuvert = false }
                        ) {
                            // On propose les 6 étapes actives.
                            // L'étape "Planté" est réservée au bouton dédié.
                            JeunePlantEtapes.ACTIVES.forEach { stade ->
                                val em = JeunePlantEtapes.emoji(stade)
                                DropdownMenuItem(
                                    text = { Text("$em $stade") },
                                    onClick = {
                                        scope.launch {
                                            repository.changerStade(plantActuel.id, stade)
                                            plantActuel = plantActuel.copy(stade = stade)
                                        }
                                        stadeMenuOuvert = false
                                    }
                                )
                            }
                        }
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
                    
                    InfoLigne(
                        "Date de semis",
                        plantActuel.dateSemis?.let { dateFormat.format(Date(it)) }
                    )
                    InfoLigne(
                        "Date d'achat",
                        plantActuel.dateAchat?.let { dateFormat.format(Date(it)) }
                    )
                    InfoLigne("Fournisseur", plantActuel.fournisseur)
                    InfoLigne("Emplacement actuel", plantActuel.emplacementActuel)
                    InfoLigne(
                        "Ajouté le",
                        dateFormat.format(Date(plantActuel.dateAjout))
                    )
                    InfoLigne("Statut", if (plantActuel.estActif) "✅ En stock" else "❌ Planté")
                }
            }
            
            // === Notes ===
            if (!plantActuel.notes.isNullOrBlank()) {
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
                                plantActuel.notes!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }
            
            // === Bouton "Marquer comme planté" ===
            if (plantActuel.estActif) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            repository.marquerPlante(plantActuel.id)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)
                ) {
                    Text("✅ Marquer comme planté")
                }
            }
            
            // === Bouton supprimer ===
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
                Text("Supprimer ce plant")
            }
        }
    }
    
    // === Dialogue : modification manuelle de la quantité ===
    if (showModificationQuantiteDialog) {
        var nouvelleQuantite by remember {
            mutableStateOf(plantActuel.quantite.toString())
        }
        
        AlertDialog(
            onDismissRequest = { showModificationQuantiteDialog = false },
            title = { Text("Modifier la quantité", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Combien de plants reste-t-il ?",
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
                            val maj = plantActuel.copy(
                                quantite = q,
                                estActif = q > 0
                            )
                            repository.mettreAJourJeunePlant(maj)
                            plantActuel = maj
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
                Text("Voulez-vous vraiment supprimer ce jeune plant ? Cette action est définitive.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.supprimerJeunePlant(plantActuel)
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
