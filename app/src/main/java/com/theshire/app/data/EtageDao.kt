package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les étages des tours empilables.
 * 
 * ⚠️ Uniquement utilisé pour les contenants de type "tour".
 *    Les autres types de contenants n'ont pas d'étages.
 * 
 * Fournit :
 *  - CRUD pour les étages
 *  - Requêtes de lecture (par contenant, par id)
 *  - Insertion en lot (création des N étages d'une tour d'un coup)
 *  - Requêtes avec Flow pour l'observation Compose
 */
@Dao
interface EtageDao {
    
    // ============================================================
    // LECTURE
    // ============================================================
    
    /**
     * Récupère tous les étages d'un contenant, triés par numéro croissant
     * (base → haut de la tour).
     */
    @Query("SELECT * FROM etages WHERE contenantId = :contenantId ORDER BY numero ASC")
    fun getEtagesPourContenant(contenantId: Long): Flow<List<EtageEntity>>
    
    /**
     * Version synchrone pour usage ponctuel (hors Compose).
     */
    @Query("SELECT * FROM etages WHERE contenantId = :contenantId ORDER BY numero ASC")
    suspend fun getEtagesPourContenantSync(contenantId: Long): List<EtageEntity>
    
    /**
     * Récupère un étage par son ID.
     */
    @Query("SELECT * FROM etages WHERE id = :id")
    suspend fun getEtageParId(id: Long): EtageEntity?
    
    /**
     * Récupère un étage précis d'un contenant par son numéro.
     */
    @Query("SELECT * FROM etages WHERE contenantId = :contenantId AND numero = :numero LIMIT 1")
    suspend fun getEtageParNumero(contenantId: Long, numero: Int): EtageEntity?
    
    /**
     * Compte le nombre d'étages d'un contenant.
     */
    @Query("SELECT COUNT(*) FROM etages WHERE contenantId = :contenantId")
    suspend fun countEtagesPourContenant(contenantId: Long): Int
    
    /**
     * Compte le nombre d'étages initialisés (nombreEmplacements > 0).
     */
    @Query("SELECT COUNT(*) FROM etages WHERE contenantId = :contenantId AND nombreEmplacements > 0")
    suspend fun countEtagesInitialises(contenantId: Long): Int
    
    /**
     * Récupère tous les étages de tous les contenants (usage debug / stats).
     */
    @Query("SELECT * FROM etages ORDER BY contenantId ASC, numero ASC")
    fun getAllEtages(): Flow<List<EtageEntity>>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Insère un nouvel étage et retourne son ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEtage(etage: EtageEntity): Long
    
    /**
     * Insère plusieurs étages d'un coup (création d'une tour complète).
     * 
     * ⚠️ Utilise ABORT pour détecter les conflits d'unicité
     *    (contenantId + numero doit être unique).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEtages(etages: List<EtageEntity>)
    
    /**
     * Met à jour un étage.
     */
    @Update
    suspend fun updateEtage(etage: EtageEntity)
    
    /**
     * Supprime un étage.
     */
    @Delete
    suspend fun deleteEtage(etage: EtageEntity)
    
    /**
     * Supprime un étage par son ID.
     */
    @Query("DELETE FROM etages WHERE id = :id")
    suspend fun deleteEtageParId(id: Long)
    
    /**
     * Supprime tous les étages d'un contenant.
     * (Normalement géré automatiquement par le CASCADE, mais utile pour reset.)
     */
    @Query("DELETE FROM etages WHERE contenantId = :contenantId")
    suspend fun deleteEtagesPourContenant(contenantId: Long)
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    /**
     * Définit le nombre d'emplacements d'un étage (1ère plantation).
     * Une fois défini, ce nombre ne change plus automatiquement.
     */
    @Query("UPDATE etages SET nombreEmplacements = :nombre WHERE id = :id")
    suspend fun definirNombreEmplacements(id: Long, nombre: Int)
    
    /**
     * Met à jour les notes d'un étage.
     */
    @Query("UPDATE etages SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
    
    /**
     * Récupère tous les étages non encore initialisés (nombreEmplacements = 0).
     * Utile pour un futur écran de "nettoyage" ou d'audit.
     */
    @Query("SELECT * FROM etages WHERE nombreEmplacements = 0 ORDER BY contenantId ASC, numero ASC")
    suspend fun getEtagesNonInitialises(): List<EtageEntity>
}
