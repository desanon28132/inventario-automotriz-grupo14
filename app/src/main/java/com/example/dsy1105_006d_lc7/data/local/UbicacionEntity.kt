package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ubicaciones",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class UbicacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val codigo: String,
    val zona: String,
    val estante: String,
    val repisa: String,
    val posicion: String,

    // Fecha de creación o última edición de esta ubicación.
    val fechaActualizacion: Long
)