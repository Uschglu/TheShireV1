package com.theshire.app

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.theshire.app.data.BrandingApp
import com.theshire.app.data.OutilsApp
import com.theshire.app.data.ThemePreferences
import com.theshire.app.ui.navigation.MainScreen
import com.theshire.app.ui.theme.CouleursApp
import com.theshire.app.ui.theme.PotagerShireTheme
import okhttp3.OkHttpClient
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Demande des permissions au démarrage
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), 1000)
        }

        // Initialisation des préférences et du branding
        CouleursApp.changerModeSombre(ThemePreferences.chargerModeSombre(this))
        BrandingApp.initialiser(this)
        OutilsApp.initialiser(this)

        setContent {
            PotagerShireTheme {
                MainScreen()
            }
        }

        planifierNotifications()
    }

    /**
     * Programme les notifications quotidiennes (arrosage le soir, opérations le matin).
     */
    private fun planifierNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Notification arrosage à 18h
        val intent1 = Intent(this, NotificationReceiver::class.java)
            .putExtra("type", "arrosage")
        val pending1 = PendingIntent.getBroadcast(
            this, 1, intent1,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal1 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal1.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending1
        )

        // Notification opérations à 8h
        val intent2 = Intent(this, NotificationReceiver::class.java)
            .putExtra("type", "operations")
        val pending2 = PendingIntent.getBroadcast(
            this, 2, intent2,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal2 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal2.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending2
        )
    }
}

/**
 * Fournisseur d'ImageLoader pour Coil (cache disque + mémoire).
 * Utilisé par les écrans Accueil et Reconnaissance.
 */
object ImageLoaderProvider {
    fun getImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .okHttpClient {
                OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
}
