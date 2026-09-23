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
import com.theshire.app.data.ResultatPlantation
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
 * Explique les 4 onglets (Graines / Plants / Matériel / Récoltes),
 * la distinction Semis / Jeune plant, le mode projection/réel,
 * comment ajouter un élément, et donne une astuce générale.
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
                    .height(450.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ===== INTRO =====
                item {
                    Text(
                        "Ton inventaire de jardinage, en 4 onglets. Suis ce que tu as sous la main pour ne jamais être pris de court au moment de semer, planter ou cuisiner.",
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
                        "À quoi ça sert :\nTon inventaire complet des plants et semis en cours de croissance — tout ce qui n'est pas encore en terre.\n\n🌰 Semis en cours :\nUn semis reste dans cette catégorie tant qu'il n'a pas passé le stade critique. Tu le suis depuis Jardin > Semis.\n\n🌿 Jeune plant :\nUne fois que ton semis a atteint le stade Rempoté, tu peux le promouvoir en jeune plant via le bouton dédié dans sa fiche. Il reste dans ton inventaire, garde tout son historique, mais quitte l'onglet Semis du Jardin. Les plants achetés en jardinerie arrivent directement dans cette catégorie.\n\nComment ajouter :\nAppuie sur le bouton \"+\", choisis un légume et une variété, puis indique le stade actuel et l'emplacement (godet, mini-serre, balcon…).",
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

                // ===== ONGLET RÉCOLTES =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "🥕 Onglet Récoltes",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.VertPrincipal,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "À quoi ça sert :\nGarder une trace de tout ce que tu as récolté au potager, en kilogrammes. Le poids total cumulé s'affiche en haut de l'onglet.\n\nD'où ça vient :\n• Automatiquement : quand tu cliques sur \"Récolter\" depuis une culture en pleine terre ou en urbain. Tu saisis le poids obtenu, la récolte est enregistrée ici, et la culture se termine.\n• Manuellement : via le bouton \"+\", pour saisir un achat au marché, un don, une cueillette sauvage…\n\nComment modifier :\nTouche une carte pour ouvrir la fiche détail : tu peux corriger le poids ou les notes, ou supprimer la récolte.",
                        color = CouleursApp.TexteFonce,
                        fontSize = 13.sp
                    )
                }

                // ===== MODE PROJECTION / RÉEL =====
                item {
                    HorizontalDivider(color = CouleursApp.VertPale)
                }
                item {
                    Text(
                        "🌱 Mode projection / réel",
                        fontWeight = FontWeight.Bold,
                        color = CouleursApp.Terracotta,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Text(
                        "Le mode actif (interrupteur sur l'écran d'accueil) change le comportement :\n\n• Mode projection : tu simules ton potager sans impacter les stocks. Idéal pour tester des associations, prévoir une rotation.\n\n• Mode réel : quand tu plantes (semis ou graine directe), le stock est automatiquement décrémenté. La promotion d'un semis en jeune plant ou la plantation d'un jeune plant consomme aussi du stock.",
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
                        "Touche n'importe quelle carte (graine, plant, outil ou récolte) pour ouvrir sa fiche détail : tu pourras modifier la quantité, les notes, le fournisseur, ou supprimer l'élément.",
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

/**
 * Dialogue d'erreur affiché après une tentative de plantation en mode réel
 * qui a échoué (source introuvable, stock insuffisant, etc.).
 *
 * Utilisé par JardinPlanchesScreen et EcranContenants quand
 * JardinRepository / ContenantRepository retourne un ResultatPlantation
 * autre que Succes.
 */
@Composable
fun DialogErreurPlantation(
    resultat: ResultatPlantation,
    onDismiss: () -> Unit
) {
    val titre: String
    val message: String
    
    when (resultat) {
        is ResultatPlantation.ErreurSourceIntrouvable -> {
            titre = "Source introuvable"
            message = "Aucun stock correspondant n'a été trouvé pour cette plantation.\n\n" +
                "Source demandée : ${resultat.source}\n\n" +
                "Ajoute d'abord un sachet ou un semis dans tes Stocks, ou choisis une autre source."
        }
        is ResultatPlantation.ErreurSourceInactive -> {
            titre = "Source épuisée"
            val nom = if (resultat.varieteNom != null) {
                "${resultat.legumeNom} (${resultat.varieteNom})"
            } else {
                resultat.legumeNom
            }
            message = "Ton stock pour \"$nom\" est épuisé ou déjà utilisé.\n\n" +
                "Recharge ton stock avant de pouvoir replanter, ou choisis une autre source."
        }
        is ResultatPlantation.ErreurStockInsuffisant -> {
            titre = "Stock insuffisant"
            val nom = if (resultat.varieteNom != null) {
                "${resultat.legumeNom} (${resultat.varieteNom})"
            } else {
                resultat.legumeNom
            }
            message = "Il te manque ${resultat.manquant} unité(s) pour planter.\n\n" +
                "Disponible : ${resultat.disponible}\n" +
                "Demandé  : ${resultat.demande}\n" +
                "Plante   : $nom\n\n" +
                "Complète ton stock ou réduis la quantité."
        }
        is ResultatPlantation.Succes -> {
            // Ne devrait jamais arriver : pas la peine d'afficher un dialogue.
            titre = "Plantage réussi"
            message = "La plantation a réussi."
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "⚠️ $titre",
                fontWeight = FontWeight.Bold,
                color = CouleursApp.Terracotta
            )
        },
        text = {
            Text(
                message,
                color = CouleursApp.TexteFonce
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
            ) {
                Text("Compris")
            }
        }
    )
}
