package com.theshire.app

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import com.theshire.app.data.AdventiceEntity
import com.theshire.app.data.AvertissementRotation
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.LocalisationRepository
import com.theshire.app.data.LuneRepository
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.NiveauRisque
import com.theshire.app.data.OutilsApp
import com.theshire.app.data.PhaseLune
import com.theshire.app.data.PlancheEntity
import com.theshire.app.data.PlantIdentification
import com.theshire.app.data.PlantNetRepository
import com.theshire.app.data.PrevisionJour
import com.theshire.app.data.RappelCulturelEntity
import com.theshire.app.data.RotationRepository
import com.theshire.app.data.ThemePreferences
import com.theshire.app.data.VarieteEntity
import com.theshire.app.data.BrandingApp
import com.theshire.app.ui.AdventiceRepository
import com.theshire.app.ui.ContenantRepository
import com.theshire.app.ui.EcranContenants
import com.theshire.app.ui.EcranOutils
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.OngletConseilsUrbains
import com.theshire.app.ui.OperationsCulturales
import com.theshire.app.ui.Outil
import com.theshire.app.ui.Outils
import com.theshire.app.ui.ParametresScreen
import com.theshire.app.ui.RappelCulturelRepository
import com.theshire.app.ui.RappelRepository
import com.theshire.app.ui.VarieteRepository
import com.theshire.app.ui.theme.CouleursApp
import com.theshire.app.ui.theme.PotagerShireTheme
import com.theshire.app.ui.theme.envelopperAvecTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun getDegradeFond(): Brush {
    return if (CouleursApp.isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1F1A),
                Color(0xFF1F241F),
                Color(0xFF252B25)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF6F0),
                Color(0xFFF0F0E8),
                Color(0xFFE8EFE8)
            )
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.isNotEmpty()) requestPermissions(permissions.toTypedArray(), 1000)
        
        CouleursApp.changerModeSombre(ThemePreferences.chargerModeSombre(this))
        BrandingApp.initialiser(this)
        OutilsApp.initialiser(this)
        
        setContent { PotagerShireTheme { MainScreen() } }
        planifierNotifications()
    }
    
    private fun planifierNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        val intent1 = android.content.Intent(this, NotificationReceiver::class.java).putExtra("type", "arrosage")
        val pending1 = android.app.PendingIntent.getBroadcast(this, 1, intent1, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val cal1 = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1) }
        alarmManager.setRepeating(android.app.AlarmManager.RTC_WAKEUP, cal1.timeInMillis, android.app.AlarmManager.INTERVAL_DAY, pending1)
        val intent2 = android.content.Intent(this, NotificationReceiver::class.java).putExtra("type", "operations")
        val pending2 = android.app.PendingIntent.getBroadcast(this, 2, intent2, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        val cal2 = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1) }
        alarmManager.setRepeating(android.app.AlarmManager.RTC_WAKEUP, cal2.timeInMillis, android.app.AlarmManager.INTERVAL_DAY, pending2)
    }
}

object ImageLoaderProvider {
    fun getImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient { OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build() }
        .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
        .diskCache { DiskCache.Builder().directory(context.cacheDir.resolve("image_cache")).maxSizePercent(0.02).build() }
        .crossfade(true).build()
}

