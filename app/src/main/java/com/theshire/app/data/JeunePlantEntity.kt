package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant un jeune plant possédé par l'utilisateur.
 * 
 * "Jeune plant" = semis en godet, plant acheté en jardinerie,
 * bouture, ou tout végétal en cours de croissance qui n'est
 * pas encore planté en pleine terre ou en contenant.
 * 
 * Utilisé dans l'onglet "Jeunes plants" de l'écran Stocks.
 * 
 * TODO : ajouter un champ "prix" quand un partenaire magasin sera identifié
 *        (permettra de calculer le coût réel du potager).
 * 
 * TODO (plus tard) : ajouter une durée de vie / date limite de plantation
 *        + rappels culturaux automatiques via le calendrier.
 */
@Entity(tableName = "jeunes_plants")
data class JeunePlantEntity(
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // === LIEN AVEC LA BIBLIOTHÈQUE ===
    // Le nom du légume parent (ex : "Basilic")
    // Correspond à LegumeEntity.nom
    val legumeNom: String,
    
    // Nom de la variété si précisée (ex : "Grand Vert")
    // Correspond à VarieteEntity.nom, ou null si variété standard
    val varieteNom: String? = null,
    
    // Emoji du légume (hérité au moment de l'ajout)
    // Ex : "🌿" pour Basilic
    val emoji: String = "🌱",
    
    // === INFORMATIONS DE STOCK ===
    // Nombre de plants de cette variété
    val quantite: Int = 1,
    
    // === STADE DE DÉVELOPPEMENT ===
    // Valeurs possibles : "Semis", "Repiqué", "Prêt à planter", "Endurci"
    // Liste fixe définie dans JeunePlantStades (constante)
    val stade: String = STADE_SEMIS,
    
    // === DATES ===
    // Date de semis en timestamp (millis) si l'utilisateur les a semés lui-même
    val dateSemis: Long? = null,
    
    // Date d'achat en timestamp (millis) si acheté en jardinerie
    val dateAchat: Long? = null,
    
    // === INFORMATIONS D'ACHAT ===
    // Nom du fournisseur (ex : "Jardiland", "Truffaut")
    val fournisseur: String? = null,
    
    // === EMPLACEMENT ===
    // Où se trouve actuellement le plant
    // Ex : "Godet", "Mini-serre", "Plein soleil", "Balcon"
    val emplacementActuel: String? = null,
    
    // === NOTES LIBRES ===
    // Ex : "À repiquer dans 2 semaines"
    val notes: String? = null,
    
    // === MÉTADONNÉES ===
    // Date d'ajout dans l'app (pour trier par ajout récent)
    val dateAjout: Long = System.currentTimeMillis(),
    
    // Est-ce que ce plant est encore actif (pas encore planté) ?
    val estActif: Boolean = true
)

/**
 * Constantes des stades de développement pour un jeune plant.
 * Utilisées pour le menu déroulant et les filtres.
 */
object JeunePlantStades {
    const val SEMIS = "Semis"
    const val REPIQUE = "Repiqué"
    const val PRET_A_PLANTER = "Prêt à planter"
    const val ENDURCI = "Endurci"
    
    val TOUS = listOf(SEMIS, REPIQUE, PRET_A_PLANTER, ENDURCI)
}

// Raccourcis pour les valeurs par défaut dans la data class
private const val STADE_SEMIS = "Semis"
