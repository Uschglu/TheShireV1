package com.theshire.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de données Room de l'application TheShire.
 *
 * Version 13 : ajout des champs de cycle de vie à JeunePlantEntity
 *              (dateLevee, dateRepiquage, dateRempotage, dateEndurcissement,
 *              datePlantation, historiqueEtapes) pour supporter
 *              l'onglet Semis de Jardin.
 *
 * ⚠️ fallbackToDestructiveMigration est activé : à chaque changement de version,
 * les données sont perdues. À désactiver / migrer proprement avant la mise en production.
 */
@Database(
    entities = [
        LegumeEntity::class,
        VarieteEntity::class,
        AdventiceEntity::class,
        PlancheEntity::class,
        CarreEntity::class,
        ContenantEntity::class,
        EmplacementContenantEntity::class,
        RappelEntity::class,
        RappelCulturelEntity::class,
        GraineEntity::class,
        JeunePlantEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ============================================================
    // DAOs
    // ============================================================

    abstract fun legumeDao(): LegumeDao
    abstract fun varieteDao(): VarieteDao
    abstract fun adventiceDao(): AdventiceDao
    abstract fun plancheDao(): PlancheDao
    abstract fun carreDao(): CarreDao
    abstract fun contenantDao(): ContenantDao
    abstract fun emplacementContenantDao(): EmplacementContenantDao
    abstract fun rappelDao(): RappelDao
    abstract fun rappelCulturelDao(): RappelCulturelDao
    abstract fun graineDao(): GraineDao
    abstract fun jeunePlantDao(): JeunePlantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "theshire_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
