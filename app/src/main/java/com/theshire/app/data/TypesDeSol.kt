package com.theshire.app.data

/**
 * Base de connaissances des types de sol (triangle simplifié à 6 types).
 * 
 * Chaque type est défini par :
 * - @param id Identifiant technique
 * - @param nom Nom affiché
 * - @param emoji Icône
 * - @param description Courte description
 * - @param caracteristiques Liste des caractéristiques (points clés)
 * - @param amelioration Comment améliorer ce type de sol
 * - @param maintien Comment le maintenir dans le temps
 * - @param culturesQuiReussissent Légumes qui réussissent le mieux
 * - @param culturesAEviter Légumes à éviter (ou à cultiver avec précautions)
 */
data class TypeSol(
    val id: String,
    val nom: String,
    val emoji: String,
    val description: String,
    val caracteristiques: List<String>,
    val amelioration: List<String>,
    val maintien: List<String>,
    val culturesQuiReussissent: List<String>,
    val culturesAEviter: List<String>
)

object TypesDeSol {
    
    /**
     * Détermine le type de sol à partir des pourcentages saisis.
     * 
     * Triangle simplifié :
     * - Argileux lourd : argile > 60%
     * - Argileux : argile 40-60%
     * - Limono-argileux : argile 25-40% ET limon > 40%
     * - Limoneux : limon > 50% ET argile < 25%
     * - Sablo-limoneux : sable 50-70% ET limon > 20%
     * - Sableux : sable > 70%
     */
    fun getTypeSol(argile: Int, sable: Int, limon: Int): TypeSol? {
        return when {
            argile > 60 -> TYPE_ARGILEUX_LOURD
            argile in 40..60 -> TYPE_ARGILEUX
            argile in 25..39 && limon > 40 -> TYPE_LIMONO_ARGILEUX
            limon > 50 && argile < 25 -> TYPE_LIMONEUX
            sable in 50..70 && limon > 20 -> TYPE_SABLO_LIMONEUX
            sable > 70 -> TYPE_SABLEUX
            else -> TYPE_EQUILIBRE  // Fallback
        }
    }
    
    // ============================================================
    // 6 TYPES DE SOL
    // ============================================================
    
    val TYPE_ARGILEUX_LOURD = TypeSol(
        id = "argileux_lourd",
        nom = "Sol argileux lourd",
        emoji = "🟤",
        description = "Sol très riche mais très difficile à travailler.",
        caracteristiques = listOf(
            "Très lourd et compact, collant quand il est humide",
            "Dur comme du béton quand il est sec",
            "Retient énormément l'eau (risque d'engorgement)",
            "Très riche en nutriments mais mal drainé",
            "Se réchauffe très lentement au printemps",
            "Se fissure en été (fentes de retrait)"
        ),
        amelioration = listOf(
            "Ajouter du sable grossier (10-20% du volume)",
            "Incorporer beaucoup de compost mûr (3-5 kg/m²/an)",
            "Ajouter de la matière organique : feuilles mortes, paille, fumier composté",
            "Planter des engrais verts à racines profondes (moutarde, phacélie, seigle)",
            "Créer des buttes surélevées pour améliorer le drainage",
            "Ne JAMAIS travailler le sol humide (ça aggrave la compaction)"
        ),
        maintien = listOf(
            "Paillage permanent obligatoire (paille, BRF, tontes séchées)",
            "Éviter de marcher sur les planches (utiliser des passe-pieds)",
            "Bêchage minimum : préférer la grelinette qui aère sans retourner",
            "Apport annuel de compost à l'automne",
            "Rotation des cultures avec engrais verts",
            "Chauler tous les 3-4 ans si le sol est acide"
        ),
        culturesQuiReussissent = listOf(
            "Chou, brocoli, chou-fleur (adorent les sols lourds)",
            "Céleri, poireau",
            "Haricots, pois, fèves",
            "Betterave, blette",
            "Épinard",
            "Rosier, arbustes (en général)"
        ),
        culturesAEviter = listOf(
            "Carotte (se déforme, fourchue)",
            "Navet, radis (deviennent piquants)",
            "Pomme de terre (risque de pourriture)",
            "Ail, oignon (pourrissent en hiver)",
            "Plantes de terre de bruyère (azalée, rhododendron)"
        )
    )
    
