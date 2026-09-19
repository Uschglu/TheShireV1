package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.CalculEmplacements
import com.theshire.app.data.ContenantEntity
import com.theshire.app.data.EmplacementContenantEntity
import com.theshire.app.data.LegumeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository pour les contenants urbains et leurs emplacements.
 * 
 * Gère :
 * - La création de contenants avec calcul automatique des emplacements
 * - L'installation de plantes dans les emplacements
 * - Les associations de culture au sein d'un même contenant
 * - La suppression en cascade
 */
class ContenantRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val contenantDao = AppDatabase.getDatabase(context).contenantDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    companion object {
        /**
         * Verrou global pour éviter les créations concurrentes de contenants.
         */
        private val mutexCreation = Mutex()
    }
    
    // ============================================================
    // CONTENANTS
    // ============================================================
    
    /**
     * Récupère tous les contenants (Flow).
     */
    val contenants: Flow<List<ContenantEntity>> = contenantDao.getAllContenants()
    
    /**
     * Récupère un contenant par son ID.
     */
    suspend fun getContenantParId(id: Long): ContenantEntity? {
        return contenantDao.getContenantParId(id)
    }
    
    /**
     * Récupère tous les contenants (version synchrone).
     */
    suspend fun getTousContenants(): List<ContenantEntity> {
        return contenantDao.getAllContenantsSync()
    }
    
    /**
     * Crée un nouveau contenant SANS emplacements (ils seront ajoutés
     * quand l'utilisateur plantera quelque chose).
     * 
     * Retourne l'ID du contenant créé.
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
            return contenantDao.insertContenant(contenant)
        }
    }
    
    /**
     * Crée un contenant AVEC les emplacements calculés automatiquement
     * selon la plante prévue.
     * 
     * Utilisé si on veut directement créer les emplacements en même temps
     * que le contenant. Sinon, on crée le contenant vide et on ajoute
     * les emplacements à la plantation.
     * 
     * @param legumePlante La plante prévue (pour calculer le nombre d'emplacements)
     */
    suspend fun creerContenantAvecPlante(
        nom: String,
        type: String,
        emoji: String,
        dimension1: Int,
        dimension2: Int = 0,
        dimension3: Int = 0,
        nombreEtages: Int = 1,
        milieu: String = "Balcon",
        notes: String = "",
        legumePlante: LegumeEntity
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
            
            // Calculer le nombre d'emplacements
            val nombreEmplacements = CalculEmplacements.calculerNombreEmplacements(
                contenant = contenant,
                legume = legumePlante
            )
            
            // Créer les emplacements vides
            val emplacements = (1..nombreEmplacements).map { numero ->
                EmplacementContenantEntity(
                    contenantId = contenantId,
                    numero = numero
                )
            }
            contenantDao.insertEmplacements(emplacements)
            
            return contenantId
        }
    }
    
    /**
     * Modifie le nom d'un contenant.
     */
    suspend fun modifierNom(contenantId: Long, nouveauNom: String) {
        val contenant = contenantDao.getContenantParId(contenantId) ?: return
        contenantDao.updateContenant(contenant.copy(nom = nouveauNom))
    }
    
    /**
     * Modifie les dimensions d'un contenant.
     * Attention : recalculer les emplacements si nécessaire.
     */
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
    
    /**
     * Modifie les notes d'un contenant.
     */
    suspend fun modifierNotes(contenantId: Long, nouvellesNotes: String) {
        val contenant = contenantDao.getContenantParId(contenantId) ?: return
        contenantDao.updateContenant(contenant.copy(notes = nouvellesNotes))
    }
    
    /**
     * Supprime un contenant (et ses emplacements par CASCADE).
     */
    suspend fun supprimerContenant(contenant: ContenantEntity) {
        contenantDao.deleteContenant(contenant)
    }
    
    // ============================================================
    // EMPLACEMENTS
    // ============================================================
    
    /**
     * Récupère les emplacements d'un contenant (Flow).
     */
    fun getEmplacementsPourContenant(contenantId: Long): Flow<List<EmplacementContenantEntity>> {
        return contenantDao.getEmplacementsPourContenant(contenantId)
    }
    
    /**
     * Récupère les emplacements d'un contenant (version synchrone).
     */
    suspend fun getEmplacementsSync(contenantId: Long): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsPourContenantSync(contenantId)
    }
    
    /**
     * Compte les emplacements d'un contenant.
     */
    suspend fun countEmplacements(contenantId: Long): Int {
        return contenantDao.countEmplacementsPourContenant(contenantId)
    }
    
    /**
     * Compte les emplacements occupés d'un contenant.
     */
    suspend fun countEmplacementsOccupes(contenantId: Long): Int {
        return contenantDao.countEmplacementsOccupes(contenantId)
    }
    
    /**
     * Installe une plante dans un emplacement donné.
     * 
     * @param contenant Le contenant
     * @param numeroEmplacement Le numéro de l'emplacement
     * @param legumeNom Le nom de la plante ("Basilic", "Tomate (Marmande)")
     * @param legume L'objet LegumeEntity (pour calculer le nombre total d'emplacements)
     */
    suspend fun planterDansEmplacement(
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String,
        legume: LegumeEntity
    ) {
        val dateActuelle = System.currentTimeMillis()
        
        // Récupérer les emplacements actuels
        val emplacements = contenantDao.getEmplacementsPourContenantSync(contenant.id)
        
        // Si l'emplacement n'existe pas, le créer avec les autres
        if (emplacements.isEmpty()) {
            // Première plantation : créer tous les emplacements d'un coup
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
            // Emplacement existant : mettre à jour
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
    }
    
    /**
     * Vide un emplacement (retire la plante).
     */
    suspend fun viderEmplacement(emplacementId: Long) {
        val emplacement = contenantDao.getEmplacementParId(emplacementId) ?: return
        contenantDao.updateEmplacement(
            emplacement.copy(
                legumeNom = null,
                datePlantation = null
            )
        )
    }
    
    /**
     * Ajoute des emplacements supplémentaires à un contenant (si l'utilisateur
     * veut en ajouter manuellement).
     */
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
    
    /**
     * Modifie les notes d'un emplacement.
     */
    suspend fun modifierNotesEmplacement(emplacementId: Long, nouvellesNotes: String) {
        val emplacement = contenantDao.getEmplacementParId(emplacementId) ?: return
        contenantDao.updateEmplacement(emplacement.copy(notes = nouvellesNotes))
    }
    
    // ============================================================
    // VALIDATION DES ASSOCIATIONS
    // ============================================================
    
    /**
     * Vérifie les associations de culture entre une plante et les
     * autres plantes déjà installées dans le MÊME contenant.
     * 
     * Retourne la liste des plantes mal associées (avec leur type d'association).
     * 
     * @param contenant Le contenant concerné
     * @param numeroEmplacement L'emplacement où on veut planter
     * @param legumeNom Le nom de la nouvelle plante
     * @return Liste de paires (nomPlanteVoisine, typeAssociation) où
     *         typeAssociation est "bonne" ou "mauvaise"
     */
    suspend fun verifierAssociationsContenant(
        contenant: ContenantEntity,
        numeroEmplacement: Int,
        legumeNom: String
    ): List<Pair<String, String>> {
        val resultats = mutableListOf<Pair<String, String>>()
        
        // Récupérer les emplacements actuels
        val emplacements = contenantDao.getEmplacementsPourContenantSync(contenant.id)
        
        // Récupérer le légume
        val nomBase = if (legumeNom.contains("(")) legumeNom.substringBefore("(").trim() else legumeNom
        val legume = legumeDao.getLegumeByNom(nomBase) ?: return emptyList()
        
        // Vérifier chaque autre emplacement
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
    
    /**
     * Retourne le nombre total de contenants.
     */
    suspend fun countContenants(): Int {
        return contenantDao.countContenants()
    }
    
    /**
     * Retourne le nombre total d'emplacements (tous contenants confondus).
     */
    suspend fun countAllEmplacements(): Int {
        return contenantDao.countAllEmplacements()
    }
    
    /**
     * Retourne le nombre total d'emplacements occupés.
     */
    suspend fun countAllEmplacementsOccupes(): Int {
        return contenantDao.countAllEmplacementsOccupes()
    }
    
    /**
     * Retourne tous les emplacements occupés (toutes contenants confondus).
     */
    suspend fun getAllEmplacementsOccupes(): List<EmplacementContenantEntity> {
        return contenantDao.getAllEmplacementsOccupes()
    }
    
    /**
     * Retourne la liste des emplacements où un légume est planté.
     */
    suspend fun getEmplacementsPourLegume(legumeNom: String): List<EmplacementContenantEntity> {
        return contenantDao.getEmplacementsPourLegume(legumeNom)
    }
}
