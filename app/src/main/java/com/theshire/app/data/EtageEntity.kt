package com.theshire.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant un étage d'une tour empilable.
 * 
 * ⚠️ Uniquement utilisée pour les contenants de type "tour".
 *    Pour tous les autres types (pot, jardinière, bac, sac, suspendu,
 *    réserve, mur), la forme est déduite directement depuis
 *    ContenantEntity.type et aucun EtageEntity n'est créé.
 * 
 * Chaque étage est INDÉPENDANT :
 *  - Il a son propre nombre d'emplacements
 *  - Il peut contenir une culture différente des autres étages
 *  - Il a sa propre forme (en théorie toujours "rond" pour une tour,
 *    mais le champ est là pour extensibilité future)
 * 
 * Le nombre d'emplacements est calculé à la 1ère plantation sur l'étage
 * (comme pour un contenant classique : surface d'un anneau × densité urbaine).
 * Tant qu'aucune plantation n'a eu lieu, nombreEmplacements reste à 0.
 * 
 * NOTE : les étages sont supprimés automatiquement si le contenant
 * parent est supprimé (ON DELETE CASCADE).
 */
@Entity(
    tableName = "etages",
    foreignKeys = [
        ForeignKey(
            entity = ContenantEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["contenantId"]),
        Index(value = ["contenantId", "numero"], unique = true)
    ]
)
data class EtageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // Lien vers le contenant parent (une tour)
    val contenantId: Long,
    
    // Position dans la tour (1 = base, 2, 3... vers le haut)
    val numero: Int,
    
    /**
     * Forme de l'étage :
     *  - "rond"          : anneau circulaire (cas normal d'une tour)
     *  - "carre"         : carré (extensibilité future)
     *  - "rectangulaire" : rectangle (extensibilité future)
     */
    val forme: String = FORME_ROND,
    
    /**
     * Nombre d'emplacements disponibles sur cet étage.
     * 
     * ⚠️ 0 = pas encore défini. Le calcul se fait à la 1ère plantation
     *    sur l'étage, en fonction de :
     *    - La surface de l'anneau (diamètre × π × hauteur d'anneau)
     *    - La densité urbaine de la plante (densité pleine terre × 2)
     * 
     * Une fois défini, ce nombre ne change plus automatiquement (l'utilisateur
     * peut le modifier manuellement s'il le souhaite).
     */
    val nombreEmplacements: Int = 0,
    
    // Notes libres
    val notes: String = "",
    
    // Métadonnées
    val dateCreation: Long = System.currentTimeMillis()
) {
    companion object {
        // Formes possibles
        const val FORME_ROND = "rond"
        const val FORME_CARRE = "carre"
        const val FORME_RECTANGULAIRE = "rectangulaire"
        
        /** Toutes les formes valides. */
        val FORMES = listOf(FORME_ROND, FORME_CARRE, FORME_RECTANGULAIRE)
    }
    
    /**
     * Retourne true si l'étage a déjà été initialisé (au moins une plantation).
     */
    fun estInitialise(): Boolean = nombreEmplacements > 0
    
    /**
     * Retourne un libellé lisible de la position de l'étage.
     * Ex : "Étage 1 (base)", "Étage 3 (haut)"
     */
    fun libellePosition(): String = "Étage $numero"
}
