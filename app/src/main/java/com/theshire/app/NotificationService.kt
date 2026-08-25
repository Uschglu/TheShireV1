package com.theshire.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.theshire.app.data.MeteoRepository
import com.theshire.app.ui.JardinRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService(private val context: Context) {
    
    private val meteoRepository = MeteoRepository()
    private val jardinRepository = JardinRepository(context)
    
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
    
    fun verifierEtNotifier(type: String = "arrosage") {
        CoroutineScope(Dispatchers.IO).launch {
            when (type) {
                "arrosage" -> {
                    verifierArrosageEtMeteo()
                }
                "operations" -> {
                    envoyerRappelOperations()
                }
            }
        }
    }
    
    private suspend fun verifierArrosageEtMeteo() {
        val previsions = meteoRepository.getPrevisions7Jours()
        
        if (previsions.isNotEmpty()) {
            val aujourdhui = previsions.first()
            val demain = previsions.getOrNull(1)
            
            val pluieAujourdhui = aujourdhui.description.contains("pluie", ignoreCase = true)
            val pluieDemain = demain?.description?.contains("pluie", ignoreCase = true) ?: false
            
            if (!pluieAujourdhui && !pluieDemain) {
                envoyerNotificationArrosage()
            }
            
            if (demain != null && demain.tempMin < 2) {
                envoyerAlerteGel()
            }
            
            if (demain != null && demain.tempMax > 32) {
                envoyerAlerteCanicule()
            }
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
            .setContentTitle("❄️ Alerte gel pour demain !")
            .setContentText("Des températures proches de 0°C sont prévues demain. Protégez vos plantes sensibles.")
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
            .setContentTitle("🔥 Alerte canicule pour demain !")
            .setContentText("Températures élevées prévues demain. Arrosez abondamment le soir et paillez.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(3, notification)
        } catch (e: SecurityException) {
            // Permission non accordée
        }
    }
    
    private suspend fun envoyerRappelOperations() {
        // Récupérer les légumes plantés depuis la base de données
        val legumesPlantes = jardinRepository.getLegumesPlantes()
        
        val operations = mutableListOf<String>()
        
        legumesPlantes.forEach { legume ->
            when {
                legume.contains("Pomme de terre", ignoreCase = true) -> {
                    if ("Butter les pommes de terre" !in operations) operations.add("Butter les pommes de terre")
                }
                legume.contains("Tomate", ignoreCase = true) -> {
                    if ("Tuteurer et effeuiller les tomates" !in operations) operations.add("Tuteurer et effeuiller les tomates")
                }
                legume.contains("Salade", ignoreCase = true) || legume.contains("Épinard", ignoreCase = true) -> {
                    if ("Éclaircir les semis" !in operations) operations.add("Éclaircir les semis")
                }
                legume.contains("Haricot", ignoreCase = true) || legume.contains("Pois", ignoreCase = true) -> {
                    if ("Butter les haricots/pois" !in operations) operations.add("Butter les haricots/pois")
                }
                legume.contains("Courgette", ignoreCase = true) || legume.contains("Potiron", ignoreCase = true) -> {
                    if ("Pailler les cucurbitacées" !in operations) operations.add("Pailler les cucurbitacées")
                }
                legume.contains("Poireau", ignoreCase = true) -> {
                    if ("Butter les poireaux" !in operations) operations.add("Butter les poireaux")
                }
            }
        }
        
        // Opérations générales
        operations.add("Vérifier le paillage")
        operations.add("Désherber les planches")
        
        val message = if (operations.isNotEmpty()) {
            operations.take(3).joinToString(" • ")
        } else {
            "Pensez au paillage et au désherbage de vos planches."
        }
        
        val notification = NotificationCompat.Builder(context, "operations")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🌱 Entretien du potager")
            .setContentText(message)
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
