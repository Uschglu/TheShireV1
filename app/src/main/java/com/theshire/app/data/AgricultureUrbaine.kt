package com.theshire.app.data

/**
 * Base de connaissances : Agriculture urbaine.
 * 
 * Contient trois catégories :
 * - Les CONSEILS (exposition, arrosage, vent, froid, poids, lumière)
 * - Le MATÉRIEL spécifique urbain (pots, terreaux, systèmes, protection)
 * - Les PLANTES adaptées à la culture urbaine
 * 
 * Tout est en Kotlin pur (pas de Room), car le contenu est statique.
 * 
 * La liste des plantes urbaines fait référence aux noms d'espèces déjà
 * présents dans LegumeEntity, pour permettre la navigation directe vers
 * la fiche détaillée.
 */

// ============================================================
// STRUCTURE DE DONNÉES
// ============================================================

/**
 * Un conseil général sur l'agriculture urbaine.
 * 
 * @param id Identifiant technique
 * @param titre Titre du conseil
 * @param emoji Icône
 * @param categorie Catégorie ("Exposition", "Arrosage", "Vent", etc.)
 * @param description Explication détaillée
 * @param astuces Liste de conseils pratiques
 */
data class ConseilUrbain(
    val id: String,
    val titre: String,
    val emoji: String,
    val categorie: String,
    val description: String,
    val astuces: List<String> = emptyList()
)

/**
 * Un élément de matériel spécifique à la culture urbaine.
 * 
 * @param id Identifiant technique
 * @param nom Nom affiché
 * @param emoji Icône
 * @param categorie Catégorie ("Contenants", "Terreaux", "Arrosage", "Protection", "Éclairage")
 * @param description Description courte
 * @param utilisation Comment l'utiliser
 * @param conseil Conseil bonus
 * @param prixIndicatif Fourchette de prix indicative (optionnel)
 */
data class MaterielUrbain(
    val id: String,
    val nom: String,
    val emoji: String,
    val categorie: String,
    val description: String,
    val utilisation: String = "",
    val conseil: String = "",
    val prixIndicatif: String = ""
)

/**
 * Une plante adaptée à la culture urbaine.
 * 
 * @param nomEspece Nom exact de l'espèce (doit correspondre à LegumeEntity.nom)
 * @param emoji Icône
 * @param milieu "Balcon", "Intérieur" ou "Les deux"
 * @param difficulte "Facile", "Moyen", "Difficile"
 * @param exposition Besoin en lumière
 * @param contenantRequis Type de pot recommandé
 * @param arrosage Fréquence d'arrosage en pot
 * @param periodePlantation Quand planter
 * @param rendement Rendement attendu en pot
 * @param astuce Conseil spécifique
 */
data class PlanteUrbaine(
    val nomEspece: String,
    val emoji: String,
    val milieu: String,
    val difficulte: String,
    val exposition: String,
    val contenantRequis: String,
    val arrosage: String,
    val periodePlantation: String,
    val rendement: String,
    val astuce: String
)

// ============================================================
// OBJET PRINCIPAL
// ============================================================

object AgricultureUrbaine {
    
    // ========== CONSEILS GÉNÉRAUX ==========
    
