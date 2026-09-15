package com.theshire.app.data

import android.content.Context

/**
 * Gestion des préférences utilisateur pour le thème.
 * 
 * Sauvegarde le choix du mode sombre dans SharedPreferences
 * pour qu'il soit conservé entre les sessions.
 * 
 * Usage :
 * - Appeler `chargerModeSombre()` au démarrage de l'app
 * - Appeler `sauvegarderModeSombre(true/false)` quand l'utilisateur change
 */
object ThemePreferences {
    
    // Nom du fichier de préférences (séparé de jardin_prefs pour clarté)
    private const val PREFS_NAME = "potager_theme_prefs"
    
    // Clé pour le mode sombre
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    
    /**
     * Charge le mode sombre depuis les SharedPreferences.
     * Retourne false par défaut (mode clair).
     */
    fun chargerModeSombre(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    /**
     * Sauvegarde le mode sombre dans les SharedPreferences.
     */
    fun sauvegarderModeSombre(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    /**
     * Bascule le mode sombre et retourne la nouvelle valeur.
     * Fonction utilitaire pour simplifier le toggle.
     */
    fun toggleModeSombre(context: Context): Boolean {
        val nouveauMode = !chargerModeSombre(context)
        sauvegarderModeSombre(context, nouveauMode)
        return nouveauMode
    }
    
    /**
     * Réinitialise les préférences de thème (retour au mode clair).
     */
    fun resetTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
