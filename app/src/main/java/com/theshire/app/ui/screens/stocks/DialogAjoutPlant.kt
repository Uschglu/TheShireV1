@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.data.JeunePlantStades
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LegumeRepository
import com.theshire.app.data.VarieteEntity
import com.theshire.app.data.VarieteRepository
import com.theshire.app.data.getEmojiCategorie
import com.theshire.app.ui.theme.CouleursApp
import com.theshire.app.ui.theme.envelopperAvecTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Dialogue d'ajout d'un jeune plant, en 2 étapes :
 * 
 * Étape 1 : Choisir un légume dans la bibliothèque
 * Étape 2 : Compléter les infos (variété, quantité, stade, dates, emplacement, notes)
 */
@Composable
fun DialogAjoutPlant(
    repository: JeunePlantRepository,
    onDismiss: () -> Unit,
    onPlantAjoute: () -> Unit
) {
    var etape by remember { mutableStateOf(1) }
    var legumeChoisi by remember { mutableStateOf<LegumeEntity?>(null) }
    
    if (etape == 1) {
        Etape1ChoixLegumePlant(
            onLegumeChoisi = { legume ->
                legumeChoisi = legume
                etape = 2
            },
            onDismiss = onDismiss
        )
    } else {
        Etape2DetailsPlant(
            legumeChoisi = legumeChoisi!!,
            repository = repository,
            onRetour = { etape = 1 },
            onValide = { onPlantAjoute() },
            onDismiss = onDismiss
        )
    }
}

/**
 * Étape 1 : choisir un légume dans la bibliothèque.
 */
@Composable
fun Etape1ChoixLegumePlant(
    onLegumeChoisi: (LegumeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val legumeRepository = remember { LegumeRepository(context) }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis() }
    
    var searchQuery by remember { mutableStateOf("") }
    var categorieSelectionnee by remember { mutableStateOf<String?>(null) }
    
    val categories = remember(legumes) {
        legumes.map { it.categorie }.distinct().sorted()
    }
    
    val legumesFiltres = legumes.filter { legume ->
        (searchQuery.isEmpty() || legume.nom.contains(searchQuery, ignoreCase = true)) &&
        (categorieSelectionnee == null || legume.categorie == categorieSelectionnee)
    }.sortedBy { it.nom }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🌱 Choisir un légume",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Text(
                    "Étape 1/2 : Sélectionnez le légume dans la bibliothèque.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("🔍 Rechercher...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = categorieSelectionnee == cat,
                            onClick = {
                                categorieSelectionnee = if (categorieSelectionnee == cat) null else cat
                            },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                ) {
                    items(legumesFiltres, key = { it.id }) { legume ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLegumeChoisi(legume) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                getEmojiCategorie(legume.categorie),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    legume.nom,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text(
                                    legume.categorie,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                                )
                            }
                        }
                        HorizontalDivider(color = CouleursApp.VertPale)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.VertPrincipal)
            }
        }
    )
}

/**
 * Étape 2 : compléter les infos du jeune plant.
 */
