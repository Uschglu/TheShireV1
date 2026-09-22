package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les jeunes plants.
 * 
 * Encapsule l'accès au JeunePlantDao et fournit une API métier claire.
 * 
 * Utilisé par :
 *  - EcranStocks (onglet "Plants") : inventaire des plants possédés
 *  - JardinScreen (onglet "Semis") : suivi du cycle de vie
 */
class JeunePlantRepository(context: Context) {
    
    private val jeunePlantDao = AppDatabase.getDatabase(context).jeunePlantDao()
    
    // ============================================================
    // LECTURE — Stocks > Plants
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
    // LECTURE — Jardin > Semis
    // ============================================================
    
    /**
     * Semis actifs (dans le cycle), triés par date de semis croissante.
     */
    val semisActifs: Flow<List<JeunePlantEntity>> = jeunePlantDao.getSemisActifs()
    
    /**
     * Semis actifs d'une étape donnée.
     */
    fun getSemisParEtape(stade: String): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getSemisParEtape(stade)
    }
    
    /**
     * Semis déjà plantés (fin de cycle) — historique V2.
     */
    fun getSemisPlantes(): Flow<List<JeunePlantEntity>> {
        return jeunePlantDao.getSemisPlantes(JeunePlantEtapes.PLANTE)
    }
    
    /**
     * Nombre de semis actuellement actifs (dans le cycle).
     */
    fun countSemisActifs(): Flow<Int> {
        return jeunePlantDao.countSemisActifs()
    }
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Ajoute un nouveau jeune plant.
     * Retourne l'ID généré.
     * 
     * ⚠️ Cette méthode ne touche PAS aux stocks de graines.
     *    Pour un ajout qui impacte les graines (mode réel),
     *    utiliser creerSemisAvecMode().
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
     * 
     * ⚠️ Pour un vrai changement d'étape du cycle, préférer avancerEtape() ou reculerEtape()
     * qui gèrent automatiquement les dates et l'historique.
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
    // MODE RÉEL — création de semis avec impact sur les stocks
    // ============================================================
    
    /**
     * Crée un semis en tenant compte du mode (projection / réel).
     * 
     * - Mode PROJECTION : crée simplement le semis (comportement actuel).
     * - Mode RÉEL : vérifie qu'un sachet de graines correspondant existe et
     *   qu'il contient assez de graines, puis décrémente le stock avant de
     *   créer le semis.
     * 
     * @param context     Contexte Android (pour lire ModePreferences + GraineRepository)
     * @param semis       Le JeunePlantEntity à créer (stade initial, dates, etc.)
     * @return Un ResultatCreationSemis indiquant le succès ou l'erreur.
     */
    suspend fun creerSemisAvecMode(
        context: Context,
        semis: JeunePlantEntity
    ): ResultatCreationSemis {
        
        val modeReel = ModePreferences.estModeReel(context)
        
        // === MODE PROJECTION : on crée le semis directement ===
        if (!modeReel) {
            val id = jeunePlantDao.insertJeunePlant(semis)
            return ResultatCreationSemis.Succes(id, grainesDecrementees = 0, modeReel = false)
        }
        
        // === MODE RÉEL : vérification + décrément graines ===
        val graineRepository = GraineRepository(context)
        val graine = graineRepository.getGraineExacte(
            legumeNom = semis.legumeNom,
            varieteNom = semis.varieteNom
        )
        
        // Cas 1 : aucun sachet exact trouvé
        if (graine == null) {
            return ResultatCreationSemis.ErreurSachetIntrouvable(
                legumeNom = semis.legumeNom,
                varieteNom = semis.varieteNom
            )
        }
        
        // Cas 2 : sachet trouvé mais pas assez de graines
        if (graine.quantite < semis.quantite) {
            return ResultatCreationSemis.ErreurStockInsuffisant(
                grainesDisponibles = graine.quantite,
                grainesDemandees = semis.quantite,
                legumeNom = semis.legumeNom,
                varieteNom = semis.varieteNom
            )
        }
        
        // Cas 3 : tout est bon → décrément + création
        graineRepository.decrementerQuantite(graine.id, semis.quantite)
        val id = jeunePlantDao.insertJeunePlant(semis)
        return ResultatCreationSemis.Succes(
            id = id,
            grainesDecrementees = semis.quantite,
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
    
    /**
     * Le semis a été créé avec succès.
     */
    data class Succes(
        val id: Long,
        val grainesDecrementees: Int,
        val modeReel: Boolean
    ) : ResultatCreationSemis()
    
    /**
     * Aucun sachet exact n'a été trouvé pour ce légume + variété.
     */
    data class ErreurSachetIntrouvable(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationSemis()
    
    /**
     * Le sachet a été trouvé mais pas assez de graines.
     */
    data class ErreurStockInsuffisant(
        val grainesDisponibles: Int,
        val grainesDemandees: Int,
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatCreationSemis() {
        /** Nombre de graines manquantes. */
        val grainesManquantes: Int
            get() = (grainesDemandees - grainesDisponibles).coerceAtLeast(0)
    }
}
