package com.theshire.app.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// PALETTE MATERIAL 3 — MODE CLAIR
// ============================================================

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B8C5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8DC),
    onPrimaryContainer = Color(0xFF2D3A2D),
    
    secondary = Color(0xFFC67B4B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E6DC),
    onSecondaryContainer = Color(0xFF4A3D2E),
    
    background = Color(0xFFFAF6F0),
    onBackground = Color(0xFF2D3A2D),
    
    surface = Color(0xFFFFFDF9),
    onSurface = Color(0xFF2D3A2D),
    
    surfaceVariant = Color(0xFFF0EBE3),
    onSurfaceVariant = Color(0xFF5A5A4A),
    
    outline = Color(0xFFD4CFC4),
    outlineVariant = Color(0xFFE8E3D8),
    
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    
    tertiary = Color(0xFF8B7355),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E6D8),
    onTertiaryContainer = Color(0xFF4A3D2E)
)

// ============================================================
// PALETTE MATERIAL 3 — MODE SOMBRE
// ============================================================

private val DarkColors = darkColorScheme(
    // Vert plus lumineux pour ressortir sur fond sombre
    primary = Color(0xFF7FB87D),
    onPrimary = Color(0xFF0F2B0F),
    primaryContainer = Color(0xFF2E4A2D),
    onPrimaryContainer = Color(0xFFDCE8DC),
    
    // Terracotta plus lumineux
    secondary = Color(0xFFE89B6B),
    onSecondary = Color(0xFF3D1F0F),
    secondaryContainer = Color(0xFF5A3520),
    onSecondaryContainer = Color(0xFFF5E6DC),
    
    // Fonds sombres vert très foncé
    background = Color(0xFF1A1F1A),
    onBackground = Color(0xFFE8EFE8),
    
    // Surface = cartes en vert foncé (utilisé par AlertDialog, TextField, etc.)
    surface = Color(0xFF252B25),
    onSurface = Color(0xFFE8EFE8),
    
    surfaceVariant = Color(0xFF2D342D),
    onSurfaceVariant = Color(0xFFB8C0B8),
    
    outline = Color(0xFF4A544A),
    outlineVariant = Color(0xFF353D35),
    
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0F0F),
    errorContainer = Color(0xFF5A2020),
    onErrorContainer = Color(0xFFFFDAD6),
    
    tertiary = Color(0xFFB89980),
    onTertiary = Color(0xFF2A1F14),
    tertiaryContainer = Color(0xFF4A3D2E),
    onTertiaryContainer = Color(0xFFF0E6D8)
)

// ============================================================
// THÈME POTAGER SHIRE
// ============================================================

/**
 * Thème principal de l'app.
 * 
 * Utilise automatiquement la palette Material 3 (claire ou sombre)
 * en fonction de `CouleursApp.isDarkMode`.
 * 
 * Quand l'utilisateur bascule le mode sombre via le bouton,
 * TOUS les composants Material 3 (AlertDialog, Card, TextField,
 * DropdownMenu, etc.) s'adaptent instantanément.
 */
@Composable
fun PotagerShireTheme(content: @Composable () -> Unit) {
    val colorScheme = if (CouleursApp.isDarkMode) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// ============================================================
// UTILITAIRE : ContextThemeWrapper pour dialogues natifs
// ============================================================

/**
 * Enveloppe le contexte dans un thème sombre ou clair selon le mode actif.
 * 
 * Nécessaire pour les dialogues Android natifs (TimePickerDialog, DatePickerDialog)
 * qui n'héritent PAS du thème Compose/Material 3 et restent donc sur
 * le thème par défaut du système.
 * 
 * Usage :
