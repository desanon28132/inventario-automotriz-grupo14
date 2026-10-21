package com.example.dsy1105_006d_lc7.ui.proveedor



import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.ProveedorEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProveedorUiState(
    val id: Int = 0,
    val codigo: String = "",
    val nombre: String = "",
    val correo: String = "",
    val activo: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class ProveedorViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dao = AppDatabase
        .getDatabase(application)
        .proveedorDao()

    private val _estado = MutableStateFlow(ProveedorUiState())
    val estado = _estado.asStateFlow()

    val proveedores = dao.observarTodos()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun onCodigoChange(valor: String) {
        _estado.update {
            it.copy(codigo = valor, error = null, mensaje = null)
        }
    }

    fun onNombreChange(valor: String) {
        _estado.update {
            it.copy(nombre = valor, error = null, mensaje = null)
        }
    }

    fun onCorreoChange(valor: String) {
        _estado.update {
            it.copy(correo = valor, error = null, mensaje = null)
        }
    }

    fun onActivoChange(valor: Boolean) {
        _estado.update { it.copy(activo = valor, mensaje = null) }
    }

    fun nuevo() {
        if (!_estado.value.guardando) {
            _estado.value = ProveedorUiState()
        }
    }

    fun editar(proveedor: ProveedorEntity) {
        if (_estado.value.guardando) return

        _estado.value = ProveedorUiState(
            id = proveedor.id,
            codigo = proveedor.codigo,
            nombre = proveedor.nombre,
            correo = proveedor.correo,
            activo = proveedor.activo
        )
    }

    fun guardar() {
        val s = _estado.value
        if (s.guardando) return

        val error = when {
            s.codigo.isBlank() -> "Escribe el código"
            s.nombre.isBlank() -> "Escribe el nombre"
            !Patterns.EMAIL_ADDRESS.matcher(s.correo.trim()).matches() ->
                "Escribe un correo válido"
            else -> null
        }

        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }

        _estado.update {
            it.copy(guardando = true, error = null, mensaje = null)
        }

        viewModelScope.launch {
            try {
                val codigo = s.codigo.trim()
                val existente = dao.obtenerPorCodigo(codigo)

                if (existente != null && existente.id != s.id) {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            error = "Ya existe un proveedor con ese código"
                        )
                    }
                    return@launch
                }

                val proveedor = ProveedorEntity(
                    id = s.id,
                    codigo = codigo,
                    nombre = s.nombre.trim(),
                    correo = s.correo.trim(),
                    activo = s.activo
                )

                if (s.id == 0) {
                    dao.insertar(proveedor)
                } else {
                    dao.actualizar(proveedor)
                }

                _estado.value = ProveedorUiState(
                    mensaje = "Proveedor guardado correctamente"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        error = "No se pudo guardar el proveedor"
                    )
                }
            }
        }
    }
}