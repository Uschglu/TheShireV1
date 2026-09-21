package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les jeunes plants.
 * 
 * Encapsule l'accès au JeunePlantDao et fournit une API métier claire.
 * 
 * Utilisé par : EcranStocks (onglet Jeunes plants)
 */
class JeunePlantRepository(context: Context) {
    
    private val jeunePlantDao = AppDatabase.getDatabase(context).jeunePlantDao()
    
    // ============================================================
    // LECTURE
    // ============================================================
    
    /**
     * Tous les jeunes plants actifs (non encore plantés).
     */
    val jeunesPlantsActifs: Flow<List<JeunePlantEntity>> = jeunePlantDao.getJeunesPlantsActifs()
    
    /**
     * Tous les jeunes plants (y compris inactifs).
     */
    val tousJeunesPlants: Flow<List<JeunePlantEntity>> = jeunePlantDao.getAllJeunesPlants()
    
    /**
     * Récupère un jeune plant par son ID.
     */
    suspend fun getJeunePlantParId(id: Long): JeunePlantEntity? {
        return jeunePlantDao.getJeunePlantParId(id)
    }
    
    /**
     * Récupère les jeunes plants d'un légume donné.
     */
    fun getJeunesPlantsPourLegume(legumeNom: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getJeunesPlantsPourLegume(legumeNom)
    }
    
    /**
     * Récupère les jeunes plants d'un stade donné.
     * Ex : tous les plants "Prêt à planter".
     */
    fun getJeunesPlantsParStade(stade: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getJeunesPlantsParStade(stade)
    }
    
    /**
     * Compte le nombre de jeunes plants actifs.
     */
    fun countJeunesPlantsActifs(): Flow<Int> {
        return jeunePlantDao.countJeunesPlantsActifs()
    }
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Ajoute un nouveau jeune plant.
     * Retourne l'ID généré.
     */
    suspend fun ajouterJeunePlant(jeunePlant: JeunePlantEntity): Long {
        return jeunePlantDao.insertJeunePlant(jeunePlant)
    }
    
    /**
     * Met à jour un jeune plant existant.
     */
    suspend fun mettreAJourJeunePlant(jeunePlant: JeunePlantEntity) {
        jeunePlantDao.updateJeunePlant(jeunePlant)
    }
    
    /**
     * Supprime un jeune plant.
     */
    suspend fun supprimerJeunePlant(jeunePlant: JeunePlantEntity) {
        jeunePlantDao.deleteJeunePlant(jeunePlant)
    }
    
    /**
     * Supprime un jeune plant par son ID.
     */
    suspend fun supprimerJeunePlantParId(id: Long) {
        jeunePlantDao.deleteJeunePlantParId(id)
    }
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    /**
     * Change le stade d'un jeune plant.
     * Ex : passer de "Semis" à "Repiqué".
     */
    suspend fun changerStade(jeunePlantId: Long, nouveauStade: String) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        jeunePlantDao.updateJeunePlant(plant.copy(stade = nouveauStade))
    }
    
    /**
     * Décrémente la quantité (ex : après avoir planté 1 godet en pleine terre).
     * Marque automatiquement comme inactif si quantité = 0.
     */
    suspend fun decrementerQuantite(jeunePlantId: Long, quantiteUtilisee: Int) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        val nouvelleQuantite = maxOf(0, plant.quantite - quantiteUtilisee)
        val nouveauPlant = plant.copy(
            quantite = nouvelleQuantite,
            estActif = nouvelleQuantite > 0
        )
        jeunePlantDao.updateJeunePlant(nouveauPlant)
    }
    
    /**
     * Ajoute de la quantité à un jeune plant existant.
     */
    suspend fun incrementerQuantite(jeunePlantId: Long, quantiteAjoutee: Int) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        val nouveauPlant = plant.copy(
            quantite = plant.quantite + quantiteAjoutee,
            estActif = true
        )
        jeunePlantDao.updateJeunePlant(nouveauPlant)
    }
    
    /**
     * Marque un jeune plant comme planté (retiré du stock actif).
     */
    suspend fun marquerPlante(jeunePlantId: Long) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        jeunePlantDao.updateJeunePlant(plant.copy(estActif = false))
    }
    
    /**
     * Réactive un jeune plant précédemment marqué planté.
     */
    suspend fun reactiver(jeunePlantId: Long) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        jeunePlantDao.updateJeunePlant(plant.copy(estActif = true))
    }
}
