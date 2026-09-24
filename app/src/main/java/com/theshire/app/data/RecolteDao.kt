package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les récoltes enregistrées (4e onglet de Stocks).
 *
 * Utilisé par :
 *  - L'onglet "Récoltes" de Stocks : liste de toutes les récoltes
 *  - La fiche "Récolte" : détail + modification + suppression
 *  - La récolte automatique depuis une culture (CultureRepository)
 *  - Les statistiques futures (poids total par légume, par année…)
 *
 * Toutes les quantités sont en kilogrammes (double).
 *
 * ⚠️ MODE PROJECTION vs RÉEL :
 *    Le champ `estProjection` permet de filtrer les récoltes selon le mode
 *    actif (comme pour CultureEntity). Les requêtes de base retournent TOUT ;
 *    les variantes "ParMode" filtrent sur `estProjection = :estProjection`.
 */
@Dao
interface RecolteDao {
    
    // ============================================================
    // LECTURE — générique
    // ============================================================
    
    @Query("SELECT * FROM recoltes ORDER BY dateRecolte DESC")
    fun getAllRecoltes(): Flow<List<RecolteEntity>>
    
    @Query("SELECT * FROM recoltes ORDER BY dateRecolte DESC")
    suspend fun getAllRecoltesSync(): List<RecolteEntity>
    
    @Query("SELECT * FROM recoltes WHERE id = :id")
    suspend fun getRecolteParId(id: Long): RecolteEntity?
    
    @Query("SELECT COUNT(*) FROM recoltes")
    fun countRecoltes(): Flow<Int>
    
    // ============================================================
    // LECTURE — par mode (projection / réel)
    // ============================================================
    
    /**
     * Toutes les récoltes d'un mode donné, triées par date décroissante.
     * 
     * @param estProjection true → récoltes "projetées", false → récoltes "réelles"
     */
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE estProjection = :estProjection " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesParMode(estProjection: Boolean): Flow<List<RecolteEntity>>
    
    /**
     * Compte les récoltes d'un mode donné.
     */
    @Query("SELECT COUNT(*) FROM recoltes WHERE estProjection = :estProjection")
    fun countRecoltesParMode(estProjection: Boolean): Flow<Int>
    
    // ============================================================
    // LECTURE — par légume
    // ============================================================
    
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE legumeNom = :legumeNom " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesPourLegume(legumeNom: String): Flow<List<RecolteEntity>>
    
    /**
     * Poids total récolté pour un légume donné (toutes récoltes confondues).
     */
    @Query(
        "SELECT COALESCE(SUM(poidsKg), 0.0) FROM recoltes " +
        "WHERE legumeNom = :legumeNom"
    )
    fun getPoidsTotalPourLegume(legumeNom: String): Flow<Double>
    
    /**
     * Poids total récolté tous légumes confondus.
     */
    @Query("SELECT COALESCE(SUM(poidsKg), 0.0) FROM recoltes")
    fun getPoidsTotalGlobal(): Flow<Double>
    
    /**
     * Poids total récolté pour un mode donné.
     */
    @Query(
        "SELECT COALESCE(SUM(poidsKg), 0.0) FROM recoltes " +
        "WHERE estProjection = :estProjection"
    )
    fun getPoidsTotalParMode(estProjection: Boolean): Flow<Double>
    
    // ============================================================
    // LECTURE — par culture d'origine
    // ============================================================
    
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE cultureId = :cultureId " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesPourCulture(cultureId: Long): Flow<List<RecolteEntity>>
    
    // ============================================================
    // LECTURE — par période
    // ============================================================
    
    /**
     * Récoltes effectuées entre deux timestamps (bornes incluses).
     */
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE dateRecolte BETWEEN :debut AND :fin " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesEntreDates(debut: Long, fin: Long): Flow<List<RecolteEntity>>
    
    /**
     * Poids total récolté entre deux timestamps.
     */
    @Query(
        "SELECT COALESCE(SUM(poidsKg), 0.0) FROM recoltes " +
        "WHERE dateRecolte BETWEEN :debut AND :fin"
    )
    fun getPoidsTotalEntreDates(debut: Long, fin: Long): Flow<Double>
    
