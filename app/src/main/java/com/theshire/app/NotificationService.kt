package com.theshire.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.theshire.app.data.MeteoRepository
import com.theshire.app.data.PrevisionJour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService(private val context: Context) {
    
    private val meteoRepository = MeteoRepository()
    
    init {
        creerCanaux()
    }
    
    private fun creerCanaux() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canalArrosage = NotificationChannel(
                "arrosage",
                "Rappels d'arrosage",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour l'arrosage du potager"
            }
            
            val canalMeteo = NotificationChannel(
                "meteo",
                "Alertes météo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes météo importantes pour le potager"
            }
            
            val canalOperations = NotificationChannel(
                "operations",
                "Opérations culturales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels pour les opérations du potager"
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(canalArrosage)
            notificationManager.createNotificationChannel(canalMeteo)
            notificationManager.createNotificationChannel(canalOperations)
        }
    }
    
    fun verifierEtNotifier() {
        CoroutineScope(Dispatchers.IO).launch {
            // Récupérer la météo
            val previsions = meteoRepository.getPrevisions7Jours()
            
            if (previsions.isNotEmpty()) {
                val aujourdhui = previsions.first()
                val demain = previsions.getOrNull(1)
                
                // Vérifier la pluie pour aujourd'hui et demain
                val pluieAujourdhui = aujourdhui.description.contains("pluie", ignoreCase = true)
                val pluieDemain = demain?.description?.contains("pluie", ignoreCase = true) ?: false
                
                if (!pluieAujourdhui && !pluieDemain) {
                    // Pas de pluie prévue → envoyer notification d'arrosage
                    envoyerNotificationArrosage()
                }
                
                // Vérifier les alertes météo
                if (aujourdhui.tempMin < 2) {
                    envoyerAlerteGel()
                }
                if (aujourdhui.tempMax > 32) {
                    envoyerAlerteCanicule()
                }
            }
            
            // Envoyer les rappels d'opérations culturales
            envoyerRappelOperations()
        }
    }
    
    private fun envoyerNotificationArrosage() {
        val notification = NotificationCompat.Builder(context, "arrosage")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("💧 Pensez à arroser votre potager")
            .setContentText("Un binage vaut 3 arrosages ! Arrosez le soir pour limiter l'évaporation.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(1, notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }
    
    private fun envoyerAlerteGel() {
        val notification = NotificationCompat.Builder(context, "meteo")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("❄️ Alerte gel !")
            .setContentText("Des températures proches de 0°C sont prévues. Protégez vos plantes sensibles.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(2, notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }
    
    private fun envoyerAlerteCanicule() {
        val notification = NotificationCompat.Builder(context, "meteo")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🔥 Alerte canicule !")
            .setContentText("Températures élevées prévues. Arrosez abondamment le soir et paillez.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(3, notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }
    
    private fun envoyerRappelOperations() {
        val notification = NotificationCompat.Builder(context, "operations")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🌱 Entretien du potager")
            .setContentText("Pensez au buttage, au paillage, au désherbage et à l'éclaircissement selon vos cultures.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(4, notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }
}
