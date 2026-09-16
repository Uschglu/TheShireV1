package com.theshire.app

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.theshire.app.data.PhaseLune
import com.theshire.app.data.PlancheEntity
import com.theshire.app.data.PlantIdentification
import com.theshire.app.data.PlantNetRepository
import com.theshire.app.data.PrevisionJour
import com.theshire.app.data.RappelCulturelEntity
import com.theshire.app.data.ReseauRepository
import com.theshire.app.data.RotationRepository
import com.theshire.app.data.ThemePreferences
import com.theshire.app.data.VarieteEntity
import com.theshire.app.data.BrandingApp
import com.theshire.app.ui.AdventiceRepository
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
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
        
        // Charger le thème depuis les préférences
        CouleursApp.changerModeSombre(ThemePreferences.chargerModeSombre(this))
        
        // Charger le branding depuis les préférences
        BrandingApp.initialiser(this)
        
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
    val screens = listOf("accueil", "bibliotheque", "jardin", "calendrier", "conservation")
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
                "parametres" -> ParametresScreen(
                    onBack = { goBack() },
                    onRevoirTutoriel = {
                        // Reset du flag pour forcer l'affichage du tutoriel
                        val prefs = context.getSharedPreferences("jardin_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("tuto_vu_v5", false).apply()
                        // Retour à l'accueil : le tutoriel s'affichera automatiquement
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
    var showTuto by remember { mutableStateOf(prefs.getBoolean("tuto_vu_v5", false) == false) }
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { output -> input.copyTo(output) } }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    
    LaunchedEffect(Unit) { try { val v = localisationRepository.getVille(); if (v != null) ville = v; meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" }) } catch (e: Exception) {} }
    
    Scaffold(
        containerColor = CouleursApp.Creme
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            // ===== EN-TÊTE : Titre + Bouton Paramètres =====
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    BrandingApp.config.nomApp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CouleursApp.VertPrincipal
                )
                IconButton(
                    onClick = onNavigateToParametres,
                    modifier = Modifier
                        .background(CouleursApp.VertPale, CircleShape)
                        .size(48.dp)
                ) {
                    Text(
                        "⚙️",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).clickable { showPrevisions = true }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
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
        AlertDialog(onDismissRequest = { showPrevisions = false }, title = { Text("📅 Prévisions 7 jours", fontWeight = FontWeight.Bold) },
            text = { if (previsions.isEmpty()) Text("Chargement...") else Column { previsions.forEach { p -> Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.date, fontWeight = FontWeight.Bold); Text(p.emoji); Text("${p.tempMin.toInt()}°/${p.tempMax.toInt()}°") } } } },
            confirmButton = { TextButton(onClick = { showPrevisions = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } })
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
                item { Column { Text("🏠 Accueil", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Météo, phase de lune et photo de votre jardin. Le bouton ⚙️ en haut à droite donne accès aux paramètres (mode sombre, tutoriel, à propos).") } }
                item { Column { Text("📚 Bibliothèque", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Plantes, Adventices, Reconnaissance photo.") } }
                item { Column { Text("🏡 Jardin", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Créez des planches et choisissez vos plantes. Les distances de plantation sont automatiquement respectées.") } }
                item { Column { Text("🌱 Case centrale", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Appuyez sur la case centrale d'un carré : un menu vous propose de remplir tout le m² (les 9 cases) avec la même plante, ou juste cette case.") } }
                item { Column { Text("🎨 Couleurs des cases", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Vert = bonne association, Orange = neutre, Rouge = mauvaise association. Les associations tiennent compte des carrés voisins (m² adjacents).") } }
                item { Column { Text("🔍 Zoom sur les planches", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Pincez à deux doigts sur une planche dépliée pour zoomer et dézoomer. Glissez à deux doigts pour vous déplacer. Un bouton ↺ apparaît pour réinitialiser le zoom.") } }
                item { Column { Text("📅 Calendrier & opérations", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Quand vous plantez, des opérations culturales (tuteurage, buttage, éclaircissage...) sont générées automatiquement et affichées sur le calendrier sous forme de barres ←→ sur leur période. Cliquez pour voir les détails et marquer comme fait.") } }
                item { Column { Text("🌿 Adventices = mauvaises herbes", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Les adventices indiquent la nature de votre sol.") } }
                item { Column { Text("🥫 Conservation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Guide détaillé avec le bouton ?.") } }
                item { Column { Text("👆 Navigation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Swipe pour changer de page, cliquez sur les billes en bas pour accéder directement. Le bouton ⚙️ ouvre les paramètres.") } }
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

// ============== JARDIN ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("planches") }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = if (selectedOnglet == "planches") 0 else 1, containerColor = CouleursApp.VertPrincipal, contentColor = Color.White) {
            Tab(selected = selectedOnglet == "planches", onClick = { selectedOnglet = "planches" }, text = { Text("🌱 Planches", fontWeight = FontWeight.Bold, color = if (selectedOnglet == "planches") Color.White else Color.White.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "analyse", onClick = { selectedOnglet = "analyse" }, text = { Text("🔬 Analyse du sol", fontWeight = FontWeight.Bold, color = if (selectedOnglet == "analyse") Color.White else Color.White.copy(alpha = 0.6f)) })
        }
        if (selectedOnglet == "planches") JardinPlanchesScreen(onBack) else AnalyseSolScreen(onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinPlanchesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val varieteRepository = remember { VarieteRepository(context) }
    val planches by jardinRepository.planches.collectAsState(initial = emptyList())
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val rotationRepository = remember { RotationRepository() }
    var showAddPlancheDialog by remember { mutableStateOf(false) }
    var expandedPlancheId by remember { mutableStateOf<Long?>(null) }
    var selectedCarre by remember { mutableStateOf<CarreEntity?>(null) }
    var selectedCaseNumero by remember { mutableStateOf(0) }
    var selectedLegumeNom by remember { mutableStateOf<String?>(null) }
    var showLegumeSelection by remember { mutableStateOf(false) }
    var showVarieteSelection by remember { mutableStateOf(false) }
    var showChoixRemplissage by remember { mutableStateOf(false) }
    var remplirM2Mode by remember { mutableStateOf(false) }
    var avertissement by remember { mutableStateOf<AvertissementRotation?>(null) }
    var showAvertissement by remember { mutableStateOf(false) }
    var currentPlancheId by remember { mutableStateOf<Long>(0L) }
    
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis(); varieteRepository.ajouterVarietesPredefinies() }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Mon Jardin 🏡", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddPlancheDialog = true }, containerColor = CouleursApp.VertClair, shape = CircleShape) { Icon(Icons.Default.Add, "Ajouter") } }
    ) { innerPadding ->
        if (planches.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🏡", style = MaterialTheme.typography.displayLarge); Text("Aucune planche", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text("Cliquez sur + pour créer", color = CouleursApp.TexteFonce)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { LegendeCouleurs() }
                items(planches, key = { it.id }) { planche ->
                    PlancheCard(
                        planche = planche,
                        isExpanded = expandedPlancheId == planche.id,
                        onToggleExpand = { expandedPlancheId = if (expandedPlancheId == planche.id) null else planche.id },
                        onDelete = { scope.launch { jardinRepository.supprimerPlanche(planche) } },
                        jardinRepository = jardinRepository,
                        legumes = legumes,
                        onSousCarreClick = { carre, caseNumero -> 
                            selectedCarre = carre
                            selectedCaseNumero = caseNumero
                            currentPlancheId = planche.id
                            if (caseNumero == 5) {
                                showChoixRemplissage = true
                            } else {
                                remplirM2Mode = false
                                showLegumeSelection = true
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showChoixRemplissage && selectedCarre != null) {
        AlertDialog(
            onDismissRequest = { showChoixRemplissage = false },
            title = { Text("Case centrale", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("Que souhaitez-vous faire avec cette case centrale ?", style = MaterialTheme.typography.bodyMedium)
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
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌱", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Remplir tout le m²", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
                            Text("Les 9 cases avec la même plante", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
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
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌿", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Remplir juste cette case", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                            Text("Seulement la case centrale", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                        }
                    }
                }
            } },
            confirmButton = { TextButton(onClick = { showChoixRemplissage = false }) { Text("Annuler", color = CouleursApp.VertPrincipal) } }
        )
    }
    
    if (showAddPlancheDialog) {
        var nom by remember { mutableStateOf("") }; var largeur by remember { mutableStateOf("3") }; var longueur by remember { mutableStateOf("4") }
        AlertDialog(onDismissRequest = { showAddPlancheDialog = false }, title = { Text("Nouvelle planche", fontWeight = FontWeight.Bold) },
            text = { Column {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom de la planche") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = largeur, onValueChange = { largeur = it }, label = { Text("Largeur (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                    Text("×", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(value = longueur, onValueChange = { longueur = it }, label = { Text("Longueur (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("💡 Les distances de plantation sont automatiquement respectées selon la densité de chaque plante.", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal)
            } },
            confirmButton = { Button(onClick = { val l = largeur.toIntOrNull() ?: 1; val L = longueur.toIntOrNull() ?: 1; if (l > 0 && L > 0 && nom.isNotBlank()) { scope.launch { jardinRepository.ajouterPlanche(nom, l, L) }; showAddPlancheDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Créer") } },
            dismissButton = { TextButton(onClick = { showAddPlancheDialog = false }) { Text("Annuler") } })
    }
    
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!; val caseNumero = selectedCaseNumero
        var selectedCategorie by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val categories = legumes.groupBy { it.categorie }.keys.toList()
        AlertDialog(onDismissRequest = { showLegumeSelection = false }, title = { Text(if (remplirM2Mode) "Remplir le m² entier" else "Choisissez une plante", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("Case $caseNumero du carré", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                if (!remplirM2Mode) {
                    Text("🗑️ Vider la case", modifier = Modifier.fillMaxWidth().clickable { scope.launch { jardinRepository.modifierCasePrecise(carre, caseNumero, null, currentPlancheId) }; showLegumeSelection = false }.padding(12.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("🔍 Rechercher...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                if (searchQuery.isEmpty() && selectedCategorie == null) {
                    Column(modifier = Modifier.fillMaxWidth().height(150.dp).verticalScroll(rememberScrollState())) {
                        categories.forEach { c -> 
                            FilterChip(
                                selected = selectedCategorie == c, 
                                onClick = { selectedCategorie = if (selectedCategorie == c) null else c }, 
                                label = { Text("${getEmojiCategorie(c)} $c", fontSize = MaterialTheme.typography.bodySmall.fontSize) }, 
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), 
                                shape = RoundedCornerShape(16.dp)
                            ) 
                        }
                    }
                }
                if (searchQuery.isNotEmpty() || selectedCategorie != null) {
                    val plantes = legumes.filter { (searchQuery.isEmpty() || it.nom.contains(searchQuery, true)) && (selectedCategorie == null || it.categorie == selectedCategorie) }
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) { 
                        items(plantes) { legume ->
                            Text(
                                text = "${legume.nom} (${getDistanceEntrePlants(legume)} cm)", 
                                modifier = Modifier.fillMaxWidth().clickable {
                                    scope.launch {
                                        if (!jardinRepository.peutPlanterDansCase(carre, caseNumero, legume.nom)) { 
                                            android.widget.Toast.makeText(context, "${legume.nom} est trop volumineux pour cette zone", android.widget.Toast.LENGTH_LONG).show()
                                            showLegumeSelection = false 
                                        } else {
                                            val associations = jardinRepository.verifierAssociationsAdjacentes(carre, caseNumero, legume.nom)
                                            val mauvaiseAssoc = associations.filter { it.second == "mauvaise" }
                                            if (mauvaiseAssoc.isNotEmpty()) {
                                                selectedLegumeNom = legume.nom
                                                avertissement = AvertissementRotation(
                                                    niveau = NiveauRisque.MOYEN,
                                                    message = "⚠️ Mauvaise association avec : ${mauvaiseAssoc.joinToString(", ") { it.first }}"
                                                )
                                                showAvertissement = true
                                                showLegumeSelection = false
                                            } else {
                                                selectedLegumeNom = legume.nom
                                                showVarieteSelection = true
                                                showLegumeSelection = false
                                            }
                                        }
                                    }
                                }.padding(14.dp), 
                                style = MaterialTheme.typography.bodyLarge,
                                color = CouleursApp.TexteFonce
                            )
                            HorizontalDivider()
                        } 
                    }
                }
            } },
            confirmButton = { TextButton(onClick = { showLegumeSelection = false }) { Text("Annuler") } })
    }
    
    if (showVarieteSelection && selectedLegumeNom != null && selectedCarre != null) {
        val carre = selectedCarre!!
        val caseNumero = selectedCaseNumero
        val modeM2 = remplirM2Mode
        val plancheId = currentPlancheId
        VarieteSelectionDialog(
            legumeNom = selectedLegumeNom!!,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
                scope.launch {
                    if (modeM2) {
                        jardinRepository.remplirM2Entier(carre, nomComplet, plancheId)
                    } else {
                        jardinRepository.modifierCasePrecise(carre, caseNumero, nomComplet, plancheId)
                    }
                }
                showVarieteSelection = false
                selectedLegumeNom = null
                selectedCarre = null
                selectedCaseNumero = 0
                remplirM2Mode = false
            },
            onDismiss = { 
                showVarieteSelection = false
                selectedLegumeNom = null
                selectedCarre = null
                selectedCaseNumero = 0
                remplirM2Mode = false
            }
        )
    }
    
    if (showAvertissement && avertissement != null) {
        val av = avertissement!!; val carre = selectedCarre; val caseNumero = selectedCaseNumero; val legumeNom = selectedLegumeNom
        AlertDialog(onDismissRequest = { showAvertissement = false; avertissement = null }, title = { Text("Avertissement", fontWeight = FontWeight.Bold) },
            text = { Text(av.message) },
            confirmButton = { Button(onClick = { 
                if (carre != null && caseNumero > 0 && legumeNom != null) {
                    scope.launch { 
                        jardinRepository.modifierCasePrecise(carre, caseNumero, legumeNom, currentPlancheId)
                    }
                }
                showAvertissement = false
                avertissement = null
                selectedLegumeNom = null
                selectedCarre = null
                selectedCaseNumero = 0
            }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Planter quand même") } },
            dismissButton = { TextButton(onClick = { 
                showAvertissement = false
                avertissement = null
                selectedLegumeNom = null
                selectedCarre = null
                selectedCaseNumero = 0
            }) { Text("Annuler") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseSolScreen(onBack: () -> Unit) {
    var argile by remember { mutableStateOf("") }
    var sable by remember { mutableStateOf("") }
    var limon by remember { mutableStateOf("") }
    var typeSol by remember { mutableStateOf("") }
    fun calculer() { val a = argile.toIntOrNull() ?: 0; val s = sable.toIntOrNull() ?: 0; val l = limon.toIntOrNull() ?: 0; typeSol = if (a + s + l == 100) when { a > 40 -> "Sol argileux"; s > 70 -> "Sol sableux"; l > 50 -> "Sol limoneux"; else -> "Sol équilibré" } else "Total = ${a + s + l}% (doit faire 100%)" }
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Analyse du sol 🔬", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { OutlinedTextField(value = argile, onValueChange = { argile = it }, label = { Text("Argile (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(value = sable, onValueChange = { sable = it }, label = { Text("Sable (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(value = limon, onValueChange = { limon = it }, label = { Text("Limon (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { Button(onClick = { calculer() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Analyser") } }
            if (typeSol.isNotEmpty()) item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)) { Text(typeSol, modifier = Modifier.padding(20.dp), fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce) } }
        }
    }
}

// ============== CALENDRIER ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendrierScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val rappelCulturelRepository = remember { RappelCulturelRepository(context) }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val meteoRepository = remember { MeteoRepository() }
    val luneRepository = remember { LuneRepository() }
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("Paris") }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var selectedTimestamp by remember { mutableStateOf(0L) }
    val rappelRepository = remember { RappelRepository(context) }
    var rappelActif by remember { mutableStateOf(false) }
    var rappelNote by remember { mutableStateOf("") }
    var rappelHeure by remember { mutableStateOf(9) }
    var rappelMinute by remember { mutableStateOf(0) }
    
    var rappelsCulturelsDuMois by remember { mutableStateOf<List<RappelCulturelEntity>>(emptyList()) }
    var showOperationsDialog by remember { mutableStateOf(false) }
    var operationsDuJour by remember { mutableStateOf<List<RappelCulturelEntity>>(emptyList()) }
    var showDetailOperationDialog by remember { mutableStateOf(false) }
    var operationSelectionnee by remember { mutableStateOf<RappelCulturelEntity?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis() }
    LaunchedEffect(Unit) { try { meteo = meteoRepository.getMeteo(ville) } catch (e: Exception) {} }
    
    LaunchedEffect(currentMonth, currentYear) {
        val calDebut = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calFin = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
            add(Calendar.DAY_OF_MONTH, 42)
        }
        rappelsCulturelsDuMois = rappelCulturelRepository.getRappelsEntreDates(calDebut.timeInMillis, calFin.timeInMillis)
    }
    
    val moisNoms = listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Calendrier 📅", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { MeteoCard(meteo, ville, true, phaseLune) }
            
            item { 
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth-- }) { Text("◀") }
                            Text("${moisNoms[currentMonth]} $currentYear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = CouleursApp.TexteFonce)
                            TextButton(onClick = { if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++ }) { Text("▶") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) { 
                            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { 
                                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = CouleursApp.VertPrincipal) 
                            } 
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val cal = Calendar.getInstance()
                        cal.set(currentYear, currentMonth, 1)
                        val firstDay = cal.get(Calendar.DAY_OF_WEEK)
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2
                        val nombreSemaines = (offset + daysInMonth + 6) / 7
                        
                        for (semaine in 0 until nombreSemaines) {
                            SemaineCalendrier(
                                semaine = semaine,
                                offset = offset,
                                daysInMonth = daysInMonth,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                selectedDay = selectedDay,
                                rappelsCulturels = rappelsCulturelsDuMois,
                                onDayClick = { dayNumber ->
                                    selectedDay = dayNumber
                                    val calJour = Calendar.getInstance()
                                    calJour.set(currentYear, currentMonth, dayNumber, rappelHeure, rappelMinute, 0)
                                    selectedTimestamp = calJour.timeInMillis
                                    
                                    val debutJour = Calendar.getInstance().apply {
                                        timeInMillis = calJour.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    val finJour = debutJour + 24L * 60 * 60 * 1000 - 1
                                    
                                    val ops = rappelsCulturelsDuMois.filter { rappel ->
                                        rappel.dateDebut <= finJour && rappel.dateFin >= debutJour
                                    }
                                    
                                    operationsDuJour = ops
                                    
                                    val rappel = rappelRepository.getRappelSync(selectedTimestamp)
                                    rappelActif = rappel?.estActif ?: false
                                    rappelNote = rappel?.note ?: ""
                                    showOperationsDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🌱 Les barres ←→ indiquent les opérations culturales automatiques", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontStyle = FontStyle.Italic)
                    }
                }
            }
            
            item { LegendeOperations() }
        }
    }
    
    if (showOperationsDialog) {
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        val dateAffichee = Date(selectedTimestamp)
        
        AlertDialog(
            onDismissRequest = { showOperationsDialog = false },
            title = { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                    Text("📅 ${dateFormat.format(dateAffichee)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { 
                        scope.launch { 
                            val cal = Calendar.getInstance()
                            cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0)
                            rappelRepository.toggleRappel(cal.timeInMillis, "Rappel")
                            rappelActif = !rappelActif
                        } 
                    }) { 
                        Text(if (rappelActif) "🔔" else "🔕", style = MaterialTheme.typography.titleLarge) 
                    } 
                } 
            },
            text = { 
                Column {
                    if (operationsDuJour.isNotEmpty()) {
                        Text("🌱 Opérations culturales :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
                        Spacer(modifier = Modifier.height(8.dp))
                        operationsDuJour.forEach { op ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    operationSelectionnee = op
                                    showDetailOperationDialog = true
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(android.graphics.Color.parseColor(op.couleurHex)).copy(alpha = if (CouleursApp.isDarkMode) 0.30f else 0.15f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(op.emoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(op.typeOperation, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                                        Text("${op.legumeNom} - Case ${op.caseNumero}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal)
                                        if (op.estTermine) {
                                            Text("✅ Terminé", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text("📝 Note personnelle :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(modifier = Modifier.fillMaxWidth().clickable { 
                        val tp = TimePickerDialog(envelopperAvecTheme(context), { _, h, m -> rappelHeure = h; rappelMinute = m }, rappelHeure, rappelMinute, true)
                        tp.show()
                    }, colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale), shape = RoundedCornerShape(16.dp)) { 
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
                            Text("⏰", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column { 
                                Text("Heure du rappel", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                                Text("${String.format("%02d", rappelHeure)}:${String.format("%02d", rappelMinute)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = CouleursApp.TexteFonce) 
                            } 
                        } 
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = rappelNote, onValueChange = { rappelNote = it }, label = { Text("Note (optionnel)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), minLines = 2)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { 
                        scope.launch { 
                            val cal = Calendar.getInstance()
                            cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0)
                            val ts = cal.timeInMillis
                            val r = rappelRepository.getRappel(ts)
                            if (r != null) rappelRepository.mettreAJourNote(ts, rappelNote) else rappelRepository.ajouterRappel(ts, "Rappel", rappelNote)
                        }
                        showOperationsDialog = false
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { 
                        Text("Enregistrer la note") 
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { showOperationsDialog = false }) { 
                    Text("Fermer", color = CouleursApp.VertPrincipal) 
                } 
            }
        )
    }
    
    if (showDetailOperationDialog && operationSelectionnee != null) {
        val op = operationSelectionnee!!
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
        
        AlertDialog(
            onDismissRequest = { showDetailOperationDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(op.emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(op.typeOperation, fontWeight = FontWeight.Bold)
                        Text(op.legumeNom, style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal)
                    }
                }
            },
            text = { 
                Column {
                    Text(op.description, style = MaterialTheme.typography.bodyMedium)
                    if (op.conseil.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💡 Conseil", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(op.conseil, style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📅 Période : ${dateFormat.format(Date(op.dateDebut))} → ${dateFormat.format(Date(op.dateFin))}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                    Text("📍 Case ${op.caseNumero}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                    if (op.estTermine) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✅ Marqué comme terminé", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    scope.launch {
                        if (op.estTermine) {
                            rappelCulturelRepository.marquerNonTermine(op.id)
                        } else {
                            rappelCulturelRepository.marquerTermine(op.id)
                        }
                        val calDebut = Calendar.getInstance().apply {
                            set(currentYear, currentMonth, 1, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val calFin = Calendar.getInstance().apply {
                            set(currentYear, currentMonth, 1, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                            add(Calendar.DAY_OF_MONTH, 42)
                        }
                        rappelsCulturelsDuMois = rappelCulturelRepository.getRappelsEntreDates(calDebut.timeInMillis, calFin.timeInMillis)
                    }
                    showDetailOperationDialog = false
                    showOperationsDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { 
                    Text(if (op.estTermine) "Annuler" else "Marquer comme fait") 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { showDetailOperationDialog = false }) { 
                    Text("Fermer", color = CouleursApp.VertPrincipal) 
                } 
            }
        )
    }
}

@Composable
fun SemaineCalendrier(
    semaine: Int,
    offset: Int,
    daysInMonth: Int,
    currentYear: Int,
    currentMonth: Int,
    selectedDay: Int,
    rappelsCulturels: List<RappelCulturelEntity>,
    onDayClick: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (col in 0..6) {
            val dayNumber = semaine * 7 + col - offset + 1
            if (dayNumber in 1..daysInMonth) {
                val isSelected = dayNumber == selectedDay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.2f)
                        .background(if (isSelected) CouleursApp.VertPrincipal else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { onDayClick(dayNumber) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$dayNumber",
                        color = if (isSelected) Color.White else CouleursApp.TexteFonce,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).aspectRatio(1.2f))
            }
        }
    }
    
    val debutSemaine = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_MONTH, semaine * 7 - offset)
    }.timeInMillis
    val finSemaine = debutSemaine + 7L * 24 * 60 * 60 * 1000 - 1
    
    val rappelsSemaine = rappelsCulturels.filter { rappel ->
        rappel.dateDebut <= finSemaine && rappel.dateFin >= debutSemaine
    }
    
    if (rappelsSemaine.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        val rappelsTries = rappelsSemaine.sortedBy { it.dateDebut }
        val rappelsAAfficher = rappelsTries.take(2)
        val nombreEnPlus = (rappelsTries.size - 2).coerceAtLeast(0)
        
        rappelsAAfficher.forEach { rappel ->
            BarreOperation(rappel = rappel, debutSemaine = debutSemaine, finSemaine = finSemaine)
            Spacer(modifier = Modifier.height(2.dp))
        }
        
        if (nombreEnPlus > 0) {
            Text(
                "+$nombreEnPlus autre(s)...",
                style = MaterialTheme.typography.labelSmall,
                color = CouleursApp.VertPrincipal,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun BarreOperation(
    rappel: RappelCulturelEntity,
    debutSemaine: Long,
    finSemaine: Long
) {
    val MILLIS_PAR_JOUR = 24L * 60 * 60 * 1000
    
    val debutAffiche = maxOf(rappel.dateDebut, debutSemaine)
    val finAffiche = minOf(rappel.dateFin, finSemaine)
    
    val jourDebutSemaine = ((debutAffiche - debutSemaine) / MILLIS_PAR_JOUR).toInt().coerceIn(0, 6)
    val jourFinSemaine = ((finAffiche - debutSemaine) / MILLIS_PAR_JOUR).toInt().coerceIn(0, 6)
    
    val nombreJoursCouverts = jourFinSemaine - jourDebutSemaine + 1
    val estTermine = rappel.estTermine
    val estEnRetard = !estTermine && rappel.dateFin < System.currentTimeMillis()
    
    val couleurBase = try {
        Color(android.graphics.Color.parseColor(rappel.couleurHex))
    } catch (e: Exception) {
        Color(0xFFFFA726)
    }
    
    val couleurAffichee = when {
        estTermine -> couleurBase.copy(alpha = 0.25f)
        estEnRetard -> Color(0xFFE53935).copy(alpha = 0.7f)
        else -> couleurBase.copy(alpha = 0.75f)
    }
    
    Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
        if (jourDebutSemaine > 0) {
            Spacer(modifier = Modifier.weight(jourDebutSemaine.toFloat()))
        }
        
        Box(
            modifier = Modifier
                .weight(nombreJoursCouverts.toFloat())
                .fillMaxHeight()
                .padding(horizontal = 1.dp)
                .background(couleurAffichee, RoundedCornerShape(4.dp))
                .border(0.5.dp, couleurBase.copy(alpha = 0.9f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "${rappel.emoji} ${rappel.typeOperation}",
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (jourFinSemaine < 6) {
            Spacer(modifier = Modifier.weight((6 - jourFinSemaine).toFloat()))
        }
    }
}

@Composable
fun LegendeOperations() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Types d'opérations", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF66BB6A))); 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plantation, semis, repiquage", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(16.dp).background(Color(0xFFFFA726))); 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entretien (tuteurage, buttage, paillage)", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(16.dp).background(Color(0xFFAB47BC))); 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Taille, effeuillage, pincement", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(16.dp).background(Color(0xFFEF5350))); 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Traitement, surveillance maladies", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF42A5F5))); 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Récolte, arrêt arrosage", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
        }
    }
}

@Composable
fun MeteoCard(meteo: MeteoData?, ville: String, estConnecte: Boolean, phaseLune: PhaseLune? = null) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("🌦️ Météo à $ville", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
            if (phaseLune != null) Text("${phaseLune.emoji} ${phaseLune.nom}", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (meteo != null) { Text("🌡️ ${meteo.temperature}°C", color = CouleursApp.TexteFonce); Text("☁️ ${meteo.description}", color = CouleursApp.TexteFonce); Text("💧 ${meteo.humidite}%", color = CouleursApp.TexteFonce); Text("🌬️ ${meteo.vent} m/s", color = CouleursApp.TexteFonce) }
            else Text("Météo indisponible", color = CouleursApp.TexteFonce)
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
    var showAide by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { repository.ajouterLegumesPredefinis() }
    val methodes = listOf("Tous", "Séchage", "Lactofermentation", "Conserves", "Congélation")
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Conservation 🥫", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, actions = { IconButton(onClick = { showAide = true }) { Icon(Icons.Default.Help, "Aide", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Filtrer par méthode :", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Column { methodes.forEach { m -> FilterChip(selected = filtre == m, onClick = { filtre = m }, label = { Text(m) }, modifier = Modifier.padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp)) } } }
            item { Text("${legumes.filter { if (filtre == "Tous") true else it.conservation.contains(filtre, true) }.size} plantes", color = CouleursApp.TexteFonce) }
            legumes.filter { if (filtre == "Tous") true else it.conservation.contains(filtre, true) }.forEach { legume -> item { ConservationCard(legume) } }
        }
    }
    if (showAide) AideConservationDialog(onDismiss = { showAide = false })
}

@Composable
fun AideConservationDialog(onDismiss: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("sechage") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📖 Guide de conservation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = when(selectedOnglet) { "sechage" -> 0; "lacto" -> 1; "conserves" -> 2; else -> 3 }, containerColor = CouleursApp.VertPale, contentColor = CouleursApp.VertPrincipal) {
                    Tab(selected = selectedOnglet == "sechage", onClick = { selectedOnglet = "sechage" }, text = { Text("🌬️ Séchage", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "lacto", onClick = { selectedOnglet = "lacto" }, text = { Text("🥬 Lacto", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "conserves", onClick = { selectedOnglet = "conserves" }, text = { Text("🫙 Conserves", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "congelation", onClick = { selectedOnglet = "congelation" }, text = { Text("❄️ Congélation", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (selectedOnglet) {
                        "sechage" -> {
                            item { Text("🌬️ Séchage optimal", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Choisissez des légumes frais et sains\n• Lavez et séchez soigneusement\n• Coupez en tranches fines et régulières (3-5mm)\n• Blanchissez les légumes durs (carottes, haricots) 2-3 min\n• Disposez sans chevauchement sur les plateaux\n• Température idéale : 50-60°C\n• Durée : 6-12h selon l'épaisseur\n• Les légumes doivent être cassants et croquants\n• Stockez dans des bocaux hermétiques à l'abri de la lumière\n• Conservation : 6-12 mois", color = CouleursApp.TexteFonce) }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Tomates, champignons, carottes, courgettes, oignons, poivrons, herbes aromatiques, haricots verts", color = CouleursApp.TexteFonce) }
                        }
                        "lacto" -> {
                            item { Text("🥬 Lactofermentation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Utilisez du sel sans iode (sel de mer)\n• Proportion : 2-3% de sel (20-30g par litre d'eau)\n• Coupez les légumes en morceaux réguliers\n• Tassez bien pour éliminer les bulles d'air\n• Les légumes doivent être immergés sous la saumure\n• Utilisez un poids pour maintenir sous l'eau\n• Laissez fermenter à température ambiante (18-22°C)\n• Durée : 1-4 semaines selon le goût\n• Goûtez régulièrement\n• Une fois ouvert, conservez au réfrigérateur", color = CouleursApp.TexteFonce) }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Choux (choucroute), carottes, radis, concombres (pickles), haricots verts, betteraves, navets", color = CouleursApp.TexteFonce) }
                        }
                        "conserves" -> {
                            item { Text("🫙 Conserves (stérilisation)", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Stérilisez les bocaux et couvercles à l'eau bouillante\n• Utilisez des légumes très frais\n• Remplissez les bocaux en laissant 2cm de vide\n• Ajoutez de l'eau salée bouillante (20g sel/litre)\n• Fermez hermétiquement\n• Stérilisez à 100°C pendant 1h-1h30\n• Vérifiez l'étanchéité après refroidissement\n• Le couvercle doit être bombé vers l'intérieur\n• Stockez dans un endroit frais et sombre\n• Conservation : 1-2 ans", color = CouleursApp.TexteFonce) }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Tomates, haricots verts, petits pois, carottes, betteraves, ratatouille, coulis de tomate", color = CouleursApp.TexteFonce) }
                        }
                        "congelation" -> {
                            item { Text("❄️ Congélation optimale", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Choisissez des légumes très frais\n• Lavez et séchez soigneusement\n• Blanchissez la plupart des légumes 2-3 min\n• Refroidissez immédiatement dans l'eau glacée\n• Égouttez bien avant de congeler\n• Disposez à plat pour éviter les blocs\n• Utilisez des sacs de congélation sans air\n• Étiquetez avec le nom et la date\n• Température idéale : -18°C ou moins\n• Ne recongelez jamais un produit décongelé\n• Conservation : 8-12 mois", color = CouleursApp.TexteFonce) }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Haricots verts, petits pois, carottes, courgettes, poivrons, épinards, brocolis, choux-fleurs", color = CouleursApp.TexteFonce) }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Fermer") } }
    )
}

@Composable
fun ConservationCard(legume: LegumeEntity) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleLarge)
            Text(legume.conservation, color = CouleursApp.TexteFonce)
        }
    }
}

// ============== FICHE DÉTAILLÉE ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegumeDetailScreen(legume: LegumeEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    val varieteRepository = remember { VarieteRepository(context) }
    val varietes by varieteRepository.getVarietesForLegume(legume.nom).collectAsState(initial = emptyList())
    var selectedVariete by remember { mutableStateOf<VarieteEntity?>(null) }
    LaunchedEffect(Unit) { varieteRepository.ajouterVarietesPredefinies() }
    
    if (selectedVariete != null) VarieteDetailScreen(selectedVariete!!, { selectedVariete = null })
    else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = { TopAppBar(title = { Text(legume.nom, fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(24.dp)).background(CouleursApp.VertPale), contentAlignment = Alignment.Center) { Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayLarge) } }
                item { InfoCard("Catégorie", legume.categorie) }
                if (legume.estVivace) item { InfoCard("Type", "🌿 Plante vivace") }
                if (legume.estFleur) item { InfoCard("Type", "🌸 Fleur") }
                item { InfoCard("Difficulté", legume.difficulte) }
                item { InfoCard("Exposition", legume.exposition) }
                item { InfoCard("Arrosage", legume.arrosage) }
                item { InfoCard("Semis", legume.semis) }
                item { InfoCard("Plantation", legume.plantation) }
                item { InfoCard("Récolte", legume.recolte) }
                item { InfoCard("Conservation", legume.conservation) }
                if (varietes.isNotEmpty()) {
                    item { Text("🌱 Variétés (${varietes.size}) :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }
                    items(varietes) { v -> Card(modifier = Modifier.fillMaxWidth().clickable { selectedVariete = v }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) { Text("🌿 ${v.nom}", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarieteDetailScreen(variete: VarieteEntity, onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text(variete.nom, fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = Color.White)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { InfoCard("Description", variete.description) }
            item { InfoCard("Particularités", variete.particularites) }
        }
    }
}

// ============== COMPOSANTS ==============
@Composable
fun LegumeCard(legume: LegumeEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                Text("${legume.categorie} - ${legume.difficulte}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun VarieteSelectionDialog(legumeNom: String, varieteRepository: VarieteRepository, onVarieteChoisie: (String) -> Unit, onDismiss: () -> Unit) {
    val varietes by varieteRepository.getVarietesForLegume(legumeNom).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { varieteRepository.ajouterVarietesPredefinies() } catch (e: Exception) {} } }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Variétés de $legumeNom", fontWeight = FontWeight.Bold) },
        text = { LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item { Text("🌱 Variété standard", modifier = Modifier.fillMaxWidth().clickable { onVarieteChoisie(legumeNom) }.padding(16.dp), fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); HorizontalDivider() }
            if (varietes.isEmpty()) item { Text("Chargement...", modifier = Modifier.padding(16.dp), color = CouleursApp.TexteFonce) }
            else items(varietes) { v -> Text("🌿 ${v.nom}", modifier = Modifier.fillMaxWidth().clickable { onVarieteChoisie("${legumeNom} (${v.nom})") }.padding(16.dp), color = CouleursApp.TexteFonce); Text(v.description, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce); HorizontalDivider() }
        } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun PlancheCard(
    planche: PlancheEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    jardinRepository: JardinRepository,
    legumes: List<LegumeEntity>,
    onSousCarreClick: (CarreEntity, Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) { Text(planche.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text("${planche.largeur}m × ${planche.longueur}m", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                val carres by jardinRepository.getCarresForPlanche(planche.id).collectAsState(initial = emptyList())
                
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                val state = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(0.5f, 5f)
                    offset += panChange
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
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (x in 0 until planche.largeur) {
                                    val carre = carres.find { it.positionX == x && it.positionY == y }
                                    if (carre != null) {
                                        val couleurs = calculerCouleursCarre(
                                            carre = carre,
                                            planche = planche,
                                            tousLesCarres = carres,
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
                            onClick = { scale = 1f; offset = Offset.Zero },
                            containerColor = CouleursApp.VertPrincipal,
                            contentColor = Color.White,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) { Text("↺", style = MaterialTheme.typography.titleMedium) }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("🔍 Pincez à deux doigts pour zoomer", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal.copy(alpha = 0.7f), fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun calculerCouleursCarre(
    carre: CarreEntity,
    planche: PlancheEntity,
    tousLesCarres: List<CarreEntity>,
    legumes: List<LegumeEntity>
): Map<Int, Color> {
    val resultat = mutableMapOf<Int, Color>()
    
    fun nomBase(nom: String?): String? {
        if (nom == null) return null
        return if (nom.contains("(")) nom.substringBefore("(").trim() else nom
    }
    
    fun infosLegume(nom: String?): LegumeEntity? {
        val base = nomBase(nom) ?: return null
        return legumes.find { it.nom == base }
    }
    
    fun verifierAssociation(plante1: String?, plante2: String?): String {
        if (plante1 == null || plante2 == null) return "neutre"
        val leg1 = infosLegume(plante1) ?: return "neutre"
        val base2 = nomBase(plante2) ?: return "neutre"
        if (leg1.bonnesAssociations.contains(base2, true)) return "bonne"
        if (leg1.mauvaisesAssociations.contains(base2, true)) return "mauvaise"
        return "neutre"
    }
    
    fun planteDansCase(c: CarreEntity, num: Int): String? = when (num) {
        1 -> c.case1; 2 -> c.case2; 3 -> c.case3
        4 -> c.case4; 5 -> c.case5; 6 -> c.case6
        7 -> c.case7; 8 -> c.case8; 9 -> c.case9
        else -> null
    }
    
    fun casesVoisinesMemeCarre(num: Int): List<Int> = when (num) {
        1 -> listOf(2, 4, 5)
        2 -> listOf(1, 3, 4, 5, 6)
        3 -> listOf(2, 5, 6)
        4 -> listOf(1, 2, 5, 7, 8)
        5 -> listOf(1, 2, 3, 4, 6, 7, 8, 9)
        6 -> listOf(2, 3, 5, 8, 9)
        7 -> listOf(4, 5, 8)
        8 -> listOf(4, 5, 6, 7, 9)
        9 -> listOf(5, 6, 8)
        else -> emptyList()
    }
    
    for (num in 1..9) {
        val plante = planteDansCase(carre, num)
        if (plante == null) {
            resultat[num] = CouleursApp.CaseVide
            continue
        }
        
        val plantesVoisines = mutableListOf<String>()
        
        casesVoisinesMemeCarre(num).forEach { voisinNum ->
            val planteVoisine = planteDansCase(carre, voisinNum)
            if (planteVoisine != null) plantesVoisines.add(planteVoisine)
        }
        
        val row = (num - 1) / 3
        val col = (num - 1) % 3
        
        if (col == 0) {
            val carreGauche = tousLesCarres.find { it.positionX == carre.positionX - 1 && it.positionY == carre.positionY }
            if (carreGauche != null) {
                val planteVoisine = planteDansCase(carreGauche, row * 3 + 3)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (col == 2) {
            val carreDroite = tousLesCarres.find { it.positionX == carre.positionX + 1 && it.positionY == carre.positionY }
            if (carreDroite != null) {
                val planteVoisine = planteDansCase(carreDroite, row * 3 + 1)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (row == 0) {
            val carreHaut = tousLesCarres.find { it.positionX == carre.positionX && it.positionY == carre.positionY - 1 }
            if (carreHaut != null) {
                val planteVoisine = planteDansCase(carreHaut, 7 + col)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (row == 2) {
            val carreBas = tousLesCarres.find { it.positionX == carre.positionX && it.positionY == carre.positionY + 1 }
            if (carreBas != null) {
                val planteVoisine = planteDansCase(carreBas, 1 + col)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        
        var aBonne = false
        var aMauvaise = false
        plantesVoisines.forEach { voisine ->
            when (verifierAssociation(plante, voisine)) {
                "bonne" -> aBonne = true
                "mauvaise" -> aMauvaise = true
            }
        }
        
        resultat[num] = when {
            aMauvaise -> CouleursApp.MauvaiseAssociation
            aBonne -> CouleursApp.BonneAssociation
            else -> CouleursApp.NeutreAssociation
        }
    }
    
    return resultat
}

fun calculerCouleurPire(couleurs: Map<Int, Color>): Color {
    var aMauvaise = false
    var aBonne = false
    var aNeutre = false
    
    for (num in 1..9) {
        val couleur = couleurs[num] ?: continue
        when (couleur) {
            CouleursApp.MauvaiseAssociation -> aMauvaise = true
            CouleursApp.BonneAssociation -> aBonne = true
            CouleursApp.NeutreAssociation -> aNeutre = true
        }
    }
    
    return when {
        aMauvaise -> CouleursApp.MauvaiseAssociation
        aBonne -> CouleursApp.BonneAssociation
        aNeutre -> CouleursApp.NeutreAssociation
        else -> CouleursApp.CaseVide
    }
}

@Composable
fun Grille3x3(
    carre: CarreEntity,
    couleurs: Map<Int, Color>,
    onSousCarreClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val legumes = listOfNotNull(carre.case1, carre.case2, carre.case3, carre.case4, carre.case5, carre.case6, carre.case7, carre.case8, carre.case9)
    
    if (legumes.size == 9 && legumes.distinct().size == 1) {
        val couleurGrandCarre = calculerCouleurPire(couleurs)
        
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(couleurGrandCarre)
                .border(2.dp, CouleursApp.VertPrincipal)
                .clickable { onSousCarreClick(1) },
            contentAlignment = Alignment.Center
        ) {
            Text(legumes[0], fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(2.dp), color = CouleursApp.TexteFonce)
        }
    } else {
        Column(modifier = modifier.aspectRatio(1f).border(2.dp, CouleursApp.VertPrincipal)) {
            for (row in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..2) {
                        val caseNumero = row * 3 + col + 1
                        val legume = when (caseNumero) {
                            1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3
                            4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6
                            7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9
                            else -> null
                        }
                        
                        val backgroundColor = couleurs[caseNumero] ?: if (legume != null) CouleursApp.NeutreAssociation else CouleursApp.CaseVide
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(backgroundColor)
                                .border(1.dp, CouleursApp.VertPrincipal)
                                .clickable { onSousCarreClick(caseNumero) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(legume ?: "", fontSize = MaterialTheme.typography.bodySmall.fontSize, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(2.dp), color = CouleursApp.TexteFonce)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendeCouleurs() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Légende des couleurs", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(20.dp).background(CouleursApp.BonneAssociation).border(1.dp, CouleursApp.VertPrincipal)); 
                Spacer(modifier = Modifier.width(8.dp))
                Text(" Bonne association", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(20.dp).background(CouleursApp.NeutreAssociation).border(1.dp, CouleursApp.VertPrincipal)); 
                Spacer(modifier = Modifier.width(8.dp))
                Text(" Association neutre", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.size(20.dp).background(CouleursApp.MauvaiseAssociation).border(1.dp, CouleursApp.VertPrincipal)); 
                Spacer(modifier = Modifier.width(8.dp))
                Text(" Mauvaise association", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce) 
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("🌱 Les couleurs tiennent compte des carrés voisins (m² adjacents)", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("🔍 Pincez à deux doigts sur une planche pour zoomer", style = MaterialTheme.typography.bodySmall, color = CouleursApp.Terracotta, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("👆 Case centrale : choisir entre remplir tout le m² ou une seule case", style = MaterialTheme.typography.bodySmall, color = CouleursApp.Terracotta)
        }
    }
}

fun getEmojiCategorie(categorie: String): String = when {
    categorie.contains("Racine", true) -> "🥕"
    categorie.contains("Tubercule", true) -> "🥔"
    categorie.contains("Fruit", true) -> "🍅"
    categorie.contains("Feuille", true) -> "🥬"
    categorie.contains("Légumineuse", true) -> "🫘"
    categorie.contains("Alliacé", true) -> "🧅"
    categorie.contains("Chou", true) -> "🥦"
    categorie.contains("Cucurbitacée", true) -> "🎃"
    categorie.contains("Fleur", true) -> "🌸"
    categorie.contains("Aromatique", true) -> "🌿"
    else -> "🌱"
}

@Composable
fun InfoCard(titre: String, contenu: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(contenu, color = CouleursApp.TexteFonce)
        }
    }
}

fun estPlanteVolumineuse(nomLegume: String): Boolean = nomLegume in listOf("Tomate", "Courgette", "Potiron", "Courge", "Aubergine", "Poivron", "Concombre", "Melon", "Chou pommé", "Brocoli", "Chou-fleur", "Topinambour")

fun peutPlanterIci(carre: CarreEntity, caseNumero: Int, legumeNom: String): Boolean {
    if (!estPlanteVolumineuse(legumeNom)) return true
    val adj = when (caseNumero) {
        1 -> listOf(2, 4, 5); 2 -> listOf(1, 3, 4, 5, 6); 3 -> listOf(2, 5, 6)
        4 -> listOf(1, 2, 5, 7, 8); 5 -> listOf(1, 2, 3, 4, 6, 7, 8, 9); 6 -> listOf(2, 3, 5, 8, 9)
        7 -> listOf(4, 5, 8); 8 -> listOf(4, 5, 6, 7, 9); 9 -> listOf(5, 6, 8)
        else -> emptyList()
    }
    val legumesAdj = adj.mapNotNull { when (it) { 1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3; 4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6; 7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9; else -> null } }
    return !legumesAdj.any { it != null && estPlanteVolumineuse(it) }
}

fun getDensiteFromPlantation(legume: LegumeEntity): Int {
    val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) plants/m²").find(legume.plantation)
    return if (match != null) {
        val valeur = match.groupValues[1]
        when {
            valeur.contains(",") -> valeur.replace(",", ".").toDouble().toInt()
            valeur.contains("-") -> { val parts = valeur.split("-"); (parts[0].toInt() + parts[1].toInt()) / 2 }
            else -> valeur.toInt()
        }
    } else {
        when (legume.categorie) {
            "Fruit", "Cucurbitacée", "Chou", "Tubercule" -> 4
            "Racine", "Alliacé" -> 30
            "Feuille", "Légumineuse" -> 20
            "Aromatique" -> 15
            "Fleur annuelle", "Fleur vivace" -> 10
            else -> 9
        }
    }
}

fun getDistanceEntrePlants(legume: LegumeEntity): String {
    val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) cm entre plants").find(legume.plantation)
    return match?.groupValues?.get(1) ?: "20"
}
