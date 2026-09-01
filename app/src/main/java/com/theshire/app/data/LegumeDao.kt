package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LegumeDao {
    
    @Insert
    suspend fun insertLegume(legume: LegumeEntity)
    
    @Update
    suspend fun updateLegume(legume: LegumeEntity)
    
    @Delete
    suspend fun deleteLegume(legume: LegumeEntity)
    
    @Query("SELECT * FROM legumes ORDER BY nom ASC")
    fun getAllLegumes(): Flow<List<LegumeEntity>>
    
    @Query("SELECT * FROM legumes WHERE id = :id")
    suspend fun getLegumeById(id: Long): LegumeEntity?
    
    @Query("SELECT COUNT(*) FROM legumes")
    suspend fun countLegumes(): Int
    
    @Query("DELETE FROM legumes WHERE id = :id")
    suspend fun deleteLegumeById(id: Long)
    
    // ===== VARIÉTÉS =====
    
    @Insert
    suspend fun insertVariete(variete: VarieteEntity)
    
    @Query("SELECT * FROM varietes WHERE legumeParent = :legumeNom")
    fun getVarietesForLegume(legumeNom: String): Flow<List<VarieteEntity>>
    
    @Query("SELECT * FROM varietes WHERE legumeParent = :legumeNom")
    fun getVarietesForLegumeSync(legumeNom: String): List<VarieteEntity>
    
    @Query("SELECT COUNT(*) FROM varietes")
    suspend fun countVarietes(): Int
}
