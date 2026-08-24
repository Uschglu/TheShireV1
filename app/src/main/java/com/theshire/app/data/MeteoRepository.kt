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

data class PrevisionJour(
    val date: String,
    val tempMin: Double,
    val tempMax: Double,
    val description: String,
    val emoji: String
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
    
    suspend fun getPrevisions7Jours(ville: String = "Paris"): List<PrevisionJour> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.openweathermap.org/data/2.5/forecast?q=$ville&appid=$apiKey&units=metric&lang=fr"
                
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext emptyList()
                }
                
                val body = response.body?.string()
                response.close()
                
                if (body == null) {
                    return@withContext emptyList()
                }
                
                val json = JSONObject(body)
                val list = json.getJSONArray("list")
                
                // Regrouper par jour (toutes les 3 heures)
                val previsionsParJour = mutableMapOf<String, MutableList<JSONObject>>()
                
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    val dateTexte = item.getString("dt_txt").substring(0, 10)
                    
                    if (!previsionsParJour.containsKey(dateTexte)) {
                        previsionsParJour[dateTexte] = mutableListOf()
                    }
                    previsionsParJour[dateTexte]?.add(item)
                }
                
                // Prendre les 7 premiers jours
                val previsions = mutableListOf<PrevisionJour>()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.FRANCE)
                val jourFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.FRANCE)
                
                previsionsParJour.keys.take(7).forEach { dateTexte ->
                    val items = previsionsParJour[dateTexte] ?: return@forEach
                    
                    var tempMin = Double.MAX_VALUE
                    var tempMax = Double.MIN_VALUE
                    var description = ""
                    
                    items.forEach { item ->
                        val main = item.getJSONObject("main")
                        val temp = main.getDouble("temp")
                        if (temp < tempMin) tempMin = temp
                        if (temp > tempMax) tempMax = temp
                        if (description.isEmpty()) {
                            description = item.getJSONArray("weather").getJSONObject(0).getString("description")
                        }
                    }
                    
                    val date = dateFormat.parse(dateTexte)
                    val jourNom = if (date != null) jourFormat.format(date) else dateTexte
                    
                    previsions.add(
                        PrevisionJour(
                            date = jourNom.replaceFirstChar { it.uppercase() },
                            tempMin = tempMin,
                            tempMax = tempMax,
                            description = description,
                            emoji = getEmojiFromDescription(description)
                        )
                    )
                }
                
                previsions
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    private fun getEmojiFromDescription(description: String): String {
        return when {
            description.contains("pluie", ignoreCase = true) -> "🌧️"
            description.contains("nuage", ignoreCase = true) -> "☁️"
            description.contains("soleil", ignoreCase = true) || 
            description.contains("clair", ignoreCase = true) -> "☀️"
            description.contains("neige", ignoreCase = true) -> "❄️"
            description.contains("orage", ignoreCase = true) -> "⛈️"
            description.contains("brume", ignoreCase = true) -> "🌫️"
            else -> "🌤️"
        }
    }
}
