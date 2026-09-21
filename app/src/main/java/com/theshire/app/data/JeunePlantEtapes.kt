package com.theshire.app.data

/**
 * Cycle de vie complet d'un jeune plant, du semis à la plantation.
 *
 * Cet objet remplace l'ancien JeunePlantStades (déprécié puis supprimé).
 * Il définit :
 *  - Les constantes de chaque étape (valeurs stockées dans la colonne `stade`)
 *  - L'ordre chronologique
 *  - Les métadonnées d'affichage (emoji, libellé, description)
 *  - Des helpers pour naviguer entre les étapes
 *
 * Utilisé par :
 *  - JeunePlantEntity (valeur par défaut)
 *  - OngletPlants (Stocks) : affichage des plants
 *  - OngletSemis (Jardin) : workflow des semis en cours
 *  - FicheSemis : avancement d'étape
 *  - DialogAjoutPlant / DialogAjoutSemis : choix du stade initial
 */
object JeunePlantEtapes {

    // ============================================================
    // CONSTANTES DES ÉTAPES (valeurs stockées en base)
    // ============================================================

    /** Étape 0 : graine en terre, en attente de levée. */
    const val SEMIS = "Semis"

    /** Étape 1 : la graine a germé, premières feuilles visibles. */
    const val LEVEE = "Levée"

    /** Étape 2 : premier repiquage en godet plus grand. */
    const val REPIQUE = "Repiqué"

    /** Étape 3 : changement de contenant (pot plus grand). */
    const val REMPOTE = "Rempoté"

    /** Étape 4 : plant assez développé, prêt pour le jardin. */
    const val PRET_A_PLANTER = "Prêt à planter"

    /** Étape 5 : habitué progressivement au froid et au vent. */
    const val ENDURCI = "Endurci"

    /** Étape 6 : planté (fin du cycle de suivi actif). */
    const val PLANTE = "Planté"

    // ============================================================
    // LISTE ORDONNÉE DES ÉTAPES
    // ============================================================

    /**
     * Toutes les étapes dans l'ordre chronologique.
     */
    val TOUS = listOf(
        SEMIS,
        LEVEE,
        REPIQUE,
        REMPOTE,
        PRET_A_PLANTER,
        ENDURCI,
        PLANTE
    )

    /**
     * Étapes du cycle actif (hors "Planté").
     */
    val ACTIVES = listOf(
        SEMIS,
        LEVEE,
        REPIQUE,
        REMPOTE,
        PRET_A_PLANTER,
        ENDURCI
    )

    // ============================================================
    // MÉTADONNÉES D'AFFICHAGE
    // ============================================================

    /** Emoji associé à une étape (pour l'UI). */
    fun emoji(etape: String): String = when (etape) {
        SEMIS -> "🌰"
        LEVEE -> "🌱"
        REPIQUE -> "🌿"
        REMPOTE -> "🪴"
        PRET_A_PLANTER -> "🌳"
        ENDURCI -> "🌲"
        PLANTE -> "🎉"
        else -> "🌱"
    }

    /** Libellé lisible (identique à la valeur stockée, mais centralisé). */
    fun libelle(etape: String): String = etape

    /** Description pédagogique d'une étape. */
    fun description(etape: String): String = when (etape) {
        SEMIS -> "Graine en terre, en attente de levée."
        LEVEE -> "La graine a germé, les premières feuilles apparaissent."
        REPIQUE -> "Premier repiquage en godet plus grand."
        REMPOTE -> "Changé dans un contenant plus grand."
        PRET_A_PLANTER -> "Assez développé, prêt pour le jardin."
        ENDURCI -> "Habitué au froid et au vent, peut sortir définitivement."
        PLANTE -> "A quitté le suivi actif — fin du cycle."
        else -> ""
    }

    // ============================================================
    // HELPERS DE NAVIGATION
    // ============================================================

    /** Index d'une étape dans le cycle (0-based). -1 si inconnue. */
    fun indexDe(etape: String): Int = TOUS.indexOf(etape)

    /**
     * Étape suivante dans le cycle, ou null si on est déjà à la fin
     * ("Planté") ou si l'étape est inconnue.
     */
    fun etapeSuivante(etape: String): String? {
        val idx = indexDe(etape)
        if (idx < 0 || idx >= TOUS.size - 1) return null
        return TOUS[idx + 1]
    }

    /**
     * Étape précédente dans le cycle, ou null si on est au tout début
     * ("Semis") ou si l'étape est inconnue.
     */
    fun etapePrecedente(etape: String): String? {
        val idx = indexDe(etape)
        if (idx <= 0) return null
        return TOUS[idx - 1]
    }

    /** Est-ce que c'est l'étape finale du cycle ? */
    fun estEtapeFinale(etape: String): Boolean = etape == PLANTE

    /**
     * Progression relative (0.0 à 1.0) dans le cycle actif.
     */
    fun progression(etape: String): Float {
        val idx = indexDe(etape)
        if (idx < 0) return 0f
        if (idx >= ACTIVES.size - 1) return 1f
        return idx.toFloat() / (ACTIVES.size - 1).toFloat()
    }
}
