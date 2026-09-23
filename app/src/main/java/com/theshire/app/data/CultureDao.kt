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
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE plancheId = :plancheId AND estActive = 1 " +
        "ORDER BY carreId, caseNumero"
    )
    fun getCulturesActivesPourPlanche(plancheId: Long): Flow<List<CultureEntity>>
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId AND estActive = 1 " +
        "ORDER BY caseNumero"
    )
    fun getCulturesActivesPourCarre(carreId: Long): Flow<List<CultureEntity>>
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId AND caseNumero = :caseNumero AND estActive = 1 " +
        "LIMIT 1"
    )
    suspend fun getCultureActiveDansCase(carreId: Long, caseNumero: Int): CultureEntity?
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE contenantId = :contenantId AND estActive = 1 " +
        "ORDER BY emplacementNumero"
    )
    fun getCulturesActivesPourContenant(contenantId: Long): Flow<List<CultureEntity>>
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE contenantId = :contenantId AND emplacementNumero = :emplacementNumero AND estActive = 1 " +
        "LIMIT 1"
    )
    suspend fun getCultureActiveDansEmplacement(contenantId: Long, emplacementNumero: Int): CultureEntity?
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE typeEmplacement = :type AND estActive = 1 " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesParType(type: String): Flow<List<CultureEntity>>
    
    // ============================================================
    // LECTURE — par plante
    // ============================================================
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE legumeNom = :legumeNom " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesPourLegume(legumeNom: String): Flow<List<CultureEntity>>
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE legumeNom = :legumeNom AND estActive = 1 " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesPourLegume(legumeNom: String): Flow<List<CultureEntity>>
    
    @Query(
        "SELECT COUNT(*) FROM cultures " +
        "WHERE legumeNom = :legumeNom AND estActive = 1"
    )
    fun countCulturesActivesPourLegume(legumeNom: String): Flow<Int>
    
    // ============================================================
    // LECTURE — par mode (projection / réel)
    // ============================================================
    
    @Query(
        "SELECT * FROM cultures " +
        "WHERE estActive = 1 AND estProjection = :estProjection " +
        "ORDER BY datePlantation DESC"
    )
    fun getCulturesActivesFiltrees(estProjection: Boolean): Flow<List<CultureEntity>>
    
    // ============================================================
    // LECTURE SYNCHRONE — filtrée par mode (pour usage UI)
    // ============================================================
    
    /**
     * Récupère toutes les cultures actives d'un carré, filtrées par mode.
     * Version synchrone, utile pour appeler depuis un scope.launch en UI.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId AND estActive = 1 AND estProjection = :estProjection"
    )
    suspend fun getCulturesActivesPourCarreParMode(
        carreId: Long,
        estProjection: Boolean
    ): List<CultureEntity>
    
    /**
     * Récupère toutes les cultures actives d'un contenant, filtrées par mode.
     * Version synchrone.
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE contenantId = :contenantId AND estActive = 1 AND estProjection = :estProjection"
    )
    suspend fun getCulturesActivesPourContenantParMode(
        contenantId: Long,
        estProjection: Boolean
    ): List<CultureEntity>
    
    // ============================================================
    // LECTURE — cultures identiques (détection "m² entier")
    // ============================================================
    
    /**
     * Récupère toutes les cultures actives d'un carré ayant la même plante,
     * la même variété et la même date de plantation que la référence.
     * 
     * Utilisé pour détecter si un carré a été rempli "d'un coup"
     * (les 9 cases ont la même plante à la même date).
     */
    @Query(
        "SELECT * FROM cultures " +
        "WHERE carreId = :carreId " +
        "AND estActive = 1 " +
        "AND legumeNom = :legumeNom " +
        "AND COALESCE(varieteNom, '') = COALESCE(:varieteNom, '') " +
        "AND datePlantation = :datePlantation"
    )
    suspend fun getCulturesIdentiquesDansCarre(
        carreId: Long,
        legumeNom: String,
        varieteNom: String?,
        datePlantation: Long
    ): List<CultureEntity>
    
    // ============================================================
    // LECTURE — historique / stats
    // ============================================================
    
    @Query("SELECT COUNT(*) FROM cultures")
    fun countTotalCultures(): Flow<Int>
    
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
    
    @Query("DELETE FROM cultures WHERE carreId = :carreId")
    suspend fun deleteCulturesPourCarre(carreId: Long)
    
    @Query("DELETE FROM cultures WHERE contenantId = :contenantId")
    suspend fun deleteCulturesPourContenant(contenantId: Long)
    
    @Query("DELETE FROM cultures WHERE plancheId = :plancheId")
    suspend fun deleteCulturesPourPlanche(plancheId: Long)
    
    @Query("DELETE FROM cultures")
    suspend fun deleteAllCultures()
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    @Query(
        "UPDATE cultures " +
        "SET estActive = 0, dateRecolteReelle = :dateRecolte " +
        "WHERE id = :id"
    )
    suspend fun terminerCulture(id: Long, dateRecolte: Long?)
    
    @Query("UPDATE cultures SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)
    
    /**
     * Termine plusieurs cultures d'un coup (pour "vider tout le m²").
     */
    @Query(
        "UPDATE cultures " +
        "SET estActive = 0, dateRecolteReelle = :dateRecolte " +
        "WHERE id IN (:ids)"
    )
    suspend fun terminerCultures(ids: List<Long>, dateRecolte: Long?)
}
