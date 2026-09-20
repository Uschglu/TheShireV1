package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant un sachet de graines possédé par l'utilisateur.
 * 
 * Utilisé dans l'onglet "Graines" de l'écran Stocks.
 * 
 * TODO : ajouter un champ "prix" quand un partenaire magasin sera identifié
 *        (permettra de calculer le coût réel du potager).
 */
@Entity(tableName = "graines")
data class GraineEntity(
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // === LIEN AVEC LA BIBLIOTHÈQUE ===
    // Le nom du légume parent (ex : "Tomate")
    // Correspond à LegumeEntity.nom
    val legumeNom: String,
    
    // Nom de la variété si précisée (ex : "Marmande")
    // Correspond à VarieteEntity.nom, ou null si variété standard
    val varieteNom: String? = null,
    
    // Emoji du légume (hérité au moment de l'ajout)
    // Ex : "🍅" pour Tomate
    val emoji: String = "🌱",
    
    // === INFORMATIONS DE STOCK ===
    // Nombre de graines restantes dans le sachet
    val quantite: Int = 0,
    
    // Année de production/récolte des graines (ex : 2024)
    val anneeRecolte: Int? = null,
    
    // === INFORMATIONS D'ACHAT ===
    // Nom du fournisseur (ex : "Kokopelli", "Sainte-Marthe", "Jardiland")
    val fournisseur: String? = null,
    
    // Date d'achat en timestamp (millis), null si inconnu
    val dateAchat: Long? = null,
    
    // Date de péremption estimée (timestamp), null si pas de date
    // Note : les graines gardent leur pouvoir germinatif 2-5 ans selon les espèces
    val datePeremption: Long? = null,
    
    // === NOTES LIBRES ===
    // Ex : "Sachet ouvert, à utiliser en priorité"
    val notes: String? = null,
    
    // === MÉTADONNÉES ===
    // Date d'ajout dans l'app (pour trier par ajout récent)
    val dateAjout: Long = System.currentTimeMillis(),
    
    // Est-ce que le sachet est actuellement utilisé (pas terminé) ?
    val estActif: Boolean = true
)
