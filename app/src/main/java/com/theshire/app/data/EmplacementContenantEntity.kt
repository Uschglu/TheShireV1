package com.theshire.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant un emplacement dans un contenant urbain.
 * 
 * Un contenant contient plusieurs emplacements (1 par plant).
 * Le nombre d'emplacements est calculé automatiquement selon :
 * - La surface du contenant
 * - La densité urbaine de la plante (densité pleine terre × 2)
 * 
 * Chaque emplacement peut contenir :
 * - Un légume (avec variété éventuelle) : "Tomate (Marmande)"
 * - Une date de plantation
 * - Des notes libres
 * 
 * Si legumeNom est null → emplacement vide (disponible)
 * 
 * NOTE : Les emplacements sont supprimés automatiquement si le contenant
 * est supprimé (ON DELETE CASCADE).
 */
@Entity(
    tableName = "emplacements_contenants",
    foreignKeys = [
        ForeignKey(
            entity = ContenantEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contenantId"])]
)
data class EmplacementContenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // Lien vers le contenant parent
    val contenantId: Long,
    
    // Position dans le contenant (1, 2, 3... pour l'ordre d'affichage)
    val numero: Int,
    
    // Plant installé (null = emplacement vide)
    val legumeNom: String? = null,      // "Basilic", "Tomate (Marmande)"
    val datePlantation: Long? = null,   // Timestamp de plantation
    
    // Notes libres
    val notes: String = ""
) {
    
    /**
     * Retourne true si l'emplacement est occupé par une plante.
     */
    fun estOccupe(): Boolean = legumeNom != null
    
    /**
     * Retourne true si l'emplacement est vide.
     */
    fun estVide(): Boolean = legumeNom == null
}
