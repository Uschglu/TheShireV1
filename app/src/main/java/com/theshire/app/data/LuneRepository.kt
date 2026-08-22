package com.theshire.app.data

import java.util.Calendar

data class PhaseLune(
    val nom: String,
    val emoji: String
)

class LuneRepository {
    
    fun getPhaseLune(): PhaseLune {
        // Calcul simplifié de la phase lunaire
        // Cycle lunaire = 29.53 jours
        val cal = Calendar.getInstance()
        val jourAnnee = cal.get(Calendar.DAY_OF_YEAR)
        val annee = cal.get(Calendar.YEAR)
        
        // Date de référence : Nouvelle lune le 1er janvier 2000
        val joursDepuis2000 = (annee - 2000) * 365 + jourAnnee
        val cycleLunaire = 29.53
        val phase = (joursDepuis2000 % cycleLunaire) / cycleLunaire
        
        return when {
            phase < 0.0625 -> PhaseLune("Nouvelle lune", "🌑")
            phase < 0.1875 -> PhaseLune("Premier croissant", "🌒")
            phase < 0.3125 -> PhaseLune("Premier quartier", "🌓")
            phase < 0.4375 -> PhaseLune("Lune gibbeuse croissante", "🌔")
            phase < 0.5625 -> PhaseLune("Pleine lune", "🌕")
            phase < 0.6875 -> PhaseLune("Lune gibbeuse décroissante", "🌖")
            phase < 0.8125 -> PhaseLune("Dernier quartier", "🌗")
            else -> PhaseLune("Dernier croissant", "🌘")
        }
    }
}
