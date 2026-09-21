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
 * Deux usages :
 *  - Onglet "Plants" de Stocks
 *  - Onglet "Semis" de Jardin
 */
@Dao
interface JeunePlantDao {
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    @Query("SELECT * FROM jeunes_plants ORDER BY dateAjout DESC")
    fun getAllJeunesPlants(): Flow<List<JeunePlantEntity>>
    
    @Query("SELECT * FROM jeunes_plants ORDER BY dateAjout DESC")
    suspend fun getAllJeunesPlantsSync(): List<JeunePlantEntity>
    
    @Query("SELECT * FROM jeunes_plants WHERE id = :id")
    suspend fun getJeunePlantParId(id: Long): JeunePlantEntity?
    
    @Query("SELECT * FROM jeunes_plants WHERE legumeNom = :legumeNom ORDER BY dateAjout DESC")
    fun getJeunesPlantsPourLegume(legumeNom: String): Flow<List<JeunePlantEntity>>
    
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 ORDER BY dateAjout DESC")
    fun getJeunesPlantsActifs(): Flow<List<JeunePlantEntity>>
    
    @Query("SELECT * FROM jeunes_plants WHERE stade = :stade AND estActif = 1 ORDER BY dateAjout DESC")
    fun getJeunesPlantsParStade(stade: String): Flow<List<JeunePlantEntity>>
    
    @Query("SELECT COUNT(*) FROM jeunes_plants WHERE estActif = 1")
    fun countJeunesPlantsActifs(): Flow<Int>
    
    // ============================================================
    // LECTURE — spécifique Semis (Jardin)
    // ============================================================
    
    /**
     * Tous les semis actifs (dans le cycle, pas encore plantés),
     * triés par date de semis croissante (plus anciens en premier).
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisActifs(): Flow<List<JeunePlantEntity>>
    
    /**
     * Les semis actifs d'une étape donnée.
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 AND stade = :stade ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisParEtape(stade: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les semis plantés (fin de cycle), triés par date de plantation décroissante.
     */
    @Query("SELECT * FROM jeunes_plants WHERE stade = :etapePlante ORDER BY datePlantation DESC")
    fun getSemisPlantes(etapePlante: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs (dans le cycle).
     */
    @Query("SELECT COUNT(*) FROM jeunes_plants WHERE estActif = 1")
    fun countSemisActifs(): Flow<Int>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeunePlant(jeunePlant: JeunePlantEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeunesPlants(jeunesPlants: List<JeunePlantEntity>)
    
    @Update
    suspend fun updateJeunePlant(jeunePlant: JeunePlantEntity)
    
    @Delete
    suspend fun deleteJeunePlant(jeunePlant: JeunePlantEntity)
    
    @Query("DELETE FROM jeunes_plants WHERE id = :id")
    suspend fun deleteJeunePlantParId(id: Long)
    
    @Query("DELETE FROM jeunes_plants")
    suspend fun deleteAllJeunesPlants()
}
