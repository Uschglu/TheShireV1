/**
 * Dialog d'aide pour l'écran Stocks.
 *
 * Explique les 3 onglets (Graines / Plants / Matériel), la distinction
 * Semis / Jeune plant, comment ajouter un élément, comment lire les
 * indicateurs, et donne une astuce générale.
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
