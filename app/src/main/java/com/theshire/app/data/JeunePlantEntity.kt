package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant un jeune plant ou un semis en cours.
 *
 * Deux catégories distinctes, stockées dans le champ `categorie` :
 *  - "Semis"       : semis en cours (cycle actif, de Semis à Endurci).
 *                    Affiché dans l'onglet Semis du Jardin.
 *  - "JeunePlant"  : plant promu après le stade Rempoté (ou plant acheté
 *                    en jardinerie, bouture…). Affiché dans Stocks > Plants.
 *
 * Le passage Semis → JeunePlant se fait manuellement via le bouton
 * "🌿 Promouvoir en jeune plant" dans FicheSemis (à partir de Rempoté).
 *
 * Utilisé à deux endroits :
 *  - Onglet "Plants" de l'écran Stocks : inventaire global (2 catégories, tous modes)
 *  - Onglet "Semis" de l'écran Jardin : suivi du cycle (catégorie = "Semis", filtré par mode)
 *
 * Le cycle de vie est défini dans JeunePlantEtapes (7 étapes) :
 * Semis → Levée → Repiqué → Rempoté → Prêt à planter → Endurci → Planté
 *
 * Le champ estProjection détermine si ce semis a été créé en mode projection
 * (suivi sans impact sur les stocks) ou en mode réel (décrément des graines).
 * Il est hérité au moment de la promotion (projeté → jeune plant projeté).
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
    
    // === CATÉGORIE ===
    // "Semis"      : semis en cours (affiché dans Jardin > Semis)
    // "JeunePlant" : jeune plant promu ou acheté (affiché dans Stocks > Plants)
    val categorie: String = CATEGORIE_SEMIS,
    
    // === STADE DE DÉVELOPPEMENT ===
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
    
    // === MODE DE CRÉATION ===
    // true  = créé en mode projection (suivi sans impact sur les stocks)
    // false = créé en mode réel (les graines ont été décrémentées du stock)
    // 
    // L'onglet Semis du Jardin affiche uniquement les semis correspondant
    // au mode actuellement actif. L'onglet Plants de Stocks affiche tout.
    val estProjection: Boolean = true,
    
    // === MÉTADONNÉES ===
    val dateAjout: Long = System.currentTimeMillis(),
    val estActif: Boolean = true
) {
    companion object {
        /** Catégorie : semis en cours (avant promotion). */
        const val CATEGORIE_SEMIS = "Semis"

        /** Catégorie : jeune plant (après promotion ou achat). */
        const val CATEGORIE_JEUNE_PLANT = "JeunePlant"

        /** Toutes les catégories valides. */
        val CATEGORIES = listOf(CATEGORIE_SEMIS, CATEGORIE_JEUNE_PLANT)
    }
}
