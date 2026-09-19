package com.theshire.app.data

/**
 * Logique de calcul du nombre d'emplacements dans un contenant urbain.
 * 
 * Principe :
 * - En agriculture urbaine, on concentre les plants (densité × 2)
 * - Le nombre d'emplacements = surface utile (m²) × densité urbaine (plants/m²)
 * - Le résultat est arrondi à l'entier inférieur, minimum 1
 * 
 * Exemples :
 * - Pot Ø 30 cm (0,07 m²) + Basilic (15 plants/m² → 30 urbain)
 *   → 0,07 × 30 = 2,1 → 2 plants
 * - Jardinière 60×20 cm (0,12 m²) + Salade (15 plants/m² → 30 urbain)
 *   → 0,12 × 30 = 3,6 → 3 plants
 * - Pot Ø 30 cm + Tomate (3 plants/m² → 6 urbain)
 *   → 0,07 × 6 = 0,42 → 1 plant (minimum)
 * 
 * Le calcul prend en compte la variété si elle change la densité
 * (mais pour l'instant, on utilise la densité de l'espèce de base).
 */
object CalculEmplacements {
    
    /**
     * Coefficient multiplicateur appliqué à la densité pleine terre
     * pour obtenir la densité urbaine.
     * 
     * En pot, on concentre les plants car le substrat est plus riche
     * et l'arrosage/fertilisation sont contrôlés.
     */
    private const val COEFFICIENT_URBAIN = 2.0
    
    /**
     * Calcule le nombre d'emplacements pour un contenant donné et une plante donnée.
     * 
     * @param contenant Le contenant (avec ses dimensions)
     * @param densitePleineTerre La densité de la plante en pleine terre (plants/m²)
     * @return Le nombre d'emplacements (minimum 1)
     */
    fun calculerNombreEmplacements(
        contenant: ContenantEntity,
        densitePleineTerre: Int
    ): Int {
        // 1. Surface du contenant en m²
        val surfaceM2 = contenant.surfaceM2()
        
        // 2. Densité urbaine = densité pleine terre × coefficient
        val densiteUrbaine = densitePleineTerre * COEFFICIENT_URBAIN
        
        // 3. Nombre d'emplacements
        val nombre = surfaceM2 * densiteUrbaine
        
        // 4. Arrondi à l'entier inférieur, minimum 1
        return maxOf(1, kotlin.math.floor(nombre).toInt())
    }
    
    /**
     * Calcule le nombre d'emplacements en utilisant directement la densité
     * extraite du champ `plantation` de LegumeEntity.
     * 
     * Exemple de plantation : "Semis direct - Distance : 5-8 cm entre plants,
     * 25-30 cm entre rangs, 50-80 plants/m²"
     * → densité extraite : 65 (moyenne de 50-80)
     * 
     * Si la densité n'est pas trouvée, on utilise une densité par défaut selon
     * la catégorie du légume.
     * 
     * @param contenant Le contenant
     * @param legume Le légume (avec son champ `plantation` et `categorie`)
     */
    fun calculerNombreEmplacements(
        contenant: ContenantEntity,
        legume: LegumeEntity
    ): Int {
        val densite = extraireDensite(legume)
        return calculerNombreEmplacements(contenant, densite)
    }
    
    /**
     * Extrait la densité (plants/m²) du champ `plantation` d'un légume.
     * 
     * Format attendu : "50-80 plants/m²" ou "25 plants/m²" ou "2-3 plants/m²"
     * 
     * @return La densité (moyenne si fourchette). Si non trouvée, retourne
     * une valeur par défaut selon la catégorie.
     */
    fun extraireDensite(legume: LegumeEntity): Int {
        // Regex pour trouver "X plants/m²" ou "X-Y plants/m²" ou "X,Y plants/m²"
        val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) plants/m²").find(legume.plantation)
        
        if (match != null) {
            val valeur = match.groupValues[1]
            return when {
                valeur.contains(",") -> valeur.replace(",", ".").toDouble().toInt()
                valeur.contains("-") -> {
                    val parts = valeur.split("-")
                    (parts[0].toInt() + parts[1].toInt()) / 2
                }
                else -> valeur.toInt()
            }
        }
        
