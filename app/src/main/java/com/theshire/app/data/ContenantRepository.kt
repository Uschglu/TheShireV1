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
 * ⚠️ TOURS EMPILABLES (v19) :
 *    Les contenants de type "tour" possèdent des ÉTAGES (EtageEntity).
 *    Chaque étage est INDÉPENDANT : il a ses propres emplacements,
 *    sa propre culture, son propre nombre d'emplacements.
 *    
 *    Pour les tours, TOUS les emplacements ont un etageId non-null.
 *    Pour les autres contenants, tous les emplacements ont etageId = null.
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
                        nombreEmplacements = 0,
                        notes = ""
                    )
                }
                try {
                    etageDao.insertEtages(etages)
                } catch (e: Exception) {
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
        // 1. Rappels culturaux
        try {
            rappelCulturelRepository.supprimerRappelsPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        // 2. Cultures liées
        try {
            cultureRepository.supprimerCulturesPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        // 3. Emplacements (tous, y compris ceux d'étages)
        try {
            contenantDao.deleteEmplacementsPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        // 4. Étages
        try {
            etageDao.deleteEtagesPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        // 5. Contenant
        contenantDao.deleteContenant(contenant)
    }
    
    // ============================================================
    // ÉTAGES (TOURS EMPILABLES)
    // ============================================================
    
    fun estTour(contenant: ContenantEntity): Boolean {
        return contenant.type == TYPE_TOUR
    }
    
    fun getEtagesPourContenant(contenantId: Long): Flow<List<EtageEntity>> {
        return etageDao.getEtagesPourContenant(contenantId)
    }
    
    suspend fun getEtagesSync(contenantId: Long): List<EtageEntity> {
        return etageDao.getEtagesPourContenantSync(contenantId)
    }
    
    suspend fun getEtageParId(id: Long): EtageEntity? {
        return etageDao.getEtageParId(id)
    }
    
    suspend fun getEtageParNumero(contenantId: Long, numero: Int): EtageEntity? {
        return etageDao.getEtageParNumero(contenantId, numero)
    }
    
    suspend fun countEtages(contenantId: Long): Int {
        return etageDao.countEtagesPourContenant(contenantId)
    }
    
    suspend fun countEtagesInitialises(contenantId: Long): Int {
        return etageDao.countEtagesInitialises(contenantId)
    }
    
    suspend fun definirNombreEmplacementsEtage(etageId: Long, nombre: Int) {
        etageDao.definirNombreEmplacements(etageId, nombre)
    }
    
    suspend fun modifierNotesEtage(etageId: Long, nouvellesNotes: String) {
        etageDao.updateNotes(etageId, nouvellesNotes)
    }
    
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
    
    /**
     * Récupère uniquement les emplacements DIRECTS d'un contenant
     * (ceux sans étage — pour les contenants non-tour).
     */
    fun getEmplacementsDirectsPourContenant(contenantId: Long): Flow<List<EmplacementContenantEntity>> {
        return contenantDao.getEmplacementsDirectsPourContenant(contenantId)
    }
    
    suspend fun getEmplacementsDirectsSync(contenantId: Long): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsDirectsPourContenantSync(contenantId)
    }
    
    /**
     * Récupère les emplacements d'un étage spécifique (tour).
     */
    fun getEmplacementsPourEtage(etageId: Long): Flow<List<EmplacementContenantEntity>> {
        return contenantDao.getEmplacementsPourEtage(etageId)
    }
    
    suspend fun getEmplacementsPourEtageSync(etageId: Long): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsPourEtageSync(etageId)
    }
    
    suspend fun countEmplacements(contenantId: Long): Int {
        return contenantDao.countEmplacementsPourContenant(contenantId)
    }
    
    suspend fun countEmplacementsOccupes(contenantId: Long): Int {
        return contenantDao.countEmplacementsOccupes(contenantId)
    }
    
    suspend fun countEmplacementsPourEtage(etageId: Long): Int {
        return contenantDao.countEmplacementsPourEtage(etageId)
    }
    
    suspend fun countEmplacementsOccupesPourEtage(etageId: Long): Int {
        return contenantDao.countEmplacementsOccupesPourEtage(etageId)
    }
    
    // ============================================================
    // PLANTATION
    // ============================================================
    
    /**
     * Plante une culture dans un emplacement d'un contenant NON-TOUR.
     * 
     * Pour planter sur une tour, utiliser `planterDansEtage()`.
     * 
     * @return ResultatCreationCulture (identifiants + info décrément)
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
    ): ResultatCreationCulture {
        // ⚠️ Si c'est une tour, on redirige vers planterDansEtage (avec étage 1 par défaut)
        if (estTour(contenant)) {
            val etages = getEtagesSync(contenant.id)
            if (etages.isEmpty()) {
                throw IllegalStateException(
                    "Tour sans étage : ${contenant.nom} (id=${contenant.id})"
                )
            }
            return planterDansEtage(
                context = context,
                contenant = contenant,
                etage = etages.first(),
                numeroEmplacement = numeroEmplacement,
                legumeNom = legumeNom,
                legume = legume,
                varieteNom = varieteNom,
                emoji = emoji,
                sourceStock = sourceStock,
                sourceStockId = sourceStockId
            )
        }
        
        // ==== Contenant NON-TOUR : comportement classique ====
        val dateActuelle = System.currentTimeMillis()
        
        // 1. Créer la CultureEntity (gère le mode réel / projection)
        val culture = CultureEntity(
            typeEmplacement = CultureEntity.TYPE_URBAIN,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement,
            legumeNom = legumeNom,
            varieteNom = varieteNom,
            emoji = emoji,
            quantite = 1,
            sourceStock = sourceStock,
            estProjection = true,
            datePlantation = dateActuelle
        )
        
        val resultatCulture = cultureRepository.creerCulture(
            context = context,
            culture = culture,
            sourceStockId = sourceStockId
        )
        
        // Si erreur, on remonte l'erreur telle quelle
        if (resultatCulture !is ResultatCreationCulture.Succes) {
            return resultatCulture
        }
        
        // 2. Mettre à jour l'emplacement
        val emplacements = contenantDao.getEmplacementsDirectsPourContenantSync(contenant.id)
        
        if (emplacements.isEmpty()) {
            // Premier plant → générer tous les emplacements
            val nombreEmplacements = CalculEmplacements.calculerNombreEmplacements(
                contenant = contenant,
                legume = legume
            )
            
            val nouveauxEmplacements = (1..nombreEmplacements).map { numero ->
                EmplacementContenantEntity(
                    contenantId = contenant.id,
                    etageId = null,
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
        
        // 3. Rappels culturaux
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = null,
            caseNumero = null,
            plancheId = null,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement
        )
        
        return resultatCulture
    }
    
    /**
     * Plante une culture sur un ÉTAGE spécifique d'une tour.
     * 
     * Chaque étage a ses propres emplacements, indépendants des autres étages.
     * 
     * ⚠️ La 1ère plantation sur un étage définit automatiquement son
     *    nombre d'emplacements (basé sur la densité de la plante et la
     *    surface de l'anneau de l'étage).
     * 
     * @return ResultatCreationCulture
     */
    suspend fun planterDansEtage(
        context: Context,
        contenant: ContenantEntity,
        etage: EtageEntity,
        numeroEmplacement: Int,
        legumeNom: String,
        legume: LegumeEntity,
        varieteNom: String? = null,
        emoji: String = "🌱",
        sourceStock: String = CultureEntity.SOURCE_AUCUNE,
        sourceStockId: Long? = null
    ): ResultatCreationCulture {
        val dateActuelle = System.currentTimeMillis()
        
        // 1. Créer la CultureEntity
        val culture = CultureEntity(
            typeEmplacement = CultureEntity.TYPE_URBAIN,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement,
            legumeNom = legumeNom,
            varieteNom = varieteNom,
            emoji = emoji,
            quantite = 1,
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
            return resultatCulture
        }
        
        // 2. Récupérer les emplacements de l'étage
        val emplacements = contenantDao.getEmplacementsPourEtageSync(etage.id)
        
        if (emplacements.isEmpty()) {
            // ⭐ Premier plant sur cet étage → calculer le nombre d'emplacements
            //    La surface d'un étage = surface d'un disque simple (pas × nombre d'étages)
            val contenantUnEtage = contenant.copy(nombreEtages = 1)
            val nombreEmplacements = CalculEmplacements.calculerNombreEmplacements(
                contenant = contenantUnEtage,
                legume = legume
            )
            
            // Mettre à jour le nombre d'emplacements de l'étage
            etageDao.definirNombreEmplacements(etage.id, nombreEmplacements)
            
            // Créer tous les emplacements de l'étage
            val nouveauxEmplacements = (1..nombreEmplacements).map { numero ->
                EmplacementContenantEntity(
                    contenantId = contenant.id,
                    etageId = etage.id,
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
        
        // 3. Rappels culturaux
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = null,
            caseNumero = null,
            plancheId = null,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement
        )
        
        return resultatCulture
    }
    
    // ============================================================
    // VIDAGE
    // ============================================================
    
    /**
     * Vide un emplacement (retrait sans récolte).
     */
    suspend fun viderEmplacement(emplacementId: Long) {
        val emplacement = contenantDao.getEmplacementParId(emplacementId) ?: return
        
        try {
            val cultureActive = cultureRepository.getCultureActiveDansEmplacement(
                contenantId = emplacement.contenantId,
                emplacementNumero = emplacement.numero
            )
            if (cultureActive != null) {
                cultureRepository.terminerSansRecolte(cultureActive.id)
            }
        } catch (e: Exception) {
        }
        
        try {
            rappelCulturelRepository.supprimerRappelsPourEmplacement(
                contenantId = emplacement.contenantId,
                emplacementNumero = emplacement.numero
            )
        } catch (e: Exception) {
            throw e
        }
        
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
    
    /**
     * Vide un étage entier (toutes les cultures terminées, emplacements libérés).
     * 
     * ⚠️ Le nombre d'emplacements de l'étage est conservé.
     */
    suspend fun viderEtage(etageId: Long) {
        val emplacements = contenantDao.getEmplacementsPourEtageSync(etageId)
        
        emplacements.forEach { emp ->
            if (emp.estOccupe()) {
                try {
                    val cultureActive = cultureRepository.getCultureActiveDansEmplacement(
                        contenantId = emp.contenantId,
                        emplacementNumero = emp.numero
                    )
                    if (cultureActive != null) {
                        cultureRepository.terminerSansRecolte(cultureActive.id)
                    }
                } catch (e: Exception) {
                }
                
                try {
                    rappelCulturelRepository.supprimerRappelsPourEmplacement(
                        contenantId = emp.contenantId,
                        emplacementNumero = emp.numero
                    )
                } catch (e: Exception) {
                }
                
                try {
                    contenantDao.updateEmplacement(
                        emp.copy(legumeNom = null, datePlantation = null)
                    )
                } catch (e: Exception) {
                }
            }
        }
    }
    
    suspend fun ajouterEmplacements(contenantId: Long, nombre: Int) {
        val existants = contenantDao.getEmplacementsDirectsPourContenantSync(contenantId)
        val dernierNumero = existants.maxOfOrNull { it.numero } ?: 0
        
        val nouveaux = (1..nombre).map { i ->
            EmplacementContenantEntity(
                contenantId = contenantId,
                etageId = null,
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
    
    /**
     * Vérifie les associations d'un nouvel emplacement avec les autres
     * emplacements DU MÊME ÉTAGE (pour une tour) ou du même contenant.
     */
    suspend fun verifierAssociationsContenant(
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String,
        etageId: Long? = null
    ): List<Pair<String, String>> {
        val resultats = mutableListOf<Pair<String, String>>()
        
        val emplacements = if (etageId != null) {
            contenantDao.getEmplacementsPourEtageSync(etageId)
        } else {
            contenantDao.getEmplacementsDirectsPourContenantSync(contenant.id)
        }
        
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
