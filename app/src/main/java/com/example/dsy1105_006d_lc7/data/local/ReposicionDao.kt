package com.example.dsy1105_006d_lc7.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReposicionDao {

    @Query(
        """
        SELECT * FROM productos
        WHERE estado = 'Activo'
          AND (cantidad < stockMinimo OR cantidad = 0)
        ORDER BY nombre ASC
        """
    )
    abstract fun observarPendientes(): Flow<List<ProductoEntity>>

    @Query(
        """
        SELECT * FROM productos
        WHERE proveedorId = :proveedorId
          AND estado = 'Activo'
          AND (cantidad < stockMinimo OR cantidad = 0)
        ORDER BY nombre ASC
        """
    )
    abstract suspend fun obtenerPendientes(
        proveedorId: Int
    ): List<ProductoEntity>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    abstract suspend fun obtenerProveedor(id: Int): ProveedorEntity?

    @Query(
        """
        SELECT * FROM solicitudes_reposicion
        ORDER BY fechaCreacion DESC, id DESC
        """
    )
    abstract fun observarSolicitudes():
            Flow<List<SolicitudReposicionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM solicitudes_reposicion
        WHERE proveedorId = :proveedorId
          AND estado != 'Atendida'
        """
    )
    abstract suspend fun contarAbiertas(proveedorId: Int): Int

    @Query(
        """
        SELECT * FROM solicitudes_reposicion
        WHERE id = :id
        """
    )
    abstract suspend fun obtenerSolicitud(
        id: Int
    ): SolicitudReposicionEntity?

    @Insert
    abstract suspend fun insertar(
        solicitud: SolicitudReposicionEntity
    )

    @Query(
        """
        UPDATE solicitudes_reposicion
        SET estado = :estado,
            fechaActualizacion = :fecha
        WHERE id = :id
        """
    )
    abstract suspend fun actualizarEstado(
        id: Int,
        estado: String,
        fecha: Long
    )

    @Transaction
    open suspend fun crearSolicitud(proveedorId: Int) {
        val proveedor = obtenerProveedor(proveedorId)
            ?: throw IllegalArgumentException(
                "El proveedor ya no existe"
            )

        require(proveedor.activo) {
            "El proveedor está inactivo"
        }

        require(contarAbiertas(proveedorId) == 0) {
            "Este proveedor ya tiene una solicitud abierta"
        }

        val productos = obtenerPendientes(proveedorId)

        require(productos.isNotEmpty()) {
            "Ya no hay productos pendientes para este proveedor"
        }

        val detalle = productos.joinToString("\n\n") { producto ->
            val sugerida = calcularReposicion(producto)

            "${producto.codigo} - ${producto.nombre}\n" +
                    "Disponible: ${producto.cantidad}\n" +
                    "Mínimo: ${producto.stockMinimo}\n" +
                    "Comprar: $sugerida ${producto.unidadMedida}"
        }

        val ahora = System.currentTimeMillis()

        insertar(
            SolicitudReposicionEntity(
                proveedorId = proveedor.id,
                proveedorNombre = proveedor.nombre,
                proveedorCorreo = proveedor.correo,
                detalle = detalle,
                fechaCreacion = ahora,
                fechaActualizacion = ahora
            )
        )
    }

    @Transaction
    open suspend fun avanzarEstado(id: Int) {
        val solicitud = obtenerSolicitud(id)
            ?: throw IllegalArgumentException(
                "La solicitud ya no existe"
            )

        val siguiente = when (solicitud.estado) {
            "Pendiente" -> "Preparada"
            "Preparada" -> "Enviada"
            "Enviada" -> "Atendida"

            else -> throw IllegalArgumentException(
                "La solicitud ya está atendida"
            )
        }

        actualizarEstado(
            id = id,
            estado = siguiente,
            fecha = System.currentTimeMillis()
        )
    }
}

// Regla académica provisional compartida por DAO y pantalla.
fun calcularReposicion(producto: ProductoEntity): Int {
    val objetivo = (producto.stockMinimo.toLong() * 2L)
        .coerceAtLeast(1L)
        .coerceAtMost(Int.MAX_VALUE.toLong())

    return (objetivo - producto.cantidad.toLong())
        .coerceAtLeast(0L)
        .toInt()
}