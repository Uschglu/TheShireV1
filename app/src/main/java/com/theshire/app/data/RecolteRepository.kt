package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les récoltes (4e onglet de Stocks).
 *
 * Utilisé par :
 *  - L'onglet "Récoltes" de Stocks : liste, statistiques
 *  - La fiche "Récolte" : détail + modification + suppression
 *  - L'ajout manuel d'une récolte (marché, don, cueillette sauvage…)
 *  - La récolte automatique depuis une culture (génère l'entité ici)
 *
 * Toutes les quantités sont en kilogrammes (double).
 */
class RecolteRepository(context: Context) {
    
    private val recolteDao = AppDatabase.getDatabase(context).recolteDao()
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    /**
     * Toutes les récoltes, triées par date décroissante.
     */
    val toutesLesRecoltes: Flow<List<RecolteEntity>> = recolteDao.getAllRecoltes()
    
    /**
     * Poids total récolté (tous légumes confondus).
     */
    val poidsTotalGlobal: Flow<Double> = recolteDao.getPoidsTotalGlobal()
    
    suspend fun getRecolteParId(id: Long): RecolteEntity? {
        return recolteDao.getRecolteParId(id)
    }
    
    fun countRecoltes(): Flow<Int> {
        return recolteDao.countRecoltes()
    }
    
    // ============================================================
    // LECTURE — par légume
    // ============================================================
    
    fun getRecoltesPourLegume(legumeNom: String): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesPourLegume(legumeNom)
    }
    
    fun getPoidsTotalPourLegume(legumeNom: String): Flow<Double> {
        return recolteDao.getPoidsTotalPourLegume(legumeNom)
    }
    
    // ============================================================
    // LECTURE — par culture d'origine
    // ============================================================
    
    fun getRecoltesPourCulture(cultureId: Long): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesPourCulture(cultureId)
    }
    
    // ============================================================
    // LECTURE — par période
    // ============================================================
    
    fun getRecoltesEntreDates(debut: Long, fin: Long): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesEntreDates(debut, fin)
    }
    
    fun getPoidsTotalEntreDates(debut: Long, fin: Long): Flow<Double> {
        return recolteDao.getPoidsTotalEntreDates(debut, fin)
    }
    
    // ============================================================
    // LECTURE — par source
    // ============================================================
    
    fun getRecoltesLieesAUneCulture(): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesLieesAUneCulture()
    }
    
    fun getRecoltesManuelles(): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesManuelles()
    }
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Ajoute une récolte manuellement.
     * 
     * Utilisé par le bouton "+" de l'onglet Récoltes (achat marché, don,
     * cueillette sauvage, etc.).
     * 
     * @param recolte RecolteEntity (sans id)
     * @return ID de la récolte créée
     */
    suspend fun ajouterRecolte(recolte: RecolteEntity): Long {
        return recolteDao.insertRecolte(recolte)
    }
    
    /**
     * Ajoute une récolte liée à une culture (appelée par CultureRepository).
     * 
     * ⚠️ En usage normal, c'est `CultureRepository.terminerAvecRecolte()`
     *    qu'on appelle. Cette méthode existe pour un cas particulier.
     */
    suspend fun ajouterRecoltePourCulture(
        culture: CultureEntity,
        poidsKg: Double,
        notes: String? = null
    ): Long {
        val recolte = RecolteEntity(
            legumeNom = culture.legumeNom,
            varieteNom = culture.varieteNom,
            emoji = culture.emoji,
            poidsKg = poidsKg,
            dateRecolte = System.currentTimeMillis(),
            cultureId = culture.id,
            notes = notes
        )
        return recolteDao.insertRecolte(recolte)
    }
    
    /**
     * Met à jour une récolte existante.
     */
    suspend fun mettreAJourRecolte(recolte: RecolteEntity) {
        recolteDao.updateRecolte(recolte)
    }
    
    /**
     * Met à jour uniquement le poids d'une récolte.
     */
    suspend fun mettreAJourPoids(recolteId: Long, poidsKg: Double) {
        recolteDao.updatePoids(recolteId, poidsKg)
    }
    
    /**
     * Met à jour uniquement les notes d'une récolte.
     */
    suspend fun mettreAJourNotes(recolteId: Long, notes: String?) {
        recolteDao.updateNotes(recolteId, notes)
    }
    
    /**
     * Supprime une récolte.
     * 
     * ⚠️ Si la récolte est liée à une culture terminée, la culture n'est
     *    PAS rétablie (l'historique reste : la culture a bien été récoltée).
     */
    suspend fun supprimerRecolte(recolte: RecolteEntity) {
        recolteDao.deleteRecolte(recolte)
    }
    
    suspend fun supprimerRecolteParId(id: Long) {
        recolteDao.deleteRecolteParId(id)
    }
    
    // ============================================================
    // STATISTIQUES
    // ============================================================
    
    suspend fun getLegumesRecoltes(): List<String> {
        return recolteDao.getLegumesRecoltes()
    }
    
    suspend fun getPoidsTotalParLegume(): List<LegumePoidsTotal> {
        return recolteDao.getPoidsTotalParLegume()
    }
}
