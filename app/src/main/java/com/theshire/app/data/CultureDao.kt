package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les cultures actives.
 *
 * Trois usages principaux :
 *  - Pleine terre : cultures liées à une planche / un carré / une case
 *  - Urbain      : cultures liées à un contenant / un emplacement
 *  - Historique  : cultures terminées (récoltées ou retirées)
 *
 * Les cultures sont créées au moment de la plantation, et passent à
 * `estActive = false` une fois récoltées ou retirées.
 */
@Dao
interface CultureDao {
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    @Query("SELECT * FROM cultures ORDER BY datePlantation DESC")
    fun getAllCultures(): Flow<List<CultureEntity>>
    
    @Query("SELECT * FROM cultures ORDER BY datePlantation DESC")
    suspend fun getAllCulturesSync(): List<CultureEntity>
    
    @Query("SELECT * FROM cultures WHERE id = :id")
    suspend fun getCultureParId(id: Long): CultureEntity?
    
    @Query("SELECT * FROM cultures WHERE estActive = 1 ORDER BY datePlantation DESC")
    fun getCulturesActives(): Flow<List<CultureEntity>>
    
    @Query("SELECT * FROM cultures WHERE estActive = 0 ORDER BY dateRecolteReelle DESC, datePlantation DESC")
    fun getCulturesTerminees(): Flow<List<CultureEntity>>
    
    @Query("SELECT COUNT(*) FROM cultures WHERE estActive = 1")
    fun countCulturesActives(): Flow<Int>
    
    // ============================================================
    // LECTURE — par localisation
    // ============================================================
    
    /**
     * Cultures liées à une planche (toutes les cases).
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE plancheId = :plancheId AND estActive = 1 " +
        "ORDER BY carreId, caseNumero"
    )
    fun getCulturesActivesPourPlanche(plancheId: Long): Flow<List<CultureEntity>>
    
    /**
     * Cultures liées à un carré précis.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId AND estActive = 1 " +
        "ORDER BY caseNumero"
    )
    fun getCulturesActivesPourCarre(carreId: Long): Flow<List<CultureEntity>>
    
    /**
     * Culture active dans une case précise d'un carré.
     * Retourne null si la case est vide.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId AND caseNumero = :caseNumero AND estActive = 1 " +
        "LIMIT 1"
    )
    suspend fun getCultureActiveDansCase(carreId: Long, caseNumero: Int): CultureEntity?
    
    /**
     * Cultures liées à un contenant (tous les emplacements).
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE contenantId = :contenantId AND estActive = 1 " +
        "ORDER BY emplacementNumero"
    )
    fun getCulturesActivesPourContenant(contenantId: Long): Flow<List<CultureEntity>>
    
    /**
     * Culture active dans un emplacement précis d'un contenant.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE contenantId = :contenantId AND emplacementNumero = :emplacementNumero AND estActive = 1 " +
        "LIMIT 1"
    )
    suspend fun getCultureActiveDansEmplacement(contenantId: Long, emplacementNumero: Int): CultureEntity?
    
    /**
     * Toutes les cultures actives liées à la pleine terre.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE typeEmplacement = :type AND estActive = 1 " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesParType(type: String): Flow<List<CultureEntity>>
    
    // ============================================================
    // LECTURE — par plante
    // ============================================================
    
    /**
     * Toutes les cultures d'un légume donné (actives + terminées).
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE legumeNom = :legumeNom " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesPourLegume(legumeNom: String): Flow<List<CultureEntity>>
    
    /**
     * Cultures actives d'un légume donné.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE legumeNom = :legumeNom AND estActive = 1 " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesPourLegume(legumeNom: String): Flow<List<CultureEntity>>
    
    /**
     * Compte les cultures actives d'un légume donné (utile pour stats).
     */
    @Query(
        "SELECT COUNT(*) FROM cultures " +
        "WHERE legumeNom = :legumeNom AND estActive = 1"
    )
    fun countCulturesActivesPourLegume(legumeNom: String): Flow<Int>
    
    // ============================================================
    // LECTURE — par mode (projection / réel)
    // ============================================================
    
    /**
     * Cultures actives filtrées par mode.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE estActive = 1 AND estProjection = :estProjection " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesFiltrees(estProjection: Boolean): Flow<List<CultureEntity>>
    
    // ============================================================
    // LECTURE — historique / stats
    // ============================================================
    
    /**
     * Nombre total de cultures créées (actives + terminées).
     */
    @Query("SELECT COUNT(*) FROM cultures")
    fun countTotalCultures(): Flow<Int>
    
    /**
     * Tous les noms de légumes cultivés (distinct).
     */
    @Query("SELECT DISTINCT legumeNom FROM cultures ORDER BY legumeNom")
    suspend fun getLegumesCultives(): List<String>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCulture(culture: CultureEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultures(cultures: List<CultureEntity>)
    
    @Update
    suspend fun updateCulture(culture: CultureEntity)
    
    @Delete
    suspend fun deleteCulture(culture: CultureEntity)
    
    @Query("DELETE FROM cultures WHERE id = :id")
    suspend fun deleteCultureParId(id: Long)
    
    /**
     * Supprime toutes les cultures d'un carré.
     * (utilisé quand on vide une planche entière, par exemple)
     */
    @Query("DELETE FROM cultures WHERE carreId = :carreId")
    suspend fun deleteCulturesPourCarre(carreId: Long)
    
    /**
     * Supprime toutes les cultures d'un contenant.
     * (utilisé quand on supprime un contenant)
     */
    @Query("DELETE FROM cultures WHERE contenantId = :contenantId")
    suspend fun deleteCulturesPourContenant(contenantId: Long)
    
    /**
     * Supprime toutes les cultures d'une planche.
     */
    @Query("DELETE FROM cultures WHERE plancheId = :plancheId")
    suspend fun deleteCulturesPourPlanche(plancheId: Long)
    
    @Query("DELETE FROM cultures")
    suspend fun deleteAllCultures()
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    /**
     * Marque une culture comme terminée (récoltée ou retirée).
     * 
     * @param id ID de la culture
     * @param dateRecolte timestamp de la récolte (null si retrait sans récolte)
     */
    @Query(
        "UPDATE cultures " +
        "SET estActive = 0, dateRecolteReelle = :dateRecolte " +
        "WHERE id = :id"
    )
    suspend fun terminerCulture(id: Long, dateRecolte: Long?)
    
    /**
     * Met à jour uniquement les notes d'une culture.
     */
    @Query("UPDATE cultures SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)
}
