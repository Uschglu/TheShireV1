package com.theshire.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theshire.app.ui.components.InfoCard
import com.theshire.app.ui.components.VarieteSelectionDialog
import com.theshire.app.data.AvertissementRotation
import com.theshire.app.data.CalculEmplacements
import com.theshire.app.data.ContenantEntity
import com.theshire.app.data.EmplacementContenantEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.NiveauRisque
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class TypeContenant(
    val id: String,
    val nom: String,
    val emoji: String,
    val description: String,
    val champsDimensions: List<ChampDimension>
)

data class ChampDimension(
    val id: String,
    val label: String,
    val unite: String = "cm",
    val defaut: String = ""
)

val TYPES_CONTENANTS = listOf(
    TypeContenant(
        id = "pot",
        nom = "Pot classique",
        emoji = "🪴",
        description = "Pot rond standard, pour une plante isolée",
        champsDimensions = listOf(ChampDimension("diametre", "Diamètre", "cm", "25"))
    ),
    TypeContenant(
        id = "jardiniere",
        nom = "Jardinière",
        emoji = "📦",
        description = "Rectangle allongé, pour plusieurs plants alignés",
        champsDimensions = listOf(
            ChampDimension("longueur", "Longueur", "cm", "60"),
            ChampDimension("largeur", "Largeur", "cm", "20")
        )
    ),
    TypeContenant(
        id = "suspendu",
        nom = "Pot suspendu",
        emoji = "🪝",
        description = "Suspendu à un crochet, pour retombantes",
        champsDimensions = listOf(ChampDimension("diametre", "Diamètre", "cm", "25"))
    ),
    TypeContenant(
        id = "tour",
        nom = "Tour empilable",
        emoji = "🗼",
        description = "Plusieurs étages empilés verticalement",
        champsDimensions = listOf(
            ChampDimension("diametre", "Diamètre d'un étage", "cm", "30"),
            ChampDimension("etages", "Nombre d'étages", "", "3")
        )
    ),
    TypeContenant(
        id = "sac",
        nom = "Sac géotextile",
        emoji = "🛍️",
        description = "Sac en tissu, idéal pour tomates et courgettes",
        champsDimensions = listOf(ChampDimension("diametre", "Diamètre", "cm", "40"))
    ),
    TypeContenant(
        id = "bac",
        nom = "Bac sur pied",
        emoji = "🌿",
        description = "Grand bac rectangulaire, surélevé du sol",
        champsDimensions = listOf(
            ChampDimension("longueur", "Longueur", "cm", "80"),
            ChampDimension("largeur", "Largeur", "cm", "40")
        )
    ),
    TypeContenant(
        id = "mur",
        nom = "Mur végétal",
        emoji = "🧱",
        description = "Poches fixées sur un mur, culture verticale",
        champsDimensions = listOf(
            ChampDimension("longueur", "Largeur", "cm", "60"),
            ChampDimension("hauteur", "Hauteur", "cm", "80")
        )
    ),
    TypeContenant(
        id = "reserve",
        nom = "Pot à réserve d'eau",
        emoji = "💧",
        description = "Pot avec réservoir, arrosage automatique",
        champsDimensions = listOf(ChampDimension("diametre", "Diamètre", "cm", "30"))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranContenants() {
    val context = LocalContext.current
    val repository = remember { ContenantRepository(context) }
    val contenants by repository.contenants.collectAsState(initial = emptyList())
    var showAjoutDialog by remember { mutableStateOf(false) }
    var contenantSelectionne by remember { mutableStateOf<ContenantEntity?>(null) }
    
    if (contenantSelectionne != null) {
        val contenantActuel = contenants.find { it.id == contenantSelectionne!!.id } ?: contenantSelectionne!!
        FicheContenant(
            contenant = contenantActuel,
            repository = repository,
            onBack = { contenantSelectionne = null }
        )
        return
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (contenants.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🪴", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aucun contenant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = CouleursApp.TexteFonce)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cliquez sur + pour ajouter votre premier pot, jardinière ou tour de culture.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${contenants.size} contenant(s) urbain(s)",
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(contenants, key = { it.id }) { contenant ->
                    CardContenant(
                        contenant = contenant,
                        repository = repository,
                        onClick = { contenantSelectionne = contenant }
                    )
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAjoutDialog = true },
            containerColor = CouleursApp.VertClair,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter un contenant")
        }
    }
    
    if (showAjoutDialog) {
        AjoutContenantDialog(
            onDismiss = { showAjoutDialog = false },
            onValider = { typeId, nom, dim1, dim2, dim3, etages, milieu ->
                val type = TYPES_CONTENANTS.find { it.id == typeId } ?: TYPES_CONTENANTS[0]
                val scope = CoroutineScope(Dispatchers.Main)
                scope.launch {
                    repository.creerContenant(
                        nom = nom, type = type.id, emoji = type.emoji,
                        dimension1 = dim1, dimension2 = dim2, dimension3 = dim3,
                        nombreEtages = etages, milieu = milieu
                    )
                }
                showAjoutDialog = false
            }
        )
    }
}

@Composable
fun CardContenant(
    contenant: ContenantEntity,
    repository: ContenantRepository,
    onClick: () -> Unit
) {
    var emplacements by remember { mutableStateOf<List<EmplacementContenantEntity>>(emptyList()) }
    
    LaunchedEffect(contenant.id) {
        emplacements = repository.getEmplacementsSync(contenant.id)
    }
    
    val nombreOccupes = emplacements.count { it.estOccupe() }
    val nombreTotal = emplacements.size
    
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(contenant.emoji, style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contenant.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce, style = MaterialTheme.typography.bodyLarge)
                Text(contenant.dimensionsTexte(), style = MaterialTheme.typography.bodySmall, color = CouleursApp.VertPrincipal, fontWeight = FontWeight.Bold)
                Text(contenant.milieu, style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce.copy(alpha = 0.6f))
                if (nombreTotal > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "$nombreOccupes / $nombreTotal planté(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (nombreOccupes == 0) CouleursApp.TexteFonce.copy(alpha = 0.5f) else CouleursApp.VertPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjoutContenantDialog(
    onDismiss: () -> Unit,
    onValider: (typeId: String, nom: String, dim1: Int, dim2: Int, dim3: Int, etages: Int, milieu: String) -> Unit
) {
    var etape by remember { mutableStateOf(1) }
    var typeSelectionne by remember { mutableStateOf<TypeContenant?>(null) }
    var nom by remember { mutableStateOf("") }
    var milieu by remember { mutableStateOf("Balcon") }
    val valeurs = remember { mutableStateMapOf<String, String>() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (etape == 1) "Choisir un type de contenant" else "Configurer le contenant", fontWeight = FontWeight.Bold) },
        text = {
            if (etape == 1) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TYPES_CONTENANTS, key = { it.id }) { type ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                                typeSelectionne = type
                                type.champsDimensions.forEach { champ -> valeurs[champ.id] = champ.defaut }
                                etape = 2
                            },
                            colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(type.emoji, style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(type.nom, fontWeight = FontWeight.Bold, color = CouleursApp.TexteFonce)
                                    Text(type.description, style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nom, onValueChange = { nom = it },
                        label = { Text("Nom (ex : Balcon Sud)") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Emplacement :", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Balcon", "Terrasse", "Intérieur").forEach { m ->
                            FilterChip(
                                selected = milieu == m, onClick = { milieu = m },
                                label = { Text(m, fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Dimensions :", style = MaterialTheme.typography.bodySmall, color = CouleursApp.TexteFonce, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    typeSelectionne?.champsDimensions?.forEach { champ ->
                        OutlinedTextField(
                            value = valeurs[champ.id] ?: "",
                            onValueChange = { valeurs[champ.id] = it.filter { c -> c.isDigit() } },
                            label = { Text("${champ.label} ${if (champ.unite.isNotEmpty()) "(${champ.unite})" else ""}") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp), singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (nom.isBlank() && typeSelectionne != null) {
                        Text(
                            "💡 Nom suggéré : ${typeSelectionne!!.emoji} ${typeSelectionne!!.nom}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal, fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (etape == 1) {
                TextButton(onClick = onDismiss) { Text("Annuler", color = CouleursApp.VertPrincipal) }
            } else {
                Button(
                    onClick = {
                        val type = typeSelectionne ?: return@Button
                        val nomFinal = if (nom.isBlank()) "${type.nom}" else nom
                        val dim1 = valeurs["diametre"]?.toIntOrNull() ?: valeurs["longueur"]?.toIntOrNull() ?: 20
                        val dim2 = valeurs["largeur"]?.toIntOrNull() ?: 0
                        val dim3 = valeurs["hauteur"]?.toIntOrNull() ?: 0
                        val etages = valeurs["etages"]?.toIntOrNull() ?: 1
                        onValider(type.id, nomFinal, dim1, dim2, dim3, etages, milieu)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) { Text("Créer") }
            }
        },
        dismissButton = {
            if (etape == 2) {
                TextButton(onClick = { etape = 1 }) { Text("Retour", color = CouleursApp.VertPrincipal) }
            }
        }
    )
}

fun calculerCouleursEmplacements(
    emplacements: List<EmplacementContenantEntity>,
    legumes: List<LegumeEntity>
): Map<Int, Color> {
    val resultat = mutableMapOf<Int, Color>()
    
    fun nomBase(nom: String?): String? {
        if (nom == null) return null
        return if (nom.contains("(")) nom.substringBefore("(").trim() else nom
    }
    
    fun infosLegume(nom: String?): LegumeEntity? {
        val base = nomBase(nom) ?: return null
        return legumes.find { it.nom == base }
    }
    
    fun verifierAssociation(plante1: String?, plante2: String?): String {
        if (plante1 == null || plante2 == null) return "neutre"
        val leg1 = infosLegume(plante1) ?: return "neutre"
        val base2 = nomBase(plante2) ?: return "neutre"
        if (leg1.bonnesAssociations.contains(base2, true)) return "bonne"
        if (leg1.mauvaisesAssociations.contains(base2, true)) return "mauvaise"
        return "neutre"
    }
    
    emplacements.forEach { emp ->
        if (emp.estVide()) {
            resultat[emp.numero] = CouleursApp.CaseVide
            return@forEach
        }
        
        val plante = emp.legumeNom
        var aBonne = false
        var aMauvaise = false
        
        emplacements.forEach { autre ->
            if (autre.numero != emp.numero && autre.estOccupe()) {
                when (verifierAssociation(plante, autre.legumeNom)) {
                    "bonne" -> aBonne = true
                    "mauvaise" -> aMauvaise = true
                }
            }
        }
        
        resultat[emp.numero] = when {
            aMauvaise -> CouleursApp.MauvaiseAssociation
            aBonne -> CouleursApp.BonneAssociation
            else -> CouleursApp.NeutreAssociation
        }
    }
    
    return resultat
}

@Composable
fun GrilleEmplacements(
    emplacements: List<EmplacementContenantEntity>,
    couleurs: Map<Int, Color>,
    legumes: List<LegumeEntity>,
    onEmplacementClick: (EmplacementContenantEntity) -> Unit
) {
    if (emplacements.isEmpty()) return
    
    val nombre = emplacements.size
    
    val (colonnes, taille) = when {
        nombre <= 3 -> Pair(nombre, 80.dp)
        nombre <= 6 -> Pair(3, 70.dp)
        nombre <= 12 -> Pair(4, 60.dp)
        nombre <= 20 -> Pair(5, 50.dp)
        else -> Pair(6, 45.dp)
    }
    
    val lignes = emplacements.chunked(colonnes)
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        lignes.forEach { ligne ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                ligne.forEach { emp ->
                    val couleur = couleurs[emp.numero] ?: CouleursApp.CaseVide
                    val emoji = if (emp.estOccupe()) {
                        val nomBase = if (emp.legumeNom!!.contains("(")) emp.legumeNom.substringBefore("(").trim() else emp.legumeNom
                        val legume = legumes.find { it.nom == nomBase }
                        getEmojiCategorieLegume(legume?.categorie ?: "")
                    } else {
                        "·"
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(taille)
                                .background(couleur, RoundedCornerShape(8.dp))
                                .border(1.dp, CouleursApp.VertPrincipal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { onEmplacementClick(emp) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                emoji,
                                fontSize = (taille.value / 3.5f).sp,
                                color = if (emp.estVide()) CouleursApp.TexteFonce.copy(alpha = 0.4f) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "${emp.numero}",
                            fontSize = 10.sp,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun getEmojiCategorieLegume(categorie: String): String = when {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FicheContenant(
    contenant: ContenantEntity,
    repository: ContenantRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val legumeRepository = remember { LegumeRepository(context) }
    val varieteRepository = remember { VarieteRepository(context) }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    
    val emplacements by repository.getEmplacementsPourContenant(contenant.id)
        .collectAsState(initial = emptyList())
    
    var emplacementSelectionne by remember { mutableStateOf<EmplacementContenantEntity?>(null) }
    var showAjoutPlante by remember { mutableStateOf(false) }
    var showMenuEmplacement by remember { mutableStateOf(false) }
    var showSuppression by remember { mutableStateOf(false) }
    var avertissement by remember { mutableStateOf<AvertissementRotation?>(null) }
    var showAvertissement by remember { mutableStateOf(false) }
    var legumeEnAttente by remember { mutableStateOf<LegumeEntity?>(null) }
    
    // Nouveaux états pour le choix de variété (BUG 3)
    var showVarieteSelection by remember { mutableStateOf(false) }
    var legumeChoisi by remember { mutableStateOf<LegumeEntity?>(null) }
    var emplacementPourVariete by remember { mutableStateOf<EmplacementContenantEntity?>(null) }
    
    val couleurs = remember(emplacements, legumes) {
        calculerCouleursEmplacements(emplacements, legumes)
    }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${contenant.emoji} ${contenant.nom}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSuppression = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.White)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(contenant.emoji, style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            contenant.nom,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = CouleursApp.TexteFonce
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${contenant.dimensionsTexte()} · ${contenant.milieu}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Surface utile : ${String.format("%.2f", contenant.surfaceM2())} m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            if (emplacements.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🎨 Aperçu des emplacements",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Vert = bonne association · Orange = neutre · Rouge = mauvaise",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            GrilleEmplacements(
                                emplacements = emplacements,
                                couleurs = couleurs,
                                legumes = legumes,
                                onEmplacementClick = { emp ->
                                    val empFixe = emp
                                    emplacementSelectionne = empFixe
                                    if (empFixe.estVide()) {
                                        showAjoutPlante = true
                                    } else {
                                        showMenuEmplacement = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    "🪴 Emplacements (${emplacements.size})",
                    fontWeight = FontWeight.Bold,
                    color = CouleursApp.VertPrincipal,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            if (emplacements.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Aucun emplacement pour l'instant",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CouleursApp.TexteFonce
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Plantez votre première plante : les emplacements seront créés automatiquement.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    emplacementSelectionne = EmplacementContenantEntity(
                                        contenantId = contenant.id,
                                        numero = 1
                                    )
                                    showAjoutPlante = true
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                            ) {
                                Text("Planter")
                            }
                        }
                    }
                }
            } else {
                items(emplacements, key = { it.id }) { emp ->
                    CardEmplacement(
                        emplacement = emp,
                        couleur = couleurs[emp.numero],
                        legumes = legumes,
                        onClick = {
                            val empFixe = emp
                            emplacementSelectionne = empFixe
                            if (empFixe.estVide()) {
                                showAjoutPlante = true
                            } else {
                                showMenuEmplacement = true
                            }
                        }
                    )
                }
            }
            
            if (contenant.notes.isNotEmpty()) {
                item {
                    InfoCard("📝 Notes", contenant.notes)
                }
            }
        }
    }
    
    // ===== Dialogue 1 : choix de la plante =====
    if (showAjoutPlante && emplacementSelectionne != null) {
        val empCible = emplacementSelectionne!!
        ChoixPlanteDialog(
            contenant = contenant,
            legumeRepository = legumeRepository,
            legumes = legumes,
            onPlanteChoisie = { legume ->
                showAjoutPlante = false
                legumeChoisi = legume
                emplacementPourVariete = empCible
                showVarieteSelection = true
            },
            onDismiss = {
                showAjoutPlante = false
                emplacementSelectionne = null
            }
        )
    }
    
    // ===== Dialogue 2 : choix de la variété (BUG 3) =====
    if (showVarieteSelection && legumeChoisi != null && emplacementPourVariete != null) {
        val legumeCible = legumeChoisi!!
        val empCible = emplacementPourVariete!!
        
        VarieteSelectionDialog(
            legumeNom = legumeCible.nom,
            varieteRepository = varieteRepository,
            onVarieteChoisie = { nomComplet ->
                showVarieteSelection = false
                
                scope.launch {
                    val associations = repository.verifierAssociationsContenant(
                        contenant = contenant,
                        numeroEmplacement = empCible.numero,
                        legumeNom = legumeCible.nom
                    )
                    val mauvaises = associations.filter { it.second == "mauvaise" }
                    
                    if (mauvaises.isNotEmpty()) {
                        avertissement = AvertissementRotation(
                            niveau = NiveauRisque.MOYEN,
                            message = "⚠️ Mauvaise association avec : ${mauvaises.joinToString(", ") { it.first }}"
                        )
                        legumeEnAttente = legumeCible.copy(nom = nomComplet)
                        emplacementSelectionne = empCible
                        showAvertissement = true
                    } else {
                        repository.planterDansEmplacement(
                            contenant = contenant,
                            numeroEmplacement = empCible.numero,
                            legumeNom = nomComplet,
                            legume = legumeCible
                        )
                        legumeChoisi = null
                        emplacementPourVariete = null
                        emplacementSelectionne = null
                    }
                }
            },
            onDismiss = {
                showVarieteSelection = false
                legumeChoisi = null
                emplacementPourVariete = null
                emplacementSelectionne = null
            }
        )
    }
    
    // ===== Dialogue 3 : menu emplacement occupé =====
    if (showMenuEmplacement && emplacementSelectionne != null) {
        val empAUtiliser = emplacementSelectionne!!
        AlertDialog(
            onDismissRequest = { showMenuEmplacement = false },
            title = { Text(empAUtiliser.legumeNom ?: "Emplacement", fontWeight = FontWeight.Bold) },
            text = { Text("Que souhaitez-vous faire ?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = empAUtiliser.id
                        showMenuEmplacement = false
                        emplacementSelectionne = null
                        scope.launch {
                            repository.viderEmplacement(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("🗑️ Vider l'emplacement")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMenuEmplacement = false
                    emplacementSelectionne = null
                }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue 4 : confirmation suppression contenant =====
    if (showSuppression) {
        val contenantASupprimer = contenant
        AlertDialog(
            onDismissRequest = { showSuppression = false },
            title = { Text("Supprimer le contenant ?", fontWeight = FontWeight.Bold) },
            text = { Text("Le contenant \"${contenant.nom}\" et tous ses emplacements seront supprimés.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuppression = false
                        scope.launch {
                            repository.supprimerContenant(contenantASupprimer)
                        }
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuppression = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
    
    // ===== Dialogue 5 : avertissement association =====
    if (showAvertissement && avertissement != null && legumeEnAttente != null && emplacementSelectionne != null) {
        val av = avertissement!!
        val legumeCible = legumeEnAttente!!
        val empCible = emplacementSelectionne!!
        val contenantFixe = contenant
        
        AlertDialog(
            onDismissRequest = {
                showAvertissement = false
                avertissement = null
                legumeEnAttente = null
                emplacementSelectionne = null
                legumeChoisi = null
                emplacementPourVariete = null
            },
            title = { Text("Avertissement", fontWeight = FontWeight.Bold) },
            text = { Text(av.message) },
            confirmButton = {
                Button(
                    onClick = {
                        val numero = empCible.numero
                        val nomComplet = legumeCible.nom
                        
                        showAvertissement = false
                        avertissement = null
                        legumeEnAttente = null
                        emplacementSelectionne = null
                        legumeChoisi = null
                        emplacementPourVariete = null
                        
                        scope.launch {
                            repository.planterDansEmplacement(
                                contenant = contenantFixe,
                                numeroEmplacement = numero,
                                legumeNom = nomComplet,
                                legume = legumeCible
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Planter quand même")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAvertissement = false
                    avertissement = null
                    legumeEnAttente = null
                    emplacementSelectionne = null
                    legumeChoisi = null
                    emplacementPourVariete = null
                }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
}

@Composable
fun CardEmplacement(
    emplacement: EmplacementContenantEntity,
    couleur: Color?,
    legumes: List<LegumeEntity>,
    onClick: () -> Unit
) {
    val couleurAffichee = couleur ?: CouleursApp.CaseVide
    
    val emoji = if (emplacement.estOccupe()) {
        val nomBase = if (emplacement.legumeNom!!.contains("(")) emplacement.legumeNom.substringBefore("(").trim() else emplacement.legumeNom
        val legume = legumes.find { it.nom == nomBase }
        getEmojiCategorieLegume(legume?.categorie ?: "")
    } else {
        ""
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (emplacement.estVide()) CouleursApp.Blanc else couleurAffichee.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (emplacement.estVide()) CouleursApp.VertPrincipal.copy(alpha = 0.15f) else couleurAffichee,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (emplacement.estVide()) {
                    Text(
                        "${emplacement.numero}",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal,
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (emplacement.estVide()) {
                    Text(
                        "Emplacement ${emplacement.numero} vide",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CouleursApp.TexteFonce.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        "Appuyez pour planter",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.VertPrincipal
                    )
                } else {
                    Text(
                        emplacement.legumeNom!!,
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.TexteFonce,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (emplacement.datePlantation != null) {
                        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.FRANCE)
                        Text(
                            "Planté le ${dateFormat.format(java.util.Date(emplacement.datePlantation!!))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
        }
    }
}

@Composable
fun ChoixPlanteDialog(
    contenant: ContenantEntity,
    legumeRepository: LegumeRepository,
    legumes: List<LegumeEntity>,
    onPlanteChoisie: (LegumeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis() }
    
    val plantesFiltrees = legumes.filter {
        searchQuery.isEmpty() || it.nom.contains(searchQuery, ignoreCase = true)
    }.sortedBy { it.nom }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir une plante", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                Text(
                    "${contenant.emoji} ${contenant.nom} · ${contenant.dimensionsTexte()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("🔍 Rechercher...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(plantesFiltrees, key = { it.id }) { legume ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlanteChoisie(legume) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    legume.nom,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce
                                )
                                Text(
                                    "${legume.categorie} · ${legume.difficulte}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.VertPrincipal
                                )
                            }
                            Text("›", style = MaterialTheme.typography.titleMedium, color = CouleursApp.VertPrincipal)
                        }
                        HorizontalDivider(color = CouleursApp.VertPale)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.VertPrincipal)
            }
        }
    )
}
