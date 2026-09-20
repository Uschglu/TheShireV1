@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.theshire.app.ui.screens.stocks.OngletGraines
import com.theshire.app.ui.screens.stocks.OngletMateriel
import com.theshire.app.ui.screens.stocks.OngletPlants
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran Stocks : hub de gestion du matériel et des stocks de jardinage.
 * 
 * Trois onglets :
 * - 🫘 Graines : sachets de graines en stock
 * - 🌱 Jeunes plants : semis et plants en attente
 * - 🛠️ Matériel : outils possédés
 * 
 * Le Store est désormais dans un écran séparé (EcranStore.kt).
 */
@Composable
fun EcranStocks(onBack: () -> Unit) {
    var selectedOnglet by remember { mutableStateOf("graines") }
    
    Scaffold(
        containerColor = CouleursApp.Creme
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Barre d'onglets
            TabRow(
                selectedTabIndex = when (selectedOnglet) {
                    "graines" -> 0
                    "plants" -> 1
                    else -> 2
                },
                containerColor = CouleursApp.VertPrincipal,
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedOnglet == "graines",
                    onClick = { selectedOnglet = "graines" },
                    text = {
                        Text(
                            "🫘 Graines",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOnglet == "graines") Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
                Tab(
                    selected = selectedOnglet == "plants",
                    onClick = { selectedOnglet = "plants" },
                    text = {
                        Text(
                            "🌱 Plants",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOnglet == "plants") Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
                Tab(
                    selected = selectedOnglet == "materiel",
                    onClick = { selectedOnglet = "materiel" },
                    text = {
                        Text(
                            "🛠️ Matériel",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOnglet == "materiel") Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                )
            }
            
            // Contenu de l'onglet actif
            when (selectedOnglet) {
                "graines" -> OngletGraines()
                "plants" -> OngletPlants()
                else -> OngletMateriel()
            }
        }
    }
}
