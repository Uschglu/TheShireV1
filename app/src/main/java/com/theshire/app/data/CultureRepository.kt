package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les cultures actives.
 *
 * Point central de la feature "cultures liées aux stocks" :
 *  - Crée une culture quand on plante en pleine terre ou en urbain
 *  - Gère le mode réel (décrément du stock source : semis, jeune plant, graine)
 *  - Gère le mode projection (aucun impact stock)
 *  - Termine une culture (récolte ou retrait)
 *  - Nettoie en cascade quand une planche / un contenant est supprimé
 *
 * ⚠️ Les méthodes de CRÉATION sont faites pour être appelées depuis
 *    JardinRepository (pleine terre) et ContenantRepository (urbain),
 *    pas directement depuis l'UI.
 */
class CultureRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val cultureDao = AppDatabase.getDatabase(context).cultureDao()
    private val recolteDao = AppDatabase.getDatabase(context).recolteDao()
    private val jeunePlantDao = AppDatabase.getDatabase(context).jeunePlantDao()
    private val graineDao = AppDatabase.getDatabase(context).graineDao()
    
    // ============================================================
    // LECTURE
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
    
    suspend fun getCultureActiveDansCase(carreId: Long, caseNumero: Int): CultureEntity? {
        return cultureDao.getCultureActiveDansCase(carreId, caseNumero)
    }
    
    fun getCulturesActivesPourContenant(contenantId: Long): Flow<List<CultureEntity>> {
        return cultureDao.getCulturesActivesPourContenant(contenantId)
    }
    
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
    // CRÉATION D'UNE CULTURE
    // ============================================================
    
    /**
     * Crée une nouvelle culture. Méthode centrale appelée par
     * JardinRepository.planterEnPleineTerre() et
     * ContenantRepository.planterDansEmplacement().
     * 
     * Gère le mode réel :
     *  - Si mode PROJECTION : crée juste la culture (estProjection = true)
     *  - Si mode RÉEL :
     *      - Vérifie et décrémente le stock source (semis, jeune plant ou graine)
     *      - Si la source est introuvable ou insuffisante → erreur
     *      - Crée la culture avec estProjection = false
     * 
     * ⚠️ NE PAS appeler cette méthode depuis l'UI directement : passer
     *    toujours par JardinRepository ou ContenantRepository.
     * 
     * @param context Context (pour lire ModePreferences)
     * @param culture CultureEntity à créer (sans id, sans estProjection fixé)
     * @param sourceStockId ID de l'entité source (JeunePlantEntity.id ou GraineEntity.id)
     * @return ResultatCreationCulture
     */
    suspend fun creerCulture(
        context: Context,
        culture: CultureEntity,
        sourceStockId: Long? = null
    ): ResultatCreationCulture {
        
        val modeReel = ModePreferences.estModeReel(context)
        
        // ============================================================
        // MODE PROJECTION : création directe, aucun impact stock
        // ============================================================
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
        
        // ============================================================
        // MODE RÉEL : vérification et décrément du stock source
        // ============================================================
        
        // --- Cas 1 : aucune source (plant offert/trouvé) ---
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
        
        // --- Cas 2 : semis ou jeune plant ---
        if (culture.sourceStock == CultureEntity.SOURCE_SEMIS ||
            culture.sourceStock == CultureEntity.SOURCE_JEUNE_PLANT) {
            
            if (sourceStockId == null) {
                return ResultatCreationCulture.ErreurSourceIntrouvable(
                    source = culture.sourceStock
                )
            }
            
            val jeunePlant = jeunePlantDao.getJeunePlantParId(sourceStockId)
                ?: return ResultatCreationCulture.ErreurSourceIntrouvable(
                    source = culture.sourceStock
                )
            
            // Vérification : le jeune plant doit être actif
            // ⚠️ CORRECTION : le champ s'appelle `estActif` (pas `estActive`)
            if (!jeunePlant.estActif) {
                return ResultatCreationCulture.ErreurSourceInactive(
                    legumeNom = jeunePlant.legumeNom,
                    varieteNom = jeunePlant.varieteNom
                )
            }
            
            // Vérification : la quantité du jeune plant doit être >= à celle de la culture
            if (jeunePlant.quantite < culture.quantite) {
                return ResultatCreationCulture.ErreurStockInsuffisant(
                    source = culture.sourceStock,
                    disponible = jeunePlant.quantite,
                    demande = culture.quantite,
                    legumeNom = jeunePlant.legumeNom,
                    varieteNom = jeunePlant.varieteNom
                )
            }
            
            // Décrément (ou sortie complète du stock si quantité = 1)
            // ⚠️ CORRECTION : `estActif` (pas `estActive`)
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
        
        // --- Cas 3 : graine directe ---
        if (culture.sourceStock == CultureEntity.SOURCE_GRAINE) {
            
            if (sourceStockId == null) {
                return ResultatCreationCulture.ErreurSourceIntrouvable(
                    source = culture.sourceStock
                )
            }
            
            val graine = graineDao.getGraineParId(sourceStockId)
                ?: return ResultatCreationCulture.ErreurSourceIntrouvable(
                    source = culture.sourceStock
                )
            
            // Vérification : la graine doit être active
            // (Ici c'est bien `estActive` pour les graines)
            if (!graine.estActif) {
                return ResultatCreationCulture.ErreurSourceInactive(
                    legumeNom = graine.legumeNom,
                    varieteNom = graine.varieteNom
                )
            }
            
            // Vérification : quantité suffisante
            if (graine.quantite < culture.quantite) {
                return ResultatCreationCulture.ErreurStockInsuffisant(
                    source = culture.sourceStock,
                    disponible = graine.quantite,
                    demande = culture.quantite,
                    legumeNom = graine.legumeNom,
                    varieteNom = graine.varieteNom
                )
            }
            
            // Décrément
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
        
        // --- Cas par défaut : source inconnue ---
        return ResultatCreationCulture.ErreurSourceIntrouvable(
            source = culture.sourceStock
        )
    }
    
    // ============================================================
    // TERMINAISON D'UNE CULTURE
    // ============================================================
    
    /**
     * Termine une culture SANS récolte (plant mort, arraché, etc.).
     * Marque `estActive = false` sans créer de récolte.
     */
    suspend fun terminerSansRecolte(cultureId: Long) {
        cultureDao.terminerCulture(cultureId, null)
    }
    
    /**
     * Termine une culture AVEC récolte.
     * Crée une RecolteEntity + marque la culture comme terminée.
     * 
     * @param cultureId ID de la culture à terminer
     * @param poidsKg Poids récolté en kg
     * @param notes Notes optionnelles
     */
    suspend fun terminerAvecRecolte(
        cultureId: Long,
        poidsKg: Double,
        notes: String? = null
    ): Long {
        val culture = cultureDao.getCultureParId(cultureId)
            ?: return -1L
        
        val maintenant = System.currentTimeMillis()
        
        // Créer la récolte
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
        
        // Marquer la culture comme terminée
        cultureDao.terminerCulture(cultureId, maintenant)
        
        return recolteId
    }
    
    // ============================================================
    // MODIFICATION
    // ============================================================
    
    suspend fun mettreAJourNotes(cultureId: Long, notes: String?) {
        cultureDao.updateNotes(cultureId, notes)
    }
    
    suspend fun mettreAJourCulture(culture: CultureEntity) {
        cultureDao.updateCulture(culture)
    }
    
    // ============================================================
    // SUPPRESSION
    // ============================================================
    
    suspend fun supprimerCulture(culture: CultureEntity) {
        cultureDao.deleteCulture(culture)
    }
    
    suspend fun supprimerCultureParId(id: Long) {
        cultureDao.deleteCultureParId(id)
    }
    
    /**
     * Supprime toutes les cultures d'un carré.
     * Appelée quand on vide une planche entière.
     */
    suspend fun supprimerCulturesPourCarre(carreId: Long) {
        cultureDao.deleteCulturesPourCarre(carreId)
    }
    
    /**
     * Supprime toutes les cultures d'un contenant.
     * Appelée quand on supprime un contenant.
     */
    suspend fun supprimerCulturesPourContenant(contenantId: Long) {
        cultureDao.deleteCulturesPourContenant(contenantId)
    }
    
    /**
     * Supprime toutes les cultures d'une planche.
     * Appelée quand on supprime une planche.
     */
    suspend fun supprimerCulturesPourPlanche(plancheId: Long) {
        cultureDao.deleteCulturesPourPlanche(plancheId)
    }
}

/**
 * Résultat de la création d'une culture, selon le mode (projection / réel)
 * et la source du stock.
 */
sealed class ResultatCreationCulture {
    
    /**
     * Création réussie.
     * 
     * @param id ID de la culture créée
     * @param stockDecremente true si un stock a été décrémenté
     * @param modeReel true si on était en mode réel
     */
    data class Succes(
        val id: Long,
        val stockDecremente: Boolean,
        val modeReel: Boolean
    ) : ResultatCreationCulture()
    
    /**
     * La source demandée n'existe pas dans le stock (sachet/semis introuvable).
     */
    data class ErreurSourceIntrouvable(
        val source: String
    ) : ResultatCreationCulture()
    
    /**
     * La source existe mais n'est plus active (déjà plantée, sachet vide…).
     */
    data class ErreurSourceInactive(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationCulture()
    
    /**
     * La source existe mais le stock est insuffisant.
     */
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
