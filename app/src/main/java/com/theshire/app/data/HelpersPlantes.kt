package com.theshire.app.data

/**
 * Fonctions utilitaires liées aux plantes, légumes et catégories.
 * Regroupées ici pour être réutilisables dans toute l'app.
 */

/**
 * Retourne l'emoji correspondant à la catégorie d'un légume.
 */
fun getEmojiCategorie(categorie: String): String = when {
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

/**
 * Vérifie si un légume est volumineux (nécessite beaucoup d'espace).
 */
fun estPlanteVolumineuse(nomLegume: String): Boolean = nomLegume in listOf(
    "Tomate", "Courgette", "Potiron", "Courge", "Aubergine",
    "Poivron", "Concombre", "Melon", "Chou pommé", "Brocoli",
    "Chou-fleur", "Topinambour"
)

/**
 * Retourne la distance recommandée entre plants (en cm) à partir
 * du texte "plantation" d'un légume.
 */
fun getDistanceEntrePlants(legume: LegumeEntity): String {
    val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) cm entre plants").find(legume.plantation)
    return match?.groupValues?.get(1) ?: "20"
}

/**
 * Extrait la densité de plantation (plants/m²) depuis le texte "plantation".
 */
fun getDensiteFromPlantation(legume: LegumeEntity): Int {
    val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) plants/m²").find(legume.plantation)
    return if (match != null) {
        val valeur = match.groupValues[1]
        when {
            valeur.contains(",") -> valeur.replace(",", ".").toDouble().toInt()
            valeur.contains("-") -> {
                val parts = valeur.split("-")
                (parts[0].toInt() + parts[1].toInt()) / 2
            }
            else -> valeur.toInt()
        }
    } else {
        when (legume.categorie) {
            "Fruit", "Cucurbitacée", "Chou", "Tubercule" -> 4
            "Racine", "Alliacé" -> 30
            "Feuille", "Légumineuse" -> 20
            "Aromatique" -> 15
            "Fleur annuelle", "Fleur vivace" -> 10
            else -> 9
        }
    }
}

/**
 * Vérifie si on peut planter un légume dans une case donnée
 * (règle : pas 2 plantes volumineuses adjacentes).
 */
fun peutPlanterIci(carre: CarreEntity, caseNumero: Int, legumeNom: String): Boolean {
    if (!estPlanteVolumineuse(legumeNom)) return true
    val adj = when (caseNumero) {
        1 -> listOf(2, 4, 5)
        2 -> listOf(1, 3, 4, 5, 6)
        3 -> listOf(2, 5, 6)
        4 -> listOf(1, 2, 5, 7, 8)
        5 -> listOf(1, 2, 3, 4, 6, 7, 8, 9)
        6 -> listOf(2, 3, 5, 8, 9)
        7 -> listOf(4, 5, 8)
        8 -> listOf(4, 5, 6, 7, 9)
        9 -> listOf(5, 6, 8)
        else -> emptyList()
    }
    val legumesAdj = adj.mapNotNull {
        when (it) {
            1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3
            4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6
            7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9
            else -> null
        }
    }
    return !legumesAdj.any { it != null && estPlanteVolumineuse(it) }
}
