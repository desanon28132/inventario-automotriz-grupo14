package com.example.dsy1105_006d_lc7.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovimientoDao {

    @Insert
    abstract suspend fun insertar(
        movimiento: MovimientoEntity
    )

    @Query("SELECT * FROM productos WHERE id = :productoId")
    abstract suspend fun obtenerProducto(
        productoId: Int
    ): ProductoEntity?

    @Update
    abstract suspend fun actualizarProducto(
        producto: ProductoEntity
    )

    @Query(
        """
        SELECT * FROM movimientos
        ORDER BY fechaHora DESC, id DESC
        """
    )
    abstract fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query(
        """
        SELECT * FROM movimientos
        WHERE productoId = :productoId
        ORDER BY fechaHora DESC, id DESC
        """
    )
    abstract fun observarPorProducto(
        productoId: Int
    ): Flow<List<MovimientoEntity>>

    @Transaction
    open suspend fun registrarMovimiento(
        productoId: Int,
        tipo: String,
        cantidad: Int,
        motivo: String,
        observacion: String,
        usuario: String,
        esEntrada: Boolean = true
    ) {
        require(cantidad > 0) {
            "La cantidad debe ser mayor que cero"
        }

        require(motivo.isNotBlank()) {
            "Debes indicar un motivo"
        }

        require(usuario.isNotBlank()) {
            "No se identificó al usuario"
        }

        val producto = obtenerProducto(productoId)
            ?: throw IllegalArgumentException(
                "El producto ya no existe"
            )

        // En ajustes y devoluciones se indica la dirección.
        val entrada = when (tipo) {
            "Ingreso" -> true
            "Venta", "Consumo" -> false
            "Ajuste", "Devolución" -> esEntrada

            else -> throw IllegalArgumentException(
                "Tipo de movimiento inválido"
            )
        }

        val variacion = if (entrada) cantidad else -cantidad

        // Long evita desbordamientos al sumar cantidades grandes.
        val nuevoStock = producto.cantidad.toLong() +
                variacion.toLong()

        require(nuevoStock >= 0) {
            "Stock insuficiente. Disponible: ${producto.cantidad}"
        }

        require(nuevoStock <= Int.MAX_VALUE.toLong()) {
            "La cantidad supera el límite permitido"
        }

        val fecha = System.currentTimeMillis()

        val productoActualizado = producto.copy(
            cantidad = nuevoStock.toInt(),

            fechaUltimoIngreso = if (entrada) {
                fecha
            } else {
                producto.fechaUltimoIngreso
            },

            fechaUltimaSalida = if (!entrada) {
                fecha
            } else {
                producto.fechaUltimaSalida
            }
        )

        actualizarProducto(productoActualizado)

        insertar(
            MovimientoEntity(
                productoId = producto.id,
                codigoProducto = producto.codigo,
                nombreProducto = producto.nombre,
                tipo = tipo,
                cantidad = cantidad,
                variacion = variacion,
                fechaHora = fecha,
                motivo = motivo.trim(),
                observacion = observacion.trim(),
                stockAnterior = producto.cantidad,
                stockResultante = nuevoStock.toInt(),
                usuario = usuario.trim()
            )
        )
    }
}