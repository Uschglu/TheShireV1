package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les jeunes plants.
 * 
 * Toutes les opérations CRUD + quelques requêtes utiles pour l'UI.
 */
@Dao
interface JeunePlantDao {
    
    // ============================================================
    // LECTURE
    // ============================================================
    
    /**
     * Tous les jeunes plants, triés par date d'ajout (plus récents en premier).
     */
    @Query("SELECT * FROM jeunes_plants ORDER BY dateAjout DESC")
    fun getAllJeunesPlants(): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les jeunes plants synchrones (pour usage ponctuel hors Compose).
     */
    @Query("SELECT * FROM jeunes_plants ORDER BY dateAjout DESC")
    suspend fun getAllJeunesPlantsSync(): List<JeunePlantEntity>
    
    /**
     * Un jeune plant par son ID.
     */
    @Query("SELECT * FROM jeunes_plants WHERE id = :id")
    suspend fun getJeunePlantParId(id: Long): JeunePlantEntity?
    
    /**
     * Les jeunes plants d'un légume donné.
     */
    @Query("SELECT * FROM jeunes_plants WHERE legumeNom = :legumeNom ORDER BY dateAjout DESC")
    fun getJeunesPlantsPourLegume(legumeNom: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Les jeunes plants actifs uniquement (pas encore plantés).
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 ORDER BY dateAjout DESC")
    fun getJeunesPlantsActifs(): Flow<List<JeunePlantEntity>>
    
    /**
     * Les jeunes plants d'un stade donné.
     * Ex : tous les plants "Prêt à planter".
     */
    @Query("SELECT * FROM jeunes_plants WHERE stade = :stade AND estActif = 1 ORDER BY dateAjout DESC")
    fun getJeunesPlantsParStade(stade: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte total de jeunes plants actifs.
     */
    @Query("SELECT COUNT(*) FROM jeunes_plants WHERE estActif = 1")
    fun countJeunesPlantsActifs(): Flow<Int>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    /**
     * Insère un nouveau jeune plant.
     * Retourne l'ID auto-généré.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeunePlant(jeunePlant: JeunePlantEntity): Long
    
    /**
     * Insère plusieurs jeunes plants d'un coup.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeunesPlants(jeunesPlants: List<JeunePlantEntity>)
    
    /**
     * Met à jour un jeune plant existant.
     */
    @Update
    suspend fun updateJeunePlant(jeunePlant: JeunePlantEntity)
    
    /**
     * Supprime un jeune plant.
     */
    @Delete
    suspend fun deleteJeunePlant(jeunePlant: JeunePlantEntity)
    
    /**
     * Supprime un jeune plant par son ID.
     */
    @Query("DELETE FROM jeunes_plants WHERE id = :id")
    suspend fun deleteJeunePlantParId(id: Long)
    
    /**
     * Supprime tous les jeunes plants (usage debug / reset).
     */
    @Query("DELETE FROM jeunes_plants")
    suspend fun deleteAllJeunesPlants()
}
