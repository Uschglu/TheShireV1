package com.theshire.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theshire.app.data.VarieteRepository
import com.theshire.app.ui.theme.CouleursApp
import kotlinx.coroutines.launch

/**
 * Dialog permettant de choisir une variété pour un légume donné.
 * 
 * Affiche :
 * - Une option "Variété standard"
 * - Les variétés spécifiques issues de la base de données
 */
@Composable
fun VarieteSelectionDialog(
    legumeNom: String,
    varieteRepository: VarieteRepository,
    onVarieteChoisie: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val varietes by varieteRepository.getVarietesForLegume(legumeNom)
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                varieteRepository.ajouterVarietesPredefinies()
            } catch (e: Exception) {
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Variétés de $legumeNom", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        "🌱 Variété standard",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVarieteChoisie(legumeNom) }
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal
                    )
                    HorizontalDivider()
                }
                if (varietes.isEmpty()) {
                    item {
                        Text(
                            "Chargement...",
                            modifier = Modifier.padding(16.dp),
                            color = CouleursApp.TexteFonce
                        )
                    }
                } else {
                    items(varietes) { v ->
                        Text(
                            "🌿 ${v.nom}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVarieteChoisie("${legumeNom} (${v.nom})") }
                                .padding(16.dp),
                            color = CouleursApp.TexteFonce
                        )
                        Text(
                            v.description,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.TexteFonce
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Dialog d'aide pour la conservation avec 4 onglets :
 * Séchage, Lactofermentation, Conserves, Congélation.
 */
@Composable
fun AideConservationDialog(onDismiss: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("sechage") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📖 Guide de conservation",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = when (selectedOnglet) {
                        "sechage" -> 0
                        "lacto" -> 1
                        "conserves" -> 2
                        else -> 3
                    },
                    containerColor = CouleursApp.VertPale,
                    contentColor = CouleursApp.VertPrincipal
                ) {
                    Tab(
                        selected = selectedOnglet == "sechage",
                        onClick = { selectedOnglet = "sechage" },
                        text = {
                            Text(
                                "🌬️ Séchage",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedOnglet == "lacto",
                        onClick = { selectedOnglet = "lacto" },
                        text = {
                            Text(
                                "🥬 Lacto",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedOnglet == "conserves",
                        onClick = { selectedOnglet = "conserves" },
                        text = {
                            Text(
                                "🫙 Conserves",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedOnglet == "congelation",
                        onClick = { selectedOnglet = "congelation" },
                        text = {
                            Text(
                                "❄️ Congélation",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedOnglet) {
                        "sechage" -> {
                            item {
                                Text(
                                    "🌬️ Séchage optimal",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            item {
                                Text(
                                    "• Choisissez des légumes frais et sains\n• Lavez et séchez soigneusement\n• Coupez en tranches fines et régulières (3-5mm)\n• Blanchissez les légumes durs (carottes, haricots) 2-3 min\n• Disposez sans chevauchement sur les plateaux\n• Température idéale : 50-60°C\n• Durée : 6-12h selon l'épaisseur\n• Les légumes doivent être cassants et croquants\n• Stockez dans des bocaux hermétiques à l'abri de la lumière\n• Conservation : 6-12 mois",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                            item {
                                Text(
                                    "🥕 Légumes adaptés",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.Terracotta
                                )
                            }
                            item {
                                Text(
                                    "Tomates, champignons, carottes, courgettes, oignons, poivrons, herbes aromatiques, haricots verts",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                        "lacto" -> {
                            item {
                                Text(
                                    "🥬 Lactofermentation",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            item {
                                Text(
                                    "• Utilisez du sel sans iode (sel de mer)\n• Proportion : 2-3% de sel (20-30g par litre d'eau)\n• Coupez les légumes en morceaux réguliers\n• Tassez bien pour éliminer les bulles d'air\n• Les légumes doivent être immergés sous la saumure\n• Utilisez un poids pour maintenir sous l'eau\n• Laissez fermenter à température ambiante (18-22°C)\n• Durée : 1-4 semaines selon le goût\n• Goûtez régulièrement\n• Une fois ouvert, conservez au réfrigérateur",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                            item {
                                Text(
                                    "🥕 Légumes adaptés",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.Terracotta
                                )
                            }
                            item {
                                Text(
                                    "Choux (choucroute), carottes, radis, concombres (pickles), haricots verts, betteraves, navets",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                        "conserves" -> {
                            item {
                                Text(
                                    "🫙 Conserves (stérilisation)",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            item {
                                Text(
                                    "• Stérilisez les bocaux et couvercles à l'eau bouillante\n• Utilisez des légumes très frais\n• Remplissez les bocaux en laissant 2cm de vide\n• Ajoutez de l'eau salée bouillante (20g sel/litre)\n• Fermez hermétiquement\n• Stérilisez à 100°C pendant 1h-1h30\n• Vérifiez l'étanchéité après refroidissement\n• Le couvercle doit être bombé vers l'intérieur\n• Stockez dans un endroit frais et sombre\n• Conservation : 1-2 ans",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                            item {
                                Text(
                                    "🥕 Légumes adaptés",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.Terracotta
                                )
                            }
                            item {
                                Text(
                                    "Tomates, haricots verts, petits pois, carottes, betteraves, ratatouille, coulis de tomate",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                        "congelation" -> {
                            item {
                                Text(
                                    "❄️ Congélation optimale",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.VertPrincipal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            item {
                                Text(
                                    "• Choisissez des légumes très frais\n• Lavez et séchez soigneusement\n• Blanchissez la plupart des légumes 2-3 min\n• Refroidissez immédiatement dans l'eau glacée\n• Égouttez bien avant de congeler\n• Disposez à plat pour éviter les blocs\n• Utilisez des sacs de congélation sans air\n• Étiquetez avec le nom et la date\n• Température idéale : -18°C ou moins\n• Ne recongelez jamais un produit décongelé\n• Conservation : 8-12 mois",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                            item {
                                Text(
                                    "🥕 Légumes adaptés",
                                    fontWeight = FontWeight.Bold,
                                    color = CouleursApp.Terracotta
                                )
                            }
                            item {
                                Text(
                                    "Haricots verts, petits pois, carottes, courgettes, poivrons, épinards, brocolis, choux-fleurs",
                                    color = CouleursApp.TexteFonce
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Fermer")
            }
        }
    )
}

/**
 * Dialog d'aide pour l'écran Stocks.
 *
 * Explique les 3 onglets (Graines / Plants / Matériel), comment ajouter
 * un élément, comment lire les indicateurs, et donne une astuce générale.
 */
@Composable
fun AideStocksDialog(onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📖 Guide des stocks",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ===== INTRO =====
                item {
                    Text(
                        "Ton inventaire de jardinage, en 3 onglets. Suis ce que tu as sous la main pour ne jamais être pris de court au moment de semer ou planter.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }

                // ===== ONGLET GRAINES =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "🫘 Onglet Graines",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "À quoi ça sert :\nGérer tes sachets de graines — quantité restante, année de récolte, fournisseur, date de péremption.\n\nComment ajouter :\nAppuie sur le bouton \"+\" en bas de l'onglet. Choisis un légume (et une variété si tu veux), puis renseigne la quantité et les infos du sachet.\n\nIndicateurs :\nUne pastille colorée sur chaque carte t'indique l'état du sachet — vert si tout va bien, orange si la péremption approche, rouge si les graines sont trop vieilles pour germer correctement.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }

                // ===== ONGLET PLANTS =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "🌱 Onglet Plants",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "À quoi ça sert :\nSuivre tes jeunes plants en cours de croissance — semis en godet, plants achetés en jardinerie, boutures… tout ce qui n'est pas encore en terre.\n\nComment ajouter :\nAppuie sur le bouton \"+\", choisis un légume et une variété, puis indique le stade actuel et l'emplacement (godet, mini-serre, balcon…).\n\nLes 6 étapes du cycle :\n• 🌰 Semis — la graine est en terre\n• 🌱 Levée — elle a germé, premières feuilles\n• 🌿 Repiqué — premier changement de godet\n• 🪴 Rempoté — contenant plus grand\n• 🌳 Prêt à planter — assez développé pour le jardin\n• 🌲 Endurci — habitué au froid et au vent\n\n💡 Astuce :\nLes plants que tu crées depuis l'onglet 🌰 Semis du Jardin apparaissent aussi ici — c'est le même inventaire, avec un suivi plus détaillé côté Jardin.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }

                // ===== ONGLET MATÉRIEL =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "🛠️ Onglet Matériel",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "À quoi ça sert :\nRecenser tes outils, classés par catégorie (plantation, arrosage, taille, protection…). Pratique pour savoir ce que tu as déjà avant d'acheter en double.\n\nComment ajouter :\nAppuie sur le bouton \"+\" et choisis un outil dans la liste prédéfinie, ou crée le tien si tu ne le trouves pas.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }

                // ===== ASTUCE =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "💡 Astuce",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.Terracotta,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "Touche n'importe quelle carte (graine, plant ou outil) pour ouvrir sa fiche détail : tu pourras modifier la quantité, les notes, le fournisseur, ou supprimer l'élément.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Fermer")
            }
        }
    )
}
