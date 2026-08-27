package com.theshire.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
        private const val API_KEY = "usr-_k4H1_Urg2MHCBbe8HGUQuYTLk6g5fCJPHIUQsvjHTc"
        private const val API_URL = "https://trefle.io/api/v1/plants"
    }
    
    suspend fun identifierPlante(imageFile: File): List<PlantIdentification> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
            
            // Trefle API ne fait pas de reconnaissance d'image
            // On va utiliser une recherche basée sur le nom de fichier ou un texte
            // Pour l'instant, on retourne une liste de plantes populaires
            
            val request = Request.Builder()
                .url("$API_URL?token=$API_KEY&per_page=10&order[common_name]=asc")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val data = json.optJSONArray("data")
                
                if (data == null || data.length() == 0) {
                    return@withContext listOf(
                        PlantIdentification(
                            nom = "Aucune plante trouvée",
                            nomScientifique = "Essayez une autre recherche",
                            probabilite = 0.0,
                            imageUrl = "",
                            messageErreur = "L'API a répondu mais sans résultats"
                        )
                    )
                }
                
                val identifications = mutableListOf<PlantIdentification>()
                val nbResults: Int = if (data.length() > 5) 5 else data.length()
                
                for (i in 0 until nbResults) {
                    try {
                        val plant = data.getJSONObject(i)
                        
                        val nomCommun = plant.optString("common_name", "Inconnu")
                        val nomScientifique = plant.optString("scientific_name", "Inconnu")
                        val imageUrl = plant.optString("image_url", "")
                        
                        identifications.add(
                            PlantIdentification(
                                nom = nomCommun,
                                nomScientifique = nomScientifique,
                                probabilite = 0.0,
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
