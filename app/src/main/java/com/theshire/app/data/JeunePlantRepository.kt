package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les jeunes plants et semis.
 * 
 * Encapsule l'accès au JeunePlantDao et fournit une API métier claire.
 * 
 * Utilisé par :
 *  - EcranStocks (onglet "Plants") : inventaire global (2 catégories, tous modes)
 *  - JardinScreen (onglet "Semis") : catégorie "Semis", filtré par mode
 */
class JeunePlantRepository(context: Context) {
    
    private val jeunePlantDao = AppDatabase.getDatabase(context).jeunePlantDao()
    
    // ============================================================
    // LECTURE — Stocks > Plants (2 catégories, tous modes)
    // ============================================================
    
    /**
     * Tous les jeunes plants et semis actifs (non encore plantés),
     * toutes catégories et tous modes confondus.
     * Utilisé par l'onglet Plants de Stocks (inventaire global).
     */
    val jeunesPlantsActifs: Flow<List<JeunePlantEntity>> = jeunePlantDao.getJeunesPlantsActifs()
    
    /**
     * Tous les jeunes plants et semis (y compris inactifs).
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
     */
    fun getJeunesPlantsParStade(stade: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getJeunesPlantsParStade(stade)
    }
    
    /**
     * Compte le nombre de jeunes plants actifs (toutes catégories).
     */
    fun countJeunesPlantsActifs(): Flow<Int> {
        return jeunePlantDao.countJeunesPlantsActifs()
    }
    
    // ============================================================
    // LECTURE — par catégorie
    // ============================================================
    
    /**
     * Tous les éléments actifs d'une catégorie donnée.
     * @param categorie "Semis" ou "JeunePlant"
     */
    fun getActifsParCategorie(categorie: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getActifsParCategorie(categorie)
    }
    
    /**
     * Tous les éléments d'une catégorie (actifs + inactifs).
     */
    fun getTousParCategorie(categorie: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getTousParCategorie(categorie)
    }
    
    // ============================================================
    // LECTURE — Jardin > Semis (catégorie Semis uniquement)
    // ============================================================
    
    /**
     * Tous les semis actifs (catégorie = Semis), tous modes confondus.
     */
    val semisActifs: Flow<List<JeunePlantEntity>> =
        jeunePlantDao.getSemisActifsParCategorie(JeunePlantEntity.CATEGORIE_SEMIS)
    
    /**
     * Semis actifs d'une étape donnée (catégorie = Semis), tous modes confondus.
     */
    fun getSemisParEtape(stade: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getSemisParEtapeEtCategorie(
            JeunePlantEntity.CATEGORIE_SEMIS,
            stade
        )
    }
    
