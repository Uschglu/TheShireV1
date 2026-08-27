package com.theshire.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
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
    val imageUrl: String
)

class PlantNetRepository {
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // ✅ Format correct selon la doc Pl@ntNet
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "images",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart("organs", "auto")
                .build()
            
            // ✅ Clé API dans le header (pas dans l'URL)
            val request = Request.Builder()
                .url("https://my-api.plantnet.org/v2/identify/all")
                .addHeader("Api-Key", "2b10GvkSWG8oUys4E2QLss3u")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val results = json.optJSONArray("results")
                
                if (results == null || results.length() == 0) {
                    return@withContext emptyList()
                }
                
                val identifications = mutableListOf<PlantIdentification>()
                val nbResults: Int = if (results.length() > 5) 5 else results.length()
                
                for (i in 0 until nbResults) {
                    try {
                        val result = results.getJSONObject(i)
                        val species = result.getJSONObject("species")
                        
                        val nomScientifique = species
                            .getJSONObject("scientificNameWithoutAuthor")
                            .optString("stringValue", "Inconnu")
                        
                        val nomsCommuns = species.optJSONObject("commonNames")
                        val nomCommun = if (nomsCommuns != null && nomsCommuns.length() > 0) {
                            nomsCommuns.optString("stringValue", nomScientifique)
                        } else {
                            nomScientifique
                        }
                        
                        val score = result.optDouble("score", 0.0)
                        
                        identifications.add(
                            PlantIdentification(
                                nom = nomCommun,
                                nomScientifique = nomScientifique,
                                probabilite = score,
                                imageUrl = ""
                            )
                        )
                    } catch (e: Exception) {
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
