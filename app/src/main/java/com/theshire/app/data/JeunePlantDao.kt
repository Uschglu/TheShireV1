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
 * Trois usages :
 *  - Onglet "Plants" de Stocks : inventaire global (tous modes)
 *  - Onglet "Semis" de Jardin : filtré par mode actif (projection / réel)
 *  - Historique V2 : semis plantés
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
    // LECTURE — spécifique Semis (Jardin), tous modes
    // ============================================================
    
    /**
     * Tous les semis actifs, tous modes confondus.
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisActifs(): Flow<List<JeunePlantEntity>>
    
    /**
     * Les semis actifs d'une étape donnée, tous modes confondus.
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 AND stade = :stade ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisParEtape(stade: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les semis plantés (fin de cycle), tous modes confondus.
     */
    @Query("SELECT * FROM jeunes_plants WHERE stade = :etapePlante ORDER BY datePlantation DESC")
    fun getSemisPlantes(etapePlante: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs (tous modes).
     */
    @Query("SELECT COUNT(*) FROM jeunes_plants WHERE estActif = 1")
    fun countSemisActifs(): Flow<Int>
    
    // ============================================================
    // LECTURE — Semis filtrés par mode (projection / réel)
    // ============================================================
    
    /**
     * Semis actifs filtrés par mode.
     * 
     * @param estProjection true = uniquement les semis en projection
     *                      false = uniquement les semis en mode réel
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE estActif = 1 AND estProjection = :estProjection " +
        "ORDER BY COALESCE(dateSemis, dateAjout) ASC"
    )
    fun getSemisActifsFiltres(estProjection: Boolean): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs filtrés par mode.
     */
    @Query(
        "SELECT COUNT(*) FROM jeunes_plants " +
        "WHERE estActif = 1 AND estProjection = :estProjection"
    )
    fun countSemisActifsFiltres(estProjection: Boolean): Flow<Int>
    
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
