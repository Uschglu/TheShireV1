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
 *              Toutes les récoltes existantes sont marquées "projetées".
 * Version 18 : ajout de EtageEntity (étages des tours empilables).
 *              Chaque étage d'une tour est indépendant.
 * Version 19 : ajout du champ etageId à EmplacementContenantEntity
 *              pour rattacher les emplacements à un étage de tour.
 *              Les emplacements existants restent à etageId = null
 *              (contenants non-tour).
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
    version = 19,
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
         * sont considérées comme "projetées".
         */
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recoltes ADD COLUMN estProjection INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recoltes_estProjection ON recoltes(estProjection)"
                )
            }
        }
        
        /**
         * Migration v17 → v18.
         * 
         * Crée la table `etages` pour les étages des tours empilables.
         */
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_etages_contenantId` ON `etages` (`contenantId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_etages_contenantId_numero` ON `etages` (`contenantId`, `numero`)"
                )
            }
        }
        
        /**
         * Migration v18 → v19.
         * 
         * Ajoute la colonne `etageId` à `emplacements_contenants` pour
         * rattacher un emplacement à un étage d'une tour.
         * 
         * Colonne nullable, valeur par défaut NULL (les emplacements
         * existants restent sur des contenants non-tour).
         * 
         * Ajoute aussi l'index correspondant.
         */
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE emplacements_contenants ADD COLUMN etageId INTEGER DEFAULT NULL"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_emplacements_contenants_etageId " +
                        "ON emplacements_contenants(etageId)"
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
                .addMigrations(MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19)
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
