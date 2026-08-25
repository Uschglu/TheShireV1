package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adventices")
data class AdventiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val nomScientifique: String,
    val description: String,
    val indicationSol: String,
    val typeSol: String,
    val emoji: String
)
