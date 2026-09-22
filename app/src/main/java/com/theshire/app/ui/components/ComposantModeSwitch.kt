package com.theshire.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theshire.app.data.ModePreferences
import com.theshire.app.ui.theme.CouleursApp

/**
 * Interrupteur de mode de suivi du jardin :
 *  - Mode PROJECTION (OFF, par défaut) : suivi sans impact sur les stocks
 *  - Mode RÉEL (ON) : les semis et plantations décrémentent les stocks
 * 
 * Lit et écrit via ModePreferences (SharedPreferences).
 */
@Composable
fun ComposantModeSwitch(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var modeReel by remember { mutableStateOf(ModePreferences.estModeReel(context)) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (modeReel) CouleursApp.VertPale else CouleursApp.Blanc
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (modeReel) "🌳 Mode réel" else "🌱 Mode projection",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (modeReel) CouleursApp.VertPrincipal else CouleursApp.TexteFonce
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (modeReel) {
                        "Les semis déduisent les graines du stock."
                    } else {
                        "Suivi sans impact sur tes stocks."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CouleursApp.TexteFonce.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Switch(
                checked = modeReel,
                onCheckedChange = { nouveau ->
                    modeReel = nouveau
                    ModePreferences.setModeReel(context, nouveau)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CouleursApp.Blanc,
                    checkedTrackColor = CouleursApp.VertPrincipal,
                    uncheckedThumbColor = CouleursApp.Blanc,
                    uncheckedTrackColor = CouleursApp.TexteFonce.copy(alpha = 0.3f)
                )
            )
        }
    }
}
