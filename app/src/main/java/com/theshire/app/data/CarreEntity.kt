package com.theshire.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carres")
data class CarreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plancheId: Long,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val case1: String? = null,
    val case2: String? = null,
    val case3: String? = null,
    val case4: String? = null,
    val case5: String? = null,
    val case6: String? = null,
    val case7: String? = null,
    val case8: String? = null,
    val case9: String? = null
)
