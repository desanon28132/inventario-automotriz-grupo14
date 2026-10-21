package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes_reposicion")
data class SolicitudReposicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val proveedorId: Int,
    val proveedorNombre: String,
    val proveedorCorreo: String,

    val detalle: String,

    val estado: String = "Pendiente",
    val fechaCreacion: Long,
    val fechaActualizacion: Long
)
