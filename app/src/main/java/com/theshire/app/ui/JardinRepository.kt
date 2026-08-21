package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.PlancheEntity
import kotlinx.coroutines.flow.Flow

class JardinRepository(context: Context) {
    
    private val plancheDao = AppDatabase.getDatabase(context).plancheDao()
    
    val planches: Flow<List<PlancheEntity>> = plancheDao.getAllPlanches()
    
    fun getCarresForPlanche(plancheId: Long): Flow<List<CarreEntity>> {
        return plancheDao.getCarresForPlanche(plancheId)
    }
    
    suspend fun ajouterPlanche(nom: String, nombreCarres: Int): Long {
        val plancheId = plancheDao.insertPlanche(
            PlancheEntity(nom = nom)
        )
        
        // Créer les carrés d'1m² pour cette planche
        for (i in 1..nombreCarres) {
            plancheDao.insertCarre(
                CarreEntity(
                    plancheId = plancheId,
                    position = i
                )
            )
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
    
    suspend fun ajouterCarre(plancheId: Long, position: Int) {
        plancheDao.insertCarre(
            CarreEntity(
                plancheId = plancheId,
                position = position
            )
        )
    }
}
