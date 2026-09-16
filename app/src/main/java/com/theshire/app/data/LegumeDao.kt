package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LegumeDao {
    
    // ===== LÉGUMES =====
    
    /**
     * Insertion avec IGNORE : si un légume avec le même nom existe déjà
     * (contrainte unique sur `nom`), l'insertion est silencieusement ignorée.
     * Évite les doublons même en cas d'appels concurrents.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLegume(legume: LegumeEntity): Long
    
    @Update
    suspend fun updateLegume(legume: LegumeEntity)
    
    @Delete
    suspend fun deleteLegume(legume: LegumeEntity)
    
    @Query("SELECT * FROM legumes ORDER BY nom ASC")
    fun getAllLegumes(): Flow<List<LegumeEntity>>
    
    @Query("SELECT * FROM legumes WHERE id = :id")
    suspend fun getLegumeById(id: Long): LegumeEntity?
    
    @Query("SELECT * FROM legumes WHERE nom = :nom")
    suspend fun getLegumeByNom(nom: String): LegumeEntity?
    
    @Query("SELECT COUNT(*) FROM legumes")
    suspend fun countLegumes(): Int
    
    @Query("DELETE FROM legumes WHERE id = :id")
    suspend fun deleteLegumeById(id: Long)
    
    /**
     * Supprime les doublons : garde l'entrée avec l'ID le plus petit,
     * supprime toutes les autres ayant le même nom.
     */
    @Query("""
        DELETE FROM legumes 
        WHERE id NOT IN (
            SELECT MIN(id) FROM legumes GROUP BY nom
        )
    """)
    suspend fun supprimerDoublonsLegumes()
    
    // ===== VARIÉTÉS =====
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVariete(variete: VarieteEntity): Long
    
    @Update
    suspend fun updateVariete(variete: VarieteEntity)
    
    @Delete
    suspend fun deleteVariete(variete: VarieteEntity)
    
    @Query("SELECT * FROM varietes WHERE legumeParent = :legumeNom")
    fun getVarietesForLegume(legumeNom: String): Flow<List<VarieteEntity>>
    
    @Query("SELECT * FROM varietes WHERE legumeParent = :legumeNom")
    fun getVarietesForLegumeSync(legumeNom: String): List<VarieteEntity>
    
    @Query("SELECT * FROM varietes WHERE id = :id")
    suspend fun getVarieteById(id: Long): VarieteEntity?
    
    @Query("SELECT COUNT(*) FROM varietes")
    suspend fun countVarietes(): Int
    
    @Query("DELETE FROM varietes WHERE id = :id")
    suspend fun deleteVarieteById(id: Long)
    
    /**
     * Supprime les doublons de variétés : garde l'ID le plus petit
     * pour chaque couple (nom, legumeParent).
     */
    @Query("""
        DELETE FROM varietes 
        WHERE id NOT IN (
            SELECT MIN(id) FROM varietes GROUP BY nom, legumeParent
        )
    """)
    suspend fun supprimerDoublonsVarietes()
    
    // ===== ADVENTICES (Mauvaises herbes) =====
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAdventice(adventice: AdventiceEntity): Long
    
    @Update
    suspend fun updateAdventice(adventice: AdventiceEntity)
    
    @Delete
    suspend fun deleteAdventice(adventice: AdventiceEntity)
    
    @Query("SELECT * FROM adventices ORDER BY nom ASC")
    fun getAllAdventices(): Flow<List<AdventiceEntity>>
    
    @Query("SELECT * FROM adventices WHERE id = :id")
    suspend fun getAdventiceById(id: Long): AdventiceEntity?
    
    @Query("SELECT COUNT(*) FROM adventices")
    suspend fun countAdventices(): Int
    
    @Query("DELETE FROM adventices WHERE id = :id")
    suspend fun deleteAdventiceById(id: Long)
    
    /**
     * Supprime les doublons d'adventices par nom.
     */
    @Query("""
        DELETE FROM adventices 
        WHERE id NOT IN (
            SELECT MIN(id) FROM adventices GROUP BY nom
        )
    """)
    suspend fun supprimerDoublonsAdventices()
}
