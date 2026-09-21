package com.theshire.app.ui.screens.semis

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantEtapes
import com.theshire.app.data.JeunePlantRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fiche détaillée d'un semis.
 *
 * Permet :
 *  - De voir toutes les infos (étape, dates, quantité, emplacement, notes)
 *  - De faire avancer / reculer d'étape (avec confirmation)
 *  - De changer directement d'étape (menu déroulant)
 *  - D'ajuster la quantité
 *  - De marquer comme planté (fin de cycle)
 *  - De supprimer le semis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FicheSemis(
    semis: JeunePlantEntity,
    repository: JeunePlantRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var semisActuel by remember { mutableStateOf(semis) }

    // Dialogues
    var showSuppressionDialog by remember { mutableStateOf(false) }
    var showModificationQuantiteDialog by remember { mutableStateOf(false) }
    var showAvancerEtapeDialog by remember { mutableStateOf(false) }
    var etapeCible by remember { mutableStateOf<String?>(null) }
    var stadeMenuOuvert by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE) }
    val emojiEtape = JeunePlantEtapes.emoji(semisActuel.stade)
    val couleurEtape = couleurPourEtape(semisActuel.stade)
    val progression = JeunePlantEtapes.progression(semisActuel.stade)

    val etapeSuivante = JeunePlantEtapes.etapeSuivante(semisActuel.stade)
    val etapePrecedente = JeunePlantEtapes.etapePrecedente(semisActuel.stade)
    val estFinale = JeunePlantEtapes.estEtapeFinale(semisActuel.stade)

    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${semisActuel.emoji} ${semisActuel.legumeNom}",
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
                actions = {
                    IconButton(onClick = { showSuppressionDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== En-tête : emoji + nom + étape + progression =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(semisActuel.emoji, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        semisActuel.legumeNom,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (semisActuel.varieteNom != null) {
                        Text(
                            "Variété : ${semisActuel.varieteNom}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CouleursApp.TexteFonce.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "$emojiEtape ${semisActuel.stade}",
                        style = MaterialTheme.typography.titleMedium,
                        color = couleurEtape,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progression },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = couleurEtape,
                        trackColor = CouleursApp.Blanc
                    )
                }
            }

            // ===== Cycle : avancer / reculer / changer d'étape =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "🌿 Cycle de vie",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bouton "Étape suivante" (principal, gros)
                    if (etapeSuivante != null && !estFinale) {
                        Button(
                            onClick = {
                                etapeCible = etapeSuivante
                                showAvancerEtapeDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CouleursApp.VertPrincipal
                            )
                        ) {
                            Text("➡️ Passer à : ${JeunePlantEtapes.emoji(etapeSuivante)} $etapeSuivante")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Boutons secondaires : reculer + changer manuellement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (etapePrecedente != null) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val nouvelle = repository.reculerEtape(semisActuel.id)
                                        if (nouvelle != null) {
                                            semisActuel = semisActuel.copy(
                                                stade = nouvelle,
                                                estActif = true
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("⬅️ Reculer")
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { stadeMenuOuvert = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Changer…")
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { stadeMenuOuvert = true }
                            )
                            DropdownMenu(
                                expanded = stadeMenuOuvert,
                                onDismissRequest = { stadeMenuOuvert = false }
                            ) {
                                JeunePlantEtapes.TOUS.forEach { et ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${JeunePlantEtapes.emoji(et)} $et")
                                        },
                                        onClick = {
                                            stadeMenuOuvert = false
                                            if (et != semisActuel.stade) {
                                                etapeCible = et
                                                showAvancerEtapeDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== Dates du cycle =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📅 Étapes franchies",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LigneDate("🌰 Semis", semisActuel.dateSemis, dateFormat)
                    LigneDate("🌱 Levée", semisActuel.dateLevee, dateFormat)
                    LigneDate("🌿 Repiquage", semisActuel.dateRepiquage, dateFormat)
                    LigneDate("🪴 Rempotage", semisActuel.dateRempotage, dateFormat)
                    LigneDate("🌲 Endurcissement", semisActuel.dateEndurcissement, dateFormat)
                    LigneDate("🎉 Plantation", semisActuel.datePlantation, dateFormat)
                }
            }

            // ===== Quantité =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "🌱 Quantité",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    if (semisActuel.quantite > 0) {
                                        repository.decrementerQuantite(semisActuel.id, 1)
                                        semisActuel = semisActuel.copy(
                                            quantite = semisActuel.quantite - 1
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CouleursApp.VertPale
                            )
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${semisActuel.quantite}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal
                            )
                            Text(
                                "plant(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    repository.incrementerQuantite(semisActuel.id, 1)
                                    semisActuel = semisActuel.copy(
                                        quantite = semisActuel.quantite + 1,
                                        estActif = true
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = CouleursApp.VertPale
                            )
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showModificationQuantiteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Modifier manuellement")
                    }
                }
            }

            // ===== Emplacement + statut =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📋 Informations",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LigneInfo("Emplacement", semisActuel.emplacementActuel)
                    LigneInfo(
                        "Ajouté le",
                        dateFormat.format(Date(semisActuel.dateAjout))
                    )
                    LigneInfo(
                        "Statut",
                        if (semisActuel.estActif) "✅ En cours" else "❌ Terminé"
                    )
                }
            }

            // ===== Notes =====
            if (!semisActuel.notes.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text("📝", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Notes",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                semisActuel.notes!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }

            // ===== Bouton "Marquer planté" (raccourci vers la fin) =====
            if (semisActuel.estActif && !estFinale) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            repository.marquerPlante(semisActuel.id)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertClair)
                ) {
                    Text("🎉 Marquer comme planté")
                }
            }

            // ===== Bouton supprimer =====
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showSuppressionDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supprimer ce semis")
            }
        }
    }

    // ===== Dialogue : confirmation d'avancement d'étape =====
    if (showAvancerEtapeDialog && etapeCible != null) {
        DialogAvancerEtape(
            etapeActuelle = semisActuel.stade,
            etapeCible = etapeCible!!,
            onDismiss = {
                showAvancerEtapeDialog = false
                etapeCible = null
            },
            onConfirmer = {
                val cible = etapeCible!!
                scope.launch {
                    val nouvelle = repository.changerEtape(semisActuel.id, cible)
                    if (nouvelle != null) {
                        semisActuel = semisActuel.copy(
                            stade = nouvelle,
                            estActif = !JeunePlantEtapes.estEtapeFinale(nouvelle)
                        )
                    }
                }
                showAvancerEtapeDialog = false
                etapeCible = null
            }
        )
    }

    // ===== Dialogue : modification manuelle de la quantité =====
    if (showModificationQuantiteDialog) {
        var nouvelleQuantite by remember {
            mutableStateOf(semisActuel.quantite.toString())
        }

        AlertDialog(
            onDismissRequest = { showModificationQuantiteDialog = false },
            title = { Text("Modifier la quantité", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Combien de plants reste-t-il ?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nouvelleQuantite,
                        onValueChange = { nouvelleQuantite = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantité") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = nouvelleQuantite.toIntOrNull() ?: 0
                        scope.launch {
                            val maj = semisActuel.copy(
                                quantite = q,
                                estActif = q > 0
                            )
                            repository.mettreAJourJeunePlant(maj)
                            semisActuel = maj
                        }
                        showModificationQuantiteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModificationQuantiteDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }

    // ===== Dialogue : confirmation de suppression =====
    if (showSuppressionDialog) {
        AlertDialog(
            onDismissRequest = { showSuppressionDialog = false },
            title = { Text("Supprimer ?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Voulez-vous vraiment supprimer ce semis ? Cette action est définitive.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.supprimerJeunePlant(semisActuel)
                            showSuppressionDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuppressionDialog = false }) {
                    Text("Annuler", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
}

/**
 * Une ligne "date" : libellé + valeur formatée, ou "—" si null.
 */
@Composable
private fun LigneDate(
    label: String,
    timestamp: Long?,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
        )
        Text(
            timestamp?.let { dateFormat.format(Date(it)) } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = if (timestamp != null) CouleursApp.TexteFonce else CouleursApp.TexteFonce.copy(alpha = 0.4f),
            fontWeight = if (timestamp != null) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Une ligne "info" : libellé + valeur texte, ou "—" si null/vide.
 */
@Composable
private fun LigneInfo(label: String, valeur: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
        )
        Text(
            valeur?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = CouleursApp.TexteFonce
        )
    }
}
