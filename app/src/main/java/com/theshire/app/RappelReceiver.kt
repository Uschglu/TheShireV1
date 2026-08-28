package com.theshire.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class RappelReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val titre = intent.getStringExtra("titre") ?: "Rappel"
        val note = intent.getStringExtra("note") ?: ""
        
        afficherNotification(context, titre, note)
    }
    
    private fun afficherNotification(context: Context, titre: String, note: String) {
        val channelId = "rappels"
        val notificationId = (titre + note).hashCode()
        
        // Créer le canal de notification pour Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications de rappels du calendrier"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titre)
            .setContentText(if (note.isNotEmpty()) note else "C'est l'heure !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
