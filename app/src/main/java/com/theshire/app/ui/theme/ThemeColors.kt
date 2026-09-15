package com.theshire.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Palette de couleurs POTAGER SHIRE
 * 
 * Contient deux palettes :
 * - PotagerLightPalette : mode clair (fond crème, textes vert foncé)
 * - PotagerDarkPalette : mode sombre (fond vert très foncé, textes vert très clair)
 * 
 * L'objet global `CouleursApp` expose les couleurs dynamiquement
 * selon le thème actif (light ou dark). Il suffit d'appeler
 * `CouleursApp.setDarkMode(true/false)` pour basculer.
 * 
 * Note : les palettes sont nommées PotagerLightPalette/PotagerDarkPalette
 * pour éviter le conflit avec LightColors/DarkColors du Material 3 dans Theme.kt.
 */
object CouleursApp {
    
    // ========== ÉTAT DU THÈME ==========
    
    /** État observable du mode sombre. Modifié via setDarkMode(). */
    var isDarkMode: Boolean by mutableStateOf(false)
        private set
    
    /** Change le mode et déclenche la recomposition Compose. */
    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }
    
    // ========== PALETTE ACTIVE ==========
    
    // Récupère la palette active selon le mode
    private val palette: AppPalette
        get() = if (isDarkMode) PotagerDarkPalette else PotagerLightPalette
    
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
}

/**
 * Structure de palette : toutes les couleurs utilisées dans l'app.
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

// ============================================================
// PALETTE CLAIRE (mode actuel)
// ============================================================

val PotagerLightPalette = AppPalette(
    // Fonds
    fondPrincipal = Color(0xFFFAF6F0),      // Crème
    fondSecondaire = Color(0xFFE8EFE8),     // Vert pâle
    fondCarte = Color(0xFFFFFDF9),          // Blanc cassé
    
    // Accents
    vertPrincipal = Color(0xFF5B8C5A),      // Vert forêt
    vertClair = Color(0xFF8BC34A),          // Vert clair
    terracotta = Color(0xFFC67B4B),         // Terracotta
    brunDoux = Color(0xFF8B7355),           // Brun doux
    
    // Textes
    textePrincipal = Color(0xFF2D3A2D),     // Vert foncé
    
    // Cases du jardin
    caseVide = Color(0xFFFFFFFF),           // Blanc
    bonneAssociation = Color(0xFF66BB6A).copy(alpha = 0.55f),
    neutreAssociation = Color(0xFFFFA726).copy(alpha = 0.45f),
    mauvaiseAssociation = Color(0xFFEF5350).copy(alpha = 0.55f)
)

// ============================================================
// PALETTE SOMBRE
// ============================================================

val PotagerDarkPalette = AppPalette(
    // Fonds
    fondPrincipal = Color(0xFF1A1F1A),      // Vert très foncé (fond principal)
    fondSecondaire = Color(0xFF252B25),     // Vert foncé (fonds secondaires)
    fondCarte = Color(0xFF2D342D),          // Vert légèrement plus clair (cartes)
    
    // Accents (plus lumineux pour contraste sur fond sombre)
    vertPrincipal = Color(0xFF7FB87D),      // Vert clair lumineux
    vertClair = Color(0xFFA5D67E),          // Vert très clair
    terracotta = Color(0xFFE89B6B),         // Terracotta plus lumineux
    brunDoux = Color(0xFFB89980),           // Brun plus clair
    
    // Textes
    textePrincipal = Color(0xFFE8EFE8),     // Vert très clair (presque blanc)
    
    // Cases du jardin (couleurs plus vives pour ressortir sur fond sombre)
    caseVide = Color(0xFF3A423A),           // Gris-vert pour les cases vides
    bonneAssociation = Color(0xFF4CAF50).copy(alpha = 0.60f),
    neutreAssociation = Color(0xFFFF9800).copy(alpha = 0.55f),
    mauvaiseAssociation = Color(0xFFE53935).copy(alpha = 0.60f)
)