    /**
     * Liste de tous les conseils pour l'agriculture urbaine.
     */
    fun getConseils(): List<ConseilUrbain> {
        return listOf(
            ConseilUrbain(
                id = "exposition",
                titre = "Choisir la bonne exposition",
                emoji = "☀️",
                categorie = "Exposition",
                description = "Sur un balcon ou une terrasse, l'exposition est déterminante. Chaque orientation a ses avantages et inconvénients. Observez votre espace aux différentes heures de la journée avant de planter.",
                astuces = listOf(
                    "Balcon SUD : idéal pour les légumes-fruits (tomates, poivrons, aubergines). Attention à la surchauffe en été, arrosez plus souvent.",
                    "Balcon EST : soleil le matin, mi-ombre l'après-midi. Parfait pour salades, aromatiques, radis.",
                    "Balcon OUEST : soleil l'après-midi, chaud en été. Bon pour tomates, herbes méditerranéennes.",
                    "Balcon NORD : ombre quasi permanente. Limitez-vous aux salades, épinards, menthe, persil, mâche.",
                    "Intérieur derrière une fenêtre SUD : jusqu'à 6-8h de soleil direct. Idéal pour la majorité des plantes.",
                    "Intérieur derrière une fenêtre NORD : 2-3h de lumière faible. Prévoyez une lampe LED horticole."
                )
            ),
            ConseilUrbain(
                id = "lumiere",
                titre = "Comprendre la lumière",
                emoji = "💡",
                categorie = "Exposition",
                description = "La lumière est le premier facteur limitant en agriculture urbaine. Une plante qui manque de lumière s'étiole, jaunit et ne produit pas. La plupart des légumes-fruits ont besoin de 6h de soleil direct minimum.",
                astuces = listOf(
                    "Besoin faible (2-4h) : salades, épinards, mâche, menthe, ciboulette",
                    "Besoin moyen (4-6h) : persil, basilic, radis, fraisier, tomate cerise",
                    "Besoin fort (6-8h) : tomate, poivron, aubergine, concombre, courgette",
                    "Utilisez un luxmètre (appli smartphone) pour mesurer la luminosité réelle.",
                    "Une fenêtre double vitrage filtre 30-50% de la lumière. Privilégiez le simple vitrage.",
                    "Nettoyez régulièrement vos vitres : la poussière peut réduire la lumière de 20%."
                )
            ),
            ConseilUrbain(
                id = "arrosage",
                titre = "Maîtriser l'arrosage en pot",
                emoji = "💧",
                categorie = "Arrosage",
                description = "En pot, l'eau s'évapore beaucoup plus vite qu'en pleine terre. Un pot peut se dessécher en 24h en été. L'arrosage est le geste le plus important en agriculture urbaine.",
                astuces = listOf(
                    "Arrosez abondamment jusqu'à ce que l'eau sorte par les trous de drainage.",
                    "Vérifiez l'humidité avec le doigt : si le premier cm est sec, arrosez.",
                    "En été : 1 à 2 fois par jour selon la chaleur. Au printemps/automne : tous les 2-3 jours.",
                    "Arrosez le matin tôt ou le soir tard pour limiter l'évaporation.",
                    "Ne laissez JAMAIS d'eau stagnante dans les soucoupes (pourriture des racines).",
                    "Paillez la surface des pots (billes d'argile, cosses de cacao) pour réduire l'évaporation.",
                    "Un goutte-à-goutte solaire ou un oya (pot en terre cuite poreuse) sont d'excellents investissements.",
                    "En hiver, réduisez fortement : un arrosage tous les 7-10 jours suffit."
                )
            ),
            ConseilUrbain(
                id = "vent",
                titre = "Protéger du vent",
                emoji = "💨",
                categorie = "Vent",
                description = "En hauteur, le vent est plus fort qu'au sol. Il dessèche les plantes, casse les tiges et renverse les pots. C'est un facteur à ne pas négliger sur les balcons et terrasses.",
                astuces = listOf(
                    "Installez un brise-vent ajouré (canisse, filet brise-vue) : il filtre sans bloquer.",
                    "Évitez les brise-vents pleins : ils créent des turbulences qui abîment plus.",
                    "Tuteurez systématiquement les plantes hautes (tomates, haricots, tournesols).",
                    "Posez des soucoupes lourdes ou des pieds lestés pour stabiliser les pots.",
                    "Regroupez les pots : ils se protègent mutuellement.",
                    "Choisissez des variétés naines ou buissonnantes sur les balcons exposés."
                )
            ),
            ConseilUrbain(
                id = "froid",
                titre = "Protéger du froid",
                emoji = "❄️",
                categorie = "Protection",
                description = "En ville, le gel peut être plus tardif au printemps et plus précoce en automne qu'en pleine terre (surtout en hauteur). Les plantes en pot sont plus sensibles au gel car leurs racines sont exposées.",
                astuces = listOf(
                    "Rentrez les pots sensibles dès les premières gelées (basilic, tomate, piment).",
                    "Utilisez un voile d'hivernage (P17 ou P30) sur les plantes en place.",
                    "Regroupez les pots contre un mur exposé sud : la chaleur accumulée protège.",
                    "Surélevez les pots du sol (cales en bois, briques) pour éviter le gel par le bas.",
                    "Réduisez l'arrosage en hiver : trop d'eau + froid = pourriture.",
                    "Certaines plantes résistent très bien : mâche, épinard d'hiver, ail, thym, romarin."
                )
            ),
            ConseilUrbain(
                id = "poids",
                titre = "Attention au poids",
                emoji = "⚖️",
                categorie = "Sécurité",
                description = "Un pot de 30 litres rempli de terreau pèse entre 30 et 50 kg. Une jardinière de 1 mètre peut dépasser 80 kg. Les balcons ont des limites de charge à respecter pour votre sécurité.",
                astuces = listOf(
                    "Limite standard d'un balcon d'appartement : 350 kg/m² (norme française).",
                    "Privilégiez les pots en plastique, résine ou géotextile (bien plus légers).",
                    "Utilisez des billes d'argile au fond (2-3 cm) : drainage + allègement.",
                    "Mélangez des billes d'argile ou de la perlite dans le terreau (10-20%).",
                    "Répartissez les pots sur toute la surface, pas tous dans un coin.",
                    "Surélevez légèrement les pots pour que l'air circule (évite l'humidité stagnante et le poids d'eau).",
                    "En cas de doute sur une terrasse, consultez un professionnel (étanchéité + charge)."
                )
            ),
            ConseilUrbain(
                id = "substrat",
                titre = "Choisir son terreau",
                emoji = "🌱",
                categorie = "Substrat",
                description = "En pot, la plante ne peut pas puiser ses nutriments dans le sol : tout vient du terreau. Un bon terreau est la base de la réussite en agriculture urbaine.",
                astuces = listOf(
                    "Terreau universel : bon compromis pour la majorité des légumes.",
                    "Terreau pour géraniums / plantes fleuries : plus riche, parfait pour tomates et poivrons.",
                    "Terreau pour semis : plus fin et plus léger, sans trop d'engrais.",
                    "Mélangez avec 20% de compost mûr pour enrichir naturellement.",
                    "Ajoutez 10-20% de perlite ou vermiculite pour l'aération et le drainage.",
                    "Renouvelez 1/3 du terreau chaque année (les nutriments s'épuisent).",
                    "N'utilisez JAMAIS de terre de jardin pure : trop lourde, mauvaise en pot."
                )
            ),
            ConseilUrbain(
                id = "fertilisation",
                titre = "Fertiliser régulièrement",
                emoji = "🌾",
                categorie = "Substrat",
                description = "En pot, les nutriments sont lessivés à chaque arrosage. Une plante en pot a besoin d'un apport d'engrais régulier, contrairement à une plante en pleine terre.",
                astuces = listOf(
                    "Engrais liquide bio : 1 fois par semaine en arrosage pendant la croissance.",
                    "Engrais en bâtonnets : 1 bâtonnet tous les 2 mois (moins précis mais pratique).",
                    "Compost mûr : en surface, 2-3 cm renouvelés au printemps et en automne.",
                    "Purin d'ortie ou de consoude : engrais naturel très efficace, dilué à 10%.",
                    "Attention au sur-engrais : brûle les racines. Respectez les doses.",
                    "Stoppez l'engrais en hiver (les plantes sont en repos)."
                )
            ),
            ConseilUrbain(
                id = "maladies",
                titre = "Prévenir les maladies",
                emoji = "🐛",
                categorie = "Protection",
                description = "En pot, les maladies et ravageurs sont plus fréquents : confinement, humidité, air stagnant. La prévention est essentielle.",
                astuces = listOf(
                    "Aérez régulièrement (fenêtre ouverte, ventilateur doux à l'intérieur).",
                    "N'arrosez pas le feuillage : uniquement le pied, le matin de préférence.",
                    "Surveillez les feuilles (dessous compris) 2-3 fois par semaine.",
                    "Savon noir dilué : contre pucerons, cochenilles, aleurodes.",
                    "Purin d'ortie : stimulant naturel et répulsif.",
                    "Retirez immédiatement les feuilles malades.",
                    "Isolez les plantes atteintes pour éviter la contamination."
                )
            ),
            ConseilUrbain(
                id = "pollinisation",
                titre = "Polliniser à la main",
                emoji = "🌸",
                categorie = "Fructification",
                description = "En intérieur ou sur un balcon en hauteur, les pollinisateurs (abeilles, bourdons) sont rares. Beaucoup de plantes (tomates, poivrons, aubergines) ont besoin d'aide pour fructifier.",
                astuces = listOf(
                    "Secouez délicatement les plants de tomates en fleur (le matin, par temps sec).",
                    "Utilisez un pinceau fin pour transférer le pollen d'une fleur à l'autre.",
                    "Pour les courges, mélangez le pollen des fleurs mâles et femelles à la main.",
                    "Ouvrez les fenêtres par beau temps pour laisser entrer les insectes.",
                    "Plantez des fleurs mellifères (souci, bourrache, capucine) pour attirer les pollinisateurs.",
                    "À l'intérieur : un petit ventilateur peut suffire à secouer le pollen."
                )
            ),
            ConseilUrbain(
                id = "interieur_specifique",
                titre = "Spécificités de l'intérieur",
                emoji = "🏠",
                categorie = "Intérieur",
                description = "Cultiver à l'intérieur est possible mais demande un peu plus d'attention. La lumière et l'humidité sont les deux grands défis.",
                astuces = listOf(
                    "Privilégiez une fenêtre exposée SUD, SUD-EST ou SUD-OUEST.",
                    "Ajoutez une lampe LED horticole si moins de 4h de soleil direct.",
                    "Retournez les pots d'un quart de tour chaque semaine (croissance homogène).",
                    "Attention à la climatisation : elle assèche l'air. Brumisez ou placez un bol d'eau.",
                    "Attention au chauffage : éloignez les plantes des radiateurs.",
                    "Surveillez les moucherons (moucherons du terreau) : arrosez moins, laissez sécher la surface.",
                    "N'utilisez JAMAIS d'insecticide chimique à l'intérieur.",
                    "Sortez les plantes à l'extérieur dès que possible (printemps, été) pour les fortifier."
                )
            ),
            ConseilUrbain(
                id = "espace",
                titre = "Optimiser l'espace",
                emoji = "📐",
                categorie = "Organisation",
                description = "L'espace est limité en ville. Quelques astuces permettent de cultiver beaucoup dans peu de surface.",
                astuces = listOf(
                    "Culture verticale : treillis, palissades, murs végétaux.",
                    "Pots empilables et jardinières suspendues.",
                    "Associez les cultures : salade sous tomates, radis entre carottes.",
                    "Choisissez des variétés naines ou compactes.",
                    "Utilisez des jardinières profondes (30 cm minimum) plutôt que larges et peu profondes.",
                    "Culture en lasagne : alternez couches de carton, compost, terreau pour créer un sol riche en hauteur."
                )
            )
        )
    }
    
