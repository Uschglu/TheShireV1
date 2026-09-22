package com.theshire.app.ui.screens.semis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LegumeRepository
import com.theshire.app.data.ResultatCreationSemis
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
 * Dialogue d'ajout d'un semis, en 2 étapes :
 *
 * Étape 1 : Choisir un légume dans la bibliothèque
 * Étape 2 : Compléter les infos (variété, quantité, date de semis, emplacement, notes)
 *
 * Le stade initial est verrouillé à "Semis" — la progression se fera
 * ensuite depuis la fiche détail du semis.
 *
 * En mode réel (ModePreferences), la création vérifie et décrémente
 * les graines du stock avant de créer le semis.
 */
@Composable
fun DialogAjoutSemis(
    repository: JeunePlantRepository,
    onDismiss: () -> Unit,
    onSemisAjoute: () -> Unit
) {
    var etape by remember { mutableStateOf(1) }
    var legumeChoisi by remember { mutableStateOf<LegumeEntity?>(null) }

    if (etape == 1) {
        Etape1ChoixLegumeSemis(
            onLegumeChoisi = { legume ->
                legumeChoisi = legume
                etape = 2
            },
            onDismiss = onDismiss
        )
    } else {
        Etape2DetailsSemis(
            legumeChoisi = legumeChoisi!!,
            repository = repository,
            onRetour = { etape = 1 },
            onValide = { onSemisAjoute() },
            onDismiss = onDismiss
        )
    }
}

/**
 * Étape 1 : choisir un légume dans la bibliothèque.
 * Reprend le style de Etape1ChoixLegumePlant (recherche + filtres catégorie).
 */
@Composable
fun Etape1ChoixLegumeSemis(
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
                    "Étape 1/2 : Sélectionnez le légume à semer.",
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

                LazyRow(
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
 * Étape 2 : compléter les infos du semis.
 */
@Composable
fun Etape2DetailsSemis(
    legumeChoisi: LegumeEntity,
    repository: JeunePlantRepository,
    onRetour: () -> Unit,
    onValide: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val varieteRepository = remember { VarieteRepository(context) }

    var varietesPredefiniesChargees by remember { mutableStateOf(false) }

    LaunchedEffect(legumeChoisi.nom) {
        varieteRepository.ajouterVarietesPredefinies()
        varietesPredefiniesChargees = true
    }

    val varietes by remember(legumeChoisi.nom, varietesPredefiniesChargees) {
        varieteRepository.getVarietesForLegume(legumeChoisi.nom)
    }.collectAsState(initial = emptyList())

    // États du formulaire
    var varieteSelectionnee by remember { mutableStateOf<VarieteEntity?>(null) }
    var varieteMenuOuvert by remember { mutableStateOf(false) }

    var quantite by remember { mutableStateOf("1") }

    // Date de semis pré-remplie à aujourd'hui
    var dateSemis by remember {
        mutableStateOf<Long?>(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis)
    }

    var emplacementActuel by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Message d'erreur (mode réel uniquement)
    var erreur by remember { mutableStateOf<String?>(null) }

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
                    "Étape 2/2 : Complétez les informations du semis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Bandeau : étape initiale verrouillée
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CouleursApp.VertPale
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌰", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Stade initial : Semis",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Le semis sera ajouté au début du cycle. Tu pourras le faire avancer d'étape depuis sa fiche.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
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
                    label = { Text("Nombre de graines semées") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date de semis
                OutlinedTextField(
                    value = dateSemis?.let { dateFormat.format(Date(it)) } ?: "Non renseignée",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date de semis") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            afficherDatePickerSemis(context) { timestamp ->
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

                // Message d'erreur (mode réel)
                if (erreur != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CouleursApp.MauvaiseAssociation.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                erreur!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
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
                        // Efface l'erreur précédente
                        erreur = null

                        val semis = JeunePlantEntity(
                            legumeNom = legumeChoisi.nom,
                            varieteNom = varieteSelectionnee?.nom,
                            emoji = getEmojiCategorie(legumeChoisi.categorie),
                            quantite = quantiteInt,
                            stade = JeunePlantEtapes.SEMIS, // verrouillé
                            dateSemis = dateSemis,
                            emplacementActuel = emplacementActuel.ifBlank { null },
                            notes = notes.ifBlank { null },
                            estActif = true
                        )

                        when (val resultat = repository.creerSemisAvecMode(context, semis)) {
                            is ResultatCreationSemis.Succes -> {
                                val message = if (resultat.modeReel) {
                                    "Semis créé — ${resultat.grainesDecrementees} graine(s) déduites du stock 🌱"
                                } else {
                                    "Semis créé 🌱"
                                }
                                android.widget.Toast.makeText(
                                    context,
                                    message,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                onValide()
                            }
                            is ResultatCreationSemis.ErreurSachetIntrouvable -> {
                                val variete = resultat.varieteNom?.let { " ($it)" } ?: ""
                                erreur = "Aucun sachet de ${resultat.legumeNom}$variete trouvé.\n" +
                                    "Ajoute-le d'abord dans Stocks > Graines."
                            }
                            is ResultatCreationSemis.ErreurStockInsuffisant -> {
                                erreur = "Stock insuffisant : ${resultat.grainesDisponibles} graine(s) disponible(s), " +
                                    "${resultat.grainesDemandees} demandée(s).\n" +
                                    "Il te manque ${resultat.grainesManquantes} graine(s)."
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Semer")
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
 * Dupliqué depuis DialogAjoutPlant pour éviter les dépendances croisées
 * entre ui/screens/stocks et ui/screens/semis.
 */
fun afficherDatePickerSemis(
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
