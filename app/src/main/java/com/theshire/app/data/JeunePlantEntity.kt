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
 * Utilisé à deux endroits :
 *  - Onglet "Plants" de l'écran Stocks : inventaire des plants possédés
 *  - Onglet "Semis" de l'écran Jardin : suivi du cycle de vie d'un semis
 * 
 * Le cycle de vie est défini dans JeunePlantEtapes (7 étapes) :
 * Semis → Levée → Repiqué → Rempoté → Prêt à planter → Endurci → Planté
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
    // Valeurs possibles : voir JeunePlantEtapes
    val stade: String = JeunePlantEtapes.SEMIS,
    
    // === DATES DU CYCLE DE VIE ===
    val dateSemis: Long? = null,
    val dateLevee: Long? = null,
    val dateRepiquage: Long? = null,
    val dateRempotage: Long? = null,
    val dateEndurcissement: Long? = null,
    val datePlantation: Long? = null,
    val dateAchat: Long? = null,
    
    // === INFORMATIONS D'ACHAT ===
    val fournisseur: String? = null,
    
    // === EMPLACEMENT ===
    val emplacementActuel: String? = null,
    
    // === HISTORIQUE LÉGER ===
    // Format : "Semis:1234567890|Levée:1234600000|"
    val historiqueEtapes: String? = null,
    
    // === NOTES LIBRES ===
    val notes: String? = null,
    
    // === MÉTADONNÉES ===
    val dateAjout: Long = System.currentTimeMillis(),
    val estActif: Boolean = true
)
