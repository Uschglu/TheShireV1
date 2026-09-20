package com.theshire.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.theshire.app.data.RappelCulturelEntity
import com.theshire.app.ui.theme.CouleursApp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Affiche une semaine complète du calendrier avec les barres
 * d'opérations culturales en dessous.
 */
@Composable
fun SemaineCalendrier(
    semaine: Int,
    offset: Int,
    daysInMonth: Int,
    currentYear: Int,
    currentMonth: Int,
    selectedDay: Int,
    rappelsCulturels: List<RappelCulturelEntity>,
    onDayClick: (Int) -> Unit
) {
    // Ligne des jours de la semaine
    Row(modifier = Modifier.fillMaxWidth()) {
        for (col in 0..6) {
            val dayNumber = semaine * 7 + col - offset + 1
            if (dayNumber in 1..daysInMonth) {
                val isSelected = dayNumber == selectedDay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.2f)
                        .background(
                            if (isSelected) CouleursApp.VertPrincipal else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onDayClick(dayNumber) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$dayNumber",
                        color = if (isSelected) Color.White else CouleursApp.TexteFonce,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).aspectRatio(1.2f))
            }
        }
    }

    // Bornes temporelles de la semaine
    val debutSemaine = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_MONTH, semaine * 7 - offset)
    }.timeInMillis
    val finSemaine = debutSemaine + 7L * 24 * 60 * 60 * 1000 - 1

    // Filtrer les rappels qui chevauchent cette semaine
    val rappelsSemaine = rappelsCulturels.filter { rappel ->
        rappel.dateDebut <= finSemaine && rappel.dateFin >= debutSemaine
    }

    if (rappelsSemaine.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        val rappelsTries = rappelsSemaine.sortedBy { it.dateDebut }
        val rappelsAAfficher = rappelsTries.take(2)
        val nombreEnPlus = (rappelsTries.size - 2).coerceAtLeast(0)

        rappelsAAfficher.forEach { rappel ->
            BarreOperation(
                rappel = rappel,
                debutSemaine = debutSemaine,
                finSemaine = finSemaine
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (nombreEnPlus > 0) {
            var showPopup by remember { mutableStateOf(false) }

            Text(
                "+$nombreEnPlus autre(s)...",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = CouleursApp.VertPrincipal,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .padding(start = 4.dp, top = 2.dp)
                    .clickable { showPopup = true }
            )

            if (showPopup) {
                PopupListeOperations(
                    rappels = rappelsTries,
                    onDismiss = { showPopup = false }
                )
            }
        }
    }
}

/**
 * Popup listant toutes les opérations culturales d'une semaine.
 */
@Composable
fun PopupListeOperations(
    rappels: List<RappelCulturelEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Opérations de la semaine",
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(rappels, key = { it.id }) { op ->
                    val couleurBase = try {
                        Color(android.graphics.Color.parseColor(op.couleurHex))
                    } catch (e: Exception) {
                        Color(0xFFFFA726)
                    }

                    val dateFormat = SimpleDateFormat("dd/MM", Locale.FRANCE)
                    val dateDebutTexte = dateFormat.format(Date(op.dateDebut))
                    val dateFinTexte = dateFormat.format(Date(op.dateFin))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDismiss() },
                        colors = CardDefaults.cardColors(
                            containerColor = couleurBase.copy(
                                alpha = if (CouleursApp.isDarkMode) 0.35f else 0.20f
                            )
                        ),
                        border = BorderStroke(1.dp, couleurBase.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(40.dp)
                                    .background(couleurBase, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                op.emoji,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    op.typeOperation,
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.TexteFonce,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    op.legumeNom,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                                )
                                Text(
                                    "Du $dateDebutTexte au $dateFinTexte",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = CouleursApp.TexteFonce.copy(alpha = 0.6f)
                                )
                            }

                            if (op.estTermine) {
                                Text(
                                    "✅",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = CouleursApp.VertPrincipal)
            }
        }
    )
}

/**
 * Barre horizontale colorée représentant une opération culturale
 * sur une semaine du calendrier.
 */
@Composable
fun BarreOperation(
    rappel: RappelCulturelEntity,
    debutSemaine: Long,
    finSemaine: Long
) {
    val MILLIS_PAR_JOUR = 24L * 60 * 60 * 1000

    val debutAffiche = maxOf(rappel.dateDebut, debutSemaine)
    val finAffiche = minOf(rappel.dateFin, finSemaine)

    val jourDebutSemaine = ((debutAffiche - debutSemaine) / MILLIS_PAR_JOUR)
        .toInt().coerceIn(0, 6)
    val jourFinSemaine = ((finAffiche - debutSemaine) / MILLIS_PAR_JOUR)
        .toInt().coerceIn(0, 6)

    val nombreJoursCouverts = jourFinSemaine - jourDebutSemaine + 1
    val estTermine = rappel.estTermine
    val estEnRetard = !estTermine && rappel.dateFin < System.currentTimeMillis()

    val couleurBase = try {
        Color(android.graphics.Color.parseColor(rappel.couleurHex))
    } catch (e: Exception) {
        Color(0xFFFFA726)
    }

    val couleurAffichee = when {
        estTermine -> couleurBase.copy(alpha = 0.25f)
        estEnRetard -> Color(0xFFE53935).copy(alpha = 0.7f)
        else -> couleurBase.copy(alpha = 0.75f)
    }

    Row(modifier = Modifier.fillMaxWidth().height(20.dp)) {
        if (jourDebutSemaine > 0) {
            Spacer(modifier = Modifier.weight(jourDebutSemaine.toFloat()))
        }

        Box(
            modifier = Modifier
                .weight(nombreJoursCouverts.toFloat())
                .fillMaxHeight()
                .padding(horizontal = 1.dp)
                .background(couleurAffichee, RoundedCornerShape(4.dp))
                .border(0.5.dp, couleurBase.copy(alpha = 0.9f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "${rappel.emoji} ${rappel.typeOperation}",
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (jourFinSemaine < 6) {
            Spacer(modifier = Modifier.weight((6 - jourFinSemaine).toFloat()))
        }
    }
}

/**
 * Légende expliquant les différents types d'opérations culturales
 * et leurs couleurs.
 */
@Composable
fun LegendeOperations() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Types d'opérations",
                fontWeight = FontWeight.Bold,
                color = CouleursApp.TexteFonce
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(16.dp).background(Color(0xFF66BB6A)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Plantation, semis, repiquage",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(16.dp).background(Color(0xFFFFA726)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Entretien (tuteurage, buttage, paillage)",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(16.dp).background(Color(0xFFAB47BC)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Taille, effeuillage, pincement",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(16.dp).background(Color(0xFFEF5350)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Traitement, surveillance maladies",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(16.dp).height(16.dp).background(Color(0xFF42A5F5)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Récolte, arrêt arrosage",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce
                )
            }
        }
    }
}
