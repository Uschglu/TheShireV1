package com.theshire.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeEntity
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.components.LegumeCard
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequeScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("plantes") }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when (selectedOnglet) {
                "plantes" -> 0
                "adventices" -> 1
                else -> 2
            },
            containerColor = CouleursApp.VertPrincipal,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedOnglet == "plantes",
                onClick = { selectedOnglet = "plantes" },
                text = {
                    Text(
                        "🌱 Plantes",
                        fontWeight = FontWeight.Bold,
                        fontSize = androidx.compose.material3.MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedOnglet == "plantes") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "adventices",
                onClick = { selectedOnglet = "adventices" },
                text = {
                    Text(
                        "🌿 Adventices",
                        fontWeight = FontWeight.Bold,
                        fontSize = androidx.compose.material3.MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedOnglet == "adventices") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "reconnaissance",
                onClick = { selectedOnglet = "reconnaissance" },
                text = {
                    Text(
                        "📸 Identifier",
                        fontWeight = FontWeight.Bold,
                        fontSize = androidx.compose.material3.MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedOnglet == "reconnaissance") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
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

    if (selectedLegume != null) {
        LegumeDetailScreen(selectedLegume!!, onBack = { selectedLegume = null })
    } else {
        Scaffold(
            containerColor = CouleursApp.Creme,
            topBar = {
                TopAppBar(
                    title = {
                        Text("Bibliothèque 📚", fontWeight = FontWeight.Bold, color = Color.White)
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("🔍 Rechercher une plante...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
                item {
                    Text(
                        "${legumes.filter { it.nom.contains(searchQuery, true) }.size} plantes trouvées",
                        color = CouleursApp.TexteFonce
                    )
                }
                items(
                    legumes.filter { it.nom.contains(searchQuery, true) },
                    key = { it.id }
                ) { legume ->
                    LegumeCard(
                        legume = legume,
                        onClick = { selectedLegume = legume },
                        onDelete = { scope.launch { repository.supprimerLegume(legume) } }
                    )
                }
            }
        }
    }
}