    /**
     * Récupère les conseils par catégorie.
     */
    fun getConseilsParCategorie(categorie: String): List<ConseilUrbain> {
        return getConseils().filter { it.categorie.equals(categorie, ignoreCase = true) }
    }
    
    /**
     * Récupère toutes les catégories de conseils disponibles.
     */
    fun getCategoriesConseils(): List<String> {
        return getConseils().map { it.categorie }.distinct().sorted()
    }
        
    // ========== MATÉRIEL URBAIN ==========
    
    /**
     * Liste du matériel spécifique à la culture urbaine.
     * 
     * N.B. : les outils classiques (bêche, arrosoir...) sont dans Outils.kt
     * et apparaissent dans l'onglet "Équipement > Mes outils".
     */
    fun getMateriel(): List<MaterielUrbain> {
        return listOf(
            // --- CONTENANTS ---
            MaterielUrbain(
                id = "pot_terre_cuite",
                nom = "Pot en terre cuite",
                emoji = "🏺",
                categorie = "Contenants",
                description = "Matériau naturel et respirant. Idéal pour les plantes méditerranéennes (thym, romarin, lavande).",
                utilisation = "Choisissez un pot 2 à 3 cm plus large que la motte. Un pot trop grand garde l'eau et fait pourrir les racines.",
                conseil = "Attention au gel : la terre cuite peut casser en hiver. Rentrez ou protégez.",
                prixIndicatif = "5-15 € selon taille"
            ),
            MaterielUrbain(
                id = "pot_plastique",
                nom = "Pot en plastique",
                emoji = "🪣",
                categorie = "Contenants",
                description = "Léger, économique, idéal pour les balcons à charge limitée. Retient mieux l'humidité que la terre cuite.",
                utilisation = "Vérifiez toujours la présence de trous de drainage. Percez si nécessaire.",
                conseil = "Préférez les plastiques recyclés ou recyclables, et évitez le noir (surchauffe au soleil).",
                prixIndicatif = "1-5 € selon taille"
            ),
            MaterielUrbain(
                id = "pot_resine",
                nom = "Pot en résine (imitation terre cuite)",
                emoji = "🏺",
                categorie = "Contenants",
                description = "Aspect esthétique de la terre cuite, légèreté du plastique. Bon compromis.",
                utilisation = "Résiste au gel, au vent et aux UV. Idéal sur les terrasses exposées.",
                conseil = "Plus cher que le plastique mais bien plus durable.",
                prixIndicatif = "10-30 € selon taille"
            ),
            MaterielUrbain(
                id = "pot_geotextile",
                nom = "Pot en géotextile",
                emoji = "🛍️",
                categorie = "Contenants",
                description = "Sac textile perméable, très utilisé en permaculture. Excellente oxygénation des racines.",
                utilisation = "Idéal pour tomates, courgettes, pommes de terre. Se plie et se range en hiver.",
                conseil = "S'asseche plus vite que les autres pots. Arrosez plus souvent.",
                prixIndicatif = "3-10 € selon taille"
            ),
            MaterielUrbain(
                id = "jardiniere",
                nom = "Jardinière",
                emoji = "📦",
                categorie = "Contenants",
                description = "Grand contenant rectangulaire, parfait pour aligner plusieurs plants. Existe en plastique, résine, bois.",
                utilisation = "Profondeur minimum 25-30 cm pour les légumes-racines (carottes, radis).",
                conseil = "Préférez les modèles avec réserve d'eau intégrée pour espacer les arrosages.",
                prixIndicatif = "10-40 € selon taille"
            ),
            MaterielUrbain(
                id = "pot_empilable",
                nom = "Pot empilable / tour de culture",
                emoji = "🗼",
                categorie = "Contenants",
                description = "Système modulaire qui empile plusieurs pots sur un même support vertical. Gain de place considérable.",
                utilisation = "Idéal pour les fraises, salades, aromatiques. Chaque étage a son propre substrat.",
                conseil = "Stabilisez bien la base : une tour pleine peut être lourde et basculer.",
                prixIndicatif = "20-60 € selon nombre d'étages"
            ),
            MaterielUrbain(
                id = "pot_suspendu",
                nom = "Pot suspendu",
                emoji = "🪝",
                categorie = "Contenants",
                description = "Suspension à crochet, idéale pour les fraises, aromatiques retombantes, tomates cerises.",
                utilisation = "Utilisez un terreau léger et un arrosage régulier (les pots suspendus sèchent vite).",
                conseil = "Vérifiez la solidité du support avant d'accrocher (poids + vent).",
                prixIndicatif = "5-20 € selon modèle"
            ),
            
            // --- TERREAUX & SUBSTRATS ---
            MaterielUrbain(
                id = "terreau_universel",
                nom = "Terreau universel",
                emoji = "🌱",
                categorie = "Substrats",
                description = "Base de tout potager urbain. Convient à la majorité des légumes.",
                utilisation = "Mélangez avec 20% de compost et 10% de perlite pour un substrat idéal.",
                conseil = "Évitez les terreaux trop bon marché : souvent trop lourds ou peu riches.",
                prixIndicatif = "5-10 € les 50 L"
            ),
            MaterielUrbain(
                id = "terreau_semis",
                nom = "Terreau pour semis",
                emoji = "🌾",
                categorie = "Substrats",
                description = "Terreau fin et léger, pauvre en engrais, adapté aux jeunes pousses fragiles.",
                utilisation = "Utilisez dans les godets ou plaques alvéolées pour les semis.",
                conseil = "Ne tassez pas trop : les jeunes racines ont besoin d'air pour se développer.",
                prixIndicatif = "4-8 € les 20 L"
            ),
            MaterielUrbain(
                id = "compost",
                nom = "Compost mûr",
                emoji = "♻️",
                categorie = "Substrats",
                description = "Amendement naturel riche en nutriments, produit à partir de déchets organiques.",
                utilisation = "Mélangez 20% au terreau, ou étalez 2-3 cm en surface au printemps.",
                conseil = "Vous pouvez le faire vous-même avec un lombricomposteur d'appartement !",
                prixIndicatif = "Gratuit (fait maison) ou 5-10 € les 40 L"
            ),
            MaterielUrbain(
                id = "perlite",
                nom = "Perlite ou vermiculite",
                emoji = "⚪",
                categorie = "Substrats",
                description = "Roche volcanique expansée, allège le substrat et améliore le drainage.",
                utilisation = "Mélangez 10-20% dans votre terreau pour les plantes sensibles à l'excès d'eau.",
                conseil = "Particulièrement utile pour les pots en intérieur (évite la pourriture des racines).",
                prixIndicatif = "5-10 € le sac de 10 L"
            ),
            MaterielUrbain(
                id = "billes_argile",
                nom = "Billes d'argile",
                emoji = "🟤",
                categorie = "Substrats",
                description = "Au fond du pot : assurent le drainage. En surface : limitent l'évaporation.",
                utilisation = "Étalez 2-3 cm au fond du pot avant de mettre le terreau.",
                conseil = "Réutilisables plusieurs années : lavez-les à l'eau claire avant réutilisation.",
                prixIndicatif = "3-8 € le sac de 5 L"
            ),
            
            // --- ARROSAGE ---
            MaterielUrbain(
                id = "goutte_a_goutte_urbain",
                nom = "Kit goutte-à-goutte",
                emoji = "💧",
                categorie = "Arrosage",
                description = "Système d'arrosage automatique, indispensable pour les absences ou pour ne pas oublier d'arroser.",
                utilisation = "Installez un goutteur par pot. Branchez sur un programmateur ou une pompe solaire.",
                conseil = "Économie d'eau de 50-70%. Rentabilisé en une saison.",
                prixIndicatif = "15-40 € le kit complet"
            ),
            MaterielUrbain(
                id = "oya",
                nom = "Oya (pot en terre cuite poreuse)",
                emoji = "🏺",
                categorie = "Arrosage",
                description = "Pot en terre cuite non verni à enterrer. Diffuse l'eau lentement selon les besoins de la plante.",
                utilisation = "Enterrez au 2/3 près de la plante, remplissez-le d'eau toutes les 1-2 semaines.",
                conseil = "Système ancestral et très efficace, idéal pour les vacances.",
                prixIndicatif = "10-25 € selon taille"
            ),
            MaterielUrbain(
                id = "programmateur",
                nom = "Programmateur d'arrosage",
                emoji = "⏰",
                categorie = "Arrosage",
                description = "Se branche sur le robinet et déclenche l'arrosage automatiquement à heures fixes.",
                utilisation = "Programmez 1 à 2 arrosages par jour en été, tous les 3-4 jours en hiver.",
                conseil = "Modèles à pile ou solaires. Vérifiez régulièrement les piles.",
                prixIndicatif = "20-50 € selon modèle"
            ),
            MaterielUrbain(
                id = "brumisateur",
                nom = "Brumisateur",
                emoji = "💨",
                categorie = "Arrosage",
                description = "Petit vaporisateur pour humidifier les feuilles et jeunes plants.",
                utilisation = "Brumisez 1 à 2 fois par jour les semis et les plantes d'intérieur.",
                conseil = "Essentiel pour les semis et pour lutter contre la sécheresse de l'air intérieur.",
                prixIndicatif = "3-10 €"
            ),
            
            // --- PROTECTION ---
            MaterielUrbain(
                id = "voile_hivernage",
                nom = "Voile d'hivernage",
                emoji = "❄️",
                categorie = "Protection",
                description = "Toile non tissée qui protège les plantes du gel (jusqu'à -5°C pour P17, -8°C pour P30).",
                utilisation = "Enveloppez les pots ou posez sur les plantes avant les premières gelées.",
                conseil = "Retirez-le la journée pour aérer, remettez-le le soir.",
                prixIndicatif = "5-15 € selon taille"
            ),
            MaterielUrbain(
                id = "brise_vent",
                nom = "Brise-vent / canisse",
                emoji = "🎋",
                categorie = "Protection",
                description = "Canisse en bambou, filet brise-vue ou toile ajourée pour filtrer le vent.",
                utilisation = "Fixez sur les rambardes ou en hauteur. Laissez passer l'air (pas de barrière pleine).",
                conseil = "Un brise-vent doit être ajouré : un mur plein crée plus de turbulences.",
                prixIndicatif = "10-30 € le rouleau"
            ),
            MaterielUrbain(
                id = "voile_anti_insectes_urbain",
                nom = "Voile anti-insectes",
                emoji = "🕸️",
                categorie = "Protection",
                description = "Fine maille qui empêche les insectes ravageurs de pondre sur les cultures.",
                utilisation = "Posez sur arceaux au-dessus des cultures dès la plantation. Enterrez les bords.",
                conseil = "Particulièrement efficace contre la piéride du chou et la mouche de la carotte.",
                prixIndicatif = "10-20 € selon taille"
            ),
            MaterielUrbain(
                id = "soucoupe",
                nom = "Soucoupe / coupelle",
                emoji = "🍽️",
                categorie = "Protection",
                description = "Récupère l'eau d'arrosage. Attention : ne jamais laisser d'eau stagnante.",
                utilisation = "Videz la soucoupe 30 min après chaque arrosage.",
                conseil = "Utile contre les tâches d'eau sur le balcon. Préférez des modèles profonds avec grille.",
                prixIndicatif = "1-5 €"
            ),
            
            // --- ÉCLAIRAGE ---
            MaterielUrbain(
                id = "lampe_led_horticole",
                nom = "Lampe LED horticole",
                emoji = "💡",
                categorie = "Éclairage",
                description = "Éclairage artificiel spécial plantes (spectre rouge/bleu). Indispensable en intérieur mal exposé.",
                utilisation = "Placez à 20-30 cm au-dessus des plantes. Allumez 12-14h/jour en hiver.",
                conseil = "Choisissez un spectre complet (blanc + rouge + bleu), plus polyvalent.",
                prixIndicatif = "20-100 € selon puissance"
            ),
            MaterielUrbain(
                id = "minuteur_led",
                nom = "Minuteur programmable",
                emoji = "⏲️",
                categorie = "Éclairage",
                description = "Prise programmable qui allume et éteint automatiquement les lampes.",
                utilisation = "Programmez 12-16h d'éclairage/jour pour simuler une journée naturelle.",
                conseil = "Évitez d'éclairer la nuit (perturbe le cycle des plantes).",
                prixIndicatif = "5-15 €"
            ),
            
            // --- AUTRES ---
            MaterielUrbain(
                id = "pH_metre",
                nom = "Testeur pH / humidité",
                emoji = "📊",
                categorie = "Autres",
                description = "Appareil qui mesure le pH et l'humidité du terreau.",
                utilisation = "Mesurez régulièrement pour ajuster l'arrosage et détecter les carences.",
                conseil = "La plupart des légumes aiment un pH entre 6 et 7.",
                prixIndicatif = "10-25 €"
            ),
            MaterielUrbain(
                id = "etiquettes",
                nom = "Étiquettes de jardin",
                emoji = "🏷️",
                categorie = "Autres",
                description = "Petites étiquettes pour identifier chaque plantation.",
                utilisation = "Notez le nom, la variété et la date de plantation.",
                conseil = "Indispensable quand on commence à avoir plusieurs pots.",
                prixIndicatif = "3-8 € le lot"
            ),
            MaterielUrbain(
                id = "tablier_jardin",
                nom = "Tablier de jardinage",
                emoji = "🦺",
                categorie = "Autres",
                description = "Protège les vêtements et offre des poches pour les outils.",
                utilisation = "Enfilez avant chaque séance de jardinage, surtout en intérieur.",
                conseil = "Choisissez un modèle avec genouillère pour les travaux bas.",
                prixIndicatif = "15-30 €"
            )
        )
    }
    
