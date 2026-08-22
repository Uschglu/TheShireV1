package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.PlancheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
    
    suspend fun modifierCase(carre: CarreEntity, caseNumero: Int, legumeNom: String?) {
        val nouveauCarre = when (caseNumero) {
            1 -> carre.copy(case1 = legumeNom)
            2 -> carre.copy(case2 = legumeNom)
            3 -> carre.copy(case3 = legumeNom)
            4 -> carre.copy(case4 = legumeNom)
            5 -> carre.copy(case5 = legumeNom)
            6 -> carre.copy(case6 = legumeNom)
            7 -> carre.copy(case7 = legumeNom)
            8 -> carre.copy(case8 = legumeNom)
            9 -> carre.copy(case9 = legumeNom)
            else -> carre
        }
        plancheDao.updateCarre(nouveauCarre)
    }
    
    suspend fun modifierCasePrecise(carre: CarreEntity, caseNumero: Int, legumeNom: String?) {
        val dateActuelle = System.currentTimeMillis()
        val nouveauCarre = when (caseNumero) {
            1 -> carre.copy(case1 = legumeNom, datePlantationCase1 = if (legumeNom != null) dateActuelle else null)
            2 -> carre.copy(case2 = legumeNom, datePlantationCase2 = if (legumeNom != null) dateActuelle else null)
            3 -> carre.copy(case3 = legumeNom, datePlantationCase3 = if (legumeNom != null) dateActuelle else null)
            4 -> carre.copy(case4 = legumeNom, datePlantationCase4 = if (legumeNom != null) dateActuelle else null)
            5 -> carre.copy(case5 = legumeNom, datePlantationCase5 = if (legumeNom != null) dateActuelle else null)
            6 -> carre.copy(case6 = legumeNom, datePlantationCase6 = if (legumeNom != null) dateActuelle else null)
            7 -> carre.copy(case7 = legumeNom, datePlantationCase7 = if (legumeNom != null) dateActuelle else null)
            8 -> carre.copy(case8 = legumeNom, datePlantationCase8 = if (legumeNom != null) dateActuelle else null)
            9 -> carre.copy(case9 = legumeNom, datePlantationCase9 = if (legumeNom != null) dateActuelle else null)
            else -> carre
        }
        plancheDao.updateCarre(nouveauCarre)
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
}
