@file:OptIn(ExperimentalMaterial3Api::class)

package com.theshire.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.ui.theme.CouleursApp

/**
 * Écran Store : à venir.
 * 
 * Sera utilisé pour :
 * - Proposer les outils manquants
 * - Proposer les consommables (graines, plants, terreau, paillage...)
 * - Afficher les produits recommandés par la jardinerie partenaire
 */
@Composable
fun EcranStore(onBack: () -> Unit) {
    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Store 🛒",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🛒", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Store à venir",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = CouleursApp.TexteFonce
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Vous retrouverez ici les outils manquants et les produits recommandés par votre jardinerie.",
                style = MaterialTheme.typography.bodyMedium,
                color = CouleursApp.TexteFonce.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
