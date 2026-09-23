package com.theshire.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.TypeSol
import com.theshire.app.data.TypesDeSol
import com.theshire.app.ui.navigation.LayoutConstantes
import com.theshire.app.ui.theme.CouleursApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyseSolScreen(onBack: () -> Unit) {
    var argile by remember { mutableStateOf("") }
    var sable by remember { mutableStateOf("") }
    var limon by remember { mutableStateOf("") }
    var typeSol by remember { mutableStateOf<TypeSol?>(null) }
    var erreurSaisie by remember { mutableStateOf("") }

    fun calculer() {
        val a = argile.toIntOrNull() ?: 0
        val s = sable.toIntOrNull() ?: 0
        val l = limon.toIntOrNull() ?: 0
        val total = a + s + l

        if (total != 100) {
            erreurSaisie = "Le total doit faire 100% (actuellement : $total%)"
            typeSol = null
        } else {
            erreurSaisie = ""
            typeSol = TypesDeSol.getTypeSol(a, s, l)
        }
    }

    Scaffold(
        containerColor = CouleursApp.Creme,
        topBar = {
            TopAppBar(
                title = {
                    Text("Analyse du sol 🔬", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CouleursApp.VertPrincipal,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Introduction
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🔬 Test du bocal (méthode simple)",
                            fontWeight = FontWeight.Bold,
                            color = CouleursApp.VertPrincipal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Remplissez un bocal avec 1/3 de terre et 2/3 d'eau. Secouez, laissez reposer 24h. Les couches se séparent : sable en bas, limon au milieu, argile en haut. Mesurez chaque couche et calculez le pourcentage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CouleursApp.TexteFonce
                        )
                    }
                }
            }

            // Saisie
            item {
                OutlinedTextField(
                    value = argile,
                    onValueChange = { argile = it.filter { c -> c.isDigit() } },
                    label = { Text("Argile (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = sable,
                    onValueChange = { sable = it.filter { c -> c.isDigit() } },
                    label = { Text("Sable (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = limon,
                    onValueChange = { limon = it.filter { c -> c.isDigit() } },
                    label = { Text("Limon (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = { calculer() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CouleursApp.VertPrincipal)
                ) {
                    Text("Analyser")
                }
            }

            // Erreur de saisie
            if (erreurSaisie.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            erreurSaisie,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Résultat enrichi
            if (typeSol != null) {
                val type = typeSol!!

                // En-tête
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.VertPrincipal),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(type.emoji, style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                type.nom,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                type.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Caractéristiques
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🌾 Caractéristiques",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            type.caracteristiques.forEach { c ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        "•",
                                        color = CouleursApp.VertPrincipal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        c,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Comment l'améliorer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🔧 Comment l'améliorer",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.Terracotta,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            type.amelioration.forEach { a ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        "→",
                                        color = CouleursApp.Terracotta,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        a,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Comment le maintenir
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CouleursApp.Blanc),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "✅ Comment le maintenir",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            type.maintien.forEach { m ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        "✓",
                                        color = CouleursApp.VertPrincipal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        m,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Cultures qui réussissent
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CouleursApp.BonneAssociation.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🌱 Cultures qui réussissent le mieux",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.VertPrincipal,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            type.culturesQuiReussissent.forEach { c ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("🌿", modifier = Modifier.width(20.dp))
                                    Text(
                                        c,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Cultures à éviter
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CouleursApp.MauvaiseAssociation.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Cultures à éviter (ou avec précautions)",
                                fontWeight = FontWeight.Bold,
                                color = CouleursApp.Terracotta,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            type.culturesAEviter.forEach { c ->
                                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text("⚠️", modifier = Modifier.width(20.dp))
                                    Text(
                                        c,
                                        color = CouleursApp.TexteFonce,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ⚠️ NOUVEAU : Spacer pour ne pas être collé / caché
            // par la barre de navigation flottante
            item {
                Spacer(modifier = Modifier.height(LayoutConstantes.PADDING_BAS_FAB))
            }
        }
    }
}
