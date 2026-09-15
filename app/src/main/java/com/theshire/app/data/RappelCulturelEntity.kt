package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une opération culturale à réaliser.
 * 
 * Ces rappels sont générés AUTOMATIQUEMENT quand on plante un légume
 * dans un carré du jardin (tuteurage tomate, buttage pomme de terre, etc.)
 * 
 * Chaque opération a une période flexible (dateDebut → dateFin) qui correspond
 * au créneau pendant lequel l'action peut être réalisée.
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
    val couleurHex: String = "#FFA726",  // Couleur pour l'affichage (#FFA726 = orange par défaut)
    
    // --- Dates (période flexible) ---
    val dateIdeale: Long,                // Date idéale (en millisecondes)
    val dateDebut: Long,                 // Début de la fenêtre (dateIdeale - 2 jours)
    val dateFin: Long,                   // Fin de la fenêtre (dateIdeale + 2 jours)
    
    // --- Localisation dans le jardin ---
    val carreId: Long,                   // ID du carré concerné
    val caseNumero: Int,                 // Numéro de la case (1-9) dans le carré
    val plancheId: Long,                 // ID de la planche pour retrouver le contexte
    
    // --- Statut ---
    val estActif: Boolean = true,        // false si la plantation a été supprimée
    val estTermine: Boolean = false,     // true si l'utilisateur a marqué l'opération comme faite
    val dateRealisation: Long? = null,   // Date à laquelle l'opération a été effectuée
    
    // --- Notification ---
    val notificationEnvoyee: Boolean = false,  // true si la notif a déjà été déclenchée
    val notificationHeure: Int = 9,            // Heure de la notification (0-23)
    val notificationMinute: Int = 0            // Minute de la notification (0-59)
)
