package com.theshire.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de données Room de l'application TheShire.
 *
 * Version 13 : ajout des champs de cycle de vie à JeunePlantEntity.
 * Version 14 : ajout du champ estProjection à JeunePlantEntity
 *              pour séparer les semis projetés des semis réels.
 * Version 15 : ajout du champ categorie à JeunePlantEntity
 *              ("Semis" / "JeunePlant") pour distinguer les semis en cours
 *              des jeunes plants promus (ou achetés).
 * Version 16 : ajout de CultureEntity (cultures en pleine terre et en urbain)
 *              et de RecolteEntity (récoltes en kg, 4e onglet de Stocks).
 * Version 17 : ajout du champ estProjection à RecolteEntity
 *              pour filtrer les récoltes par mode (projection/réel).
 *              Toutes les récoltes existantes sont marquées "projetées"
 *              (estProjection = 1) par défaut.
 * Version 18 : ajout de EtageEntity (étages des tours empilables).
 *              Chaque étage d'une tour est indépendant (emplacements,
 *              cultures, notes).
 */
@Database(
    entities = [
        LegumeEntity::class,
        PlancheEntity::class,
        CarreEntity::class,
        VarieteEntity::class,
        AdventiceEntity::class,
        RappelEntity::class,
        RappelCulturelEntity::class,
        ContenantEntity::class,
        EmplacementContenantEntity::class,
        GraineEntity::class,
        JeunePlantEntity::class,
        CultureEntity::class,
        RecolteEntity::class,
        EtageEntity::class
    ],
    version = 18,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun legumeDao(): LegumeDao
    abstract fun plancheDao(): PlancheDao
    abstract fun rappelDao(): RappelDao
    abstract fun rappelCulturelDao(): RappelCulturelDao
    abstract fun contenantDao(): ContenantDao
    abstract fun graineDao(): GraineDao
    abstract fun jeunePlantDao(): JeunePlantDao
    abstract fun cultureDao(): CultureDao
    abstract fun recolteDao(): RecolteDao
    abstract fun etageDao(): EtageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Migration v16 → v17.
         * 
         * Ajoute la colonne `estProjection` à la table `recoltes`.
         * Valeur par défaut : 1 (true) → toutes les récoltes existantes
         * sont considérées comme "projetées" (cohérent avec le mode par défaut).
         * 
         * Ajoute aussi l'index correspondant pour accélérer les requêtes filtrées.
         */
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ajout de la colonne estProjection avec valeur par défaut 1 (true)
                db.execSQL(
                    "ALTER TABLE recoltes ADD COLUMN estProjection INTEGER NOT NULL DEFAULT 1"
                )
                // Index pour accélérer les requêtes filtrées par mode
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recoltes_estProjection ON recoltes(estProjection)"
                )
            }
        }
        
        /**
         * Migration v17 → v18.
         * 
         * Crée la table `etages` pour les étages des tours empilables.
         * 
         * Structure :
         *  - id (PK auto)
         *  - contenantId (FK vers contenants, CASCADE)
         *  - numero (ordre dans la tour)
         *  - forme ("rond" par défaut)
         *  - nombreEmplacements (0 = non initialisé)
         *  - notes (vide par défaut)
         *  - dateCreation
         * 
         * Index :
         *  - Index sur contenantId (pour accès rapide aux étages d'une tour)
         *  - Index unique sur (contenantId, numero) (un seul étage par position)
         */
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Création de la table etages
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `etages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `contenantId` INTEGER NOT NULL,
                        `numero` INTEGER NOT NULL,
                        `forme` TEXT NOT NULL DEFAULT 'rond',
                        `nombreEmplacements` INTEGER NOT NULL DEFAULT 0,
                        `notes` TEXT NOT NULL DEFAULT '',
                        `dateCreation` INTEGER NOT NULL,
                        FOREIGN KEY(`contenantId`) REFERENCES `contenants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                // Index sur contenantId
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_etages_contenantId` ON `etages` (`contenantId`)"
                )
                // Index unique sur (contenantId, numero)
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_etages_contenantId_numero` ON `etages` (`contenantId`, `numero`)"
                )
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "potager_db"
                )
                .addMigrations(MIGRATION_16_17, MIGRATION_17_18)
                // ⚠️ On garde fallbackToDestructiveMigration en sécurité,
                //    mais les migrations explicites sont prioritaires.
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
