package com.theshire.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
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
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LocalisationRepository
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.PlancheEntity
import com.theshire.app.data.ReseauRepository
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.theme.PotagerShireTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != 
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != 
                PackageManager.PERMISSION_GRANTED) {
                
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    100
                )
            }
        }
        
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
            onNavigateToCalendrier = { currentScreen = "calendrier" },
            onNavigateToConservation = { currentScreen = "conservation" }
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
        "conservation" -> ConservationScreen(
            onBack = { currentScreen = "accueil" }
        )
    }
}

// ============== ACCUEIL ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccueilScreen(
    onNavigateToBibliotheque: () -> Unit,
    onNavigateToJardin: () -> Unit,
    onNavigateToCalendrier: () -> Unit,
    onNavigateToConservation: () -> Unit
) {
    val context = LocalContext.current
    val meteoRepository = remember { MeteoRepository() }
    val localisationRepository = remember { LocalisationRepository(context) }
    
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("") }
    
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val dateDuJour = remember { dateFormat.format(Date()) }
    
    LaunchedEffect(Unit) {
        try {
            val villeDetectee = localisationRepository.getVille()
            if (villeDetectee != null) {
                ville = villeDetectee
            }
            meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" })
        } catch (e: Exception) {
            meteo = null
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToBibliotheque,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Bibliothèque") },
                    label = { Text("Biblio", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToJardin,
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Jardin") },
                    label = { Text("Jardin", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCalendrier,
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendrier") },
                    label = { Text("Calend.", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToConservation,
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = "Conservation") },
                    label = { Text("Conserv.", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
                )
            }
        }
    ) { innerPadding ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getEmojiMeteo(meteo),
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        if (meteo != null) {
                            Text(
                                text = "${meteo!!.temperature}°C",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = meteo!!.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "--°C",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Météo indisponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateDuJour,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (ville.isNotEmpty()) {
                            Text(
                                text = "📍 $ville",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Photo du jardin",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "📸 Photo de mon jardin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prenez une photo de votre potager pour suivre son évolution",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prendre une photo")
                    }
                }
            }
        }
    }
}

fun getEmojiMeteo(meteo: MeteoData?): String {
    if (meteo == null) return "🌤️"
    
    return when {
        meteo.description.contains("pluie", ignoreCase = true) -> "🌧️"
        meteo.description.contains("nuage", ignoreCase = true) -> "☁️"
        meteo.description.contains("soleil", ignoreCase = true) || 
        meteo.description.contains("clair", ignoreCase = true) -> "☀️"
        meteo.description.contains("neige", ignoreCase = true) -> "❄️"
        meteo.description.contains("orage", ignoreCase = true) -> "⛈️"
        meteo.description.contains("brume", ignoreCase = true) -> "🌫️"
        else -> "🌤️"
    }
}

// ============== BIBLIOTHÈQUE ==============
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

// ============== JARDIN ==============
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
    var selectedCaseNumero by remember { mutableStateOf(0) }
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
                        onSousCarreClick = { carre, caseNumero ->
                            selectedCarre = carre
                            selectedCaseNumero = caseNumero
                            showLegumeSelection = true
                        }
                    )
                }
            }
        }
    }
    
    if (showAddPlancheDialog) {
        var nomPlanche by remember { mutableStateOf("") }
        var largeur by remember { mutableStateOf("3") }
        var longueur by remember { mutableStateOf("4") }
        
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Dimensions (largeur × longueur en mètres)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            singleLine = true
                        )
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        OutlinedTextField(
                            value = longueur,
                            onValueChange = { longueur = it },
                            label = { Text("Longueur (m)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalCarres = (largeur.toIntOrNull() ?: 0) * (longueur.toIntOrNull() ?: 0)
                    if (totalCarres > 0) {
                        Text(
                            text = "= $totalCarres carrés d'1m²",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val l = largeur.toIntOrNull() ?: 1
                        val L = longueur.toIntOrNull() ?: 1
                        if (l > 0 && L > 0 && nomPlanche.isNotBlank()) {
                            scope.launch {
                                jardinRepository.ajouterPlanche(nomPlanche, l, L)
                            }
                            showAddPlancheDialog = false
                        }
                    },
                    enabled = nomPlanche.isNotBlank() && 
                              (largeur.toIntOrNull() ?: 0) > 0 && 
                              (longueur.toIntOrNull() ?: 0) > 0
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
    
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!
        val caseNumero = selectedCaseNumero
        
        AlertDialog(
            onDismissRequest = { 
                showLegumeSelection = false
                selectedCarre = null
                selectedCaseNumero = 0
            },
            title = { Text("Que voulez-vous faire ?") },
            text = {
                Column {
                    Text(
                        text = "Case ${caseNumero} du carré",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Choisissez un légume à planter :",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        item {
                            Text(
                                text = "🗑️ Vider la case",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            jardinRepository.modifierCasePrecise(
                                                carre,
                                                caseNumero,
                                                null
                                            )
                                        }
                                        showLegumeSelection = false
                                        selectedCarre = null
                                        selectedCaseNumero = 0
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()
                        }
                        
                        items(legumes) { legume ->
                            Text(
                                text = legume.nom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            jardinRepository.modifierCasePrecise(
                                                carre,
                                                caseNumero,
                                                legume.nom
                                            )
                                        }
                                        showLegumeSelection = false
                                        selectedCarre = null
                                        selectedCaseNumero = 0
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showLegumeSelection = false
                    selectedCarre = null
                    selectedCaseNumero = 0
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
    onSousCarreClick: (CarreEntity, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planche.nom,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onToggleExpand)
                    )
                    Text(
                        text = "${planche.largeur}m × ${planche.longueur}m = ${planche.largeur * planche.longueur} carrés",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (y in 0 until planche.longueur) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (x in 0 until planche.largeur) {
                                val carre = carres.find { it.positionX == x && it.positionY == y }
                                if (carre != null) {
                                    Grille3x3(
                                        carre = carre,
                                        onSousCarreClick = { caseNumero ->
                                            onSousCarreClick(carre, caseNumero)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Grille3x3(
    carre: CarreEntity,
    onSousCarreClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
                    
                    val couleurFond = if (legume != null) {
                        val autresLegumes = listOfNotNull(
                            carre.case1, carre.case2, carre.case3,
                            carre.case4, carre.case5, carre.case6,
                            carre.case7, carre.case8, carre.case9
                        ).filter { it != legume }
                        
                        if (autresLegumes.isEmpty()) {
                            Color(0xFF4CAF50).copy(alpha = 0.3f)
                        } else {
                            val estMauvaise = autresLegumes.any { voisin ->
                                estMauvaiseAssociation(legume, voisin)
                            }
                            val estBonne = autresLegumes.any { voisin ->
                                estBonneAssociation(legume, voisin)
                            }
                            
                            when {
                                estMauvaise -> Color(0xFFF44336).copy(alpha = 0.3f)
                                estBonne -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                else -> Color(0xFFFF9800).copy(alpha = 0.3f)
                            }
                        }
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(couleurFond)
                            .border(1.dp, MaterialTheme.colorScheme.primary)
                            .clickable(onClick = { onSousCarreClick(caseNumero) }),
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

fun estBonneAssociation(legume1: String, legume2: String): Boolean {
    val bonnesAssociations = mapOf(
        "Carotte" to listOf("Tomate", "Salade", "Oignon", "Poireau"),
        "Tomate" to listOf("Carotte", "Basilic", "Oignon"),
        "Salade" to listOf("Carotte", "Radis", "Concombre"),
        "Oignon" to listOf("Carotte", "Tomate", "Betterave"),
        "Poireau" to listOf("Carotte", "Céleri"),
        "Basilic" to listOf("Tomate", "Poivron"),
        "Radis" to listOf("Salade", "Carotte"),
        "Concombre" to listOf("Salade", "Haricot"),
        "Haricot" to listOf("Concombre", "Maïs", "Courge"),
        "Courge" to listOf("Haricot", "Maïs")
    )
    
    return bonnesAssociations[legume1]?.contains(legume2) == true ||
           bonnesAssociations[legume2]?.contains(legume1) == true
}

fun estMauvaiseAssociation(legume1: String, legume2: String): Boolean {
    val mauvaisesAssociations = mapOf(
        "Carotte" to listOf("Aneth", "Persil"),
        "Tomate" to listOf("Pomme de terre", "Concombre"),
        "Pomme de terre" to listOf("Tomate", "Aubergine"),
        "Oignon" to listOf("Haricot", "Pois"),
        "Poireau" to listOf("Haricot", "Pois"),
        "Haricot" to listOf("Ail", "Oignon"),
        "Salade" to listOf("Persil", "Céleri"),
        "Concombre" to listOf("Tomate", "Pomme de terre")
    )
    
    return mauvaisesAssociations[legume1]?.contains(legume2) == true ||
           mauvaisesAssociations[legume2]?.contains(legume1) == true
}

// ============== CALENDRIER ==============
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
    var ville by remember { mutableStateOf("Paris") }
    var estConnecte by remember { mutableStateOf(true) }
    
    var currentMonth by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)) }
    
    LaunchedEffect(Unit) {
        legumeRepository.ajouterLegumesPredefinis()
        isLoading = false
    }
    
    LaunchedEffect(Unit) {
        try {
            legumesPlantes = jardinRepository.getLegumesPlantes()
        } catch (e: Exception) {
            legumesPlantes = emptyList()
        }
    }
    
    LaunchedEffect(Unit) {
        val reseauRepository = ReseauRepository(context)
        estConnecte = reseauRepository.estConnecte()
        
        if (estConnecte) {
            try {
                val localisationRepository = LocalisationRepository(context)
                val villeDetectee = localisationRepository.getVille()
                if (villeDetectee != null) {
                    ville = villeDetectee
                }
                meteo = meteoRepository.getMeteo(ville)
            } catch (e: Exception) {
                meteo = null
            }
        } else {
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
            item {
                MeteoCard(meteo, ville, estConnecte)
            }
            
            item {
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
fun MeteoCard(meteo: MeteoData?, ville: String, estConnecte: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌦️ Météo à $ville",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!estConnecte) {
                Text(
                    text = "📡 Mode hors-ligne - Pas de connexion internet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (meteo == null) {
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

// ============== CONSERVATION ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConservationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { LegumeRepository(context) }
    val legumes by repository.legumes.collectAsState(initial = emptyList())
    
    var filtre by remember { mutableStateOf("Tous") }
    
    LaunchedEffect(Unit) {
        repository.ajouterLegumesPredefinis()
    }
    
    val methodes = listOf("Tous", "Séchage", "Lactofermentation", "Conserves", "Congélation")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Conservation 🥫", fontWeight = FontWeight.Bold)
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
                    text = "Filtrer par méthode :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    methodes.forEach { methode ->
                        FilterChip(
                            selected = filtre == methode,
                            onClick = { filtre = methode },
                            label = { Text(methode) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${legumes.filter { legume -> 
                        if (filtre == "Tous") true else legume.conservation.contains(filtre, ignoreCase = true)
                    }.size} légumes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            legumes.filter { legume ->
                if (filtre == "Tous") true else legume.conservation.contains(filtre, ignoreCase = true)
            }.forEach { legume ->
                item {
                    ConservationCard(legume)
                }
            }
        }
    }
}

@Composable
fun ConservationCard(legume: LegumeEntity) {
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
                text = legume.conservation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ============== FICHE DÉTAILLÉE ==============
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
            item { InfoCard("Conservation", legume.conservation) }
        }
    }
}

// ============== CARTE LÉGUME ==============
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

// ============== CARTE INFO ==============
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
