package com.theshire.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.theshire.app.RappelReceiver
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.RappelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RappelRepository(context: Context) {
    
    private val rappelDao = AppDatabase.getDatabase(context).rappelDao()
    private val appContext = context.applicationContext
    
    suspend fun ajouterRappel(timestamp: Long, titre: String, note: String = "") {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant == null) {
            val id = rappelDao.inserer(
                RappelEntity(
                    timestamp = timestamp,
                    titre = titre,
                    note = note,
                    estActif = true
                )
            )
            programmerAlarme(timestamp, titre, note, id)
        }
    }
    
    suspend fun toggleRappel(timestamp: Long, titre: String) {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant != null) {
            val nouveauEtat = !existant.estActif
            rappelDao.mettreAJour(
                existant.copy(estActif = nouveauEtat)
            )
            if (nouveauEtat) {
                programmerAlarme(timestamp, existant.titre, existant.note, existant.id)
            } else {
                annulerAlarme(timestamp, existant.id)
            }
        } else {
            val id = rappelDao.inserer(
                RappelEntity(
                    timestamp = timestamp,
                    titre = titre,
                    note = "",
                    estActif = true
                )
            )
            programmerAlarme(timestamp, titre, "", id)
        }
    }
    
    suspend fun supprimerRappel(timestamp: Long) {
        val existant = rappelDao.getRappelByTimestamp(timestamp)
        if (existant != null) {
            annulerAlarme(timestamp, existant.id)
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
            if (existant.estActif) {
                programmerAlarme(timestamp, existant.titre, note, existant.id)
            }
        }
    }
    
    private fun programmerAlarme(timestamp: Long, titre: String, note: String, id: Long) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(appContext, RappelReceiver::class.java)
        intent.putExtra("titre", titre)
        intent.putExtra("note", note)
        
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Si la date est déjà passée, on ne programme pas
        if (timestamp > System.currentTimeMillis()) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
        }
    }
    
    private fun annulerAlarme(timestamp: Long, id: Long) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(appContext, RappelReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}
