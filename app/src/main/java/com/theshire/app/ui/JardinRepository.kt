package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.PlancheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class JardinRepository(context: Context) {
    
    private val plancheDao = AppDatabase.getDatabase(context).plancheDao()
    
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
    
    // Fonction pour obtenir la famille d'un légume
    private fun getFamilleLegume(legume: String): String {
        return when (legume) {
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
