package com.theshire.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RappelDao {
    
    @Insert
    suspend fun inserer(rappel: RappelEntity): Long
    
    @Update
    suspend fun mettreAJour(rappel: RappelEntity)
    
    @Delete
    suspend fun supprimer(rappel: RappelEntity)
    
    @Query("SELECT * FROM rappels ORDER BY timestamp ASC")
    fun getAllRappels(): List<RappelEntity>
    
    @Query("SELECT * FROM rappels WHERE timestamp = :timestamp")
    suspend fun getRappelByTimestamp(timestamp: Long): RappelEntity?
    
    @Query("SELECT * FROM rappels WHERE timestamp = :timestamp")
    fun getRappelByTimestampSync(timestamp: Long): RappelEntity?
    
    @Query("DELETE FROM rappels WHERE id = :id")
    suspend fun supprimerParId(id: Long)
}
