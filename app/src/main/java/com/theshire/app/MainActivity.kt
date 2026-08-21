package com.theshire.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.LegumeEntity
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.theme.PotagerShireTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PotagerShireTheme {
                MainScreen()
            }
        }
    }
}

// Écran principal avec navigation
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("accueil") }
    
    when (currentScreen) {
        "accueil" -> AccueilScreen(
            onNavigateToBibliotheque = { currentScreen = "bibliotheque" },
            onNavigateToJardin = { currentScreen = "jardin" },
            onNavigateToCalendrier = { currentScreen = "calendrier" }
        )
        "bibliotheque" -> BibliothequeScreen(
            onBack = { currentScreen = "accueil" }
        )
        "jardin" -> JardinScreen(
            onBack = { currentScreen = "accueil" }
        )
        "calendrier" -> CalendrierScreen(
            onBack = { currentScreen = "accueil" }
        )
    }
}

// Page d'accueil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccueilScreen(
    onNavigateToBibliotheque: () -> Unit,
    onNavigateToJardin: () -> Unit,
    onNavigateToCalendrier: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Potager de la Comté 🌱", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌱",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bienvenue dans votre potager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Bouton Bibliothèque
            MenuButton(
                icon = Icons.Default.MenuBook,
                titre = "Bibliothèque",
                description = "Fiches de culture des légumes",
                onClick = onNavigateToBibliotheque
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton Jardin
            MenuButton(
                icon = Icons.Default.GridView,
                titre = "Mon Jardin",
                description = "Grille de 1m² en 9 carrés",
                onClick = onNavigateToJardin
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton Calendrier
            MenuButton(
                icon = Icons.Default.CalendarMonth,
                titre = "Calendrier",
                description = "Opérations culturales et météo",
                onClick = onNavigateToCalendrier
            )
        }
    }
}

@Composable
fun MenuButton(
    icon: ImageVector,
    titre: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = titre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Écran Bibliothèque (avec les fiches de culture)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliothequeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { LegumeRepository(context) }
    val legumes by repository.legumes.collectAsState(initial = emptyList())
    var selectedLegume by remember { mutableStateOf<LegumeEntity?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        repository.ajouterLegumesPredefinis()
    }
    
    if (selectedLegume != null) {
        LegumeDetailScreen(
            legume = selectedLegume!!,
            onBack = { selectedLegume = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("Bibliothèque 📚", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${legumes.size} légumes dans votre bibliothèque",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(legumes, key = { it.id }) { legume ->
                    LegumeCard(
                        legume = legume,
                        onClick = { selectedLegume = legume },
                        onDelete = { 
                            scope.launch { 
                                repository.supprimerLegume(legume) 
                            }
                        }
                    )
                }
            }
        }
    }
}

// Écran Jardin (à créer)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JardinScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Mon Jardin 🏡", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏡",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Votre jardin arrive bientôt !",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grille de 1m² divisée en 9 carrés",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Écran Calendrier (à créer)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendrierScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Calendrier 📅", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Votre calendrier arrive bientôt !",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Météo et rappels des opérations culturales",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Fiche détaillée d'un légume
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegumeDetailScreen(
    legume: LegumeEntity,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(legume.nom, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InfoCard("Catégorie", legume.categorie) }
            item { InfoCard("Difficulté", legume.difficulte) }
            item { InfoCard("Exposition", legume.exposition) }
            item { InfoCard("Sol", legume.sol) }
            item { InfoCard("Arrosage", legume.arrosage) }
            item { InfoCard("Température", legume.temperature) }
            item { InfoCard("Semis", legume.semis) }
            item { InfoCard("Plantation", legume.plantation) }
            item { InfoCard("Récolte", legume.recolte) }
            item { InfoCard("Entretien", legume.entretien) }
            item { InfoCard("Maladies", legume.maladies) }
            item { InfoCard("Prévention naturelle", legume.prevention) }
            item { InfoCard("Bonnes associations", legume.bonnesAssociations) }
            item { InfoCard("Mauvaises associations", legume.mauvaisesAssociations) }
        }
    }
}

// Carte d'un légume dans la liste
@Composable
fun LegumeCard(
    legume: LegumeEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = legume.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Catégorie : ${legume.categorie}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Difficulté : ${legume.difficulte}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Carte d'information dans la fiche détaillée
@Composable
fun InfoCard(titre: String, contenu: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contenu,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
