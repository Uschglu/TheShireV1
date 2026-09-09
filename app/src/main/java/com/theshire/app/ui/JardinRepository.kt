package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.PlancheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class JardinRepository(context: Context) {
    
    private val plancheDao = AppDatabase.getDatabase(context).plancheDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    val planches: Flow<List<PlancheEntity>> = plancheDao.getAllPlanches()
    
    fun getCarresForPlanche(plancheId: Long): Flow<List<CarreEntity>> {
        return plancheDao.getCarresForPlanche(plancheId)
    }
    
    suspend fun ajouterPlanche(nom: String, largeur: Int, longueur: Int): Long {
        val plancheId = plancheDao.insertPlanche(
            PlancheEntity(
                nom = nom,
                largeur = largeur,
                longueur = longueur
            )
        )
        
        for (y in 0 until longueur) {
            for (x in 0 until largeur) {
                plancheDao.insertCarre(
                    CarreEntity(
                        plancheId = plancheId,
                        positionX = x,
                        positionY = y
                    )
                )
            }
        }
        
        return plancheId
    }
    
    suspend fun supprimerPlanche(planche: PlancheEntity) {
        plancheDao.deleteCarresForPlanche(planche.id)
        plancheDao.deletePlanche(planche)
    }
    
    suspend fun modifierCasePrecise(carre: CarreEntity, caseNumero: Int, legumeNom: String?) {
        val dateActuelle = System.currentTimeMillis()
        val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR)
        
        val nouveauCarre = when (caseNumero) {
            1 -> carre.copy(
                case1 = legumeNom, 
                datePlantationCase1 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            2 -> carre.copy(
                case2 = legumeNom, 
                datePlantationCase2 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            3 -> carre.copy(
                case3 = legumeNom, 
                datePlantationCase3 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            4 -> carre.copy(
                case4 = legumeNom, 
                datePlantationCase4 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            5 -> carre.copy(
                case5 = legumeNom, 
                datePlantationCase5 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            6 -> carre.copy(
                case6 = legumeNom, 
                datePlantationCase6 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            7 -> carre.copy(
                case7 = legumeNom, 
                datePlantationCase7 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            8 -> carre.copy(
                case8 = legumeNom, 
                datePlantationCase8 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            9 -> carre.copy(
                case9 = legumeNom, 
                datePlantationCase9 = if (legumeNom != null) dateActuelle else null,
                anneeCulture = if (legumeNom != null) anneeActuelle else carre.anneeCulture
            )
            else -> carre
        }
        
        // Mettre à jour les familles plantées
        val legumesActuels = listOfNotNull(
            nouveauCarre.case1, nouveauCarre.case2, nouveauCarre.case3,
            nouveauCarre.case4, nouveauCarre.case5, nouveauCarre.case6,
            nouveauCarre.case7, nouveauCarre.case8, nouveauCarre.case9
        )
        
        val famillesSet = mutableSetOf<String>()
        legumesActuels.forEach { legume ->
            val famille = getFamilleLegume(legume)
            if (famille != "Autre") {
                famillesSet.add(famille)
            }
        }
        
        val carreFinal = nouveauCarre.copy(famillesPlantees = famillesSet.joinToString(","))
        
        plancheDao.updateCarre(carreFinal)
    }
    
    suspend fun getLegumesPlantes(): List<String> {
        val listeLegumes = mutableListOf<String>()
        
        val toutesPlanches = plancheDao.getAllPlanches().first()
        
        toutesPlanches.forEach { planche ->
            val carres = plancheDao.getCarresForPlanche(planche.id).first()
            carres.forEach { carre ->
                listOf(
                    carre.case1, carre.case2, carre.case3,
                    carre.case4, carre.case5, carre.case6,
                    carre.case7, carre.case8, carre.case9
                ).forEach { legume ->
                    if (legume != null && legume !in listeLegumes) {
                        listeLegumes.add(legume)
                    }
                }
            }
        }
        
        return listeLegumes
    }
    
    suspend fun getDatesPlantation(): Map<String, Long> {
        val dates = mutableMapOf<String, Long>()
        val toutesPlanches = plancheDao.getAllPlanches().first()
        
        toutesPlanches.forEach { planche ->
            val carres = plancheDao.getCarresForPlanche(planche.id).first()
            carres.forEach { carre ->
                listOf(
                    carre.case1 to carre.datePlantationCase1,
                    carre.case2 to carre.datePlantationCase2,
                    carre.case3 to carre.datePlantationCase3,
                    carre.case4 to carre.datePlantationCase4,
                    carre.case5 to carre.datePlantationCase5,
                    carre.case6 to carre.datePlantationCase6,
                    carre.case7 to carre.datePlantationCase7,
                    carre.case8 to carre.datePlantationCase8,
                    carre.case9 to carre.datePlantationCase9
                ).forEach { (legume, date) ->
                    if (legume != null && date != null) {
                        dates[legume] = date
                    }
                }
            }
        }
        
        return dates
    }
    
    // ========== NOUVELLES FONCTIONS DE VALIDATION ==========
    
    // Fonction pour obtenir les informations d'un légume
    suspend fun getLegumeInfo(legumeNom: String): LegumeEntity? {
        // Extraire le nom de base si c'est une variété
        val nomBase = if (legumeNom.contains("(")) {
            legumeNom.substringBefore("(").trim()
        } else {
            legumeNom
        }
        return legumeDao.getLegumeByNom(nomBase)
    }
    
    // Fonction pour extraire la distance entre rangs
    fun extraireDistanceRangs(plantation: String): Int {
        val match = Regex("(\\d+-\\d+|\\d+) cm entre rangs").find(plantation)
        return if (match != null) {
            val valeur = match.groupValues[1]
            if (valeur.contains("-")) {
                val parts = valeur.split("-")
                (parts[0].toInt() + parts[1].toInt()) / 2
            } else {
                valeur.toInt()
            }
        } else {
            30 // Valeur par défaut
        }
    }
    
    // Fonction pour extraire la distance entre plants
    fun extraireDistancePlants(plantation: String): Int {
        val match = Regex("(\\d+-\\d+|\\d+) cm entre plants").find(plantation)
        return if (match != null) {
            val valeur = match.groupValues[1]
            if (valeur.contains("-")) {
                val parts = valeur.split("-")
                (parts[0].toInt() + parts[1].toInt()) / 2
            } else {
                valeur.toInt()
            }
        } else {
            20 // Valeur par défaut
        }
    }
    
    // Fonction pour calculer le nombre de lignes dans une sous-case
    suspend fun calculerNombreLignes(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
        val distanceEntreRangs = extraireDistanceRangs(legume.plantation)
        // Une sous-case fait environ 33cm x 33cm (1m² divisé en 9)
        val largeurSousCase = 33 // en cm
        return maxOf(1, largeurSousCase / maxOf(distanceEntreRangs, 1))
    }
    
    // Fonction pour calculer le nombre de plants par ligne dans une sous-case
    suspend fun calculerPlantsParLigne(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
        val distanceEntrePlants = extraireDistancePlants(legume.plantation)
        // Une sous-case fait environ 33cm de long
        val longueurSousCase = 33 // en cm
        return maxOf(1, longueurSousCase / maxOf(distanceEntrePlants, 1))
    }
    
    // Fonction pour calculer le nombre total de plants dans une sous-case
    suspend fun calculerTotalPlants(legumeNom: String): Int {
        val nombreLignes = calculerNombreLignes(legumeNom)
        val plantsParLigne = calculerPlantsParLigne(legumeNom)
        return nombreLignes * plantsParLigne
    }
    
    // Fonction pour vérifier si une plante est volumineuse
    fun estPlanteVolumineuse(nomLegume: String): Boolean {
        val nomBase = if (nomLegume.contains("(")) {
            nomLegume.substringBefore("(").trim()
        } else {
            nomLegume
        }
        return nomBase in listOf(
            "Tomate", "Courgette", "Potiron", "Courge", "Aubergine", 
            "Poivron", "Concombre", "Melon", "Chou pommé", "Brocoli", 
            "Chou-fleur", "Topinambour"
        )
    }
    
    // Fonction pour obtenir les cases adjacentes
    fun getCasesAdjacentes(caseNumero: Int): List<Int> {
        return when (caseNumero) {
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
    }
    
    // Fonction pour obtenir la plante dans une case
    fun getPlanteDansCase(carre: CarreEntity, caseNumero: Int): String? {
        return when (caseNumero) {
            1 -> carre.case1
            2 -> carre.case2
            3 -> carre.case3
            4 -> carre.case4
            5 -> carre.case5
            6 -> carre.case6
            7 -> carre.case7
            8 -> carre.case8
            9 -> carre.case9
            else -> null
        }
    }
    
    // Fonction pour vérifier si une plante peut être plantée dans une case
    suspend fun peutPlanterDansCase(carre: CarreEntity, caseNumero: Int, legumeNom: String): Boolean {
        if (estPlanteVolumineuse(legumeNom)) {
            // Vérifier les cases adjacentes pour les autres grandes plantes
            val casesAdjacentes = getCasesAdjacentes(caseNumero)
            for (caseAdj in casesAdjacentes) {
                val planteAdj = getPlanteDansCase(carre, caseAdj)
                if (planteAdj != null && estPlanteVolumineuse(planteAdj)) {
                    return false
                }
            }
        }
        return true
    }
    
    // Fonction pour vérifier les associations entre deux plantes
    suspend fun verifierAssociation(plante1: String, plante2: String): String {
        val legume1 = getLegumeInfo(plante1) ?: return "neutre"
        
        // Extraire le nom de base de la plante2
        val nomBase2 = if (plante2.contains("(")) {
            plante2.substringBefore("(").trim()
        } else {
            plante2
        }
        
        if (legume1.bonnesAssociations.contains(nomBase2, true)) return "bonne"
        if (legume1.mauvaisesAssociations.contains(nomBase2, true)) return "mauvaise"
        return "neutre"
    }
    
    // Fonction pour vérifier les associations avec les cases adjacentes
    suspend fun verifierAssociationsAdjacentes(carre: CarreEntity, caseNumero: Int, legumeNom: String): List<Pair<String, String>> {
        val resultats = mutableListOf<Pair<String, String>>()
        val casesAdjacentes = getCasesAdjacentes(caseNumero)
        
        for (caseAdj in casesAdjacentes) {
            val planteAdj = getPlanteDansCase(carre, caseAdj)
            if (planteAdj != null) {
                val association = verifierAssociation(legumeNom, planteAdj)
                if (association != "neutre") {
                    resultats.add(Pair(planteAdj, association))
                }
            }
        }
        
        return resultats
    }
    
    // Fonction pour calculer la densité réelle d'une plante
    suspend fun getDensiteReelle(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
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
            // Calcul basé sur les distances
            val distancePlants = extraireDistancePlants(legume.plantation)
            val distanceRangs = extraireDistanceRangs(legume.plantation)
            val surfaceCm2 = 10000 // 1m² en cm²
            val surfaceParPlant = distancePlants * distanceRangs
            maxOf(1, surfaceCm2 / maxOf(surfaceParPlant, 1))
        }
    }
    
    // Fonction pour obtenir la famille d'un légume
    private fun getFamilleLegume(legume: String): String {
        val nomBase = if (legume.contains("(")) {
            legume.substringBefore("(").trim()
        } else {
            legume
        }
        
        return when (nomBase) {
            "Tomate", "Poivron", "Aubergine", "Pomme de terre" -> "Solanacées"
            "Chou", "Brocoli", "Chou-fleur", "Radis", "Navet", "Rutabaga", "Chou frisé (Kale)" -> "Brassicacées"
            "Oignon", "Ail", "Poireau", "Ciboulette" -> "Alliacées"
            "Haricot vert", "Petit pois" -> "Légumineuses"
            "Courgette", "Concombre", "Potiron" -> "Cucurbitacées"
            "Carotte", "Panais", "Persil", "Cerfeuil tubéreux", "Coriandre", "Aneth" -> "Apiacées"
            "Épinard", "Betterave" -> "Chénopodiacées"
            "Salade", "Cardon", "Topinambour" -> "Astéracées"
            "Basilic", "Menthe", "Thym", "Romarin" -> "Lamiacées"
            else -> "Autre"
        }
    }
}
