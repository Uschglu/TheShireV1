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
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Rumex_obtusifolius_-_Rumex_obtuse-leaf_dock.jpg/400px-Rumex_obtusifolius_-_Rumex_obtuse-leaf_dock.jpg"
            ),
            AdventiceEntity(
                nom = "Ortie",
                nomScientifique = "Urtica dioica",
                description = "Plante vivace urticante, très commune. Indique un sol riche.",
                indicationSol = "Sol riche en azote et en matière organique",
                typeSol = "Sol fertile, riche en humus",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Urtica_dioica_-_Brennnessel.jpg/400px-Urtica_dioica_-_Brennnessel.jpg"
            ),
            AdventiceEntity(
                nom = "Chiendent",
                nomScientifique = "Elymus repens",
                description = "Graminée vivace à rhizomes traçants très envahissants.",
                indicationSol = "Sol tassé, pauvre, souvent épuisé",
                typeSol = "Sol compact, appauvri",
                emoji = "🌾",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Elymus_repens_-_couch_grass.jpg/400px-Elymus_repens_-_couch_grass.jpg"
            ),
            AdventiceEntity(
                nom = "Pissenlit",
                nomScientifique = "Taraxacum officinale",
                description = "Plante vivace à racine pivotante, fleurs jaunes.",
                indicationSol = "Sol compact, riche en matière organique",
                typeSol = "Sol lourd, fertile",
                emoji = "🌼",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Taraxacum_officinale_-_Dandelion.jpg/400px-Taraxacum_officinale_-_Dandelion.jpg"
            ),
            AdventiceEntity(
                nom = "Bouton d'or",
                nomScientifique = "Ranunculus repens",
                description = "Plante rampante à fleurs jaunes brillantes.",
                indicationSol = "Sol humide, argileux, mal drainé",
                typeSol = "Sol argileux, hydromorphe",
                emoji = "🌼",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Ranunculus_repens_-_Creeping_buttercup.jpg/400px-Ranunculus_repens_-_Creeping_buttercup.jpg"
            ),
            AdventiceEntity(
                nom = "Liseron",
                nomScientifique = "Convolvulus arvensis",
                description = "Plante grimpante à fleurs blanches ou roses en trompette.",
                indicationSol = "Sol compact, calcaire",
                typeSol = "Sol calcaire, tassé",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Convolvulus_arvensis_-_Field_bindweed.jpg/400px-Convolvulus_arvensis_-_Field_bindweed.jpg"
            ),
            AdventiceEntity(
                nom = "Fougère",
                nomScientifique = "Pteridium aquilinum",
                description = "Grande fougère vivace, très envahissante.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, siliceux",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Pteridium_aquilinum_-_Bracken_fern.jpg/400px-Pteridium_aquilinum_-_Bracken_fern.jpg"
            ),
            AdventiceEntity(
                nom = "Prêle",
                nomScientifique = "Equisetum arvense",
                description = "Plante primitive à tiges articulées, très résistante.",
                indicationSol = "Sol acide, humide, compact",
                typeSol = "Sol acide, hydromorphe",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Equisetum_arvense_-_Field_horsetail.jpg/400px-Equisetum_arvense_-_Field_horsetail.jpg"
            ),
            AdventiceEntity(
                nom = "Mouron blanc",
                nomScientifique = "Stellaria media",
                description = "Petite plante annuelle rampante, fleurs blanches étoilées.",
                indicationSol = "Sol fertile, équilibré",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Stellaria_media_-_Common_chickweed.jpg/400px-Stellaria_media_-_Common_chickweed.jpg"
            ),
            AdventiceEntity(
                nom = "Chardon",
                nomScientifique = "Cirsium arvense",
                description = "Plante vivace épineuse, très envahissante.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌵",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Cirsium_arvense_-_Creeping_thistle.jpg/400px-Cirsium_arvense_-_Creeping_thistle.jpg"
            ),
            AdventiceEntity(
                nom = "Amarante",
                nomScientifique = "Amaranthus retroflexus",
                description = "Plante annuelle à inflorescences dressées rougeâtres.",
                indicationSol = "Sol riche, fertile",
                typeSol = "Sol fertile, bien drainé",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Amaranthus_retroflexus_-_Redroot_amaranth.jpg/400px-Amaranthus_retroflexus_-_Redroot_amaranth.jpg"
            ),
            AdventiceEntity(
                nom = "Morelle noire",
                nomScientifique = "Solanum nigrum",
                description = "Plante annuelle à baies noires (toxiques).",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🫐",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Solanum_nigrum_-_Black_nightshade.jpg/400px-Solanum_nigrum_-_Black_nightshade.jpg"
            ),
            AdventiceEntity(
                nom = "Plantain",
                nomScientifique = "Plantago major",
                description = "Plante vivace en rosette, très résistante au piétinement.",
                indicationSol = "Sol compact, piétiné",
                typeSol = "Sol tassé, compact",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Plantago_major_-_Broadleaf_plantain.jpg/400px-Plantago_major_-_Broadleaf_plantain.jpg"
            ),
            AdventiceEntity(
                nom = "Véronique",
                nomScientifique = "Veronica persica",
                description = "Petite plante rampante à fleurs bleues.",
                indicationSol = "Sol acide",
                typeSol = "Sol acide, léger",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/Veronica_persica_-_Persian_speedwell.jpg/400px-Veronica_persica_-_Persian_speedwell.jpg"
            ),
            AdventiceEntity(
                nom = "Trèfle blanc",
                nomScientifique = "Trifolium repens",
                description = "Plante rampante à feuilles trifoliées, fleurs blanches.",
                indicationSol = "Sol pauvre en azote",
                typeSol = "Sol pauvre, appauvri",
                emoji = "🍀",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Trifolium_repens_-_White_clover.jpg/400px-Trifolium_repens_-_White_clover.jpg"
            ),
            AdventiceEntity(
                nom = "Chénopode blanc",
                nomScientifique = "Chenopodium album",
                description = "Plante annuelle à feuilles farineuses, très commune.",
                indicationSol = "Sol riche en azote, fertile",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Chenopodium_album_-_White_goosefoot.jpg/400px-Chenopodium_album_-_White_goosefoot.jpg"
            ),
            AdventiceEntity(
                nom = "Renouée des oiseaux",
                nomScientifique = "Polygonum aviculare",
                description = "Plante rampante très résistante au piétinement.",
                indicationSol = "Sol compact, piétiné",
                typeSol = "Sol tassé, compact",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Polygonum_aviculare_-_Prostrate_knotweed.jpg/400px-Polygonum_aviculare_-_Prostrate_knotweed.jpg"
            ),
            AdventiceEntity(
                nom = "Capselle bourse-à-pasteur",
                nomScientifique = "Capsella bursa-pastoris",
                description = "Petite plante à fruits en forme de cœur.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Capsella_bursa-pastoris_-_Shepherd%27s_purse.jpg/400px-Capsella_bursa-pastoris_-_Shepherd%27s_purse.jpg"
            ),
            AdventiceEntity(
                nom = "Coquelicot",
                nomScientifique = "Papaver rhoeas",
                description = "Fleur rouge emblématique des champs.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌺",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Papaver_rhoeas_-_Common_poppy.jpg/400px-Papaver_rhoeas_-_Common_poppy.jpg"
            ),
            AdventiceEntity(
                nom = "Matricaire",
                nomScientifique = "Matricaria discoidea",
                description = "Petite plante à fleurs jaunes, odeur de camomille.",
                indicationSol = "Sol compact, acide",
                typeSol = "Sol tassé, acide",
                emoji = "🌼",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Matricaria_discoidea_-_Pineappleweed.jpg/400px-Matricaria_discoidea_-_Pineappleweed.jpg"
            ),
            AdventiceEntity(
                nom = "Séneçon commun",
                nomScientifique = "Senecio vulgaris",
                description = "Plante annuelle à fleurs jaunes, très commune.",
                indicationSol = "Sol riche, frais",
                typeSol = "Sol fertile, humide",
                emoji = "🌼",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Senecio_vulgaris_-_Common_groundsel.jpg/400px-Senecio_vulgaris_-_Common_groundsel.jpg"
            ),
            AdventiceEntity(
                nom = "Laiteron",
                nomScientifique = "Sonchus oleraceus",
                description = "Plante à latex blanc, feuilles découpées.",
                indicationSol = "Sol fertile, riche",
                typeSol = "Sol riche, bien drainé",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Sonchus_oleraceus_-_Smooth_sow-thistle.jpg/400px-Sonchus_oleraceus_-_Smooth_sow-thistle.jpg"
            ),
            AdventiceEntity(
                nom = "Euphorbe réveille-matin",
                nomScientifique = "Euphorbia helioscopia",
                description = "Plante à latex blanc, petites fleurs vertes.",
                indicationSol = "Sol calcaire, sec",
                typeSol = "Sol calcaire, drainé",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Euphorbia_helioscopia_-_Sun_spurge.jpg/400px-Euphorbia_helioscopia_-_Sun_spurge.jpg"
            ),
            AdventiceEntity(
                nom = "Moutarde des champs",
                nomScientifique = "Sinapis arvensis",
                description = "Plante à fleurs jaunes, très envahissante.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌼",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Sinapis_arvensis_-_Field_mustard.jpg/400px-Sinapis_arvensis_-_Field_mustard.jpg"
            ),
            AdventiceEntity(
                nom = "Gaillet gratteron",
                nomScientifique = "Galium aparine",
                description = "Plante grimpante qui s'accroche aux vêtements.",
                indicationSol = "Sol riche en azote",
                typeSol = "Sol fertile, riche",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Galium_aparine_-_Cleavers.jpg/400px-Galium_aparine_-_Cleavers.jpg"
            ),
            AdventiceEntity(
                nom = "Compagnon blanc",
                nomScientifique = "Silene latifolia",
                description = "Plante à fleurs blanches, commune dans les cultures.",
                indicationSol = "Sol calcaire, sec",
                typeSol = "Sol calcaire, drainé",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Silene_latifolia_-_White_campion.jpg/400px-Silene_latifolia_-_White_campion.jpg"
            ),
            AdventiceEntity(
                nom = "Bleuet",
                nomScientifique = "Centaurea cyanus",
                description = "Fleur bleue emblématique, protégée dans certains endroits.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, appauvri",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Centaurea_cyanus_-_Cornflower.jpg/400px-Centaurea_cyanus_-_Cornflower.jpg"
            ),
            AdventiceEntity(
                nom = "Saponaire",
                nomScientifique = "Saponaria officinalis",
                description = "Plante à fleurs roses, contient de la saponine.",
                indicationSol = "Sol calcaire, riche",
                typeSol = "Sol calcaire, fertile",
                emoji = "🌸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Saponaria_officinalis_-_Common_soapwort.jpg/400px-Saponaria_officinalis_-_Common_soapwort.jpg"
            ),
            AdventiceEntity(
                nom = "Arroche",
                nomScientifique = "Atriplex patula",
                description = "Plante à feuilles triangulaires, supporte le sel.",
                indicationSol = "Sol riche, salin",
                typeSol = "Sol riche, salé",
                emoji = "🌿",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Atriplex_patula_-_Spreading_orache.jpg/400px-Atriplex_patula_-_Spreading_orache.jpg"
            ),
            AdventiceEntity(
                nom = "Folle avoine",
                nomScientifique = "Avena fatua",
                description = "Graminée annuelle, ressemble à l'avoine cultivée.",
                indicationSol = "Sol acide, pauvre",
                typeSol = "Sol acide, appauvri",
                emoji = "🌾",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Avena_fatua_-_Wild_oat.jpg/400px-Avena_fatua_-_Wild_oat.jpg"
            )
        )
    }
}
