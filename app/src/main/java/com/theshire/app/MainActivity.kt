package com.theshire.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import com.theshire.app.data.AdventiceEntity
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LocalisationRepository
import com.theshire.app.data.LuneRepository
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.PhaseLune
import com.theshire.app.data.PlancheEntity
import com.theshire.app.data.PrevisionJour
import com.theshire.app.data.ReseauRepository
import com.theshire.app.data.VarieteEntity
import com.theshire.app.ui.AdventiceRepository
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.VarieteRepository
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

object ImageLoaderProvider {
    fun getImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}

@Composable
fun MainScreen() {
    var currentScreen by rememberSaveable { mutableStateOf("accueil") }
    
    val screens = listOf("accueil", "bibliotheque", "jardin", "calendrier", "conservation")
    
    fun goToNext() {
        val currentIndex = screens.indexOf(currentScreen)
        if (currentIndex < screens.size - 1) {
            currentScreen = screens[currentIndex + 1]
        }
    }
    
    fun goToPrevious() {
        val currentIndex = screens.indexOf(currentScreen)
        if (currentIndex > 0) {
            currentScreen = screens[currentIndex - 1]
        }
    }
    
    var dragOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 200f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentScreen) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -swipeThreshold) {
                            goToNext()
                        } else if (dragOffset > swipeThreshold) {
                            goToPrevious()
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
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
        
        PageIndicator(
            currentPage = screens.indexOf(currentScreen),
            totalPages = screens.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "←",
            style = MaterialTheme.typography.bodySmall,
            color = if (currentPage > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 12.dp else 8.dp)
                    .background(
                        color = if (index == currentPage) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "→",
            style = MaterialTheme.typography.bodySmall,
            color = if (currentPage < totalPages - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
    val luneRepository = remember { LuneRepository() }
    
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showPrevisions by remember { mutableStateOf(false) }
    var previsions by remember { mutableStateOf<List<PrevisionJour>>(emptyList()) }
    var isLoadingPrevisions by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val dateDuJour = remember { dateFormat.format(Date()) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val bytes = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bytes)
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Photo_Jardin",
                null
            )
            imageUri = Uri.parse(path)
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }
    
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
    
    LaunchedEffect(showPrevisions) {
        if (showPrevisions && previsions.isEmpty()) {
            isLoadingPrevisions = true
            try {
                previsions = meteoRepository.getPrevisions7Jours(ville.ifEmpty { "Paris" })
            } catch (e: Exception) {
                previsions = emptyList()
            }
            isLoadingPrevisions = false
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
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showPrevisions = true },
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
                        Text(
                            text = "${phaseLune.emoji} ${phaseLune.nom}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
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
                if (imageUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Photo du jardin",
                            imageLoader = imageLoader,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                        )
                        IconButton(
                            onClick = { showPhotoDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Changer la photo",
                                tint = Color.White
                            )
                        }
                    }
                } else {
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
                            text = "Prenez une photo ou choisissez une image",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPhotoDialog = true },
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
                            Text("Ajouter une photo")
                        }
                    }
                }
            }
        }
    }
    
    if (showPrevisions) {
        AlertDialog(
            onDismissRequest = { showPrevisions = false },
            title = { 
                Text("📅 Prévisions 7 jours")
            },
            text = {
                if (isLoadingPrevisions) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chargement...")
                    }
                } else if (previsions.isEmpty()) {
                    Text("Impossible de récupérer les prévisions")
                } else {
                    Column {
                        previsions.forEach { prevision ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prevision.date,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = prevision.emoji,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${prevision.tempMin.toInt()}° / ${prevision.tempMax.toInt()}°",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrevisions = false }) {
                    Text("Fermer")
                }
            }
        )
    }
    
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Ajouter une photo") },
            text = {
                Column {
                    Text(
                        text = "📸 Prendre une photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoDialog = false
                                photoLauncher.launch(null)
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Text(
                        text = "🖼️ Choisir depuis la galerie",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (imageUri != null) {
                        HorizontalDivider()
                        Text(
                            text = "🗑️ Supprimer la photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    imageUri = null
                                    showPhotoDialog = false
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Annuler")
                }
            }
        )
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

// ============== BIBLIOTHÈQUE (avec onglets Plantes / Mauvaises herbes) ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequeScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("plantes") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedOnglet == "plantes") 0 else 1,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedOnglet == "plantes",
                onClick = { selectedOnglet = "plantes" },
                text = { 
                    Text(
                        "🌱 Plantes",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "plantes") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "mauvaises",
                onClick = { selectedOnglet = "mauvaises" },
                text = { 
                    Text(
                        "🌿 Mauvaises herbes",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "mauvaises") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
            )
        }
        
        if (selectedOnglet == "plantes") {
            BibliothequePlantesScreen(onBack = onBack)
        } else {
            AdventicesScreen(onBack = onBack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequePlantesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { LegumeRepository(context) }
    val legumes by repository.legumes.collectAsState(initial = emptyList())
    var selectedLegume by remember { mutableStateOf<LegumeEntity?>(null) }
    var showLegende by remember { mutableStateOf(false) }
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
                    actions = {
                        IconButton(onClick = { showLegende = true }) {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = "Légende",
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
                        text = "${legumes.size} plantes dans votre bibliothèque",
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
    
    if (showLegende) {
        AlertDialog(
            onDismissRequest = { showLegende = false },
            title = { Text("Légende des catégories") },
            text = {
                LazyColumn {
                    item { LigneLegende("🥕", "Racines (carotte, panais, navet...)") }
                    item { LigneLegende("🥔", "Tubercules (pomme de terre, topinambour...)") }
                    item { LigneLegende("🍅", "Fruits (tomate, courgette, poivron...)") }
                    item { LigneLegende("🥬", "Feuilles (salade, épinard, chou frisé...)") }
                    item { LigneLegende("🫘", "Légumineuses (haricot, pois...)") }
                    item { LigneLegende("🧅", "Alliacés (oignon, ail, poireau...)") }
                    item { LigneLegende("🥦", "Choux (brocoli, chou-fleur...)") }
                    item { LigneLegende("🎃", "Cucurbitacées (potiron, courge...)") }
                    item { LigneLegende("🌿", "Aromatiques (basilic, thym, romarin...)") }
                    item { LigneLegende("💐", "Fleurs vivaces (lavande...)") }
                    item { LigneLegende("🌸", "Fleurs annuelles (capucine, souci...)") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLegende = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun LigneLegende(emoji: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventicesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdventiceRepository(context) }
    val adventices by repository.adventices.collectAsState(initial = emptyList())
    var selectedAdventice by remember { mutableStateOf<AdventiceEntity?>(null) }
    
    LaunchedEffect(Unit) {
        repository.ajouterAdventicesPredefinies()
    }
    
    if (selectedAdventice != null) {
        AdventiceDetailScreen(
            adventice = selectedAdventice!!,
            onBack = { selectedAdventice = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("Mauvaises herbes 🌿", fontWeight = FontWeight.Bold)
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
                        text = "${adventices.size} adventices courantes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ces plantes indiquent la nature de votre sol. Cliquez pour en savoir plus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(adventices, key = { it.id }) { adventice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAdventice = adventice },
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
                            Text(
                                text = adventice.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = adventice.nom,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = adventice.indicationSol,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Voir",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventiceDetailScreen(
    adventice: AdventiceEntity,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(adventice.nom, fontWeight = FontWeight.Bold)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = adventice.emoji,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
            item { InfoCard("Nom scientifique", adventice.nomScientifique) }
            item { InfoCard("Description", adventice.description) }
            item { InfoCard("Ce qu'elle indique", adventice.indicationSol) }
            item { InfoCard("Type de sol", adventice.typeSol) }
        }
    }
}

// ============== JARDIN (avec onglets) ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("planches") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedOnglet == "planches") 0 else 1,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedOnglet == "planches",
                onClick = { selectedOnglet = "planches" },
                text = { 
                    Text(
                        "🌱 Planches",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "planches") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "analyse",
                onClick = { selectedOnglet = "analyse" },
                text = { 
                    Text(
                        "🔬 Analyse du sol",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "analyse") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                }
            )
        }
        
        if (selectedOnglet == "planches") {
            JardinPlanchesScreen(
                onBack = onBack,
                onNavigateToAnalyse = { selectedOnglet = "analyse" }
            )
        } else {
            AnalyseSolScreen(
                onBack = onBack,
                onNavigateToPlanches = { selectedOnglet = "planches" }
            )
        }
    }
}

// ============== JARDIN - PLANCHES ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinPlanchesScreen(onBack: () -> Unit, onNavigateToAnalyse: () -> Unit) {
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
                item {
                    LegendeCouleurs()
                }
                
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
                    LegendeCompacte()
                    Spacer(modifier = Modifier.height(16.dp))
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
        var selectedCategorie by remember { mutableStateOf<String?>(null) }
        
        val categories = legumes.groupBy { it.categorie }.keys.toList()
        
        AlertDialog(
            onDismissRequest = { 
                showLegumeSelection = false
                selectedCarre = null
                selectedCaseNumero = 0
            },
            title = { Text("Choisissez une plante") },
            text = {
                Column {
                    Text(
                        text = "Case ${caseNumero} du carré",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { categorie ->
                            FilterChip(
                                selected = selectedCategorie == categorie,
                                onClick = { 
                                    selectedCategorie = if (selectedCategorie == categorie) null else categorie
                                },
                                label = { 
                                    Text(
                                        text = "${getEmojiCategorie(categorie)} ${categorie}",
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (selectedCategorie != null) {
                        val plantes = legumes.filter { it.categorie == selectedCategorie }
                        LazyColumn {
                            items(plantes) { legume ->
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

// ============== JARDIN - ANALYSE DU SOL ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseSolScreen(onBack: () -> Unit, onNavigateToPlanches: () -> Unit) {
    var argile by remember { mutableStateOf("") }
    var sable by remember { mutableStateOf("") }
    var limon by remember { mutableStateOf("") }
    var showAide by remember { mutableStateOf(false) }
    var typeSol by remember { mutableStateOf("") }
    
    fun calculerTypeSol() {
        val a = argile.toIntOrNull() ?: 0
        val s = sable.toIntOrNull() ?: 0
        val l = limon.toIntOrNull() ?: 0
        val total = a + s + l
        
        if (total == 100) {
            typeSol = when {
                a > 40 -> "Sol argileux"
                s > 70 -> "Sol sableux"
                l > 50 -> "Sol limoneux"
                a in 20..35 && s in 35..50 -> "Sol équilibré (idéal)"
                a > 35 -> "Sol argilo-limoneux"
                s > 50 -> "Sol sablo-limoneux"
                else -> "Sol limono-argileux"
            }
        } else {
            typeSol = "Le total doit faire 100% (actuellement ${total}%)"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Analyse du sol 🔬", fontWeight = FontWeight.Bold)
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
                actions = {
                    IconButton(onClick = { showAide = true }) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = "Aide",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Composition du sol",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Renseignez les pourcentages d'argile, de sable et de limon.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = argile,
                            onValueChange = { argile = it },
                            label = { Text("Argile (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sable,
                            onValueChange = { sable = it },
                            label = { Text("Sable (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = limon,
                            onValueChange = { limon = it },
                            label = { Text("Limon (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = { calculerTypeSol() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Analyser")
                        }
                    }
                }
            }
            
            if (typeSol.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Résultat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = typeSol,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAide) {
        AlertDialog(
            onDismissRequest = { showAide = false },
            title = { Text("Méthodes d'analyse du sol") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            text = "Méthode I : Test tactile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Prélevez une poignée de terre humide.\n2. Roulez-la entre vos doigts pour former une boule.\n3. Écrasez-la entre le pouce et l'index.\n\n• Si elle est rugueuse et se désagrège : sol sableux.\n• Si elle est douce comme du talc : sol limoneux.\n• Si elle colle et se lisse facilement : sol argileux.")
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Méthode II : Test du bocal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Prélevez un échantillon de sol (sans racines ni cailloux).\n2. Remplissez un bocal transparent à moitié avec ce sol.\n3. Ajoutez de l'eau et une goutte de liquide vaisselle.\n4. Secouez vigoureusement pendant 1 à 2 minutes.\n5. Laissez reposer 24 à 48 heures.\n\n• Le sable se dépose en premier (au fond).\n• Le limon forme la couche intermédiaire.\n• L'argile reste en suspension ou dépose lentement.\n\nMesurez la hauteur de chaque couche pour calculer les pourcentages.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAide = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

// Légende des couleurs pour le jardin
@Composable
fun LegendeCouleurs() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Légende",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bonne association", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Association neutre", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFF44336).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mauvaise association", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plante vivace (bordure en gras)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun LegendeCompacte() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Légende des couleurs :",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bonne", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Neutre", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFF44336).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mauvaise", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vivace", style = MaterialTheme.typography.bodySmall)
            }
        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planche.nom,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
    val legumes = listOfNotNull(
        carre.case1, carre.case2, carre.case3,
        carre.case4, carre.case5, carre.case6,
        carre.case7, carre.case8, carre.case9
    )
    
    val toutesMemePlante = legumes.size == 9 && legumes.distinct().size == 1
    
    if (toutesMemePlante) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clickable(onClick = { onSousCarreClick(1) }),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = legumes[0],
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    } else {
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
                                .border(
                                    width = if (legume != null && estVivace(legume)) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
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
}

fun estVivace(nomLegume: String): Boolean {
    val vivaces = listOf(
        "Lavande", "Menthe", "Thym", "Romarin", "Ciboulette", "Topinambour"
    )
    return nomLegume in vivaces
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
        "Courge" to listOf("Haricot", "Maïs"),
        "Lavande" to listOf("Tomate", "Chou"),
        "Capucine" to listOf("Tomate", "Chou", "Courgette"),
        "Souci" to listOf("Tomate", "Chou", "Carotte"),
        "Bourrache" to listOf("Fraisier", "Tomate", "Courgette"),
        "Phacélie" to listOf("Tous les légumes"),
        "Cosmos" to listOf("Tous les légumes"),
        "Œillet d'Inde" to listOf("Tomate", "Pomme de terre", "Chou")
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
    val luneRepository = remember { LuneRepository() }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    
    var legumesPlantes by remember { mutableStateOf<List<String>>(emptyList()) }
    var datesPlantation by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("Paris") }
    var estConnecte by remember { mutableStateOf(true) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
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
            datesPlantation = jardinRepository.getDatesPlantation()
        } catch (e: Exception) {
            legumesPlantes = emptyList()
            datesPlantation = emptyMap()
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
                MeteoCard(meteo, ville, estConnecte, phaseLune)
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
                    text = "Plantes plantées (${legumesPlantes.size}) :",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (legumesPlantes.isEmpty()) {
                item {
                    Text(
                        text = "Aucune plante plantée. Ajoutez des plantes dans votre jardin !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                legumesPlantes.forEach { legumeNom ->
                    val legume = legumes.find { it.nom == legumeNom }
                    if (legume != null) {
                        item {
                            CalendrierLegumeCard(
                                legume = legume,
                                datePlantation = datesPlantation[legume.nom]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeteoCard(meteo: MeteoData?, ville: String, estConnecte: Boolean, phaseLune: PhaseLune? = null) {
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
            if (phaseLune != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${phaseLune.emoji} ${phaseLune.nom}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
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
fun CalendrierLegumeCard(legume: LegumeEntity, datePlantation: Long? = null) {
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
            if (datePlantation != null) {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
                val date = Date(datePlantation)
                Text(
                    text = "🌱 Planté le : ${dateFormat.format(date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
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
                    }.size} plantes",
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
    val context = LocalContext.current
    val varieteRepository = remember { VarieteRepository(context) }
    val varietes by varieteRepository.getVarietesForLegume(legume.nom).collectAsState(initial = emptyList())
    var selectedVariete by remember { mutableStateOf<VarieteEntity?>(null) }
    
    LaunchedEffect(Unit) {
        varieteRepository.ajouterVarietesPredefinies()
    }
    
    if (selectedVariete != null) {
        VarieteDetailScreen(
            variete = selectedVariete!!,
            onBack = { selectedVariete = null }
        )
    } else {
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
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getEmojiCategorie(legume.categorie),
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
                
                item { InfoCard("Catégorie", legume.categorie) }
                if (legume.estVivace) {
                    item { InfoCard("Type", "🌿 Plante vivace") }
                }
                if (legume.estFleur) {
                    item { InfoCard("Type", "🌸 Fleur") }
                }
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
                
                if (varietes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🌱 Variétés populaires (${varietes.size}) :",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(varietes) { variete ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVariete = variete },
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
                                Text(
                                    text = "🌱",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = variete.nom,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = variete.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Voir",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarieteDetailScreen(
    variete: VarieteEntity,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(variete.nom, fontWeight = FontWeight.Bold)
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
            item { InfoCard("Description", variete.description) }
            item { InfoCard("Semis", variete.semis) }
            item { InfoCard("Plantation", variete.plantation) }
            item { InfoCard("Récolte", variete.recolte) }
            item { InfoCard("Entretien", variete.entretien) }
            item { InfoCard("Particularités", variete.particularites) }
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
            Text(
                text = getEmojiCategorie(legume.categorie),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.size(60.dp)
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

fun getEmojiCategorie(categorie: String): String {
    return when {
        categorie.contains("Racine ancienne", ignoreCase = true) -> "🥕"
        categorie.contains("Tubercule ancien", ignoreCase = true) -> "🥔"
        categorie.contains("Racine", ignoreCase = true) -> "🥕"
        categorie.contains("Tubercule", ignoreCase = true) -> "🥔"
        categorie.contains("Fruit", ignoreCase = true) -> "🍅"
        categorie.contains("Feuille ancienne", ignoreCase = true) -> "🥬"
        categorie.contains("Feuille", ignoreCase = true) -> "🥬"
        categorie.contains("Légumineuse", ignoreCase = true) -> "🫘"
        categorie.contains("Alliacé", ignoreCase = true) -> "🧅"
        categorie.contains("Chou", ignoreCase = true) -> "🥦"
        categorie.contains("Cucurbitacée", ignoreCase = true) -> "🎃"
        categorie.contains("Fleur vivace", ignoreCase = true) -> "💐"
        categorie.contains("Fleur", ignoreCase = true) -> "🌸"
        categorie.contains("Aromatique", ignoreCase = true) -> "🌿"
        else -> "🌱"
    }
}

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
