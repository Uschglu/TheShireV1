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
            ),
            AdventiceEntity(
                nom = "Chénopode blanc",
                nomScientifique = "Chenopodium album",
                description = "Plante annuelle à feuilles farineuses, très commune.",
                indicationSol = "Sol riche en azote, fertile",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Renouée des oiseaux",
                nomScientifique = "Polygonum aviculare",
                description = "Plante rampante très résistante au piétinement.",
                indicationSol = "Sol compact, piétiné",
                typeSol = "Sol tassé, compact",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Capselle bourse-à-pasteur",
                nomScientifique = "Capsella bursa-pastoris",
                description = "Petite plante à fruits en forme de cœur.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Coquelicot",
                nomScientifique = "Papaver rhoeas",
                description = "Fleur rouge emblématique des champs.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌺"
            ),
            AdventiceEntity(
                nom = "Matricaire",
                nomScientifique = "Matricaria discoidea",
                description = "Petite plante à fleurs jaunes, odeur de camomille.",
                indicationSol = "Sol compact, acide",
                typeSol = "Sol tassé, acide",
                emoji = "🌼"
            ),
            AdventiceEntity(
                nom = "Séneçon commun",
                nomScientifique = "Senecio vulgaris",
                description = "Plante annuelle à fleurs jaunes, très commune.",
                indicationSol = "Sol riche, frais",
                typeSol = "Sol fertile, humide",
                emoji = "🌼"
            ),
            AdventiceEntity(
                nom = "Laiteron",
                nomScientifique = "Sonchus oleraceus",
                description = "Plante à latex blanc, feuilles découpées.",
                indicationSol = "Sol fertile, riche",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Euphorbe réveille-matin",
                nomScientifique = "Euphorbia helioscopia",
                description = "Plante à latex blanc, petites fleurs vertes.",
                indicationSol = "Sol calcaire, sec",
                typeSol = "Sol calcaire, drainé",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Moutarde des champs",
                nomScientifique = "Sinapis arvensis",
                description = "Plante à fleurs jaunes, très envahissante.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌼"
            ),
            AdventiceEntity(
                nom = "Gaillet gratteron",
                nomScientifique = "Galium aparine",
                description = "Plante grimpante qui s'accroche aux vêtements.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Compagnon blanc",
                nomScientifique = "Silene latifolia",
                description = "Plante à fleurs blanches, commune dans les cultures.",
                indicationSol = "Sol calcaire, sec",
                typeSol = "Sol calcaire, drainé",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Bleuet",
                nomScientifique = "Centaurea cyanus",
                description = "Fleur bleue emblématique, protégée dans certains endroits.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, appauvri",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Saponaire",
                nomScientifique = "Saponaria officinalis",
                description = "Plante à fleurs roses, contient de la saponine.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌸"
            ),
            AdventiceEntity(
                nom = "Arroche",
                nomScientifique = "Atriplex patula",
                description = "Plante à feuilles triangulaires, supporte le sel.",
                indicationSol = "Sol riche, salin",
                typeSol = "Sol riche, salé",
                emoji = "🌿"
            ),
            AdventiceEntity(
                nom = "Folle avoine",
                nomScientifique = "Avena fatua",
                description = "Graminée annuelle, ressemble à l'avoine cultivée.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, appauvri",
                emoji = "🌾"
            )
        )
    }
}
