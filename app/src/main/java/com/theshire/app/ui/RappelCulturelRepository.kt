package com.theshire.app.ui

import android.content.Context
import com.theshire.app.data.AppDatabase
import com.theshire.app.data.RappelCulturelEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Repository pour les rappels culturaux automatiques.
 * 
 * Génère automatiquement les rappels (tuteurage, buttage, éclaircissage...)
 * quand on plante un légume dans un carré du jardin.
 * 
 * Fournit aussi les requêtes pour l'affichage dans le calendrier
 * (rappels entre deux dates, rappels en retard, rappels à notifier).
 */
class RappelCulturelRepository(context: Context) {
    
    private val rappelCulturelDao = AppDatabase.getDatabase(context).rappelCulturelDao()
    
    // Constante : fenêtre flexible de ±2 jours autour de la date idéale
    private val FENETRE_JOURS = 2L
    private val MILLIS_PAR_JOUR = 24L * 60L * 60L * 1000L
    
    // ============================================================
    // GÉNÉRATION AUTOMATIQUE
    // ============================================================
    
    /**
     * Génère automatiquement les rappels culturaux pour une plantation.
     * 
     * @param legumeNom Nom du légume (ex: "Tomate" ou "Tomate (Marmande)")
     * @param datePlantation Timestamp de la plantation (millis)
     * @param carreId ID du carré où la plante est installée
     * @param caseNumero Numéro de la case (1-9) dans le carré
     * @param plancheId ID de la planche (pour retrouver le contexte)
     */
    suspend fun genererRappelsPourPlantation(
        legumeNom: String,
        datePlantation: Long,
        carreId: Long,
        caseNumero: Int,
        plancheId: Long
    ) {
        // 1. Supprimer les anciens rappels de cette case (au cas où on remplace une plante)
        rappelCulturelDao.deleteRappelsPourCase(carreId, caseNumero)
        
        // 2. Récupérer les opérations culturales du légume
        val operations = OperationsCulturales.getOperationsPourLegume(legumeNom)
        if (operations.isEmpty()) return
        
        // 3. Créer un rappel pour chaque opération
        val rappels = operations.map { operation ->
            // Calculer la date idéale
            val dateIdeale = datePlantation + (operation.jourDebut * MILLIS_PAR_JOUR)
            
            // Calculer la fenêtre ±FENETRE_JOURS
            val dateDebut = dateIdeale - (FENETRE_JOURS * MILLIS_PAR_JOUR)
            val dateFin = if (operation.jourFin > operation.jourDebut) {
                // Opération sur plusieurs jours : dateFin = dateDebut + durée
                datePlantation + (operation.jourFin * MILLIS_PAR_JOUR) + (FENETRE_JOURS * MILLIS_PAR_JOUR)
            } else {
                // Opération ponctuelle : juste ±2 jours
                dateIdeale + (FENETRE_JOURS * MILLIS_PAR_JOUR)
            }
            
            RappelCulturelEntity(
                legumeNom = legumeNom,
                typeOperation = operation.nom,
                emoji = operation.emoji,
                description = operation.description,
                conseil = operation.conseil,
                couleurHex = operation.couleurHex,
                dateIdeale = dateIdeale,
                dateDebut = dateDebut,
                dateFin = dateFin,
                carreId = carreId,
                caseNumero = caseNumero,
                plancheId = plancheId,
                estActif = true,
                estTermine = false
            )
        }
        
        // 4. Insérer tous les rappels d'un coup
        rappelCulturelDao.insertRappels(rappels)
    }
    
    /**
     * Supprime les rappels culturaux pour une case donnée.
     * Appelé quand on vide une case.
     */
    suspend fun supprimerRappelsPourCase(carreId: Long, caseNumero: Int) {
        rappelCulturelDao.deleteRappelsPourCase(carreId, caseNumero)
    }
    
    /**
     * Supprime les rappels culturaux pour un carré entier.
     * Appelé quand on supprime un carré.
     */
    suspend fun supprimerRappelsPourCarre(carreId: Long) {
        rappelCulturelDao.deleteRappelsPourCarre(carreId)
    }
    
    /**
     * Supprime les rappels culturaux pour une planche entière.
     * Appelé quand on supprime une planche.
     */
    suspend fun supprimerRappelsPourPlanche(plancheId: Long) {
        rappelCulturelDao.deleteRappelsPourPlanche(plancheId)
    }
    
    /**
     * Désactive les rappels pour une case (les marque comme inactifs sans les supprimer).
     * Utile pour garder un historique.
     */
    suspend fun desactiverRappelsPourCase(carreId: Long, caseNumero: Int) {
        val rappels = rappelCulturelDao.getRappelsPourCase(carreId, caseNumero)
        rappels.forEach { rappel ->
            rappelCulturelDao.updateRappel(rappel.copy(estActif = false))
        }
    }
    
