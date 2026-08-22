package com.theshire.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MeteoData(
    val temperature: Double,
    val description: String,
    val humidite: Int,
    val vent: Double
)

class MeteoRepository {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    private val apiKey = "c19910ffc79b78f2f7eeed0c8865a1ad"
    
    suspend fun getMeteo(ville: String = "Paris"): MeteoData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.openweathermap.org/data/2.5/weather?q=$ville&appid=$apiKey&units=metric&lang=fr"
                
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext null
                }
                
                val body = response.body?.string()
                response.close()
                
                if (body == null) {
                    return@withContext null
                }
                
                val json = JSONObject(body)
                val main = json.getJSONObject("main")
                val weather = json.getJSONArray("weather").getJSONObject(0)
                val wind = json.getJSONObject("wind")
                
                MeteoData(
                    temperature = main.getDouble("temp"),
                    description = weather.getString("description"),
                    humidite = main.getInt("humidity"),
                    vent = wind.getDouble("speed")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
