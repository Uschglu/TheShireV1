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
 *
 * ⚠️ MODE PROJECTION vs RÉEL :
 *    Les récoltes sont filtrées selon le mode actif (ModePreferences).
 *    - Une récolte auto depuis une culture hérite du mode de la culture
 *    - Une récolte manuelle utilise le mode actif au moment de l'ajout
 */
class RecolteRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val recolteDao = AppDatabase.getDatabase(context).recolteDao()
    
    // ============================================================
    // OUTILS INTERNES
    // ============================================================
    
    /**
     * Retourne la valeur `estProjection` correspondant au mode actif.
     * 
     *  - mode réel       → estProjection = false
     *  - mode projection → estProjection = true
     */
    private fun estProjectionDuModeActif(): Boolean {
        return !ModePreferences.estModeReel(appContext)
    }
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    /**
     * Toutes les récoltes, tous modes confondus, triées par date décroissante.
     * 
     * ⚠️ À utiliser avec parcimonie — pour l'affichage normal, préférer
     *    `recoltesDuModeActif`.
     */
    val toutesLesRecoltes: Flow<List<RecolteEntity>> = recolteDao.getAllRecoltes()
    
    /**
     * Poids total récolté (tous légumes confondus, tous modes confondus).
     */
    val poidsTotalGlobal: Flow<Double> = recolteDao.getPoidsTotalGlobal()
    
    // ============================================================
    // LECTURE — par mode
    // ============================================================
    
    /**
     * Toutes les récoltes du mode actif (projection OU réel).
     * 
     * C'est la méthode à privilégier pour l'affichage dans l'onglet Récoltes.
     * Le mode est lu dynamiquement via ModePreferences à chaque collecte.
     */
    fun recoltesDuModeActif(): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesParMode(estProjectionDuModeActif())
    }
    
    /**
     * Toutes les récoltes d'un mode explicite.
     */
    fun recoltesParMode(estProjection: Boolean): Flow<List<RecolteEntity>> {
        return recolteDao.getRecoltesParMode(estProjection)
    }
    
    /**
     * Poids total récolté pour le mode actif.
     */
    fun poidsTotalDuModeActif(): Flow<Double> {
        return recolteDao.getPoidsTotalParMode(estProjectionDuModeActif())
    }
    
    /**
     * Poids total récolté pour un mode explicite.
     */
    fun poidsTotalParMode(estProjection: Boolean): Flow<Double> {
        return recolteDao.getPoidsTotalParMode(estProjection)
    }
    
    // ============================================================
    // LECTURE — par identifiant / comptage
    // ============================================================
    
    suspend fun getRecolteParId(id: Long): RecolteEntity? {
        return recolteDao.getRecolteParId(id)
    }
    
    fun countRecoltes(): Flow<Int> {
        return recolteDao.countRecoltes()
    }
    
    fun countRecoltesDuModeActif(): Flow<Int> {
        return recolteDao.countRecoltesParMode(estProjectionDuModeActif())
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
    // ÉCRITURE — ajout
    // ============================================================
    
    /**
     * Ajoute une récolte manuellement.
     * 
     * ⚠️ Le mode (`estProjection`) est automatiquement forcé sur le mode actif
     *    au moment de l'ajout. Cela garantit que la récolte apparaît bien dans
     *    l'onglet du mode en cours.
     * 
     * Utilisé par le bouton "+" de l'onglet Récoltes (achat marché, don,
     * cueillette sauvage, etc.).
     * 
     * @param recolte RecolteEntity (sans id, estProjection ignoré)
     * @return ID de la récolte créée
     */
    suspend fun ajouterRecolte(recolte: RecolteEntity): Long {
        val recolteModee = recolte.copy(estProjection = estProjectionDuModeActif())
        return recolteDao.insertRecolte(recolteModee)
    }
    
    /**
     * Ajoute une récolte liée à une culture (appelée par CultureRepository).
     * 
     * ⚠️ La récolte hérite du mode de la culture source (projection ou réel).
     *    C'est indispensable pour que les récoltes d'une culture en mode réel
     *    restent en mode réel et inversement.
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
            estProjection = culture.estProjection,
            notes = notes
        )
        return recolteDao.insertRecolte(recolte)
    }
    
    // ============================================================
    // ÉCRITURE — modification
    // ============================================================
    
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
     * Bascule le mode d'une récolte (rarement utilisé).
     */
    suspend fun mettreAJourMode(recolteId: Long, estProjection: Boolean) {
        recolteDao.updateMode(recolteId, estProjection)
    }
    
    // ============================================================
    // ÉCRITURE — suppression
    // ============================================================
    
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
    
    suspend fun getPoidsTotalParLegumeParMode(estProjection: Boolean): List<LegumePoidsTotal> {
        return recolteDao.getPoidsTotalParLegumeParMode(estProjection)
    }
}
