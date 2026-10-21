package com.example.dsy1105_006d_lc7.ui.producto

import com.example.dsy1105_006d_lc7.data.local.ProductoEntity

data class ProductoUiState(
    val id: Int = 0,
    val codigo: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val cantidad: String = "",
    val stockMinimo: String = "",

    val descripcion: String = "",
    val marca: String = "",
    val unidadMedida: String = "Unidad",
    val precioCompra: String = "0",
    val precioVenta: String = "0",
    val estado: String = "Activo",
    val cantidadReposicion: String = "0",
    val proveedorId: Int? = null,

    val codigoError: String? = null,
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val cantidadError: String? = null,
    val stockMinimoError: String? = null,
    val descripcionError: String? = null,
    val marcaError: String? = null,
    val unidadMedidaError: String? = null,
    val precioCompraError: String? = null,
    val precioVentaError: String? = null,
    val cantidadReposicionError: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val errorGeneral: String? = null
) {
    val esEdicion: Boolean
        get() = id != 0
}

data class ListaProductosUiState(
    val productos: List<ProductoEntity> = emptyList(),
    val busqueda: String = ""
)