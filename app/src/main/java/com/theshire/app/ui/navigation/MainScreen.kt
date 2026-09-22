package com.theshire.app.ui.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theshire.app.ui.EcranStocks
import com.theshire.app.ui.EcranStore
import com.theshire.app.ui.ParametresScreen
import com.theshire.app.ui.screens.AccueilScreen
import com.theshire.app.ui.screens.BibliothequeScreen
import com.theshire.app.ui.screens.CalendrierScreen
import com.theshire.app.ui.screens.ConservationScreen
import com.theshire.app.ui.screens.JardinScreen
import com.theshire.app.ui.theme.CouleursApp

/**
 * Constantes de mise en page partagées dans toute l'app.
 *
 * Centralise les valeurs liées à la barre de navigation flottante,
 * pour que les FAB (boutons "+" des onglets) ne soient pas cachés
 * derrière elle.
 *
 * ⚠️ Si tu modifies la hauteur de la barre dans MainScreen, pense
 *    à mettre à jour HAUTEUR_BARRE_NAVIGATION ici.
 */
object LayoutConstantes {
    
    /** Hauteur totale de la barre de navigation flottante (paddings inclus). */
    val HAUTEUR_BARRE_NAVIGATION: Dp = 92.dp
    
    /** Marge minimale entre la barre et un élément qui flotte au-dessus. */
    val MARGE_AU_DESSUS_BARRE: Dp = 16.dp
    
    /**
     * Padding bottom à appliquer aux FAB pour qu'ils soient visibles
     * au-dessus de la barre de navigation.
     * = 92 + 16 = 108dp
     */
    val PADDING_BAS_FAB: Dp = HAUTEUR_BARRE_NAVIGATION + MARGE_AU_DESSUS_BARRE
}

/**
 * Retourne le dégradé de fond selon le mode sombre/clair.
 */
@Composable
fun getDegradeFond(): Brush {
    return if (CouleursApp.isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1F1A),
                Color(0xFF1F241F),
                Color(0xFF252B25)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF6F0),
                Color(0xFFF0F0E8),
                Color(0xFFE8EFE8)
            )
        )
    }
}

/**
 * Écran principal de l'application avec navigation entre les 7 sections :
 * Accueil, Bibliothèque, Jardin, Calendrier, Stocks, Conservation, Store.
 * 
 * Navigation :
 *  - Par clic sur les emojis en bas (barre flottante avec pastille sur l'actif)
 *  - Par swipe horizontal
 *  - Par bouton retour Android
 */
@Composable
fun MainScreen() {
    var currentScreen by rememberSaveable { mutableStateOf("accueil") }
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }
    val navigationStack = remember { mutableStateListOf("accueil") }
    val screens = listOf(
        "accueil", "bibliotheque", "jardin",
        "calendrier", "stocks", "conservation", "store"
    )
    
    // Mapping emoji ↔ écran
    val emojiParEcran = mapOf(
        "accueil" to "🏠",
        "bibliotheque" to "📚",
        "jardin" to "🌾",
        "calendrier" to "📅",
        "stocks" to "📦",
        "conservation" to "🥫",
        "store" to "🛒"
    )

    fun navigateTo(screen: String) {
        navigationStack.add(screen)
        currentScreen = screen
    }

    fun goBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = navigationStack.last()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 1000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = now
                android.widget.Toast.makeText(
                    context,
                    "Appuyez encore pour quitter",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun goToAccueil() {
        navigationStack.clear()
        navigationStack.add("accueil")
        currentScreen = "accueil"
    }

    BackHandler { goBack() }

    fun goToNext() {
        val i = screens.indexOf(currentScreen)
        if (i < screens.size - 1) navigateTo(screens[i + 1])
    }

    fun goToPrevious() {
        val i = screens.indexOf(currentScreen)
        if (i > 0) navigateTo(screens[i - 1])
    }

    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getDegradeFond())
            .pointerInput(currentScreen) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -200f) goToNext()
                        else if (dragOffset > 200f) goToPrevious()
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 3 } togetherWith
                    fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 3 }
            }
        ) { screen ->
            when (screen) {
                "accueil" -> AccueilScreen(
                    onNavigateToParametres = { navigateTo("parametres") }
                )
                "bibliotheque" -> BibliothequeScreen(onBack = { goToAccueil() })
                "jardin" -> JardinScreen(onBack = { goToAccueil() })
                "calendrier" -> CalendrierScreen(onBack = { goToAccueil() })
                "stocks" -> EcranStocks(onBack = { goToAccueil() })
                "conservation" -> ConservationScreen(onBack = { goToAccueil() })
                "store" -> EcranStore(onBack = { goToAccueil() })
                "parametres" -> ParametresScreen(
                    onBack = { goBack() },
                    onRevoirTutoriel = {
                        val prefs = context.getSharedPreferences(
                            "jardin_prefs",
                            Context.MODE_PRIVATE
                        )
                        prefs.edit().putBoolean("tuto_vu_v5", false).apply()
                        goToAccueil()
                    }
                )
            }
        }

        // Barre de navigation flottante en bas (emojis + pastille sur l'actif)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            color = CouleursApp.Blanc,
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val isCurrent = screen == currentScreen
                    
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                currentScreen = screen
                                navigationStack.clear()
                                navigationStack.add(screen)
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) CouleursApp.VertPale
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emojiParEcran[screen] ?: "❓",
                                fontSize = if (isCurrent) 24.sp else 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
