package com.theshire.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlancheDao {
    
    @Query("SELECT * FROM planches ORDER BY dateCreation")
    fun getAllPlanches(): Flow<List<PlancheEntity>>
    
    @Insert
    suspend fun insertPlanche(planche: PlancheEntity): Long
    
    @Delete
    suspend fun deletePlanche(planche: PlancheEntity)
    
    @Query("SELECT * FROM carres WHERE plancheId = :plancheId ORDER BY positionY, positionX")
    fun getCarresForPlanche(plancheId: Long): Flow<List<CarreEntity>>
    
    @Insert
    suspend fun insertCarre(carre: CarreEntity): Long
    
    @Update
    suspend fun updateCarre(carre: CarreEntity)
    
    @Delete
    suspend fun deleteCarre(carre: CarreEntity)
    
    @Query("DELETE FROM carres WHERE plancheId = :plancheId")
    suspend fun deleteCarresForPlanche(plancheId: Long)
}
