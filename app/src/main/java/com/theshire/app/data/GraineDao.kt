package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les graines.
 * 
 * Toutes les opérations CRUD + quelques requêtes utiles pour l'UI.
 */
@Dao
interface GraineDao {
    
    // ============================================================
    // LECTURE
    // ============================================================
    
    /**
     * Toutes les graines, triées par date d'ajout (plus récentes en premier).
     */
    @Query("SELECT * FROM graines ORDER BY dateAjout DESC")
    fun getAllGraines(): Flow<List<GraineEntity>>
    
    /**
     * Toutes les graines synchrones (pour usage ponctuel hors Compose).
     */
    @Query("SELECT * FROM graines ORDER BY dateAjout DESC")
    suspend fun getAllGrainesSync(): List<GraineEntity>
    
    /**
     * Une graine par son ID.
     */
    @Query("SELECT * FROM graines WHERE id = :id")
    suspend fun getGraineParId(id: Long): GraineEntity?
    
    /**
     * Les graines d'un légume donné.
     */
    @Query("SELECT * FROM graines WHERE legumeNom = :legumeNom ORDER BY dateAjout DESC")
    fun getGrainesPourLegume(legumeNom: String): Flow<List<GraineEntity>>
    
    /**
     * Les graines actives uniquement (pas terminées).
     */
    @Query("SELECT * FROM graines WHERE estActif = 1 ORDER BY dateAjout DESC")
    fun getGrainesActives(): Flow<List<GraineEntity>>
    
    /**
     * Les graines dont la quantité est faible (< :seuil).
     * Utile pour afficher une alerte "à racheter".
     */
    @Query("SELECT * FROM graines WHERE quantite < :seuil AND estActif = 1 ORDER BY quantite ASC")
    suspend fun getGrainesQuantiteFaible(seuil: Int = 5): List<GraineEntity>
    
    /**
     * Compte total de graines actives.
     */
    @Query("SELECT COUNT(*) FROM graines WHERE estActif = 1")
    fun countGrainesActives(): Flow<Int>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Insère une nouvelle graine.
     * Retourne l'ID auto-généré.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGraine(graine: GraineEntity): Long
    
    /**
     * Insère plusieurs graines d'un coup.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGraines(graines: List<GraineEntity>)
    
    /**
     * Met à jour une graine existante.
     */
    @Update
    suspend fun updateGraine(graine: GraineEntity)
    
    /**
     * Supprime une graine.
     */
    @Delete
    suspend fun deleteGraine(graine: GraineEntity)
    
    /**
     * Supprime une graine par son ID.
     */
    @Query("DELETE FROM graines WHERE id = :id")
    suspend fun deleteGraineParId(id: Long)
    
    /**
     * Supprime toutes les graines (usage debug / reset).
     */
    @Query("DELETE FROM graines")
    suspend fun deleteAllGraines()
}
