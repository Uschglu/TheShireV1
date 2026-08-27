package com.theshire.app.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class PlantIdentification(
    val nom: String,
    val nomScientifique: String,
    val probabilite: Double,
    val imageUrl: String,
    val messageErreur: String = ""
)

class PlantNetRepository {
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // Lire le fichier et convertir en Base64
            val imageBytes = imageFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            // Construire le JSON pour l'API de reconnaissance iNaturalist
            val jsonBody = JSONObject()
            jsonBody.put("image", base64Image)
            jsonBody.put("taxa_filter", "plantae")
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            // Utiliser l'API de vision par ordinateur iNaturalist
            val request = Request.Builder()
                .url("https://api.inaturalist.org/v1/computervision_score")
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val results = json.optJSONArray("results")
                
                if (results == null || results.length() == 0) {
                    return@withContext listOf(
                        PlantIdentification(
                            nom = "Aucune plante identifiée",
                            nomScientifique = "Essayez avec une photo plus nette",
                            probabilite = 0.0,
                            imageUrl = "",
                            messageErreur = "L'API n'a pas reconnu la plante. Prenez une photo plus nette en plein jour."
                        )
                    )
                }
                
                val identifications = mutableListOf<PlantIdentification>()
                val nbResults: Int = if (results.length() > 5) 5 else results.length()
                
                for (i in 0 until nbResults) {
                    try {
                        val result = results.getJSONObject(i)
                        val taxon = result.getJSONObject("taxon")
                        
                        val nomScientifique = taxon.optString("name", "Inconnu")
                        val nomCommun = taxon.optString("preferred_common_name", nomScientifique)
                        
                        val score = result.optDouble("combined_score", 0.0)
                        
                        val imageUrl = taxon.optJSONObject("default_photo")
                            ?.optString("medium_url", "") ?: ""
                        
                        identifications.add(
                            PlantIdentification(
                                nom = nomCommun,
                                nomScientifique = nomScientifique,
                                probabilite = score,
                                imageUrl = imageUrl,
                                messageErreur = ""
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                identifications
            } else {
                val errorBody = response.body?.string() ?: ""
                return@withContext listOf(
                    PlantIdentification(
                        nom = "Erreur ${response.code}",
                        nomScientifique = response.message ?: "Inconnu",
                        probabilite = 0.0,
                        imageUrl = "",
                        messageErreur = "Code: ${response.code} - $errorBody"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext listOf(
                PlantIdentification(
                    nom = "Exception",
                    nomScientifique = e.message ?: "Inconnu",
                    probabilite = 0.0,
                    imageUrl = "",
                    messageErreur = "Exception: ${e.message}"
                )
            )
        }
    }
}