    val TYPE_ARGILEUX = TypeSol(
        id = "argileux",
        nom = "Sol argileux",
        emoji = "🟤",
        description = "Sol riche et fertile, mais dense et difficile à travailler.",
        caracteristiques = listOf(
            "Lourd et compact, mais moins que l'argileux lourd",
            "Retient bien l'eau (risque d'engorgement en hiver)",
            "Riche en nutriments",
            "Se réchauffe lentement au printemps",
            "Sensible à la compaction si travaillé humide",
            "Bon potentiel de fertilité si bien géré"
        ),
        amelioration = listOf(
            "Apporter du compost mûr (2-3 kg/m²/an)",
            "Ajouter du sable grossier (10%) ou de la perlite",
            "Incorporer de la matière organique : feuilles, paille, BRF",
            "Planter des engrais verts (moutarde, phacélie)",
            "Pailler en permanence"
        ),
        maintien = listOf(
            "Paillage régulier (3-5 cm)",
            "Éviter le bêchage profond (préférer la grelinette)",
            "Apport annuel de compost à l'automne",
            "Rotation des cultures",
            "Ne pas marcher sur le sol humide"
        ),
        culturesQuiReussissent = listOf(
            "Chou, brocoli, chou-fleur",
            "Céleri, poireau",
            "Haricots, pois",
            "Betterave, blette",
            "Épinard",
            "Tomate (avec bon drainage)"
        ),
        culturesAEviter = listOf(
            "Carotte (se déforme)",
            "Navet, radis (piquants)",
            "Ail, oignon (pourrissent)",
            "Plantes de terre de bruyère"
        )
    )
    
    val TYPE_LIMONO_ARGILEUX = TypeSol(
        id = "limono_argileux",
        nom = "Sol limono-argileux",
        emoji = "🟠",
        description = "Sol équilibré, fertile et facile à travailler.",
        caracteristiques = listOf(
            "Texture équilibrée entre limon et argile",
            "Fertile et bonne rétention d'eau",
            "Facile à travailler (le meilleur compromis)",
            "Se réchauffe moyennement au printemps",
            "Sensible à la compaction si travaillé humide",
            "Idéal pour la plupart des cultures"
        ),
        amelioration = listOf(
            "Compost régulier (2-3 kg/m²/an)",
            "Paillage pour protéger la structure",
            "Éviter le travail en sol humide",
            "Engrais verts (moutarde, phacélie, trèfle)"
        ),
        maintien = listOf(
            "Paillage permanent",
            "Rotation des cultures",
            "Apport annuel de compost",
            "Éviter le piétinement",
            "Travailler le sol à bon moment (ni trop sec, ni trop humide)"
        ),
        culturesQuiReussissent = listOf(
            "Presque tous les légumes !",
            "Tomate, courgette, poivron, aubergine",
            "Haricots, pois, fèves",
            "Salades, épinards, blettes",
            "Choux (tous), brocoli",
            "Carottes, betteraves, radis",
            "Aromatiques (basilic, persil, coriandre)"
        ),
        culturesAEviter = listOf(
            "Plantes de terre de bruyère",
            "Plantes de sol très sec (lavande, thym en excès d'eau)"
        )
    )
    
    val TYPE_LIMONEUX = TypeSol(
        id = "limoneux",
        nom = "Sol limoneux",
        emoji = "🟡",
        description = "Sol doux, fertile, mais qui se tasse facilement.",
        caracteristiques = listOf(
            "Texture douce et soyeuse au toucher",
            "Fertile, riche en éléments nutritifs",
            "Retient bien l'eau",
            "Se tasse facilement (compaction)",
            "Forme une croûte en surface quand il sèche",
            "Bon pour les cultures exigeantes"
        ),
        amelioration = listOf(
            "Ajouter de la matière organique (compost, fumier)",
            "Paillage régulier pour éviter la croûte",
            "Éviter le travail en sol humide",
            "Engrais verts à racines profondes",
            "Ajouter du sable si trop compact"
        ),
        maintien = listOf(
            "Paillage permanent",
            "Éviter le piétinement",
            "Ne pas travailler le sol humide",
            "Apport annuel de compost",
            "Rotation des cultures"
        ),
        culturesQuiReussissent = listOf(
            "Salades, épinards, blettes",
            "Carottes, betteraves, radis",
            "Oignons, poireaux, ail",
            "Haricots, pois",
            "Choux, brocoli",
            "Tomate, courgette, poivron",
            "Fraisiers"
        ),
        culturesAEviter = listOf(
            "Plantes de sol très sec (lavande, romarin en excès d'eau)",
            "Plantes de terre de bruyère"
        )
    )
    
