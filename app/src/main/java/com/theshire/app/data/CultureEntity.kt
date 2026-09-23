package com.theshire.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant une culture active.
 *
 * Une "culture" = une plante qui pousse quelque part, avec un début de vie
 * identifiable. Elle peut être :
 *  - En pleine terre : dans une case précise (1-9) d'un carré d'une planche
 *  - En urbain      : dans un emplacement d'un contenant (pot, jardinière…)
 *
 * Chaque culture connaît :
 *  - Sa localisation (planche/carré/case OU contenant/emplacement)
 *  - La plante cultivée (légume + variété)
 *  - Sa provenance (semis promu, jeune plant, graine semée direct, ou aucune
 *    source — plant offert/trouvé)
 *  - Son mode (projection ou réel) hérité du mode actif au moment de la plantation
 *
 * ⚠️ Évolution prévue (V2) : le champ `quantite` sera calculé automatiquement
 *    à partir de la densité de la plante (via LegumeEntity.plantation) et de
 *    la surface de la case / de l'emplacement. En V1, `quantite = 1` par défaut.
 *
 * Cycle de vie :
 *  - Créée quand on plante en pleine terre ou en urbain
 *  - Reste `estActive = true` tant qu'elle pousse
 *  - Devient `estActive = false` quand elle est récoltée ou retirée
 */
@Entity(
    tableName = "cultures",
    indices = [
        Index(value = ["plancheId"]),
        Index(value = ["carreId"]),
        Index(value = ["contenantId"]),
        Index(value = ["estActive"]),
        Index(value = ["typeEmplacement"])
    ]
)
data class CultureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // ============================================================
    // LOCALISATION
    // ============================================================
    
    /**
     * Type d'emplacement :
     *  - "PleineTerre" : culture dans un carré d'une planche
     *  - "Urbain"      : culture dans un emplacement d'un contenant
     */
    val typeEmplacement: String,
    
    // --- Pleine terre ---
    val plancheId: Long? = null,
    val carreId: Long? = null,
    val caseNumero: Int? = null,       // 1-9 (cf. CarreEntity.case1..9)
    
    // --- Urbain ---
    val contenantId: Long? = null,
    val emplacementNumero: Int? = null,
    
    // ============================================================
    // LA PLANTE
    // ============================================================
    
    val legumeNom: String,             // "Tomate"
    val varieteNom: String? = null,    // "Marmande"
    val emoji: String = "🌱",
    
    /**
     * Nombre de plants dans cette culture.
     * 
     * ⚠️ V1 : toujours 1 (une case = un plant, un emplacement = un plant).
     * ⚠️ V2 prévue : calculé automatiquement depuis la densité de la plante
     *    (via LegumeEntity.plantation) et la surface disponible.
     */
    val quantite: Int = 1,
    
    // ============================================================
    // PROVENANCE (source stock)
    // ============================================================
    
    /**
     * Source de la plante :
     *  - "Semis"      : provient d'un semis promu (ou non)
     *  - "JeunePlant" : provient d'un jeune plant (promu ou acheté)
     *  - "Graine"     : graine semée directement en place
     *  - "Aucune"     : plant offert, trouvé, origine inconnue
     */
    val sourceStock: String,
    
    /**
     * ID de l'entité source (JeunePlantEntity.id pour Semis/JeunePlant,
     * GraineEntity.id pour Graine). Null si sourceStock = "Aucune".
     */
    val sourceStockId: Long? = null,
    
    // ============================================================
    // MODE
    // ============================================================
    
    /**
     * true  : culture créée en mode projection (aucun impact sur les stocks)
     * false : culture créée en mode réel (le stock source a été décrémenté)
     * 
     * Hérité du mode actif au moment de la plantation.
     */
    val estProjection: Boolean = true,
    
    // ============================================================
    // DATES
    // ============================================================
    
    val datePlantation: Long = System.currentTimeMillis(),
    
    /**
     * Date de récolte prévue (calculée depuis le cycle cultural du légume
     * si l'info est disponible — TODO V2, laissé null en V1).
     */
    val dateRecoltePrevue: Long? = null,
    
    /**
     * Date de récolte réelle (renseignée au moment de la récolte).
     */
    val dateRecolteReelle: Long? = null,
    
    // ============================================================
    // ÉTAT
    // ============================================================
    
    /**
     * true  : la culture est active (la plante pousse)
     * false : la culture est terminée (récoltée ou retirée)
     */
    val estActive: Boolean = true,
    
    val notes: String? = null,
    
    // ============================================================
    // MÉTADONNÉES
    // ============================================================
    
    val dateAjout: Long = System.currentTimeMillis()
) {
    companion object {
        // Types d'emplacement
        const val TYPE_PLEINE_TERRE = "PleineTerre"
        const val TYPE_URBAIN = "Urbain"
        
        // Sources de stock
        const val SOURCE_SEMIS = "Semis"
        const val SOURCE_JEUNE_PLANT = "JeunePlant"
        const val SOURCE_GRAINE = "Graine"
        const val SOURCE_AUCUNE = "Aucune"
        
        /** Toutes les sources valides. */
        val SOURCES = listOf(
            SOURCE_SEMIS,
            SOURCE_JEUNE_PLANT,
            SOURCE_GRAINE,
            SOURCE_AUCUNE
        )
    }
    
    /**
     * Retourne un libellé lisible de la source pour l'affichage.
     */
    fun libelleSource(): String = when (sourceStock) {
        SOURCE_SEMIS -> "🌰 Semis"
        SOURCE_JEUNE_PLANT -> "🌿 Jeune plant"
        SOURCE_GRAINE -> "🫘 Graine directe"
        SOURCE_AUCUNE -> "🚫 Aucune (offert/trouvé)"
        else -> "❓ Inconnu"
    }
    
    /**
     * Retourne un libellé de la localisation pour l'affichage.
     */
    fun libelleLocalisation(): String = when (typeEmplacement) {
        TYPE_PLEINE_TERRE -> {
            if (caseNumero != null) "🌾 Pleine terre · case $caseNumero"
            else "🌾 Pleine terre"
        }
        TYPE_URBAIN -> {
            if (emplacementNumero != null) "🏙️ Urbain · emplacement $emplacementNumero"
            else "🏙️ Urbain"
        }
        else -> "❓"
    }
}
