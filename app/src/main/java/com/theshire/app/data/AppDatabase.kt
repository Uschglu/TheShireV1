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
        PlancheEntity::class,
        CarreEntity::class,
        VarieteEntity::class,
        AdventiceEntity::class,
        RappelEntity::class,
        RappelCulturelEntity::class,
        ContenantEntity::class,
        EmplacementContenantEntity::class,
        GraineEntity::class,
        JeunePlantEntity::class
    ],
    version = 13,                            // ← Version augmentée (12 → 13) pour le cycle de vie des semis
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
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "potager_db"
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
