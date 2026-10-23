package com.example.dsy1105_006d_lc7.ui.producto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ProductoViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    )

    private val proveedorDao = AppDatabase
        .getDatabase(application)
        .proveedorDao()

    private val ubicacionDao = AppDatabase
        .getDatabase(application)
        .ubicacionDao()

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

    private val _form = MutableStateFlow(ProductoUiState())
    val form: StateFlow<ProductoUiState> = _form.asStateFlow()

    // Búsqueda y filtro de la lista.
    private val _busqueda = MutableStateFlow("")
    private val _categoriaSeleccionada =
        MutableStateFlow<String?>(null)

    val lista: StateFlow<ListaProductosUiState> = combine(
        repository.observarTodos(),
        _busqueda,
        _categoriaSeleccionada
    ) { productos, texto, seleccion ->

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
            val coincideCategoria =
                categoriaActual == null ||
                        producto.categoria.trim().equals(
                            categoriaActual,
                            ignoreCase = true
                        )

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

            coincideCategoria && coincideBusqueda
        }

        ListaProductosUiState(
            productos = filtrados,
            busqueda = texto,
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

    fun onFiltroCategoriaChange(categoria: String?) {
        _categoriaSeleccionada.value = categoria
    }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _categoriaSeleccionada.value = null
    }

    // Campos del formulario.
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
        if (v in listOf("Activo", "Inactivo", "Descontinuado")) {
            _form.update { it.copy(estado = v) }
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
            it.copy(proveedorId = id, errorGeneral = null)
        }
    }

    fun onUbicacionChange(id: Int?) {
        _form.update {
            it.copy(ubicacionId = id, errorGeneral = null)
        }
    }

    fun nuevoProducto() {
        _form.value = ProductoUiState()
    }

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
                    cantidadReposicion = p.cantidadReposicion.toString()
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

    private fun validarEntero(valor: String): String? {
        if (valor.isBlank()) return "Campo obligatorio"

        val numero = valor.trim().toIntOrNull()
            ?: return "Ingresa un número entero válido"

        return if (numero < 0) {
            "No puede ser negativo"
        } else {
            null
        }
    }

    private fun validarPrecio(valor: String): String? {
        if (valor.isBlank()) return "Campo obligatorio"

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
            codigoError = if (s.codigo.isBlank())
                "El código es obligatorio" else null,
            nombreError = if (s.nombre.isBlank())
                "El nombre es obligatorio" else null,
            categoriaError = if (s.categoria.isBlank())
                "La categoría es obligatoria" else null,
            descripcionError = if (s.descripcion.isBlank())
                "La descripción es obligatoria" else null,
            marcaError = if (s.marca.isBlank())
                "La marca es obligatoria" else null,
            unidadMedidaError = if (s.unidadMedida.isBlank())
                "La unidad de medida es obligatoria" else null,
            cantidadError = validarEntero(s.cantidad),
            stockMinimoError = validarEntero(s.stockMinimo),
            precioCompraError = validarPrecio(s.precioCompra),
            precioVentaError = validarPrecio(s.precioVenta),
            cantidadReposicionError = validarEntero(s.cantidadReposicion),
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

    fun guardar() {
        if (_form.value.guardando || _form.value.cargando) return
        if (!validar()) return

        val s = _form.value
        _form.update { it.copy(guardando = true) }

        viewModelScope.launch {
            try {
                val duplicado = repository.obtenerPorCodigo(
                    s.codigo.trim()
                )

                if (duplicado != null && duplicado.id != s.id) {
                    _form.update {
                        it.copy(
                            guardando = false,
                            codigoError = "Ya existe un producto con este código"
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

                val proveedorId = s.proveedorId

                if (proveedorId != null) {
                    val proveedor = proveedorDao.obtenerPorId(
                        proveedorId
                    )

                    val esAsociacionExistente =
                        s.esEdicion && base.proveedorId == proveedorId

                    if (
                        proveedor == null ||
                        (!proveedor.activo && !esAsociacionExistente)
                    ) {
                        _form.update {
                            it.copy(
                                guardando = false,
                                errorGeneral = "Selecciona un proveedor activo"
                            )
                        }
                        return@launch
                    }
                }

                val ubicacionId = s.ubicacionId

                if (
                    ubicacionId != null &&
                    ubicacionDao.obtenerPorId(ubicacionId) == null
                ) {
                    _form.update {
                        it.copy(
                            guardando = false,
                            errorGeneral = "La ubicación seleccionada ya no existe"
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

                val producto = base.copy(
                    codigo = s.codigo.trim(),
                    nombre = s.nombre.trim(),
                    categoria = s.categoria.trim(),
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
                    cantidadReposicion = s.cantidadReposicion.trim().toInt()
                )

                if (s.esEdicion) {
                    repository.actualizar(producto)
                } else {
                    repository.insertar(producto)
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
                        errorGeneral = "No se pudo guardar. Intenta nuevamente."
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

    fun eliminar(producto: ProductoEntity) {
        viewModelScope.launch {
            repository.eliminar(producto)
        }
    }
}