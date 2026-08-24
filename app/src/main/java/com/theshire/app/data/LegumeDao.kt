package com.theshire.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LegumeDao {
    
    @Query("SELECT * FROM legumes ORDER BY nom")
    fun getAllLegumes(): Flow<List<LegumeEntity>>
    
    @Insert
    suspend fun insertLegume(legume: LegumeEntity)
    
    @Delete
    suspend fun deleteLegume(legume: LegumeEntity)
    
    @Query("DELETE FROM legumes")
    suspend fun deleteAllLegumes()
    
    @Query("SELECT COUNT(*) FROM legumes")
    suspend fun countLegumes(): Int
    
    // ===== VARIÉTÉS =====
    
    @Query("SELECT * FROM varietes WHERE legumeParent = :legumeNom ORDER BY nom")
    fun getVarietesForLegume(legumeNom: String): Flow<List<VarieteEntity>>
    
    @Insert
    suspend fun insertVariete(variete: VarieteEntity)
    
    @Query("DELETE FROM varietes")
    suspend fun deleteAllVarietes()
    
    @Query("SELECT COUNT(*) FROM varietes")
    suspend fun countVarietes(): Int
}
