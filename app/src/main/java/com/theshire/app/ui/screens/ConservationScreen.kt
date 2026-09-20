package com.theshire.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeRepository
import com.theshire.app.ui.components.AideConservationDialog
import com.theshire.app.ui.components.ConservationCard
import com.theshire.app.ui.theme.CouleursApp

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
        topBar = {
            TopAppBar(
                title = {
                    Text("Conservation 🥫", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAide = true }) {
                        Icon(Icons.Default.Help, "Aide", tint = Color.White)
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
                Text(
                    "Filtrer par méthode :",
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce
                )
                Column {
                    methodes.forEach { m ->
                        FilterChip(
                            selected = filtre == m,
                            onClick = { filtre = m },
                            label = { Text(m) },
                            modifier = Modifier.padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
            item {
                Text(
                    "${legumes.filter { if (filtre == "Tous") true else it.conservation.contains(filtre, true) }.size} plantes",
                    color = CouleursApp.TexteFonce
                )
            }
            items(
                legumes.filter {
                    if (filtre == "Tous") true
                    else it.conservation.contains(filtre, true)
                },
                key = { it.id }
            ) { legume ->
                ConservationCard(legume)
            }
        }
    }

    if (showAide) {
        AideConservationDialog(onDismiss = { showAide = false })
    }
}
