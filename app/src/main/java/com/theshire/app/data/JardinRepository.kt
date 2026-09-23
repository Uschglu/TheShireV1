package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class JardinRepository(context: Context) {
    
    private val appContext = context.applicationContext
    private val plancheDao = AppDatabase.getDatabase(context).plancheDao()
    private val legumeDao = AppDatabase.getDatabase(context).legumeDao()
    
    // Repository pour générer automatiquement les rappels culturaux
    private val rappelCulturelRepository = RappelCulturelRepository(context)
    
    // Repository pour gérer les cultures liées aux stocks
    private val cultureRepository = CultureRepository(context)
    
    val planches: Flow<List<PlancheEntity>> = plancheDao.getAllPlanches()
    
    fun getCarresForPlanche(plancheId: Long): Flow<List<CarreEntity>> {
        return plancheDao.getCarresForPlanche(plancheId)
    }
    
    suspend fun ajouterPlanche(nom: String, largeur: Int, longueur: Int): Long {
        val plancheId = plancheDao.insertPlanche(
            PlancheEntity(
                nom = nom,
                largeur = largeur,
                longueur = longueur
            )
        )
        
        for (y in 0 until longueur) {
            for (x in 0 until largeur) {
                plancheDao.insertCarre(
                    CarreEntity(
                        plancheId = plancheId,
                        positionX = x,
                        positionY = y
                    )
                )
            }
        }
        
        return plancheId
    }
    
    suspend fun supprimerPlanche(planche: PlancheEntity) {
        // Supprimer les rappels culturaux associés à cette planche
        rappelCulturelRepository.supprimerRappelsPourPlanche(planche.id)
        
        // Supprimer les cultures liées à cette planche
        cultureRepository.supprimerCulturesPourPlanche(planche.id)
        
        plancheDao.deleteCarresForPlanche(planche.id)
        plancheDao.deletePlanche(planche)
    }
    
    /**
     * Modifie une case précise d'un carré.
     * 
     * ⚠️ NOUVEAU : cette méthode crée désormais une CultureEntity en parallèle
     *    du remplissage de la case (pour lier la plantation au stock).
     * 
     * @param context Context (nécessaire pour lire ModePreferences)
     * @param carre Le carré concerné
     * @param caseNumero Numéro de la case (1-9)
     * @param legumeNom Nom du légume à planter (null = vider la case)
     * @param varieteNom Variété éventuelle (ex : "Marmande")
     * @param emoji Emoji du légume
     * @param plancheId ID de la planche (nécessaire pour générer les rappels culturaux)
     * @param sourceStock Source du stock ("Semis" / "JeunePlant" / "Graine" / "Aucune")
     * @param sourceStockId ID de l'entité source (JeunePlantEntity.id ou GraineEntity.id)
     * @return ResultatPlantation pour indiquer succès ou erreur (mode réel)
     */
    suspend fun modifierCasePrecise(
        context: Context,
        carre: CarreEntity,
        caseNumero: Int,
        legumeNom: String?,
        varieteNom: String? = null,
        emoji: String = "🌱",
        plancheId: Long,
        sourceStock: String = CultureEntity.SOURCE_AUCUNE,
        sourceStockId: Long? = null
    ): ResultatPlantation {
        val dateActuelle = System.currentTimeMillis()
        val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR)
        
        // ===== CAS 1 : on vide la case =====
        if (legumeNom == null) {
            // Supprimer la culture active dans cette case (si elle existe)
            val cultureExistante = cultureRepository
                .getCultureActiveDansCase(carre.id, caseNumero)
            if (cultureExistante != null) {
                cultureRepository.terminerSansRecolte(cultureExistante.id)
            }
            
            // Supprimer les rappels culturaux de cette case
            rappelCulturelRepository.supprimerRappelsPourCase(carre.id, caseNumero)
            
            // Vider la case dans CarreEntity
            val nouveauCarre = when (caseNumero) {
                1 -> carre.copy(case1 = null, datePlantationCase1 = null)
                2 -> carre.copy(case2 = null, datePlantationCase2 = null)
                3 -> carre.copy(case3 = null, datePlantationCase3 = null)
                4 -> carre.copy(case4 = null, datePlantationCase4 = null)
                5 -> carre.copy(case5 = null, datePlantationCase5 = null)
                6 -> carre.copy(case6 = null, datePlantationCase6 = null)
                7 -> carre.copy(case7 = null, datePlantationCase7 = null)
                8 -> carre.copy(case8 = null, datePlantationCase8 = null)
                9 -> carre.copy(case9 = null, datePlantationCase9 = null)
                else -> carre
            }
            
            val carreFinal = recalculerFamilles(nouveauCarre)
            plancheDao.updateCarre(carreFinal)
            
            return ResultatPlantation.Succes
        }
        
        // ===== CAS 2 : on plante un légume =====
        
        // Construire le nom complet (légume + variété)
        val nomComplet = if (varieteNom != null) "$legumeNom ($varieteNom)" else legumeNom
        
        // 2.a — Créer la CultureEntity (gère le mode réel / projection)
        val culture = CultureEntity(
            typeEmplacement = CultureEntity.TYPE_PLEINE_TERRE,
            plancheId = plancheId,
            carreId = carre.id,
            caseNumero = caseNumero,
            legumeNom = legumeNom,
            varieteNom = varieteNom,
            emoji = emoji,
            quantite = 1, // V1 : 1 plant par case
            sourceStock = sourceStock,
            estProjection = true, // sera écrasé par CultureRepository selon le mode
            datePlantation = dateActuelle
        )
        
        val resultatCulture = cultureRepository.creerCulture(
            context = context,
            culture = culture,
            sourceStockId = sourceStockId
        )
        
        // Si la culture a échoué (ex : stock insuffisant en mode réel), on bloque
        if (resultatCulture !is ResultatCreationCulture.Succes) {
            return when (resultatCulture) {
                is ResultatCreationCulture.ErreurSourceIntrouvable ->
                    ResultatPlantation.ErreurSourceIntrouvable(resultatCulture.source)
                is ResultatCreationCulture.ErreurSourceInactive ->
                    ResultatPlantation.ErreurSourceInactive(
                        resultatCulture.legumeNom,
                        resultatCulture.varieteNom
                    )
                is ResultatCreationCulture.ErreurStockInsuffisant ->
                    ResultatPlantation.ErreurStockInsuffisant(
                        resultatCulture.disponible,
                        resultatCulture.demande,
                        resultatCulture.legumeNom,
                        resultatCulture.varieteNom
                    )
                else -> ResultatPlantation.ErreurSourceIntrouvable("Inconnu")
            }
        }
        
        // 2.b — Mettre à jour la case dans CarreEntity
        val nouveauCarre = when (caseNumero) {
            1 -> carre.copy(
                case1 = nomComplet,
                datePlantationCase1 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            2 -> carre.copy(
                case2 = nomComplet,
                datePlantationCase2 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            3 -> carre.copy(
                case3 = nomComplet,
                datePlantationCase3 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            4 -> carre.copy(
                case4 = nomComplet,
                datePlantationCase4 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            5 -> carre.copy(
                case5 = nomComplet,
                datePlantationCase5 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            6 -> carre.copy(
                case6 = nomComplet,
                datePlantationCase6 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            7 -> carre.copy(
                case7 = nomComplet,
                datePlantationCase7 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            8 -> carre.copy(
                case8 = nomComplet,
                datePlantationCase8 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            9 -> carre.copy(
                case9 = nomComplet,
                datePlantationCase9 = dateActuelle,
                anneeCulture = anneeActuelle
            )
            else -> carre
        }
        
        val carreFinal = recalculerFamilles(nouveauCarre)
        plancheDao.updateCarre(carreFinal)
        
        // 2.c — Générer les rappels culturaux
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = carre.id,
            caseNumero = caseNumero,
            plancheId = plancheId
        )
        
        return ResultatPlantation.Succes
    }
    
    /**
     * Remplit les 9 cases d'un carré avec le même légume.
     * 
     * ⚠️ NOUVEAU : cette méthode crée désormais 9 CultureEntity (une par case),
     *    ou 1 seule selon le mode.
     * 
     * En mode réel : on demande 9 exemplaires du stock (9 semis, ou 9 graines).
     * Si le stock est insuffisant, on bloque AVANT de tout remplir.
     * 
     * @param context Context
     * @param carre Le carré à remplir
     * @param legumeNom Nom du légume (null = vider tout le carré)
     * @param varieteNom Variété éventuelle
     * @param emoji Emoji du légume
     * @param plancheId ID de la planche
     * @param sourceStock Source du stock
     * @param sourceStockId ID de l'entité source
     * @return ResultatPlantation
     */
    suspend fun remplirM2Entier(
        context: Context,
        carre: CarreEntity,
        legumeNom: String?,
        varieteNom: String? = null,
        emoji: String = "🌱",
        plancheId: Long,
        sourceStock: String = CultureEntity.SOURCE_AUCUNE,
        sourceStockId: Long? = null
    ): ResultatPlantation {
        val dateActuelle = System.currentTimeMillis()
        val anneeActuelle = Calendar.getInstance().get(Calendar.YEAR)
        
        // ===== CAS 1 : on vide tout le carré =====
        if (legumeNom == null) {
            // Terminer toutes les cultures actives du carré
            val culturesActives = cultureRepository
                .getCulturesActivesPourCarre(carre.id)
                .first()
            culturesActives.forEach { culture ->
                cultureRepository.terminerSansRecolte(culture.id)
            }
            
            // Supprimer les rappels culturaux du carré
            rappelCulturelRepository.supprimerRappelsPourCarre(carre.id)
            
            // Vider les 9 cases
            val carreFinal = carre.copy(
                case1 = null, case2 = null, case3 = null,
                case4 = null, case5 = null, case6 = null,
                case7 = null, case8 = null, case9 = null,
                datePlantationCase1 = null, datePlantationCase2 = null,
                datePlantationCase3 = null, datePlantationCase4 = null,
                datePlantationCase5 = null, datePlantationCase6 = null,
                datePlantationCase7 = null, datePlantationCase8 = null,
                datePlantationCase9 = null,
                famillesPlantees = ""
            )
            plancheDao.updateCarre(carreFinal)
            
            return ResultatPlantation.Succes
        }
        
        // ===== CAS 2 : on plante le même légume dans les 9 cases =====
        
        val nomComplet = if (varieteNom != null) "$legumeNom ($varieteNom)" else legumeNom
        
        // 2.a — Vérifier qu'on peut créer 9 cultures d'un coup (mode réel)
        val modeReel = ModePreferences.estModeReel(context)
        
        if (modeReel && sourceStock != CultureEntity.SOURCE_AUCUNE && sourceStockId != null) {
            when (sourceStock) {
                CultureEntity.SOURCE_SEMIS, CultureEntity.SOURCE_JEUNE_PLANT -> {
                    val jeunePlant = AppDatabase.getDatabase(context)
                        .jeunePlantDao()
                        .getJeunePlantParId(sourceStockId)
                    // ⚠️ CORRECTION : `estActif` (pas `estActive`)
                    if (jeunePlant == null || !jeunePlant.estActif) {
                        return ResultatPlantation.ErreurSourceInactive(legumeNom, varieteNom)
                    }
                    if (jeunePlant.quantite < 9) {
                        return ResultatPlantation.ErreurStockInsuffisant(
                            disponible = jeunePlant.quantite,
                            demande = 9,
                            legumeNom = legumeNom,
                            varieteNom = varieteNom
                        )
                    }
                }
                CultureEntity.SOURCE_GRAINE -> {
                    val graine = AppDatabase.getDatabase(context)
                        .graineDao()
                        .getGraineParId(sourceStockId)
                    // ⚠️ Ici c'est `estActive` (avec E) pour les graines
                    if (graine == null || !graine.estActif) {
                        return ResultatPlantation.ErreurSourceInactive(legumeNom, varieteNom)
                    }
                    if (graine.quantite < 9) {
                        return ResultatPlantation.ErreurStockInsuffisant(
                            disponible = graine.quantite,
                            demande = 9,
                            legumeNom = legumeNom,
                            varieteNom = varieteNom
                        )
                    }
                }
            }
        }
        
        // 2.b — Créer 9 cultures (une par case)
        for (caseNumero in 1..9) {
            val culture = CultureEntity(
                typeEmplacement = CultureEntity.TYPE_PLEINE_TERRE,
                plancheId = plancheId,
                carreId = carre.id,
                caseNumero = caseNumero,
                legumeNom = legumeNom,
                varieteNom = varieteNom,
                emoji = emoji,
                quantite = 1,
                sourceStock = sourceStock,
                estProjection = true,
                datePlantation = dateActuelle
            )
            
            val resultatCulture = cultureRepository.creerCulture(
                context = context,
                culture = culture,
                sourceStockId = sourceStockId
            )
            
            if (resultatCulture !is ResultatCreationCulture.Succes) {
                return when (resultatCulture) {
                    is ResultatCreationCulture.ErreurSourceIntrouvable ->
                        ResultatPlantation.ErreurSourceIntrouvable(resultatCulture.source)
                    is ResultatCreationCulture.ErreurSourceInactive ->
                        ResultatPlantation.ErreurSourceInactive(
                            resultatCulture.legumeNom,
                            resultatCulture.varieteNom
                        )
                    is ResultatCreationCulture.ErreurStockInsuffisant ->
                        ResultatPlantation.ErreurStockInsuffisant(
                            resultatCulture.disponible,
                            resultatCulture.demande,
                            resultatCulture.legumeNom,
                            resultatCulture.varieteNom
                        )
                    else -> ResultatPlantation.ErreurSourceIntrouvable("Inconnu")
                }
            }
        }
        
        // 2.c — Mettre à jour les 9 cases dans CarreEntity
        val carreFinal = carre.copy(
            case1 = nomComplet, case2 = nomComplet, case3 = nomComplet,
            case4 = nomComplet, case5 = nomComplet, case6 = nomComplet,
            case7 = nomComplet, case8 = nomComplet, case9 = nomComplet,
            datePlantationCase1 = dateActuelle, datePlantationCase2 = dateActuelle,
            datePlantationCase3 = dateActuelle, datePlantationCase4 = dateActuelle,
            datePlantationCase5 = dateActuelle, datePlantationCase6 = dateActuelle,
            datePlantationCase7 = dateActuelle, datePlantationCase8 = dateActuelle,
            datePlantationCase9 = dateActuelle,
            anneeCulture = anneeActuelle
        )
        
        val carreAvecFamilles = recalculerFamilles(carreFinal)
        plancheDao.updateCarre(carreAvecFamilles)
        
        // 2.d — Générer les rappels culturaux (une fois pour le carré entier)
        rappelCulturelRepository.genererRappelsPourPlantation(
            legumeNom = legumeNom,
            datePlantation = dateActuelle,
            carreId = carre.id,
            caseNumero = 5,
            plancheId = plancheId
        )
        
        return ResultatPlantation.Succes
    }
    
    /**
     * Recalcule les familles plantées d'un carré.
     */
    private fun recalculerFamilles(carre: CarreEntity): CarreEntity {
        val legumesActuels = listOfNotNull(
            carre.case1, carre.case2, carre.case3,
            carre.case4, carre.case5, carre.case6,
            carre.case7, carre.case8, carre.case9
        )
        
        val famillesSet = mutableSetOf<String>()
        legumesActuels.forEach { legume ->
            val famille = getFamilleLegume(legume)
            if (famille != "Autre") {
                famillesSet.add(famille)
            }
        }
        
        return carre.copy(famillesPlantees = famillesSet.joinToString(","))
    }
    
    suspend fun getLegumesPlantes(): List<String> {
        val listeLegumes = mutableListOf<String>()
        
        val toutesPlanches = plancheDao.getAllPlanches().first()
        
        toutesPlanches.forEach { planche ->
            val carres = plancheDao.getCarresForPlanche(planche.id).first()
            carres.forEach { carre ->
                listOf(
                    carre.case1, carre.case2, carre.case3,
                    carre.case4, carre.case5, carre.case6,
                    carre.case7, carre.case8, carre.case9
                ).forEach { legume ->
                    if (legume != null && legume !in listeLegumes) {
                        listeLegumes.add(legume)
                    }
                }
            }
        }
        
        return listeLegumes
    }
    
    suspend fun getDatesPlantation(): Map<String, Long> {
        val dates = mutableMapOf<String, Long>()
        val toutesPlanches = plancheDao.getAllPlanches().first()
        
        toutesPlanches.forEach { planche ->
            val carres = plancheDao.getCarresForPlanche(planche.id).first()
            carres.forEach { carre ->
                listOf(
                    carre.case1 to carre.datePlantationCase1,
                    carre.case2 to carre.datePlantationCase2,
                    carre.case3 to carre.datePlantationCase3,
                    carre.case4 to carre.datePlantationCase4,
                    carre.case5 to carre.datePlantationCase5,
                    carre.case6 to carre.datePlantationCase6,
                    carre.case7 to carre.datePlantationCase7,
                    carre.case8 to carre.datePlantationCase8,
                    carre.case9 to carre.datePlantationCase9
                ).forEach { (legume, date) ->
                    if (legume != null && date != null) {
                        dates[legume] = date
                    }
                }
            }
        }
        
        return dates
    }
    
    // ========== FONCTIONS DE VALIDATION ==========
    
    suspend fun getLegumeInfo(legumeNom: String): LegumeEntity? {
        val nomBase = if (legumeNom.contains("(")) {
            legumeNom.substringBefore("(").trim()
        } else {
            legumeNom
        }
        return legumeDao.getLegumeByNom(nomBase)
    }
    
    fun extraireDistanceRangs(plantation: String): Int {
        val match = Regex("(\\d+-\\d+|\\d+) cm entre rangs").find(plantation)
        return if (match != null) {
            val valeur = match.groupValues[1]
            if (valeur.contains("-")) {
                val parts = valeur.split("-")
                (parts[0].toInt() + parts[1].toInt()) / 2
            } else {
                valeur.toInt()
            }
        } else {
            30
        }
    }
    
    fun extraireDistancePlants(plantation: String): Int {
        val match = Regex("(\\d+-\\d+|\\d+) cm entre plants").find(plantation)
        return if (match != null) {
            val valeur = match.groupValues[1]
            if (valeur.contains("-")) {
                val parts = valeur.split("-")
                (parts[0].toInt() + parts[1].toInt()) / 2
            } else {
                valeur.toInt()
            }
        } else {
            20
        }
    }
    
    suspend fun calculerNombreLignes(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
        val distanceEntreRangs = extraireDistanceRangs(legume.plantation)
        val largeurSousCase = 33
        return maxOf(1, largeurSousCase / maxOf(distanceEntreRangs, 1))
    }
    
    suspend fun calculerPlantsParLigne(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
        val distanceEntrePlants = extraireDistancePlants(legume.plantation)
        val longueurSousCase = 33
        return maxOf(1, longueurSousCase / maxOf(distanceEntrePlants, 1))
    }
    
    suspend fun calculerTotalPlants(legumeNom: String): Int {
        val nombreLignes = calculerNombreLignes(legumeNom)
        val plantsParLigne = calculerPlantsParLigne(legumeNom)
        return nombreLignes * plantsParLigne
    }
    
    fun estPlanteVolumineuse(nomLegume: String): Boolean {
        val nomBase = if (nomLegume.contains("(")) {
            nomLegume.substringBefore("(").trim()
        } else {
            nomLegume
        }
        return nomBase in listOf(
            "Tomate", "Courgette", "Potiron", "Courge", "Aubergine", 
            "Poivron", "Concombre", "Melon", "Chou pommé", "Brocoli", 
            "Chou-fleur", "Topinambour"
        )
    }
    
    fun getCasesAdjacentes(caseNumero: Int): List<Int> {
        return when (caseNumero) {
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
    }
    
    fun getPlanteDansCase(carre: CarreEntity, caseNumero: Int): String? {
        return when (caseNumero) {
            1 -> carre.case1
            2 -> carre.case2
            3 -> carre.case3
            4 -> carre.case4
            5 -> carre.case5
            6 -> carre.case6
            7 -> carre.case7
            8 -> carre.case8
            9 -> carre.case9
            else -> null
        }
    }
    
    suspend fun peutPlanterDansCase(carre: CarreEntity, caseNumero: Int, legumeNom: String): Boolean {
        if (estPlanteVolumineuse(legumeNom)) {
            val casesAdjacentes = getCasesAdjacentes(caseNumero)
            for (caseAdj in casesAdjacentes) {
                val planteAdj = getPlanteDansCase(carre, caseAdj)
                if (planteAdj != null && estPlanteVolumineuse(planteAdj)) {
                    return false
                }
            }
        }
        return true
    }
    
    suspend fun verifierAssociation(plante1: String, plante2: String): String {
        val legume1 = getLegumeInfo(plante1) ?: return "neutre"
        
        val nomBase2 = if (plante2.contains("(")) {
            plante2.substringBefore("(").trim()
        } else {
            plante2
        }
        
        if (legume1.bonnesAssociations.contains(nomBase2, true)) return "bonne"
        if (legume1.mauvaisesAssociations.contains(nomBase2, true)) return "mauvaise"
        return "neutre"
    }
    
    suspend fun verifierAssociationsAdjacentes(carre: CarreEntity, caseNumero: Int, legumeNom: String): List<Pair<String, String>> {
        val resultats = mutableListOf<Pair<String, String>>()
        val casesAdjacentes = getCasesAdjacentes(caseNumero)
        
        for (caseAdj in casesAdjacentes) {
            val planteAdj = getPlanteDansCase(carre, caseAdj)
            if (planteAdj != null) {
                val association = verifierAssociation(legumeNom, planteAdj)
                if (association != "neutre") {
                    resultats.add(Pair(planteAdj, association))
                }
            }
        }
        
        return resultats
    }
    
    suspend fun getDensiteReelle(legumeNom: String): Int {
        val legume = getLegumeInfo(legumeNom) ?: return 1
        val match = Regex("(\\d+-\\d+|\\d+,\\d+|\\d+) plants/m²").find(legume.plantation)
        return if (match != null) {
            val valeur = match.groupValues[1]
            when {
                valeur.contains(",") -> valeur.replace(",", ".").toDouble().toInt()
                valeur.contains("-") -> { 
                    val parts = valeur.split("-")
                    (parts[0].toInt() + parts[1].toInt()) / 2 
                }
                else -> valeur.toInt()
            }
        } else {
            val distancePlants = extraireDistancePlants(legume.plantation)
            val distanceRangs = extraireDistanceRangs(legume.plantation)
            val surfaceCm2 = 10000
            val surfaceParPlant = distancePlants * distanceRangs
            maxOf(1, surfaceCm2 / maxOf(surfaceParPlant, 1))
        }
    }
    
    private fun getFamilleLegume(legume: String): String {
        val nomBase = if (legume.contains("(")) {
            legume.substringBefore("(").trim()
        } else {
            legume
        }
        
        return when (nomBase) {
            "Tomate", "Poivron", "Aubergine", "Pomme de terre" -> "Solanacées"
            "Chou", "Brocoli", "Chou-fleur", "Radis", "Navet", "Rutabaga", "Chou frisé (Kale)" -> "Brassicacées"
            "Oignon", "Ail", "Poireau", "Ciboulette" -> "Alliacées"
            "Haricot vert", "Petit pois" -> "Légumineuses"
            "Courgette", "Concombre", "Potiron" -> "Cucurbitacées"
            "Carotte", "Panais", "Persil", "Cerfeuil tubéreux", "Coriandre", "Aneth" -> "Apiacées"
            "Épinard", "Betterave" -> "Chénopodiacées"
            "Salade", "Cardon", "Topinambour" -> "Astéracées"
            "Basilic", "Menthe", "Thym", "Romarin" -> "Lamiacées"
            else -> "Autre"
        }
    }
}

/**
 * Résultat d'une plantation (pleine terre).
 */
sealed class ResultatPlantation {
    
    /** Plantation réussie. */
    object Succes : ResultatPlantation()
    
    /** La source demandée n'existe pas dans le stock. */
    data class ErreurSourceIntrouvable(val source: String) : ResultatPlantation()
    
    /** La source existe mais n'est plus active. */
    data class ErreurSourceInactive(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatPlantation()
    
    /** La source existe mais le stock est insuffisant. */
    data class ErreurStockInsuffisant(
        val disponible: Int,
        val demande: Int,
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatPlantation() {
        val manquant: Int
            get() = (demande - disponible).coerceAtLeast(0)
    }
}
