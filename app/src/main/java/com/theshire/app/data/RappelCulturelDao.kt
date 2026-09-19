package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les rappels culturaux automatiques.
 * 
 * Fournit les opérations CRUD + requêtes spécifiques :
 * - Rappels d'un carré (pleine terre)
 * - Rappels d'un contenant urbain
 * - Rappels d'une plante donnée
 * - Rappels dans une période donnée (calendrier)
 * - Rappels en retard
 * - Rappels à notifier
 */
@Dao
interface RappelCulturelDao {
    
    // ===== INSERTION =====
    
    @Insert
    suspend fun insertRappel(rappel: RappelCulturelEntity): Long
    
    @Insert
    suspend fun insertRappels(rappels: List<RappelCulturelEntity>)
    
    // ===== MISE À JOUR =====
    
    @Update
    suspend fun updateRappel(rappel: RappelCulturelEntity)
    
    // ===== SUPPRESSION =====
    
    @Delete
    suspend fun deleteRappel(rappel: RappelCulturelEntity)
    
    @Query("DELETE FROM rappels_culturels WHERE id = :id")
    suspend fun deleteRappelParId(id: Long)
    
    // --- Pleine terre ---
    
    @Query("DELETE FROM rappels_culturels WHERE carreId = :carreId AND caseNumero = :caseNumero")
    suspend fun deleteRappelsPourCase(carreId: Long, caseNumero: Int)
    
    @Query("DELETE FROM rappels_culturels WHERE carreId = :carreId")
    suspend fun deleteRappelsPourCarre(carreId: Long)
    
    @Query("DELETE FROM rappels_culturels WHERE plancheId = :plancheId")
    suspend fun deleteRappelsPourPlanche(plancheId: Long)
    
    // --- Urbain ---
    
    @Query("DELETE FROM rappels_culturels WHERE contenantId = :contenantId AND emplacementNumero = :emplacementNumero")
    suspend fun deleteRappelsPourEmplacement(contenantId: Long, emplacementNumero: Int)
    
    @Query("DELETE FROM rappels_culturels WHERE contenantId = :contenantId")
    suspend fun deleteRappelsPourContenant(contenantId: Long)
    
    // ===== LECTURE =====
    
    @Query("SELECT * FROM rappels_culturels ORDER BY dateIdeale ASC")
    fun getAllRappels(): Flow<List<RappelCulturelEntity>>
    
    @Query("SELECT * FROM rappels_culturels WHERE estActif = 1 ORDER BY dateIdeale ASC")
    fun getRappelsActifs(): Flow<List<RappelCulturelEntity>>
    
    @Query("SELECT * FROM rappels_culturels WHERE estActif = 1 AND estTermine = 0 ORDER BY dateIdeale ASC")
    fun getRappelsEnCours(): Flow<List<RappelCulturelEntity>>
    
    @Query("SELECT * FROM rappels_culturels WHERE id = :id")
    suspend fun getRappelParId(id: Long): RappelCulturelEntity?
    
    // --- Pleine terre ---
    
    @Query("SELECT * FROM rappels_culturels WHERE carreId = :carreId AND caseNumero = :caseNumero AND estActif = 1")
    suspend fun getRappelsPourCase(carreId: Long, caseNumero: Int): List<RappelCulturelEntity>
    
    @Query("SELECT * FROM rappels_culturels WHERE carreId = :carreId AND estActif = 1 ORDER BY dateIdeale ASC")
    fun getRappelsPourCarre(carreId: Long): Flow<List<RappelCulturelEntity>>
    
    @Query("SELECT * FROM rappels_culturels WHERE plancheId = :plancheId AND estActif = 1 ORDER BY dateIdeale ASC")
    fun getRappelsPourPlanche(plancheId: Long): Flow<List<RappelCulturelEntity>>
    
    // --- Urbain ---
    
    @Query("SELECT * FROM rappels_culturels WHERE contenantId = :contenantId AND emplacementNumero = :emplacementNumero AND estActif = 1")
    suspend fun getRappelsPourEmplacement(contenantId: Long, emplacementNumero: Int): List<RappelCulturelEntity>
    
    @Query("SELECT * FROM rappels_culturels WHERE contenantId = :contenantId AND estActif = 1 ORDER BY dateIdeale ASC")
    fun getRappelsPourContenant(contenantId: Long): Flow<List<RappelCulturelEntity>>
    
    // --- Période (calendrier) ---
    
    /**
     * Récupère les rappels dont la période couvre la date donnée.
     */
    @Query("""
        SELECT * FROM rappels_culturels 
        WHERE estActif = 1 
        AND dateDebut <= :dateFin 
        AND dateFin >= :dateDebut
        ORDER BY dateIdeale ASC
    """)
    suspend fun getRappelsEntreDates(dateDebut: Long, dateFin: Long): List<RappelCulturelEntity>
    
    /**
     * Version Flow de la requête ci-dessus.
     */
    @Query("""
        SELECT * FROM rappels_culturels 
        WHERE estActif = 1 
        AND dateDebut <= :dateFin 
        AND dateFin >= :dateDebut
        ORDER BY dateIdeale ASC
    """)
    fun getRappelsEntreDatesFlow(dateDebut: Long, dateFin: Long): Flow<List<RappelCulturelEntity>>
    
    /**
     * Récupère les rappels en retard.
     */
    @Query("""
        SELECT * FROM rappels_culturels 
        WHERE estActif = 1 
        AND estTermine = 0 
        AND dateFin < :dateActuelle
        ORDER BY dateIdeale ASC
    """)
    suspend fun getRappelsEnRetard(dateActuelle: Long): List<RappelCulturelEntity>
    
    /**
     * Récupère les rappels à notifier.
     */
    @Query("""
        SELECT * FROM rappels_culturels 
        WHERE estActif = 1 
        AND estTermine = 0 
        AND notificationEnvoyee = 0
        AND dateDebut <= :dateActuelle
        ORDER BY dateIdeale ASC
    """)
    suspend fun getRappelsANotifier(dateActuelle: Long): List<RappelCulturelEntity>
    
    /**
     * Récupère les rappels d'une plante donnée.
     */
    @Query("SELECT * FROM rappels_culturels WHERE legumeNom = :legumeNom ORDER BY dateIdeale DESC")
    fun getRappelsPourLegume(legumeNom: String): Flow<List<RappelCulturelEntity>>
    
    // ===== COMPTAGE =====
    
    @Query("SELECT COUNT(*) FROM rappels_culturels WHERE estActif = 1 AND estTermine = 0")
    fun countRappelsEnCours(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM rappels_culturels")
    suspend fun countAllRappels(): Int
}
