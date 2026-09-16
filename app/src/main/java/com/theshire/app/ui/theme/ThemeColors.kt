package com.theshire.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.theshire.app.data.BrandingApp

/**
 * Palette de couleurs POTAGER SHIRE — Version dynamique (white-label).
 * 
 * Contient deux palettes :
 * - PotagerLightPalette : mode clair (fond crème, textes vert foncé)
 * - PotagerDarkPalette : mode sombre (fond vert très foncé, textes vert très clair)
 * 
 * L'objet global `CouleursApp` expose les couleurs dynamiquement :
 * - Selon le thème actif (light ou dark)
 * - Selon le branding actif (nom/couleurs de la jardinerie)
 * 
 * Pour basculer light/dark : `CouleursApp.changerModeSombre(true/false)`
 * Pour changer le branding : via `BrandingPreferences` + `BrandingApp.recharger(context)`
 */
object CouleursApp {
    
    // ========== ÉTAT DU THÈME (LIGHT / DARK) ==========
    
    /** État observable du mode sombre. Modifié via changerModeSombre(). */
    var isDarkMode: Boolean by mutableStateOf(false)
        private set
    
    /** Change le mode et déclenche la recomposition Compose. */
    fun changerModeSombre(enabled: Boolean) {
        isDarkMode = enabled
    }
    
    // ========== PALETTE ACTIVE ==========
    
    /**
     * Récupère la palette active selon :
     * - Le mode (dark ou light)
     * - Les couleurs de branding (BrandingApp.config)
     * 
     * Les couleurs sont recalculées à chaque accès (via get()) pour
     * refléter immédiatement tout changement dans BrandingApp.config.
     */
    private val palette: AppPalette
        get() {
            // 1. Récupérer les couleurs de branding actuelles
            val config = BrandingApp.config
            
            val vertPrincipal = parseHex(config.couleurVertPrincipal, Color(0xFF5B8C5A))
            val vertClair = parseHex(config.couleurVertClair, Color(0xFF8BC34A))
            val terracotta = parseHex(config.couleurTerracotta, Color(0xFFC67B4B))
            val brunDoux = parseHex(config.couleurBrunDoux, Color(0xFF8B7355))
            
            // 2. Construire la palette selon le mode
            return if (isDarkMode) {
                construirePaletteDark(vertPrincipal, vertClair, terracotta, brunDoux)
            } else {
                construirePaletteLight(vertPrincipal, vertClair, terracotta, brunDoux)
            }
        }
    
    // ----- FONDS -----
    val Creme: Color get() = palette.fondPrincipal
    val Blanc: Color get() = palette.fondCarte
    val VertPale: Color get() = palette.fondSecondaire
    
    // ----- COULEURS D'ACCENT -----
    val VertPrincipal: Color get() = palette.vertPrincipal
    val VertClair: Color get() = palette.vertClair
    val Terracotta: Color get() = palette.terracotta
    val BrunDoux: Color get() = palette.brunDoux
    
    // ----- TEXTES -----
    val TexteFonce: Color get() = palette.textePrincipal
    
    // ----- FONDS D'ASSOCIATION (cases du jardin) -----
    val CaseVide: Color get() = palette.caseVide
    val BonneAssociation: Color get() = palette.bonneAssociation
    val NeutreAssociation: Color get() = palette.neutreAssociation
    val MauvaiseAssociation: Color get() = palette.mauvaiseAssociation
    
    // ========== CONSTRUCTION DES PALETTES ==========
    
    /**
     * Construit la palette mode CLAIR à partir des couleurs de branding.
     */
    private fun construirePaletteLight(
        vertPrincipal: Color,
        vertClair: Color,
        terracotta: Color,
        brunDoux: Color
    ): AppPalette {
        return AppPalette(
            // Fonds (fixes, indépendants du branding — gardent l'identité crème)
            fondPrincipal = Color(0xFFFAF6F0),      // Crème
            fondSecondaire = Color(0xFFE8EFE8),     // Vert pâle
            fondCarte = Color(0xFFFFFDF9),          // Blanc cassé
            
            // Accents (dynamiques selon branding)
            vertPrincipal = vertPrincipal,
            vertClair = vertClair,
            terracotta = terracotta,
            brunDoux = brunDoux,
            
            // Textes (fixes)
            textePrincipal = Color(0xFF2D3A2D),     // Vert foncé
            
            // Cases du jardin (fixes, indépendantes du branding)
            caseVide = Color(0xFFFFFFFF),           // Blanc
            bonneAssociation = Color(0xFF66BB6A).copy(alpha = 0.55f),
            neutreAssociation = Color(0xFFFFA726).copy(alpha = 0.45f),
            mauvaiseAssociation = Color(0xFFEF5350).copy(alpha = 0.55f)
        )
    }
    
