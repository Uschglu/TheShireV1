package com.theshire.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf

/**
 * Gestion des préférences liées aux outils de jardinage.
 * 
 * Stocke la liste des outils que l'utilisateur POSSÈDE.
 * Le reste = outils manquants (utilisés dans l'onglet Store).
 * 
 * Stockage : SharedPreferences "potager_outils_prefs"
 * 
 * L'objet global `OutilsApp` expose la liste de manière observable
 * (Compose) : toute modification déclenche une recomposition.
 */
object OutilsPreferences {
    
    private const val PREFS_NAME = "potager_outils_prefs"
    private const val KEY_OUTILS_POSSEDES = "outils_possedes"
    
    /**
     * Charge la liste des IDs d'outils possédés.
     * Retourne une liste vide si aucune donnée.
     */
    fun chargerOutilsPossedes(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stocke = prefs.getString(KEY_OUTILS_POSSEDES, null) ?: return emptySet()
        if (stocke.isEmpty()) return emptySet()
        return stocke.split(",").filter { it.isNotEmpty() }.toSet()
    }
    
    /**
     * Sauvegarde la liste complète des IDs d'outils possédés.
     */
    fun sauvegarderOutilsPossedes(context: Context, ids: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_OUTILS_POSSEDES, ids.joinToString(",")).apply()
    }
    
    /**
     * Ajoute un outil à la liste des outils possédés.
     */
    fun ajouterOutil(context: Context, outilId: String) {
        val actuels = chargerOutilsPossedes(context).toMutableSet()
        actuels.add(outilId)
        sauvegarderOutilsPossedes(context, actuels)
    }
    
    /**
     * Retire un outil de la liste des outils possédés.
     */
    fun retirerOutil(context: Context, outilId: String) {
        val actuels = chargerOutilsPossedes(context).toMutableSet()
        actuels.remove(outilId)
        sauvegarderOutilsPossedes(context, actuels)
    }
    
    /**
     * Bascule la possession d'un outil (possédé ↔ non possédé).
     * Retourne le nouvel état.
     */
    fun toggleOutil(context: Context, outilId: String): Boolean {
        val actuels = chargerOutilsPossedes(context).toMutableSet()
        val nouveauEtat = if (actuels.contains(outilId)) {
            actuels.remove(outilId)
            false
        } else {
            actuels.add(outilId)
            true
        }
        sauvegarderOutilsPossedes(context, actuels)
        return nouveauEtat
    }
    
    /**
     * Vérifie si l'utilisateur possède un outil donné.
     */
    fun possedeOutil(context: Context, outilId: String): Boolean {
        return chargerOutilsPossedes(context).contains(outilId)
    }
    
    /**
     * Réinitialise la liste (décoche tous les outils).
     */
    fun reinitialiser(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

/**
 * État observable des outils pour Compose.
 * 
 * Chargé UNE FOIS au démarrage via `OutilsApp.initialiser(context)`.
 * Toute modification via `OutilsApp.toggle(context, outilId)` déclenche
 * une recomposition de tous les composables qui lisent sa liste.
 */
object OutilsApp {
    
    /**
     * Liste observable des IDs d'outils possédés.
     * Utiliser `contient(outilId)` pour tester la possession.
     */
    val outilsPossedes = mutableStateListOf<String>()
    
    /**
     * Charge la liste depuis SharedPreferences.
     * À appeler dans MainActivity.onCreate() avant setContent {}.
     */
    fun initialiser(context: Context) {
        outilsPossedes.clear()
        outilsPossedes.addAll(OutilsPreferences.chargerOutilsPossedes(context))
    }
    
    /**
     * Bascule la possession d'un outil et met à jour l'état observable.
     * Retourne le nouvel état.
     */
    fun toggle(context: Context, outilId: String): Boolean {
        val nouveauEtat = OutilsPreferences.toggleOutil(context, outilId)
        outilsPossedes.clear()
        outilsPossedes.addAll(OutilsPreferences.chargerOutilsPossedes(context))
        return nouveauEtat
    }
    
    /**
     * Recharge la liste depuis SharedPreferences.
     * Utile si une modification externe a eu lieu.
     */
    fun recharger(context: Context) {
        outilsPossedes.clear()
        outilsPossedes.addAll(OutilsPreferences.chargerOutilsPossedes(context))
    }
    
    /**
     * Vérifie si un outil est possédé (version observable).
     */
    fun possede(outilId: String): Boolean {
        return outilsPossedes.contains(outilId)
    }
    
    /**
     * Retourne la liste des IDs d'outils NON possédés (outils manquants).
     */
    fun outilsManquants(tousLesIds: List<String>): List<String> {
        return tousLesIds.filter { !outilsPossedes.contains(it) }
    }
}
