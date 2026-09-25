package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les contenants urbains et leurs emplacements.
 * 
 * Fournit :
 * - CRUD pour les contenants
 * - CRUD pour les emplacements
 * - Requêtes avec Flow pour l'observation Compose
 * 
 * ⚠️ TOURS EMPILABLES (v19) :
 *    Les emplacements peuvent être rattachés à un ÉTAGE (etageId).
 *    Requêtes spécifiques par étage fournies.
 */
@Dao
interface ContenantDao {
    
    // ============================================================
    // CONTENANTS
    // ============================================================
    
    /**
     * Récupère tous les contenants, triés par date de création décroissante.
     */
    @Query("SELECT * FROM contenants ORDER BY dateCreation DESC")
    fun getAllContenants(): Flow<List<ContenantEntity>>
    
    /**
     * Récupère tous les contenants (version synchrone, pour usage ponctuel).
     */
    @Query("SELECT * FROM contenants ORDER BY dateCreation DESC")
    suspend fun getAllContenantsSync(): List<ContenantEntity>
    
    /**
     * Récupère un contenant par son ID.
     */
    @Query("SELECT * FROM contenants WHERE id = :id")
    suspend fun getContenantParId(id: Long): ContenantEntity?
    
    /**
     * Compte le nombre total de contenants.
     */
    @Query("SELECT COUNT(*) FROM contenants")
    suspend fun countContenants(): Int
    
    /**
     * Insère un nouveau contenant et retourne son ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContenant(contenant: ContenantEntity): Long
    
    /**
     * Met à jour un contenant.
     */
    @Update
    suspend fun updateContenant(contenant: ContenantEntity)
    
    /**
     * Supprime un contenant (et tous ses emplacements par CASCADE).
     */
    @Delete
    suspend fun deleteContenant(contenant: ContenantEntity)
    
    /**
     * Supprime un contenant par son ID (et ses emplacements par CASCADE).
     */
    @Query("DELETE FROM contenants WHERE id = :id")
    suspend fun deleteContenantParId(id: Long)
    
    // ============================================================
    // EMPLACEMENTS — génériques
    // ============================================================
    
    /**
     * Récupère TOUS les emplacements d'un contenant, triés par numéro.
     * (Y compris les emplacements rattachés à des étages.)
     */
    @Query("SELECT * FROM emplacements_contenants WHERE contenantId = :contenantId ORDER BY numero ASC")
    fun getEmplacementsPourContenant(contenantId: Long): Flow<List<EmplacementContenantEntity>>
    
    /**
     * Récupère tous les emplacements d'un contenant (version synchrone).
     */
    @Query("SELECT * FROM emplacements_contenants WHERE contenantId = :contenantId ORDER BY numero ASC")
    suspend fun getEmplacementsPourContenantSync(contenantId: Long): List<EmplacementContenantEntity>
    
    /**
     * Récupère un emplacement par son ID.
     */
    @Query("SELECT * FROM emplacements_contenants WHERE id = :id")
    suspend fun getEmplacementParId(id: Long): EmplacementContenantEntity?
    
    /**
     * Compte le nombre d'emplacements d'un contenant (tous étages confondus).
     */
    @Query("SELECT COUNT(*) FROM emplacements_contenants WHERE contenantId = :contenantId")
    suspend fun countEmplacementsPourContenant(contenantId: Long): Int
    
    /**
     * Compte le nombre d'emplacements occupés d'un contenant.
     */
    @Query("SELECT COUNT(*) FROM emplacements_contenants WHERE contenantId = :contenantId AND legumeNom IS NOT NULL")
    suspend fun countEmplacementsOccupes(contenantId: Long): Int
    
    // ============================================================
    // EMPLACEMENTS — directs (contenants non-tour, etageId IS NULL)
    // ============================================================
    
    /**
     * Récupère les emplacements DIRECTS d'un contenant
     * (non rattachés à un étage, donc pour les contenants non-tour).
     */
    @Query(
        "SELECT * FROM emplacements_contenants " +
        "WHERE contenantId = :contenantId AND etageId IS NULL " +
        "ORDER BY numero ASC"
    )
    fun getEmplacementsDirectsPourContenant(contenantId: Long): Flow<List<EmplacementContenantEntity>>
    
    /**
     * Version synchrone des emplacements directs.
     */
    @Query(
        "SELECT * FROM emplacements_contenants " +
        "WHERE contenantId = :contenantId AND etageId IS NULL " +
        "ORDER BY numero ASC"
    )
    suspend fun getEmplacementsDirectsPourContenantSync(contenantId: Long): List<EmplacementContenantEntity>
    
    /**
     * Compte les emplacements directs (non-tour).
     */
    @Query(
        "SELECT COUNT(*) FROM emplacements_contenants " +
        "WHERE contenantId = :contenantId AND etageId IS NULL"
    )
    suspend fun countEmplacementsDirects(contenantId: Long): Int
    
