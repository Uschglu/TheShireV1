package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant un contenant urbain (pot, jardinière, suspension...).
 * 
 * Chaque contenant est défini par :
 * - Un type (pot, jardiniere, suspendu, tour, sac, bac, mur, reserve)
 * - Ses dimensions (variables selon le type)
 * - Un milieu (Balcon, Intérieur, Terrasse)
 * 
 * Les EMPLACEMENTS (plants individuels) sont dans EmplacementContenantEntity
 * et sont liés par contenantId.
 * 
 * Le nombre d'emplacements est calculé automatiquement en fonction :
 * - De la surface du contenant
 * - De la densité urbaine de la plante (densité pleine terre × 2)
 */
@Entity(tableName = "contenants")
data class ContenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // Identification
    val nom: String,                    // "Balcon Sud", "Pot tomate n°1"
    val type: String,                   // "pot" | "jardiniere" | "suspendu" | "tour" | "sac" | "bac" | "mur" | "reserve"
    val emoji: String,                  // 🪴, 📦, 🪝, etc.
    
    // Dimensions (cm) — selon le type, certaines sont à 0
    val dimension1: Int = 0,            // Diamètre OU Longueur
    val dimension2: Int = 0,            // Largeur (jardinière/bac/mur) — 0 sinon
    val dimension3: Int = 0,            // Hauteur (tour/mur/bac) — 0 sinon
    val nombreEtages: Int = 1,          // Pour tours empilables et murs végétaux
    
    // Contexte
    val milieu: String = "Balcon",      // "Balcon" | "Intérieur" | "Terrasse"
    val notes: String = "",             // Notes libres
    
    // Métadonnées
    val dateCreation: Long = System.currentTimeMillis()
) {
    
    /**
     * Calcule la surface utile en cm² selon le type de contenant.
     * 
     * - Pot, suspendu, sac, réserve : disque = π × (diamètre/2)²
     * - Jardinière, bac : rectangle = longueur × largeur
     * - Tour empilable : disque × nombre d'étages (chaque étage = 1 anneau)
     * - Mur végétal : longueur × hauteur (poches réparties)
     */
    fun surfaceCm2(): Double {
        return when (type) {
            "pot", "suspendu", "sac", "reserve" -> {
                val rayon = dimension1 / 2.0
                Math.PI * rayon * rayon
            }
            "jardiniere", "bac" -> {
                dimension1.toDouble() * dimension2.toDouble()
            }
            "tour" -> {
                val rayon = dimension1 / 2.0
                Math.PI * rayon * rayon * nombreEtages
            }
            "mur" -> {
                // Mur végétal : poches verticales sur la hauteur
                dimension1.toDouble() * dimension3.toDouble()
            }
            else -> 0.0
        }
    }
    
    /**
     * Retourne la surface utile en m².
     */
    fun surfaceM2(): Double = surfaceCm2() / 10000.0
    
    /**
     * Décrit les dimensions pour l'affichage.
     */
    fun dimensionsTexte(): String {
        return when (type) {
            "pot", "suspendu", "sac", "reserve" -> "Ø ${dimension1} cm"
            "jardiniere", "bac" -> "${dimension1} × ${dimension2} cm"
            "tour" -> "Ø ${dimension1} cm × ${nombreEtages} étages"
            "mur" -> "${dimension1} × ${dimension3} cm"
            else -> "—"
        }
    }
}
