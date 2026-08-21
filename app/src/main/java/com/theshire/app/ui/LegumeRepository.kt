package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.LegumeEntity
import kotlinx.coroutines.flow.Flow

class LegumeRepository(context: Context) {
    
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    val legumes: Flow<List<LegumeEntity>> = legumeDao.getAllLegumes()
    
    suspend fun ajouterLegumesPredefinis() {
        if (legumeDao.countLegumes() == 0) {
            getLegumesPredefinis().forEach { legume ->
                legumeDao.insertLegume(legume)
            }
        }
    }
    
    suspend fun supprimerLegume(legume: LegumeEntity) {
        legumeDao.deleteLegume(legume)
    }
    
    private fun getLegumesPredefinis(): List<LegumeEntity> {
        return listOf(
            LegumeEntity(
                nom = "Carotte",
                categorie = "Racine",
                difficulte = "Facile",
                exposition = "Plein soleil",
                sol = "Meuble, sableux, sans cailloux",
                arrosage = "Régulier mais modéré",
                temperature = "Rustique",
                semis = "Mars à juillet",
                plantation = "Semis direct",
                recolte = "3 à 4 mois après semis",
                entretien = "Éclaircir, biner régulièrement",
                maladies = "Mouche de la carotte",
                prevention = "Voile anti-insectes, association avec oignons",
                bonnesAssociations = "Oignon, poireau, tomate",
                mauvaisesAssociations = "Aneth, persil"
            ),
            LegumeEntity(
                nom = "Tomate",
                categorie = "Fruit",
                difficulte = "Moyen",
                exposition = "Plein soleil",
                sol = "Riche en humus",
                arrosage = "Au pied, sans mouiller les feuilles",
                temperature = "Frileuse",
                semis = "Février à avril",
                plantation = "Mai",
                recolte = "Juillet à octobre",
                entretien = "Tuteurer, effeuiller",
                maladies = "Mildiou, cul noir",
                prevention = "Purin d'ortie, éviter l'humidité",
                bonnesAssociations = "Basilic, carotte, oignon",
                mauvaisesAssociations = "Pomme de terre"
            ),
            LegumeEntity(
                nom = "Salade",
                categorie = "Feuille",
                difficulte = "Facile",
                exposition = "Mi-ombre",
                sol = "Frais, riche",
                arrosage = "Régulier",
                temperature = "Rustique",
                semis = "Mars à septembre",
                plantation = "Repiquage",
                recolte = "4 à 8 semaines",
                entretien = "Pailler, éclaircir",
                maladies = "Limaces",
                prevention = "Cendre, purin de prêle",
                bonnesAssociations = "Carotte, radis, concombre",
                mauvaisesAssociations = "Céleri"
            )
        )
    }
}
