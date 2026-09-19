package com.theshire.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.theshire.app.ui.EcranContenants
import com.theshire.app.ui.OngletConseilsUrbains
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran principal "Mon Jardin".
 * 
 * Deux grands onglets :
 * 1. Pleine terre (planches + analyse de sol)
 * 2. Urbain (contenants + conseils)
 */
@Composable
fun JardinScreen(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("pleine_terre") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedOnglet == "pleine_terre") 0 else 1,
            containerColor = CouleursApp.VertPrincipal,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedOnglet == "pleine_terre",
                onClick = { selectedOnglet = "pleine_terre" },
                text = {
                    Text(
                        "🌱 Pleine terre",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedOnglet == "pleine_terre") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
            Tab(
                selected = selectedOnglet == "urbain",
                onClick = { selectedOnglet = "urbain" },
                text = {
                    Text(
                        "🏙️ Urbain",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedOnglet == "urbain") Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            )
        }
        when (selectedOnglet) {
            "pleine_terre" -> EcranPleineTerre(onBack)
            "urbain" -> EcranUrbain()
        }
    }
}

/**
 * Sous-écran "Pleine terre" avec deux sous-onglets :
 * - Planches (grille de plantation)
 * - Analyse du sol
 */
@Composable
fun EcranPleineTerre(onBack: () -> Unit) {
    var selectedSousOnglet by remember { mutableStateOf("planches") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedSousOnglet == "planches") 0 else 1,
            containerColor = CouleursApp.VertClair,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedSousOnglet == "planches",
                onClick = { selectedSousOnglet = "planches" },
                text = {
                    Text(
                        "🌿 Planches",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedSousOnglet == "planches") Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
            Tab(
                selected = selectedSousOnglet == "analyse",
                onClick = { selectedSousOnglet = "analyse" },
                text = {
                    Text(
                        "🔬 Analyse du sol",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedSousOnglet == "analyse") Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
        }
        when (selectedSousOnglet) {
            "planches" -> JardinPlanchesScreen(onBack)
            else -> AnalyseSolScreen(onBack)
        }
    }
}

/**
 * Sous-écran "Urbain" avec deux sous-onglets :
 * - Mes contenants (pots, jardinières, tours)
 * - Conseils (agriculture urbaine)
 */
@Composable
fun EcranUrbain() {
    var selectedSousOnglet by remember { mutableStateOf("contenants") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedSousOnglet == "contenants") 0 else 1,
            containerColor = CouleursApp.VertClair,
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedSousOnglet == "contenants",
                onClick = { selectedSousOnglet = "contenants" },
                text = {
                    Text(
                        "🪴 Mes contenants",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedSousOnglet == "contenants") Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
            Tab(
                selected = selectedSousOnglet == "conseils",
                onClick = { selectedSousOnglet = "conseils" },
                text = {
                    Text(
                        "📖 Conseils",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = if (selectedSousOnglet == "conseils") Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
        }
        when (selectedSousOnglet) {
            "contenants" -> EcranContenants()
            else -> OngletConseilsUrbains()
        }
    }
}
