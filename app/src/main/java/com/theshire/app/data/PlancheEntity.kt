package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planches")
data class PlancheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val largeur: Int = 1,
    val longueur: Int = 1,
    val dateCreation: Long = System.currentTimeMillis()
)
