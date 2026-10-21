package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    indices = [Index(value = ["productoId"])]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val productoId: Int,

    // Conservamos estos datos para consultar el historial.
    val codigoProducto: String,
    val nombreProducto: String,

    val tipo: String,
    val cantidad: Int,

    // Positivo: entraron unidades. Negativo: salieron.
    val variacion: Int,

    val fechaHora: Long,
    val motivo: String,
    val observacion: String = "",

    val stockAnterior: Int,
    val stockResultante: Int,

    val usuario: String
)