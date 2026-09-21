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
     * 
     * Cette méthode reste utile pour un ajustement manuel rapide.
     */
    suspend fun changerStade(jeunePlantId: Long, nouveauStade: String) {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return
        jeunePlantDao.updateJeunePlant(plant.copy(stade = nouveauStade))
    }
    
    /**
     * Fait avancer un semis à l'étape suivante du cycle.
     * 
     * Effets :
     *  - Met à jour le champ `stade` à l'étape suivante
     *  - Remplit la date correspondante (dateLevee, dateRepiquage, …)
     *  - Ajoute une entrée dans l'historique
     *  - Si l'étape atteinte est "Planté" : estActif = false + datePlantation remplie
     * 
     * Ne fait rien si l'étape actuelle est déjà la dernière ("Planté")
     * ou si l'étape est inconnue.
     * 
     * @return La nouvelle étape (ou null si rien n'a changé).
     */
    suspend fun avancerEtape(jeunePlantId: Long): String? {
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return null
        val etapeSuivante = JeunePlantEtapes.etapeSuivante(plant.stade) ?: return null
        
        val maintenant = System.currentTimeMillis()
        val nouvelHistorique = ajouterAuHistorique(plant.historiqueEtapes, etapeSuivante, maintenant)
        
        // On prépare les nouvelles dates à remplir (une seule sera effective)
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
     * Utile pour corriger une erreur de manip.
     * 
     * ⚠️ On ne supprime pas l'entrée d'historique correspondante (on garde la trace
     * brute). L'historique peut donc contenir des "aller-retours".
     * 
     * @return La nouvelle étape (ou null si rien n'a changé).
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
     * 
     * Ex : depuis un menu déroulant, l'utilisateur choisit directement "Prêt à planter"
     * alors qu'il était à "Levée". Les dates intermédiaires sont remplies avec
     * la date du jour pour ne pas laisser de trous.
     * 
     * @return La nouvelle étape, ou null si l'étape cible est invalide.
     */
    suspend fun changerEtape(jeunePlantId: Long, etapeCible: String): String? {
        if (JeunePlantEtapes.indexDe(etapeCible) < 0) return null
        val plant = jeunePlantDao.getJeunePlantParId(jeunePlantId) ?: return null
        if (plant.stade == etapeCible) return etapeCible
        
        val maintenant = System.currentTimeMillis()
        val nouvelHistorique = ajouterAuHistorique(plant.historiqueEtapes, etapeCible, maintenant)
        
        // On remplit toutes les dates jusqu'à l'étape cible qui ne sont pas encore définies
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
     * 
     * Passage à l'étape "Planté", remplissage de datePlantation, et bascule en inactif.
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
     * Le ramène à l'étape précédente du cycle ("Endurci") pour qu'il reste
     * dans un état cohérent.
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
    
    // ============================================================
    // HELPERS PRIVÉS
    // ============================================================
    
    /**
     * Ajoute une entrée "Étape:timestamp|" à la fin de l'historique existant.
     * Format : "Semis:1234567890|Levée:1234600000|"
     */
    private fun ajouterAuHistorique(historique: String?, etape: String, timestamp: Long): String {
        val base = historique ?: ""
        return "$base$etape:$timestamp|"
    }
    
    /**
     * Structure interne : dates à remplir en fonction d'une étape.
     * Tous les champs null par défaut = "pas de nouvelle date à remplir".
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
     * Une seule date non-nulle (sauf cas spécial PLANTE qui remplit datePlantation).
     */
    private fun datePourEtape(etape: String, timestamp: Long): DatesPourEtape {
        return when (etape) {
            JeunePlantEtapes.SEMIS -> DatesPourEtape(dateSemis = timestamp)
            JeunePlantEtapes.LEVEE -> DatesPourEtape(dateLevee = timestamp)
            JeunePlantEtapes.REPIQUE -> DatesPourEtape(dateRepiquage = timestamp)
            JeunePlantEtapes.REMPOTE -> DatesPourEtape(dateRempotage = timestamp)
            JeunePlantEtapes.PRET_A_PLANTER -> DatesPourEtape() // pas de date dédiée
            JeunePlantEtapes.ENDURCI -> DatesPourEtape(dateEndurcissement = timestamp)
            JeunePlantEtapes.PLANTE -> DatesPourEtape(datePlantation = timestamp)
            else -> DatesPourEtape()
        }
    }
}
