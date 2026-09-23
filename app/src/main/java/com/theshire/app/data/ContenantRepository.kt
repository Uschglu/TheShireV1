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
 */
class ContenantRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val contenantDao = AppDatabase.getDatabase(context).contenantDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    private val rappelCulturelRepository = RappelCulturelRepository(context)
    private val cultureRepository = CultureRepository(context)
    
    companion object {
        private val mutexCreation = Mutex()
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
            return contenantDao.insertContenant(contenant)
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
        try {
            rappelCulturelRepository.supprimerRappelsPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        try {
            cultureRepository.supprimerCulturesPourContenant(contenant.id)
        } catch (e: Exception) {
        }
        
        contenantDao.deleteContenant(contenant)
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
     * ⚠️ NOUVEAU : cette méthode crée désormais une CultureEntity en parallèle
     *    de l'occupation de l'emplacement (pour lier la plantation au stock).
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
     * ⚠️ NOUVEAU : termine aussi la CultureEntity associée.
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