@Composable
fun MainScreen() {
    var currentScreen by rememberSaveable { mutableStateOf("accueil") }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }
    val navigationStack = remember { mutableStateListOf("accueil") }
    val screens = listOf("accueil", "bibliotheque", "jardin", "calendrier", "conservation", "equipement")
    fun navigateTo(screen: String) { navigationStack.add(screen); currentScreen = screen }
    fun goBack() {
        if (navigationStack.size > 1) { navigationStack.removeAt(navigationStack.size - 1); currentScreen = navigationStack.last() }
        else { val now = System.currentTimeMillis(); if (now - lastBackPressTime < 1000) (context as? android.app.Activity)?.finish() else { lastBackPressTime = now; android.widget.Toast.makeText(context, "Appuyez encore pour quitter", android.widget.Toast.LENGTH_SHORT).show() } }
    }
    fun goToAccueil() { navigationStack.clear(); navigationStack.add("accueil"); currentScreen = "accueil" }
    androidx.activity.compose.BackHandler { goBack() }
    fun goToNext() { val i = screens.indexOf(currentScreen); if (i < screens.size - 1) navigateTo(screens[i + 1]) }
    fun goToPrevious() { val i = screens.indexOf(currentScreen); if (i > 0) navigateTo(screens[i - 1]) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    Box(modifier = Modifier.fillMaxSize().background(getDegradeFond()).pointerInput(currentScreen) {
        detectHorizontalDragGestures(
            onDragEnd = { if (dragOffset < -200f) goToNext() else if (dragOffset > 200f) goToPrevious(); dragOffset = 0f },
            onHorizontalDrag = { change, amount -> change.consume(); dragOffset += amount }
        )
    }) {
        AnimatedContent(targetState = currentScreen, transitionSpec = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 } togetherWith fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 3 } }) { screen ->
            when (screen) {
                "accueil" -> AccueilScreen(onNavigateToParametres = { navigateTo("parametres") })
                "bibliotheque" -> BibliothequeScreen(onBack = { goToAccueil() })
                "jardin" -> JardinScreen(onBack = { goToAccueil() })
                "calendrier" -> CalendrierScreen(onBack = { goToAccueil() })
                "conservation" -> ConservationScreen(onBack = { goToAccueil() })
                "equipement" -> EcranOutils(onBack = { goToAccueil() })
                "parametres" -> ParametresScreen(
                    onBack = { goBack() },
                    onRevoirTutoriel = {
                        val prefs = context.getSharedPreferences("jardin_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("tuto_vu_v5", false).apply()
                        goToAccueil()
                    }
                )
            }
        }
        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(CouleursApp.Blanc.copy(alpha = 0.85f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            screens.forEach { screen ->
                val isCurrent = screen == currentScreen
                Box(modifier = Modifier.size(if (isCurrent) 12.dp else 10.dp).background(if (isCurrent) CouleursApp.VertPrincipal else CouleursApp.Blanc, CircleShape).border(if (isCurrent) 0.dp else 1.dp, CouleursApp.VertPrincipal.copy(alpha = 0.3f), CircleShape).clickable { currentScreen = screen; navigationStack.clear(); navigationStack.add(screen) })
            }
        }
    }
}

