package com.theshire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.InfoCard
import com.theshire.app.data.AgricultureUrbaine
import com.theshire.app.data.ConseilUrbain
import com.theshire.app.data.MaterielUrbain
import com.theshire.app.data.PlanteUrbaine
import com.theshire.app.ui.components.InfoCard
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran Agriculture Urbaine.
 * 
 * Trois onglets internes :
 * - 📖 Conseils : les conseils généraux (exposition, arrosage, vent, froid...)
 * - 🛠️ Matériel : le matériel spécifique à l'urbain (pots, substrats, systèmes...)
 * - 🌱 Plantes : les plantes adaptées à la culture en pot
 * 
 * Cet écran est intégré comme sous-onglet de "Jardin".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranAgricultureUrbaine() {
    var selectedOnglet by remember { mutableStateOf("conseils") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Barre d'onglets interne
        TabRow(
            selectedTabIndex = when (selectedOnglet) {
                "conseils" -> 0
                "materiel" -> 1
                else -> 2
            },
            containerColor = CouleursApp.VertPrincipal,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedOnglet == "conseils",
                onClick = { selectedOnglet = "conseils" },
                text = {
                    Text(
                        "📖 Conseils",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "conseils") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "materiel",
                onClick = { selectedOnglet = "materiel" },
                text = {
                    Text(
                        "🛠️ Matériel",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "materiel") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "plantes",
                onClick = { selectedOnglet = "plantes" },
                text = {
                    Text(
                        "🌱 Plantes",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedOnglet == "plantes") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
        }
        
        // Contenu selon l'onglet
        when (selectedOnglet) {
            "conseils" -> OngletConseilsUrbains()
            "materiel" -> OngletMaterielUrbain()
            else -> OngletPlantesUrbaines()
        }
    }
}

// ============================================================
// ONGLET 1 : CONSEILS
// ============================================================

@Composable
fun OngletConseilsUrbains() {
    val conseils = remember { AgricultureUrbaine.getConseils() }
    var conseilSelectionne by remember { mutableStateOf<ConseilUrbain?>(null) }
    
    // Si un conseil est sélectionné, afficher sa fiche détaillée
    if (conseilSelectionne != null) {
        FicheConseil(
            conseil = conseilSelectionne!!,
            onBack = { conseilSelectionne = null }
        )
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "🏙️ Bienvenue en agriculture urbaine !",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.VertPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Cultivez sur votre balcon, terrasse ou à l'intérieur, même sans jardin.",
                style = MaterialTheme.typography.bodyMedium,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
            )
        }
        
        items(conseils, key = { it.id }) { conseil ->
            CardConseil(
                conseil = conseil,
                onClick = { conseilSelectionne = conseil }
            )
        }
    }
}

@Composable
fun CardConseil(
    conseil: ConseilUrbain,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(conseil.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    conseil.titre,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    conseil.categorie,
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FicheConseil(
    conseil: ConseilUrbain,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${conseil.emoji} ${conseil.titre}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête avec emoji
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(conseil.emoji, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            conseil.titre,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            conseil.categorie,
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📝 Description",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            conseil.description,
                            color = CouleursApp.TexteFonce,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Astuces
            if (conseil.astuces.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "💡 Astuces pratiques",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            conseil.astuces.forEach { astuce ->
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        "•",
                                        fontWeight = FontWeight.Bold,
                                        color = CouleursApp.VertPrincipal,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        astuce,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CouleursApp.TexteFonce
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// ONGLET 2 : MATÉRIEL
// ============================================================

@Composable
fun OngletMaterielUrbain() {
    val materiel = remember { AgricultureUrbaine.getMateriel() }
    val categories = remember { AgricultureUrbaine.getCategoriesMateriel() }
    var categorieSelectionnee by remember { mutableStateOf<String?>(null) }
    var materielSelectionne by remember { mutableStateOf<MaterielUrbain?>(null) }
    
    // Si un matériel est sélectionné, afficher sa fiche détaillée
    if (materielSelectionne != null) {
        FicheMateriel(
            materiel = materielSelectionne!!,
            onBack = { materielSelectionne = null }
        )
        return
    }
    
    val materielFiltre = if (categorieSelectionnee == null) {
        materiel
    } else {
        materiel.filter { it.categorie == categorieSelectionnee }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "🛠️ Matériel pour l'urbain",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.VertPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Pots, substrats, arrosage, protection... Tout ce qu'il faut pour cultiver en ville.",
                style = MaterialTheme.typography.bodyMedium,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
            )
        }
        
        // Filtres par catégorie
        item {
            Column {
                Text(
                    "Filtrer par catégorie :",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = categorieSelectionnee == null,
                    onClick = { categorieSelectionnee = null },
                    label = { Text("Tous", fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                    modifier = Modifier.padding(vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = categorieSelectionnee == cat,
                        onClick = { categorieSelectionnee = if (categorieSelectionnee == cat) null else cat },
                        label = { Text(getEmojiCategorieMateriel(cat) + " " + cat, fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                        modifier = Modifier.padding(vertical = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
        
        item {
            Text(
                "${materielFiltre.size} élément(s)",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.TexteFonce,
                fontStyle = FontStyle.Italic
            )
        }
        
        items(materielFiltre, key = { it.id }) { mat ->
            CardMateriel(
                materiel = mat,
                onClick = { materielSelectionne = mat }
            )
        }
    }
}

@Composable
fun CardMateriel(
    materiel: MaterielUrbain,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(materiel.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    materiel.nom,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    materiel.categorie,
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                if (materiel.prixIndicatif.isNotEmpty()) {
                    Text(
                        "💶 ${materiel.prixIndicatif}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FicheMateriel(
    materiel: MaterielUrbain,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${materiel.emoji} ${materiel.nom}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
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
            // En-tête
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(materiel.emoji, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            materiel.nom,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            materiel.categorie,
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                        if (materiel.prixIndicatif.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "💶 Prix indicatif : ${materiel.prixIndicatif}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }
            
            // Description
            item {
                InfoCard("📝 Description", materiel.description)
            }
            
            // Utilisation
            if (materiel.utilisation.isNotEmpty()) {
                item {
                    InfoCard("🎯 Utilisation", materiel.utilisation)
                }
            }
            
            // Conseil
            if (materiel.conseil.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("💡", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Conseil",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    materiel.conseil,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getEmojiCategorieMateriel(categorie: String): String = when {
    categorie.contains("Contenant", true) -> "🏺"
    categorie.contains("Substrat", true) -> "🌱"
    categorie.contains("Arrosage", true) -> "💧"
    categorie.contains("Protection", true) -> "🛡️"
    categorie.contains("Éclairage", true) -> "💡"
    else -> "📦"
}

// ============================================================
// ONGLET 3 : PLANTES
// ============================================================

@Composable
fun OngletPlantesUrbaines() {
    val plantes = remember { AgricultureUrbaine.getPlantesAdaptees() }
    var milieuFiltre by remember { mutableStateOf<String?>(null) }
    var planteSelectionnee by remember { mutableStateOf<PlanteUrbaine?>(null) }
    
    // Si une plante est sélectionnée, afficher sa fiche détaillée
    if (planteSelectionnee != null) {
        FichePlanteUrbaine(
            plante = planteSelectionnee!!,
            onBack = { planteSelectionnee = null }
        )
        return
    }
    
    val plantesFiltrees = if (milieuFiltre == null) {
        plantes
    } else {
        plantes.filter { it.milieu == milieuFiltre || it.milieu == "Les deux" }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "🌱 Plantes pour l'urbain",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.VertPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Les espèces les mieux adaptées à la culture en pot, sur balcon ou à l'intérieur.",
                style = MaterialTheme.typography.bodyMedium,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
            )
        }
        
        // Filtres par milieu
        item {
            Column {
                Text(
                    "Filtrer par milieu :",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = milieuFiltre == null,
                    onClick = { milieuFiltre = null },
                    label = { Text("Tous", fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                    modifier = Modifier.padding(vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    selected = milieuFiltre == "Balcon",
                    onClick = { milieuFiltre = if (milieuFiltre == "Balcon") null else "Balcon" },
                    label = { Text("🏙️ Balcon", fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                    modifier = Modifier.padding(vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                FilterChip(
                    selected = milieuFiltre == "Intérieur",
                    onClick = { milieuFiltre = if (milieuFiltre == "Intérieur") null else "Intérieur" },
                    label = { Text("🏠 Intérieur", fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                    modifier = Modifier.padding(vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
        
        item {
            Text(
                "${plantesFiltrees.size} plante(s)",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.TexteFonce,
                fontStyle = FontStyle.Italic
            )
        }
        
        items(plantesFiltrees, key = { it.nomEspece }) { plante ->
            CardPlanteUrbaine(
                plante = plante,
                onClick = { planteSelectionnee = plante }
            )
        }
    }
}

@Composable
fun CardPlanteUrbaine(
    plante: PlanteUrbaine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plante.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plante.nomEspece,
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.TexteFonce,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${plante.milieu} · ${plante.difficulte}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichePlanteUrbaine(
    plante: PlanteUrbaine,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${plante.emoji} ${plante.nomEspece}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
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
            // En-tête
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(plante.emoji, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            plante.nomEspece,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${plante.milieu} · ${plante.difficulte}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Caractéristiques
            item { InfoCard("☀️ Exposition", plante.exposition) }
            item { InfoCard("🏺 Contenant recommandé", plante.contenantRequis) }
            item { InfoCard("💧 Arrosage", plante.arrosage) }
            item { InfoCard("📅 Période de plantation", plante.periodePlantation) }
            item { InfoCard("🌾 Rendement", plante.rendement) }
            
            // Astuce
            if (plante.astuce.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("💡", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Astuce",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    plante.astuce,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
