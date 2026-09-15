package com.theshire.app.ui

/**
 * Base de connaissances agricoles : opérations culturales par légume.
 * 
 * Chaque opération est définie par :
 * - @param nom : nom court de l'opération (affiché dans le calendrier)
 * - @param emoji : icône visuelle
 * - @param description : description complète de ce qu'il faut faire
 * - @param conseil : conseil supplémentaire (optionnel)
 * - @param jourDebut : nombre de jours après plantation pour le début de la fenêtre
 * - @param jourFin : nombre de jours après plantation pour la fin de la fenêtre
 *   (si jourDebut == jourFin → opération ponctuelle, sinon opération sur plusieurs jours)
 * - @param couleurHex : couleur de la barre dans le calendrier
 * - @param dureeJours : durée totale de la fenêtre (dateFin - dateDebut)
 * 
 * Si dureeJours == 0 → fenêtre de ±2 jours autour de jourDebut
 * Sinon → période de jourDebut à jourFin
 * 
 * Les délais sont basés sur les pratiques agricoles courantes en climat tempéré.
 */
data class OperationCulturale(
    val nom: String,
    val emoji: String,
    val description: String,
    val conseil: String = "",
    val jourDebut: Int,
    val jourFin: Int = jourDebut,
    val couleurHex: String = "#FFA726"
)

object OperationsCulturales {
    
    // ========== PALETTE DE COULEURS ==========
    // Pour faciliter la lecture, chaque type d'opération a une couleur associée
    private const val COULEUR_PLANTATION = "#66BB6A"      // Vert : plantation, semis, repiquage
    private const val COULEUR_ENTRETIEN = "#FFA726"       // Orange : tuteurage, buttage, éclaircissage
    private const val COULEUR_TAILLE = "#AB47BC"          // Violet : taille, effeuillage, pincement
    private const val COULEUR_TRAITEMENT = "#EF5350"      // Rouge : traitement, surveillance maladies
    private const val COULEUR_RECOLTE = "#42A5F5"         // Bleu : récolte, arrêt arrosage
    
