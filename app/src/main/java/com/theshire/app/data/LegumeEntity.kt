package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legumes")
data class LegumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val categorie: String,
    val conseils: String
)
