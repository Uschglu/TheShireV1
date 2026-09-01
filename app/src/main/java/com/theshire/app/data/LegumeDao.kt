package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LegumeDao {
    
    // ===== LÉGUMES =====
    
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
    
    @Query("SELECT * FROM legumes WHERE nom = :nom")
    suspend fun getLegumeByNom(nom: String): LegumeEntity?
    
    @Query("SELECT COUNT(*) FROM legumes")
    suspend fun countLegumes(): Int
    
    @Query("DELETE FROM legumes WHERE id = :id")
    suspend fun deleteLegumeById(id: Long)
    
    // ===== VARIÉTÉS =====
    
    @Insert
    suspend fun insertVariete(variete: VarieteEntity)
    
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
    
    // ===== ADVENTICES (Mauvaises herbes) =====
    
    @Insert
    suspend fun insertAdventice(adventice: AdventiceEntity)
    
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
}