    // ============================================================
    // EMPLACEMENTS — par étage (tours)
    // ============================================================
    
    /**
     * Récupère les emplacements d'un étage donné.
     */
    @Query(
        "SELECT * FROM emplacements_contenants " +
        "WHERE etageId = :etageId " +
        "ORDER BY numero ASC"
    )
    fun getEmplacementsPourEtage(etageId: Long): Flow<List<EmplacementContenantEntity>>
    
    /**
     * Version synchrone des emplacements d'un étage.
     */
    @Query(
        "SELECT * FROM emplacements_contenants " +
        "WHERE etageId = :etageId " +
        "ORDER BY numero ASC"
    )
    suspend fun getEmplacementsPourEtageSync(etageId: Long): List<EmplacementContenantEntity>
    
    /**
     * Compte le nombre d'emplacements d'un étage.
     */
    @Query("SELECT COUNT(*) FROM emplacements_contenants WHERE etageId = :etageId")
    suspend fun countEmplacementsPourEtage(etageId: Long): Int
    
    /**
     * Compte le nombre d'emplacements occupés d'un étage.
     */
    @Query(
        "SELECT COUNT(*) FROM emplacements_contenants " +
        "WHERE etageId = :etageId AND legumeNom IS NOT NULL"
    )
    suspend fun countEmplacementsOccupesPourEtage(etageId: Long): Int
    
    /**
     * Supprime tous les emplacements d'un étage.
     */
    @Query("DELETE FROM emplacements_contenants WHERE etageId = :etageId")
    suspend fun deleteEmplacementsPourEtage(etageId: Long)
    
    // ============================================================
    // EMPLACEMENTS — écriture
    // ============================================================
    
    /**
     * Insère un nouvel emplacement et retourne son ID.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmplacement(emplacement: EmplacementContenantEntity): Long
    
    /**
     * Insère plusieurs emplacements d'un coup.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmplacements(emplacements: List<EmplacementContenantEntity>)
    
    /**
     * Met à jour un emplacement.
     */
    @Update
    suspend fun updateEmplacement(emplacement: EmplacementContenantEntity)
    
    /**
     * Supprime un emplacement.
     */
    @Delete
    suspend fun deleteEmplacement(emplacement: EmplacementContenantEntity)
    
    /**
     * Supprime un emplacement par son ID.
     */
    @Query("DELETE FROM emplacements_contenants WHERE id = :id")
    suspend fun deleteEmplacementParId(id: Long)
    
    /**
     * Supprime tous les emplacements d'un contenant.
     */
    @Query("DELETE FROM emplacements_contenants WHERE contenantId = :contenantId")
    suspend fun deleteEmplacementsPourContenant(contenantId: Long)
    
    // ============================================================
    // EMPLACEMENTS — opérations métier
    // ============================================================
    
    /**
     * Rattache un emplacement à un étage.
     */
    @Query("UPDATE emplacements_contenants SET etageId = :etageId WHERE id = :id")
    suspend fun updateEtageIdPourEmplacement(id: Long, etageId: Long?)
    
    // ============================================================
    // REQUÊTES UTILITAIRES
    // ============================================================
    
    /**
     * Récupère tous les emplacements (tous contenants confondus).
     */
    @Query("SELECT * FROM emplacements_contenants ORDER BY contenantId ASC, numero ASC")
    fun getAllEmplacements(): Flow<List<EmplacementContenantEntity>>
    
    /**
     * Compte le nombre total d'emplacements (tous contenants confondus).
     */
    @Query("SELECT COUNT(*) FROM emplacements_contenants")
    suspend fun countAllEmplacements(): Int
    
    /**
     * Compte le nombre total d'emplacements occupés (tous contenants confondus).
     */
    @Query("SELECT COUNT(*) FROM emplacements_contenants WHERE legumeNom IS NOT NULL")
    suspend fun countAllEmplacementsOccupes(): Int
    
    /**
     * Récupère tous les emplacements d'un légume donné (pour retrouver
     * où une plante est installée).
     */
    @Query(
        "SELECT * FROM emplacements_contenants " +
        "WHERE legumeNom LIKE :legumeNom || '%' " +
        "ORDER BY datePlantation DESC"
    )
    suspend fun getEmplacementsPourLegume(legumeNom: String): List<EmplacementContenantEntity>
    
    /**
     * Récupère tous les emplacements où un légume donné est planté.
     * Utilisé pour savoir si une plante est cultivée en ce moment.
     */
    @Query("SELECT * FROM emplacements_contenants WHERE legumeNom IS NOT NULL")
    suspend fun getAllEmplacementsOccupes(): List<EmplacementContenantEntity>
}
