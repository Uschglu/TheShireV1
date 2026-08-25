package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AdventiceEntity
import com.theshire.app.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class AdventiceRepository(context: Context) {
    
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    val adventices: Flow<List<AdventiceEntity>> = legumeDao.getAllAdventices()
    
    suspend fun ajouterAdventicesPredefinies() {
        if (legumeDao.countAdventices() == 0) {
            getAdventicesPredefinies().forEach { adventice ->
                legumeDao.insertAdventice(adventice)
            }
        }
    }
    
    private fun getAdventicesPredefinies(): List<AdventiceEntity> {
        return listOf(
            AdventiceEntity(
                nom = "Rumex (Oseille sauvage)",
                nomScientifique = "Rumex obtusifolius",
                description = "Grande plante vivace à larges feuilles. Racine pivotante profonde.",
                indicationSol = "Sol compact, acide, souvent humide et mal drainé",
                typeSol = "Sol lourd, tassé, pH acide",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Ortie",
                nomScientifique = "Urtica dioica",
                description = "Plante vivace urticante, très commune. Indique un sol riche.",
                indicationSol = "Sol riche en azote et en matière organique",
                typeSol = "Sol fertile, riche en humus",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Chiendent",
                nomScientifique = "Elymus repens",
                description = "Graminée vivace à rhizomes traçants très envahissants.",
                indicationSol = "Sol tassé, pauvre, souvent épuisé",
                typeSol = "Sol compact, appauvri",
                emoji = "🌾"
            ),
            AdventiceEntity(
                nom = "Pissenlit",
                nomScientifique = "Taraxacum officinale",
                description = "Plante vivace à racine pivotante, fleurs jaunes.",
                indicationSol = "Sol compact, riche en matière organique",
                typeSol = "Sol lourd, fertile",
                emoji = "🌼"
            ),
            AdventiceEntity(
                nom = "Bouton d'or",
                nomScientifique = "Ranunculus repens",
                description = "Plante rampante à fleurs jaunes brillantes.",
                indicationSol = "Sol humide, argileux, mal drainé",
                typeSol = "Sol argileux, hydromorphe",
                emoji = "🌼"
            ),
            AdventiceEntity(
                nom = "Liseron",
                nomScientifique = "Convolvulus arvensis",
                description = "Plante grimpante à fleurs blanches ou roses en trompette.",
                indicationSol = "Sol compact, calcaire",
                typeSol = "Sol calcaire, tassé",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Fougère",
                nomScientifique = "Pteridium aquilinum",
                description = "Grande fougère vivace, très envahissante.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, siliceux",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Prêle",
                nomScientifique = "Equisetum arvense",
                description = "Plante primitive à tiges articulées, très résistante.",
                indicationSol = "Sol acide, humide, compact",
                typeSol = "Sol acide, hydromorphe",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Mouron blanc",
                nomScientifique = "Stellaria media",
                description = "Petite plante annuelle rampante, fleurs blanches étoilées.",
                indicationSol = "Sol fertile, équilibré",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Chardon",
                nomScientifique = "Cirsium arvense",
                description = "Plante vivace épineuse, très envahissante.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌵"
            ),
            AdventiceEntity(
                nom = "Amarante",
                nomScientifique = "Amaranthus retroflexus",
                description = "Plante annuelle à inflorescences dressées rougeâtres.",
                indicationSol = "Sol riche, fertile",
                typeSol = "Sol fertile, bien drainé",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Morelle noire",
                nomScientifique = "Solanum nigrum",
                description = "Plante annuelle à baies noires (toxiques).",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🫐"
            ),
            AdventiceEntity(
                nom = "Plantain",
                nomScientifique = "Plantago major",
                description = "Plante vivace en rosette, très résistante au piétinement.",
                indicationSol = "Sol compact, piétiné",
                typeSol = "Sol tassé, compact",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Véronique",
                nomScientifique = "Veronica persica",
                description = "Petite plante rampante à fleurs bleues.",
                indicationSol = "Sol acide",
                typeSol = "Sol acide, léger",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Trèfle blanc",
                nomScientifique = "Trifolium repens",
                description = "Plante rampante à feuilles trifoliées, fleurs blanches.",
                indicationSol = "Sol pauvre en azote",
                typeSol = "Sol pauvre, appauvri",
                emoji = "🍀"
            )
        )
    }
}
