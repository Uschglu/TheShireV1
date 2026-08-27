package com.theshire.app.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class PlantIdentification(
    val nom: String,
    val nomScientifique: String,
    val probabilite: Double,
    val imageUrl: String
)

class PlantNetRepository {
    
    companion object {
        private const val API_KEY = "2b10GvkSWG8oUys4E2QLss3u"
        private const val API_URL = "https://my-api.plantnet.org/v2/identify/all"
    }
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Envoi en Base64 (JSON)
            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            val jsonBody = JSONObject()
                .put("images", JSONArray().put(base64Image))
                .put("organs", JSONArray().put("auto"))
                .toString()
            
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
            
            val request = Request.Builder()
                .url("$API_URL?api-key=$API_KEY")
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()
            
            println("Envoi a Pl@ntNet...")
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                println("Reponse recue (${responseBody.length} caracteres)")
                
                val json = JSONObject(responseBody)
                val results = json.optJSONArray("results")
                
                if (results == null || results.length() == 0) {
                    println("Aucun resultat")
                    return@withContext emptyList()
                }
                
                println("${results.length()} resultats trouves")
                
                val identifications = mutableListOf<PlantIdentification>()
                
                for (i in 0 until minOf(results.length(), 5)) {
                    try {
                        val result = results.getJSONObject(i)
                        val species = result.getJSONObject("species")
                        
                        val nomScientifique = species
                            .getJSONObject("scientificNameWithoutAuthor")
                            .optString("stringValue", "Inconnu")
                        
                        val nomsCommuns = species.optJSONObject("commonNames")
                        val nomCommun = if (nomsCommuns != null && nomsCommuns.length() > 0) {
                            nomsCommuns.getJSONObject(0).optString("stringValue", nomScientifique)
                        } else {
                            nomScientifique
                        }
                        
                        val score = result.optDouble("score", 0.0)
                        
                        println("Resultat $i: $nomCommun ($nomScientifique) - ${(score * 100).toInt()}%")
                        
                        identifications.add(
                            PlantIdentification(
                                nom = nomCommun,
                                nomScientifique = nomScientifique,
                                probabilite = score,
                                imageUrl = ""
                            )
                        )
                    } catch (e: Exception) {
                        println("Erreur parsing resultat $i: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                identifications
            } else {
                val errorBody = response.body?.string() ?: ""
                // ✅ CORRECTION : Utiliser toString() pour éviter le problème de type
                println("Erreur Pl@ntNet: ${response.code} - ${response.message ?: "Inconnu"}")
                println("Detail: ${errorBody.take(500)}")
                emptyList()
            }
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
