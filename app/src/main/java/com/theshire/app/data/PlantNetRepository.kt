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
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // Utiliser l'API iNaturalist observations avec multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()
            
            // Utiliser l'endpoint d'observation avec l'image en multipart
            val request = Request.Builder()
                .url("https://api.inaturalist.org/v1/observations")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                
                // Analyser la réponse pour extraire les taxons suggérés
                val results = json.optJSONArray("results")
                val identifications = mutableListOf<PlantIdentification>()
                
                if (results != null && results.length() > 0) {
                    for (i in 0 until minOf(results.length(), 5)) {
                        try {
                            val result = results.getJSONObject(i)
                            val taxon = result.optJSONObject("taxon")
                            
                            if (taxon != null) {
                                val nomScientifique = taxon.optString("name", "Inconnu")
                                val nomCommun = taxon.optString("preferred_common_name", nomScientifique)
                                val score = result.optDouble("score", 0.0)
                                
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
                            }
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