    /**
     * Retourne la liste des opérations culturales pour un légume donné.
     * 
     * Les délais sont comptés à partir de la date de plantation (ou semis direct).
     * Pour les légumes non listés, retourne une liste vide.
     */
    fun getOperationsPourLegume(legumeNom: String): List<OperationCulturale> {
        val nomBase = if (legumeNom.contains("(")) {
            legumeNom.substringBefore("(").trim()
        } else {
            legumeNom
        }
        
        return when (nomBase) {
            
            // ========== TOMATE ==========
            "Tomate" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Installer les tuteurs à 10-15 cm des plants, enfoncés de 20 cm minimum. Utiliser des tuteurs solides (bambou, fer, bois).",
                    conseil = "Planter le tuteur AVANT la tomate pour ne pas blesser les racines. Hauteur recommandée : 1,5 à 2 m.",
                    jourDebut = 0,
                    jourFin = 2,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "1er lien",
                    emoji = "🎀",
                    description = "Attacher la tige principale au tuteur avec un lien souple (raphia, ficelle douce) en formant un 8 pour ne pas serrer la tige.",
                    conseil = "Toujours attacher sous un bouquet floral pour ne pas glisser.",
                    jourDebut = 15,
                    jourFin = 17,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "Effeuillage",
                    emoji = "✂️",
                    description = "Supprimer les feuilles sous le 1er bouquet floral pour aérer le pied et limiter les maladies.",
                    conseil = "Ne pas enlever plus de 2-3 feuilles à la fois pour ne pas fatiguer la plante.",
                    jourDebut = 30,
                    jourFin = 45,
                    couleurHex = COULEUR_TAILLE
                ),
                OperationCulturale(
                    nom = "Gourmands",
                    emoji = "🌱",
                    description = "Supprimer les gourmands (pousses axillaires) qui poussent entre la tige et les feuilles. Ils épuisent la plante.",
                    conseil = "Pincer avec les doigts quand ils font 5-10 cm. À répéter toutes les semaines environ.",
                    jourDebut = 21,
                    jourFin = 90,
                    couleurHex = COULEUR_TAILLE
                ),
                OperationCulturale(
                    nom = "Pincement",
                    emoji = "🌸",
                    description = "Pincer la sommité de la tige principale pour hâter la maturation des fruits restants.",
                    conseil = "À faire fin août en climat tempéré pour concentrer l'énergie sur les fruits existants.",
                    jourDebut = 90,
                    jourFin = 100,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            // ========== POMME DE TERRE ==========
            "Pomme de terre" -> listOf(
                OperationCulturale(
                    nom = "1er buttage",
                    emoji = "⛰️",
                    description = "Ramener de la terre sur les tiges pour former une butte. À faire quand le feuillage atteint 25 cm environ.",
                    conseil = "Butter permet de protéger les tubercules du soleil (qui les rend verts et toxiques) et favorise le développement.",
                    jourDebut = 21,
                    jourFin = 25,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "2e buttage",
                    emoji = "⛰️",
                    description = "Butter une seconde fois pour maintenir les tubercules sous terre et protéger les tiges.",
                    conseil = "Utiliser une butteuse ou une binette. Butter haut (15-20 cm).",
                    jourDebut = 42,
                    jourFin = 48,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "Doryphores",
                    emoji = "🐛",
                    description = "Surveiller l'arrivée des doryphores (coléoptères rayés jaune et noir) et de leurs larves rouges. Ramassage manuel.",
                    conseil = "Passer tous les 2-3 jours. Ne pas utiliser de pesticide pour préserver les auxiliaires.",
                    jourDebut = 30,
                    jourFin = 75,
                    couleurHex = COULEUR_TRAITEMENT
                ),
                OperationCulturale(
                    nom = "Arrêt arrosage",
                    emoji = "🚫💧",
                    description = "Arrêter l'arrosage 2-3 semaines avant la récolte pour que les tubercules se conservent mieux.",
                    conseil = "Les fanes commencent à jaunir : c'est le signal.",
                    jourDebut = 90,
                    jourFin = 100,
                    couleurHex = COULEUR_RECOLTE
                )
            )
            
            // ========== CAROTTE ==========
            "Carotte" -> listOf(
                OperationCulturale(
                    nom = "Éclaircissage",
                    emoji = "✂️",
                    description = "Éclaircir pour ne garder qu'un plant tous les 3-5 cm. Les carottes ont besoin d'espace pour se développer.",
                    conseil = "Éclaircir par temps humide pour limiter les odeurs qui attirent la mouche de la carotte.",
                    jourDebut = 21,
                    jourFin = 28,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== RADIS ==========
            "Radis" -> listOf(
                OperationCulturale(
                    nom = "Éclaircissage",
                    emoji = "✂️",
                    description = "Éclaircir à 3-5 cm pour que les radis grossissent bien.",
                    conseil = "Les radis éclaircis peuvent être mangés en primeur.",
                    jourDebut = 10,
                    jourFin = 14,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== BETTERAVE ==========
            "Betterave" -> listOf(
                OperationCulturale(
                    nom = "Éclaircissage",
                    emoji = "✂️",
                    description = "Éclaircir à 10-15 cm pour permettre aux racines de grossir.",
                    conseil = "Les jeunes pousses éclaircies se mangent en salade.",
                    jourDebut = 21,
                    jourFin = 30,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== POIREAU ==========
            "Poireau" -> listOf(
                OperationCulturale(
                    nom = "Repiquage",
                    emoji = "🌱",
                    description = "Repiquer les poireaux quand ils ont la taille d'un crayon (20-25 cm), tous les 10-15 cm.",
                    conseil = "Raccourcir les racines et la pointe des feuilles avant repiquage pour favoriser la reprise.",
                    jourDebut = 60,
                    jourFin = 70,
                    couleurHex = COULEUR_PLANTATION
                ),
                OperationCulturale(
                    nom = "Buttage",
                    emoji = "⛰️",
                    description = "Butter régulièrement pour blanchir les fûts et avoir des poireaux tendres sur une plus grande longueur.",
                    conseil = "Butter tous les 15 jours environ, jusqu'à mi-octobre.",
                    jourDebut = 90,
                    jourFin = 150,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== SALADE ==========
            "Salade" -> listOf(
                OperationCulturale(
                    nom = "Arrosage",
                    emoji = "💧",
                    description = "Arroser régulièrement pour garder le sol frais. La salade monte vite en graines si elle a soif.",
                    conseil = "Arroser le matin, au pied, pour éviter les maladies.",
                    jourDebut = 7,
                    jourFin = 60,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== HARICOT VERT ==========
            "Haricot vert" -> listOf(
                OperationCulturale(
                    nom = "Buttage",
                    emoji = "⛰️",
                    description = "Butter légèrement les pieds pour les consolider et favoriser l'enracinement.",
                    conseil = "Butter quand les plants font 20 cm environ.",
                    jourDebut = 21,
                    jourFin = 28,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== PETIT POIS ==========
            "Petit pois" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Installer des rames ou filets pour soutenir les pois qui grimpent.",
                    conseil = "Placer les rames avant que les pois ne s'affalent.",
                    jourDebut = 30,
                    jourFin = 40,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== FÈVE ==========
            "Fève" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Tuteurer les fèves hautes pour éviter qu'elles ne s'affalent avec le vent.",
                    conseil = "Pincer la sommité quand 5-6 gousses sont formées pour concentrer la sève.",
                    jourDebut = 30,
                    jourFin = 45,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "Pincement",
                    emoji = "✂️",
                    description = "Pincer la sommité pour hâter la maturation et éliminer les pucerons noirs qui s'y installent.",
                    conseil = "Garder 5-6 étages de gousses.",
                    jourDebut = 60,
                    jourFin = 70,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            // ========== CONCOMBRE ==========
            "Concombre" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Installer un support vertical (treillis, filet) pour que les concombres grimpent et restent propres.",
                    conseil = "Les concombres au sol attrapent plus de maladies.",
                    jourDebut = 15,
                    jourFin = 21,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "Taille",
                    emoji = "✂️",
                    description = "Pincer la tige principale au-dessus de la 5e feuille pour favoriser les ramifications latérales.",
                    conseil = "Tailler les pousses latérales après 2 feuilles et 1 fruit.",
                    jourDebut = 30,
                    jourFin = 45,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            // ========== COURGETTE ==========
            "Courgette" -> listOf(
                OperationCulturale(
                    nom = "Paillage",
                    emoji = "🌾",
                    description = "Pailler le pied pour garder la fraîcheur et éviter les mauvaises herbes.",
                    conseil = "Ne pas arroser le feuillage pour éviter l'oïdium.",
                    jourDebut = 15,
                    jourFin = 21,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== MELON ==========
            "Melon" -> listOf(
                OperationCulturale(
                    nom = "1ère taille",
                    emoji = "✂️",
                    description = "Tailler au-dessus de la 3e feuille pour favoriser les ramifications.",
                    conseil = "Conserver 2-3 ramifications principales.",
                    jourDebut = 21,
                    jourFin = 28,
                    couleurHex = COULEUR_TAILLE
                ),
                OperationCulturale(
                    nom = "2e taille",
                    emoji = "✂️",
                    description = "Tailler les ramifications à 2 feuilles après le fruit formé.",
                    conseil = "Supprimer les fleurs mâles pour concentrer l'énergie.",
                    jourDebut = 45,
                    jourFin = 55,
                    couleurHex = COULEUR_TAILLE
                ),
                OperationCulturale(
                    nom = "Paillage",
                    emoji = "🌾",
                    description = "Pailler sous les fruits pour éviter qu'ils pourrissent au contact du sol.",
                    conseil = "Mettre une tuile ou une planche sous chaque melon.",
                    jourDebut = 60,
                    jourFin = 70,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== AUBERGINE ==========
            "Aubergine" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Tuteurer les plants car les aubergines deviennent lourdes et peuvent casser.",
                    conseil = "Installer le tuteur à la plantation ou peu après.",
                    jourDebut = 15,
                    jourFin = 21,
                    couleurHex = COULEUR_ENTRETIEN
                ),
                OperationCulturale(
                    nom = "Effeuillage",
                    emoji = "✂️",
                    description = "Supprimer les feuilles du bas et les gourmands pour aérer.",
                    conseil = "Ne garder qu'une tige principale et 2-3 ramifications.",
                    jourDebut = 45,
                    jourFin = 60,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            // ========== POIVRON ==========
            "Poivron" -> listOf(
                OperationCulturale(
                    nom = "Tuteurage",
                    emoji = "🪴",
                    description = "Tuteurer les plants pour soutenir les fruits.",
                    conseil = "Utiliser des tuteurs discrets pour ne pas abîmer les racines.",
                    jourDebut = 15,
                    jourFin = 21,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== CHOU ==========
            "Chou", "Chou pommé", "Chou-fleur", "Brocoli", "Chou frisé (Kale)" -> listOf(
                OperationCulturale(
                    nom = "Piéride",
                    emoji = "🦋",
                    description = "Surveiller la ponte de la piéride du chou (papillon blanc). Ramasser les chenilles vertes.",
                    conseil = "Utiliser un voile anti-insectes dès la plantation pour éviter les pontes.",
                    jourDebut = 15,
                    jourFin = 60,
                    couleurHex = COULEUR_TRAITEMENT
                ),
                OperationCulturale(
                    nom = "Buttage",
                    emoji = "⛰️",
                    description = "Butter légèrement les pieds pour soutenir la plante et favoriser l'enracinement.",
                    conseil = "Butter quand les plants font 30 cm environ.",
                    jourDebut = 30,
                    jourFin = 45,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== FRAISIER ==========
            "Fraisier" -> listOf(
                OperationCulturale(
                    nom = "Stolons",
                    emoji = "✂️",
                    description = "Supprimer les stolons (runners) pour concentrer l'énergie sur la production de fruits.",
                    conseil = "Laisser quelques stolons si on veut multiplier les plants.",
                    jourDebut = 30,
                    jourFin = 90,
                    couleurHex = COULEUR_TAILLE
                ),
                OperationCulturale(
                    nom = "Paillage",
                    emoji = "🌾",
                    description = "Pailler sous les fruits pour éviter qu'ils ne pourrissent au contact du sol.",
                    conseil = "Paille de blé ou copeaux de lin.",
                    jourDebut = 60,
                    jourFin = 75,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== OIGNON ==========
            "Oignon" -> listOf(
                OperationCulturale(
                    nom = "Arrêt arrosage",
                    emoji = "🚫💧",
                    description = "Arrêter l'arrosage 3 semaines avant la récolte pour permettre aux bulbes de bien se conserver.",
                    conseil = "Quand les feuilles commencent à jaunir, on arrête.",
                    jourDebut = 90,
                    jourFin = 105,
                    couleurHex = COULEUR_RECOLTE
                )
            )
            
            // ========== AIL ==========
            "Ail" -> listOf(
                OperationCulturale(
                    nom = "Arrêt arrosage",
                    emoji = "🚫💧",
                    description = "Arrêter l'arrosage 3 semaines avant la récolte.",
                    conseil = "Quand les feuilles jaunissent et se couchent.",
                    jourDebut = 180,
                    jourFin = 210,
                    couleurHex = COULEUR_RECOLTE
                )
            )
            
            // ========== AROMATIQUES ==========
            "Basilic" -> listOf(
                OperationCulturale(
                    nom = "Pincement fleurs",
                    emoji = "🌸",
                    description = "Pincer les fleurs dès leur apparition pour que la plante continue à produire des feuilles.",
                    conseil = "Récolter régulièrement le haut des tiges pour stimuler la croissance.",
                    jourDebut = 30,
                    jourFin = 120,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            "Menthe" -> listOf(
                OperationCulturale(
                    nom = "Limiter expansion",
                    emoji = "✂️",
                    description = "Arracher les rhizomes qui débordent du carré pour éviter que la menthe n'envahisse tout.",
                    conseil = "Planter la menthe dans un pot enterré pour limiter son expansion.",
                    jourDebut = 60,
                    jourFin = 120,
                    couleurHex = COULEUR_TAILLE
                )
            )
            
            "Ciboulette" -> listOf(
                OperationCulturale(
                    nom = "Division",
                    emoji = "✂️",
                    description = "Diviser la touffe tous les 2-3 ans pour la rajeunir.",
                    conseil = "À faire au printemps ou à l'automne.",
                    jourDebut = 365,
                    jourFin = 400,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== FLEURS ==========
            "Souci", "Capucine", "Bourrache", "Cosmos", "Œillet d'Inde", "Phacélie" -> listOf(
                OperationCulturale(
                    nom = "Fleur fanée",
                    emoji = "🌸",
                    description = "Supprimer les fleurs fanées pour prolonger la floraison.",
                    conseil = "Pour certaines (souci, capucine), laisser quelques fleurs monter en graines pour un ressemis spontané.",
                    jourDebut = 60,
                    jourFin = 150,
                    couleurHex = COULEUR_ENTRETIEN
                )
            )
            
            // ========== PAR DÉFAUT ==========
            else -> emptyList()
        }
    }
    
    /**
     * Retourne la liste des opérations triées par date.
     */
    fun getOperationsTriees(legumeNom: String): List<OperationCulturale> {
        return getOperationsPourLegume(legumeNom).sortedBy { it.jourDebut }
    }
}