// ============== ACCUEIL ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccueilScreen(onNavigateToParametres: () -> Unit) {
    val context = LocalContext.current
    val meteoRepository = remember { MeteoRepository() }
    val localisationRepository = remember { LocalisationRepository(context) }
    val luneRepository = remember { LuneRepository() }
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("") }
    val prefs = remember { context.getSharedPreferences("jardin_prefs", Context.MODE_PRIVATE) }
    var imagePath by remember { mutableStateOf(prefs.getString("photo_path", null)) }
    val imageFile = imagePath?.let { File(it) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showPrevisions by remember { mutableStateOf(false) }
    var previsions by remember { mutableStateOf<List<PrevisionJour>>(emptyList()) }
    var chargementPrevisions by remember { mutableStateOf(false) }
    var showTuto by remember { mutableStateOf(prefs.getBoolean("tuto_vu_v5", false) == false) }
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { output -> input.copyTo(output) } }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    
    LaunchedEffect(Unit) { 
        try { 
            val v = localisationRepository.getVille()
            if (v != null) ville = v
            meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" })
        } catch (e: Exception) {} 
    }
    
    LaunchedEffect(showPrevisions) {
        if (showPrevisions && previsions.isEmpty()) {
            chargementPrevisions = true
            try {
                previsions = meteoRepository.getPrevisions7Jours(ville.ifEmpty { "Paris" })
            } catch (e: Exception) {
                previsions = emptyList()
            }
            chargementPrevisions = false
        }
    }
    
    Scaffold(containerColor = CouleursApp.Creme) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(BrandingApp.config.nomApp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, color = CouleursApp.VertPrincipal)
                IconButton(onClick = onNavigateToParametres, modifier = Modifier.background(CouleursApp.VertPale, CircleShape).size(48.dp)) {
                    Text("⚙️", style = MaterialTheme.typography.titleLarge)
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).clickable { showPrevisions = true; chargementPrevisions = true }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                Box(modifier = Modifier.background(Brush.linearGradient(listOf(CouleursApp.VertPale, CouleursApp.Blanc)))) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(getEmojiMeteo(meteo), style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            if (meteo != null) { Text("${meteo!!.temperature}°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(meteo!!.description, color = CouleursApp.TexteFonce) }
                            else { Text("--°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text("Météo indisponible", color = CouleursApp.TexteFonce) }
                            Text(dateFormat.format(Date()), style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                            Text("${phaseLune.emoji} ${phaseLune.nom}", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                            if (ville.isNotEmpty()) Text("📍 $ville", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth().weight(1f).shadow(4.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                if (imageFile != null && imageFile.exists()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val loader = remember { ImageLoaderProvider.getImageLoader(context) }
                        AsyncImage(model = imageFile, contentDescription = "Photo", imageLoader = loader, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)))
                        IconButton(onClick = { showPhotoDialog = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).background(CouleursApp.VertPrincipal.copy(alpha = 0.8f), CircleShape)) { Icon(Icons.Default.CameraAlt, "Changer", tint = Color.White) }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(80.dp), tint = CouleursApp.VertClair)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📸 Photo de mon jardin", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = CouleursApp.TexteFonce)
                        Text("Prenez une photo ou choisissez une image", textAlign = TextAlign.Center, color = CouleursApp.TexteFonce)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showPhotoDialog = true }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Ajouter une photo") }
                    }
                }
            }
        }
    }
    
    if (showPrevisions) {
        AlertDialog(
            onDismissRequest = { showPrevisions = false },
            title = { Text("📅 Prévisions 7 jours", fontWeight = FontWeight.Bold) },
            text = {
                if (chargementPrevisions) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = CouleursApp.VertPrincipal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Chargement...", color = CouleursApp.TexteFonce)
                    }
                } else if (previsions.isEmpty()) {
                    Text("Prévisions indisponibles", color = CouleursApp.TexteFonce)
                } else {
                    Column {
                        previsions.forEach { p ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(p.date, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce, modifier = Modifier.weight(1f))
                                Text(p.emoji, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${p.tempMin.toInt()}° / ${p.tempMax.toInt()}°", color = CouleursApp.TexteFonce)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPrevisions = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } }
        )
    }
    
    if (showPhotoDialog) {
        AlertDialog(onDismissRequest = { showPhotoDialog = false }, title = { Text("Ajouter une photo", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("📸 Prendre une photo", modifier = Modifier.fillMaxWidth().clickable { showPhotoDialog = false; photoLauncher.launch(null) }.padding(16.dp), fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Text("🖼️ Choisir depuis la galerie", modifier = Modifier.fillMaxWidth().clickable { showPhotoDialog = false; galleryLauncher.launch("image/*") }.padding(16.dp), fontWeight = FontWeight.Bold)
                if (imageFile != null && imageFile.exists()) { HorizontalDivider(); Text("🗑️ Supprimer", modifier = Modifier.fillMaxWidth().clickable { imageFile!!.delete(); prefs.edit().remove("photo_path").apply(); imagePath = null; showPhotoDialog = false }.padding(16.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            } },
            confirmButton = { TextButton(onClick = { showPhotoDialog = false }) { Text("Annuler") } })
    }
    
    if (showTuto) {
        AlertDialog(
            onDismissRequest = { showTuto = false; prefs.edit().putBoolean("tuto_vu_v5", true).apply() },
            title = { Text("🌱 Bienvenue dans ${BrandingApp.config.nomApp} !", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
            text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                item { Column { Text("🏠 Accueil", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Météo, phase de lune et photo de votre jardin.") } }
                item { Column { Text("📚 Bibliothèque", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Plantes, Adventices, Reconnaissance photo.") } }
                item { Column { Text("🏡 Jardin", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Pleine terre (planches), Agriculture urbaine (contenants + conseils), Analyse du sol.") } }
                item { Column { Text("🏙️ Agriculture urbaine", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Créez vos pots, jardinières et tours de culture.") } }
                item { Column { Text("📅 Calendrier & opérations", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Opérations culturales automatiques (pleine terre ET urbain).") } }
                item { Column { Text("🛠️ Équipement", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Gérez vos outils, consultez les tutos.") } }
                item { Column { Text("👆 Navigation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Swipe ou clic sur les billes en bas.") } }
            } },
            confirmButton = { Button(onClick = { showTuto = false; prefs.edit().putBoolean("tuto_vu_v5", true).apply() }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Commencer 🌱") } }
        )
    }
}

fun getEmojiMeteo(meteo: MeteoData?): String = when {
    meteo == null -> "🌤️"
    meteo.description.contains("pluie", true) -> "🌧️"
    meteo.description.contains("nuage", true) -> "☁️"
    meteo.description.contains("soleil", true) || meteo.description.contains("clair", true) -> "☀️"
    meteo.description.contains("neige", true) -> "❄️"
    meteo.description.contains("orage", true) -> "⛈️"
    else -> "🌤️"
}

// ============== BIBLIOTHÈQUE ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequeScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("plantes") }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = when(selectedOnglet) { "plantes" -> 0; "adventices" -> 1; else -> 2 }, containerColor = CouleursApp.VertPrincipal, contentColor = Color.White) {
            Tab(selected = selectedOnglet == "plantes", onClick = { selectedOnglet = "plantes" }, text = { Text("🌱 Plantes", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "plantes") Color.White else Color.White.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "adventices", onClick = { selectedOnglet = "adventices" }, text = { Text("🌿 Adventices", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "adventices") Color.White else Color.White.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "reconnaissance", onClick = { selectedOnglet = "reconnaissance" }, text = { Text("📸 Identifier", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "reconnaissance") Color.White else Color.White.copy(alpha = 0.6f)) })
        }
        when (selectedOnglet) {
            "plantes" -> BibliothequePlantesScreen(onBack)
            "adventices" -> AdventicesScreen(onBack)
            else -> ReconnaissanceScreen(onBack)
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
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { repository.ajouterLegumesPredefinis() }
    
    if (selectedLegume != null) LegumeDetailScreen(selectedLegume!!, onBack = { selectedLegume = null })
    else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = { TopAppBar(title = { Text("Bibliothèque 📚", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("🔍 Rechercher une plante...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true) }
                item { Text("${legumes.filter { it.nom.contains(searchQuery, true) }.size} plantes trouvées", color = CouleursApp.TexteFonce) }
                items(legumes.filter { it.nom.contains(searchQuery, true) }, key = { it.id }) { legume ->
                    LegumeCard(legume, { selectedLegume = legume }, { scope.launch { repository.supprimerLegume(legume) } })
                }
            }
        }
    }
}

// ============== RECONNAISSANCE ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconnaissanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { PlantNetRepository() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var resultats by remember { mutableStateOf<List<PlantIdentification>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    
    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg"); f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it) }; imageUri = Uri.fromFile(f); resultats = emptyList(); scope.launch { isAnalyzing = true; try { resultats = repo.identifierPlante(f) } catch (e: Exception) { errorMessage = e.message ?: "Erreur" }; isAnalyzing = false } }
    }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { output -> input.copyTo(output) } }; imageUri = Uri.fromFile(f); resultats = emptyList(); scope.launch { isAnalyzing = true; try { resultats = repo.identifierPlante(f) } catch (e: Exception) { errorMessage = e.message ?: "Erreur" }; isAnalyzing = false } }
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Identifier une plante 📸", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { photoLauncher.launch(null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("📸 Photo") }
                Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)) { Text("🖼️ Galerie") }
            } }
            if (imageUri != null) { item { val loader = remember { ImageLoaderProvider.getImageLoader(context) }; AsyncImage(model = imageUri, contentDescription = "Plante", imageLoader = loader, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp))) } }
            if (isAnalyzing) item { CircularProgressIndicator(color = CouleursApp.VertPrincipal); Text("Analyse en cours...", color = CouleursApp.TexteFonce) }
            if (errorMessage.isNotEmpty()) item { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            if (resultats.isNotEmpty()) item { Text("🔍 Résultats :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }
            resultats.forEach { r -> item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)) { Column(modifier = Modifier.padding(16.dp)) { Text(r.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(r.nomScientifique, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce); if (r.probabilite > 0) Text("Confiance : ${(r.probabilite * 100).toInt()}%", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold) } } } }
        }
    }
}

// ============== ADVENTICES ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventicesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdventiceRepository(context) }
    val adventices by repository.adventices.collectAsState(initial = emptyList())
    var selected by remember { mutableStateOf<AdventiceEntity?>(null) }
    LaunchedEffect(Unit) { repository.ajouterAdventicesPredefinies() }
    
    if (selected != null) AdventiceDetailScreen(selected!!, onBack = { selected = null })
    else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = { TopAppBar(title = { Text("Adventices 🌿", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("${adventices.size} adventices courantes", color = CouleursApp.TexteFonce); Spacer(modifier = Modifier.height(8.dp)); Text("Les adventices (mauvaises herbes) indiquent la nature de votre sol.", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) }
                items(adventices, key = { it.id }) { a ->
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { selected = a }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.emoji, style = MaterialTheme.typography.headlineMedium); Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(a.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(a.indicationSol, style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, maxLines = 2) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventiceDetailScreen(adventice: AdventiceEntity, onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text(adventice.nom, fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)).background(CouleursApp.VertPale), contentAlignment = Alignment.Center) { Text(adventice.emoji, style = MaterialTheme.typography.displayLarge) } }
            item { InfoCard("Nom scientifique", adventice.nomScientifique) }
            item { InfoCard("Description", adventice.description) }
            item { InfoCard("Ce qu'elle indique", adventice.indicationSol) }
            item { InfoCard("Type de sol", adventice.typeSol) }
        }
    }
}
