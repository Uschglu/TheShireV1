package com.theshire.app.data

/**
 * Résultat d'une tentative de plantation.
 * 
 * Utilisé par les repositories (JardinRepository, ContenantRepository)
 * pour signaler :
 *  - Un succès
 *  - Une erreur (source introuvable, source inactive, stock insuffisant)
 * 
 * ⚠️ HISTORIQUE : cette classe était référencée mais non définie dans le
 *    projet (import fantôme). Elle est maintenant officiellement définie
 *    pour permettre à `DialogErreurPlantation` de fonctionner correctement.
 * 
 * ⚠️ NOTE FUTURE : à terme, on pourrait unifier `ResultatPlantation` et
 *    `ResultatCreationCulture` (qui ont la même structure). Pour l'instant,
 *    les deux coexistent pour éviter de casser les appels existants.
 */
sealed class ResultatPlantation {
    
    /**
     * Plantation réussie.
     */
    object Succes : ResultatPlantation()
    
    /**
     * La source de stock n'a pas été trouvée.
     * Ex : aucune graine correspondante dans l'inventaire.
     */
    data class ErreurSourceIntrouvable(
        val source: String
    ) : ResultatPlantation()
    
    /**
     * La source existe mais est inactive/épuisée.
     * Ex : sachet de graines vide, jeune plant déjà planté.
     */
    data class ErreurSourceInactive(
        val legumeNom: String,
        val varieteNom: String?
    ) : ResultatPlantation()
    
    /**
     * Stock insuffisant pour la quantité demandée.
     */
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