    val TYPE_SABLO_LIMONEUX = TypeSol(
        id = "sablo_limoneux",
        nom = "Sol sablo-limoneux",
        emoji = "🟨",
        description = "Sol léger, facile à travailler, mais qui draine beaucoup.",
        caracteristiques = listOf(
            "Texture légère et friable",
            "Facile à travailler toute l'année",
            "Draine très bien (sèche vite en été)",
            "Se réchauffe rapidement au printemps",
            "Pauvre en nutriments (lessivage)",
            "Nécessite des arrosages fréquents"
        ),
        amelioration = listOf(
            "Apporter beaucoup de compost mûr (3-5 kg/m²/an)",
            "Incorporer de la matière organique (fumier, feuilles)",
            "Pailler épais pour retenir l'eau",
            "Utiliser des engrais verts (trèfle, vesce)",
            "Arroser régulièrement en été"
        ),
        maintien = listOf(
            "Paillage épais permanent",
            "Apport annuel de compost",
            "Arrosage régulier (goutte-à-goutte recommandé)",
            "Éviter les engrais minéraux (lessivage)",
            "Cultures en lasagne possibles"
        ),
        culturesQuiReussissent = listOf(
            "Carottes, radis, navets (adorent le sable)",
            "Pommes de terre",
            "Asperges",
            "Aromatiques méditerranéennes (thym, romarin, lavande)",
            "Oignons, ail, échalotes",
            "Salades, épinards (avec arrosage)",
            "Fraisiers"
        ),
        culturesAEviter = listOf(
            "Choux (besoin de nutriments)",
            "Céleri (besoin d'humidité constante)",
            "Plantes gourmandes sans apport massif de compost"
        )
    )
    
    val TYPE_SABLEUX = TypeSol(
        id = "sableux",
        nom = "Sol sableux",
        emoji = "🟨",
        description = "Sol très léger, qui draine énormément et se dessèche vite.",
        caracteristiques = listOf(
            "Très léger et friable",
            "Très facile à travailler",
            "Draine excessivement (sécheresse en été)",
            "Se réchauffe très vite au printemps",
            "Très pauvre en nutriments",
            "Nécessite des arrosages très fréquents"
        ),
        amelioration = listOf(
            "Apporter BEAUCOUP de compost mûr (5+ kg/m²/an)",
            "Incorporer de l'argile bentonite (5-10 kg/m²)",
            "Pailler très épais (10 cm)",
            "Utiliser des engrais verts à racines profondes",
            "Créer une couche d'humus en surface (lasagne)",
            "Arroser abondamment et régulièrement"
        ),
        maintien = listOf(
            "Paillage très épais permanent",
            "Apport de compost à chaque saison",
            "Goutte-à-goutte quasi obligatoire",
            "Éviter les engrais minéraux (lessivés)",
            "Cultures en lasagne recommandées",
            "Ne jamais laisser le sol nu"
        ),
        culturesQuiReussissent = listOf(
            "Carottes, radis, navets (sols légers idéaux)",
            "Pommes de terre (faciles à récolter)",
            "Asperges",
            "Aromatiques méditerranéennes (thym, romarin, lavande, sauge)",
            "Oignons, ail, échalotes",
            "Salades (avec arrosage quotidien)",
            "Melon, pastèque (aiment la chaleur)"
        ),
        culturesAEviter = listOf(
            "Choux, brocoli, céleri (trop exigeants)",
            "Épinards (montent en graines trop vite)",
            "Plantes gourmandes en eau",
            "Cultures nécessitant une humidité constante"
        )
    )
    
    val TYPE_EQUILIBRE = TypeSol(
        id = "equilibre",
        nom = "Sol équilibré (terre franche)",
        emoji = "🟢",
        description = "Sol idéal : parfait équilibre entre argile, limon et sable.",
        caracteristiques = listOf(
            "Texture idéale, équilibrée",
            "Fertile et bien drainé",
            "Retient l'eau sans excès",
            "Se réchauffe facilement au printemps",
            "Facile à travailler",
            "Le sol rêvé du jardinier !"
        ),
        amelioration = listOf(
            "Apport annuel de compost (2-3 kg/m²)",
            "Paillage pour maintenir la structure",
            "Éviter la compaction (ne pas marcher dessus)",
            "Rotation des cultures pour éviter l'épuisement"
        ),
        maintien = listOf(
            "Paillage régulier",
            "Apport annuel de compost",
            "Rotation des cultures",
            "Éviter les excès d'engrais",
            "Maintenir la vie du sol (vers de terre, micro-organismes)"
        ),
        culturesQuiReussissent = listOf(
            "TOUS les légumes !",
            "C'est le sol idéal pour le potager",
            "Tomate, courgette, poivron, aubergine",
            "Tous les choux, brocoli",
            "Carottes, betteraves, radis, navets",
            "Salades, épinards, blettes",
            "Haricots, pois, fèves",
            "Toutes les aromatiques"
        ),
        culturesAEviter = listOf(
            "Aucune ! Toutes les plantes s'y plaisent",
            "Ne pas négliger l'entretien pour le garder ainsi"
        )
    )
}
