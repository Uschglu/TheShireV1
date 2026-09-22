package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les graines.
 * 
 * Encapsule l'accès au GraineDao et fournit une API métier claire.
 * 
 * Utilisé par :
 *  - EcranStocks (onglet Graines)
 *  - Mode réel : décrément du stock à la création d'un semis
 */
class GraineRepository(context: Context) {
    
    private val graineDao = AppDatabase.getDatabase(context).graineDao()
    
    // ============================================================
    // LECTURE
    // ============================================================
    
    /**
     * Toutes les graines actives (non terminées), triées par ajout récent.
     */
    val grainesActives: Flow<List<GraineEntity>> = graineDao.getGrainesActives()
    
    /**
     * Toutes les graines (y compris terminées).
     */
    val toutesGraines: Flow<List<GraineEntity>> = graineDao.getAllGraines()
    
    /**
     * Récupère une graine par son ID.
     */
    suspend fun getGraineParId(id: Long): GraineEntity? {
        return graineDao.getGraineParId(id)
    }
    
    /**
     * Récupère les graines d'un légume donné.
     */
    fun getGrainesPourLegume(legumeNom: String): Flow<List<GraineEntity>> {
        return graineDao.getGrainesPourLegume(legumeNom)
    }
    
    /**
     * Récupère les graines dont la quantité est faible.
     * Utile pour afficher une alerte "à racheter".
     */
    suspend fun getGrainesQuantiteFaible(seuil: Int = 5): List<GraineEntity> {
        return graineDao.getGrainesQuantiteFaible(seuil)
    }
    
    /**
     * Compte le nombre de graines actives.
     */
    fun countGrainesActives(): Flow<Int> {
        return graineDao.countGrainesActives()
    }
    
    // ============================================================
    // LECTURE — spécifique mode réel
    // ============================================================
    
    /**
     * Cherche le sachet exact correspondant à un légume + variété.
     * 
     * Matching :
     *  - legumeNom identique (insensible à la casse)
     *  - varieteNom identique si fourni, null si non fourni
     * 
     * On ne renvoie QUE les sachets actifs (quantite > 0).
     * Si plusieurs sachets correspondent, on renvoie celui avec la plus
     * grande quantité (le plus susceptible d'avoir assez de graines).
     * 
     * @return Le sachet trouvé, ou null si aucun ne correspond.
     */
    suspend fun getGraineExacte(legumeNom: String, varieteNom: String?): GraineEntity? {
        val actives = graineDao.getAllGrainesSync()
            .filter { it.estActif && it.quantite > 0 }
            .filter { graine ->
                graine.legumeNom.equals(legumeNom, ignoreCase = true) &&
                normaliserVariete(graine.varieteNom) == normaliserVariete(varieteNom)
            }
        return actives.maxByOrNull { it.quantite }
    }
    
    /**
     * Vérifie rapidement si l'utilisateur a assez de graines en stock
     * pour un légume + variété + quantité donnés.
     */
    suspend fun aAssezDeGraines(
        legumeNom: String,
        varieteNom: String?,
        quantiteDemandee: Int
    ): Boolean {
        val graine = getGraineExacte(legumeNom, varieteNom) ?: return false
        return graine.quantite >= quantiteDemandee
    }
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Ajoute une nouvelle graine.
     * Retourne l'ID généré.
     */
    suspend fun ajouterGraine(graine: GraineEntity): Long {
        return graineDao.insertGraine(graine)
    }
    
    /**
     * Met à jour une graine existante.
     */
    suspend fun mettreAJourGraine(graine: GraineEntity) {
        graineDao.updateGraine(graine)
    }
    
    /**
     * Supprime une graine.
     */
    suspend fun supprimerGraine(graine: GraineEntity) {
        graineDao.deleteGraine(graine)
    }
    
    /**
     * Supprime une graine par son ID.
     */
    suspend fun supprimerGraineParId(id: Long) {
        graineDao.deleteGraineParId(id)
    }
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    /**
     * Décrémente la quantité d'une graine (ex : après avoir semé).
     * Ne descend pas en dessous de 0.
     * Marque automatiquement comme inactive si quantité = 0.
     */
    suspend fun decrementerQuantite(graineId: Long, quantiteUtilisee: Int) {
        val graine = graineDao.getGraineParId(graineId) ?: return
        val nouvelleQuantite = maxOf(0, graine.quantite - quantiteUtilisee)
        val nouvelleGraine = graine.copy(
            quantite = nouvelleQuantite,
            estActif = nouvelleQuantite > 0
        )
        graineDao.updateGraine(nouvelleGraine)
    }
    
    /**
     * Ajoute de la quantité à une graine existante (ex : rachat d'un sachet).
     */
    suspend fun incrementerQuantite(graineId: Long, quantiteAjoutee: Int) {
        val graine = graineDao.getGraineParId(graineId) ?: return
        val nouvelleGraine = graine.copy(
            quantite = graine.quantite + quantiteAjoutee,
            estActif = true
        )
        graineDao.updateGraine(nouvelleGraine)
    }
    
    /**
     * Marque une graine comme terminée (sachet vide) sans la supprimer.
     */
    suspend fun marquerTerminee(graineId: Long) {
        val graine = graineDao.getGraineParId(graineId) ?: return
        graineDao.updateGraine(graine.copy(estActif = false))
    }
    
    /**
     * Réactive une graine précédemment marquée terminée.
     */
    suspend fun reactiver(graineId: Long) {
        val graine = graineDao.getGraineParId(graineId) ?: return
        graineDao.updateGraine(graine.copy(estActif = true))
    }
    
    // ============================================================
    // HELPERS PRIVÉS
    // ============================================================
    
    /**
     * Normalise le nom de variété pour comparaison :
     *  - null et chaîne vide sont équivalents
     *  - trim
     */
    private fun normaliserVariete(variete: String?): String? {
        return variete?.trim()?.takeIf { it.isNotBlank() }
    }
}