    // ============================================================
    // LECTURE — par source
    // ============================================================
    
    /**
     * Récoltes liées à une culture (générées automatiquement).
     */
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE cultureId IS NOT NULL " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesLieesAUneCulture(): Flow<List<RecolteEntity>>
    
    /**
     * Récoltes ajoutées manuellement (sans culture liée).
     */
    @Query(
        "SELECT * FROM recoltes " +
        "WHERE cultureId IS NULL " +
        "ORDER BY dateRecolte DESC"
    )
    fun getRecoltesManuelles(): Flow<List<RecolteEntity>>
    
    // ============================================================
    // ÉCRITURE
    // ============================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecolte(recolte: RecolteEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoltes(recoltes: List<RecolteEntity>)
    
    @Update
    suspend fun updateRecolte(recolte: RecolteEntity)
    
    @Delete
    suspend fun deleteRecolte(recolte: RecolteEntity)
    
    @Query("DELETE FROM recoltes WHERE id = :id")
    suspend fun deleteRecolteParId(id: Long)
    
    /**
     * Supprime toutes les récoltes liées à une culture.
     * 
     * ⚠️ Normalement on ne supprime JAMAIS une récolte quand on supprime
     *    une culture (on veut garder l'historique). Cette méthode est là
     *    pour un cas exceptionnel de reset.
     */
    @Query("DELETE FROM recoltes WHERE cultureId = :cultureId")
    suspend fun deleteRecoltesPourCulture(cultureId: Long)
    
    /**
     * Supprime toutes les récoltes d'un mode donné.
     * (usage debug / reset sélectif)
     */
    @Query("DELETE FROM recoltes WHERE estProjection = :estProjection")
    suspend fun deleteRecoltesParMode(estProjection: Boolean)
    
    @Query("DELETE FROM recoltes")
    suspend fun deleteAllRecoltes()
    
    // ============================================================
    // OPÉRATIONS MÉTIER
    // ============================================================
    
    /**
     * Met à jour uniquement le poids d'une récolte.
     */
    @Query("UPDATE recoltes SET poidsKg = :poidsKg WHERE id = :id")
    suspend fun updatePoids(id: Long, poidsKg: Double)
    
    /**
     * Met à jour uniquement les notes d'une récolte.
     */
    @Query("UPDATE recoltes SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)
    
    /**
     * Bascule le mode d'une récolte (utile pour migration ou correction).
     */
    @Query("UPDATE recoltes SET estProjection = :estProjection WHERE id = :id")
    suspend fun updateMode(id: Long, estProjection: Boolean)
    
    // ============================================================
    // STATISTIQUES (préparation pour plus tard)
    // ============================================================
    
    /**
     * Récupère la liste des légumes ayant au moins une récolte.
     */
    @Query("SELECT DISTINCT legumeNom FROM recoltes ORDER BY legumeNom")
    suspend fun getLegumesRecoltes(): List<String>
    
    /**
     * Récupère le poids total groupé par légume.
     * Utile pour un futur écran de stats.
     */
    @Query(
        "SELECT legumeNom, SUM(poidsKg) as total " +
        "FROM recoltes " +
        "GROUP BY legumeNom " +
        "ORDER BY total DESC"
    )
    suspend fun getPoidsTotalParLegume(): List<LegumePoidsTotal>
    
    /**
     * Récupère le poids total groupé par légume, pour un mode donné.
     */
    @Query(
        "SELECT legumeNom, SUM(poidsKg) as total " +
        "FROM recoltes " +
        "WHERE estProjection = :estProjection " +
        "GROUP BY legumeNom " +
        "ORDER BY total DESC"
    )
    suspend fun getPoidsTotalParLegumeParMode(estProjection: Boolean): List<LegumePoidsTotal>
}

/**
 * Résultat d'une requête de statistiques : poids total par légume.
 */
data class LegumePoidsTotal(
    val legumeNom: String,
    val total: Double
)
