package com.theshire.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LegumeEntity::class,
        PlancheEntity::class,
        CarreEntity::class,
        VarieteEntity::class,
        AdventiceEntity::class,
        RappelEntity::class,
        RappelCulturelEntity::class        // ← 🆕 AJOUT
    ],
    version = 9,                            // ← 🆕 Version augmentée (8 → 9)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun legumeDao(): LegumeDao
    abstract fun plancheDao(): PlancheDao
    abstract fun rappelDao(): RappelDao
    abstract fun rappelCulturelDao(): RappelCulturelDao   // ← 🆕 AJOUT
    
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
