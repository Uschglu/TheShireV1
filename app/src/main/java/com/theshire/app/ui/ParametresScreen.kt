package com.theshire.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.BrandingApp
import com.theshire.app.data.ThemePreferences
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran Paramètres : regroupe tous les réglages de l'application.
 * 
 * Sections actuelles :
 * - Apparence : mode sombre
 * - Aide : revoir le tutoriel
 * - À propos : nom de l'app, version, mentions légales (à venir)
 * 
 * Sections futures prévues :
 * - Langue
 * - Notifications
 * - Branding (logo, couleurs, PIN) → écran séparé
 * - Compte utilisateur
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametresScreen(
    onBack: () -> Unit,
    onRevoirTutoriel: () -> Unit
) {
    val context = LocalContext.current
    
    // État local pour le mode sombre (synchronisé avec CouleursApp)
    var modeSombre by remember { mutableStateOf(CouleursApp.isDarkMode) }
    
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Paramètres ⚙️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CouleursApp.VertPrincipal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // ========== SECTION APPARENCE ==========
            SectionTitre("🎨 Apparence")
            
            CarteParametre(
                titre = "Mode sombre",
                description = "Réduit la luminosité pour un confort visuel en soirée",
                action = {
                    Switch(
                        checked = modeSombre,
                        onCheckedChange = { nouveauMode ->
                            modeSombre = nouveauMode
                            ThemePreferences.sauvegarderModeSombre(context, nouveauMode)
                            CouleursApp.changerModeSombre(nouveauMode)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CouleursApp.VertPrincipal
                        )
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ========== SECTION AIDE ==========
            SectionTitre("❓ Aide")
            
            CarteParametre(
                titre = "Revoir le tutoriel",
                description = "Découvrir ou redécouvrir les fonctionnalités de l'app",
                action = {
                    Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal)
                },
                onClick = onRevoirTutoriel
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ========== SECTION À PROPOS ==========
            SectionTitre("ℹ️ À propos")
            
            CarteParametre(
                titre = "Nom de l'application",
                description = BrandingApp.config.nomApp,
                action = {
                    Text("", style = MaterialTheme.typography.bodyMedium)
                }
            )
            
            CarteParametre(
                titre = "Version",
                description = "1.0.0",
                action = {
                    Text("", style = MaterialTheme.typography.bodyMedium)
                }
            )
            
            CarteParametre(
                titre = "Mentions légales",
                description = "Bientôt disponible",
                action = {
                    Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal.copy(alpha = 0.4f))
                },
                onClick = null // Désactivé pour l'instant
            )
            
            CarteParametre(
                titre = "Politique de confidentialité",
                description = "Bientôt disponible",
                action = {
                    Text("›", style = MaterialTheme.typography.titleLarge, color = CouleursApp.VertPrincipal.copy(alpha = 0.4f))
                },
                onClick = null // Désactivé pour l'instant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== PIED DE PAGE ==========
            Text(
                text = "🌱 ${BrandingApp.config.nomApp} - Fait avec amour pour les jardiniers",
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.TexteFonce.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Titre de section : texte en majuscules, avec une couleur d'accent.
 */
@Composable
private fun SectionTitre(texte: String) {
    Text(
        text = texte,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = CouleursApp.VertPrincipal,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

/**
 * Carte cliquable (ou non) présentant un paramètre.
 * 
 * @param titre Titre principal (ex : "Mode sombre")
 * @param description Sous-titre explicatif
 * @param action Contenu de droite (bouton, switch, flèche)
 * @param onClick Action au clic sur la carte entière (null = non cliquable)
 */
@Composable
private fun CarteParametre(
    titre: String,
    description: String,
    action: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(CouleursApp.Blanc)
        .let { base ->
            if (onClick != null) {
                base.clickable(onClick = onClick)
            } else {
                base
            }
        }
        .padding(16.dp)
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titre,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = CouleursApp.TexteFonce
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        action()
    }
}