    /**
     * Construit la palette mode SOMBRE à partir des couleurs de branding.
     * Les accents sont éclaircis pour ressortir sur fond sombre.
     */
    private fun construirePaletteDark(
        vertPrincipal: Color,
        vertClair: Color,
        terracotta: Color,
        brunDoux: Color
    ): AppPalette {
        return AppPalette(
            // Fonds (fixes)
            fondPrincipal = Color(0xFF1A1F1A),      // Vert très foncé
            fondSecondaire = Color(0xFF252B25),     // Vert foncé
            fondCarte = Color(0xFF2D342D),          // Vert légèrement plus clair
            
            // Accents (dynamiques, éclaircis pour contraste)
            vertPrincipal = eclaircirPourDark(vertPrincipal),
            vertClair = eclaircirPourDark(vertClair),
            terracotta = eclaircirPourDark(terracotta),
            brunDoux = eclaircirPourDark(brunDoux),
            
            // Textes (fixes)
            textePrincipal = Color(0xFFE8EFE8),     // Vert très clair
            
            // Cases du jardin (fixes)
            caseVide = Color(0xFF3A423A),           // Gris-vert
            bonneAssociation = Color(0xFF4CAF50).copy(alpha = 0.60f),
            neutreAssociation = Color(0xFFFF9800).copy(alpha = 0.55f),
            mauvaiseAssociation = Color(0xFFE53935).copy(alpha = 0.60f)
        )
    }
    
    /**
     * Éclaircit une couleur pour améliorer le contraste en mode sombre.
     * 
     * Méthode : mélange avec du blanc à hauteur de 25%.
     * Si la couleur est déjà claire (> 0.7 de luminosité), on la garde telle quelle.
     */
    private fun eclaircirPourDark(couleur: Color): Color {
        val luminance = 0.299f * couleur.red + 0.587f * couleur.green + 0.114f * couleur.blue
        if (luminance > 0.7f) return couleur
        
        // Mélange 75% couleur + 25% blanc
        return Color(
            red = couleur.red + (1f - couleur.red) * 0.25f,
            green = couleur.green + (1f - couleur.green) * 0.25f,
            blue = couleur.blue + (1f - couleur.blue) * 0.25f,
            alpha = couleur.alpha
        )
    }
    
    /**
     * Convertit une chaîne hexadécimale "#RRGGBB" ou "#AARRGGBB" en Color.
     * Si la chaîne est invalide, retourne la couleur de fallback.
     */
    private fun parseHex(hex: String, fallback: Color): Color {
        return try {
            val cleaned = hex.trim().removePrefix("#")
            when (cleaned.length) {
                6 -> Color(android.graphics.Color.parseColor("#FF$cleaned"))
                8 -> Color(android.graphics.Color.parseColor("#$cleaned"))
                else -> fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }
}

/**
 * Structure de palette : toutes les couleurs utilisées dans l'app.
 * 
 * Cette structure est construite dynamiquement à chaque accès
 * (voir CouleursApp.palette) à partir du branding courant.
 */
data class AppPalette(
    // Fonds
    val fondPrincipal: Color,
    val fondSecondaire: Color,
    val fondCarte: Color,
    
    // Accents
    val vertPrincipal: Color,
    val vertClair: Color,
    val terracotta: Color,
    val brunDoux: Color,
    
    // Textes
    val textePrincipal: Color,
    
    // Cases du jardin
    val caseVide: Color,
    val bonneAssociation: Color,
    val neutreAssociation: Color,
    val mauvaiseAssociation: Color
)
