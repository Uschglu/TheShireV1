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
 * 
 * Deux usages :
 *  - Onglet "Plants" de Stocks : getJeunesPlantsActifs(), getJeunesPlantsParStade()…
 *  - Onglet "Semis" de Jardin : getSemisActifs(), getSemisParEtape()…
 */
@Dao
interface JeunePlantDao {
    
    // ============================================================
    // LECTURE — générique
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
    // LECTURE — spécifique Semis (Jardin)
    // ============================================================
    
    /**
     * Tous les semis actifs (dans le cycle, pas encore plantés),
     * triés par date de semis croissante (plus anciens en premier —
     * ce sont ceux qui demandent le plus d'attention).
     * 
     * Si dateSemis est null, on retombe sur dateAjout pour le tri.
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisActifs(): Flow<List<JeunePlantEntity>>
    
    /**
     * Les semis actifs d'une étape donnée.
     * Ex : tous les semis qui sont actuellement à l'étape "Levée".
     */
    @Query("SELECT * FROM jeunes_plants WHERE estActif = 1 AND stade = :stade ORDER BY COALESCE(dateSemis, dateAjout) ASC")
    fun getSemisParEtape(stade: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les semis plantés (fin de cycle), triés par date de plantation décroissante.
     * Utile pour une vue "historique" V2.
     */
    @Query("SELECT * FROM jeunes_plants WHERE stade = :etapePlante ORDER BY datePlantation DESC")
    fun getSemisPlantes(etapePlante: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs (dans le cycle).
     * Utile pour afficher un badge sur l'onglet Semis.
     */
    @Query("SELECT COUNT(*) FROM jeunes_plants WHERE estActif = 1")
    fun countSemisActifs(): Flow<Int>
    
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
