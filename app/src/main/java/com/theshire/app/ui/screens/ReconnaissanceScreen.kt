package com.theshire.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.theshire.app.ImageLoaderProvider
import com.theshire.app.data.PlantIdentification
import com.theshire.app.data.PlantNetRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.io.File

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

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg")
            f.outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it)
            }
            imageUri = Uri.fromFile(f)
            resultats = emptyList()
            scope.launch {
                isAnalyzing = true
                try {
                    resultats = repo.identifierPlante(f)
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Erreur"
                }
                isAnalyzing = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val f = File(context.cacheDir, "plante_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                f.outputStream().use { output -> input.copyTo(output) }
            }
            imageUri = Uri.fromFile(f)
            resultats = emptyList()
            scope.launch {
                isAnalyzing = true
                try {
                    resultats = repo.identifierPlante(f)
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Erreur"
                }
                isAnalyzing = false
            }
        }
    }

    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text("Identifier une plante 📸", fontWeight = FontWeight.Bold, color = Color.White)
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { photoLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                    ) {
                        Text("📸 Photo")
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)
                    ) {
                        Text("🖼️ Galerie")
                    }
                }
            }

            if (imageUri != null) {
                item {
                    val loader = remember { ImageLoaderProvider.getImageLoader(context) }
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Plante",
                        imageLoader = loader,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            if (isAnalyzing) {
                item {
                    CircularProgressIndicator(color = CouleursApp.VertPrincipal)
                    Text("Analyse en cours...", color = CouleursApp.TexteFonce)
                }
            }

            if (errorMessage.isNotEmpty()) {
                item {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }

            if (resultats.isNotEmpty()) {
                item {
                    Text(
                        "🔍 Résultats :",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal
                    )
                }
            }

            resultats.forEach { r ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                r.nom,
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.TexteFonce
                            )
                            Text(
                                r.nomScientifique,
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                            if (r.probabilite > 0) {
                                Text(
                                    "Confiance : ${(r.probabilite * 100).toInt()}%",
                                    color = CouleursApp.VertPrincipal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
