package com.theshire.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
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
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val imageBody = imageFile.toRequestBody("image/jpeg".toMediaType())
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", imageFile.name, imageBody)
                .addFormDataPart("organs", "leaf")
                .build()
            
            val request = Request.Builder()
                .url("$API_URL?api-key=$API_KEY")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val results = jsonResponse.optJSONArray("results")
                
                if (results == null || results.length() == 0) {
                    return@withContext emptyList()
                }
                
                val identifications = mutableListOf<PlantIdentification>()
                val maxResults = minOf(results.length(), 5)
                
                for (i in 0 until maxResults) {
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
                        
                        val images = species.optJSONObject("images")
                        val imageUrl = if (images != null && images.length() > 0) {
                            images.getJSONObject(0).optString("url.o", "")
                        } else {
                            ""
                        }
                        
                        identifications.add(
                            PlantIdentification(
                                nom = nomCommun,
                                nomScientifique = nomScientifique,
                                probabilite = score,
                                imageUrl = imageUrl
                            )
                        )
                    } catch (e: Exception) {
                        // Ignorer cette entrée et passer à la suivante
                        e.printStackTrace()
                    }
                }
                
                identifications
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
