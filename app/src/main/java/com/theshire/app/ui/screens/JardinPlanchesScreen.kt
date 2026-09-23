package com.theshire.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.AvertissementRotation
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.CultureEntity
import com.theshire.app.data.CultureRepository
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.ModePreferences
import com.theshire.app.data.NiveauRisque
import com.theshire.app.data.PlancheEntity
import com.theshire.app.data.ResultatPlantation
import com.theshire.app.data.getEmojiCategorie
import com.theshire.app.data.getDistanceEntrePlants
import com.theshire.app.data.JardinRepository
import com.theshire.app.data.LegumeRepository
import com.theshire.app.data.VarieteRepository
import com.theshire.app.ui.components.DialogErreurPlantation
import com.theshire.app.ui.components.Grille3x3
import com.theshire.app.ui.components.LegendeCouleurs
import com.theshire.app.ui.components.VarieteSelectionDialog
import com.theshire.app.ui.components.calculerCouleursCarre
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.screens.jardin.ChoixPlantation
import com.theshire.app.ui.screens.jardin.DialogPlanterCulture
import com.theshire.app.ui.screens.jardin.FicheCulture
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinPlanchesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val cultureRepository = remember { CultureRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val varieteRepository = remember { VarieteRepository(context) }
    val planches by jardinRepository.planches.collectAsState(initial = emptyList())
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddPlancheDialog by remember { mutableStateOf(false) }
    var expandedPlancheId by remember { mutableStateOf<Long?>(null) }
    var selectedCarre by remember { mutableStateOf<CarreEntity?>(null) }
    var selectedCaseNumero by remember { mutableStateOf(0) }
    var selectedLegumeNom by remember { mutableStateOf<String?>(null) }
    var selectedLegumeEmoji by remember { mutableStateOf("🌱") }
    var selectedVarieteNom by remember { mutableStateOf<String?>(null) }
    var showLegumeSelection by remember { mutableStateOf(false) }
    var showVarieteSelection by remember { mutableStateOf(false) }
    var showChoixRemplissage by remember { mutableStateOf(false) }
    var showChoixSourceStock by remember { mutableStateOf(false) }
    var remplirM2Mode by remember { mutableStateOf(false) }
    var avertissement by remember { mutableStateOf<AvertissementRotation?>(null) }
    var showAvertissement by remember { mutableStateOf(false) }
    var currentPlancheId by remember { mutableStateOf<Long>(0L) }
    var erreurPlantation by remember { mutableStateOf<ResultatPlantation?>(null) }
    var cultureSelectionnee by remember { mutableStateOf<CultureEntity?>(null) }

    LaunchedEffect(Unit) {
        legumeRepository.ajouterLegumesPredefinis()
        varieteRepository.ajouterVarietesPredefinies()
    }

    // Si une culture est sélectionnée → afficher sa fiche
    if (cultureSelectionnee != null) {
        FicheCulture(
            culture = cultureSelectionnee!!,
            repository = cultureRepository,
            onBack = { cultureSelectionnee = null }
        )
        return
    }

    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mon Jardin 🏡", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
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
                onClick = { showAddPlancheDialog = true },
                containerColor = CouleursApp.VertClair,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = LayoutConstantes.PADDING_BAS_FAB - 16.dp)
            ) {
                Icon(Icons.Default.Add, "Ajouter")
            }
        }
    ) { innerPadding ->
        if (planches.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🏡", style = MaterialTheme.typography.displayLarge)
                Text("Aucune planche", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                Text("Cliquez sur + pour créer", color = CouleursApp.TexteFonce)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { LegendeCouleurs() }
                items(planches, key = { it.id }) { planche ->
                    PlancheCard(
                        planche = planche,
                        isExpanded = expandedPlancheId == planche.id,
                        onToggleExpand = {
                            expandedPlancheId = if (expandedPlancheId == planche.id) null else planche.id
                        },
                        onDelete = { scope.launch { jardinRepository.supprimerPlanche(planche) } },
                        jardinRepository = jardinRepository,
                        cultureRepository = cultureRepository,
                        legumes = legumes,
                        onSousCarreClick = { carre, caseNumero ->
                            selectedCarre = carre
                            selectedCaseNumero = caseNumero
                            currentPlancheId = planche.id
                            
                            scope.launch {
                                val cultureActive = cultureRepository.getCultureActiveDansCasePourMode(
                                    context = context,
                                    carreId = carre.id,
                                    caseNumero = caseNumero
                                )
                                if (cultureActive != null) {
                                    cultureSelectionnee = cultureActive
                                } else {
                                    if (caseNumero == 5) {
                                        showChoixRemplissage = true
                                    } else {
                                        remplirM2Mode = false
                                        showLegumeSelection = true
                                    }
                                }
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(LayoutConstantes.PADDING_BAS_FAB))
                }
            }
        }
    }

    // ============================================================
    // Dialog : case centrale → choix entre tout le m² ou une seule case
    // ============================================================
    if (showChoixRemplissage && selectedCarre != null) {
        AlertDialog(
            onDismissRequest = { showChoixRemplissage = false },
            title = { Text("Case centrale", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Que souhaitez-vous faire avec cette case centrale ?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            remplirM2Mode = true
                            showChoixRemplissage = false
                            showLegumeSelection = true
                        },
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌱", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Remplir tout le m²",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal
                                )
                                Text(
                                    "Les 9 cases avec la même plante",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            remplirM2Mode = false
                            showChoixRemplissage = false
                            showLegumeSelection = true
                        },
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌿", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Remplir juste cette case",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text(
                                    "Seulement la case centrale",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChoixRemplissage = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }

    // ============================================================
    // Dialog : création d'une nouvelle planche
    // ============================================================
    if (showAddPlancheDialog) {
        var nom by remember { mutableStateOf("") }
        var largeur by remember { mutableStateOf("3") }
        var longueur by remember { mutableStateOf("4") }
        AlertDialog(
            onDismissRequest = { showAddPlancheDialog = false },
            title = { Text("Nouvelle planche", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom de la planche") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = largeur,
                            onValueChange = { largeur = it },
                            label = { Text("Largeur (m)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Text("×", style = MaterialTheme.typography.headlineMedium)
                        OutlinedTextField(
                            value = longueur,
                            onValueChange = { longueur = it },
                            label = { Text("Longueur (m)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💡 Les distances de plantation sont automatiquement respectées selon la densité de chaque plante.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.VertPrincipal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val l = largeur.toIntOrNull() ?: 1
                        val L = longueur.toIntOrNull() ?: 1
                        if (l > 0 && L > 0 && nom.isNotBlank()) {
                            scope.launch { jardinRepository.ajouterPlanche(nom, l, L) }
                            showAddPlancheDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlancheDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // ============================================================
    // Dialog : sélection d'un légume
    // ============================================================
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!
        val caseNumero = selectedCaseNumero
        var selectedCategorie by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val categories = legumes.groupBy { it.categorie }.keys.toList()

        AlertDialog(
            onDismissRequest = { showLegumeSelection = false },
            title = {
                Text(
                    if (remplirM2Mode) "Remplir le m² entier" else "Choisissez une plante",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Case $caseNumero du carré",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (!remplirM2Mode) {
                        Text(
                            "🗑️ Vider la case",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        val res = jardinRepository.modifierCasePrecise(
                                            context = context,
                                            carre = carre,
                                            caseNumero = caseNumero,
                                            legumeNom = null,
                                            plancheId = currentPlancheId
                                        )
                                        if (res !is ResultatPlantation.Succes) {
                                            erreurPlantation = res
                                        }
                                    }
                                    showLegumeSelection = false
                                }
                                .padding(12.dp),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("🔍 Rechercher...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    if (searchQuery.isEmpty() && selectedCategorie == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            categories.forEach { c ->
                                FilterChip(
                                    selected = selectedCategorie == c,
                                    onClick = {
                                        selectedCategorie = if (selectedCategorie == c) null else c
                                    },
                                    label = {
                                        Text(
                                            "${getEmojiCategorie(c)} $c",
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                    if (searchQuery.isNotEmpty() || selectedCategorie != null) {
                        val plantes = legumes.filter {
                            (searchQuery.isEmpty() || it.nom.contains(searchQuery, true)) &&
                                (selectedCategorie == null || it.categorie == selectedCategorie)
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                            items(plantes) { legume ->
                                Text(
                                    text = "${legume.nom} (${getDistanceEntrePlants(legume)} cm)",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                if (!jardinRepository.peutPlanterDansCase(
                                                        carre, caseNumero, legume.nom
                                                    )
                                                ) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "${legume.nom} est trop volumineux pour cette zone",
                                                        android.widget.Toast.LENGTH_LONG
                                                    ).show()
                                                    showLegumeSelection = false
                                                } else {
                                                    val associations =
                                                        jardinRepository.verifierAssociationsAdjacentes(
                                                            carre, caseNumero, legume.nom
                                                        )
                                                    val mauvaiseAssoc =
                                                        associations.filter { it.second == "mauvaise" }
                                                    if (mauvaiseAssoc.isNotEmpty()) {
                                                        selectedLegumeNom = legume.nom
                                                        selectedLegumeEmoji = getEmojiCategorie(legume.categorie)
                                                        avertissement = AvertissementRotation(
                                                            niveau = NiveauRisque.MOYEN,
                                                            message = "⚠️ Mauvaise association avec : ${mauvaiseAssoc.joinToString(", ") { it.first }}"
                                                        )
                                                        showAvertissement = true
                                                        showLegumeSelection = false
                                                    } else {
                                                        selectedLegumeNom = legume.nom
                                                        selectedLegumeEmoji = getEmojiCategorie(legume.categorie)
                                                        showVarieteSelection = true
                                                        showLegumeSelection = false
                                                    }
                                                }
                                            }
                                        }
                                        .padding(14.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CouleursApp.TexteFonce
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLegumeSelection = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // ============================================================
    // Dialog : sélection d'une variété
    // ============================================================
    if (showVarieteSelection && selectedLegumeNom != null && selectedCarre != null) {
        VarieteSelectionDialog(
            legumeNom = selectedLegumeNom!!,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
                val variete = if (nomComplet.contains("(")) {
                    nomComplet.substringAfter("(").substringBefore(")").trim()
                } else {
                    null
                }
                selectedVarieteNom = variete
                showVarieteSelection = false
                showChoixSourceStock = true
            },
            onDismiss = {
                showVarieteSelection = false
                selectedLegumeNom = null
                selectedLegumeEmoji = "🌱"
                selectedVarieteNom = null
                selectedCarre = null
                selectedCaseNumero = 0
                remplirM2Mode = false
            }
        )
    }

    // ============================================================
    // Dialog : choix de la source du stock
    // ============================================================
    if (showChoixSourceStock && selectedLegumeNom != null && selectedCarre != null) {
        DialogPlanterCulture(
            legumeNom = selectedLegumeNom!!,
            emoji = selectedLegumeEmoji,
            varieteDejaChoisie = selectedVarieteNom,
            onDismiss = {
                showChoixSourceStock = false
                selectedLegumeNom = null
                selectedLegumeEmoji = "🌱"
                selectedVarieteNom = null
                selectedCarre = null
                selectedCaseNumero = 0
                remplirM2Mode = false
            },
            onValider = { choix ->
                val carre = selectedCarre!!
                val caseNumero = selectedCaseNumero
                val modeM2 = remplirM2Mode
                val plancheId = currentPlancheId
                
                showChoixSourceStock = false
                
                scope.launch {
                    val resultat: ResultatPlantation
                    if (modeM2) {
                        resultat = jardinRepository.remplirM2Entier(
                            context = context,
                            carre = carre,
                            legumeNom = choix.legumeNom,
                            varieteNom = choix.varieteNom,
                            emoji = choix.emoji,
                            plancheId = plancheId,
                            sourceStock = choix.sourceStock,
                            sourceStockId = choix.sourceStockId
                        )
                    } else {
                        resultat = jardinRepository.modifierCasePrecise(
                            context = context,
                            carre = carre,
                            caseNumero = caseNumero,
                            legumeNom = choix.legumeNom,
                            varieteNom = choix.varieteNom,
                            emoji = choix.emoji,
                            plancheId = plancheId,
                            sourceStock = choix.sourceStock,
                            sourceStockId = choix.sourceStockId
                        )
                    }
                    
                    if (resultat !is ResultatPlantation.Succes) {
                        erreurPlantation = resultat
                    }
                }
                
                selectedLegumeNom = null
                selectedLegumeEmoji = "🌱"
                selectedVarieteNom = null
                selectedCarre = null
                selectedCaseNumero = 0
                remplirM2Mode = false
            }
        )
    }

    // ============================================================
    // Dialog : avertissement d'association
    // ============================================================
    if (showAvertissement && avertissement != null) {
        val av = avertissement!!
        AlertDialog(
            onDismissRequest = {
                showAvertissement = false
                avertissement = null
                selectedLegumeNom = null
                selectedLegumeEmoji = "🌱"
                selectedCarre = null
                selectedCaseNumero = 0
            },
            title = { Text("Avertissement", fontWeight = FontWeight.Bold) },
            text = { Text(av.message) },
            confirmButton = {
                Button(
                    onClick = {
                        showAvertissement = false
                        avertissement = null
                        showVarieteSelection = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Continuer")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAvertissement = false
                    avertissement = null
                    selectedLegumeNom = null
                    selectedLegumeEmoji = "🌱"
                    selectedCarre = null
                    selectedCaseNumero = 0
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    // ============================================================
    // Dialog : erreur de plantation
    // ============================================================
    if (erreurPlantation != null) {
        DialogErreurPlantation(
            resultat = erreurPlantation!!,
            onDismiss = { erreurPlantation = null }
        )
    }
}

/**
 * Carte d'une planche avec grille 3x3.
 * 
 * ⚠️ NOUVEAU : limitation du pan (déplacement) pour ne pas perdre la planche
 * de vue. Le zoom reste libre. La limite est calculée en fonction de la taille
 * de l'écran + une marge (2 petits carrés autour).
 */
@Composable
fun PlancheCard(
    planche: PlancheEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    jardinRepository: JardinRepository,
    cultureRepository: CultureRepository,
    legumes: List<LegumeEntity>,
    onSousCarreClick: (CarreEntity, Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    var carresFiltres by remember { mutableStateOf<List<CarreEntity>>(emptyList()) }
    
    val carres by jardinRepository.getCarresForPlanche(planche.id)
        .collectAsState(initial = emptyList())
    
    LaunchedEffect(carres, planche.id) {
        val modeReel = ModePreferences.estModeReel(context)
        val estProjectionAttendue = !modeReel
        
        carresFiltres = carres.map { carre ->
            val culturesMode = try {
                com.theshire.app.data.AppDatabase.getDatabase(context)
                    .cultureDao()
                    .getCulturesActivesPourCarreParMode(carre.id, estProjectionAttendue)
            } catch (e: Exception) {
                emptyList()
            }
            
            val casesActives = culturesMode.mapNotNull { it.caseNumero }.toSet()
            
            carre.copy(
                case1 = if (casesActives.contains(1)) carre.case1 else null,
                case2 = if (casesActives.contains(2)) carre.case2 else null,
                case3 = if (casesActives.contains(3)) carre.case3 else null,
                case4 = if (casesActives.contains(4)) carre.case4 else null,
                case5 = if (casesActives.contains(5)) carre.case5 else null,
                case6 = if (casesActives.contains(6)) carre.case6 else null,
                case7 = if (casesActives.contains(7)) carre.case7 else null,
                case8 = if (casesActives.contains(8)) carre.case8 else null,
                case9 = if (casesActives.contains(9)) carre.case9 else null
            )
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        planche.nom,
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.TexteFonce
                    )
                    Text(
                        "${planche.largeur}m × ${planche.longueur}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                
                // ⚠️ NOUVEAU : marge de déplacement autorisée
                // On autorise un déplacement maximal de la taille de l'écran × 0.4
                // (environ 2 carrés autour de la planche selon la densité de la grille)
                val maxOffsetX = (configuration.screenWidthDp * 0.4f).dp.value
                val maxOffsetY = (configuration.screenHeightDp * 0.3f).dp.value
                
                val state = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(0.5f, 5f)
                    
                    // Calcul de l'offset avec limite
                    val nouvelOffsetX = (offset.x + panChange.x)
                        .coerceIn(-maxOffsetX, maxOffsetX)
                    val nouvelOffsetY = (offset.y + panChange.y)
                        .coerceIn(-maxOffsetY, maxOffsetY)
                    
                    offset = Offset(nouvelOffsetX, nouvelOffsetY)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 600.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CouleursApp.VertPale.copy(alpha = 0.3f))
                        .transformable(state = state)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y,
                                transformOrigin = TransformOrigin.Center
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (y in 0 until planche.longueur) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (x in 0 until planche.largeur) {
                                    val carre = carresFiltres.find {
                                        it.positionX == x && it.positionY == y
                                    }
                                    if (carre != null) {
                                        val couleurs = calculerCouleursCarre(
                                            carre = carre,
                                            planche = planche,
                                            tousLesCarres = carresFiltres,
                                            legumes = legumes
                                        )
                                        Grille3x3(
                                            carre = carre,
                                            couleurs = couleurs,
                                            onSousCarreClick = { case -> onSousCarreClick(carre, case) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (scale != 1f || offset != Offset.Zero) {
                        SmallFloatingActionButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            containerColor = CouleursApp.VertPrincipal,
                            contentColor = Color.White,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) {
                            Text("↺", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🔍 Pincez à deux doigts pour zoomer",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
