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
 *    (créé depuis zéro par l'utilisateur, avance d'étape en étape)
 * 
 * Le cycle de vie est défini dans JeunePlantEtapes (7 étapes) :
 * Semis → Levée → Repiqué → Rempoté → Prêt à planter → Endurci → Planté
 * 
 * TODO : ajouter un champ "prix" quand un partenaire magasin sera identifié
 *        (permettra de calculer le coût réel du potager).
 * 
 * TODO (V2) : lier le semis à sa destination réelle (planche, carré, contenant)
 *        pour permettre un vrai workflow "mode plantation" (décrémenter le
 *        stock de graines, incrémenter le plant dans la planche cible…).
 *        Actuellement le mode reste "projection" : on suit le cycle, mais
 *        on ne modifie pas automatiquement les autres stocks.
 */
@Entity(tableName = "jeunes_plants")
data class JeunePlantEntity(
    
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // === LIEN AVEC LA BIBLIOTHÈQUE ===
    // Le nom du légume parent (ex : "Basilic")
    // Correspond à LegumeEntity.nom
    val legumeNom: String,
    
    // Nom de la variété si précisée (ex : "Grand Vert")
    // Correspond à VarieteEntity.nom, ou null si variété standard
    val varieteNom: String? = null,
    
    // Emoji du légume (hérité au moment de l'ajout)
    // Ex : "🌿" pour Basilic
    val emoji: String = "🌱",
    
    // === INFORMATIONS DE STOCK ===
    // Nombre de plants de cette variété
    val quantite: Int = 1,
    
    // === STADE DE DÉVELOPPEMENT ===
    // Valeurs possibles : voir JeunePlantEtapes
    // ("Semis", "Levée", "Repiqué", "Rempoté",
    //  "Prêt à planter", "Endurci", "Planté")
    val stade: String = JeunePlantEtapes.SEMIS,
    
    // === DATES DU CYCLE DE VIE ===
    // Chaque date est remplie quand le plant passe à l'étape correspondante.
    // Toutes optionnelles (le plant peut avoir été créé directement à une étape avancée).

    // Date de semis en timestamp (millis) si l'utilisateur les a semés lui-même
    val dateSemis: Long? = null,
    
    // Date de levée (germination visible) en timestamp (millis)
    val dateLevee: Long? = null,
    
    // Date de repiquage (1er changement de godet) en timestamp (millis)
    val dateRepiquage: Long? = null,
    
    // Date de rempotage (changement de contenant plus grand) en timestamp (millis)
    val dateRempotage: Long? = null,
    
    // Date d'endurcissement (sortie progressive) en timestamp (millis)
    val dateEndurcissement: Long? = null,
    
    // Date de plantation finale en timestamp (millis)
    val datePlantation: Long? = null,
    
    // Date d'achat en timestamp (millis) si acheté en jardinerie
    val dateAchat: Long? = null,
    
    // === INFORMATIONS D'ACHAT ===
    // Nom du fournisseur (ex : "Jardiland", "Truffaut")
    val fournisseur: String? = null,
    
    // === EMPLACEMENT ===
    // Où se trouve actuellement le plant
    // Ex : "Godet", "Mini-serre", "Plein soleil", "Balcon"
    val emplacementActuel: String? = null,
    
    // === HISTORIQUE LÉGER ===
    // Chaîne encodée des étapes traversées avec leur date.
    // Format : "Semis:1234567890|Levée:1234600000|Repiqué:1235000000|"
    // Rempli automatiquement à chaque changement d'étape.
    // Parsable côté UI si on veut afficher une timeline (V2).
    // Null si aucun changement d'étape n'a encore eu lieu.
    val historiqueEtapes: String? = null,
    
    // === NOTES LIBRES ===
    // Ex : "À repiquer dans 2 semaines"
    val notes: String? = null,
    
    // === MÉTADONNÉES ===
    // Date d'ajout dans l'app (pour trier par ajout récent)
    val dateAjout: Long = System.currentTimeMillis(),
    
    // Est-ce que ce plant est encore actif (pas encore planté) ?
    // Passe à false automatiquement quand on atteint l'étape "Planté".
    val estActif: Boolean = true
)
