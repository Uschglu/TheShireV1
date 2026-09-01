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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.theshire.app.data.RappelEntity
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

val CouleurFond = Color(0xFFFAF6F0)
val CouleurCarte = Color(0xFFFFFDF9)
val CouleurVertSauge = Color(0xFF5B8C5A)
val CouleurTerracotta = Color(0xFFC67B4B)
val CouleurBrunDoux = Color(0xFF8B7355)
val CouleurTexte = Color(0xFF2D3A2D)

val DegradeFond = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFAF6F0),
        Color(0xFFF0F0E8),
        Color(0xFFE8EFE8)
    )
)

val DegradeCarte = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFDF9),
        Color(0xFFF8F4EE)
    )
)

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != 
                PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != 
                PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != 
                PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != 
                PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), 1000)
        }
        
        setContent {
            PotagerShireTheme {
                MainScreen()
            }
        }
        
        planifierNotifications()
    }
    
    private fun planifierNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        
        val intentArrosage = android.content.Intent(this, NotificationReceiver::class.java)
        intentArrosage.putExtra("type", "arrosage")
        val pendingIntentArrosage = android.app.PendingIntent.getBroadcast(
            this, 1, intentArrosage,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendarArrosage = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 18)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendarArrosage.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntentArrosage
        )
        
        val intentOperations = android.content.Intent(this, NotificationReceiver::class.java)
        intentOperations.putExtra("type", "operations")
        val pendingIntentOperations = android.app.PendingIntent.getBroadcast(
            this, 2, intentOperations,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendarOperations = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 8)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendarOperations.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntentOperations
        )
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
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }
    
    val navigationStack = remember { mutableStateListOf("accueil") }
    val screens = listOf("accueil", "bibliotheque", "jardin", "calendrier", "conservation")
    
    fun navigateTo(screen: String) {
        navigationStack.add(screen)
        currentScreen = screen
    }
    
    fun goBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = navigationStack.last()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 1000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = now
                android.widget.Toast.makeText(
                    context,
                    "Appuyez encore pour quitter",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    fun goToAccueil() {
        navigationStack.clear()
        navigationStack.add("accueil")
        currentScreen = "accueil"
    }
    
    androidx.activity.compose.BackHandler { goBack() }
    
    fun goToNext() {
        val currentIndex = screens.indexOf(currentScreen)
        if (currentIndex < screens.size - 1) {
            navigateTo(screens[currentIndex + 1])
        }
    }
    
    fun goToPrevious() {
        val currentIndex = screens.indexOf(currentScreen)
        if (currentIndex > 0) {
            navigateTo(screens[currentIndex - 1])
        }
    }
    
    var dragOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 200f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DegradeFond)
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
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(animationSpec = tween(300)) { it / 3 } togetherWith 
                fadeOut(animationSpec = tween(300)) + 
                slideOutHorizontally(animationSpec = tween(300)) { -it / 3 }
            }
        ) { screen ->
            when (screen) {
                "accueil" -> AccueilScreen()
                "bibliotheque" -> BibliothequeScreen(onBack = { goToAccueil() })
                "jardin" -> JardinScreen(onBack = { goToAccueil() })
                "calendrier" -> CalendrierScreen(onBack = { goToAccueil() })
                "conservation" -> ConservationScreen(onBack = { goToAccueil() })
            }
        }
        
        // 🟢⚪ Indicateurs de page (billes)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(
                    CouleursApp.Blanc.copy(alpha = 0.85f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isCurrent = screen == currentScreen
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 12.dp else 10.dp)
                        .background(
                            color = if (isCurrent) CouleursApp.VertPrincipal else CouleursApp.Blanc,
                            shape = CircleShape
                        )
                        .border(
                            width = if (isCurrent) 0.dp else 1.dp,
                            color = CouleursApp.VertPrincipal.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { 
                            currentScreen = screen
                            navigationStack.clear()
                            navigationStack.add(screen)
                        }
                )
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
    var isLoadingPrevisions by remember { mutableStateOf(false) }
    var showTuto by remember { mutableStateOf(prefs.getBoolean("tuto_vu", false) == false) }
    
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE) }
    val dateDuJour = remember { dateFormat.format(Date()) }
    val phaseLune = remember { luneRepository.getPhaseLune() }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val fileName = "photo_jardin_${System.currentTimeMillis()}.jpg"
                val outputFile = File(context.filesDir, fileName)
                outputFile.outputStream().use { output ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)
                }
                val localPath = outputFile.absolutePath
                prefs.edit().putString("photo_path", localPath).apply()
                imagePath = localPath
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "photo_jardin_${System.currentTimeMillis()}.jpg"
                val outputFile = File(context.filesDir, fileName)
                inputStream?.use { input -> outputFile.outputStream().use { output -> input.copyTo(output) } }
                val localPath = outputFile.absolutePath
                prefs.edit().putString("photo_path", localPath).apply()
                imagePath = localPath
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    LaunchedEffect(Unit) {
        try {
            val villeDetectee = localisationRepository.getVille()
            if (villeDetectee != null) ville = villeDetectee
            meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" })
        } catch (e: Exception) { meteo = null }
    }
    
    LaunchedEffect(showPrevisions) {
        if (showPrevisions && previsions.isEmpty()) {
            isLoadingPrevisions = true
            try { previsions = meteoRepository.getPrevisions7Jours(ville.ifEmpty { "Paris" }) }
            catch (e: Exception) { previsions = emptyList() }
            isLoadingPrevisions = false
        }
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTuto = true },
                containerColor = CouleursApp.Terracotta,
                contentColor = CouleursApp.Blanc,
                shape = CircleShape
            ) { Text("❓", style = MaterialTheme.typography.titleLarge) }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).clickable { showPrevisions = true },
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.background(Brush.linearGradient(listOf(CouleursApp.VertPale, CouleursApp.Blanc)))) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(getEmojiMeteo(meteo), style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            if (meteo != null) {
                                Text("${meteo!!.temperature}°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                                Text(meteo!!.description, style = MaterialTheme.typography.bodyMedium, color = CouleursApp.TexteFonce)
                            } else {
                                Text("--°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                                Text("Météo indisponible", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dateDuJour, style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                            Text("${phaseLune.emoji} ${phaseLune.nom}", style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                            if (ville.isNotEmpty()) Text("📍 $ville", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).shadow(4.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (imageFile != null && imageFile.exists()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }
                        AsyncImage(model = imageFile, contentDescription = "Photo du jardin", imageLoader = imageLoader, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)))
                        IconButton(onClick = { showPhotoDialog = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).background(CouleursApp.VertPrincipal.copy(alpha = 0.8f), CircleShape)) {
                            Icon(Icons.Default.CameraAlt, "Changer la photo", tint = CouleursApp.Blanc)
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.PhotoCamera, "Photo du jardin", modifier = Modifier.size(80.dp), tint = CouleursApp.VertClair)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📸 Photo de mon jardin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Prenez une photo ou choisissez une image", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showPhotoDialog = true }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal), contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(20.dp))
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
            title = { Text("📅 Prévisions 7 jours", fontWeight = FontWeight.Bold) },
            text = {
                if (isLoadingPrevisions) { CircularProgressIndicator(color = CouleursApp.VertPrincipal) }
                else if (previsions.isEmpty()) { Text("Impossible de récupérer les prévisions") }
                else {
                    Column { previsions.forEach { prevision ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = CouleursApp.Creme)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(prevision.date, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(prevision.emoji)
                                Text("${prevision.tempMin.toInt()}° / ${prevision.tempMax.toInt()}°", fontWeight = FontWeight.Bold)
                            }
                        }
                    } }
                }
            },
            confirmButton = { TextButton(onClick = { showPrevisions = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } }
        )
    }
    
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Ajouter une photo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("📸 Prendre une photo", modifier = Modifier.fillMaxWidth().clickable { showPhotoDialog = false; photoLauncher.launch(null) }.padding(16.dp), fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Text("🖼️ Choisir depuis la galerie", modifier = Modifier.fillMaxWidth().clickable { showPhotoDialog = false; galleryLauncher.launch("image/*") }.padding(16.dp), fontWeight = FontWeight.Bold)
                    if (imageFile != null && imageFile.exists()) {
                        HorizontalDivider()
                        Text("🗑️ Supprimer la photo", modifier = Modifier.fillMaxWidth().clickable { try { imageFile.delete() } catch (e: Exception) {}; prefs.edit().remove("photo_path").apply(); imagePath = null; showPhotoDialog = false }.padding(16.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPhotoDialog = false }) { Text("Annuler") } }
        )
    }
    
    if (showTuto) {
        AlertDialog(
            onDismissRequest = { showTuto = false; prefs.edit().putBoolean("tuto_vu", true).apply() },
            title = { Text("🌱 Bienvenue dans Potager Shire !", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    item { Column { Text("🏠 Accueil", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Météo, lune et photo du jardin.") } }
                    item { Column { Text("📚 Bibliothèque", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Plantes, mauvaises herbes et reconnaissance photo.") } }
                    item { Column { Text("🏡 Jardin", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Planches, associations et variétés. Les plantes volumineuses ont besoin d'espace !") } }
                    item { Column { Text("📅 Calendrier", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Rappels avec cloche 🔔 et heure personnalisable.") } }
                    item { Column { Text("🥫 Conservation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Guide détaillé avec le bouton ?.") } }
                    item { Column { Text("👆 Navigation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal); Text("Swipe pour changer de page. Billes en bas pour naviguer.") } }
                }
            },
            confirmButton = { Button(onClick = { showTuto = false; prefs.edit().putBoolean("tuto_vu", true).apply() }, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Commencer 🌱") } }
        )
    }
}

fun getEmojiMeteo(meteo: MeteoData?): String {
    if (meteo == null) return "🌤️"
    return when {
        meteo.description.contains("pluie", ignoreCase = true) -> "🌧️"
        meteo.description.contains("nuage", ignoreCase = true) -> "☁️"
        meteo.description.contains("soleil", ignoreCase = true) || meteo.description.contains("clair", ignoreCase = true) -> "☀️"
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
    var selectedOnglet by remember { mutableStateOf("plantes") }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = when(selectedOnglet) { "plantes" -> 0; "mauvaises" -> 1; else -> 2 }, containerColor = CouleursApp.VertPrincipal, contentColor = CouleursApp.Blanc) {
            Tab(selected = selectedOnglet == "plantes", onClick = { selectedOnglet = "plantes" }, text = { Text("🌱 Plantes", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "plantes") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "mauvaises", onClick = { selectedOnglet = "mauvaises" }, text = { Text("🌿 Mauvaises", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "mauvaises") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
            Tab(selected = selectedOnglet == "reconnaissance", onClick = { selectedOnglet = "reconnaissance" }, text = { Text("📸 Identifier", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = if (selectedOnglet == "reconnaissance") CouleursApp.Blanc else CouleursApp.Blanc.copy(alpha = 0.6f)) })
        }
        when (selectedOnglet) {
            "plantes" -> BibliothequePlantesScreen(onBack = onBack)
            "mauvaises" -> AdventicesScreen(onBack = onBack)
            else -> ReconnaissanceScreen(onBack = onBack)
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
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { repository.ajouterLegumesPredefinis() }
    
    if (selectedLegume != null) {
        LegumeDetailScreen(legume = selectedLegume!!, onBack = { selectedLegume = null })
    } else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = {
                TopAppBar(
                    title = { Text("Bibliothèque 📚", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } },
                    actions = { IconButton(onClick = { showLegende = true }) { Icon(Icons.Default.Help, "Légende", tint = CouleursApp.Blanc) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)
                )
            }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("🔍 Rechercher une plante...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${legumes.filter { it.nom.contains(searchQuery, ignoreCase = true) }.size} plantes trouvées", style = MaterialTheme.typography.bodyMedium, color = CouleursApp.TexteFonce)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(legumes.filter { it.nom.contains(searchQuery, ignoreCase = true) }, key = { it.id }) { legume ->
                    LegumeCard(legume = legume, onClick = { selectedLegume = legume }, onDelete = { scope.launch { repository.supprimerLegume(legume) } })
                }
            }
        }
    }
    
    if (showLegende) {
        AlertDialog(
            onDismissRequest = { showLegende = false },
            title = { Text("Légende des catégories", fontWeight = FontWeight.Bold) },
            text = { LazyColumn {
                item { LigneLegende("🥕", "Racines") }; item { LigneLegende("🥔", "Tubercules") }; item { LigneLegende("🍅", "Fruits") }
                item { LigneLegende("🥬", "Feuilles") }; item { LigneLegende("🫘", "Légumineuses") }; item { LigneLegende("🧅", "Alliacés") }
                item { LigneLegende("🥦", "Choux") }; item { LigneLegende("🎃", "Cucurbitacées") }; item { LigneLegende("🌿", "Aromatiques") }
                item { LigneLegende("💐", "Fleurs vivaces") }; item { LigneLegende("🌸", "Fleurs annuelles") }
            } },
            confirmButton = { TextButton(onClick = { showLegende = false }) { Text("Fermer", color = CouleursApp.VertPrincipal) } }
        )
    }
}

@Composable
fun LigneLegende(emoji: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }
}

// ============== RECONNAISSANCE DE PLANTES ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconnaissanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val plantNetRepository = remember { PlantNetRepository() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var identifications by remember { mutableStateOf<List<PlantIdentification>>(emptyList()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val photoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val outputFile = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg")
            outputFile.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it) }
            imageUri = Uri.fromFile(outputFile)
            identifications = emptyList(); showError = false
            scope.launch {
                isAnalyzing = true
                try { identifications = plantNetRepository.identifierPlante(outputFile); if (identifications.isEmpty()) { showError = true; errorMessage = "Impossible d'identifier." } }
                catch (e: Exception) { showError = true; errorMessage = "Erreur : ${e.message}" }
                isAnalyzing = false
            }
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputFile = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input -> outputFile.outputStream().use { output -> input.copyTo(output) } }
                imageUri = Uri.fromFile(outputFile); identifications = emptyList(); showError = false
                scope.launch {
                    isAnalyzing = true
                    try { identifications = plantNetRepository.identifierPlante(outputFile); if (identifications.isEmpty()) { showError = true; errorMessage = "Impossible d'identifier." } }
                    catch (e: Exception) { showError = true; errorMessage = "Erreur : ${e.message}" }
                    isAnalyzing = false
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Identifier 📸", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Science, null, modifier = Modifier.size(64.dp), tint = CouleursApp.VertPrincipal)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reconnaissance de plantes", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Prenez une photo et l'app l'identifiera", textAlign = TextAlign.Center)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (imageUri != null) {
                            val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }
                            AsyncImage(model = imageUri, contentDescription = "Plante", imageLoader = imageLoader, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)))
                            if (isAnalyzing) { CircularProgressIndicator(color = CouleursApp.VertPrincipal); Text("Analyse en cours...") }
                        } else {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(80.dp), tint = CouleursApp.VertClair)
                            Text("Aucune photo sélectionnée")
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { photoLauncher.launch(null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("📸 Photo") }
                    Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)) { Text("🖼️ Galerie") }
                }
            }
            if (identifications.isNotEmpty()) {
                item { Text("🔍 Résultats :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }
                identifications.forEach { identification ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (identification.messageErreur.isNotEmpty()) MaterialTheme.colorScheme.errorContainer else CouleursApp.VertPale), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(identification.nom, fontWeight = FontWeight.Bold)
                                Text(identification.nomScientifique, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall)
                                if (identification.probabilite > 0) Text("Confiance : ${(identification.probabilite * 100).toInt()}%", color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                                if (identification.imageUrl.isNotEmpty()) {
                                    val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }
                                    AsyncImage(model = identification.imageUrl, contentDescription = identification.nom, imageLoader = imageLoader, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)))
                                }
                            }
                        }
                    }
                }
            }
            if (showError) { item { Text(errorMessage, color = MaterialTheme.colorScheme.error) } }
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
    var selectedAdventice by remember { mutableStateOf<AdventiceEntity?>(null) }
    
    LaunchedEffect(Unit) { repository.ajouterAdventicesPredefinies() }
    
    if (selectedAdventice != null) { AdventiceDetailScreen(adventice = selectedAdventice!!, onBack = { selectedAdventice = null }) }
    else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = { TopAppBar(title = { Text("Mauvaises herbes 🌿", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("${adventices.size} adventices courantes", color = CouleursApp.TexteFonce) }
                items(adventices, key = { it.id }) { adventice ->
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable { selectedAdventice = adventice }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(adventice.emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(adventice.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce); Text(adventice.indicationSol, style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, maxLines = 2) }
                            Icon(Icons.Default.ArrowForward, "Voir", tint = CouleursApp.VertPrincipal, modifier = Modifier.size(20.dp))
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
        TabRow(selectedTabIndex = if (selectedOnglet == "planches") 0 else 1, containerColor = CouleursApp.VertPrincipal, contentColor = CouleursApp.Blanc) {
            Tab(selected = selectedOnglet == "planches", onClick = { selectedOnglet = "planches" }, text = { Text("🌱 Planches", fontWeight = FontWeight.Bold) })
            Tab(selected = selectedOnglet == "analyse", onClick = { selectedOnglet = "analyse" }, text = { Text("🔬 Analyse du sol", fontWeight = FontWeight.Bold) })
        }
        if (selectedOnglet == "planches") JardinPlanchesScreen(onBack = onBack, onNavigateToAnalyse = { selectedOnglet = "analyse" })
        else AnalyseSolScreen(onBack = onBack, onNavigateToPlanches = { selectedOnglet = "planches" })
    }
}

// ============== JARDIN - PLANCHES ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinPlanchesScreen(onBack: () -> Unit, onNavigateToAnalyse: () -> Unit) {
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
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🏡", style = MaterialTheme.typography.displayLarge); Text("Aucune planche", style = MaterialTheme.typography.titleLarge); Text("Cliquez sur +")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { LegendeCouleurs() }
                items(planches, key = { it.id }) { planche ->
                    PlancheCard(planche = planche, isExpanded = expandedPlancheId == planche.id, onToggleExpand = { expandedPlancheId = if (expandedPlancheId == planche.id) null else planche.id }, onDelete = { scope.launch { jardinRepository.supprimerPlanche(planche) } }, jardinRepository = jardinRepository, onSousCarreClick = { carre, caseNumero -> selectedCarre = carre; selectedCaseNumero = caseNumero; showLegumeSelection = true })
                }
            }
        }
    }
    
    if (showAddPlancheDialog) {
        var nomPlanche by remember { mutableStateOf("") }; var largeur by remember { mutableStateOf("3") }; var longueur by remember { mutableStateOf("4") }
        AlertDialog(
            onDismissRequest = { showAddPlancheDialog = false },
            title = { Text("Nouvelle planche", fontWeight = FontWeight.Bold) },
            text = { Column {
                OutlinedTextField(value = nomPlanche, onValueChange = { nomPlanche = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = largeur, onValueChange = { largeur = it }, label = { Text("Largeur (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                    Text("×", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(value = longueur, onValueChange = { longueur = it }, label = { Text("Longueur (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                }
            } },
            confirmButton = { Button(onClick = { val l = largeur.toIntOrNull() ?: 1; val L = longueur.toIntOrNull() ?: 1; if (l > 0 && L > 0 && nomPlanche.isNotBlank()) { scope.launch { jardinRepository.ajouterPlanche(nomPlanche, l, L) }; showAddPlancheDialog = false } }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Créer") } },
            dismissButton = { TextButton(onClick = { showAddPlancheDialog = false }) { Text("Annuler") } }
        )
    }
    
    if (showLegumeSelection && selectedCarre != null) {
        val carre = selectedCarre!!; val caseNumero = selectedCaseNumero
        var selectedCategorie by remember { mutableStateOf<String?>(null) }
        var searchQueryPlante by remember { mutableStateOf("") }
        val categories = legumes.groupBy { it.categorie }.keys.toList()
        
        AlertDialog(
            onDismissRequest = { showLegumeSelection = false; selectedCarre = null; selectedCaseNumero = 0 },
            title = { Text("Choisissez une plante", fontWeight = FontWeight.Bold) },
            text = { Column {
                Text("Case ${caseNumero} du carré", style = MaterialTheme.typography.bodySmall)
                Text("🗑️ Vider la case", modifier = Modifier.fillMaxWidth().clickable { scope.launch { jardinRepository.modifierCasePrecise(carre, caseNumero, null) }; showLegumeSelection = false }.padding(16.dp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                OutlinedTextField(value = searchQueryPlante, onValueChange = { searchQueryPlante = it }, label = { Text("🔍 Rechercher...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
                if (searchQueryPlante.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().height(120.dp).verticalScroll(rememberScrollState())) {
                        categories.forEach { categorie -> FilterChip(selected = selectedCategorie == categorie, onClick = { selectedCategorie = if (selectedCategorie == categorie) null else categorie }, label = { Text("${getEmojiCategorie(categorie)} ${categorie}", fontSize = MaterialTheme.typography.bodySmall.fontSize) }, modifier = Modifier.fillMaxWidth()) }
                    }
                }
                if (searchQueryPlante.isNotEmpty() || selectedCategorie != null) {
                    val plantes = legumes.filter { legume -> (searchQueryPlante.isEmpty() || legume.nom.contains(searchQueryPlante, ignoreCase = true)) && (selectedCategorie == null || legume.categorie == selectedCategorie) }
                    LazyColumn {
                        items(plantes) { legume ->
                            Text(legume.nom, modifier = Modifier.fillMaxWidth().clickable {
                                if (!peutPlanterIci(carre, caseNumero, legume.nom)) {
                                    android.widget.Toast.makeText(context, "${legume.nom} est volumineux, espace insuffisant.", android.widget.Toast.LENGTH_LONG).show()
                                    showLegumeSelection = false
                                } else {
                                    val av = rotationRepository.getAvertissement(legume.nom, carre)
                                    if (av != null) { selectedLegumeNom = legume.nom; avertissement = av; showAvertissement = true; showLegumeSelection = false }
                                    else { selectedLegumeNom = legume.nom; showVarieteSelection = true; showLegumeSelection = false }
                                }
                            }.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                            HorizontalDivider()
                        }
                    }
                }
            } },
            confirmButton = { TextButton(onClick = { showLegumeSelection = false }) { Text("Annuler") } }
        )
    }
    
    if (showVarieteSelection && selectedLegumeNom != null) {
        VarieteSelectionDialog(
            legumeNom = selectedLegumeNom!!,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
                scope.launch { jardinRepository.modifierCasePrecise(selectedCarre!!, selectedCaseNumero, nomComplet) }
                showVarieteSelection = false; selectedLegumeNom = null; selectedCarre = null; selectedCaseNumero = 0
            },
            onDismiss = { showVarieteSelection = false; selectedLegumeNom = null; selectedCarre = null; selectedCaseNumero = 0 }
        )
    }
    
    if (showAvertissement && avertissement != null) {
        val av = avertissement!!; val carre = selectedCarre; val caseNumero = selectedCaseNumero; val legumeNom = selectedLegumeNom
        AlertDialog(
            onDismissRequest = { showAvertissement = false; avertissement = null },
            title = { Text("Rotation des cultures", fontWeight = FontWeight.Bold) },
            text = { Text(av.message) },
            confirmButton = { Button(onClick = { if (carre != null && caseNumero > 0 && legumeNom != null) scope.launch { jardinRepository.modifierCasePrecise(carre, caseNumero, legumeNom) }; showAvertissement = false; avertissement = null; selectedLegumeNom = null }, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Planter quand même") } },
            dismissButton = { TextButton(onClick = { showAvertissement = false; avertissement = null }) { Text("Annuler") } }
        )
    }
}

// ============== JARDIN - ANALYSE DU SOL ==============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseSolScreen(onBack: () -> Unit, onNavigateToPlanches: () -> Unit) {
    var argile by remember { mutableStateOf("") }; var sable by remember { mutableStateOf("") }; var limon by remember { mutableStateOf("") }
    var showAide by remember { mutableStateOf(false) }; var typeSol by remember { mutableStateOf("") }
    
    fun calculerTypeSol() {
        val a = argile.toIntOrNull() ?: 0; val s = sable.toIntOrNull() ?: 0; val l = limon.toIntOrNull() ?: 0; val total = a + s + l
        if (total == 100) {
            when {
                a > 40 -> typeSol = "Sol argileux - lourd, retient l'eau"
                s > 70 -> typeSol = "Sol sableux - léger, se réchauffe vite"
                l > 50 -> typeSol = "Sol limoneux - fertile, idéal"
                a in 20..35 && s in 35..50 -> typeSol = "Sol équilibré (idéal)"
                else -> typeSol = "Sol mixte"
            }
        } else typeSol = "Le total doit faire 100% (actuellement ${total}%)"
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Analyse du sol 🔬", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, actions = { IconButton(onClick = { showAide = true }) { Icon(Icons.Default.Help, "Aide", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { OutlinedTextField(value = argile, onValueChange = { argile = it }, label = { Text("Argile (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(value = sable, onValueChange = { sable = it }, label = { Text("Sable (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { OutlinedTextField(value = limon, onValueChange = { limon = it }, label = { Text("Limon (%)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
            item { Button(onClick = { calculerTypeSol() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Analyser") } }
            if (typeSol.isNotEmpty()) { item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)) { Text(typeSol, modifier = Modifier.padding(20.dp), fontWeight = FontWeight.Bold) } } }
        }
    }
    
    if (showAide) {
        AlertDialog(
            onDismissRequest = { showAide = false },
            title = { Text("Analyse du sol", fontWeight = FontWeight.Bold) },
            text = { Text("Test tactile : roulez une boule de terre humide.\n\n• Rugueuse = sableux\n• Douce = limoneux\n• Collante = argileux") },
            confirmButton = { TextButton(onClick = { showAide = false }) { Text("Fermer") } }
        )
    }
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
    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("Paris") }
    var estConnecte by remember { mutableStateOf(true) }
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
    LaunchedEffect(Unit) { val reseauRepository = ReseauRepository(context); estConnecte = reseauRepository.estConnecte(); if (estConnecte) { try { val localisationRepository = LocalisationRepository(context); ville = localisationRepository.getVille() ?: "Paris"; meteo = meteoRepository.getMeteo(ville) } catch (e: Exception) {} } }
    
    val moisNoms = listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = { TopAppBar(title = { Text("Calendrier 📅", fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { MeteoCard(meteo, ville, estConnecte, phaseLune) }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth-- }) { Text("◀") }
                            Text("${moisNoms[currentMonth]} $currentYear", fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++ }) { Text("▶") }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) { listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.bodySmall.fontSize, color = CouleursApp.VertPrincipal) } }
                        val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, 1)
                        val firstDay = cal.get(Calendar.DAY_OF_WEEK); val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2
                        for (row in 0 until ((offset + daysInMonth + 6) / 7)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    val dayNumber = row * 7 + col - offset + 1
                                    if (dayNumber in 1..daysInMonth) {
                                        val isSelected = dayNumber == selectedDay
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(if (isSelected) CouleursApp.VertPrincipal else Color.Transparent, RoundedCornerShape(8.dp)).clickable { selectedDay = dayNumber; val calJour = Calendar.getInstance(); calJour.set(currentYear, currentMonth, dayNumber, rappelHeure, rappelMinute, 0); selectedTimestamp = calJour.timeInMillis; val rappel = rappelRepository.getRappelSync(selectedTimestamp); rappelActif = rappel?.estActif ?: false; rappelNote = rappel?.note ?: ""; showRappelDialog = true }.padding(4.dp), contentAlignment = Alignment.Center) { Text("$dayNumber", color = if (isSelected) CouleursApp.Blanc else CouleursApp.TexteFonce) }
                                    } else Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }
            item { Text("Plantes plantées (${legumesPlantes.size}) :", fontWeight = FontWeight.Bold) }
            legumesPlantes.forEach { legumeNom -> val legume = legumes.find { it.nom == legumeNom }; if (legume != null) item { CalendrierLegumeCard(legume, datesPlantation[legume.nom]) } }
        }
    }
    
    if (showRappelDialog) {
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        val dateRappel = Date(selectedTimestamp)
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showRappelDialog = false },
            title = { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("📅 ${dateFormat.format(dateRappel)}", fontWeight = FontWeight.Bold); IconButton(onClick = { scope.launch { val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0); rappelRepository.toggleRappel(cal.timeInMillis, "Rappel"); rappelActif = !rappelActif } }) { Text(if (rappelActif) "🔔" else "🔕", style = MaterialTheme.typography.titleLarge) } } },
            text = { Column {
                Text(if (rappelActif) "Rappel activé" else "Rappel désactivé", color = if (rappelActif) CouleursApp.VertPrincipal else MaterialTheme.colorScheme.error)
                Card(modifier = Modifier.fillMaxWidth().clickable { val tp = TimePickerDialog(context, { _, h, m -> rappelHeure = h; rappelMinute = m }, rappelHeure, rappelMinute, true); tp.show() }, colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)) { Row(modifier = Modifier.padding(16.dp)) { Text("⏰ Heure : ${String.format("%02d", rappelHeure)}:${String.format("%02d", rappelMinute)}", fontWeight = FontWeight.Bold) } }
                OutlinedTextField(value = rappelNote, onValueChange = { rappelNote = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Button(onClick = { scope.launch { val cal = Calendar.getInstance(); cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0); val ts = cal.timeInMillis; val rappel = rappelRepository.getRappel(ts); if (rappel != null) rappelRepository.mettreAJourNote(ts, rappelNote) else rappelRepository.ajouterRappel(ts, "Rappel", rappelNote) }; showRappelDialog = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Enregistrer") }
            } },
            confirmButton = { TextButton(onClick = { showRappelDialog = false }) { Text("Fermer") } }
        )
    }
}

@Composable
fun MeteoCard(meteo: MeteoData?, ville: String, estConnecte: Boolean, phaseLune: PhaseLune? = null) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("🌦️ Météo à $ville", fontWeight = FontWeight.Bold)
            if (phaseLune != null) Text("${phaseLune.emoji} ${phaseLune.nom}", color = CouleursApp.VertPrincipal)
            if (!estConnecte) Text("📡 Hors-ligne")
            else if (meteo == null) Text("Météo indisponible")
            else { Text("🌡️ ${meteo.temperature}°C"); Text("☁️ ${meteo.description}"); Text("💧 ${meteo.humidite}%"); Text("🌬️ ${meteo.vent} m/s") }
        }
    }
}

@Composable
fun CalendrierLegumeCard(legume: LegumeEntity, datePlantation: Long? = null) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
            if (datePlantation != null) { val df = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE); Text("🌱 Planté le ${df.format(Date(datePlantation))}") }
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
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Filtrer par méthode :", fontWeight = FontWeight.Bold); Column { methodes.forEach { m -> FilterChip(selected = filtre == m, onClick = { filtre = m }, label = { Text(m) }, modifier = Modifier.padding(vertical = 4.dp)) } } }
            item { Text("${legumes.filter { if (filtre == "Tous") true else it.conservation.contains(filtre, ignoreCase = true) }.size} plantes") }
            legumes.filter { if (filtre == "Tous") true else it.conservation.contains(filtre, ignoreCase = true) }.forEach { legume -> item { ConservationCard(legume) } }
        }
    }
    if (showAide) AideConservationDialog(onDismiss = { showAide = false })
}

