package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une opération culturale à réaliser.
 * 
 * Ces rappels sont générés AUTOMATIQUEMENT quand on plante un légume
 * soit dans un carré de pleine terre (planche) soit dans un contenant urbain.
 * 
 * Chaque opération a une période flexible (dateDebut → dateFin) qui correspond
 * au créneau pendant lequel l'action peut être réalisée.
 * 
 * CONTEXTE :
 * - Si carreId != null → rappel pour un carré de pleine terre
 * - Si contenantId != null → rappel pour un contenant urbain
 * - Les deux ne peuvent pas être non-null en même temps (contrainte logique)
 */
@Entity(tableName = "rappels_culturels")
data class RappelCulturelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // --- Identification de la plante concernée ---
    val legumeNom: String,               // "Tomate", "Pomme de terre", "Carotte"
    val varieteNom: String? = null,      // "Tomate (Marmande)" si variété spécifique
    
    // --- Identification de l'opération ---
    val typeOperation: String,           // "Tuteurage", "Buttage", "Éclaircissage"
    val emoji: String,                   // "🪴", "⛰️", "✂️"
    val description: String,             // "Installer les tuteurs à 10 cm des plants"
    val conseil: String = "",            // Conseil supplémentaire optionnel
    val couleurHex: String = "#FFA726",  // Couleur pour l'affichage
    
    // --- Dates (période flexible) ---
    val dateIdeale: Long,                // Date idéale (en millisecondes)
    val dateDebut: Long,                 // Début de la fenêtre (dateIdeale - 2 jours)
    val dateFin: Long,                   // Fin de la fenêtre (dateIdeale + 2 jours)
    
    // --- Localisation dans le jardin (PLEINE TERRE) ---
    // Nullable car peut être un rappel urbain
    val carreId: Long? = null,           // ID du carré concerné (null si urbain)
    val caseNumero: Int? = null,         // Numéro de la case 1-9 (null si urbain)
    val plancheId: Long? = null,         // ID de la planche (null si urbain)
    
    // --- Localisation dans un CONTENANT URBAIN ---
    // Nullable car peut être un rappel pleine terre
    val contenantId: Long? = null,       // ID du contenant (null si pleine terre)
    val emplacementNumero: Int? = null,  // Numéro d'emplacement (null si pleine terre)
    
    // --- Statut ---
    val estActif: Boolean = true,        // false si la plantation a été supprimée
    val estTermine: Boolean = false,     // true si marqué comme fait
    val dateRealisation: Long? = null,   // Date à laquelle l'opération a été effectuée
    
    // --- Notification ---
    val notificationEnvoyee: Boolean = false,
    val notificationHeure: Int = 9,
    val notificationMinute: Int = 0
) {
    
    /**
     * Retourne true si ce rappel est lié à un carré de pleine terre.
     */
    fun estPleineTerre(): Boolean {
        return carreId != null && contenantId == null
    }
    
    /**
     * Retourne true si ce rappel est lié à un contenant urbain.
     */
    fun estUrbain(): Boolean {
        return contenantId != null && carreId == null
    }
    
    /**
     * Retourne un texte court de localisation pour l'affichage.
     * Ex : "Case 5" (pleine terre) ou "Emplacement 3" (urbain)
     */
    fun localisationTexte(): String {
        return when {
            estPleineTerre() -> "Case ${caseNumero ?: "?"}"
            estUrbain() -> "Emplacement ${emplacementNumero ?: "?"}"
            else -> "—"
        }
    }
}