@Composable
fun Etape2DetailsPlant(
    legumeChoisi: LegumeEntity,
    repository: JeunePlantRepository,
    onRetour: () -> Unit,
    onValide: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val varieteRepository = remember { VarieteRepository(context) }
    
    // État pour déclencher le rechargement des variétés
var varietesPredefiniesChargees by remember { mutableStateOf(false) }

// 1. D'abord, ajouter les variétés prédéfinies en base (si pas déjà fait)
LaunchedEffect(legumeChoisi.nom) {
    varieteRepository.ajouterVarietesPredefinies()
    varietesPredefiniesChargees = true
}

// 2. Ensuite, charger les variétés du légume choisi (déclenché une fois les prédéfinies chargées)
val varietes by remember(legumeChoisi.nom, varietesPredefiniesChargees) {
    varieteRepository.getVarietesForLegume(legumeChoisi.nom)
}.collectAsState(initial = emptyList())

    
    // États du formulaire
    var varieteSelectionnee by remember { mutableStateOf<VarieteEntity?>(null) }
    var varieteMenuOuvert by remember { mutableStateOf(false) }
    
    var quantite by remember { mutableStateOf("1") }
    
    var stadeSelectionne by remember { mutableStateOf(JeunePlantStades.SEMIS) }
    var stadeMenuOuvert by remember { mutableStateOf(false) }
    
    // Source : semis maison ou achat ?
    var provenanceSemis by remember { mutableStateOf(true) }
    
    var dateSemis by remember { mutableStateOf<Long?>(null) }
    var dateAchat by remember { mutableStateOf<Long?>(null) }
    var fournisseur by remember { mutableStateOf("") }
    
    var emplacementActuel by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRetour) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = CouleursApp.VertPrincipal
                    )
                }
                Text(
                    "${getEmojiCategorie(legumeChoisi.categorie)} ${legumeChoisi.nom}",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Étape 2/2 : Complétez les informations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Variété (menu déroulant)
                if (varietes.isNotEmpty()) {
                    Box {
                        OutlinedTextField(
                            value = varieteSelectionnee?.nom ?: "Variété standard",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Variété") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                IconButton(onClick = { varieteMenuOuvert = true }) {
                                    Text("▼", color = CouleursApp.VertPrincipal)
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { varieteMenuOuvert = true }
                        )
                        
                        DropdownMenu(
                            expanded = varieteMenuOuvert,
                            onDismissRequest = { varieteMenuOuvert = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Variété standard") },
                                onClick = {
                                    varieteSelectionnee = null
                                    varieteMenuOuvert = false
                                }
                            )
                            varietes.forEach { variete ->
                                DropdownMenuItem(
                                    text = { Text(variete.nom) },
                                    onClick = {
                                        varieteSelectionnee = variete
                                        varieteMenuOuvert = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Quantité
                OutlinedTextField(
                    value = quantite,
                    onValueChange = { quantite = it.filter { c -> c.isDigit() } },
                    label = { Text("Nombre de plants") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Stade (menu déroulant)
                Box {
                    OutlinedTextField(
                        value = stadeSelectionne,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Stade de développement") },
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
                        JeunePlantStades.TOUS.forEach { stade ->
                            DropdownMenuItem(
                                text = { Text(stade) },
                                onClick = {
                                    stadeSelectionne = stade
                                    stadeMenuOuvert = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Provenance : semis maison ou achat
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Provenance",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = provenanceSemis,
                                onClick = { provenanceSemis = true },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CouleursApp.VertPrincipal
                                )
                            )
                            Text("🌰 Semé par moi", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !provenanceSemis,
                                onClick = { provenanceSemis = false },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CouleursApp.VertPrincipal
                                )
                            )
                            Text("🛒 Acheté", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date de semis (si provenance = semis)
                if (provenanceSemis) {
                    OutlinedTextField(
                        value = dateSemis?.let { dateFormat.format(Date(it)) } ?: "Non renseignée",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date de semis") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                afficherDatePickerPlant(context) { timestamp ->
                                    dateSemis = timestamp
                                }
                            }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Choisir une date",
                                    tint = CouleursApp.VertPrincipal
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Date d'achat (si provenance = achat)
                    OutlinedTextField(
                        value = dateAchat?.let { dateFormat.format(Date(it)) } ?: "Non renseignée",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date d'achat") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                afficherDatePickerPlant(context) { timestamp ->
                                    dateAchat = timestamp
                                }
                            }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Choisir une date",
                                    tint = CouleursApp.VertPrincipal
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Fournisseur
                    OutlinedTextField(
                        value = fournisseur,
                        onValueChange = { fournisseur = it },
                        label = { Text("Fournisseur (optionnel)") },
                        placeholder = { Text("Ex : Jardiland, Truffaut") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Emplacement actuel
                OutlinedTextField(
                    value = emplacementActuel,
                    onValueChange = { emplacementActuel = it },
                    label = { Text("Emplacement actuel (optionnel)") },
                    placeholder = { Text("Ex : Godet, Mini-serre, Balcon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    placeholder = { Text("Ex : À repiquer dans 2 semaines") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantiteInt = quantite.toIntOrNull() ?: 0
                    
                    if (quantiteInt <= 0) {
                        android.widget.Toast.makeText(
                            context,
                            "Veuillez saisir une quantité valide",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    
                    scope.launch {
                        val plant = JeunePlantEntity(
                            legumeNom = legumeChoisi.nom,
                            varieteNom = varieteSelectionnee?.nom,
                            emoji = getEmojiCategorie(legumeChoisi.categorie),
                            quantite = quantiteInt,
                            stade = stadeSelectionne,
                            dateSemis = if (provenanceSemis) dateSemis else null,
                            dateAchat = if (!provenanceSemis) dateAchat else null,
                            fournisseur = if (!provenanceSemis) fournisseur.ifBlank { null } else null,
                            emplacementActuel = emplacementActuel.ifBlank { null },
                            notes = notes.ifBlank { null },
                            estActif = true
                        )
                        
                        repository.ajouterJeunePlant(plant)
                        onValide()
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.VertPrincipal)
            }
        }
    )
}

/**
 * Affiche un DatePicker pour choisir une date (retourne un timestamp).
 */
fun afficherDatePickerPlant(
    context: android.content.Context,
    onDateChoisie: (Long) -> Unit
) {
    val calendrier = Calendar.getInstance()
    android.app.DatePickerDialog(
        envelopperAvecTheme(context),
        { _, annee, mois, jour ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, annee)
                set(Calendar.MONTH, mois)
                set(Calendar.DAY_OF_MONTH, jour)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateChoisie(cal.timeInMillis)
        },
        calendrier.get(Calendar.YEAR),
        calendrier.get(Calendar.MONTH),
        calendrier.get(Calendar.DAY_OF_MONTH)
    ).show()
}
