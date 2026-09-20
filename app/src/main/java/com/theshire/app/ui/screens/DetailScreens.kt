package com.theshire.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.VarieteEntity
import com.theshire.app.data.getEmojiCategorie
import com.theshire.app.data.VarieteRepository
import com.theshire.app.ui.components.InfoCard
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran de détail d'un légume : affiche toutes ses caractéristiques
 * (exposition, arrosage, semis, plantation, récolte, conservation...)
 * et la liste de ses variétés.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegumeDetailScreen(legume: LegumeEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    val varieteRepository = remember { VarieteRepository(context) }
    val varietes by varieteRepository.getVarietesForLegume(legume.nom)
        .collectAsState(initial = emptyList())
    var selectedVariete by remember { mutableStateOf<VarieteEntity?>(null) }

    LaunchedEffect(Unit) { varieteRepository.ajouterVarietesPredefinies() }

    if (selectedVariete != null) {
        VarieteDetailScreen(selectedVariete!!, onBack = { selectedVariete = null })
    } else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = {
                TopAppBar(
                    title = {
                        Text(legume.nom, fontWeight = FontWeight.Bold, color = Color.White)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(CouleursApp.VertPale),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            getEmojiCategorie(legume.categorie),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
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
                    item {
                        Text(
                            "🌱 Variétés (${varietes.size}) :",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal
                        )
                    }
                    items(varietes) { v ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVariete = v },
                            colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
                        ) {
                            Text(
                                "🌿 ${v.nom}",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Écran de détail d'une variété de légume.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarieteDetailScreen(variete: VarieteEntity, onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(variete.nom, fontWeight = FontWeight.Bold, color = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InfoCard("Description", variete.description) }
            item { InfoCard("Particularités", variete.particularites) }
        }
    }
}