        // Densité par défaut selon la catégorie
        return when (legume.categorie) {
            "Fruit", "Cucurbitacée", "Chou", "Tubercule" -> 4
            "Racine", "Alliacé" -> 30
            "Feuille", "Légumineuse" -> 20
            "Aromatique" -> 15
            "Fleur annuelle", "Fleur vivace" -> 10
            else -> 9
        }
    }
    
    /**
     * Calcule l'espacement entre plants en cm, à partir de la densité.
     * 
     * Utile pour la plantation en contenant : au lieu de dire "12 plants/m²",
     * on dit "espacement 28 cm entre plants".
     * 
     * @param densiteUrbaine Densité en plants/m²
     * @return Espacement en cm
     */
    fun calculerEspacementCm(densiteUrbaine: Int): Int {
        if (densiteUrbaine <= 0) return 30
        // Si densité = 30 plants/m² → 1 plant par 0,033 m² → espacement ~18 cm
        val surfaceParPlantM2 = 1.0 / densiteUrbaine
        val espacementM = kotlin.math.sqrt(surfaceParPlantM2)
        return (espacementM * 100).toInt()
    }
    
    /**
     * Retourne la densité urbaine (pleine terre × 2) pour affichage.
     * 
     * @param legume Le légume
     * @return Densité urbaine en plants/m²
     */
    fun densiteUrbaine(legume: LegumeEntity): Int {
        return extraireDensite(legume) * COEFFICIENT_URBAIN.toInt()
    }
    
    /**
     * Retourne la densité urbaine pour une densité donnée.
     */
    fun densiteUrbaine(densitePleineTerre: Int): Int {
        return densitePleineTerre * COEFFICIENT_URBAIN.toInt()
    }
    
    /**
     * Vérifie si une plante peut être installée dans un contenant
     * en fonction de sa taille.
     * 
     * Par exemple, une tomate nécessite au minimum Ø 25 cm.
     * Un potiron nécessite Ø 40 cm minimum.
     * 
     * @param contenant Le contenant
     * @param legume Le légume à tester
     * @return true si la plante peut être installée
     */
    fun peutAccueillir(contenant: ContenantEntity, legume: LegumeEntity): Boolean {
        // Surface minimale requise selon la catégorie (en cm²)
        val surfaceMinRequiseCm2 = when (legume.categorie) {
            "Fruit" -> {
                // Tomate, poivron, aubergine, concombre : Ø 25-30 cm minimum
                when (legume.nom) {
                    "Tomate", "Aubergine", "Poivron", "Concombre", "Melon" -> 
                        Math.PI * 15 * 15  // Ø30 cm = ~707 cm²
                    else -> Math.PI * 12.5 * 12.5  // Ø25 cm = ~490 cm²
                }
            }
            "Cucurbitacée" -> {
                // Courge, potiron : Ø 40 cm minimum
                Math.PI * 20 * 20  // Ø40 cm = ~1256 cm²
            }
            "Tubercule" -> {
                // Pomme de terre, topinambour : Ø 30 cm minimum
                Math.PI * 15 * 15
            }
            "Chou" -> {
                // Chou : Ø 30 cm minimum
                Math.PI * 15 * 15
            }
            "Arbuste" -> {
                // Grands arbustes : Ø 40 cm
                Math.PI * 20 * 20
            }
            else -> {
                // Plantes compactes (salade, radis, aromatiques) : Ø 15 cm minimum
                Math.PI * 7.5 * 7.5  // Ø15 cm = ~176 cm²
            }
        }
        
        return contenant.surfaceCm2() >= surfaceMinRequiseCm2
    }
    
    /**
     * Retourne la surface minimale requise (en cm de diamètre) pour une plante.
     * Utilisée pour les messages d'avertissement.
     * 
     * @param legume Le légume
     * @return Diamètre minimum en cm
     */
    fun diametreMinRequis(legume: LegumeEntity): Int {
        return when (legume.categorie) {
            "Fruit" -> {
                when (legume.nom) {
                    "Tomate", "Aubergine", "Poivron", "Concombre", "Melon" -> 30
                    else -> 25
                }
            }
            "Cucurbitacée" -> 40
            "Tubercule", "Chou" -> 30
            else -> 15
        }
    }
}
