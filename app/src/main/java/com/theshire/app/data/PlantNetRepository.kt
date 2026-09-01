package com.theshire.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
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
    
    companion object {
        private const val API_KEY = "fmkoylfOyLghY2QAsbxvU8miS1Vtn72z5Vp9PfRYo7Qch4MipG"
        private const val BASE_URL = "https://api.plant.id/v2"
    }
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // Préparer le corps multipart avec l'image
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "images",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()
            
            // Créer la requête vers Plant.id
            val request = Request.Builder()
                .url("$BASE_URL/identify")
                .post(requestBody)
                .header("Api-Key", API_KEY)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                
                val identifications = mutableListOf<PlantIdentification>()
                
                // Plant.id retourne "suggestions" avec les résultats
                val suggestions = json.optJSONArray("suggestions")
                
                if (suggestions != null && suggestions.length() > 0) {
                    val nbResults = minOf(suggestions.length(), 5)
                    
                    for (i in 0 until nbResults) {
                        try {
                            val suggestion = suggestions.getJSONObject(i)
                            
                            val nomCommun = suggestion.optString("plant_name", "Inconnu")
                            val nomScientifique = suggestion.optString("plant_details", "")
                                ?.let { 
                                    try {
                                        JSONObject(it).optJSONObject("scientific_name")
                                            ?.optString("name", nomCommun) ?: nomCommun
                                    } catch (e: Exception) {
                                        nomCommun
                                    }
                                } ?: nomCommun
                            
                            val probabilite = suggestion.optDouble("probability", 0.0)
                            
                            val imageUrl = suggestion.optJSONArray("similar_images")
                                ?.optJSONObject(0)
                                ?.optString("url", "") ?: ""
                            
                            identifications.add(
                                PlantIdentification(
                                    nom = nomCommun,
                                    nomScientifique = nomScientifique,
                                    probabilite = probabilite,
                                    imageUrl = imageUrl,
                                    messageErreur = ""
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                if (identifications.isEmpty()) {
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
                
                identifications
            } else {
                val errorBody = response.body?.string() ?: ""
                return@withContext listOf(
                    PlantIdentification(
                        nom = "Erreur ${response.code}",
                        nomScientifique = response.message ?: "Inconnu",
                        probabilite = 0.0,
                        imageUrl = "",
                        messageErreur = "Code: ${response.code} - ${response.message}"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext listOf(
                PlantIdentification(
                    nom = "Erreur",
                    nomScientifique = e.message ?: "Inconnu",
                    probabilite = 0.0,
                    imageUrl = "",
                    messageErreur = "Exception: ${e.message}"
                )
            )
        }
    }
}
