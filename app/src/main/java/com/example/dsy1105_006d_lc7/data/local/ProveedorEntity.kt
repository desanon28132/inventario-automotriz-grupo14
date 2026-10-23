package com.example.dsy1105_006d_lc7.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "proveedores",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class ProveedorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val codigo: String,
    val nombre: String,
    val correo: String,
    val activo: Boolean = true,

    @ColumnInfo(defaultValue = "''")
    val observaciones: String = "",

    @ColumnInfo(defaultValue = "''")
    val condicionesCompra: String = ""
)