package com.example.dsy1105_006d_lc7.ui.ubicacion


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.UbicacionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UbicacionUiState(
    val id: Int = 0,
    val codigo: String = "",
    val zona: String = "",
    val estante: String = "",
    val repisa: String = "",
    val posicion: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class UbicacionViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dao = AppDatabase
        .getDatabase(application)
        .ubicacionDao()

    private val _estado = MutableStateFlow(UbicacionUiState())
    val estado = _estado.asStateFlow()

    val ubicaciones = dao.observarTodas()
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

    fun onZonaChange(valor: String) {
        _estado.update {
            it.copy(zona = valor, error = null, mensaje = null)
        }
    }

    fun onEstanteChange(valor: String) {
        _estado.update {
            it.copy(estante = valor, error = null, mensaje = null)
        }
    }

    fun onRepisaChange(valor: String) {
        _estado.update {
            it.copy(repisa = valor, error = null, mensaje = null)
        }
    }

    fun onPosicionChange(valor: String) {
        _estado.update {
            it.copy(posicion = valor, error = null, mensaje = null)
        }
    }

    fun nuevo() {
        if (!_estado.value.guardando) {
            _estado.value = UbicacionUiState()
        }
    }

    fun editar(ubicacion: UbicacionEntity) {
        if (_estado.value.guardando) return

        _estado.value = UbicacionUiState(
            id = ubicacion.id,
            codigo = ubicacion.codigo,
            zona = ubicacion.zona,
            estante = ubicacion.estante,
            repisa = ubicacion.repisa,
            posicion = ubicacion.posicion
        )
    }

    fun guardar() {
        val s = _estado.value
        if (s.guardando) return

        val error = when {
            s.codigo.isBlank() -> "Escribe el código"
            s.zona.isBlank() -> "Escribe la zona"
            s.estante.isBlank() -> "Escribe el estante"
            s.repisa.isBlank() -> "Escribe la repisa"
            s.posicion.isBlank() -> "Escribe la posición"
            else -> null
        }

        if (error != null) {
            _estado.update {
                it.copy(error = error, mensaje = null)
            }
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
                            error = "Ya existe una ubicación con ese código"
                        )
                    }
                    return@launch
                }

                val ubicacion = UbicacionEntity(
                    id = s.id,
                    codigo = codigo,
                    zona = s.zona.trim(),
                    estante = s.estante.trim(),
                    repisa = s.repisa.trim(),
                    posicion = s.posicion.trim(),
                    fechaActualizacion = System.currentTimeMillis()
                )

                if (s.id == 0) {
                    dao.insertar(ubicacion)
                } else {
                    dao.actualizar(ubicacion)
                }

                _estado.value = UbicacionUiState(
                    mensaje = "Ubicación guardada correctamente"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        error = "No se pudo guardar la ubicación"
                    )
                }
            }
        }
    }
}