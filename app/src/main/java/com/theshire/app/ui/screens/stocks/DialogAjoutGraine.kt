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
import com.theshire.app.data.GraineEntity
import com.theshire.app.data.GraineRepository
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
 * Dialogue d'ajout d'une graine, en 2 étapes :
 * 
 * Étape 1 : Choisir un légume dans la bibliothèque
 * Étape 2 : Compléter les infos (variété, quantité, fournisseur, dates, notes)
 */
@Composable
fun DialogAjoutGraine(
    repository: GraineRepository,
    onDismiss: () -> Unit,
    onGraineAjoutee: () -> Unit
) {
    var etape by remember { mutableStateOf(1) }
    var legumeChoisi by remember { mutableStateOf<LegumeEntity?>(null) }
    
    if (etape == 1) {
        Etape1ChoixLegume(
            onLegumeChoisi = { legume ->
                legumeChoisi = legume
                etape = 2
            },
            onDismiss = onDismiss
        )
    } else {
        Etape2DetailsGraine(
            legumeChoisi = legumeChoisi!!,
            repository = repository,
            onRetour = { etape = 1 },
            onValide = { onGraineAjoutee() },
            onDismiss = onDismiss
        )
    }
}

/**
 * Étape 1 : choisir un légume dans la bibliothèque.
 */
@Composable
fun Etape1ChoixLegume(
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
                "🫘 Choisir un légume",
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
                
                // Filtres par catégorie (scrollables horizontalement)
                LazyRowCategories(
                    categories = categories,
                    categorieSelectionnee = categorieSelectionnee,
                    onCategorieClick = { cat ->
                        categorieSelectionnee = if (categorieSelectionnee == cat) null else cat
                    }
                )
                
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
 * Filtres par catégorie, en ligne scrollable horizontale.
 */
@Composable
fun LazyRowCategories(
    categories: List<String>,
    categorieSelectionnee: String?,
    onCategorieClick: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories) { cat ->
            FilterChip(
                selected = categorieSelectionnee == cat,
                onClick = { onCategorieClick(cat) },
                label = { Text(cat, style = MaterialTheme.typography.bodySmall) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Étape 2 : compléter les infos de la graine.
 */
@Composable
fun Etape2DetailsGraine(
    legumeChoisi: LegumeEntity,
    repository: GraineRepository,
    onRetour: () -> Unit,
    onValide: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val varieteRepository = remember { VarieteRepository(context) }
    
    // Charger les variétés du légume choisi
    val varietes by varieteRepository.getVarietesForLegume(legumeChoisi.nom)
        .collectAsState(initial = emptyList())
    
    LaunchedEffect(Unit) { varieteRepository.ajouterVarietesPredefinies() }
    
    // États du formulaire
    var varieteSelectionnee by remember { mutableStateOf<VarieteEntity?>(null) }
    var varieteMenuOuvert by remember { mutableStateOf(false) }
    var quantite by remember { mutableStateOf("10") }
    var anneeRecolte by remember { mutableStateOf("") }
    var fournisseur by remember { mutableStateOf("") }
    var dateAchat by remember { mutableStateOf<Long?>(null) }
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
                        // Zone cliquable invisible par-dessus
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
                    label = { Text("Quantité (nombre de graines)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Année de récolte
                OutlinedTextField(
                    value = anneeRecolte,
                    onValueChange = { anneeRecolte = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Année de récolte (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fournisseur
                OutlinedTextField(
                    value = fournisseur,
                    onValueChange = { fournisseur = it },
                    label = { Text("Fournisseur (optionnel)") },
                    placeholder = { Text("Ex : Kokopelli, Sainte-Marthe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date d'achat
                OutlinedTextField(
                    value = dateAchat?.let { dateFormat.format(Date(it)) } ?: "Non renseignée",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date d'achat (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            afficherDatePickerAchat(context) { timestamp ->
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
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    placeholder = { Text("Ex : Sachet ouvert, à utiliser en priorité") },
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
                    val anneeInt = anneeRecolte.toIntOrNull()
                    
                    if (quantiteInt <= 0) {
                        android.widget.Toast.makeText(
                            context,
                            "Veuillez saisir une quantité valide",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    
                    scope.launch {
                        val graine = GraineEntity(
                            legumeNom = legumeChoisi.nom,
                            varieteNom = varieteSelectionnee?.nom,
                            emoji = getEmojiCategorie(legumeChoisi.categorie),
                            quantite = quantiteInt,
                            anneeRecolte = anneeInt,
                            fournisseur = fournisseur.ifBlank { null },
                            dateAchat = dateAchat,
                            notes = notes.ifBlank { null },
                            estActif = true
                        )
                        
                        repository.ajouterGraine(graine)
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
fun afficherDatePickerAchat(
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
