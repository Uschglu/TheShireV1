package com.theshire.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.theshire.app.data.LocalisationRepository
import com.theshire.app.data.LuneRepository
import com.theshire.app.data.MeteoData
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.OutilsApp
import com.theshire.app.data.RappelCulturelEntity
import com.theshire.app.ui.ContenantRepository
import com.theshire.app.ui.JardinRepository
import com.theshire.app.ui.LegumeRepository
import com.theshire.app.ui.OperationsCulturales
import com.theshire.app.ui.Outils
import com.theshire.app.ui.RappelCulturelRepository
import com.theshire.app.ui.RappelRepository
import com.theshire.app.ui.components.LegendeOperations
import com.theshire.app.ui.components.MeteoCard
import com.theshire.app.ui.components.SemaineCalendrier
import com.theshire.app.ui.theme.CouleursApp
import com.theshire.app.ui.theme.envelopperAvecTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendrierScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val jardinRepository = remember { JardinRepository(context) }
    val legumeRepository = remember { LegumeRepository(context) }
    val rappelCulturelRepository = remember { RappelCulturelRepository(context) }
    val contenantRepository = remember { ContenantRepository(context) }
    val legumes by legumeRepository.legumes.collectAsState(initial = emptyList())
    val meteoRepository = remember { MeteoRepository() }
    val localisationRepository = remember { LocalisationRepository(context) }
    val luneRepository = remember { LuneRepository() }

    var meteo by remember { mutableStateOf<MeteoData?>(null) }
    var ville by remember { mutableStateOf("") }
    val phaseLune = remember { luneRepository.getPhaseLune() }

    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var selectedTimestamp by remember { mutableStateOf(0L) }

    val rappelRepository = remember { RappelRepository(context) }
    var rappelActif by remember { mutableStateOf(false) }
    var rappelNote by remember { mutableStateOf("") }
    var rappelHeure by remember { mutableStateOf(9) }
    var rappelMinute by remember { mutableStateOf(0) }

    var rappelsCulturelsDuMois by remember {
        mutableStateOf<List<RappelCulturelEntity>>(emptyList())
    }
    var showOperationsDialog by remember { mutableStateOf(false) }
    var operationsDuJour by remember {
        mutableStateOf<List<RappelCulturelEntity>>(emptyList())
    }
    var showDetailOperationDialog by remember { mutableStateOf(false) }
    var operationSelectionnee by remember { mutableStateOf<RappelCulturelEntity?>(null) }
    var nomContenantsMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { legumeRepository.ajouterLegumesPredefinis() }

    LaunchedEffect(Unit) {
        try {
            val v = localisationRepository.getVille()
            if (v != null) ville = v
            meteo = meteoRepository.getMeteo(ville.ifEmpty { "Paris" })
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(Unit) {
        try {
            val contenants = contenantRepository.getTousContenants()
            nomContenantsMap = contenants.associate { it.id to it.nom }
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(currentMonth, currentYear) {
        val calDebut = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calFin = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
            add(Calendar.DAY_OF_MONTH, 42)
        }
        rappelsCulturelsDuMois = rappelCulturelRepository.getRappelsEntreDates(
            calDebut.timeInMillis, calFin.timeInMillis
        )
    }

    val moisNoms = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )

    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text("Calendrier 📅", fontWeight = FontWeight.Bold, color = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MeteoCard(meteo, ville.ifEmpty { "Localisation..." }, true, phaseLune) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Navigation mois
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11; currentYear--
                                } else currentMonth--
                            }) { Text("◀") }
                            Text(
                                "${moisNoms[currentMonth]} $currentYear",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = CouleursApp.TexteFonce
                            )
                            TextButton(onClick = {
                                if (currentMonth == 11) {
                                    currentMonth = 0; currentYear++
                                } else currentMonth++
                            }) { Text("▶") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // En-têtes de jours
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach {
                                Text(
                                    it,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    color = CouleursApp.VertPrincipal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grille du mois
                        val cal = Calendar.getInstance()
                        cal.set(currentYear, currentMonth, 1)
                        val firstDay = cal.get(Calendar.DAY_OF_WEEK)
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val offset = if (firstDay == Calendar.SUNDAY) 6 else firstDay - 2
                        val nombreSemaines = (offset + daysInMonth + 6) / 7

                        for (semaine in 0 until nombreSemaines) {
                            SemaineCalendrier(
                                semaine = semaine,
                                offset = offset,
                                daysInMonth = daysInMonth,
                                currentYear = currentYear,
                                currentMonth = currentMonth,
                                selectedDay = selectedDay,
                                rappelsCulturels = rappelsCulturelsDuMois,
                                onDayClick = { dayNumber ->
                                    selectedDay = dayNumber
                                    val calJour = Calendar.getInstance()
                                    calJour.set(
                                        currentYear, currentMonth, dayNumber,
                                        rappelHeure, rappelMinute, 0
                                    )
                                    selectedTimestamp = calJour.timeInMillis

                                    val debutJour = Calendar.getInstance().apply {
                                        timeInMillis = calJour.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    val finJour = debutJour + 24L * 60 * 60 * 1000 - 1

                                    operationsDuJour = rappelsCulturelsDuMois.filter { rappel ->
                                        rappel.dateDebut <= finJour && rappel.dateFin >= debutJour
                                    }

                                    val rappel = rappelRepository.getRappelSync(selectedTimestamp)
                                    rappelActif = rappel?.estActif ?: false
                                    rappelNote = rappel?.note ?: ""
                                    showOperationsDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🌱 Les barres ←→ indiquent les opérations culturales automatiques",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            item { LegendeOperations() }
        }
    }

    // Dialog : opérations du jour
    if (showOperationsDialog) {
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE)
        val dateAffichee = Date(selectedTimestamp)

        AlertDialog(
            onDismissRequest = { showOperationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📅 ${dateFormat.format(dateAffichee)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = {
                        scope.launch {
                            val cal = Calendar.getInstance()
                            cal.set(currentYear, currentMonth, selectedDay, rappelHeure, rappelMinute, 0)
                            rappelRepository.toggleRappel(cal.timeInMillis, "Rappel")
                            rappelActif = !rappelActif
                        }
                    }) {
                        Text(
                            if (rappelActif) "🔔" else "🔕",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            },
            text = {
                Column {
                    if (operationsDuJour.isNotEmpty()) {
                        Text(
                            "🌱 Opérations culturales :",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        operationsDuJour.forEach { op ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        operationSelectionnee = op
                                        showDetailOperationDialog = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(
                                        android.graphics.Color.parseColor(op.couleurHex)
                                    ).copy(alpha = if (CouleursApp.isDarkMode) 0.30f else 0.15f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(op.emoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            op.typeOperation,
                                            fontWeight = FontWeight.Bold,
                                            color = CouleursApp.TexteFonce
                                        )
                                        val localisation = if (op.estUrbain()) {
                                            val nomContenant =
                                                nomContenantsMap[op.contenantId] ?: "Contenant"
                                            "$nomContenant · Emplacement ${op.emplacementNumero ?: "?"}"
                                        } else {
                                            "${op.legumeNom} - Case ${op.caseNumero ?: "?"}"
                                        }
                                        Text(
                                            localisation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CouleursApp.VertPrincipal
                                        )
                                        if (op.estTermine) {
                                            Text(
                                                "✅ Terminé",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CouleursApp.VertPrincipal,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        "›",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = CouleursApp.VertPrincipal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        "📝 Note personnelle :",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val tp = TimePickerDialog(
                                envelopperAvecTheme(context),
                                { _, h, m -> rappelHeure = h; rappelMinute = m },
                                rappelHeure, rappelMinute, true
                            )
                            tp.show()
                        },
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏰", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Heure du rappel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.VertPrincipal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${String.format("%02d", rappelHeure)}:${String.format("%02d", rappelMinute)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rappelNote,
                        onValueChange = { rappelNote = it },
                        label = { Text("Note (optionnel)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val cal = Calendar.getInstance()
                                cal.set(
                                    currentYear, currentMonth, selectedDay,
                                    rappelHeure, rappelMinute, 0
                                )
                                val ts = cal.timeInMillis
                                val r = rappelRepository.getRappel(ts)
                                if (r != null) {
                                    rappelRepository.mettreAJourNote(ts, rappelNote)
                                } else {
                                    rappelRepository.ajouterRappel(ts, "Rappel", rappelNote)
                                }
                            }
                            showOperationsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                    ) {
                        Text("Enregistrer la note")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOperationsDialog = false }) {
                    Text("Fermer", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }

    // Dialog : détail d'une opération
    if (showDetailOperationDialog && operationSelectionnee != null) {
        val op = operationSelectionnee!!
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)

        val operationCulturale = remember(op.typeOperation, op.legumeNom) {
            OperationsCulturales.getOperationsPourLegume(op.legumeNom)
                .find { it.nom == op.typeOperation }
        }
        val outilsRequis = remember(operationCulturale, op) {
            val ids = if (op.estUrbain()) {
                operationCulturale?.outilsRequisUrbain ?: emptyList()
            } else {
                operationCulturale?.outilsRequis ?: emptyList()
            }
            Outils.getOutilsParIds(ids)
        }

        val localisationComplete = if (op.estUrbain()) {
            val nomContenant = nomContenantsMap[op.contenantId] ?: "Contenant"
            "$nomContenant · Emplacement ${op.emplacementNumero ?: "?"}"
        } else {
            "Case ${op.caseNumero ?: "?"}"
        }

        AlertDialog(
            onDismissRequest = { showDetailOperationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(op.emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(op.typeOperation, fontWeight = FontWeight.Bold)
                        Text(
                            op.legumeNom,
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.VertPrincipal
                        )
                    }
                }
            },
            text = {
                Column {
                    Text(op.description, style = MaterialTheme.typography.bodyMedium)
                    if (op.conseil.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "💡 Conseil",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    op.conseil,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }

                    if (outilsRequis.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "🛠️ Outils nécessaires :",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        outilsRequis.forEach { outil ->
                            val possede = OutilsApp.possede(outil.id)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(outil.emoji, style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    outil.nom,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CouleursApp.TexteFonce,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!possede) {
                                    Text(
                                        "❌ Non possédé",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        "✅",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CouleursApp.VertPrincipal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "📅 Période : ${dateFormat.format(Date(op.dateDebut))} → ${dateFormat.format(Date(op.dateFin))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                    Text(
                        "📍 $localisationComplete",
                        style = MaterialTheme.typography.bodySmall,
                        color = CouleursApp.TexteFonce
                    )
                    if (op.estTermine) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "✅ Marqué comme terminé",
                            color = CouleursApp.VertPrincipal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (op.estTermine) {
                                rappelCulturelRepository.marquerNonTermine(op.id)
                            } else {
                                rappelCulturelRepository.marquerTermine(op.id)
                            }
                            val calDebut = Calendar.getInstance().apply {
                                set(currentYear, currentMonth, 1, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val calFin = Calendar.getInstance().apply {
                                set(currentYear, currentMonth, 1, 23, 59, 59)
                                set(Calendar.MILLISECOND, 999)
                                add(Calendar.DAY_OF_MONTH, 42)
                            }
                            rappelsCulturelsDuMois =
                                rappelCulturelRepository.getRappelsEntreDates(
                                    calDebut.timeInMillis, calFin.timeInMillis
                                )
                        }
                        showDetailOperationDialog = false
                        showOperationsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text(if (op.estTermine) "Annuler" else "Marquer comme fait")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailOperationDialog = false }) {
                    Text("Fermer", color = CouleursApp.VertPrincipal)
                }
            }
        )
    }
}
