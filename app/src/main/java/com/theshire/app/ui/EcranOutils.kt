@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui

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
import androidx.compose.ui.unit.dp
import com.theshire.app.data.OutilsApp
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran Équipement : gestion des outils de jardinage.
 * 
 * Deux onglets :
 * - 🔧 Mes outils : liste des outils possédés + ajout via FAB
 * - 🛒 Store : à venir (achat d'outils manquants)
 * 
 * L'utilisateur peut :
 * - Ajouter des outils via le FAB + (sélection multiple)
 * - Consulter le tuto d'un outil au clic
 * - Retirer un outil depuis sa fiche détaillée (icône 🗑️)
 */
@Composable
fun EcranOutils(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("mes_outils") }
    var outilSelectionne by remember { mutableStateOf<Outil?>(null) }
    
    // Si un outil est sélectionné, afficher sa fiche détaillée en plein écran
    if (outilSelectionne != null) {
        FicheOutil(
            outil = outilSelectionne!!,
            onBack = { outilSelectionne = null }
        )
        return
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Barre d'onglets
        TabRow(
            selectedTabIndex = if (selectedOnglet == "mes_outils") 0 else 1,
            containerColor = CouleursApp.VertPrincipal,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedOnglet == "mes_outils",
                onClick = { selectedOnglet = "mes_outils" },
                text = {
                    Text(
                        "🔧 Mes outils",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "mes_outils") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "store",
                onClick = { selectedOnglet = "store" },
                text = {
                    Text(
                        "🛒 Store",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "store") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
        }
        
        // Contenu de l'onglet actif
        if (selectedOnglet == "mes_outils") {
            MesOutilsScreen(
                onBack = onBack,
                onOutilClick = { outil -> outilSelectionne = outil }
            )
        } else {
            StoreScreen(onBack = onBack)
        }
    }
}

/**
 * Onglet "Mes outils" : liste des outils possédés + FAB d'ajout.
 */
@Composable
fun MesOutilsScreen(
    onBack: () -> Unit,
    onOutilClick: (Outil) -> Unit
) {
    val context = LocalContext.current
    var showAjoutDialog by remember { mutableStateOf(false) }
    
    // Liste observable des IDs possédés
    val outilsPossedesIds = OutilsApp.outilsPossedes.toList()
    
    // Filtrer les outils complets depuis la liste des IDs
    val outilsPossedes = Outils.getOutilsParIds(outilsPossedesIds)
        .sortedBy { it.nom }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mes outils",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAjoutDialog = true },
                containerColor = CouleursApp.VertClair,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un outil")
            }
        }
    ) { innerPadding ->
        if (outilsPossedes.isEmpty()) {
            // Écran vide
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${outilsPossedes.size} outil(s) dans mon équipement",
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(outilsPossedes, key = { it.id }) { outil ->
                    CarteOutil(
                        outil = outil,
                        onClick = { onOutilClick(outil) }
                    )
                }
            }
        }
    }
    
    // Boîte de dialogue d'ajout multiple
    if (showAjoutDialog) {
        AjoutOutilsDialog(
            onDismiss = { showAjoutDialog = false },
            onValider = { idsSelectionnes ->
                // Ajouter tous les outils sélectionnés
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
 * Boîte de dialogue d'ajout multiple d'outils.
 * L'utilisateur coche les outils qu'il possède puis valide.
 */
@Composable
fun AjoutOutilsDialog(
    onDismiss: () -> Unit,
    onValider: (Set<String>) -> Unit
) {
    val tousLesOutils = remember { Outils.getTousLesOutils().sortedBy { it.nom } }
    val idsPossedes = remember { OutilsApp.outilsPossedes.toSet() }
    
    // Sélection locale (avant validation)
    val selection = remember { mutableStateListOf<String>() }
    // Pré-cocher ceux déjà possédés
    LaunchedEffect(Unit) {
        selection.clear()
        selection.addAll(idsPossedes)
    }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val outilsFiltres = tousLesOutils.filter {
        searchQuery.isEmpty() || it.nom.contains(searchQuery, ignoreCase = true)
    }
    
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
                    items(outilsFiltres, key = { it.id }) { outil ->
                        val estCoche = selection.contains(outil.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (estCoche) {
                                        selection.remove(outil.id)
                                    } else {
                                        selection.add(outil.id)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = estCoche,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selection.contains(outil.id)) selection.add(outil.id)
                                    } else {
                                        selection.remove(outil.id)
                                    }
                                },
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
 * Carte d'un outil dans la liste.
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
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
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
            // En-tête : emoji + nom + description
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

/**
 * Onglet "Store" : à venir.
 */
@Composable
fun StoreScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Store 🛒",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🛒", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Store à venir",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.TexteFonce
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Vous retrouverez ici les outils manquants et les produits recommandés par votre jardinerie.",
                style = MaterialTheme.typography.bodyMedium,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
