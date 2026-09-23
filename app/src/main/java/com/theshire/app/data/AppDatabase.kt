package com.theshire.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
 *
 * ⚠️ fallbackToDestructiveMigration est activé : les données sont perdues
 * à chaque changement de version. À désactiver / migrer proprement avant prod.
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
        RecolteEntity::class
    ],
    version = 16,
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
