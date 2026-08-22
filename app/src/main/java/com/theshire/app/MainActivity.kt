package com.theshire.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.PlancheEntity
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.theme.PotagerShireTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PotagerShireTheme {
                MainScreen()
            }
        }
    }
}

// Écran principal avec navigation
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("accueil") }
    
    when (currentScreen) {
        "accueil" -> AccueilScreen(
            onNavigateToBibliotheque = { currentScreen = "bibliotheque" },
            onNavigateToJardin = { currentScreen = "jardin" },
            onNavigateToCalendrier = { currentScreen = "calendrier" }
        )
        "bibliotheque" -> BibliothequeScreen(
            onBack = { currentScreen = "accueil" }
        )
        "jardin" -> JardinScreen(
            onBack = { currentScreen = "accueil" }
        )
        "calendrier" -> CalendrierScreen(
            onBack = { currentScreen = "accueil" }
        )
    }
}

// Page d'accueil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccueilScreen(
    onNavigateToBibliotheque: () -> Unit,
    onNavigateToJardin: () -> Unit,
    onNavigateToCalendrier: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Potager de la Comté 🌱", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌱",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bienvenue dans votre potager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            MenuButton(
                icon = Icons.Default.MenuBook,
                titre = "Bibliothèque",
                description = "Fiches de culture des légumes",
                onClick = onNavigateToBibliotheque
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MenuButton(
                icon = Icons.Default.GridView,
                titre = "Mon Jardin",
                description = "Grilles de 1m² en 9 carrés",
                onClick = onNavigateToJardin
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MenuButton(
                icon = Icons.Default.CalendarMonth,
                titre = "Calendrier",
                description = "Opérations culturales et météo",
                onClick = onNavigateToCalendrier
            )
        }
    }
}

@Composable
fun MenuButton(
    icon: ImageVector,
    titre: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = titre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Écran Bibliothèque
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { LegumeRepository(context) }
    val legumes by repository.legumes.collectAsState(initial = emptyList())
    var selectedLegume by remember { mutableStateOf<LegumeEntity?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        repository.ajouterLegumesPredefinis()
    }
    
    if (selectedLegume != null) {
        LegumeDetailScreen(
            legume = selectedLegume!!,
            onBack = { selectedLegume = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("Bibliothèque 📚", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${legumes.size} légumes dans votre bibliothèque",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(legumes, key = { it.id }) { legume ->
                    LegumeCard(
                        legume = legume,
                        onClick = { selectedLegume = legume },
                        onDelete = { 
                            scope.launch { 
                                repository.supprimerLegume(legume) 
                            }
                        }
                    )
                }
            }
        }
    }
}