@Composable
fun AideConservationDialog(onDismiss: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("sechage") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📖 Guide de conservation", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TabRow(selectedTabIndex = when(selectedOnglet) { "sechage" -> 0; "lacto" -> 1; "conserves" -> 2; else -> 3 }, containerColor = CouleursApp.VertPale) {
                    Tab(selected = selectedOnglet == "sechage", onClick = { selectedOnglet = "sechage" }, text = { Text("🌬️", fontSize = MaterialTheme.typography.bodySmall.fontSize) })
                    Tab(selected = selectedOnglet == "lacto", onClick = { selectedOnglet = "lacto" }, text = { Text("🥬", fontSize = MaterialTheme.typography.bodySmall.fontSize) })
                    Tab(selected = selectedOnglet == "conserves", onClick = { selectedOnglet = "conserves" }, text = { Text("🫙", fontSize = MaterialTheme.typography.bodySmall.fontSize) })
                    Tab(selected = selectedOnglet == "congelation", onClick = { selectedOnglet = "congelation" }, text = { Text("❄️", fontSize = MaterialTheme.typography.bodySmall.fontSize) })
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    when (selectedOnglet) {
                        "sechage" -> { item { Text("🌬️ Séchage", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }; item { Text("• Tranches fines (3-5mm)\n• 50-60°C\n• 6-12h\n• Bocaux hermétiques\n• 6-12 mois") }; item { Text("🥕 Tomates, carottes, courgettes, oignons, herbes") } }
                        "lacto" -> { item { Text("🥬 Lactofermentation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }; item { Text("• Sel sans iode : 20-30g/L\n• Légumes immergés\n• 18-22°C\n• 1-4 semaines") }; item { Text("🥕 Choux, carottes, radis, concombres") } }
                        "conserves" -> { item { Text("🫙 Conserves", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }; item { Text("• Stériliser bocaux\n• 2cm de vide\n• Eau salée 20g/L\n• 100°C pendant 1h-1h30") }; item { Text("🥕 Tomates, haricots, petits pois") } }
                        "congelation" -> { item { Text("❄️ Congélation", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }; item { Text("• Blanchir 2-3 min\n• Eau glacée\n• À plat\n• -18°C\n• 8-12 mois") }; item { Text("🥕 Haricots, petits pois, épinards") } }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)) { Text("Fermer") } }
    )
}

@Composable
fun ConservationCard(legume: LegumeEntity) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
            Text(legume.conservation)
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
    
    if (selectedVariete != null) VarieteDetailScreen(variete = selectedVariete!!, onBack = { selectedVariete = null })
    else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = { TopAppBar(title = { Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.Blanc) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Retour", tint = CouleursApp.Blanc) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CouleursApp.VertPrincipal, titleContentColor = CouleursApp.Blanc)) }
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(24.dp)).background(CouleursApp.VertPale), contentAlignment = Alignment.Center) { Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayLarge) } }
                item { InfoCard("Catégorie", legume.categorie) }
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
                    item { Text("🌱 Variétés (${varietes.size}) :", fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal) }
                    items(varietes) { variete ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { selectedVariete = variete }, colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
                            Row(modifier = Modifier.padding(16.dp)) { Text("🌿 ${variete.nom}", fontWeight = FontWeight.Bold) }
                        }
                    }
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
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { InfoCard("Description", variete.description) }
            item { InfoCard("Semis", variete.semis) }
            item { InfoCard("Plantation", variete.plantation) }
            item { InfoCard("Récolte", variete.recolte) }
            item { InfoCard("Particularités", variete.particularites) }
        }
    }
}

