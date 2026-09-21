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
 */
@Entity(tableName = "jeunes_plants")
data class JeunePlantEntity(
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // === LIEN AVEC LA BIBLIOTHÈQUE ===
    val legumeNom: String,
    val varieteNom: String? = null,
    val emoji: String = "🌱",
    
    // === INFORMATIONS DE STOCK ===
    val quantite: Int = 1,
    
    // === STADE DE DÉVELOPPEMENT ===
    // Valeurs possibles : "Semis", "Repiqué", "Prêt à planter", "Endurci"
    val stade: String = JeunePlantStades.SEMIS,
    
    // === DATES ===
    val dateSemis: Long? = null,
    val dateAchat: Long? = null,
    
    // === INFORMATIONS D'ACHAT ===
    val fournisseur: String? = null,
    
    // === EMPLACEMENT ===
    val emplacementActuel: String? = null,
    
    // === NOTES LIBRES ===
    val notes: String? = null,
    
    // === MÉTADONNÉES ===
    val dateAjout: Long = System.currentTimeMillis(),
    val estActif: Boolean = true
)

/**
 * Constantes des stades de développement pour un jeune plant.
 */
object JeunePlantStades {
    const val SEMIS = "Semis"
    const val REPIQUE = "Repiqué"
    const val PRET_A_PLANTER = "Prêt à planter"
    const val ENDURCI = "Endurci"
    
    val TOUS = listOf(SEMIS, REPIQUE, PRET_A_PLANTER, ENDURCI)
}

private const val STADE_SEMIS = "Semis"
