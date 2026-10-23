
package com.example.dsy1105_006d_lc7.ui.producto

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.cantidadSugerida
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class BorradorCorreo(
    val destinatario: String,
    val asunto: String,
    val cuerpo: String
)

class ProductoViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val repository = ProductoRepository(
        database.productoDao()
    )

    private val proveedorDao = database.proveedorDao()
    private val ubicacionDao = database.ubicacionDao()

    val proveedores = proveedorDao.observarTodos()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val ubicaciones = ubicacionDao.observarTodas()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // ESTADO DEL FORMULARIO

    private val _form = MutableStateFlow(ProductoUiState())
    val form: StateFlow<ProductoUiState> = _form.asStateFlow()

    // MENSAJES

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    // FILTROS DE PRODUCTOS

    private val _busqueda = MutableStateFlow("")
    private val _soloStockBajo = MutableStateFlow(false)
    private val _categoriaSeleccionada =
        MutableStateFlow<String?>(null)

    val lista: StateFlow<ListaProductosUiState> = combine(
        repository.observarTodos(),
        _busqueda,
        _soloStockBajo,
        _categoriaSeleccionada
    ) { productos, texto, soloBajo, seleccion ->

        val categorias = productos
            .map { it.categoria.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

        val categoriaActual = seleccion?.let { categoria ->
            categorias.firstOrNull {
                it.equals(categoria, ignoreCase = true)
            }
        }

        val consulta = texto.trim()

        val filtrados = productos.filter { producto ->

            val coincideBusqueda =
                consulta.isBlank() ||
                        producto.nombre.contains(
                            consulta,
                            ignoreCase = true
                        ) ||
                        producto.codigo.contains(
                            consulta,
                            ignoreCase = true
                        )

            val coincideCategoria =
                categoriaActual == null ||
                        producto.categoria.trim().equals(
                            categoriaActual,
                            ignoreCase = true
                        )

            val coincideStock =
                !soloBajo || producto.stockBajo

            coincideBusqueda &&
                    coincideCategoria &&
                    coincideStock
        }

        ListaProductosUiState(
            productos = filtrados,
            busqueda = texto,
            soloStockBajo = soloBajo,
            total = productos.size,
            stockBajo = productos.count { it.stockBajo },
            categorias = categorias,
            categoriaSeleccionada = categoriaActual
        )

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ListaProductosUiState()
    )

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }

    fun onSoloStockBajoChange(valor: Boolean) {
        _soloStockBajo.value = valor
    }

    fun onFiltroCategoriaChange(categoria: String?) {
        _categoriaSeleccionada.value = categoria
    }

    fun onCategoriaSeleccionada(categoria: String?) {
        _categoriaSeleccionada.value = categoria
    }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _soloStockBajo.value = false
        _categoriaSeleccionada.value = null
    }

    // CAMPOS DEL FORMULARIO

    fun onCodigoChange(v: String) =
        _form.update {
            it.copy(codigo = v, codigoError = null)
        }

    fun onNombreChange(v: String) =
        _form.update {
            it.copy(nombre = v, nombreError = null)
        }

    fun onCategoriaChange(v: String) =
        _form.update {
            it.copy(categoria = v, categoriaError = null)
        }

    fun onCantidadChange(v: String) =
        _form.update {
            it.copy(cantidad = v, cantidadError = null)
        }

    fun onStockMinimoChange(v: String) =
        _form.update {
            it.copy(stockMinimo = v, stockMinimoError = null)
        }

    fun onDescripcionChange(v: String) =
        _form.update {
            it.copy(descripcion = v, descripcionError = null)
        }

    fun onMarcaChange(v: String) =
        _form.update {
            it.copy(marca = v, marcaError = null)
        }

    fun onUnidadMedidaChange(v: String) =
        _form.update {
            it.copy(unidadMedida = v, unidadMedidaError = null)
        }

    fun onPrecioCompraChange(v: String) =
        _form.update {
            it.copy(precioCompra = v, precioCompraError = null)
        }

    fun onPrecioVentaChange(v: String) =
        _form.update {
            it.copy(precioVenta = v, precioVentaError = null)
        }

    fun onEstadoChange(v: String) {
        if (v in listOf(
                "Activo",
                "Inactivo",
                "Descontinuado"
            )
        ) {
            _form.update {
                it.copy(estado = v)
            }
        }
    }

    fun onCantidadReposicionChange(v: String) =
        _form.update {
            it.copy(
                cantidadReposicion = v,
                cantidadReposicionError = null
            )
        }

    fun onProveedorChange(id: Int?) {
        _form.update {
            it.copy(
                proveedorId = id,
                errorGeneral = null
            )
        }
    }

    fun onUbicacionChange(id: Int?) {
        _form.update {
            it.copy(
                ubicacionId = id,
                errorGeneral = null
            )
        }
    }

    // NUEVO PRODUCTO

    fun nuevoProducto() {
        _form.value = ProductoUiState()
    }

    // CARGAR PRODUCTO

    fun cargarProducto(id: Int) {

        _form.value = ProductoUiState(
            id = id,
            cargando = true
        )

        viewModelScope.launch {
            try {
                val p = repository.obtenerPorId(id)

                if (p == null) {
                    _form.update {
                        it.copy(
                            cargando = false,
                            errorGeneral = "El producto ya no existe"
                        )
                    }
                    return@launch
                }

                _form.value = ProductoUiState(
                    id = p.id,
                    codigo = p.codigo,
                    nombre = p.nombre,
                    categoria = p.categoria,
                    cantidad = p.cantidad.toString(),
                    stockMinimo = p.stockMinimo.toString(),
                    descripcion = p.descripcion,
                    marca = p.marca,
                    unidadMedida = p.unidadMedida,
                    precioCompra = p.precioCompra.toString(),
                    precioVenta = p.precioVenta.toString(),
                    estado = p.estado,
                    proveedorId = p.proveedorId,
                    ubicacionId = p.ubicacionId,
                    fechaCambioUbicacion = p.fechaCambioUbicacion,
                    cantidadReposicion =
                        p.cantidadReposicion.toString()
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _form.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "No se pudo cargar el producto"
                    )
                }
            }
        }
    }

    // VALIDACIONES

    private fun validarEntero(valor: String): String? {

        if (valor.isBlank()) {
            return "Campo obligatorio"
        }

        val numero = valor.trim().toIntOrNull()
            ?: return "Ingresa un número entero válido"

        return if (numero < 0) {
            "No puede ser negativo"
        } else {
            null
        }
    }

    private fun validarPrecio(valor: String): String? {

        if (valor.isBlank()) {
            return "Campo obligatorio"
        }

        val numero = valor.trim().toLongOrNull()
            ?: return "Ingresa pesos enteros, sin puntos ni comas"

        return if (numero < 0) {
            "No puede ser negativo"
        } else {
            null
        }
    }

    private fun validar(): Boolean {

        val s = _form.value

        val validado = s.copy(
            codigoError =
                if (s.codigo.isBlank())
                    "El código es obligatorio"
                else null,

            nombreError =
                if (s.nombre.isBlank())
                    "El nombre es obligatorio"
                else null,

            categoriaError =
                if (s.categoria.isBlank())
                    "La categoría es obligatoria"
                else null,

            descripcionError =
                if (s.descripcion.isBlank())
                    "La descripción es obligatoria"
                else null,

            marcaError =
                if (s.marca.isBlank())
                    "La marca es obligatoria"
                else null,

            unidadMedidaError =
                if (s.unidadMedida.isBlank())
                    "La unidad de medida es obligatoria"
                else null,

            cantidadError = validarEntero(s.cantidad),
            stockMinimoError = validarEntero(s.stockMinimo),
            precioCompraError = validarPrecio(s.precioCompra),
            precioVentaError = validarPrecio(s.precioVenta),
            cantidadReposicionError =
                validarEntero(s.cantidadReposicion),

            errorGeneral = null
        )

        _form.value = validado

        return listOf(
            validado.codigoError,
            validado.nombreError,
            validado.categoriaError,
            validado.descripcionError,
            validado.marcaError,
            validado.unidadMedidaError,
            validado.cantidadError,
            validado.stockMinimoError,
            validado.precioCompraError,
            validado.precioVentaError,
            validado.cantidadReposicionError
        ).all { it == null }
    }

    // GUARDAR PRODUCTO

    fun guardar() {

        if (_form.value.guardando || _form.value.cargando) {
            return
        }

        if (!validar()) return

        val s = _form.value

        _form.update {
            it.copy(guardando = true)
        }

        viewModelScope.launch {

            try {
                val duplicado = repository.obtenerPorCodigo(
                    s.codigo.trim()
                )

                if (duplicado != null && duplicado.id != s.id) {
                    _form.update {
                        it.copy(
                            guardando = false,
                            codigoError =
                                "Ya existe un producto con este código"
                        )
                    }
                    return@launch
                }

                val base = if (s.esEdicion) {
                    repository.obtenerPorId(s.id)
                } else {
                    ProductoEntity(
                        codigo = "",
                        nombre = "",
                        categoria = "",
                        cantidad = 0,
                        stockMinimo = 0
                    )
                }

                if (base == null) {
                    _form.update {
                        it.copy(
                            guardando = false,
                            errorGeneral = "El producto ya no existe"
                        )
                    }
                    return@launch
                }

                // VALIDAR PROVEEDOR

                val proveedorId = s.proveedorId

                if (proveedorId != null) {

                    val proveedor =
                        proveedorDao.obtenerPorId(proveedorId)

                    val esAsociacionExistente =
                        s.esEdicion &&
                                base.proveedorId == proveedorId

                    if (
                        proveedor == null ||
                        (!proveedor.activo && !esAsociacionExistente)
                    ) {
                        _form.update {
                            it.copy(
                                guardando = false,
                                errorGeneral =
                                    "Selecciona un proveedor activo"
                            )
                        }
                        return@launch
                    }
                }

                // VALIDAR UBICACION

                val ubicacionId = s.ubicacionId

                if (
                    ubicacionId != null &&
                    ubicacionDao.obtenerPorId(ubicacionId) == null
                ) {
                    _form.update {
                        it.copy(
                            guardando = false,
                            errorGeneral =
                                "La ubicación seleccionada ya no existe"
                        )
                    }
                    return@launch
                }

                val fechaUbicacion =
                    if (base.ubicacionId != ubicacionId) {
                        System.currentTimeMillis()
                    } else {
                        base.fechaCambioUbicacion
                    }

                // PREPARAR PRODUCTO

                val producto = base.copy(
                    codigo = s.codigo.trim(),
                    nombre = s.nombre.trim(),
                    categoria = s.categoria.trim(),

                    // El stock existente no se modifica
                    // directamente desde el formulario.
                    cantidad = if (s.esEdicion) {
                        base.cantidad
                    } else {
                        s.cantidad.trim().toInt()
                    },

                    stockMinimo = s.stockMinimo.trim().toInt(),
                    descripcion = s.descripcion.trim(),
                    marca = s.marca.trim(),
                    unidadMedida = s.unidadMedida.trim(),
                    precioCompra = s.precioCompra.trim().toLong(),
                    precioVenta = s.precioVenta.trim().toLong(),
                    estado = s.estado,
                    proveedorId = proveedorId,
                    ubicacionId = ubicacionId,
                    fechaCambioUbicacion = fechaUbicacion,
                    cantidadReposicion =
                        s.cantidadReposicion.trim().toInt()
                )

                // GUARDAR EN ROOM

                if (s.esEdicion) {
                    repository.actualizar(producto)
                } else {
                    repository.insertar(producto)
                }

                _mensaje.value =
                    if (s.esEdicion) {
                        "Producto actualizado correctamente"
                    } else {
                        "Producto creado correctamente"
                    }

                _form.update {
                    it.copy(
                        guardando = false,
                        guardadoExitosamente = true
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _form.update {
                    it.copy(
                        guardando = false,
                        errorGeneral =
                            "No se pudo guardar. Intenta nuevamente."
                    )
                }
            }
        }
    }

    fun consumirGuardado() {
        _form.update {
            it.copy(guardadoExitosamente = false)
        }
    }

    // ELIMINAR PRODUCTO

    fun eliminar(producto: ProductoEntity) {
        viewModelScope.launch {
            repository.eliminar(producto)
        }
    }

    // RESTAURAR PRODUCTO ELIMINADO

    fun restaurar(producto: ProductoEntity) {
        viewModelScope.launch {
            repository.insertar(producto)
        }
    }

    // PRODUCTOS CON STOCK BAJO

    val productosStockBajo: StateFlow<List<ProductoEntity>> =
        repository.observarTodos()
            .map { productos ->
                productos.filter { it.stockBajo }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // GENERAR BORRADOR DE CORREO

    fun generarBorrador(
        productos: List<ProductoEntity>
    ): BorradorCorreo {

        val detalle = productos.joinToString("\n") { p ->
            "- ${p.nombre} (código ${p.codigo}): " +
                    "disponible ${p.cantidad}, " +
                    "solicitar ${p.cantidadSugerida} unidades"
        }

        val cuerpo =
            "Estimado proveedor:\n\n" +
                    "Junto con saludar, solicitamos disponibilidad " +
                    "y cotización de los siguientes productos:\n\n" +
                    detalle +
                    "\n\nQuedamos atentos a su respuesta.\n\n" +
                    "Saludos cordiales,\n" +
                    "Taller PuenteAuto"

        return BorradorCorreo(
            destinatario = "proveedor.ficticio@example.com",
            asunto = "Solicitud de cotización de reposición",
            cuerpo = cuerpo
        )
    }

    // VALIDAR DESTINATARIO

    fun validarDestinatario(texto: String): String? {

        val correo = texto.trim()

        if (correo.isBlank()) {
            return "El destinatario es obligatorio"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return "Ingresa un correo válido"
        }

        return null
    }
}