// ============== CARTE LÉGUME ==============
@Composable
fun LegumeCard(legume: LegumeEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(getEmojiCategorie(legume.categorie), style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(legume.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                Text("${legume.categorie} - ${legume.difficulte}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ============== DIALOG SÉLECTION VARIÉTÉ ==============
@Composable
fun VarieteSelectionDialog(
    legumeNom: String,
    varieteRepository: VarieteRepository,
    onVarieteChoisie: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val varietes by varieteRepository.getVarietesForLegume(legumeNom).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch { varieteRepository.ajouterVarietesPredefinies() }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Variétés de $legumeNom", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    Text("🌱 Variété standard", modifier = Modifier.fillMaxWidth().clickable { onVarieteChoisie(legumeNom) }.padding(16.dp), fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
                    HorizontalDivider()
                }
                if (varietes.isEmpty()) {
                    item { Text("Chargement des variétés...", modifier = Modifier.padding(16.dp), color = CouleursApp.TexteFonce) }
                } else {
                    items(varietes) { variete ->
                        Text("🌿 ${variete.nom}", modifier = Modifier.fillMaxWidth().clickable { onVarieteChoisie("${legumeNom} (${variete.nom})") }.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                        Text(variete.description, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce)
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ============== FONCTIONS PLANTES ==============
fun estVivace(nomLegume: String): Boolean {
    val vivaces = listOf("Lavande", "Menthe", "Thym", "Romarin", "Ciboulette", "Topinambour")
    return nomLegume in vivaces
}

fun estPlanteVolumineuse(nomLegume: String): Boolean {
    val plantesVolumineuses = listOf("Tomate", "Courgette", "Potiron", "Courge", "Aubergine", "Poivron", "Concombre", "Melon", "Chou", "Brocoli", "Chou-fleur", "Topinambour")
    return nomLegume in plantesVolumineuses
}

fun peutPlanterIci(carre: CarreEntity, caseNumero: Int, legumeNom: String): Boolean {
    if (!estPlanteVolumineuse(legumeNom)) return true
    val casesAdjacentes = when (caseNumero) {
        1 -> listOf(2, 4, 5); 2 -> listOf(1, 3, 4, 5, 6); 3 -> listOf(2, 5, 6)
        4 -> listOf(1, 2, 5, 7, 8); 5 -> listOf(1, 2, 3, 4, 6, 7, 8, 9); 6 -> listOf(2, 3, 5, 8, 9)
        7 -> listOf(4, 5, 8); 8 -> listOf(4, 5, 6, 7, 9); 9 -> listOf(5, 6, 8)
        else -> emptyList()
    }
    val legumesAdjacents = casesAdjacentes.mapNotNull { case -> when (case) { 1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3; 4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6; 7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9; else -> null } }
    return !legumesAdjacents.any { legume -> legume != null && estPlanteVolumineuse(legume) }
}

fun estBonneAssociation(legume1: String, legume2: String): Boolean {
    val bonnes = mapOf("Carotte" to listOf("Tomate", "Salade"), "Tomate" to listOf("Carotte", "Basilic"), "Basilic" to listOf("Tomate"))
    return bonnes[legume1]?.contains(legume2) == true || bonnes[legume2]?.contains(legume1) == true
}

fun estMauvaiseAssociation(legume1: String, legume2: String): Boolean {
    val mauvaises = mapOf("Tomate" to listOf("Pomme de terre"), "Oignon" to listOf("Haricot", "Pois"))
    return mauvaises[legume1]?.contains(legume2) == true || mauvaises[legume2]?.contains(legume1) == true
}

// Légende des couleurs
@Composable
fun LegendeCouleurs() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Légende", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(20.dp).background(Color(0xFF4CAF50).copy(alpha = 0.5f))); Text(" Bonne association", style = MaterialTheme.typography.bodySmall) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(20.dp).background(Color(0xFFFF9800).copy(alpha = 0.5f))); Text(" Neutre", style = MaterialTheme.typography.bodySmall) }
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(20.dp).background(Color(0xFFF44336).copy(alpha = 0.5f))); Text(" Mauvaise association", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun LegendeCompacte() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Creme)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(16.dp).background(Color(0xFF4CAF50).copy(alpha = 0.5f))); Text(" Bonne", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(16.dp).background(Color(0xFFFF9800).copy(alpha = 0.5f))); Text(" Neutre", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(16.dp).background(Color(0xFFF44336).copy(alpha = 0.5f))); Text(" Mauvaise", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun Grille3x3(carre: CarreEntity, onSousCarreClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val legumes = listOfNotNull(carre.case1, carre.case2, carre.case3, carre.case4, carre.case5, carre.case6, carre.case7, carre.case8, carre.case9)
    val toutesMemePlante = legumes.size == 9 && legumes.distinct().size == 1
    
    if (toutesMemePlante) {
        Box(modifier = modifier.aspectRatio(1f).background(Color(0xFF4CAF50).copy(alpha = 0.2f)).border(2.dp, CouleursApp.VertPrincipal).clickable { onSousCarreClick(1) }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(legumes[0], fontWeight = FontWeight.Bold); Text("✅") }
        }
    } else {
        Column(modifier = modifier.aspectRatio(1f).border(2.dp, CouleursApp.VertPrincipal)) {
            for (row in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..2) {
                        val caseNumero = row * 3 + col + 1
                        val legume = when (caseNumero) { 1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3; 4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6; 7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9; else -> null }
                        val couleurFond = if (legume != null) {
                            val autres = legumes.filter { it != legume }
                            if (autres.isEmpty()) Color(0xFF4CAF50).copy(alpha = 0.3f)
                            else { val mauvaise = autres.any { estMauvaiseAssociation(legume, it) }; val bonne = autres.any { estBonneAssociation(legume, it) }; when { mauvaise -> Color(0xFFF44336).copy(alpha = 0.3f); bonne -> Color(0xFF4CAF50).copy(alpha = 0.3f); else -> Color(0xFFFF9800).copy(alpha = 0.3f) } }
                        } else CouleursApp.Blanc
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(couleurFond).border(1.dp, CouleursApp.VertPrincipal).clickable { onSousCarreClick(caseNumero) }, contentAlignment = Alignment.Center) { Text(legume ?: "", fontSize = MaterialTheme.typography.bodySmall.fontSize) }
                    }
                }
            }
        }
    }
}

fun getEmojiCategorie(categorie: String): String {
    return when {
        categorie.contains("Racine", ignoreCase = true) -> "🥕"
        categorie.contains("Tubercule", ignoreCase = true) -> "🥔"
        categorie.contains("Fruit", ignoreCase = true) -> "🍅"
        categorie.contains("Feuille", ignoreCase = true) -> "🥬"
        categorie.contains("Légumineuse", ignoreCase = true) -> "🫘"
        categorie.contains("Alliacé", ignoreCase = true) -> "🧅"
        categorie.contains("Chou", ignoreCase = true) -> "🥦"
        categorie.contains("Cucurbitacée", ignoreCase = true) -> "🎃"
        categorie.contains("Fleur", ignoreCase = true) -> "🌸"
        categorie.contains("Aromatique", ignoreCase = true) -> "🌿"
        else -> "🌱"
    }
}

@Composable
fun InfoCard(titre: String, contenu: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, color = CouleursApp.VertPrincipal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(contenu)
        }
    }
}
