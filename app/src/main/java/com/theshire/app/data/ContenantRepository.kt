package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository pour les contenants urbains et leurs emplacements.
 * 
 * Gère :
 * - La création de contenants avec calcul automatique des emplacements
 * - L'installation de plantes dans les emplacements (AVEC création d'une
 *   CultureEntity et décrément du stock source en mode réel)
 * - Les associations de culture au sein d'un même contenant
 * - La suppression en cascade
 * 
 * ⚠️ TOURS EMPILABLES (v18) :
 *    Les contenants de type "tour" possèdent des ÉTAGES (EtageEntity).
 *    Chaque étage est indépendant (peut avoir sa propre culture).
 *    Les étages sont créés automatiquement à la création de la tour
 *    (nombreEtages) et supprimés en cascade avec le contenant.
 */
class ContenantRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val contenantDao = AppDatabase.getDatabase(context).contenantDao()
    private val etageDao = AppDatabase.getDatabase(context).etageDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    private val rappelCulturelRepository = RappelCulturelRepository(context)
    private val cultureRepository = CultureRepository(context)
    
    companion object {
        private val mutexCreation = Mutex()
        
        /** Type de contenant "tour" (pour accès rapide). */
        const val TYPE_TOUR = "tour"
    }
    
    // ============================================================
    // CONTENANTS
    // ============================================================
    
    val contenants: Flow<List<ContenantEntity>> = contenantDao.getAllContenants()
    
    suspend fun getContenantParId(id: Long): ContenantEntity? {
        return contenantDao.getContenantParId(id)
    }
    
    suspend fun getTousContenants(): List<ContenantEntity> {
        return contenantDao.getAllContenantsSync()
    }
    
    /**
     * Crée un nouveau contenant.
     * 
     * ⚠️ Si le type est "tour", les N étages sont créés automatiquement.
     * 
     * @param nombreEtages Utilisé UNIQUEMENT si type == "tour" (ignoré sinon)
     */
    suspend fun creerContenant(
        nom: String,
        type: String,
        emoji: String,
        dimension1: Int,
        dimension2: Int = 0,
        dimension3: Int = 0,
        nombreEtages: Int = 1,
        milieu: String = "Balcon",
        notes: String = ""
    ): Long {
        mutexCreation.withLock {
            val contenant = ContenantEntity(
                nom = nom,
                type = type,
                emoji = emoji,
                dimension1 = dimension1,
                dimension2 = dimension2,
                dimension3 = dimension3,
                nombreEtages = nombreEtages,
                milieu = milieu,
                notes = notes
            )
            val contenantId = contenantDao.insertContenant(contenant)
            
            // ⭐ Si c'est une tour, créer automatiquement les N étages
            if (type == TYPE_TOUR && nombreEtages > 0) {
                val etages = (1..nombreEtages).map { numero ->
                    EtageEntity(
                        contenantId = contenantId,
                        numero = numero,
                        forme = EtageEntity.FORME_ROND,
                        nombreEmplacements = 0,  // Sera défini à la 1ère plantation
                        notes = ""
                    )
                }
                try {
                    etageDao.insertEtages(etages)
                } catch (e: Exception) {
                    // En cas d'échec de création des étages, on supprime le contenant
                    // pour ne pas laisser un état incohérent.
                    contenantDao.deleteContenantParId(contenantId)
                    throw e
                }
            }
            
            return contenantId
        }
    }
    
    suspend fun modifierNom(contenantId: Long, nouveauNom: String) {
        val contenant = contenantDao.getContenantParId(contenantId) ?: return
        contenantDao.updateContenant(contenant.copy(nom = nouveauNom))
    }
    
    suspend fun modifierDimensions(
        contenantId: Long,
        dimension1: Int,
        dimension2: Int = 0,
        dimension3: Int = 0,
        nombreEtages: Int = 1
    ) {
        val contenant = contenantDao.getContenantParId(contenantId) ?: return
        contenantDao.updateContenant(
            contenant.copy(
                dimension1 = dimension1,
                dimension2 = dimension2,
                dimension3 = dimension3,
                nombreEtages = nombreEtages
            )
        )
    }
    
    suspend fun modifierNotes(contenantId: Long, nouvellesNotes: String) {
        val contenant = contenantDao.getContenantParId(contenantId) ?: return
        contenantDao.updateContenant(contenant.copy(notes = nouvellesNotes))
    }
    
    suspend fun supprimerContenant(contenant: ContenantEntity) {
        // 1. Supprimer les rappels culturaux du contenant
        try {
            rappelCulturelRepository.supprimerRappelsPourContenant(contenant.id)
        } catch (e: Exception) {
            // Silencieux : les rappels peuvent ne pas exister
        }
        
        // 2. Terminer les cultures liées au contenant
        try {
            cultureRepository.supprimerCulturesPourContenant(contenant.id)
        } catch (e: Exception) {
            // Silencieux
        }
        
        // 3. Supprimer explicitement les étages
        // (le CASCADE le ferait, mais on est explicites pour la clarté)
        try {
            etageDao.deleteEtagesPourContenant(contenant.id)
        } catch (e: Exception) {
            // Silencieux
        }
        
        // 4. Supprimer le contenant (CASCADE sur emplacements_contenants)
        contenantDao.deleteContenant(contenant)
    }
    
    // ============================================================
    // ÉTAGES (TOURS EMPILABLES)
    // ============================================================
    
    /**
     * Retourne true si le contenant est une tour (possède des étages).
     */
    fun estTour(contenant: ContenantEntity): Boolean {
        return contenant.type == TYPE_TOUR
    }
    
    /**
     * Récupère les étages d'un contenant (flow réactif).
     * Retourne une liste vide si le contenant n'est pas une tour.
     */
    fun getEtagesPourContenant(contenantId: Long): Flow<List<EtageEntity>> {
        return etageDao.getEtagesPourContenant(contenantId)
    }
    
    /**
     * Version synchrone pour usage ponctuel.
     */
    suspend fun getEtagesSync(contenantId: Long): List<EtageEntity> {
        return etageDao.getEtagesPourContenantSync(contenantId)
    }
    
    /**
     * Récupère un étage par son ID.
     */
    suspend fun getEtageParId(id: Long): EtageEntity? {
        return etageDao.getEtageParId(id)
    }
    
    /**
     * Récupère un étage précis d'une tour par son numéro.
     */
    suspend fun getEtageParNumero(contenantId: Long, numero: Int): EtageEntity? {
        return etageDao.getEtageParNumero(contenantId, numero)
    }
    
    /**
     * Compte le nombre d'étages d'un contenant.
     */
    suspend fun countEtages(contenantId: Long): Int {
        return etageDao.countEtagesPourContenant(contenantId)
    }
    
    /**
     * Compte le nombre d'étages déjà initialisés (au moins une plantation).
     */
    suspend fun countEtagesInitialises(contenantId: Long): Int {
        return etageDao.countEtagesInitialises(contenantId)
    }
    
    /**
     * Définit le nombre d'emplacements d'un étage (à la 1ère plantation).
     * Une fois défini, ce nombre ne change plus automatiquement.
     */
    suspend fun definirNombreEmplacementsEtage(etageId: Long, nombre: Int) {
        etageDao.definirNombreEmplacements(etageId, nombre)
    }
    
    /**
     * Modifie les notes d'un étage.
     */
    suspend fun modifierNotesEtage(etageId: Long, nouvellesNotes: String) {
        etageDao.updateNotes(etageId, nouvellesNotes)
    }
    
    /**
     * Met à jour un étage (usage avancé).
     */
    suspend fun mettreAJourEtage(etage: EtageEntity) {
        etageDao.updateEtage(etage)
    }
    
    // ============================================================
    // EMPLACEMENTS
    // ============================================================
    
    fun getEmplacementsPourContenant(contenantId: Long): Flow<List<EmplacementContenantEntity>> {
        return contenantDao.getEmplacementsPourContenant(contenantId)
    }
    
    suspend fun getEmplacementsSync(contenantId: Long): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsPourContenantSync(contenantId)
    }
    
    suspend fun countEmplacements(contenantId: Long): Int {
        return contenantDao.countEmplacementsPourContenant(contenantId)
    }
    
    suspend fun countEmplacementsOccupes(contenantId: Long): Int {
        return contenantDao.countEmplacementsOccupes(contenantId)
    }
    
    /**
     * Plante une culture dans un emplacement d'un contenant.
     * 
     * ⚠️ NOTE v18 : cette méthode fonctionne sur le CONTENANT global.
     *    Le support multi-étages (planter sur un étage précis d'une tour)
     *    sera ajouté dans un prochain sous-lot, avec l'UI correspondante.
     *    Pour l'instant, une tour est traitée comme un contenant unique.
     * 
     * @param context Context (nécessaire pour lire ModePreferences)
     * @param contenant Le contenant concerné
     * @param numeroEmplacement Numéro de l'emplacement
     * @param legumeNom Nom complet (ex : "Tomate (Marmande)")
     * @param legume L'entité légume (pour calcul d'emplacements)
     * @param varieteNom Variété seule (ex : "Marmande")
     * @param emoji Emoji du légume
     * @param sourceStock Source ("Semis" / "JeunePlant" / "Graine" / "Aucune")
     * @param sourceStockId ID de l'entité source
     * @return ResultatPlantation
     */
    suspend fun planterDansEmplacement(
        context: Context,
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String,
        legume: LegumeEntity,
        varieteNom: String? = null,
        emoji: String = "🌱",
        sourceStock: String = CultureEntity.SOURCE_AUCUNE,
        sourceStockId: Long? = null
    ): ResultatPlantation {
        val dateActuelle = System.currentTimeMillis()
        
        // ===== 1. Créer la CultureEntity (gère le mode réel / projection) =====
        val culture = CultureEntity(
            typeEmplacement = CultureEntity.TYPE_URBAIN,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement,
            legumeNom = legumeNom,
            varieteNom = varieteNom,
            emoji = emoji,
            quantite = 1, // V1 : 1 plant par emplacement
            sourceStock = sourceStock,
            estProjection = true,
            datePlantation = dateActuelle
        )
        
        val resultatCulture = cultureRepository.creerCulture(
            context = context,
            culture = culture,
            sourceStockId = sourceStockId
        )
        
        if (resultatCulture !is ResultatCreationCulture.Succes) {
            return when (resultatCulture) {
                is ResultatCreationCulture.ErreurSourceIntrouvable ->
                    ResultatPlantation.ErreurSourceIntrouvable(resultatCulture.source)
                is ResultatCreationCulture.ErreurSourceInactive ->
                    ResultatPlantation.ErreurSourceInactive(
                        resultatCulture.legumeNom,
                        resultatCulture.varieteNom
                    )
                is ResultatCreationCulture.ErreurStockInsuffisant ->
                    ResultatPlantation.ErreurStockInsuffisant(
                        resultatCulture.disponible,
                        resultatCulture.demande,
                        resultatCulture.legumeNom,
                        resultatCulture.varieteNom
                    )
                else -> ResultatPlantation.ErreurSourceIntrouvable("Inconnu")
            }
        }
        
        // ===== 2. Mettre à jour l'emplacement (occupation) =====
        val emplacements = contenantDao.getEmplacementsPourContenantSync(contenant.id)
        
        if (emplacements.isEmpty()) {
            // Premier plant → générer tous les emplacements d'un coup
            val nombreEmplacements = CalculEmplacements.calculerNombreEmplacements(
                contenant = contenant,
                legume = legume
            )
            
            val nouveauxEmplacements = (1..nombreEmplacements).map { numero ->
                EmplacementContenantEntity(
                    contenantId = contenant.id,
                    numero = numero,
                    legumeNom = if (numero == numeroEmplacement) legumeNom else null,
                    datePlantation = if (numero == numeroEmplacement) dateActuelle else null
                )
            }
            contenantDao.insertEmplacements(nouveauxEmplacements)
        } else {
            val emplacement = emplacements.find { it.numero == numeroEmplacement }
            if (emplacement != null) {
                contenantDao.updateEmplacement(
                    emplacement.copy(
                        legumeNom = legumeNom,
                        datePlantation = dateActuelle
                    )
                )
            }
        }
        
        // ===== 3. Générer les rappels culturaux =====
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = null,
            caseNumero = null,
            plancheId = null,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement
        )
        
        return ResultatPlantation.Succes
    }
    
    /**
     * Vide un emplacement (retrait sans récolte).
     * 
     * ⚠️ Termine aussi la CultureEntity associée.
     */
    suspend fun viderEmplacement(emplacementId: Long) {
        val emplacement = contenantDao.getEmplacementParId(emplacementId) ?: return
        
        // 1. Terminer la culture active dans cet emplacement (si elle existe)
        try {
            val cultureActive = cultureRepository.getCultureActiveDansEmplacement(
                contenantId = emplacement.contenantId,
                emplacementNumero = emplacement.numero
            )
            if (cultureActive != null) {
                cultureRepository.terminerSansRecolte(cultureActive.id)
            }
        } catch (e: Exception) {
            // Silencieux
        }
        
        // 2. Supprimer les rappels culturaux
        try {
            rappelCulturelRepository.supprimerRappelsPourEmplacement(
                contenantId = emplacement.contenantId,
                emplacementNumero = emplacement.numero
            )
        } catch (e: Exception) {
            throw e
        }
        
        // 3. Vider l'emplacement
        try {
            contenantDao.updateEmplacement(
                emplacement.copy(
                    legumeNom = null,
                    datePlantation = null
                )
            )
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun ajouterEmplacements(contenantId: Long, nombre: Int) {
        val existants = contenantDao.getEmplacementsPourContenantSync(contenantId)
        val dernierNumero = existants.maxOfOrNull { it.numero } ?: 0
        
        val nouveaux = (1..nombre).map { i ->
            EmplacementContenantEntity(
                contenantId = contenantId,
                numero = dernierNumero + i
            )
        }
        contenantDao.insertEmplacements(nouveaux)
    }
    
    suspend fun modifierNotesEmplacement(emplacementId: Long, nouvellesNotes: String) {
        val emplacement = contenantDao.getEmplacementParId(emplacementId) ?: return
        contenantDao.updateEmplacement(emplacement.copy(notes = nouvellesNotes))
    }
    
    // ============================================================
    // VALIDATION DES ASSOCIATIONS
    // ============================================================
    
    suspend fun verifierAssociationsContenant(
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String
    ): List<Pair<String, String>> {
        val resultats = mutableListOf<Pair<String, String>>()
        
        val emplacements = contenantDao.getEmplacementsPourContenantSync(contenant.id)
        
        val nomBase = if (legumeNom.contains("(")) legumeNom.substringBefore("(").trim() else legumeNom
        val legume = legumeDao.getLegumeByNom(nomBase) ?: return emptyList()
        
        emplacements.forEach { emp ->
            if (emp.numero != numeroEmplacement && emp.legumeNom != null) {
                val base2 = if (emp.legumeNom!!.contains("(")) emp.legumeNom.substringBefore("(").trim() else emp.legumeNom
                when {
                    legume.bonnesAssociations.contains(base2, true) -> {
                        resultats.add(Pair(emp.legumeNom!!, "bonne"))
                    }
                    legume.mauvaisesAssociations.contains(base2, true) -> {
                        resultats.add(Pair(emp.legumeNom!!, "mauvaise"))
                    }
                }
            }
        }
        
        return resultats
    }
    
    // ============================================================
    // STATISTIQUES
    // ============================================================
    
    suspend fun countContenants(): Int {
        return contenantDao.countContenants()
    }
    
    suspend fun countAllEmplacements(): Int {
        return contenantDao.countAllEmplacements()
    }
    
    suspend fun countAllEmplacementsOccupes(): Int {
        return contenantDao.countAllEmplacementsOccupes()
    }
    
    suspend fun getAllEmplacementsOccupes(): List<EmplacementContenantEntity> {
        return contenantDao.getAllEmplacementsOccupes()
    }
    
    suspend fun getEmplacementsPourLegume(legumeNom: String): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsPourLegume(legumeNom)
    }
}
