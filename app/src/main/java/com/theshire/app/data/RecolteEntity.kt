package com.theshire.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant une récolte enregistrée.
 *
 * Une "récolte" = un poids de légumes effectivement obtenu, à un instant donné.
 * Elle peut provenir :
 *  - D'une culture suivie dans l'app (lien vers CultureEntity via cultureId)
 *    → dans ce cas, elle a été générée automatiquement quand on a cliqué
 *      sur "Récolter" depuis la fiche d'une culture
 *  - D'un ajout manuel (marché, don, cueillette sauvage…)
 *    → dans ce cas, cultureId est null
 *
 * Les récoltes sont affichées dans le 4e onglet "Récoltes" de l'écran Stocks.
 * 
 * ⚠️ Les quantités sont exprimées en kilogrammes (kg), avec une précision
 *    au gramme (double).
 *
 * ⚠️ MODE PROJECTION vs RÉEL :
 *    - estProjection = true  → récolte "projetée" (mode simulation)
 *    - estProjection = false → récolte "réelle" (mode suivi réel)
 * 
 *    Ce champ permet de filtrer les récoltes selon le mode actif, comme
 *    on le fait déjà pour les cultures.
 *    
 *    Règle :
 *      - Récolte auto depuis une culture → hérite du mode de la culture
 *      - Récolte ajoutée manuellement   → utilise le mode actif au moment
 *                                          de l'ajout
 */
@Entity(
    tableName = "recoltes",
    indices = [
        Index(value = ["cultureId"]),
        Index(value = ["legumeNom"]),
        Index(value = ["dateRecolte"]),
        Index(value = ["estProjection"])
    ]
)
data class RecolteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // ============================================================
    // LA PLANTE RÉCOLTÉE
    // ============================================================
    
    val legumeNom: String,             // "Tomate"
    val varieteNom: String? = null,    // "Marmande"
    val emoji: String = "🥕",
    
    // ============================================================
    // QUANTITÉ
    // ============================================================
    
    /**
     * Poids récolté, en kilogrammes.
     * Ex : 1.250 = 1 kg 250 g
     */
    val poidsKg: Double,
    
    // ============================================================
    // DATES
    // ============================================================
    
    /**
     * Date à laquelle la récolte a eu lieu.
     * Par défaut : maintenant.
     */
    val dateRecolte: Long = System.currentTimeMillis(),
    
    // ============================================================
    // LIEN AVEC LA CULTURE D'ORIGINE (facultatif)
    // ============================================================
    
    /**
     * ID de la culture d'origine si la récolte provient d'une culture suivie.
     * Null si la récolte a été ajoutée manuellement (marché, don, etc.).
     */
    val cultureId: Long? = null,
    
    // ============================================================
    // MODE (projection / réel)
    // ============================================================
    
    /**
     * true  : récolte "projetée" (mode simulation)
     * false : récolte "réelle" (mode suivi réel)
     * 
     * Par défaut : true (les récoltes existantes sont considérées comme
     * des projections — cohérent avec l'esprit du mode par défaut).
     * 
     * Héritée :
     *  - Du mode de la culture si récolte automatique (cultureId != null)
     *  - Du mode actif au moment de l'ajout si récolte manuelle
     */
    val estProjection: Boolean = true,
    
    // ============================================================
    // NOTES ET MÉTADONNÉES
    // ============================================================
    
    val notes: String? = null,
    
    val dateAjout: Long = System.currentTimeMillis()
) {
    /**
     * Retourne un libellé du poids au format français.
     * Ex : "1,250 kg" ou "0,850 kg"
     */
    fun poidsTexte(): String {
        // Formate avec 3 décimales, puis remplace le point par une virgule
        return String.format("%.3f", poidsKg).replace(".", ",") + " kg"
    }
    
    /**
     * Retourne true si la récolte provient d'une culture suivie.
     */
    fun estLieeAUneCulture(): Boolean = cultureId != null
    
    /**
     * Retourne true si la récolte est réelle (mode suivi réel).
     */
    fun estReelle(): Boolean = !estProjection
    
    /**
     * Retourne true si la récolte est une projection (mode simulation).
     */
    fun estProjetee(): Boolean = estProjection
}
