@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.theshire.app.data.Outil
import com.theshire.app.data.OutilCategories
import com.theshire.app.data.Outils
import com.theshire.app.data.OutilsApp
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp

/**
 * Onglet "Matériel" de l'écran Stocks.
 * 
 * Contient :
 * - Liste des outils possédés, GROUPÉS PAR CATÉGORIE
 * - FAB d'ajout (sélection multiple depuis la base Outils)
 * - Fiche détaillée d'un outil (tuto, conseil, description)
 * 
 * Les catégories sont affichées comme des sections avec un header visuel.
 */
@Composable
fun OngletMateriel() {
    var outilSelectionne by remember { mutableStateOf<Outil?>(null) }
    
    // Si un outil est sélectionné, afficher sa fiche détaillée en plein écran
    if (outilSelectionne != null) {
        FicheOutil(
            outil = outilSelectionne!!,
            onBack = { outilSelectionne = null }
        )
        return
    }
    
    ListeOutilsParCategorie(onOutilClick = { outil -> outilSelectionne = outil })
}

/**
 * Liste des outils possédés, groupés par catégorie avec sections visuelles.
 */
@Composable
fun ListeOutilsParCategorie(
    onOutilClick: (Outil) -> Unit
) {
    val context = LocalContext.current
    var showAjoutDialog by remember { mutableStateOf(false) }
    
    // Liste observable des IDs possédés
    val outilsPossedesIds = OutilsApp.outilsPossedes.toList()
    
    // Filtrer les outils complets depuis la liste des IDs
    val outilsPossedes = Outils.getOutilsParIds(outilsPossedesIds)
    
    // Grouper par catégorie (dans l'ordre des catégories)
    val outilsGroupes: Map<String, List<Outil>> = remember(outilsPossedesIds) {
        outilsPossedes
            .sortedBy { it.nom }
            .groupBy { it.categorie }
    }
    
    // Nombre total d'outils
    val nombreTotal = outilsPossedes.size
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (outilsPossedes.isEmpty()) {
            // Écran vide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🛠️", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucun outil",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CouleursApp.TexteFonce
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cliquez sur + pour ajouter les outils que vous possédez.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    top = 20.dp,
                    bottom = LayoutConstantes.PADDING_BAS_FAB
                )
            ) {
                // En-tête : nombre total
                item {
                    Text(
                        "$nombreTotal outil(s) dans mon équipement",
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Pour chaque catégorie (dans l'ordre de OutilCategories.TOUTES)
                OutilCategories.TOUTES.forEach { categorie ->
                    val outilsDeLaCategorie = outilsGroupes[categorie]
                    
                    if (!outilsDeLaCategorie.isNullOrEmpty()) {
                        // Header de catégorie
                        item(key = "header_$categorie") {
                            HeaderCategorie(
                                categorie = categorie,
                                nombreOutils = outilsDeLaCategorie.size
                            )
                        }
                        
                        // Outils de la catégorie
                        items(
                            outilsDeLaCategorie,
                            key = { it.id }
                        ) { outil ->
                            CarteOutil(
                                outil = outil,
                                onClick = { onOutilClick(outil) }
                            )
                        }
                        
                        // Espace après la section
                        item(key = "spacer_$categorie") {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
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
            Icon(Icons.Default.Add, contentDescription = "Ajouter un outil")
        }
    }
    
    // Boîte de dialogue d'ajout multiple
    if (showAjoutDialog) {
        AjoutOutilsDialog(
            onDismiss = { showAjoutDialog = false },
            onValider = { idsSelectionnes ->
                idsSelectionnes.forEach { id ->
                    if (!OutilsApp.possede(id)) {
                        OutilsApp.toggle(context, id)
                    }
                }
                showAjoutDialog = false
            }
        )
    }
}

/**
 * Header visuel d'une catégorie (titre + emoji + nombre d'outils).
 */
@Composable
fun HeaderCategorie(
    categorie: String,
    nombreOutils: Int
) {
    val emoji = OutilCategories.getEmoji(categorie)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(CouleursApp.VertPale, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            categorie,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = CouleursApp.VertPrincipal
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            "($nombreOutils)",
            style = MaterialTheme.typography.bodySmall,
            color = CouleursApp.TexteFonce.copy(alpha = 0.5f)
        )
    }
}

/**
 * Boîte de dialogue d'ajout multiple d'outils, avec sections par catégorie.
 */
@Composable
fun AjoutOutilsDialog(
    onDismiss: () -> Unit,
    onValider: (Set<String>) -> Unit
) {
    val tousLesOutils = remember { Outils.getTousLesOutils() }
    val idsPossedes = remember { OutilsApp.outilsPossedes.toSet() }
    
    val selection = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        selection.clear()
        selection.addAll(idsPossedes)
    }
    
    var searchQuery by remember { mutableStateOf("") }
    
    // Filtrer par recherche
    val outilsFiltres = tousLesOutils.filter {
        searchQuery.isEmpty() || it.nom.contains(searchQuery, ignoreCase = true)
    }
    
    // Grouper par catégorie
    val outilsGroupes = outilsFiltres
        .sortedBy { it.nom }
        .groupBy { it.categorie }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ajouter des outils",
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
                    "Cochez les outils que vous possédez.",
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
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                ) {
                    // Si recherche active : affichage simple (pas de sections)
                    if (searchQuery.isNotEmpty()) {
                        items(outilsFiltres, key = { it.id }) { outil ->
                            LigneOutilSelectionnable(
                                outil = outil,
                                estCoche = selection.contains(outil.id),
                                onToggle = {
                                    if (selection.contains(outil.id)) {
                                        selection.remove(outil.id)
                                    } else {
                                        selection.add(outil.id)
                                    }
                                }
                            )
                        }
                    } else {
                        // Sinon : affichage groupé par catégorie
                        OutilCategories.TOUTES.forEach { categorie ->
                            val outilsCat = outilsGroupes[categorie]
                            
                            if (!outilsCat.isNullOrEmpty()) {
                                // Header de section
                                item(key = "header_dialog_$categorie") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            OutilCategories.getEmoji(categorie),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            categorie,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CouleursApp.VertPrincipal
                                        )
                                    }
                                }
                                
                                // Outils de la section
                                items(outilsCat, key = { it.id }) { outil ->
                                    LigneOutilSelectionnable(
                                        outil = outil,
                                        estCoche = selection.contains(outil.id),
                                        onToggle = {
                                            if (selection.contains(outil.id)) {
                                                selection.remove(outil.id)
                                            } else {
                                                selection.add(outil.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onValider(selection.toSet()) },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Valider (${selection.size})")
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
 * Ligne sélectionnable d'un outil dans le dialogue d'ajout.
 */
@Composable
fun LigneOutilSelectionnable(
    outil: Outil,
    estCoche: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = estCoche,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = CouleursApp.VertPrincipal,
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${outil.emoji} ${outil.nom}",
            style = MaterialTheme.typography.bodyLarge,
            color = CouleursApp.TexteFonce
        )
    }
    HorizontalDivider(color = CouleursApp.VertPale)
}

/**
 * Carte d'un outil dans la liste (avec le nom de sa catégorie en sous-titre).
 */
@Composable
fun CarteOutil(
    outil: Outil,
    onClick: () -> Unit
) {
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
            Text(outil.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    outil.nom,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    outil.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
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

/**
 * Fiche détaillée d'un outil : description, tuto, conseil, bouton retirer.
 */
@Composable
fun FicheOutil(
    outil: Outil,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${outil.emoji} ${outil.nom}",
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
                    IconButton(onClick = {
                        OutilsApp.toggle(context, outil.id)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Retirer",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // En-tête : emoji + nom + catégorie + description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(outil.emoji, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            outil.nom,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                OutilCategories.getEmoji(outil.categorie),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                outil.categorie,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            outil.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce
                        )
                    }
                }
            }
            
            // Tuto d'utilisation
            if (outil.tuto.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "📖 Comment l'utiliser",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = CouleursApp.VertPrincipal
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            outil.tuto.forEachIndexed { index, etape ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        "${index + 1}.",
                                        fontWeight = FontWeight.Bold,
                                        color = CouleursApp.VertPrincipal,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(
                                        etape,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Conseil
            if (outil.conseil.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("💡", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Conseil",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    outil.conseil,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            }
            
            // Bouton retirer
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        OutilsApp.toggle(context, outil.id)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retirer de mes outils")
                }
            }
        }
    }
}