    /**
     * Semis déjà plantés (fin de cycle) — historique V2.
     * (toutes catégories confondues pour l'instant)
     */
    fun getSemisPlantes(): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getSemisPlantes(JeunePlantEtapes.PLANTE)
    }
    
    /**
     * Nombre de semis actuellement actifs (catégorie = Semis, tous modes).
     */
    fun countSemisActifs(): Flow<Int> {
        return jeunePlantDao.countSemisActifsParCategorie(JeunePlantEntity.CATEGORIE_SEMIS)
    }
    
    // ============================================================
    // LECTURE — Jardin > Semis (catégorie Semis + filtre mode)
    // ============================================================
    
    /**
     * Semis actifs filtrés par mode (catégorie = Semis uniquement).
     * 
     * @param modeReel true = uniquement les semis créés en mode réel
     *                 false = uniquement les semis créés en mode projection
     */
    fun getSemisActifsFiltres(modeReel: Boolean): Flow<List<JeunePlantEntity>> {
        // Un semis "réel" a estProjection = false
        // Un semis "projection" a estProjection = true
        val estProjection = !modeReel
        return jeunePlantDao.getSemisActifsFiltres(
            estProjection,
            JeunePlantEntity.CATEGORIE_SEMIS
        )
    }
    
    /**
     * Compte les semis actifs filtrés par mode (catégorie = Semis uniquement).
     */
    fun countSemisActifsFiltres(modeReel: Boolean): Flow<Int> {
        return jeunePlantDao.countSemisActifsFiltres(
            !modeReel,
            JeunePlantEntity.CATEGORIE_SEMIS
        )
    }
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Ajoute un nouveau jeune plant SANS toucher aux stocks.
     * 
     * ⚠️ Cette méthode n'est utilisée que pour des cas spécifiques
     *    (ex : restauration). Pour un ajout normal, utiliser
     *    creerSemisAvecMode() qui gère la logique du mode.
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
    // OPÉRATIONS MÉTIER — cycle de vie
    // ============================================================
    
    /**
     * Change le stade d'un jeune plant (sans toucher à l'historique ni aux dates).
     */
    suspend fun changerStade(jeunePlantId: Long, nouveauStade: String) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        jeunePlantDao.updateJeunePlant(plant.copy(stade = nouveauStade))
    }
    
    /**
     * Fait avancer un semis à l'étape suivante du cycle.
     */
    suspend fun avancerEtape(jeunePlantId: Long): String? {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return null
        val etapeSuivante = JeunePlantEtapes.etapeSuivante(plant.stade) ?: return null
        
        val maintenant = System.currentTimeMillis()
        val nouvelHistorique = ajouterAuHistorique(plant.historiqueEtapes, etapeSuivante, maintenant)
        val misesAJour = datePourEtape(etapeSuivante, maintenant)
        
        val nouveauPlant = plant.copy(
            stade = etapeSuivante,
            historiqueEtapes = nouvelHistorique,
            dateSemis = misesAJour.dateSemis ?: plant.dateSemis,
            dateLevee = misesAJour.dateLevee ?: plant.dateLevee,
            dateRepiquage = misesAJour.dateRepiquage ?: plant.dateRepiquage,
            dateRempotage = misesAJour.dateRempotage ?: plant.dateRempotage,
            dateEndurcissement = misesAJour.dateEndurcissement ?: plant.dateEndurcissement,
            datePlantation = misesAJour.datePlantation ?: plant.datePlantation,
            estActif = !JeunePlantEtapes.estEtapeFinale(etapeSuivante)
        )
        jeunePlantDao.updateJeunePlant(nouveauPlant)
        return etapeSuivante
    }
    
    /**
     * Fait reculer un semis à l'étape précédente du cycle.
     */
    suspend fun reculerEtape(jeunePlantId: Long): String? {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return null
        val etapePrecedente = JeunePlantEtapes.etapePrecedente(plant.stade) ?: return null
        
        val nouveauPlant = plant.copy(
            stade = etapePrecedente,
            estActif = !JeunePlantEtapes.estEtapeFinale(etapePrecedente)
        )
        jeunePlantDao.updateJeunePlant(nouveauPlant)
        return etapePrecedente
    }
    
    /**
     * Change le stade d'un semis vers une étape précise du cycle.
     */
    suspend fun changerEtape(jeunePlantId: Long, etapeCible: String): String? {
        if (JeunePlantEtapes.indexDe(etapeCible) < 0) return null
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return null
        if (plant.stade == etapeCible) return etapeCible
        
        val maintenant = System.currentTimeMillis()
        val nouvelHistorique = ajouterAuHistorique(plant.historiqueEtapes, etapeCible, maintenant)
        val idxCible = JeunePlantEtapes.indexDe(etapeCible)
        
        val nouveauPlant = plant.copy(
            stade = etapeCible,
            historiqueEtapes = nouvelHistorique,
            dateSemis = plant.dateSemis ?: if (idxCible >= 0) maintenant else null,
            dateLevee = plant.dateLevee ?: if (idxCible >= 1) maintenant else null,
            dateRepiquage = plant.dateRepiquage ?: if (idxCible >= 2) maintenant else null,
            dateRempotage = plant.dateRempotage ?: if (idxCible >= 3) maintenant else null,
            dateEndurcissement = plant.dateEndurcissement ?: if (idxCible >= 5) maintenant else null,
            datePlantation = plant.datePlantation ?: if (idxCible >= 6) maintenant else null,
            estActif = !JeunePlantEtapes.estEtapeFinale(etapeCible)
        )
        jeunePlantDao.updateJeunePlant(nouveauPlant)
        return etapeCible
    }
    
    /**
     * Marque un jeune plant comme planté (fin de cycle).
     */
    suspend fun marquerPlante(jeunePlantId: Long) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        val maintenant = System.currentTimeMillis()
        val nouvelHistorique = ajouterAuHistorique(
            plant.historiqueEtapes,
            JeunePlantEtapes.PLANTE,
            maintenant
        )
        jeunePlantDao.updateJeunePlant(
            plant.copy(
                stade = JeunePlantEtapes.PLANTE,
                datePlantation = plant.datePlantation ?: maintenant,
                historiqueEtapes = nouvelHistorique,
                estActif = false
            )
        )
    }
    
    /**
     * Réactive un jeune plant précédemment marqué planté.
     */
    suspend fun reactiver(jeunePlantId: Long) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        val etapeRetour = JeunePlantEtapes.etapePrecedente(JeunePlantEtapes.PLANTE)
            ?: JeunePlantEtapes.ENDURCI
        jeunePlantDao.updateJeunePlant(
            plant.copy(
                stade = etapeRetour,
                estActif = true
            )
        )
    }
    
    /**
     * Décrémente la quantité.
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
    
    // ============================================================
    // PROMOTION — Semis → Jeune Plant
    // ============================================================
    
    /**
     * Promeut un semis en jeune plant.
     * 
     * Change simplement la catégorie ("Semis" → "JeunePlant") sans toucher
     * au stade, aux dates, à l'historique, ni au mode (estProjection est hérité).
     * 
     * Conditions :
     *  - Le stade doit être >= REMPOTE (index 3). Sinon la promotion est refusée.
     *  - Le semis doit exister et être actif.
     *  - Ne peut pas être appelée sur un élément déjà en catégorie "JeunePlant".
     * 
     * @return ResultatPromotion indiquant le succès ou l'erreur.
     */
    suspend fun promouvoirEnJeunePlant(jeunePlantId: Long): ResultatPromotion {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId)
            ?: return ResultatPromotion.Introuvable
        
        // Déjà promu ?
        if (plant.categorie == JeunePlantEntity.CATEGORIE_JEUNE_PLANT) {
            return ResultatPromotion.DejaJeunePlant
        }
        
        // Vérification du stade minimum
        val idxStade = JeunePlantEtapes.indexDe(plant.stade)
        val idxRempote = JeunePlantEtapes.indexDe(JeunePlantEtapes.REMPOTE)
        
        if (idxStade < idxRempote) {
            return ResultatPromotion.StadeTropPrecoce(
                stadeActuel = plant.stade,
                stadeRequis = JeunePlantEtapes.REMPOTE
            )
        }
        
        // Promotion : on change juste la catégorie
        val promu = plant.copy(categorie = JeunePlantEntity.CATEGORIE_JEUNE_PLANT)
        jeunePlantDao.updateJeunePlant(promu)
        
        return ResultatPromotion.Succes(promu)
    }
    
    // ============================================================
    // MODE RÉEL — création de semis avec impact sur les stocks
    // ============================================================
    
    /**
     * Crée un semis en tenant compte du mode (projection / réel).
     * 
     * Le champ estProjection est automatiquement renseigné :
     *  - Mode projection → estProjection = true
     *  - Mode réel       → estProjection = false
     * 
     * La catégorie est forcée à "Semis" (un nouveau semis est toujours un semis).
     * 
     * - Mode PROJECTION : crée simplement le semis.
     * - Mode RÉEL : vérifie qu'un sachet correspondant existe et qu'il
     *   contient assez de graines, puis décrémente le stock.
     */
    suspend fun creerSemisAvecMode(
        context: Context,
        semis: JeunePlantEntity
    ): ResultatCreationSemis {
        
        val modeReel = ModePreferences.estModeReel(context)
        
        // On force la catégorie "Semis" (sécurité)
        val semisBase = semis.copy(categorie = JeunePlantEntity.CATEGORIE_SEMIS)
        
        // === MODE PROJECTION : création directe, tag estProjection = true ===
        if (!modeReel) {
            val semisMarque = semisBase.copy(estProjection = true)
            val id = jeunePlantDao.insertJeunePlant(semisMarque)
            return ResultatCreationSemis.Succes(id, grainesDecrementees = 0, modeReel = false)
        }
        
        // === MODE RÉEL : vérification + décrément graines ===
        val graineRepository = GraineRepository(context)
        val graine = graineRepository.getGraineExacte(
            legumeNom = semisBase.legumeNom,
            varieteNom = semisBase.varieteNom
        )
        
        // Cas 1 : aucun sachet exact trouvé
        if (graine == null) {
            return ResultatCreationSemis.ErreurSachetIntrouvable(
                legumeNom = semisBase.legumeNom,
                varieteNom = semisBase.varieteNom
            )
        }
        
        // Cas 2 : sachet trouvé mais pas assez de graines
        if (graine.quantite < semisBase.quantite) {
            return ResultatCreationSemis.ErreurStockInsuffisant(
                grainesDisponibles = graine.quantite,
                grainesDemandees = semisBase.quantite,
                legumeNom = semisBase.legumeNom,
                varieteNom = semisBase.varieteNom
            )
        }
        
        // Cas 3 : tout est bon → décrément + création avec tag estProjection = false
        graineRepository.decrementerQuantite(graine.id, semisBase.quantite)
        val semisMarque = semisBase.copy(estProjection = false)
        val id = jeunePlantDao.insertJeunePlant(semisMarque)
        return ResultatCreationSemis.Succes(
            id = id,
            grainesDecrementees = semisBase.quantite,
            modeReel = true
        )
    }
    
    // ============================================================
    // HELPERS PRIVÉS
    // ============================================================
    
    /**
     * Ajoute une entrée "Étape:timestamp|" à la fin de l'historique existant.
     */
    private fun ajouterAuHistorique(historique: String?, etape: String, timestamp: Long): String {
        val base = historique ?: ""
        return "$base$etape:$timestamp|"
    }
    
    /**
     * Structure interne : dates à remplir en fonction d'une étape.
     */
    private data class DatesPourEtape(
        val dateSemis: Long? = null,
        val dateLevee: Long? = null,
        val dateRepiquage: Long? = null,
        val dateRempotage: Long? = null,
        val dateEndurcissement: Long? = null,
        val datePlantation: Long? = null
    )
    
    /**
     * Retourne la date à remplir quand on atteint une étape donnée.
     */
    private fun datePourEtape(etape: String, timestamp: Long): DatesPourEtape {
        return when (etape) {
            JeunePlantEtapes.SEMIS -> DatesPourEtape(dateSemis = timestamp)
            JeunePlantEtapes.LEVEE -> DatesPourEtape(dateLevee = timestamp)
            JeunePlantEtapes.REPIQUE -> DatesPourEtape(dateRepiquage = timestamp)
            JeunePlantEtapes.REMPOTE -> DatesPourEtape(dateRempotage = timestamp)
            JeunePlantEtapes.PRET_A_PLANTER -> DatesPourEtape()
            JeunePlantEtapes.ENDURCI -> DatesPourEtape(dateEndurcissement = timestamp)
            JeunePlantEtapes.PLANTE -> DatesPourEtape(datePlantation = timestamp)
            else -> DatesPourEtape()
        }
    }
}

