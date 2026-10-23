package com.example.dsy1105_006d_lc7.data.local

// Un producto tiene stock bajo cuando la cantidad es menor o igual al mínimo
val ProductoEntity.stockBajo: Boolean
    get() = cantidad <= stockMinimo

// Regla académica de reposición:
// existencia objetivo = el doble del stock mínimo
// cantidad sugerida = existencia objetivo - cantidad disponible (mínimo 1 unidad)
val ProductoEntity.cantidadSugerida: Int
    get() = (stockMinimo * 2 - cantidad).coerceAtLeast(1)
