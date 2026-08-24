package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "varietes")
data class VarieteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val legumeParent: String,
    val nom: String,
    val description: String,
    val semis: String,
    val plantation: String,
    val recolte: String,
    val entretien: String,
    val particularites: String
)
