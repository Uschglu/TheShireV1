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
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import com.theshire.app.data.AdventiceEntity
import com.theshire.app.data.AppDatabase
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
import com.theshire.app.data.ReseauRepository
import com.theshire.app.data.RotationRepository
import com.theshire.app.data.VarieteEntity
import com.theshire.app.ui.AdventiceRepository
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.RappelRepository
import com.theshire.app.ui.VarieteRepository
import com.theshire.app.ui.theme.PotagerShireTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 🎨 Nouvelle palette chaleureuse
object CouleursApp {
    val Creme = Color(0xFFFAF6F0)
    val Blanc = Color(0xFFFFFDF9)
    val VertPrincipal = Color(0xFF5B8C5A)
    val VertClair = Color(0xFF8BC34A)
    val VertPale = Color(0xFFE8EFE8)
    val TexteFonce = Color(0xFF2D3A2D)
    val Terracotta = Color(0xFFC67B4B)
    val BrunDoux = Color(0xFF8B7355)
}

val DegradeFond = Brush.verticalGradient(
    colors = listOf(Color(0xFFFAF6F0), Color(0xFFF0F0E8), Color(0xFFE8EFE8))
)

val DegradeCarte = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFDF9), Color(0xFFF8F4EE))
)

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
    
    Box(modifier = Modifier.fillMaxSize().background(DegradeFond).pointerInput(currentScreen) {
        detectHorizontalDragGestures(
            onDragEnd = { if (dragOffset < -200f) goToNext() else if (dragOffset > 200f) goToPrevious(); dragOffset = 0f },
            onHorizontalDrag = { change, amount -> change.consume(); dragOffset += amount }
        )
    }) {
        AnimatedContent(targetState = currentScreen, transitionSpec = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 } togetherWith fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 3 } }) { screen ->
            when (screen) {
                "accueil" -> AccueilScreen()
                "bibliotheque" -> BibliothequeScreen(onBack = { goToAccueil() })
                "jardin" -> JardinScreen(onBack = { goToAccueil() })
                "calendrier" -> CalendrierScreen(onBack = { goToAccueil() })
                "conservation" -> ConservationScreen(onBack = { goToAccueil() })
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
fun AccueilScreen() {
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
    var showTuto by remember { mutableStateOf(prefs.getBoolean("tuto_vu", false) == false) }
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { try { val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { output -> input.copyTo(output) } }; prefs.edit().putString("photo_path", f.absolutePath).apply(); imagePath = f.absolutePath } catch (e: Exception) {} }
    }
    
    LaunchedEffect(Unit) { try { val v = localisationRepository.getVille(); if (v != null) ville = v; meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" }) } catch (e: Exception) {} }
    LaunchedEffect(showPrevisions) { if (showPrevisions && previsions.isEmpty()) { try { previsions = meteoRepository.getPrevisions7Jours(ville.ifEmpty { "Paris" }) } catch (e: Exception) {} } }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        floatingActionButton = { FloatingActionButton(onClick = { showTuto = true }, containerColor = CouleursApp.Terracotta, shape = CircleShape) { Text("❓", style = MaterialTheme.typography.titleLarge) } }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).clickable { showPrevisions = true }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                Box(modifier = Modifier.background(Brush.linearGradient(listOf(CouleursApp.VertPale, CouleursApp.Blanc)))) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(getEmojiMeteo(meteo), style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            if (meteo != null) { Text("${meteo!!.temperature}°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(meteo!!.description) }
                            else { Text("--°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Météo indisponible") }
                            Text(dateFormat.format(Date()), style = MaterialTheme.typography.bodySmall)
                            Text("${phaseLune.emoji} ${phaseLune.nom}", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                            if (ville.isNotEmpty()) Text("📍 $ville", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth().weight(1f).shadow(4.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                if (imageFile != null && imageFile.exists()) {
                    Box(Modifier.fillMaxSize()) {
                        val loader = remember { ImageLoaderProvider.getImageLoader(context) }
                        AsyncImage(model = imageFile, contentDescription = "Photo", imageLoader = loader, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)))
                        IconButton(onClick = { showPhotoDialog = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).background(CouleursApp.VertPrincipal.copy(alpha = 0.8f), CircleShape)) { Icon(Icons.Default.CameraAlt, "Changer", tint = CouleursApp.Blanc) }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.PhotoCamera, null, Modifier.size(80.dp), tint = CouleursApp.VertClair)
                        Spacer(Modifier.height(16.dp))
                        Text("📸 Photo de mon jardin", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Prenez une photo ou choisissez une image", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showPhotoDialog = true }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Ajouter une photo") }
                    }
                }
            }
        }
    }
    
    if (showPrevisions) {
        AlertDialog(onDismissRequest = { showPrevisions = false }, title = { Text("📅 Prévisions 7 jours", fontWeight = FontWeight.Bold) },
            text = { if (previsions.isEmpty()) Text("Chargement...") else Column { previsions.forEach { p -> Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.date, fontWeight = FontWeight.Bold); Text(p.emoji); Text("${p.tempMin.toInt()}°/${p.tempMax.toInt()}°") } } } },
            confirmButton = { TextButton(onClick = { showPrevisions = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } })
    }
    
    if (showPhotoDialog) {
        AlertDialog(onDismissRequest = { showPhotoDialog = false }, title = { Text("Ajouter une photo", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("📸 Prendre une photo", Modifier.fillMaxWidth().clickable { showPhotoDialog = false; photoLauncher.launch(null) }.padding(16.dp), fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Text("🖼️ Choisir depuis la galerie", Modifier.fillMaxWidth().clickable { showPhotoDialog = false; galleryLauncher.launch("image/*") }.padding(16.dp), fontWeight = FontWeight.Bold)
                if (imageFile != null && imageFile.exists()) { HorizontalDivider(); Text("🗑️ Supprimer", Modifier.fillMaxWidth().clickable { imageFile!!.delete(); prefs.edit().remove("photo_path").apply(); imagePath = null; showPhotoDialog = false }.padding(16.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            } },
            confirmButton = { TextButton(onClick = { showPhotoDialog = false }) { Text("Annuler") } })
    }
    
    if (showTuto) {
        AlertDialog(
            onDismissRequest = { showTuto = false; prefs.edit().putBoolean("tuto_vu", true).apply() },
            title = { Text("🌱 Bienvenue dans Potager Shire !", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
            text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                item { Column { Text("🏠 Accueil", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Météo, phase de lune et photo de votre jardin. Cliquez sur la météo pour les prévisions 7 jours.") } }
                item { Column { Text("📚 Bibliothèque", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("3 onglets : Plantes (fiches détaillées), Adventices (indications du sol), Identifier (reconnaissance photo).") } }
                item { Column { Text("🌿 Adventices = mauvaises herbes", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta); Text("Les adventices sont des plantes sauvages qui poussent spontanément. Elles indiquent la nature de votre sol.") } }
                item { Column { Text("🏡 Jardin", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Créez des planches, choisissez plantes et variétés. Les plantes volumineuses (tomates, courges...) ont besoin d'espace !") } }
                item { Column { Text("📅 Calendrier", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Cliquez sur un jour pour ajouter un rappel avec cloche 🔔 et heure personnalisable.") } }
                item { Column { Text("🥫 Conservation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Filtrez par méthode et consultez le guide détaillé avec le bouton ?.") } }
                item { Column { Text("👆 Navigation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("• Swipe gauche/droite pour changer de page\n• Back téléphone : page précédente\n• Flèche retour : accueil\n• Billes en bas : position") } }
            } },
            confirmButton = { Button(onClick = { showTuto = false; prefs.edit().putBoolean("tuto_vu", true).apply() }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Commencer 🌱") } }
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
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = when(selectedOnglet) { "plantes" -> 0; "adventices" -> 1; else -> 2 }, containerColor = CouleursApp.VertPrincipal, contentColor = CouleursApp.Blanc) {
            Tab(selected = selectedOnglet == "plantes", onClick = { selectedOnglet = "plantes" }, text = { Text("🌱 Plantes", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "plantes") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "adventices", onClick = { selectedOnglet = "adventices" }, text = { Text("🌿 Adventices", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "adventices") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "reconnaissance", onClick = { selectedOnglet = "reconnaissance" }, text = { Text("📸 Identifier", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "reconnaissance") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
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
            topBar = { TopAppBar(title = { Text("Bibliothèque 📚", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
        ) { innerPadding ->
            LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        if (bitmap != null) { val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg"); f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it) }; imageUri = Uri.fromFile(f); resultats = emptyList(); errorMessage = ""; scope.launch { isAnalyzing = true; try { resultats = repo.identifierPlante(f) } catch (e: Exception) { errorMessage = e.message ?: "Erreur" }; isAnalyzing = false } }
    }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg"); context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { output -> input.copyTo(output) } }; imageUri = Uri.fromFile(f); resultats = emptyList(); errorMessage = ""; scope.launch { isAnalyzing = true; try { resultats = repo.identifierPlante(f) } catch (e: Exception) { errorMessage = e.message ?: "Erreur" }; isAnalyzing = false } }
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Identifier une plante 📸", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { photoLauncher.launch(null) }, Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Icon(Icons.Default.CameraAlt, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Photo") }
                Button(onClick = { galleryLauncher.launch("image/*") }, Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)) { Text("Galerie") }
            } }
            if (imageUri != null) { item { val loader = remember { ImageLoaderProvider.getImageLoader(context) }; AsyncImage(model = imageUri, contentDescription = "Plante", imageLoader = loader, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp))) } }
            if (isAnalyzing) item { CircularProgressIndicator(color = CouleursApp.VertPrincipal); Text("Analyse en cours...") }
            if (errorMessage.isNotEmpty()) item { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            if (resultats.isNotEmpty()) item { Text("🔍 Résultats :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }
            resultats.forEach { r ->
                item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (r.messageErreur.isNotEmpty()) MaterialTheme.colorScheme.errorContainer else CouleursApp.VertPale)) {
                    Column(Modifier.padding(16.dp)) { Text(r.nom, fontWeight = FontWeight.Bold); Text(r.nomScientifique, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall); if (r.probabilite > 0) Text("Confiance : ${(r.probabilite * 100).toInt()}%", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold); if (r.messageErreur.isNotEmpty()) Text(r.messageErreur, color = MaterialTheme.colorScheme.error) } }
                }
            }
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
            topBar = { TopAppBar(title = { Text("Adventices 🌿", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
        ) { innerPadding ->
            LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("${adventices.size} adventices courantes", color = CouleursApp.TexteFonce); Spacer(Modifier.height(8.dp)); Text("Les adventices (mauvaises herbes) indiquent la nature de votre sol.", style = MaterialTheme.typography.bodySmall) }
                items(adventices, key = { it.id }) { a ->
                    Card(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { selected = a }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { Text(a.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(a.indicationSol, style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, maxLines = 2) }
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
        topBar = { TopAppBar(title = { Text(adventice.nom, fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp)).background(CouleursApp.VertPale), contentAlignment = Alignment.Center) { Text(adventice.emoji, style = MaterialTheme.typography.displayLarge) } }
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
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = if (selectedOnglet == "planches") 0 else 1, containerColor = CouleursApp.VertPrincipal, contentColor = CouleursApp.Blanc) {
            Tab(selected = selectedOnglet == "planches", onClick = { selectedOnglet = "planches" }, text = { Text("🌱 Planches", fontWeight = FontWeight.Bold, color = if (selectedOnglet == "planches") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "analyse", onClick = { selectedOnglet = "analyse" }, text = { Text("🔬 Analyse du sol", fontWeight = FontWeight.Bold, color = if (selectedOnglet == "analyse") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
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
    var avertissement by remember { mutableStateOf<AvertissementRotation?>(null) }
    var showAvertissement by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis(); varieteRepository.ajouterVarietesPredefinies() }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Mon Jardin 🏡", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddPlancheDialog = true }, containerColor = CouleursApp.VertClair, shape = CircleShape) { Icon(Icons.Default.Add, "Ajouter") } }
    ) { innerPadding ->
        if (planches.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🏡", style = MaterialTheme.typography.displayLarge); Text("Aucune planche", fontWeight = FontWeight.Bold); Text("Cliquez sur + pour créer")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { LegendeCouleurs() }
                items(planches, key = { it.id }) { planche ->
                    PlancheCard(planche, expandedPlancheId == planche.id, { expandedPlancheId = if (expandedPlancheId == planche.id) null else planche.id }, { scope.launch { jardinRepository.supprimerPlanche(planche) } }, jardinRepository) { carre, caseNumero -> selectedCarre = carre; selectedCaseNumero = caseNumero; showLegumeSelection = true }
                }
            }
        }
    }
    
    if (showAddPlancheDialog) {
        var nom by remember { mutableStateOf("") }; var largeur by remember { mutableStateOf("3") }; var longueur by remember { mutableStateOf("4") }
        AlertDialog(onDismissRequest = { showAddPlancheDialog = false }, title = { Text("Nouvelle planche", fontWeight = FontWeight.Bold) },
            text = { Column {
                OutlinedTextField(nom, { nom = it }, label = { Text("Nom de la planche") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(largeur, { largeur = it }, label = { Text("Largeur (m)") }, Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                    Text("×", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(longueur, { longueur = it }, label = { Text("Longueur (m)") }, Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                }
            } },
            confirmButton = { Button(onClick = { val l = largeur.toIntOrNull() ?: 1; val L = longueur.toIntOrNull() ?: 1; if (l > 0 && L > 0 && nom.isNotBlank()) { scope.launch { jardinRepository.ajouterPlanche(nom, l, L) }; showAddPlancheDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Créer") } },
            dismissButton = { TextButton(onClick = { showAddPlancheDialog = false }) { Text("Annuler") } })
    }
    
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!; val caseNumero = selectedCaseNumero
        var selectedCategorie by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val categories = legumes.groupBy { it.categorie }.keys.toList()
        
        AlertDialog(onDismissRequest = { showLegumeSelection = false }, title = { Text("Choisissez une plante", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("Case $caseNumero du carré", style = MaterialTheme.typography.bodySmall)
                Text("🗑️ Vider la case", Modifier.fillMaxWidth().clickable { scope.launch { jardinRepository.modifierCasePrecise(carre, caseNumero, null) }; showLegumeSelection = false }.padding(12.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                OutlinedTextField(searchQuery, { searchQuery = it }, label = { Text("🔍 Rechercher...") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                Spacer(Modifier.height(8.dp))
                if (searchQuery.isEmpty()) {
                    Column(Modifier.fillMaxWidth().height(120.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { c -> FilterChip(selected = selectedCategorie == c, onClick = { selectedCategorie = if (selectedCategorie == c) null else c }, label = { Text("${getEmojiCategorie(c)} $c", fontSize = MaterialTheme.typography.bodySmall.fontSize) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (searchQuery.isNotEmpty() || selectedCategorie != null) {
                    val plantes = legumes.filter { (searchQuery.isEmpty() || it.nom.contains(searchQuery, true)) && (selectedCategorie == null || it.categorie == selectedCategorie) }
                    LazyColumn { items(plantes) { legume ->
                        Text(legume.nom, Modifier.fillMaxWidth().clickable {
                            if (!peutPlanterIci(carre, caseNumero, legume.nom)) { android.widget.Toast.makeText(context, "${legume.nom} est volumineux, espace insuffisant", android.widget.Toast.LENGTH_LONG).show(); showLegumeSelection = false }
                            else { val av = rotationRepository.getAvertissement(legume.nom, carre); if (av != null) { selectedLegumeNom = legume.nom; avertissement = av; showAvertissement = true; showLegumeSelection = false } else { selectedLegumeNom = legume.nom; showVarieteSelection = true; showLegumeSelection = false } }
                        }.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
                        HorizontalDivider()
                    } }
                }
            } },
            confirmButton = { TextButton(onClick = { showLegumeSelection = false }) { Text("Annuler") } })
    }
    
    if (showVarieteSelection && selectedLegumeNom != null) {
        VarieteSelectionDialog(selectedLegumeNom!!, varieteRepository,
            onVarieteChoisie = { nomComplet -> scope.launch { jardinRepository.modifierCasePrecise(selectedCarre!!, selectedCaseNumero, nomComplet) }; showVarieteSelection = false; selectedLegumeNom = null; selectedCarre = null; selectedCaseNumero = 0 },
            onDismiss = { showVarieteSelection = false; selectedLegumeNom = null; selectedCarre = null; selectedCaseNumero = 0 })
    }
    
    if (showAvertissement && avertissement != null) {
        val av = avertissement!!; val carre = selectedCarre; val caseNumero = selectedCaseNumero; val legumeNom = selectedLegumeNom
        AlertDialog(onDismissRequest = { showAvertissement = false; avertissement = null }, title = { Text("Rotation des cultures", fontWeight = FontWeight.Bold) },
            text = { Text(av.message) },
            confirmButton = { Button(onClick = { if (carre != null && caseNumero > 0 && legumeNom != null) scope.launch { jardinRepository.modifierCasePrecise(carre, caseNumero, legumeNom) }; showAvertissement = false; avertissement = null }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Planter quand même") } },
            dismissButton = { TextButton(onClick = { showAvertissement = false; avertissement = null }) { Text("Annuler") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseSolScreen(onBack: () -> Unit) {
    var argile by remember { mutableStateOf("") }; var sable by remember { mutableStateOf("") }; var limon by remember { mutableStateOf("") }
    var typeSol by remember { mutableStateOf("") }
    fun calculer() { val a = argile.toIntOrNull() ?: 0; val s = sable.toIntOrNull() ?: 0; val l = limon.toIntOrNull() ?: 0; typeSol = if (a + s + l == 100) when { a > 40 -> "Sol argileux"; s > 70 -> "Sol sableux"; l > 50 -> "Sol limoneux"; else -> "Sol équilibré" } else "Total = ${a + s + l}% (doit faire 100%)" }
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Analyse du sol 🔬", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { OutlinedTextField(argile, { argile = it }, label = { Text("Argile (%)") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(sable, { sable = it }, label = { Text("Sable (%)") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(limon, { limon = it }, label = { Text("Limon (%)") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { Button(onClick = { calculer() }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Analyser") } }
            if (typeSol.isNotEmpty()) item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)) { Text(typeSol, Modifier.padding(20.dp), fontWeight = FontWeight.Bold) } }
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
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val meteoRepository = remember { MeteoRepository() }
    val luneRepository = remember { LuneRepository() }
    var legumesPlantes by remember { mutableStateOf<List<String>>(emptyList()) }
    var datesPlantation by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("Paris") }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var showRappelDialog by remember { mutableStateOf(false) }
    var selectedTimestamp by remember { mutableStateOf(0L) }
    val rappelRepository = remember { RappelRepository(context) }
    var rappelActif by remember { mutableStateOf(false) }
    var rappelNote by remember { mutableStateOf("") }
    var rappelHeure by remember { mutableStateOf(9) }
    var rappelMinute by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis(); try { legumesPlantes = jardinRepository.getLegumesPlantes(); datesPlantation = jardinRepository.getDatesPlantation() } catch (e: Exception) {} }
    LaunchedEffect(Unit) { try { meteo = meteoRepository.getMeteo(ville) } catch (e: Exception) {} }
    val moisNoms = listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Calendrier 📅", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { MeteoCard(meteo, ville, true, phaseLune) }
            item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth-- }) { Text("◀", fontSize = MaterialTheme.typography.titleLarge.fontSize) }
                        Text("${moisNoms[currentMonth]} $currentYear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++ }) { Text("▶", fontSize = MaterialTheme.typography.titleLarge.fontSize) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) { listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = CouleursApp.VertPrincipal) } }
                    Spacer(Modifier.height(8.dp))
                    val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1)
                    val firstDay = cal.get(Calendar.DAY_OF_WEEK); val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2
                    for (row in 0 until ((offset + daysInMonth + 6) / 7)) {
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val dayNumber = row * 7 + col - offset + 1
                                if (dayNumber in 1..daysInMonth) {
                                    val isSelected = dayNumber == selectedDay
                                    Box(Modifier.weight(1f).aspectRatio(1f).background(if (isSelected) CouleursApp.VertPrincipal else Color.Transparent, RoundedCornerShape(8.dp)).clickable { selectedDay = dayNumber; val calJour = Calendar.getInstance(); calJour.set(currentYear, currentMonth, dayNumber, rappelHeure, rappelMinute, 0); selectedTimestamp = calJour.timeInMillis; val rappel = rappelRepository.getRappelSync(selectedTimestamp); rappelActif = rappel?.estActif ?: false; rappelNote = rappel?.note ?: ""; showRappelDialog = true }.padding(4.dp), contentAlignment = Alignment.Center) { Text("$dayNumber", color = if (isSelected) CouleursApp.Blanc else CouleursApp.TexteFonce, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                                } else Box(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            } }
            item { Text("Plantes plantées (${legumesPlantes.size}) :", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce) }
            legumesPlantes.forEach { nom -> val legume = legumes.find { it.nom == nom }; if (legume != null) item { CalendrierLegumeCard(legume, datesPlantation[nom]) } }
        }
    }
    
    if (showRappelDialog) {
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        val dateRappel = Date(selectedTimestamp)
        val scope = rememberCoroutineScope()
        AlertDialog(onDismissRequest = { showRappelDialog = false },
            title = { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("📅 ${dateFormat.format(dateRappel)}", fontWeight = FontWeight.Bold); IconButton(onClick = { scope.launch { val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0); rappelRepository.toggleRappel(cal.timeInMillis, "Rappel"); rappelActif = !rappelActif } }) { Text(if (rappelActif) "🔔" else "🔕", style = MaterialTheme.typography.titleLarge) } } },
            text = { Column {
                Text(if (rappelActif) "Rappel activé" else "Rappel désactivé", color = if (rappelActif) CouleursApp.VertPrincipal else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth().clickable { val tp = TimePickerDialog(context, { _, h, m -> rappelHeure = h; rappelMinute = m }, rappelHeure, rappelMinute, true); tp.show() }, colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("⏰", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.width(12.dp)); Column { Text("Heure du rappel", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold); Text("${String.format("%02d", rappelHeure)}:${String.format("%02d", rappelMinute)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) } } }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(rappelNote, { rappelNote = it }, label = { Text("Note (optionnel)") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), minLines = 3)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { scope.launch { val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0); val ts = cal.timeInMillis; val r = rappelRepository.getRappel(ts); if (r != null) rappelRepository.mettreAJourNote(ts, rappelNote) else rappelRepository.ajouterRappel(ts, "Rappel", rappelNote) }; showRappelDialog = false }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Enregistrer") }
            } },
            confirmButton = { TextButton(onClick = { showRappelDialog = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } })
    }
}

@Composable
fun MeteoCard(meteo: MeteoData?, ville: String, estConnecte: Boolean, phaseLune: PhaseLune? = null) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("🌦️ Météo à $ville", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
            if (phaseLune != null) Text("${phaseLune.emoji} ${phaseLune.nom}", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (meteo != null) { Text("🌡️ ${meteo.temperature}°C"); Text("☁️ ${meteo.description}"); Text("💧 ${meteo.humidite}%"); Text("🌬️ ${meteo.vent} m/s") }
            else Text("Météo indisponible")
        }
    }
}

@Composable
fun CalendrierLegumeCard(legume: LegumeEntity, datePlantation: Long? = null) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleLarge)
            if (datePlantation != null) { val df = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE); Text("🌱 Planté le ${df.format(Date(datePlantation))}", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold) }
            Text("📅 Semis : ${legume.semis}"); Text("🌱 Plantation : ${legume.plantation}"); Text("🧺 Récolte : ${legume.recolte}")
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
        topBar = { TopAppBar(title = { Text("Conservation 🥫", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, actions = { IconButton(onClick = { showAide = true }) { Icon(Icons.Default.Help, "Aide", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Filtrer par méthode :", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Spacer(Modifier.height(8.dp)); Column { methodes.forEach { m -> FilterChip(selected = filtre == m, onClick = { filtre = m }, label = { Text(m) }, Modifier.padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp)) } } }
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
            Column(Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = when(selectedOnglet) { "sechage" -> 0; "lacto" -> 1; "conserves" -> 2; else -> 3 }, containerColor = CouleursApp.VertPale, contentColor = CouleursApp.VertPrincipal) {
                    Tab(selected = selectedOnglet == "sechage", onClick = { selectedOnglet = "sechage" }, text = { Text("🌬️ Séchage", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "lacto", onClick = { selectedOnglet = "lacto" }, text = { Text("🥬 Lacto", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "conserves", onClick = { selectedOnglet = "conserves" }, text = { Text("🫙 Conserves", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedOnglet == "congelation", onClick = { selectedOnglet = "congelation" }, text = { Text("❄️ Congélation", fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = FontWeight.Bold) })
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (selectedOnglet) {
                        "sechage" -> {
                            item { Text("🌬️ Séchage optimal", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Choisissez des légumes frais et sains\n• Lavez et séchez soigneusement\n• Coupez en tranches fines (3-5mm)\n• Blanchissez les légumes durs 2-3 min\n• Disposez sans chevauchement\n• Température : 50-60°C\n• Durée : 6-12h\n• Stockez en bocaux hermétiques\n• Conservation : 6-12 mois") }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Tomates, champignons, carottes, courgettes, oignons, poivrons, herbes aromatiques") }
                        }
                        "lacto" -> {
                            item { Text("🥬 Lactofermentation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Sel sans iode : 2-3% (20-30g/L)\n• Coupez en morceaux réguliers\n• Tassez pour éliminer les bulles\n• Légumes immergés sous la saumure\n• Température : 18-22°C\n• Durée : 1-4 semaines\n• Réfrigérateur après ouverture") }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Choux (choucroute), carottes, radis, concombres (pickles), haricots verts, betteraves") }
                        }
                        "conserves" -> {
                            item { Text("🫙 Conserves (stérilisation)", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Stérilisez bocaux et couvercles\n• Remplissez en laissant 2cm de vide\n• Eau salée bouillante (20g/L)\n• Fermez hermétiquement\n• Stérilisez à 100°C pendant 1h-1h30\n• Vérifiez l'étanchéité\n• Conservation : 1-2 ans") }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Tomates, haricots verts, petits pois, carottes, betteraves, ratatouille") }
                        }
                        "congelation" -> {
                            item { Text("❄️ Congélation optimale", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleMedium) }
                            item { Text("• Blanchissez 2-3 min\n• Refroidissez dans l'eau glacée\n• Égouttez bien\n• Disposez à plat\n• Sacs sans air\n• Étiquetez\n• Température : -18°C\n• Conservation : 8-12 mois") }
                            item { Text("🥕 Légumes adaptés", fontWeight = FontWeight.Bold, color = CouleursApp.Terracotta) }
                            item { Text("Haricots verts, petits pois, carottes, courgettes, poivrons, épinards, brocolis") }
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
    Card(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(Modifier.padding(16.dp)) {
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
            topBar = { TopAppBar(title = { Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
        ) { innerPadding ->
            LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(24.dp)).background(CouleursApp.VertPale), contentAlignment = Alignment.Center) { Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayLarge) } }
                item { InfoCard("Catégorie", legume.categorie) }
                if (legume.estVivace) item { InfoCard("Type", "🌿 Plante vivace") }
                if (legume.estFleur) item { InfoCard("Type", "🌸 Fleur") }
                item { InfoCard("Difficulté", legume.difficulte) }
                item { InfoCard("Exposition", legume.exposition) }
                item { InfoCard("Sol", legume.sol) }
                item { InfoCard("Arrosage", legume.arrosage) }
                item { InfoCard("Semis", legume.semis) }
                item { InfoCard("Plantation", legume.plantation) }
                item { InfoCard("Récolte", legume.recolte) }
                item { InfoCard("Entretien", legume.entretien) }
                item { InfoCard("Bonnes associations", legume.bonnesAssociations) }
                item { InfoCard("Mauvaises associations", legume.mauvaisesAssociations) }
                item { InfoCard("Conservation", legume.conservation) }
                if (varietes.isNotEmpty()) {
                    item { Text("🌱 Variétés populaires (${varietes.size}) :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleLarge) }
                    items(varietes) { v -> Card(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { selectedVariete = v }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("🌱"); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(v.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(v.description, style = MaterialTheme.typography.bodySmall, maxLines = 2) } } } }
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
        topBar = { TopAppBar(title = { Text(variete.nom, fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { InfoCard("Description", variete.description) }
            item { InfoCard("Semis", variete.semis) }
            item { InfoCard("Plantation", variete.plantation) }
            item { InfoCard("Récolte", variete.recolte) }
            item { InfoCard("Particularités", variete.particularites) }
        }
    }
}

// ============== COMPOSANTS ==============
@Composable
fun LegumeCard(legume: LegumeEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce, style = MaterialTheme.typography.titleMedium)
                Text("Catégorie : ${legume.categorie}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal)
                Text("Difficulté : ${legume.difficulte}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun VarieteSelectionDialog(legumeNom: String, varieteRepository: VarieteRepository, onVarieteChoisie: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var varietes by remember { mutableStateOf<List<VarieteEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(legumeNom) { scope.launch { try { varieteRepository.ajouterVarietesPredefinies(); varietes = AppDatabase.getDatabase(context).legumeDao().getVarietesForLegumeSync(legumeNom) } catch (e: Exception) { varietes = emptyList() } } }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Variétés de $legumeNom", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item { Text("🌱 Variété standard", Modifier.fillMaxWidth().clickable { onVarieteChoisie(legumeNom) }.padding(16.dp), fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); HorizontalDivider() }
                if (varietes.isEmpty()) item { Text("Aucune variété disponible", Modifier.padding(16.dp), color = CouleursApp.TexteFonce) }
                else items(varietes) { v ->
                    Text("🌿 ${v.nom}", Modifier.fillMaxWidth().clickable { onVarieteChoisie("${legumeNom} (${v.nom})") }.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    Text(v.description, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun PlancheCard(planche: PlancheEntity, isExpanded: Boolean, onToggleExpand: () -> Unit, onDelete: () -> Unit, jardinRepository: JardinRepository, onSousCarreClick: (CarreEntity, Int) -> Unit) {
    Card(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onToggleExpand), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(planche.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce, style = MaterialTheme.typography.titleLarge); Text("${planche.largeur}m × ${planche.longueur}m = ${planche.largeur * planche.longueur} carrés", style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
            }
            if (isExpanded) {
                Spacer(Modifier.height(16.dp))
                val carres by jardinRepository.getCarresForPlanche(planche.id).collectAsState(initial = emptyList())
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (y in 0 until planche.longueur) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (x in 0 until planche.largeur) {
                                val carre = carres.find { it.positionX == x && it.positionY == y }
                                if (carre != null) Grille3x3(carre, { case -> onSousCarreClick(carre, case) }, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Grille3x3(carre: CarreEntity, onSousCarreClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val legumes = listOfNotNull(carre.case1, carre.case2, carre.case3, carre.case4, carre.case5, carre.case6, carre.case7, carre.case8, carre.case9)
    if (legumes.size == 9 && legumes.distinct().size == 1) {
        Box(modifier.aspectRatio(1f).background(Color(0xFF4CAF50).copy(alpha = 0.2f)).border(2.dp, CouleursApp.VertPrincipal).clickable { onSousCarreClick(1) }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(legumes[0], fontWeight = FontWeight.Bold); Text("✅") } }
    } else {
        Column(modifier.aspectRatio(1f).border(2.dp, CouleursApp.VertPrincipal)) {
            for (row in 0..2) { Row(Modifier.weight(1f)) { for (col in 0..2) {
                val caseNumero = row * 3 + col + 1
                val legume = when (caseNumero) { 1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3; 4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6; 7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9; else -> null }
                Box(Modifier.weight(1f).fillMaxHeight().background(if (legume != null) Color(0xFF4CAF50).copy(alpha = 0.3f) else CouleursApp.Blanc).border(1.dp, CouleursApp.VertPrincipal).clickable { onSousCarreClick(caseNumero) }, contentAlignment = Alignment.Center) { Text(legume ?: "", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
            } } }
        }
    }
}

@Composable
fun LegendeCouleurs() {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(Modifier.padding(16.dp)) {
            Text("Légende", fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(20.dp).background(Color(0xFF4CAF50).copy(alpha = 0.5f))); Text(" Bonne association", style = MaterialTheme.typography.bodySmall) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(20.dp).background(Color(0xFFFF9800).copy(alpha = 0.5f))); Text(" Association neutre", style = MaterialTheme.typography.bodySmall) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(20.dp).background(Color(0xFFF44336).copy(alpha = 0.5f))); Text(" Mauvaise association", style = MaterialTheme.typography.bodySmall) }
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
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(contenu, color = CouleursApp.TexteFonce)
        }
    }
}

// ============== FONCTIONS PLANTES ==============
fun estPlanteVolumineuse(nomLegume: String): Boolean = nomLegume in listOf("Tomate", "Courgette", "Potiron", "Courge", "Aubergine", "Poivron", "Concombre", "Melon", "Chou", "Brocoli", "Chou-fleur", "Topinambour")

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
