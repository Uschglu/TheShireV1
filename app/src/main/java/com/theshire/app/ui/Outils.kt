package com.theshire.app.ui

/**
 * Base de connaissances des outils de jardinage.
 * 
 * Chaque outil est défini par :
 * - @param id Identifiant technique unique (sans accents, minuscules, underscores)
 * - @param nom Nom affiché à l'utilisateur
 * - @param emoji Emoji représentant l'outil
 * - @param description Courte description (1-2 phrases)
 * - @param tuto Étapes d'utilisation (chaque étape sur une ligne)
 * - @param conseil Conseil supplémentaire (optionnel)
 * 
 * La liste est triée par ordre alphabétique du nom d'affichage.
 * Utilisée par :
 * - EcranOutils (to-do list, tutos)
 * - OperationsCulturales (champ outilsRequis)
 * - Calendrier (affichage des outils nécessaires)
 */
data class Outil(
    val id: String,
    val nom: String,
    val emoji: String,
    val description: String,
    val tuto: List<String> = emptyList(),
    val conseil: String = ""
)

object Outils {
    
    /**
     * Retourne la liste complète des outils disponibles.
     */
    fun getTousLesOutils(): List<Outil> {
        return listOf(
            Outil(
                id = "ailes_vent",
                nom = "Ailes de ventilation",
                emoji = "💨",
                description = "Aèrent le sol sans le retourner, en le soulevant légèrement.",
                tuto = listOf(
                    "Enfoncez les dents dans le sol à la verticale",
                    "Faites levier d'avant en arrière pour soulever la terre",
                    "Reculez de 20 cm et répétez",
                    "Ne retournez PAS la terre : laissez-la retomber en place"
                ),
                conseil = "Idéal pour l'automne : laissez les vers de terre et la vie du sol travailler tout l'hiver."
            ),
            Outil(
                id = "arrosoir",
                nom = "Arrosoir",
                emoji = "🚿",
                description = "Pour arroser délicatement les plantes, en particulier les jeunes pousses.",
                tuto = listOf(
                    "Remplissez l'arrosoir d'eau (idéalement à température ambiante)",
                    "Arrosez au pied des plantes, jamais sur les feuilles",
                    "Pour les semis, utilisez la pomme fine (petits trous)",
                    "Arrosez le matin de préférence"
                ),
                conseil = "Un arrosoir de 5L suffit pour un petit potager. Préférez arroser moins souvent mais abondamment."
            ),
            Outil(
                id = "attaches_raphia",
                nom = "Attaches / Raphia",
                emoji = "🎀",
                description = "Permet d'attacher les plantes à leurs tuteurs sans les abîmer.",
                tuto = listOf(
                    "Faites un tour autour du tuteur",
                    "Faites un tour autour de la tige, en formant un 8",
                    "Attachez sans serrer : la tige doit pouvoir bouger légèrement",
                    "Vérifiez régulièrement que rien ne serre trop"
                ),
                conseil = "Le raphia naturel est biodégradable et se décompose au fil du temps. Pas besoin de l'enlever."
            ),
            Outil(
                id = "beche",
                nom = "Bêche",
                emoji = "🪓",
                description = "Pour retourner la terre en profondeur (25-30 cm) et ameublir le sol.",
                tuto = listOf(
                    "Enfoncez la bêche à la verticale avec le pied",
                    "Appuyez sur le manche pour faire levier",
                    "Retournez la motte de terre",
                    "Cassez les mottes avec le dos de la bêche",
                    "Avancez régulièrement en reculant (pour ne pas tasser la terre fraîche)"
                ),
                conseil = "Bêchez quand la terre est ni trop sèche ni trop humide. Si elle colle à la bêche, attendez."
            ),
            Outil(
                id = "binette",
                nom = "Binette",
                emoji = "⛏️",
                description = "Pour sarcler (couper les mauvaises herbes) et ameublir la surface du sol.",
                tuto = listOf(
                    "Tenez la binette à 2 mains, dos droit",
                    "Raclez la surface sur 2-3 cm de profondeur",
                    "Sectionnez les mauvaises herbes juste sous la racine",
                    "Laissez-les sur place : elles serviront d'engrais"
                ),
                conseil = "Binez par temps sec : les adventices sèchent et ne repoussent pas."
            ),
            Outil(
                id = "brouette",
                nom = "Brouette",
                emoji = "🛒",
                description = "Pour transporter les charges lourdes : terre, compost, récoltes.",
                tuto = listOf(
                    "Chargez toujours du côté du manche (le plus proche de vous)",
                    "Gardez le centre de gravité bas",
                    "Roulez lentement, surtout dans les pentes",
                    "Videz en basculant d'un mouvement franc"
                ),
                conseil = "Ne surchargez pas : une brouette qui bascule = perte de temps et douleur au dos."
            ),
            Outil(
                id = "cordeau",
                nom = "Cordeau",
                emoji = "📏",
                description = "Pour tracer des lignes droites et aligner les plantations.",
                tuto = listOf(
                    "Plantez 2 piquets aux extrémités de votre rangée",
                    "Tendez une ficelle entre les 2",
                    "Tracez un sillon le long du cordeau avec la binette",
                    "Semez ou plantez en suivant la ligne"
                ),
                conseil = "Idéal pour les rangs de carottes, radis, salades. Le résultat est beaucoup plus propre."
            ),
            Outil(
                id = "echelle",
                nom = "Échelle",
                emoji = "🪜",
                description = "Pour accéder aux arbres fruitiers ou aux plantes hautes.",
                tuto = listOf(
                    "Posez l'échelle sur un sol stable et plat",
                    "Écartez la base à 1/4 de la hauteur",
                    "Assurez-vous que les pieds ne glissent pas",
                    "Montez sans jamais dépasser le 3ème barreau en partant du haut"
                ),
                conseil = "Ne montez jamais seul. Faites-vous tenir l'échelle par quelqu'un."
            ),
            Outil(
                id = "elagueur",
                nom = "Élagueur",
                emoji = "🗡️",
                description = "Pour couper les branches épaisses (jusqu'à 3-4 cm de diamètre).",
                tuto = listOf(
                    "Coupez d'abord sous la branche à 20 cm du tronc",
                    "Coupez ensuite au-dessus, à 5 cm du tronc",
                    "Terminez par une coupe nette au col de la branche (sans entailler le tronc)",
                    "Ne coupez pas à ras du tronc : laissez le bourrelet cicatriciel"
                ),
                conseil = "Désinfectez la lame à l'alcool entre chaque arbre pour éviter les maladies."
            ),
            Outil(
                id = "fourche_beche",
                nom = "Fourche-bêche",
                emoji = "🔱",
                description = "Pour aérer la terre et arracher les racines sans la retourner.",
                tuto = listOf(
                    "Enfoncez les 4 dents à la verticale avec le pied",
                    "Faites levier d'avant en arrière pour soulever",
                    "Secouez légèrement pour faire tomber la terre",
                    "Retirez les racines et cailloux coincés"
                ),
                conseil = "Parfaite pour les sols lourds (argileux) : elle casse moins les mottes que la bêche."
            ),
            Outil(
                id = "gants",
                nom = "Gants de jardin",
                emoji = "🧤",
                description = "Pour protéger vos mains des épines, ampoules et produits.",
                tuto = listOf(
                    "Choisissez la bonne taille : ni trop serrés, ni trop larges",
                    "Vérifiez la souplesse des doigts (préhension)",
                    "Enlevez-les en tirant par les doigts (pas par le poignet)",
                    "Lavez-les régulièrement à l'eau froide"
                ),
                conseil = "Ayez 2 paires : une fine pour les travaux précis, une épaisse pour les travaux lourds."
            ),
            Outil(
                id = "goutte_a_goutte",
                nom = "Goutte-à-goutte",
                emoji = "💧",
                description = "Système d'arrosage automatique économe en eau.",
                tuto = listOf(
                    "Déroulez le tuyau le long de vos rangs",
                    "Posez-le au pied des plantes (pas dessus)",
                    "Percez des micro-trous tous les 20-30 cm",
                    "Branchez sur un programmateur ou un robinet",
                    "Vérifiez le débit et ajustez"
                ),
                conseil = "Économie d'eau de 50-70% par rapport à l'arrosage classique. Rentabilisé en 1 saison."
            ),
            Outil(
                id = "griffe",
                nom = "Griffe",
                emoji = "🪮",
                description = "Pour ameublir la surface du sol sur 5-10 cm et enlever les petits adventices.",
                tuto = listOf(
                    "Tenez la griffe à 2 mains",
                    "Grattez la surface en tirant vers vous",
                    "Croisez les passages (en long puis en large)",
                    "Émiettez les mottes avant de semer"
                ),
                conseil = "Utilisez-la après la bêche pour affiner la terre avant les semis."
            ),
            Outil(
                id = "hygrometre",
                nom = "Hygromètre",
                emoji = "🌡️",
                description = "Mesure le taux d'humidité du sol ou de l'air.",
                tuto = listOf(
                    "Plantez la sonde dans le sol à 10-15 cm",
                    "Attendez 1-2 minutes que la mesure se stabilise",
                    "Lisez la valeur sur l'écran",
                    "Comparez à la valeur recommandée pour la plante",
                    "Nettoyez la sonde après chaque utilisation"
                ),
                conseil = "Utile surtout pour l'intérieur (plantes d'appartement) et les serres."
            ),
            Outil(
                id = "paillage",
                nom = "Paillage",
                emoji = "🌾",
                description = "Protège le sol, garde l'humidité, limite les mauvaises herbes.",
                tuto = listOf(
                    "Désherbez soigneusement avant",
                    "Arrosez si nécessaire",
                    "Étalez une couche de 3-5 cm sur le sol",
                    "Laissez un espace de 5 cm autour des tiges (pour l'aération)",
                    "Renouvelez quand la couche devient trop fine"
                ),
                conseil = "Paille de blé, tontes séchées, copeaux de lin, feuilles mortes : tout fonctionne."
            ),
            Outil(
                id = "pinceau",
                nom = "Pinceau",
                emoji = "🖌️",
                description = "Pour polliniser manuellement les fleurs (notamment les tomates sous serre).",
                tuto = listOf(
                    "Attendez que les fleurs soient bien ouvertes",
                    "Passez le pinceau doucement sur les étamines (partie jaune)",
                    "Faites le tour de la fleur",
                    "Recommencez sur les autres fleurs en mélangeant"
                ),
                conseil = "À faire le matin, entre 10h et 14h, quand le pollen est sec."
            ),
            Outil(
                id = "plantoir",
                nom = "Plantoir",
                emoji = "🪝",
                description = "Pour planter les bulbes, jeunes plants et semis en godets.",
                tuto = listOf(
                    "Enfoncez le plantoir à la profondeur voulue",
                    "Écartez légèrement pour agrandir le trou",
                    "Placez le plant ou le bulbe",
                    "Rebouchez et tassez légèrement autour"
                ),
                conseil = "Pour les bulbes : enterrez-les à 2-3 fois leur hauteur."
            ),
            Outil(
                id = "pulverisateur",
                nom = "Pulvérisateur",
                emoji = "🧴",
                description = "Pour appliquer les traitements naturels (purin, savon noir...).",
                tuto = listOf(
                    "Remplissez avec la préparation préparée la veille",
                    "Filtrez pour éviter de boucher la buse",
                    "Pulvérisez à 30-40 cm de la plante",
                    "Traitez le soir, par temps calme (pas de vent)",
                    "Nettoyez soigneusement après usage"
                ),
                conseil = "Ne mélangez jamais plusieurs produits. Un pulvérisateur = un usage."
            ),
            Outil(
                id = "rames_filets",
                nom = "Rames / Filets",
                emoji = "🪜",
                description = "Supports verticaux pour haricots à rames, pois, concombres.",
                tuto = listOf(
                    "Plantez les rames en biais, croisées en tipi",
                    "Ou fixez un filet vertical entre 2 piquets",
                    "Plantez 2-3 graines au pied de chaque rame",
                    "Guidez les jeunes tiges vers le support si besoin"
                ),
                conseil = "Les rames en bambou durent 5-10 ans. Un bon investissement."
            ),
            Outil(
                id = "rateau",
                nom = "Râteau",
                emoji = "🧹",
                description = "Pour niveler le sol, rassembler les feuilles et affiner la terre.",
                tuto = listOf(
                    "Tenez le manche à 2 mains, légèrement incliné",
                    "Tirez vers vous en gardant les dents au sol",
                    "Pour niveler : passez en croisé (long puis large)",
                    "Pour ramasser : rassemblez en petits tas"
                ),
                conseil = "Utilisez un râteau à dents fines pour les semis, à dents larges pour ramasser."
            ),
            Outil(
                id = "secateur",
                nom = "Sécateur",
                emoji = "✂️",
                description = "Pour couper les tiges, tailler, effeuiller, supprimer les gourmands.",
                tuto = listOf(
                    "Placez la lame tranchante côté plante (pour ne pas écraser la tige)",
                    "Coupez d'un geste ferme et net",
                    "Pour les grosses tiges, faites 2 coupes (dessous puis dessus)",
                    "Nettoyez la lame régulièrement",
                    "Affûtez une fois par saison"
                ),
                conseil = "Désinfectez le sécateur à l'alcool entre 2 plantes si vous taillez des plantes malades."
            ),
            Outil(
                id = "serfouette",
                nom = "Serfouette",
                emoji = "🪏",
                description = "Outil double : une panne pour sarcler, une langue pour butter.",
                tuto = listOf(
                    "Utilisez la panne (partie plate) pour sarcler",
                    "Utilisez la langue (partie pointue) pour butter",
                    "Pour butter : ramenez la terre contre la base des tiges",
                    "Travaillez à reculons pour ne pas tasser la terre"
                ),
                conseil = "Indispensable pour butter les pommes de terre et les poireaux."
            ),
            Outil(
                id = "transplantoir",
                nom = "Transplantoir",
                emoji = "🥄",
                description = "Petite pelle pour transplanter les jeunes plants et semis.",
                tuto = listOf(
                    "Enfoncez le transplantoir autour du plant",
                    "Soulevez délicatement avec la motte de terre",
                    "Faites un trou au nouvel emplacement",
                    "Placez le plant et tassez légèrement"
                ),
                conseil = "Arrosez juste après le repiquage pour faciliter la reprise."
            ),
            Outil(
                id = "tuteurs_bambou",
                nom = "Tuteurs (bambou)",
                emoji = "🎋",
                description = "Pour soutenir les plantes hautes (tomates, aubergines, poivrons).",
                tuto = listOf(
                    "Plantez le tuteur AVANT la plante (pour ne pas abîmer les racines)",
                    "Enfoncez-le de 20 cm minimum",
                    "Choisissez la bonne hauteur (1,5-2 m pour les tomates)",
                    "Attachez la tige au fur et à mesure de la croissance"
                ),
                conseil = "Plantez les tuteurs en biais pour plus de solidité face au vent."
            ),
            Outil(
                id = "tuyau_arrosage",
                nom = "Tuyau d'arrosage",
                emoji = "🌊",
                description = "Pour arroser les grandes surfaces et remplir l'arrosoir.",
                tuto = listOf(
                    "Déroulez le tuyau sans le plier",
                    "Vissez l'embout sur le robinet",
                    "Ouvrez l'eau progressivement",
                    "Arrosez au pied, jamais sur les feuilles",
                    "Videz le tuyau après usage (pour éviter le gel en hiver)"
                ),
                conseil = "Installez un dévidoir pour éviter les nœuds et prolonger la durée de vie."
            ),
            Outil(
                id = "voile_anti_insectes",
                nom = "Voile anti-insectes",
                emoji = "🕸️",
                description = "Protège les cultures des insectes ravageurs sans pesticide.",
                tuto = listOf(
                    "Posez le voile sur des arceaux (pas directement sur les plantes)",
                    "Enterrez les bords sur 10 cm tout autour",
                    "Vérifiez qu'il n'y a pas d'ouverture",
                    "Soulevez-le pour arroser et désherber"
                ),
                conseil = "À poser dès la plantation. Très efficace contre la mouche de la carotte et la piéride du chou."
            )
        )
    }
    
    /**
     * Récupère un outil par son identifiant.
     * Retourne null si l'outil n'existe pas.
     */
    fun getOutilParId(id: String): Outil? {
        return getTousLesOutils().find { it.id == id }
    }
    
    /**
     * Récupère une liste d'outils par leurs identifiants.
     * Les identifiants inconnus sont ignorés.
     */
    fun getOutilsParIds(ids: List<String>): List<Outil> {
        val tous = getTousLesOutils()
        return ids.mapNotNull { id -> tous.find { it.id == id } }
    }
}