    /**
     * Récupère le matériel par catégorie.
     */
    fun getMaterielParCategorie(categorie: String): List<MaterielUrbain> {
        return getMateriel().filter { it.categorie.equals(categorie, ignoreCase = true) }
    }
    
    /**
     * Récupère toutes les catégories de matériel disponibles.
     */
    fun getCategoriesMateriel(): List<String> {
        return getMateriel().map { it.categorie }.distinct().sorted()
    }
    
    // ========== PLANTES ADAPTÉES ==========
    
    /**
     * Liste des plantes adaptées à la culture urbaine.
     * 
     * Le champ `nomEspece` correspond exactement au nom dans LegumeEntity,
     * ce qui permet d'ouvrir la fiche détaillée en cliquant.
     */
    fun getPlantesAdaptees(): List<PlanteUrbaine> {
        return listOf(
            // --- AROMATIQUES (les plus faciles) ---
            PlanteUrbaine(
                nomEspece = "Basilic",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Facile",
                exposition = "Plein soleil, 6h minimum",
                contenantRequis = "Pot de 15-20 cm de diamètre minimum",
                arrosage = "Tous les jours en été, matin de préférence",
                periodePlantation = "Avril à juin (après les gelées)",
                rendement = "Abondant si pincé régulièrement",
                astuce = "Pincez les fleurs dès qu'elles apparaissent pour prolonger la récolte."
            ),
            PlanteUrbaine(
                nomEspece = "Menthe",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Très facile",
                exposition = "Mi-ombre à ombre",
                contenantRequis = "Pot de 20 cm minimum, à isoler (envahissante)",
                arrosage = "Tous les jours, sol toujours frais",
                periodePlantation = "Mars à mai",
                rendement = "Très abondant",
                astuce = "Plantez toujours la menthe dans son propre pot : elle envahit tout."
            ),
            PlanteUrbaine(
                nomEspece = "Persil",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Facile",
                exposition = "Mi-ombre à soleil",
                contenantRequis = "Pot de 20 cm de profondeur (racine pivotante)",
                arrosage = "Tous les 2 jours, sol frais",
                periodePlantation = "Mars à août",
                rendement = "Moyen à abondant",
                astuce = "Le persil met 3-4 semaines à germer. Semez en place, il n'aime pas le repiquage."
            ),
            PlanteUrbaine(
                nomEspece = "Thym",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Très facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 20 cm, drainage excellent",
                arrosage = "Tous les 3-4 jours, laissez sécher entre",
                periodePlantation = "Mars à mai",
                rendement = "Moyen mais durable",
                astuce = "Ajoutez du sable ou de la perlite au terreau pour un drainage optimal."
            ),
            PlanteUrbaine(
                nomEspece = "Romarin",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Très facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 30 cm minimum, drainage excellent",
                arrosage = "Tous les 4-5 jours, laissez sécher",
                periodePlantation = "Mars à mai",
                rendement = "Moyen mais durable",
                astuce = "Supporte très bien la sécheresse. Préférez la terre cuite."
            ),
            PlanteUrbaine(
                nomEspece = "Ciboulette",
                emoji = "🌿",
                milieu = "Les deux",
                difficulte = "Très facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 20 cm",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à mai",
                rendement = "Abondant si coupé régulièrement",
                astuce = "Coupez les brins à la base, jamais au milieu : ils repousseront plus vite."
            ),
            PlanteUrbaine(
                nomEspece = "Coriandre",
                emoji = "🌿",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 20 cm de profondeur",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à septembre",
                rendement = "Moyen, plusieurs semis possibles",
                astuce = "Semez toutes les 3 semaines pour avoir toujours de la coriandre fraîche."
            ),
            PlanteUrbaine(
                nomEspece = "Aneth",
                emoji = "🌿",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 25 cm de profondeur (racine pivotante)",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Avril à juillet",
                rendement = "Moyen",
                astuce = "Ne le repiquez pas : semez directement dans son pot définitif."
            ),
            
            // --- SALADES & FEUILLES ---
            PlanteUrbaine(
                nomEspece = "Salade",
                emoji = "🥬",
                milieu = "Les deux",
                difficulte = "Facile",
                exposition = "Mi-ombre à soleil (évitez le plein soleil d'été)",
                contenantRequis = "Jardinière de 20 cm de profondeur",
                arrosage = "Tous les jours en été",
                periodePlantation = "Mars à septembre",
                rendement = "1 salade par plant, plusieurs plants possibles",
                astuce = "Semez ou plantez 2-3 salades toutes les 2-3 semaines pour une récolte continue."
            ),
            PlanteUrbaine(
                nomEspece = "Roquette",
                emoji = "🥬",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Jardinière de 15 cm",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à septembre",
                rendement = "Abondant, plusieurs coupes",
                astuce = "Coupez les feuilles au ras : la roquette repousse 2-3 fois."
            ),
            PlanteUrbaine(
                nomEspece = "Épinard",
                emoji = "🥬",
                milieu = "Les deux",
                difficulte = "Facile",
                exposition = "Mi-ombre (préfère la fraîcheur)",
                contenantRequis = "Jardinière de 20 cm",
                arrosage = "Tous les jours en été",
                periodePlantation = "Mars à mai, août à septembre",
                rendement = "Moyen, plusieurs coupes",
                astuce = "Se cultive très bien à l'automne et en hiver, même en intérieur."
            ),
            PlanteUrbaine(
                nomEspece = "Blette",
                emoji = "🥬",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 25 cm minimum",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à mai",
                rendement = "Abondant, récoltes étalées",
                astuce = "Très productive en pot. Coupez les feuilles extérieures, le cœur repousse."
            ),
            PlanteUrbaine(
                nomEspece = "Chou frisé (Kale)",
                emoji = "🥬",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 30 cm minimum",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à juin",
                rendement = "Bon, plusieurs coupes",
                astuce = "Très rustique. Résiste au froid et continue à produire en hiver."
            ),
            
            // --- LÉGUMES-FRUITS ---
            PlanteUrbaine(
                nomEspece = "Tomate",
                emoji = "🍅",
                milieu = "Balcon",
                difficulte = "Moyen",
                exposition = "Plein soleil, 6-8h minimum",
                contenantRequis = "Pot de 30-40 cm de diamètre ou sac géotextile de 30 L",
                arrosage = "Tous les jours en été, au pied",
                periodePlantation = "Mai (après les gelées)",
                rendement = "2-5 kg par pied selon variété",
                astuce = "Préférez les variétés cerises ou à port déterminé, plus adaptées aux pots."
            ),
            PlanteUrbaine(
                nomEspece = "Tomate cerise",
                emoji = "🍅",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 25 cm minimum ou suspension",
                arrosage = "Tous les jours en été",
                periodePlantation = "Mai",
                rendement = "Abondant (grappes de 10-20 fruits)",
                astuce = "Idéale en suspension. N'a pas besoin d'être tuteurée si laissée retomber."
            ),
            PlanteUrbaine(
                nomEspece = "Poivron",
                emoji = "🫑",
                milieu = "Balcon",
                difficulte = "Moyen",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 30 cm",
                arrosage = "Tous les jours en été",
                periodePlantation = "Mai",
                rendement = "5-10 fruits par plant",
                astuce = "Brisez le vent autour : les poivrons sont fragiles aux courants d'air."
            ),
            PlanteUrbaine(
                nomEspece = "Piment",
                emoji = "🌶️",
                milieu = "Les deux",
                difficulte = "Moyen",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 25 cm",
                arrosage = "Tous les jours en été",
                periodePlantation = "Février à mai (sous abri)",
                rendement = "Abondant sur un seul pied",
                astuce = "Le piment se cultive très bien en intérieur près d'une fenêtre sud."
            ),
            PlanteUrbaine(
                nomEspece = "Aubergine",
                emoji = "🍆",
                milieu = "Balcon",
                difficulte = "Difficile",
                exposition = "Plein soleil, chaleur",
                contenantRequis = "Pot de 35 cm minimum",
                arrosage = "Tous les jours en été",
                periodePlantation = "Mai",
                rendement = "3-6 fruits par plant",
                astuce = "Choisissez des variétés précoces et naines (Violette de Florence)."
            ),
            PlanteUrbaine(
                nomEspece = "Concombre",
                emoji = "🥒",
                milieu = "Balcon",
                difficulte = "Moyen",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 30 cm + treillis vertical",
                arrosage = "Tous les jours, sol toujours frais",
                periodePlantation = "Mai",
                rendement = "10-20 concombres par pied",
                astuce = "Faites-le grimper sur un treillis : gain de place et fruits plus propres."
            ),
            PlanteUrbaine(
                nomEspece = "Courgette",
                emoji = "🥒",
                milieu = "Terrasse",
                difficulte = "Facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 40 cm minimum (grande plante)",
                arrosage = "Tous les jours voire 2x/jour en été",
                periodePlantation = "Mai",
                rendement = "Très abondant (10-20 courgettes par pied)",
                astuce = "Prévoyez beaucoup de place : une courgette en pot occupe 1 m²."
            ),
            
            // --- RACINES & BULBES ---
            PlanteUrbaine(
                nomEspece = "Radis",
                emoji = "🥕",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Jardinière de 15 cm de profondeur",
                arrosage = "Tous les jours, sol humide",
                periodePlantation = "Mars à septembre",
                rendement = "Abondant et rapide (18-25 jours)",
                astuce = "Idéal pour les débutants. Semez en continu tous les 15 jours."
            ),
            PlanteUrbaine(
                nomEspece = "Carotte",
                emoji = "🥕",
                milieu = "Balcon",
                difficulte = "Moyen",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 30 cm de profondeur minimum",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à juillet",
                rendement = "10-20 carottes par pot de 30 cm",
                astuce = "Choisissez des variétés courtes (Ronde de Paris, Amsterdam)."
            ),
            PlanteUrbaine(
                nomEspece = "Betterave",
                emoji = "🥕",
                milieu = "Balcon",
                difficulte = "Facile",
                exposition = "Soleil",
                contenantRequis = "Pot de 25 cm de profondeur",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Avril à juin",
                rendement = "5-10 betteraves par pot de 30 cm",
                astuce = "Récoltez les jeunes pousses comme des blettes."
            ),
            PlanteUrbaine(
                nomEspece = "Ail",
                emoji = "🧄",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 20 cm",
                arrosage = "Tous les 3-4 jours",
                periodePlantation = "Octobre à novembre",
                rendement = "1 tête par gousse plantée",
                astuce = "Plantez les gousses pointe vers le haut, à 3 cm de profondeur."
            ),
            
            // --- FRUITS ---
            PlanteUrbaine(
                nomEspece = "Fraisier",
                emoji = "🍓",
                milieu = "Les deux",
                difficulte = "Facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 25 cm ou suspension",
                arrosage = "Tous les jours",
                periodePlantation = "Mars à mai ou août à septembre",
                rendement = "20-40 fraises par pied",
                astuce = "Idéal en suspension : les fruits ne touchent pas le sol et restent propres."
            ),
            PlanteUrbaine(
                nomEspece = "Melon",
                emoji = "🍈",
                milieu = "Terrasse",
                difficulte = "Difficile",
                exposition = "Plein soleil, chaleur",
                contenantRequis = "Pot de 40 cm + support vertical",
                arrosage = "Tous les jours",
                periodePlantation = "Mai",
                rendement = "2-4 melons par pied",
                astuce = "Nouez les fruits en hauteur dans un filet pour les soutenir."
            ),
            
            // --- FLEURS COMESTIBLES ---
            PlanteUrbaine(
                nomEspece = "Capucine",
                emoji = "🌸",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Soleil à mi-ombre",
                contenantRequis = "Pot de 20 cm",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Avril à mai",
                rendement = "Fleurs et feuilles comestibles",
                astuce = "Naine en bordure, grimpante sur treillis. Plante piège à pucerons."
            ),
            PlanteUrbaine(
                nomEspece = "Souci",
                emoji = "🌸",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 20 cm",
                arrosage = "Tous les 2-3 jours",
                periodePlantation = "Mars à mai",
                rendement = "Fleurs comestibles et médicinales",
                astuce = "Attire les pollinisateurs et éloigne certains ravageurs. Idéal près des tomates."
            ),
            PlanteUrbaine(
                nomEspece = "Bourrache",
                emoji = "🌸",
                milieu = "Balcon",
                difficulte = "Très facile",
                exposition = "Plein soleil",
                contenantRequis = "Pot de 25 cm",
                arrosage = "Tous les 2 jours",
                periodePlantation = "Mars à mai",
                rendement = "Fleurs comestibles, attire les abeilles",
                astuce = "Se ressème toute seule. Compagne idéale des fraisiers."
            )
        )
    }
    
    /**
     * Récupère les plantes par milieu (Balcon / Intérieur / Les deux).
     */
    fun getPlantesParMilieu(milieu: String): List<PlanteUrbaine> {
        return getPlantesAdaptees().filter { 
            it.milieu.equals(milieu, ignoreCase = true) || it.milieu == "Les deux" 
        }
    }
    
    /**
     * Récupère les plantes par difficulté.
     */
    fun getPlantesParDifficulte(difficulte: String): List<PlanteUrbaine> {
        return getPlantesAdaptees().filter { it.difficulte.equals(difficulte, ignoreCase = true) }
    }
    
    /**
     * Récupère une plante par son nom d'espèce.
     */
    fun getPlanteParNom(nom: String): PlanteUrbaine? {
        return getPlantesAdaptees().find { it.nomEspece.equals(nom, ignoreCase = true) }
    }
}