// Écran Jardin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val planches by jardinRepository.planches.collectAsState(initial = emptyList())
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var showAddPlancheDialog by remember { mutableStateOf(false) }
    var expandedPlancheId by remember { mutableStateOf<Long?>(null) }
    var selectedCarre by remember { mutableStateOf<CarreEntity?>(null) }
    var showLegumeSelection by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        legumeRepository.ajouterLegumesPredefinis()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Mon Jardin 🏡", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPlancheDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter une planche",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    ) { innerPadding ->
        
        if (planches.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏡",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aucune planche de culture",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cliquez sur + pour créer votre première planche",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(planches, key = { it.id }) { planche ->
                    PlancheCard(
                        planche = planche,
                        isExpanded = expandedPlancheId == planche.id,
                        onToggleExpand = {
                            expandedPlancheId = if (expandedPlancheId == planche.id) null else planche.id
                        },
                        onDelete = {
                            scope.launch {
                                jardinRepository.supprimerPlanche(planche)
                            }
                        },
                        jardinRepository = jardinRepository,
                        onCarreClick = { carre ->
                            selectedCarre = carre
                            showLegumeSelection = true
                        }
                    )
                }
            }
        }
    }
    
    // Dialogue pour ajouter une planche
    if (showAddPlancheDialog) {
        var nomPlanche by remember { mutableStateOf("") }
        var nombreCarres by remember { mutableStateOf("1") }
        
        AlertDialog(
            onDismissRequest = { showAddPlancheDialog = false },
            title = { Text("Nouvelle planche") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomPlanche,
                        onValueChange = { nomPlanche = it },
                        label = { Text("Nom de la planche") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombreCarres,
                        onValueChange = { nombreCarres = it },
                        label = { Text("Nombre de carrés d'1m²") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nbCarres = nombreCarres.toIntOrNull() ?: 1
                        if (nbCarres > 0 && nomPlanche.isNotBlank()) {
                            scope.launch {
                                jardinRepository.ajouterPlanche(nomPlanche, nbCarres)
                            }
                            showAddPlancheDialog = false
                        }
                    },
                    enabled = nomPlanche.isNotBlank() && (nombreCarres.toIntOrNull() ?: 0) > 0
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
    
    // Dialogue pour choisir un légume
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!
        AlertDialog(
            onDismissRequest = { 
                showLegumeSelection = false
                selectedCarre = null
            },
            title = { Text("Choisir un légume") },
            text = {
                LazyColumn {
                    items(legumes) { legume ->
                        Text(
                            text = legume.nom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val caseVide = getPremiereCaseVide(carre)
                                    if (caseVide > 0) {
                                        scope.launch {
                                            jardinRepository.modifierCase(
                                                carre,
                                                caseVide,
                                                legume.nom
                                            )
                                        }
                                    }
                                    showLegumeSelection = false
                                    selectedCarre = null
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showLegumeSelection = false
                    selectedCarre = null
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}

fun getPremiereCaseVide(carre: CarreEntity): Int {
    if (carre.case1 == null) return 1
    if (carre.case2 == null) return 2
    if (carre.case3 == null) return 3
    if (carre.case4 == null) return 4
    if (carre.case5 == null) return 5
    if (carre.case6 == null) return 6
    if (carre.case7 == null) return 7
    if (carre.case8 == null) return 8
    if (carre.case9 == null) return 9
    return 0
}

@Composable
fun PlancheCard(
    planche: PlancheEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    jardinRepository: JardinRepository,
    onCarreClick: (CarreEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planche.nom,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onToggleExpand)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer la planche",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val carres by jardinRepository.getCarresForPlanche(planche.id).collectAsState(initial = emptyList())
                
                carres.forEach { carre ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Carré ${carre.position}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Grille3x3(
                        carre = carre,
                        onClick = { onCarreClick(carre) }
                    )
                }
            }
        }
    }
}

@Composable
fun Grille3x3(
    carre: CarreEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        for (row in 0..2) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..2) {
                    val caseNumero = row * 3 + col + 1
                    val legume = when (caseNumero) {
                        1 -> carre.case1
                        2 -> carre.case2
                        3 -> carre.case3
                        4 -> carre.case4
                        5 -> carre.case5
                        6 -> carre.case6
                        7 -> carre.case7
                        8 -> carre.case8
                        9 -> carre.case9
                        else -> null
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (legume != null) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = legume ?: "",
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Écran Calendrier avec vue mensuelle et météo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendrierScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val meteoRepository = remember { MeteoRepository() }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    
    var legumesPlantes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    
    // État du calendrier
    var currentMonth by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)) }
    
    LaunchedEffect(Unit) {
        legumeRepository.ajouterLegumesPredefinis()
        isLoading = false
    }
    
    // Récupérer les légumes plantés
    LaunchedEffect(Unit) {
        try {
            legumesPlantes = jardinRepository.getLegumesPlantes()
        } catch (e: Exception) {
            legumesPlantes = emptyList()
        }
    }
    
    // Récupérer la météo
    LaunchedEffect(Unit) {
        try {
            meteo = meteoRepository.getMeteo()
        } catch (e: Exception) {
            meteo = null
        }
    }
    
    val moisNoms = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Calendrier 📅", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Météo
            item {
                MeteoCard(meteo)
            }
            
            // Section Calendrier
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Navigation des mois
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear--
                                } else {
                                    currentMonth--
                                }
                            }) {
                                Text("◀")
                            }
                            Text(
                                text = "${moisNoms[currentMonth]} $currentYear",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = {
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear++
                                } else {
                                    currentMonth++
                                }
                            }) {
                                Text("▶")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Jours de la semaine
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { jour ->
                                Text(
                                    text = jour,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Jours du mois
                        val cal = java.util.Calendar.getInstance()
                        cal.set(currentYear, currentMonth, 1)
                        val firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        val offset = if (firstDayOfWeek == java.util.Calendar.SUNDAY) 6 else firstDayOfWeek - 2
                        
                        val totalCells = offset + daysInMonth
                        val rows = (totalCells + 6) / 7
                        
                        for (row in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    val dayNumber = row * 7 + col - offset + 1
                                    if (dayNumber in 1..daysInMonth) {
                                        val isSelected = dayNumber == selectedDay
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable { selectedDay = dayNumber }
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Section Légumes plantés
            item {
                Text(
                    text = "Légumes plantés (${legumesPlantes.size}) :",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (legumesPlantes.isEmpty()) {
                item {
                    Text(
                        text = "Aucun légume planté. Ajoutez des légumes dans votre jardin !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                legumesPlantes.forEach { legumeNom ->
                    val legume = legumes.find { it.nom == legumeNom }
                    if (legume != null) {
                        item {
                            CalendrierLegumeCard(legume)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeteoCard(meteo: MeteoData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌦️ Météo actuelle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (meteo == null) {
                Text(
                    text = "Impossible de récupérer la météo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "🌡️ Température : ${meteo.temperature}°C",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "☁️ Conditions : ${meteo.description}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💧 Humidité : ${meteo.humidite}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🌬️ Vent : ${meteo.vent} m/s",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CalendrierLegumeCard(legume: LegumeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = legume.nom,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📅 Semis : ${legume.semis}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🌱 Plantation : ${legume.plantation}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🧺 Récolte : ${legume.recolte}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💧 Arrosage : ${legume.arrosage}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "✂️ Entretien : ${legume.entretien}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Fiche détaillée d'un légume
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegumeDetailScreen(
    legume: LegumeEntity,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(legume.nom, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InfoCard("Catégorie", legume.categorie) }
            item { InfoCard("Difficulté", legume.difficulte) }
            item { InfoCard("Exposition", legume.exposition) }
            item { InfoCard("Sol", legume.sol) }
            item { InfoCard("Arrosage", legume.arrosage) }
            item { InfoCard("Température", legume.temperature) }
            item { InfoCard("Semis", legume.semis) }
            item { InfoCard("Plantation", legume.plantation) }
            item { InfoCard("Récolte", legume.recolte) }
            item { InfoCard("Entretien", legume.entretien) }
            item { InfoCard("Maladies", legume.maladies) }
            item { InfoCard("Prévention naturelle", legume.prevention) }
            item { InfoCard("Bonnes associations", legume.bonnesAssociations) }
            item { InfoCard("Mauvaises associations", legume.mauvaisesAssociations) }
        }
    }
}

// Carte d'un légume dans la liste
@Composable
fun LegumeCard(
    legume: LegumeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = legume.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Catégorie : ${legume.categorie}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Difficulté : ${legume.difficulte}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Carte d'information dans la fiche détaillée
@Composable
fun InfoCard(titre: String, contenu: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contenu,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
