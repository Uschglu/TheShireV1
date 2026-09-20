package com.theshire.app.ui.screens

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.theshire.app.data.AdventiceEntity
import com.theshire.app.ui.AdventiceRepository
import com.theshire.app.ui.components.InfoCard
import com.theshire.app.ui.theme.CouleursApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventicesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AdventiceRepository(context) }
    val adventices by repository.adventices.collectAsState(initial = emptyList())
    var selected by remember { mutableStateOf<AdventiceEntity?>(null) }

    LaunchedEffect(Unit) { repository.ajouterAdventicesPredefinies() }

    if (selected != null) {
        AdventiceDetailScreen(selected!!, onBack = { selected = null })
    } else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = {
                TopAppBar(
                    title = {
                        Text("Adventices 🌿", fontWeight = FontWeight.Bold, color = Color.White)
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
                    Text("${adventices.size} adventices courantes", color = CouleursApp.TexteFonce)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Les adventices (mauvaises herbes) indiquent la nature de votre sol.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                }
                items(adventices, key = { it.id }) { a ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selected = a },
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(a.emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    a.nom,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text(
                                    a.indicationSol,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.VertPrincipal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
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
fun AdventiceDetailScreen(adventice: AdventiceEntity, onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(adventice.nom, fontWeight = FontWeight.Bold, color = Color.White)
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
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CouleursApp.VertPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(adventice.emoji, style = MaterialTheme.typography.displayLarge)
                }
            }
            item { InfoCard("Nom scientifique", adventice.nomScientifique) }
            item { InfoCard("Description", adventice.description) }
            item { InfoCard("Ce qu'elle indique", adventice.indicationSol) }
            item { InfoCard("Type de sol", adventice.typeSol) }
        }
    }
}
