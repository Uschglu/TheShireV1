package com.theshire.app.ui.screens

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.theshire.app.ImageLoaderProvider
import com.theshire.app.data.BrandingApp
import com.theshire.app.data.LocalisationRepository
import com.theshire.app.data.LuneRepository
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.PrevisionJour
import com.theshire.app.ui.components.getEmojiMeteo
import com.theshire.app.ui.theme.CouleursApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
                f.outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it)
                }
                prefs.edit().putString("photo_path", f.absolutePath).apply()
                imagePath = f.absolutePath
            } catch (e: Exception) {
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val f = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    f.outputStream().use { output -> input.copyTo(output) }
                }
                prefs.edit().putString("photo_path", f.absolutePath).apply()
                imagePath = f.absolutePath
            } catch (e: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val v = localisationRepository.getVille()
            if (v != null) ville = v
            meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" })
        } catch (e: Exception) {
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // En-tête : nom de l'app + bouton paramètres
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
                    Text("⚙️", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Carte météo + lune
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .clickable {
                        showPrevisions = true
                        chargementPrevisions = true
                    },
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(listOf(CouleursApp.VertPale, CouleursApp.Blanc))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            getEmojiMeteo(meteo),
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            if (meteo != null) {
                                Text(
                                    "${meteo!!.temperature}°C",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text(meteo!!.description, color = CouleursApp.TexteFonce)
                            } else {
                                Text(
                                    "--°C",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text("Météo indisponible", color = CouleursApp.TexteFonce)
                            }
                            Text(
                                dateFormat.format(Date()),
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                            Text(
                                "${phaseLune.emoji} ${phaseLune.nom}",
                                color = CouleursApp.VertPrincipal,
                                fontWeight = FontWeight.Bold
                            )
                            if (ville.isNotEmpty()) {
                                Text(
                                    "📍 $ville",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Carte photo du jardin
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
            ) {
                if (imageFile != null && imageFile.exists()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val loader = remember { ImageLoaderProvider.getImageLoader(context) }
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Photo",
                            imageLoader = loader,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp))
                        )
                        IconButton(
                            onClick = { showPhotoDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(CouleursApp.VertPrincipal.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, "Changer", tint = Color.White)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = CouleursApp.VertClair
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "📸 Photo de mon jardin",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = CouleursApp.TexteFonce
                        )
                        Text(
                            "Prenez une photo ou choisissez une image",
                            textAlign = TextAlign.Center,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPhotoDialog = true },
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                        ) {
                            Text("Ajouter une photo")
                        }
                    }
                }
            }
        }
    }

    // Dialog prévisions météo
    if (showPrevisions) {
        AlertDialog(
            onDismissRequest = { showPrevisions = false },
            title = { Text("📅 Prévisions 7 jours", fontWeight = FontWeight.Bold) },
            text = {
                if (chargementPrevisions) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = CouleursApp.VertPrincipal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Chargement...", color = CouleursApp.TexteFonce)
                    }
                } else if (previsions.isEmpty()) {
                    Text("Prévisions indisponibles", color = CouleursApp.TexteFonce)
                } else {
                    Column {
                        previsions.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    p.date,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(p.emoji, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${p.tempMin.toInt()}° / ${p.tempMax.toInt()}°", color = CouleursApp.TexteFonce)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrevisions = false }) {
                    Text("Fermer", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }

    // Dialog choix photo
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Ajouter une photo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "📸 Prendre une photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoDialog = false
                                photoLauncher.launch(null)
                            }
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Text(
                        "🖼️ Choisir depuis la galerie",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    if (imageFile != null && imageFile.exists()) {
                        HorizontalDivider()
                        Text(
                            "🗑️ Supprimer",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    imageFile!!.delete()
                                    prefs.edit().remove("photo_path").apply()
                                    imagePath = null
                                    showPhotoDialog = false
                                }
                                .padding(16.dp),
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

    // Dialog tutoriel de bienvenue
    if (showTuto) {
        AlertDialog(
            onDismissRequest = {
                showTuto = false
                prefs.edit().putBoolean("tuto_vu_v5", true).apply()
            },
            title = {
                Text(
                    "🌱 Bienvenue dans ${BrandingApp.config.nomApp} !",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Intro
                    item {
                        Text(
                            "Découvrez tous les outils pour cultiver votre potager, en pleine terre ou en ville.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce
                        )
                    }

                    // Onglet 1 : Accueil
                    item {
                        Column {
                            Text(
                                "🏠 Accueil",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Météo locale, phase de lune, photo de votre jardin et prévisions sur 7 jours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 2 : Bibliothèque
                    item {
                        Column {
                            Text(
                                "📚 Bibliothèque",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Fiches détaillées des plantes et variétés (semis, plantation, arrosage, récolte), guide des adventices (mauvaises herbes), et reconnaissance photo pour identifier vos plantes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 3 : Jardin
                    item {
                        Column {
                            Text(
                                "🏡 Jardin",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Organisez votre potager : planches en pleine terre avec calcul automatique des associations de plantes (bonnes en vert, mauvaises en rouge), ou contenants urbains (pots, jardinières, tours). Analyse du sol par la méthode du bocal incluse.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 4 : Calendrier
                    item {
                        Column {
                            Text(
                                "📅 Calendrier",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Votre planning cultural automatique. Dès que vous plantez, l'app génère les rappels : tuteurage, buttage, éclaircissage, paillage, récolte... pour chaque légume, en pleine terre ou en pot. Ajoutez vos notes personnelles et recevez des notifications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 5 : Stocks
                    item {
                        Column {
                            Text(
                                "📦 Stocks",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.Terracotta
                            )
                            Text(
                                "Gérez vos ressources en 3 onglets :\n" +
                                    "• 🫘 Graines : sachets en stock (variété, quantité, fournisseur, année)\n" +
                                    "• 🌱 Jeunes plants : semis et plants en attente (stade, emplacement)\n" +
                                    "• 🛠️ Matériel : vos outils classés par usage (plantation, arrosage, taille...)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 6 : Conservation
                    item {
                        Column {
                            Text(
                                "🥫 Conservation",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Apprenez à conserver vos récoltes : séchage, lactofermentation, conserves et congélation. Pour chaque légume, les méthodes adaptées et les étapes détaillées.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Onglet 7 : Store
                    item {
                        Column {
                            Text(
                                "🛒 Store",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.Terracotta
                            )
                            Text(
                                "Bientôt : retrouvez ici les outils et produits recommandés par votre jardinerie partenaire.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Navigation
                    item {
                        Column {
                            Text(
                                "👆 Navigation",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "Glissez votre doigt horizontalement ou cliquez sur les billes en bas de l'écran pour passer d'une section à l'autre.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }

                    // Conseils d'utilisation
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CouleursApp.VertPale
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "💡 Astuce",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Commencez par la Bibliothèque pour découvrir les plantes. Puis créez votre première planche ou contenant dans Jardin. Le calendrier se remplira automatiquement !",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTuto = false
                        prefs.edit().putBoolean("tuto_vu_v5", true).apply()
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Commencer 🌱")
                }
            }
        )
    }
}
