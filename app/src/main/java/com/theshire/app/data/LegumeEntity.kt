package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legumes")
data class LegumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val categorie: String,
    val difficulte: String,
    val exposition: String,
    val sol: String,
    val arrosage: String,
    val temperature: String,
    val semis: String,
    val plantation: String,
    val recolte: String,
    val entretien: String,
    val maladies: String,
    val prevention: String,
    val bonnesAssociations: String,
    val mauvaisesAssociations: String,
    val conservation: String
)
