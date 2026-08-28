package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.RappelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RappelRepository(context: Context) {
    
    private val rappelDao = AppDatabase.getDatabase(context).rappelDao()
    
    suspend fun ajouterRappel(timestamp: Long, titre: String, note: String = "") {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant == null) {
            rappelDao.inserer(
                RappelEntity(
                    timestamp = timestamp,
                    titre = titre,
                    note = note,
                    estActif = true
                )
            )
        }
    }
    
    suspend fun toggleRappel(timestamp: Long, titre: String) {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant != null) {
            rappelDao.mettreAJour(
                existant.copy(estActif = !existant.estActif)
            )
        } else {
            rappelDao.inserer(
                RappelEntity(
                    timestamp = timestamp,
                    titre = titre,
                    note = "",
                    estActif = true
                )
            )
        }
    }
    
    suspend fun supprimerRappel(timestamp: Long) {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant != null) {
            rappelDao.supprimerParId(existant.id)
        }
    }
    
    suspend fun getRappel(timestamp: Long): RappelEntity? {
        return rappelDao.getRappelByTimestamp(timestamp)
    }
    
    fun getRappelSync(timestamp: Long): RappelEntity? {
        return rappelDao.getRappelByTimestampSync(timestamp)
    }
    
    suspend fun mettreAJourNote(timestamp: Long, note: String) {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant != null) {
            rappelDao.mettreAJour(
                existant.copy(note = note)
            )
        }
    }
}
