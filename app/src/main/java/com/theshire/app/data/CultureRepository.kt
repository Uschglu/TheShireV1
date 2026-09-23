package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les cultures actives.
 * 
 * ⚠️ NOUVEAU : distinction PROJECTION vs RÉEL.
 * Les méthodes `getCultureActiveDansCase` et `getCultureActiveDansEmplacement`
 * retournent la culture active **peu importe le mode**. Pour un filtrage
 * par mode (afficher seulement les cultures correspondant au mode actif),
 * utiliser les variantes `DansCasePourMode` / `DansEmplacementPourMode`.
 */
class CultureRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val cultureDao = AppDatabase.getDatabase(context).cultureDao()
    private val recolteDao = AppDatabase.getDatabase(context).recolteDao()
    private val jeunePlantDao = AppDatabase.getDatabase(context).jeunePlantDao()
    private val graineDao = AppDatabase.getDatabase(context).graineDao()
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    val toutesLesCultures: Flow<List<CultureEntity>> = cultureDao.getAllCultures()
    val culturesActives: Flow<List<CultureEntity>> = cultureDao.getCulturesActives()
    val culturesTerminees: Flow<List<CultureEntity>> = cultureDao.getCulturesTerminees()
    
    suspend fun getCultureParId(id: Long): CultureEntity? {
        return cultureDao.getCultureParId(id)
    }
    
    fun getCulturesActivesPourPlanche(plancheId: Long): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesPourPlanche(plancheId)
    }
    
    fun getCulturesActivesPourCarre(carreId: Long): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesPourCarre(carreId)
    }
    
    /**
     * Retourne la culture active dans une case, PEU IMPORTE le mode.
     * Utilisé pour les opérations internes (vider, terminer).
     */
    suspend fun getCultureActiveDansCase(carreId: Long, caseNumero: Int): CultureEntity? {
        return cultureDao.getCultureActiveDansCase(carreId, caseNumero)
    }
    
    fun getCulturesActivesPourContenant(contenantId: Long): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesPourContenant(contenantId)
    }
    
    /**
     * Retourne la culture active dans un emplacement, PEU IMPORTE le mode.
     * Utilisé pour les opérations internes (vider, terminer).
     */
    suspend fun getCultureActiveDansEmplacement(contenantId: Long, emplacementNumero: Int): CultureEntity? {
        return cultureDao.getCultureActiveDansEmplacement(contenantId, emplacementNumero)
    }
    
    fun getCulturesActivesParType(type: String): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesParType(type)
    }
    
    fun getCulturesPourLegume(legumeNom: String): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesPourLegume(legumeNom)
    }
    
    fun getCulturesActivesPourLegume(legumeNom: String): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesPourLegume(legumeNom)
    }
    
    fun getCulturesActivesFiltrees(modeReel: Boolean): Flow<List<CultureEntity>> {
        // Un mode réel → estProjection = false
        // Un mode projection → estProjection = true
        return cultureDao.getCulturesActivesFiltrees(!modeReel)
    }
    
    fun countCulturesActives(): Flow<Int> = cultureDao.countCulturesActives()
    
    fun countTotalCultures(): Flow<Int> = cultureDao.countTotalCultures()
    
    // ============================================================
    // LECTURE — filtrées par mode (NOUVEAU)
    // ============================================================
    
    /**
     * Retourne la culture active dans une case SI elle correspond au mode actif.
     * 
     * @param context Context (pour lire ModePreferences)
     * @param carreId ID du carré
     * @param caseNumero Numéro de case (1-9)
     * @return La culture active du bon mode, ou null si la case est vide
     *         ou si la culture existante est de l'autre mode.
     */
    suspend fun getCultureActiveDansCasePourMode(
        context: Context,
        carreId: Long,
        caseNumero: Int
    ): CultureEntity? {
        val culture = cultureDao.getCultureActiveDansCase(carreId, caseNumero) ?: return null
        val modeReel = ModePreferences.estModeReel(context)
        // Un mode réel → estProjection = false
        // Un mode projection → estProjection = true
        val estProjectionAttendue = !modeReel
        return if (culture.estProjection == estProjectionAttendue) culture else null
    }
    
    /**
     * Retourne la culture active dans un emplacement SI elle correspond au mode actif.
     */
    suspend fun getCultureActiveDansEmplacementPourMode(
        context: Context,
        contenantId: Long,
        emplacementNumero: Int
    ): CultureEntity? {
        val culture = cultureDao.getCultureActiveDansEmplacement(contenantId, emplacementNumero) ?: return null
        val modeReel = ModePreferences.estModeReel(context)
        val estProjectionAttendue = !modeReel
        return if (culture.estProjection == estProjectionAttendue) culture else null
    }
    
    /**
     * Retourne toutes les cultures actives d'un carré filtrées par le mode actif.
     * 
     * @return Map<caseNumero, CultureEntity> pour les cases occupées du bon mode
     */
    suspend fun getCulturesActivesDansCarrePourMode(
        context: Context,
        carreId: Long
    ): Map<Int, CultureEntity> {
        val cultures = cultureDao.getCulturesActivesPourCarre(carreId)
        // On ne peut pas collecter un Flow ici — c'est appelé depuis des contextes suspend
        // On utilise une méthode DAO synchrone
        return emptyMap() // sera remplacé par la version DAO
    }
    
    // ============================================================
    // CRÉATION D'UNE CULTURE
    // ============================================================
    
    suspend fun creerCulture(
        context: Context,
        culture: CultureEntity,
        sourceStockId: Long? = null
    ): ResultatCreationCulture {
        
        val modeReel = ModePreferences.estModeReel(context)
        
        // MODE PROJECTION
        if (!modeReel) {
            val cultureMarquee = culture.copy(
                estProjection = true,
                sourceStockId = sourceStockId
            )
            val id = cultureDao.insertCulture(cultureMarquee)
            return ResultatCreationCulture.Succes(
                id = id,
                stockDecremente = false,
                modeReel = false
            )
        }
        
        // MODE RÉEL — Cas 1 : aucune source
        if (culture.sourceStock == CultureEntity.SOURCE_AUCUNE) {
            val cultureMarquee = culture.copy(
                estProjection = false,
                sourceStockId = null
            )
            val id = cultureDao.insertCulture(cultureMarquee)
            return ResultatCreationCulture.Succes(
                id = id,
                stockDecremente = false,
                modeReel = true
            )
        }
        
        // MODE RÉEL — Cas 2 : semis ou jeune plant
        if (culture.sourceStock == CultureEntity.SOURCE_SEMIS ||
            culture.sourceStock == CultureEntity.SOURCE_JEUNE_PLANT) {
            
            if (sourceStockId == null) {
                return ResultatCreationCulture.ErreurSourceIntrouvable(source = culture.sourceStock)
            }
            
            val jeunePlant = jeunePlantDao.getJeunePlantParId(sourceStockId)
                ?: return ResultatCreationCulture.ErreurSourceIntrouvable(source = culture.sourceStock)
            
            if (!jeunePlant.estActif) {
                return ResultatCreationCulture.ErreurSourceInactive(
                    legumeNom = jeunePlant.legumeNom,
                    varieteNom = jeunePlant.varieteNom
                )
            }
            
            if (jeunePlant.quantite < culture.quantite) {
                return ResultatCreationCulture.ErreurStockInsuffisant(
                    source = culture.sourceStock,
                    disponible = jeunePlant.quantite,
                    demande = culture.quantite,
                    legumeNom = jeunePlant.legumeNom,
                    varieteNom = jeunePlant.varieteNom
                )
            }
            
            val nouvelleQuantite = jeunePlant.quantite - culture.quantite
            jeunePlantDao.updateJeunePlant(
                jeunePlant.copy(
                    quantite = nouvelleQuantite,
                    estActif = nouvelleQuantite > 0
                )
            )
            
            val cultureMarquee = culture.copy(
                estProjection = false,
                sourceStockId = sourceStockId
            )
            val id = cultureDao.insertCulture(cultureMarquee)
            return ResultatCreationCulture.Succes(
                id = id,
                stockDecremente = true,
                modeReel = true
            )
        }
        
        // MODE RÉEL — Cas 3 : graine directe
        if (culture.sourceStock == CultureEntity.SOURCE_GRAINE) {
            
            if (sourceStockId == null) {
                return ResultatCreationCulture.ErreurSourceIntrouvable(source = culture.sourceStock)
            }
            
            val graine = graineDao.getGraineParId(sourceStockId)
                ?: return ResultatCreationCulture.ErreurSourceIntrouvable(source = culture.sourceStock)
            
            if (!graine.estActif) {
                return ResultatCreationCulture.ErreurSourceInactive(
                    legumeNom = graine.legumeNom,
                    varieteNom = graine.varieteNom
                )
            }
            
            if (graine.quantite < culture.quantite) {
                return ResultatCreationCulture.ErreurStockInsuffisant(
                    source = culture.sourceStock,
                    disponible = graine.quantite,
                    demande = culture.quantite,
                    legumeNom = graine.legumeNom,
                    varieteNom = graine.varieteNom
                )
            }
            
            val nouvelleQuantite = graine.quantite - culture.quantite
            graineDao.updateGraine(
                graine.copy(
                    quantite = nouvelleQuantite,
                    estActif = nouvelleQuantite > 0
                )
            )
            
            val cultureMarquee = culture.copy(
                estProjection = false,
                sourceStockId = sourceStockId
            )
            val id = cultureDao.insertCulture(cultureMarquee)
            return ResultatCreationCulture.Succes(
                id = id,
                stockDecremente = true,
                modeReel = true
            )
        }
        
        return ResultatCreationCulture.ErreurSourceIntrouvable(source = culture.sourceStock)
    }
    
    // ============================================================
    // TERMINAISON
    // ============================================================
    
    suspend fun terminerSansRecolte(cultureId: Long) {
        cultureDao.terminerCulture(cultureId, null)
    }
    
    suspend fun terminerAvecRecolte(
        cultureId: Long,
        poidsKg: Double,
        notes: String? = null
    ): Long {
        val culture = cultureDao.getCultureParId(cultureId)
            ?: return -1L
        
        val maintenant = System.currentTimeMillis()
        
        val recolte = RecolteEntity(
            legumeNom = culture.legumeNom,
            varieteNom = culture.varieteNom,
            emoji = culture.emoji,
            poidsKg = poidsKg,
            dateRecolte = maintenant,
            cultureId = culture.id,
            notes = notes
        )
        val recolteId = recolteDao.insertRecolte(recolte)
        
        cultureDao.terminerCulture(cultureId, maintenant)
        
        return recolteId
    }
    
    // ============================================================
    // MODIFICATION / SUPPRESSION
    // ============================================================
    
    suspend fun mettreAJourNotes(cultureId: Long, notes: String?) {
        cultureDao.updateNotes(cultureId, notes)
    }
    
    suspend fun mettreAJourCulture(culture: CultureEntity) {
        cultureDao.updateCulture(culture)
    }
    
    suspend fun supprimerCulture(culture: CultureEntity) {
        cultureDao.deleteCulture(culture)
    }
    
    suspend fun supprimerCultureParId(id: Long) {
        cultureDao.deleteCultureParId(id)
    }
    
    suspend fun supprimerCulturesPourCarre(carreId: Long) {
        cultureDao.deleteCulturesPourCarre(carreId)
    }
    
    suspend fun supprimerCulturesPourContenant(contenantId: Long) {
        cultureDao.deleteCulturesPourContenant(contenantId)
    }
    
    suspend fun supprimerCulturesPourPlanche(plancheId: Long) {
        cultureDao.deleteCulturesPourPlanche(plancheId)
    }
}

/**
 * Résultat de la création d'une culture.
 */
sealed class ResultatCreationCulture {
    
    data class Succes(
        val id: Long,
        val stockDecremente: Boolean,
        val modeReel: Boolean
    ) : ResultatCreationCulture()
    
    data class ErreurSourceIntrouvable(
        val source: String
    ) : ResultatCreationCulture()
    
    data class ErreurSourceInactive(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationCulture()
    
    data class ErreurStockInsuffisant(
        val source: String,
        val disponible: Int,
        val demande: Int,
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationCulture() {
        val manquant: Int
            get() = (demande - disponible).coerceAtLeast(0)
    }
}
