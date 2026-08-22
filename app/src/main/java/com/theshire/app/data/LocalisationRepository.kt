package com.theshire.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class LocalisationRepository(private val context: Context) {
    
    suspend fun getVille(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                
                // Vérifier les permissions
                val hasFineLocation = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                val hasCoarseLocation = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                if (!hasFineLocation && !hasCoarseLocation) {
                    return@withContext null
                }
                
                // Récupérer la dernière position connue
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                
                if (lastLocation != null) {
                    return@withContext getVilleFromLocation(lastLocation)
                }
                
                null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun getVilleFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.FRANCE)
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
