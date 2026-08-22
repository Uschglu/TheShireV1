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
        
        // Créer les carrés d'1m² selon la largeur et la longueur
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
    
    // Fonction pour vérifier la compatibilité entre deux légumes
    suspend fun getCouleurCompatibilite(legumeActuel: String, legumeVoisin: String): ColorCompat {
        val legume = legumeDao.getAllLegumes().first().find { it.nom == legumeActuel }
            ?: return ColorCompat.NEUTRE
        
        val voisin = legumeDao.getAllLegumes().first().find { it.nom == legumeVoisin }
            ?: return ColorCompat.NEUTRE
        
        // Vérifier les bonnes associations
        if (legume.bonnesAssociations.contains(legumeVoisin, ignoreCase = true) ||
            voisin.bonnesAssociations.contains(legumeActuel, ignoreCase = true)) {
            return ColorCompat.BONNE
        }
        
        // Vérifier les mauvaises associations
        if (legume.mauvaisesAssociations.contains(legumeVoisin, ignoreCase = true) ||
            voisin.mauvaisesAssociations.contains(legumeActuel, ignoreCase = true)) {
            return ColorCompat.MAUVAISE
        }
        
        return ColorCompat.NEUTRE
    }
}

enum class ColorCompat {
    BONNE, NEUTRE, MAUVAISE
}
