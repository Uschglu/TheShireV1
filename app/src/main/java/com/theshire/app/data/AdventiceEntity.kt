package com.theshire.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "adventices",
    indices = [Index(value = ["nom"], unique = true)]
)
data class AdventiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val nomScientifique: String,
    val description: String,
    val indicationSol: String,
    val typeSol: String,
    val emoji: String,
    val imageUrl: String = ""
)
