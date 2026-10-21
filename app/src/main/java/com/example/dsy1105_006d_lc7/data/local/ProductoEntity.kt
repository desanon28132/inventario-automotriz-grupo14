package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val codigo: String,
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
    val stockMinimo: Int,

    val descripcion: String = "",
    val marca: String = "",
    val unidadMedida: String = "Unidad",


    val precioCompra: Long = 0L,
    val precioVenta: Long = 0L,

    val estado: String = "Activo",


    val proveedorId: Int? = null,
    val ubicacionId: Int? = null,
    val fechaCambioUbicacion: Long? = null,

    val cantidadReposicion: Int = 0,


    val fechaUltimoIngreso: Long? = null,
    val fechaUltimaSalida: Long? = null
)