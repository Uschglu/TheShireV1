package com.theshire.app.data

import java.util.Calendar

data class AvertissementRotation(
    val message: String,
    val niveau: NiveauRisque
)

enum class NiveauRisque {
    FAIBLE, MOYEN, ELEVE
}

class RotationRepository {
    
    // Définir les familles de légumes
    private val familles = mapOf(
        "Tomate" to "Solanacées",
        "Poivron" to "Solanacées",
        "Aubergine" to "Solanacées",
        "Pomme de terre" to "Solanacées",
        "Chou" to "Brassicacées",
        "Brocoli" to "Brassicacées",
        "Chou-fleur" to "Brassicacées",
        "Radis" to "Brassicacées",
        "Navet" to "Brassicacées",
        "Rutabaga" to "Brassicacées",
        "Chou frisé (Kale)" to "Brassicacées",
        "Oignon" to "Alliacées",
        "Ail" to "Alliacées",
        "Poireau" to "Alliacées",
        "Ciboulette" to "Alliacées",
        "Haricot vert" to "Légumineuses",
        "Petit pois" to "Légumineuses",
        "Courgette" to "Cucurbitacées",
        "Concombre" to "Cucurbitacées",
        "Potiron" to "Cucurbitacées",
        "Carotte" to "Apiacées",
        "Panais" to "Apiacées",
        "Persil" to "Apiacées",
        "Cerfeuil tubéreux" to "Apiacées",
        "Coriandre" to "Apiacées",
        "Aneth" to "Apiacées",
        "Épinard" to "Chénopodiacées",
        "Betterave" to "Chénopodiacées",
        "Salade" to "Astéracées",
        "Cardon" to "Astéracées",
        "Topinambour" to "Astéracées",
        "Basilic" to "Lamiacées",
        "Menthe" to "Lamiacées",
        "Thym" to "Lamiacées",
        "Romarin" to "Lamiacées"
    )
    
    fun getFamille(legume: String): String {
        return familles[legume] ?: "Autre"
    }
    
    fun getAvertissement(
        nouveauLegume: String,
        carre: CarreEntity
    ): AvertissementRotation? {
        val nouvelleFamille = getFamille(nouveauLegume)
        val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR)
        val anneeDerniereCulture = carre.anneeCulture
        
        // Si le carré n'a jamais été cultivé (année 0), pas d'avertissement
        if (anneeDerniereCulture == 0) {
            return null
        }
        
        // Si la dernière culture date de cette année, pas d'avertissement
        if (anneeDerniereCulture == anneeActuelle) {
            return null
        }
        
        // Vérifier si la même famille était présente l'année dernière
        val famillesAnterieures = carre.famillesPlantees.split(",").filter { it.isNotEmpty() }
        val famillePresente = famillesAnterieures.any { famille -> famille == nouvelleFamille }
        
        if (famillePresente) {
            val risque = when (nouvelleFamille) {
                "Solanacées", "Brassicacées", "Cucurbitacées" -> NiveauRisque.ELEVE
                "Alliacées", "Apiacées" -> NiveauRisque.MOYEN
                else -> NiveauRisque.FAIBLE
            }
            
            val message = when (risque) {
                NiveauRisque.ELEVE -> "⚠️ ATTENTION : Risque élevé !\n\n" +
                    "Un légume de la famille des $nouvelleFamille était déjà présent dans ce carré en $anneeDerniereCulture.\n\n" +
                    "• Risque accru de maladies (mildiou, fusariose, hernie...)\n" +
                    "• Risque accru de ravageurs (doryphores, piérides, nématodes...)\n" +
                    "• Appauvrissement important du sol\n" +
                    "• Baisse significative de productivité\n\n" +
                    "Recommandation : Attendre 3-4 ans avant de replanter des $nouvelleFamille ici."
                
                NiveauRisque.MOYEN -> "⚠️ Attention : Risque modéré\n\n" +
                    "Un légume de la famille des $nouvelleFamille était déjà présent dans ce carré en $anneeDerniereCulture.\n\n" +
                    "• Risque de maladies spécifiques\n" +
                    "• Appauvrissement du sol en nutriments\n" +
                    "• Baisse de productivité possible\n\n" +
                    "Recommandation : Attendre 2-3 ans avant de replanter des $nouvelleFamille ici."
                
                NiveauRisque.FAIBLE -> "ℹ️ Information : Risque faible\n\n" +
                    "Un légume de la famille des $nouvelleFamille était déjà présent dans ce carré en $anneeDerniereCulture.\n\n" +
                    "• Léger risque de maladies\n" +
                    "• Appauvrissement modéré du sol\n\n" +
                    "Recommandation : Un apport de compost est conseillé."
            }
            
            return AvertissementRotation(message, risque)
        }
        
        return null
    }
    
    fun getFamillesFromCarre(carre: CarreEntity): List<String> {
        val famillesList = mutableListOf<String>()
        val legumes = listOfNotNull(
            carre.case1, carre.case2, carre.case3,
            carre.case4, carre.case5, carre.case6,
            carre.case7, carre.case8, carre.case9
        )
        
        legumes.forEach { legume ->
            val famille = getFamille(legume)
            if (famille !in famillesList) {
                famillesList.add(famille)
            }
        }
        
        return famillesList
    }
    
    fun getSuggestionRotation(famillePrecedente: String): String {
        return when (famillePrecedente) {
            "Solanacées" -> "Suggestion : Plantez des Légumineuses (haricot, pois) pour enrichir le sol en azote, ou des Brassicacées (chou, radis)."
            "Brassicacées" -> "Suggestion : Plantez des Légumineuses (haricot, pois) ou des Alliacées (oignon, ail)."
            "Cucurbitacées" -> "Suggestion : Plantez des Légumineuses (haricot, pois) ou des Chénopodiacées (épinard, betterave)."
            "Alliacées" -> "Suggestion : Plantez des Solanacées (tomate) ou des Cucurbitacées (courgette)."
            "Apiacées" -> "Suggestion : Plantez des Brassicacées (chou) ou des Légumineuses (haricot)."
            "Légumineuses" -> "Suggestion : Plantez des Solanacées (tomate) ou des Brassicacées (chou) pour profiter de l'azote enrichi."
            "Chénopodiacées" -> "Suggestion : Plantez des Cucurbitacées (courgette) ou des Alliacées (oignon)."
            "Astéracées" -> "Suggestion : Plantez des Légumineuses (haricot) ou des Brassicacées (chou)."
            else -> "Suggestion : Plantez des Légumineuses (haricot, pois) pour enrichir le sol."
        }
    }
}
