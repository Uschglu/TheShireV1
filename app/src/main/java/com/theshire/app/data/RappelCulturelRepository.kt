package com.theshire.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository pour les rappels culturaux automatiques.
 * 
 * Génère automatiquement les rappels (tuteurage, buttage, éclaircissage...)
 * quand on plante un légume :
 * - Dans un CARRÉ de pleine terre (planche)
 * - Dans un EMPLACEMENT de contenant urbain
 * 
 * Fournit aussi les requêtes pour l'affichage dans le calendrier.
 */
class RappelCulturelRepository(context: Context) {
    
    private val rappelCulturelDao = AppDatabase.getDatabase(context).rappelCulturelDao()
    
    private val FENETRE_JOURS = 2L
    private val MILLIS_PAR_JOUR = 24L * 60L * 60L * 1000L
    
    // ============================================================
    // GÉNÉRATION AUTOMATIQUE
    // ============================================================
    
    suspend fun genererRappelsPourPlantation(
        legumeNom: String,
        datePlantation: Long,
        carreId: Long? = null,
        caseNumero: Int? = null,
        plancheId: Long? = null,
        contenantId: Long? = null,
        emplacementNumero: Int? = null
    ) {
        // 1. Déterminer le contexte
        val urbain = contenantId != null && emplacementNumero != null
        
        // 2. Supprimer les anciens rappels de cet emplacement
        if (urbain) {
            rappelCulturelDao.deleteRappelsPourEmplacement(contenantId!!, emplacementNumero!!)
        } else if (carreId != null && caseNumero != null) {
            rappelCulturelDao.deleteRappelsPourCase(carreId, caseNumero)
        }
        
        // 3. Récupérer les opérations culturelles FILTRÉES par contexte
        val operations = OperationsCulturales.getOperationsPourContexte(legumeNom, urbain)
        if (operations.isEmpty()) return
        
        // 4. Créer un rappel pour chaque opération
        val rappels = operations.map { operation ->
            val dateIdeale = datePlantation + (operation.jourDebut * MILLIS_PAR_JOUR)
            
            val dateDebut = dateIdeale - (FENETRE_JOURS * MILLIS_PAR_JOUR)
            val dateFin = if (operation.jourFin > operation.jourDebut) {
                datePlantation + (operation.jourFin * MILLIS_PAR_JOUR) + (FENETRE_JOURS * MILLIS_PAR_JOUR)
            } else {
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
                contenantId = contenantId,
                emplacementNumero = emplacementNumero,
                estActif = true,
                estTermine = false
            )
        }
        
        // 5. Insérer tous les rappels d'un coup
        rappelCulturelDao.insertRappels(rappels)
    }
    
    // ============================================================
    // SUPPRESSION
    // ============================================================
    
    suspend fun supprimerRappelsPourCase(carreId: Long, caseNumero: Int) {
        rappelCulturelDao.deleteRappelsPourCase(carreId, caseNumero)
    }
    
    suspend fun supprimerRappelsPourCarre(carreId: Long) {
        rappelCulturelDao.deleteRappelsPourCarre(carreId)
    }
    
    suspend fun supprimerRappelsPourPlanche(plancheId: Long) {
        rappelCulturelDao.deleteRappelsPourPlanche(plancheId)
    }
    
    suspend fun supprimerRappelsPourEmplacement(contenantId: Long, emplacementNumero: Int) {
        rappelCulturelDao.deleteRappelsPourEmplacement(contenantId, emplacementNumero)
    }
    
    suspend fun supprimerRappelsPourContenant(contenantId: Long) {
        rappelCulturelDao.deleteRappelsPourContenant(contenantId)
    }
    
    // ============================================================
    // ACCÈS POUR LE CALENDRIER
    // ============================================================
    
    fun getRappelsActifs(): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsActifs()
    }
    
    fun getRappelsEntreDatesFlow(dateDebut: Long, dateFin: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsEntreDatesFlow(dateDebut, dateFin)
    }
    
    suspend fun getRappelsEntreDates(dateDebut: Long, dateFin: Long): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsEntreDates(dateDebut, dateFin)
    }
    
    suspend fun getRappelsEnRetard(): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsEnRetard(System.currentTimeMillis())
    }
    
    fun getRappelsPourCarre(carreId: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourCarre(carreId)
    }
    
    fun getRappelsPourPlanche(plancheId: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourPlanche(plancheId)
    }
    
    fun getRappelsPourContenant(contenantId: Long): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourContenant(contenantId)
    }
    
    fun getRappelsPourLegume(legumeNom: String): Flow<List<RappelCulturelEntity>> {
        return rappelCulturelDao.getRappelsPourLegume(legumeNom)
    }
    
    // ============================================================
    // GESTION DU STATUT
    // ============================================================
    
    suspend fun marquerTermine(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(
            rappel.copy(
                estTermine = true,
                dateRealisation = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun marquerNonTermine(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(
            rappel.copy(
                estTermine = false,
                dateRealisation = null
            )
        )
    }
    
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
    
    suspend fun supprimerRappel(rappelId: Long) {
        rappelCulturelDao.deleteRappelParId(rappelId)
    }
    
    suspend fun getRappelParId(rappelId: Long): RappelCulturelEntity? {
        return rappelCulturelDao.getRappelParId(rappelId)
    }
    
    // ============================================================
    // NOTIFICATIONS
    // ============================================================
    
    suspend fun getRappelsANotifier(): List<RappelCulturelEntity> {
        return rappelCulturelDao.getRappelsANotifier(System.currentTimeMillis())
    }
    
    suspend fun marquerNotificationEnvoyee(rappelId: Long) {
        val rappel = rappelCulturelDao.getRappelParId(rappelId) ?: return
        rappelCulturelDao.updateRappel(rappel.copy(notificationEnvoyee = true))
    }
    
    // ============================================================
    // UTILITAIRES
    // ============================================================
    
    fun countRappelsEnCours(): Flow<Int> {
        return rappelCulturelDao.countRappelsEnCours()
    }
    
    fun debutDeJournee(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    fun estEnRetard(rappel: RappelCulturelEntity): Boolean {
        return !rappel.estTermine && rappel.dateFin < System.currentTimeMillis()
    }
    
    fun estEnCours(rappel: RappelCulturelEntity): Boolean {
        val now = System.currentTimeMillis()
        return !rappel.estTermine && now >= rappel.dateDebut && now <= rappel.dateFin
    }
    
    fun estProche(rappel: RappelCulturelEntity): Boolean {
        val now = System.currentTimeMillis()
        return !rappel.estTermine && now < rappel.dateDebut
    }
}