    // ============================================================
    // ACCÈS POUR LE CALENDRIER
    // ============================================================
    
    /**
     * Récupère tous les rappels actifs (Flow pour observation).
     */
    fun getRappelsActifs(): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsActifs()
    }
    
    /**
     * Récupère les rappels dont la période chevauche une plage de dates.
     * Utilisé pour afficher les barres ←→ sur le calendrier mensuel.
     * 
     * @param dateDebut Début de la plage (premier jour du mois affiché)
     * @param dateFin Fin de la plage (dernier jour du mois affiché + quelques jours)
     */
    fun getRappelsEntreDatesFlow(dateDebut: Long, dateFin: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsEntreDatesFlow(dateDebut, dateFin)
    }
    
    /**
     * Version suspend (pour usage ponctuel hors Flow).
     */
    suspend fun getRappelsEntreDates(dateDebut: Long, dateFin: Long): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsEntreDates(dateDebut, dateFin)
    }
    
    /**
     * Récupère les rappels en retard (période finie, non terminés).
     */
    suspend fun getRappelsEnRetard(): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsEnRetard(System.currentTimeMillis())
    }
    
    /**
     * Récupère les rappels d'un carré donné.
     */
    fun getRappelsPourCarre(carreId: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourCarre(carreId)
    }
    
    /**
     * Récupère les rappels d'une planche donnée.
     */
    fun getRappelsPourPlanche(plancheId: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourPlanche(plancheId)
    }
    
    /**
     * Récupère les rappels d'un légume donné (pour la fiche détaillée).
     */
    fun getRappelsPourLegume(legumeNom: String): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourLegume(legumeNom)
    }
    
    // ============================================================
    // GESTION DU STATUT
    // ============================================================
    
    /**
     * Marque un rappel comme terminé.
     */
    suspend fun marquerTermine(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(
            rappel.copy(
                estTermine = true,
                dateRealisation = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Marque un rappel comme non terminé (annule l'action "terminé").
     */
    suspend fun marquerNonTermine(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(
            rappel.copy(
                estTermine = false,
                dateRealisation = null
            )
        )
    }
    
    /**
     * Reporte un rappel de N jours (décale toutes les dates).
     */
    suspend fun reporterRappel(rappelId: Long, joursDeReport: Int) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        val decalage = joursDeReport * MILLIS_PAR_JOUR
        rappelCulturelDao.updateRappel(
            rappel.copy(
                dateIdeale = rappel.dateIdeale + decalage,
                dateDebut = rappel.dateDebut + decalage,
                dateFin = rappel.dateFin + decalage
            )
        )
    }
    
    /**
     * Supprime un rappel individuel.
     */
    suspend fun supprimerRappel(rappelId: Long) {
        rappelCulturelDao.deleteRappelParId(rappelId)
    }
    
    /**
     * Récupère un rappel par son ID.
     */
    suspend fun getRappelParId(rappelId: Long): RappelCulturelEntity? {
        return rappelCulturelDao.getRappelParId(rappelId)
    }
    
    // ============================================================
    // NOTIFICATIONS
    // ============================================================
    
    /**
     * Récupère les rappels à notifier (actifs, non terminés, notif non envoyée,
     * et dont la période est en cours).
     */
    suspend fun getRappelsANotifier(): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsANotifier(System.currentTimeMillis())
    }
    
    /**
     * Marque un rappel comme "notification envoyée".
     */
    suspend fun marquerNotificationEnvoyee(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(rappel.copy(notificationEnvoyee = true))
    }
    
    // ============================================================
    // UTILITAIRES
    // ============================================================
    
    /**
     * Compte le nombre de rappels en cours (badge éventuel).
     */
    fun countRappelsEnCours(): Flow<Int> {
        return rappelCulturelDao.countRappelsEnCours()
    }
    
    /**
     * Convertit un timestamp en début de journée (00:00:00.000).
     * Utile pour comparer des dates dans le calendrier.
     */
    fun debutDeJournee(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    /**
     * Vérifie si un rappel est "en retard" (> dateFin).
     */
    fun estEnRetard(rappel: RappelCulturelEntity): Boolean {
        return !rappel.estTermine && rappel.dateFin < System.currentTimeMillis()
    }
    
    /**
     * Vérifie si un rappel est "en cours" (période actuelle).
     */
    fun estEnCours(rappel: RappelCulturelEntity): Boolean {
        val now = System.currentTimeMillis()
        return !rappel.estTermine && now >= rappel.dateDebut && now <= rappel.dateFin
    }
    
    /**
     * Vérifie si un rappel "approche" (dans la fenêtre mais pas encore commencé).
     */
    fun estProche(rappel: RappelCulturelEntity): Boolean {
        val now = System.currentTimeMillis()
        return !rappel.estTermine && now < rappel.dateDebut
    }
}
