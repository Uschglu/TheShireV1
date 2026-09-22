package com.theshire.app.data

import android.content.Context

/**
 * Préférences utilisateur pour le mode de suivi du jardin.
 *
 * Deux modes :
 *  - PROJECTION : mode par défaut. L'utilisateur simule / projette ses cultures
 *    sans impacter les stocks. Décrémenter les graines et sortir les plants
 *    du stock ne se fait PAS automatiquement.
 *  - REEL : mode avancé (potentiellement lié à un abonnement plus tard).
 *    Quand l'utilisateur crée un semis, les graines correspondantes sont
 *    décrémentées du stock. Quand un plant est marqué "planté", il sort
 *    du stock jeunes plants.
 *
 * Persistance via SharedPreferences (léger, pas de DB nécessaire).
 *
 * Note : à terme, ce mode pourra être gated par un abonnement freemium.
 *        La logique de gate se fera ailleurs (au niveau UI).
 */
object ModePreferences {
    
    private const val PREFS_NAME = "mode_prefs"
    private const val KEY_MODE_REEL = "mode_reel"
    
    /**
     * Est-ce que le mode réel est activé ?
     * Par défaut : false (mode projection).
     */
    fun estModeReel(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MODE_REEL, false)
    }
    
    /**
     * Active ou désactive le mode réel.
     */
    fun setModeReel(context: Context, actif: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MODE_REEL, actif).apply()
    }
    
    /**
     * Bascule entre les deux modes.
     * @return Le nouvel état (true = réel, false = projection).
     */
    fun basculerMode(context: Context): Boolean {
        val nouveau = !estModeReel(context)
        setModeReel(context, nouveau)
        return nouveau
    }
}
