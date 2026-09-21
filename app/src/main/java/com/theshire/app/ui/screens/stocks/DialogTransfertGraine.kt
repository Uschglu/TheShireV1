package com.theshire.app.ui.screens.stocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theshire.app.data.GraineEntity
import com.theshire.app.data.JeunePlantEntity
import com.theshire.app.data.JeunePlantStades
import com.theshire.app.ui.theme.CouleursApp

/**
 * Dialogue de transfert Graine → Jeune plant.
 *
 * Permet à l'utilisateur de déclarer qu'il a semé / repiqué une partie
 * de son sachet de graines. À la validation :
 *  - Un nouveau JeunePlantEntity est créé (pré-rempli depuis la graine)
 *  - La quantité de graines est décrémentée
 *  - Si la quantité tombe à 0, la graine passe en inactif
 *
 * @param graine            La graine source (pour pré-remplir les champs)
 * @param onDismiss         Callback de fermeture sans validation
 * @param onValider         Callback appelé avec (nbGrainesUtilisees, jeunePlantCree)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTransfertGraine(
    graine: GraineEntity,
    onDismiss: () -> Unit,
    onValider: (nbGrainesUtilisees: Int, jeunePlant: JeunePlantEntity) -> Unit
) {

    // === ÉTAT DU FORMULAIRE ===
    // Nombre de graines utilisées (défaut = 1)
    var nbGraines by remember { mutableIntStateOf(1) }

    // Stade initial du plant (défaut = Semis)
    var stade by remember { mutableStateOf(JeunePlantStades.SEMIS) }

    // Menu déroulant du stade
    var menuStadeOuvert by remember { mutableStateOf(false) }

    // Emplacement actuel (optionnel)
    var emplacement by remember { mutableStateOf("") }

    // Notes (optionnel)
    var notes by remember { mutableStateOf("") }

    // Message d'erreur si quantité invalide
    var erreur by remember { mutableStateOf<String?>(null) }

    // Empêche de dépasser le stock disponible
    val maxGraines = graine.quantite.coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "🌱 Semer / Repiquer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${graine.emoji} ${graine.legumeNom}" +
                        (graine.varieteNom?.let { " — $it" } ?: ""),
                    fontSize = 14.sp,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // === INFO STOCK DISPONIBLE ===
                Text(
                    text = "Stock disponible : $maxGraines graine(s)",
                    fontSize = 13.sp,
                    color = CouleursApp.VertPrincipal,
                    fontWeight = FontWeight.Medium
                )

                // === SÉLECTEUR DE QUANTITÉ ===
                Text(
                    text = "Combien de graines utiliser ?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (nbGraines > 1) nbGraines-- },
                        enabled = nbGraines > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Moins")
                    }

                    OutlinedTextField(
                        value = nbGraines.toString(),
                        onValueChange = { valeur ->
                            val n = valeur.filter { it.isDigit() }.toIntOrNull()
                            if (n == null) {
                                nbGraines = 0
                            } else if (n > maxGraines) {
                                nbGraines = maxGraines
                                erreur = "Maximum $maxGraines graine(s) disponibles"
                            } else {
                                nbGraines = n
                                erreur = null
                            }
                        },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    IconButton(
                        onClick = { if (nbGraines < maxGraines) nbGraines++ },
                        enabled = nbGraines < maxGraines
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus")
                    }
                }

                // === SÉLECTEUR DE STADE ===
                Text(
                    text = "Stade initial du plant",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                ExposedDropdownMenuBox(
                    expanded = menuStadeOuvert,
                    onExpandedChange = { menuStadeOuvert = !menuStadeOuvert }
                ) {
                    OutlinedTextField(
                        value = stade,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuStadeOuvert)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = menuStadeOuvert,
                        onDismissRequest = { menuStadeOuvert = false }
                    ) {
                        JeunePlantStades.TOUS.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    stade = s
                                    menuStadeOuvert = false
                                }
                            )
                        }
                    }
                }

                // === EMPLACEMENT (OPTIONNEL) ===
                OutlinedTextField(
                    value = emplacement,
                    onValueChange = { emplacement = it },
                    label = { Text("Emplacement (optionnel)") },
                    placeholder = { Text("Ex : Godet, Mini-serre…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // === NOTES (OPTIONNEL) ===
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    placeholder = { Text("Ex : À repiquer dans 2 semaines") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // === MESSAGE D'ERREUR ===
                erreur?.let {
                    Text(
                        text = "⚠️ $it",
                        color = CouleursApp.MauvaiseAssociation,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation finale
                    if (nbGraines <= 0) {
                        erreur = "Veuillez indiquer au moins 1 graine"
                        return@Button
                    }
                    if (nbGraines > graine.quantite) {
                        erreur = "Pas assez de graines en stock"
                        return@Button
                    }

                    // Création du jeune plant pré-rempli
                    val nouveauPlant = JeunePlantEntity(
                        legumeNom = graine.legumeNom,
                        varieteNom = graine.varieteNom,
                        emoji = graine.emoji,
                        quantite = nbGraines,
                        stade = stade,
                        emplacementActuel = emplacement.ifBlank { null },
                        notes = notes.ifBlank { null },
                        dateSemis = if (stade == JeunePlantStades.SEMIS) {
                            System.currentTimeMillis()
                        } else null,
                        dateAjout = System.currentTimeMillis(),
                        estActif = true
                    )

                    onValider(nbGraines, nouveauPlant)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CouleursApp.VertPrincipal
                )
            ) {
                Text("Semer 🌱")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CouleursApp.TexteFonce)
            }
        },
        containerColor = CouleursApp.Creme
    )
}
