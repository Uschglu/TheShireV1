package com.theshire.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService = NotificationService(context)
        val type = intent.getStringExtra("type") ?: "arrosage"
        
        notificationService.verifierEtNotifier(type)
    }
}
