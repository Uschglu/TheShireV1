package com.theshire.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette chaleureuse inspirée du jardin
private val LightColors = lightColorScheme(
    // Vert principal - plus doux et naturel
    primary = Color(0xFF5B8C5A),        // Vert sauge
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8DC), // Vert très pâle pour les conteneurs
    
    // Orange/terracotta pour les accents
    secondary = Color(0xFFC67B4B),      // Terracotta chaud
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E6DC), // Terracotta pâle
    
    // Fond principal - crème chaud qui évoque le papier kraft
    background = Color(0xFFFAF6F0),     // Crème très doux
    onBackground = Color(0xFF2D3A2D),   // Vert foncé pour le texte
    
    // Surface pour les cartes - blanc cassé chaleureux
    surface = Color(0xFFFFFDF9),        // Blanc crème
    onSurface = Color(0xFF2D3A2D),      // Vert foncé
    
    // Bordures et contours
    outline = Color(0xFFD4CFC4),        // Beige grisâtre
    outlineVariant = Color(0xFFE8E3D8), // Beige plus clair
    
    // Éléments supplémentaires
    error = Color(0xFFB3261E),          // Rouge pour les erreurs
    onError = Color.White,
    surfaceVariant = Color(0xFFF0EBE3), // Surface alternative
    onSurfaceVariant = Color(0xFF5A5A4A),
    
    // Autres couleurs utiles
    tertiary = Color(0xFF8B7355),       // Brun doux
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E6D8), // Brun pâle
    onTertiaryContainer = Color(0xFF4A3D2E)
)

// Couleurs supplémentaires pour le jardin
object CouleursJardin {
    // Dégradés pour les fonds
    val DegradeFond = listOf(
        Color(0xFFFAF6F0),  // Crème en haut
        Color(0xFFF0F0E8),  // Vert pâle au centre
        Color(0xFFE8EFE8)   // Vert plus marqué en bas
    )
    
    // Couleurs des cartes
    val CarteVerte = Color(0xFFE8F0E8)     // Vert très pâle
    val CarteTerracotta = Color(0xFFF5E6DC) // Terracotta pâle
    val CarteBrun = Color(0xFFF0E6D8)      // Brun pâle
    val CarteBlancCreme = Color(0xFFFFFDF9) // Blanc crème
    
    // Couleurs des bordures
    val BordureVerte = Color(0xFF5B8C5A)
    val BordureTerracotta = Color(0xFFC67B4B)
    val BordureDouce = Color(0xFFD4CFC4)
    
    // Dégradés pour les cartes
    val DegradeCarteVerte = listOf(
        Color(0xFFF0F5F0),  // Très pâle en haut
        Color(0xFFE0EBE0)   // Un peu plus foncé en bas
    )
    
    val DegradeCarteTerracotta = listOf(
        Color(0xFFFAF0EB),  // Très pâle en haut
        Color(0xFFF0DCCB)   // Plus chaud en bas
    )
    
    val DegradeCarteTerre = listOf(
        Color(0xFFF8F0E8),  // Crème en haut
        Color(0xFFE8D5C0)   // Terre en bas
    )
}

@Composable
fun PotagerShireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