/**
 * Résultat de la création d'un semis, selon le mode (projection / réel).
 */
sealed class ResultatCreationSemis {
    
    data class Succes(
        val id: Long,
        val grainesDecrementees: Int,
        val modeReel: Boolean
    ) : ResultatCreationSemis()
    
    data class ErreurSachetIntrouvable(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationSemis()
    
    data class ErreurStockInsuffisant(
        val grainesDisponibles: Int,
        val grainesDemandees: Int,
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationSemis() {
        val grainesManquantes: Int
            get() = (grainesDemandees - grainesDisponibles).coerceAtLeast(0)
    }
}

/**
 * Résultat de la promotion d'un semis en jeune plant.
 */
sealed class ResultatPromotion {
    
    /** Promotion réussie : renvoie l'entité mise à jour. */
    data class Succes(val jeunePlant: JeunePlantEntity) : ResultatPromotion()
    
    /** L'élément n'existe plus (supprimé entre-temps). */
    object Introuvable : ResultatPromotion()
    
    /** L'élément est déjà un jeune plant. */
    object DejaJeunePlant : ResultatPromotion()
    
    /** Le stade actuel est trop précoce (avant Rempoté). */
    data class StadeTropPrecoce(
        val stadeActuel: String,
        val stadeRequis: String
    ) : ResultatPromotion()
}
