package com.theshire.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theshire.app.ui.theme.PotagerShireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DEBUG_APP", "1. MainActivity créée")
        
        try {
            setContent {
                Log.d("DEBUG_APP", "2. setContent appelé")
                PotagerShireTheme {
                    Log.d("DEBUG_APP", "3. Thème appliqué")
                    TestScreen()
                }
            }
            Log.d("DEBUG_APP", "4. Interface affichée")
        } catch (e: Exception) {
            Log.e("DEBUG_APP", "ERREUR: ${e.message}", e)
        }
    }
}

@Composable
fun TestScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌱",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TEST SIMPLE",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Si vous voyez ceci, l'app fonctionne !",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
