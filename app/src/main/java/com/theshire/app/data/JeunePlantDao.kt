package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les jeunes plants et semis.
 * 
 * Trois usages :
 *  - Onglet "Plants" de Stocks : inventaire global (2 catégories, tous modes)
 *  - Onglet "Semis" de Jardin : catégorie "Semis", filtré par mode (projection / réel)
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
    // LECTURE — générique filtrée par catégorie
    // ============================================================
    
    /**
     * Tous les éléments actifs d'une catégorie donnée.
     * @param categorie "Semis" ou "JeunePlant"
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE estActif = 1 AND categorie = :categorie " +
        "ORDER BY dateAjout DESC"
    )
    fun getActifsParCategorie(categorie: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les éléments d'une catégorie (actifs + inactifs).
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE categorie = :categorie " +
        "ORDER BY dateAjout DESC"
    )
    fun getTousParCategorie(categorie: String): Flow<List<JeunePlantEntity>>
    
    // ============================================================
    // LECTURE — spécifique Semis (Jardin), tous modes
    // ============================================================
    
    /**
     * Tous les semis actifs, tous modes confondus.
     * (catégorie = "Semis")
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE estActif = 1 AND categorie = :categorie " +
        "ORDER BY COALESCE(dateSemis, dateAjout) ASC"
    )
    fun getSemisActifsParCategorie(categorie: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Les semis actifs d'une étape donnée, tous modes confondus.
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE estActif = 1 AND categorie = :categorie AND stade = :stade " +
        "ORDER BY COALESCE(dateSemis, dateAjout) ASC"
    )
    fun getSemisParEtapeEtCategorie(categorie: String, stade: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Tous les semis plantés (fin de cycle), tous modes confondus.
     */
    @Query("SELECT * FROM jeunes_plants WHERE stade = :etapePlante ORDER BY datePlantation DESC")
    fun getSemisPlantes(etapePlante: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs (tous modes) d'une catégorie.
     */
    @Query(
        "SELECT COUNT(*) FROM jeunes_plants " +
        "WHERE estActif = 1 AND categorie = :categorie"
    )
    fun countSemisActifsParCategorie(categorie: String): Flow<Int>
    
    // ============================================================
    // LECTURE — Semis filtrés par mode (projection / réel)
    // ============================================================
    
    /**
     * Semis actifs filtrés par mode ET catégorie (usage principal du Jardin).
     * 
     * @param estProjection true = uniquement les semis en projection
     *                      false = uniquement les semis en mode réel
     * @param categorie "Semis" pour l'onglet Semis du Jardin
     */
    @Query(
        "SELECT * FROM jeunes_plants " +
        "WHERE estActif = 1 AND estProjection = :estProjection AND categorie = :categorie " +
        "ORDER BY COALESCE(dateSemis, dateAjout) ASC"
    )
    fun getSemisActifsFiltres(estProjection: Boolean, categorie: String): Flow<List<JeunePlantEntity>>
    
    /**
     * Compte des semis actifs filtrés par mode ET catégorie.
     */
    @Query(
        "SELECT COUNT(*) FROM jeunes_plants " +
        "WHERE estActif = 1 AND estProjection = :estProjection AND categorie = :categorie"
    )
    fun countSemisActifsFiltres(estProjection: Boolean, categorie: String): Flow<Int>
    
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
