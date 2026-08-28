package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rappels")
data class RappelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,           // Date du rappel en millisecondes
    val titre: String,             // Titre du rappel
    val note: String = "",         // Note optionnelle
    val estActif: Boolean = true   // Cloche activée ou non
)
