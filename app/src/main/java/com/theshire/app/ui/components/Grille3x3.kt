package com.theshire.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theshire.app.data.CarreEntity
import com.theshire.app.data.LegumeEntity
import com.theshire.app.data.PlancheEntity
import com.theshire.app.ui.theme.CouleursApp

/**
 * Grille 3x3 représentant un m² (carré) du potager.
 * 
 * - Si les 9 cases ont la même plante → affichage simplifié en 1 seul bloc
 * - Sinon → grille classique 3x3
 * - Chaque sous-case est cliquable
 */
@Composable
fun Grille3x3(
    carre: CarreEntity,
    couleurs: Map<Int, Color>,
    onSousCarreClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val legumes = listOfNotNull(
        carre.case1, carre.case2, carre.case3,
        carre.case4, carre.case5, carre.case6,
        carre.case7, carre.case8, carre.case9
    )

    if (legumes.size == 9 && legumes.distinct().size == 1) {
        // Cas particulier : les 9 cases ont la même plante
        val couleurGrandCarre = calculerCouleurPire(couleurs)

        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(couleurGrandCarre)
                .border(2.dp, CouleursApp.VertPrincipal)
                .clickable { onSousCarreClick(1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                legumes[0],
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(2.dp),
                color = CouleursApp.TexteFonce
            )
        }
    } else {
        // Grille classique 3x3
        Column(modifier = modifier.aspectRatio(1f).border(2.dp, CouleursApp.VertPrincipal)) {
            for (row in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..2) {
                        val caseNumero = row * 3 + col + 1
                        val legume = when (caseNumero) {
                            1 -> carre.case1; 2 -> carre.case2; 3 -> carre.case3
                            4 -> carre.case4; 5 -> carre.case5; 6 -> carre.case6
                            7 -> carre.case7; 8 -> carre.case8; 9 -> carre.case9
                            else -> null
                        }

                        val backgroundColor = couleurs[caseNumero]
                            ?: if (legume != null) CouleursApp.NeutreAssociation else CouleursApp.CaseVide

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(backgroundColor)
                                .border(1.dp, CouleursApp.VertPrincipal)
                                .clickable { onSousCarreClick(caseNumero) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                legume ?: "",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(2.dp),
                                color = CouleursApp.TexteFonce
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calcule la couleur de chaque sous-case d'un carré en fonction
 * des associations de plantes (bonnes / mauvaises / neutres).
 * 
 * Prend en compte :
 * - Les cases voisines dans le même carré
 * - Les cases voisines des carrés adjacents (gauche/droite/haut/bas)
 */
@Composable
fun calculerCouleursCarre(
    carre: CarreEntity,
    planche: PlancheEntity,
    tousLesCarres: List<CarreEntity>,
    legumes: List<LegumeEntity>
): Map<Int, Color> {
    val resultat = mutableMapOf<Int, Color>()

    fun nomBase(nom: String?): String? {
        if (nom == null) return null
        return if (nom.contains("(")) nom.substringBefore("(").trim() else nom
    }

    fun infosLegume(nom: String?): LegumeEntity? {
        val base = nomBase(nom) ?: return null
        return legumes.find { it.nom == base }
    }

    fun verifierAssociation(plante1: String?, plante2: String?): String {
        if (plante1 == null || plante2 == null) return "neutre"
        val leg1 = infosLegume(plante1) ?: return "neutre"
        val base2 = nomBase(plante2) ?: return "neutre"
        if (leg1.bonnesAssociations.contains(base2, true)) return "bonne"
        if (leg1.mauvaisesAssociations.contains(base2, true)) return "mauvaise"
        return "neutre"
    }

    fun planteDansCase(c: CarreEntity, num: Int): String? = when (num) {
        1 -> c.case1; 2 -> c.case2; 3 -> c.case3
        4 -> c.case4; 5 -> c.case5; 6 -> c.case6
        7 -> c.case7; 8 -> c.case8; 9 -> c.case9
        else -> null
    }

    fun casesVoisinesMemeCarre(num: Int): List<Int> = when (num) {
        1 -> listOf(2, 4, 5)
        2 -> listOf(1, 3, 4, 5, 6)
        3 -> listOf(2, 5, 6)
        4 -> listOf(1, 2, 5, 7, 8)
        5 -> listOf(1, 2, 3, 4, 6, 7, 8, 9)
        6 -> listOf(2, 3, 5, 8, 9)
        7 -> listOf(4, 5, 8)
        8 -> listOf(4, 5, 6, 7, 9)
        9 -> listOf(5, 6, 8)
        else -> emptyList()
    }

    for (num in 1..9) {
        val plante = planteDansCase(carre, num)
        if (plante == null) {
            resultat[num] = CouleursApp.CaseVide
            continue
        }

        val plantesVoisines = mutableListOf<String>()

        // Voisins dans le même carré
        casesVoisinesMemeCarre(num).forEach { voisinNum ->
            val planteVoisine = planteDansCase(carre, voisinNum)
            if (planteVoisine != null) plantesVoisines.add(planteVoisine)
        }

        // Voisins des carrés adjacents
        val row = (num - 1) / 3
        val col = (num - 1) % 3

        if (col == 0) {
            val carreGauche = tousLesCarres.find {
                it.positionX == carre.positionX - 1 && it.positionY == carre.positionY
            }
            if (carreGauche != null) {
                val planteVoisine = planteDansCase(carreGauche, row * 3 + 3)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (col == 2) {
            val carreDroite = tousLesCarres.find {
                it.positionX == carre.positionX + 1 && it.positionY == carre.positionY
            }
            if (carreDroite != null) {
                val planteVoisine = planteDansCase(carreDroite, row * 3 + 1)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (row == 0) {
            val carreHaut = tousLesCarres.find {
                it.positionX == carre.positionX && it.positionY == carre.positionY - 1
            }
            if (carreHaut != null) {
                val planteVoisine = planteDansCase(carreHaut, 7 + col)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }
        if (row == 2) {
            val carreBas = tousLesCarres.find {
                it.positionX == carre.positionX && it.positionY == carre.positionY + 1
            }
            if (carreBas != null) {
                val planteVoisine = planteDansCase(carreBas, 1 + col)
                if (planteVoisine != null) plantesVoisines.add(planteVoisine)
            }
        }

        // Déterminer la couleur en fonction des associations
        var aBonne = false
        var aMauvaise = false
        plantesVoisines.forEach { voisine ->
            when (verifierAssociation(plante, voisine)) {
                "bonne" -> aBonne = true
                "mauvaise" -> aMauvaise = true
            }
        }

        resultat[num] = when {
            aMauvaise -> CouleursApp.MauvaiseAssociation
            aBonne -> CouleursApp.BonneAssociation
            else -> CouleursApp.NeutreAssociation
        }
    }

    return resultat
}

/**
 * Retourne la "pire" couleur d'une grille de couleurs.
 * Priorité : mauvaise > bonne > neutre > vide
 * Utilisé pour afficher un carré entier d'une seule couleur.
 */
fun calculerCouleurPire(couleurs: Map<Int, Color>): Color {
    var aMauvaise = false
    var aBonne = false
    var aNeutre = false

    for (num in 1..9) {
        val couleur = couleurs[num] ?: continue
        when (couleur) {
            CouleursApp.MauvaiseAssociation -> aMauvaise = true
            CouleursApp.BonneAssociation -> aBonne = true
            CouleursApp.NeutreAssociation -> aNeutre = true
        }
    }

    return when {
        aMauvaise -> CouleursApp.MauvaiseAssociation
        aBonne -> CouleursApp.BonneAssociation
        aNeutre -> CouleursApp.NeutreAssociation
        else -> CouleursApp.CaseVide
    }
}
