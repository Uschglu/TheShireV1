package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CalculEmplacements
import com.theshire.app.data.ContenantEntity
import com.theshire.app.data.EmplacementContenantEntity
import com.theshire.app.data.LegumeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository pour les contenants urbains et leurs emplacements.
 * 
 * Gère :
 * - La création de contenants avec calcul automatique des emplacements
 * - L'installation de plantes dans les emplacements (AVEC génération
 *   automatique des rappels culturaux comme en pleine terre)
 * - Les associations de culture au sein d'un même contenant
 * - La suppression en cascade
 * 
 * ⚠️ VERSION DEBUG : contient des logs pour identifier le crash du vidage
 */
class ContenantRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val contenantDao = AppDatabase.getDatabase(context).contenantDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    private val rappelCulturelRepository = RappelCulturelRepository(context)
    
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
        android.util.Log.d("DEBUG_VIDAGE", "=== supprimerContenant appelé pour id=${contenant.id} ===")
        try {
            rappelCulturelRepository.supprimerRappelsPourContenant(contenant.id)
            android.util.Log.d("DEBUG_VIDAGE", "Rappels contenant supprimés OK")
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_VIDAGE", "ERREUR suppression rappels contenant : ${e.message}", e)
        }
        contenantDao.deleteContenant(contenant)
        android.util.Log.d("DEBUG_VIDAGE", "Contenant supprimé OK")
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
    
    suspend fun planterDansEmplacement(
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String,
        legume: LegumeEntity
    ) {
        android.util.Log.d("DEBUG_VIDAGE", "=== planterDansEmplacement : contenant=${contenant.id}, emp=$numeroEmplacement, legume=$legumeNom ===")
        val dateActuelle = System.currentTimeMillis()
        
        val emplacements = contenantDao.getEmplacementsPourContenantSync(contenant.id)
        android.util.Log.d("DEBUG_VIDAGE", "Emplacements existants : ${emplacements.size}")
        
        if (emplacements.isEmpty()) {
            val nombreEmplacements = CalculEmplacements.calculerNombreEmplacements(
                contenant = contenant,
                legume = legume
            )
            android.util.Log.d("DEBUG_VIDAGE", "Création de $nombreEmplacements emplacements")
            
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
        
        android.util.Log.d("DEBUG_VIDAGE", "Génération rappels culturaux...")
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = null,
            caseNumero = null,
            plancheId = null,
            contenantId = contenant.id,
            emplacementNumero = numeroEmplacement
        )
        android.util.Log.d("DEBUG_VIDAGE", "=== planterDansEmplacement terminé ===")
    }
    
    /**
     * Vide un emplacement (retire la plante).
     * 
     * ⚠️ VERSION DEBUG avec traces
     */
    suspend fun viderEmplacement(emplacementId: Long) {
        android.util.Log.d("DEBUG_VIDAGE", "=== DEBUT viderEmplacement ===")
        android.util.Log.d("DEBUG_VIDAGE", "A. id reçu = $emplacementId")
        
        val emplacement = contenantDao.getEmplacementParId(emplacementId)
        if (emplacement == null) {
            android.util.Log.e("DEBUG_VIDAGE", "B. Emplacement NULL, abandon")
            return
        }
        android.util.Log.d("DEBUG_VIDAGE", "C. Emplacement trouvé : contenantId=${emplacement.contenantId}, numero=${emplacement.numero}, legumeNom=${emplacement.legumeNom}")
        
        try {
            android.util.Log.d("DEBUG_VIDAGE", "D. Suppression des rappels culturaux...")
            rappelCulturelRepository.supprimerRappelsPourEmplacement(
                contenantId = emplacement.contenantId,
                emplacementNumero = emplacement.numero
            )
            android.util.Log.d("DEBUG_VIDAGE", "E. Rappels supprimés OK")
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_VIDAGE", "ERREUR suppression rappels : ${e.message}", e)
            throw e
        }
        
        try {
            android.util.Log.d("DEBUG_VIDAGE", "F. Update emplacement...")
            contenantDao.updateEmplacement(
                emplacement.copy(
                    legumeNom = null,
                    datePlantation = null
                )
            )
            android.util.Log.d("DEBUG_VIDAGE", "G. Update OK")
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_VIDAGE", "ERREUR update emplacement : ${e.message}", e)
            throw e
        }
        
        android.util.Log.d("DEBUG_VIDAGE", "=== FIN viderEmplacement ===")
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
