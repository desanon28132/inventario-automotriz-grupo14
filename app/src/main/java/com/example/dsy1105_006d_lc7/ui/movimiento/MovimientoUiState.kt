package com.example.dsy1105_006d_lc7.ui.movimiento
data class MovimientoUiState(
    val productoId: Int? = null,
    val tipo: String = "Ingreso",
    val cantidad: String = "",
    val motivo: String = "",
    val observacion: String = "",
    val esEntrada: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)