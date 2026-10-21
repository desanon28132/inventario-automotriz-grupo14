package com.example.dsy1105_006d_lc7.ui.movimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovimientoViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val movimientoDao = database.movimientoDao()

    private val _estado = MutableStateFlow(MovimientoUiState())
    val estado = _estado.asStateFlow()

    val productos = database.productoDao()
        .observarTodos()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val historial = movimientoDao.observarTodos()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun onProductoChange(id: Int) {
        _estado.update {
            it.copy(productoId = id, error = null, mensaje = null)
        }
    }

    fun onTipoChange(tipo: String) {
        _estado.update {
            it.copy(
                tipo = tipo,
                esEntrada = tipo != "Venta" && tipo != "Consumo",
                error = null,
                mensaje = null
            )
        }
    }

    fun onCantidadChange(valor: String) {
        _estado.update {
            it.copy(cantidad = valor, error = null, mensaje = null)
        }
    }

    fun onMotivoChange(valor: String) {
        _estado.update {
            it.copy(motivo = valor, error = null, mensaje = null)
        }
    }

    fun onObservacionChange(valor: String) {
        _estado.update {
            it.copy(observacion = valor, mensaje = null)
        }
    }

    fun onEntradaChange(valor: Boolean) {
        _estado.update {
            it.copy(esEntrada = valor, error = null, mensaje = null)
        }
    }

    fun guardar(usuario: String) {
        val s = _estado.value
        if (s.guardando) return

        val productoId = s.productoId
        val cantidad = s.cantidad.trim().toIntOrNull()

        val error = when {
            productoId == null -> "Selecciona un producto"
            cantidad == null || cantidad <= 0 ->
                "Ingresa una cantidad entera mayor que cero"
            s.motivo.isBlank() -> "Escribe el motivo"
            usuario.isBlank() -> "Debes iniciar sesión nuevamente"
            else -> null
        }

        if (error != null) {
            _estado.update { it.copy(error = error, mensaje = null) }
            return
        }

        // También permite a Kotlin reconocer que no son nulos.
        if (productoId == null || cantidad == null) return

        _estado.update {
            it.copy(guardando = true, error = null, mensaje = null)
        }

        viewModelScope.launch {
            try {
                movimientoDao.registrarMovimiento(
                    productoId = productoId,
                    tipo = s.tipo,
                    cantidad = cantidad,
                    motivo = s.motivo,
                    observacion = s.observacion,
                    usuario = usuario,
                    esEntrada = s.esEntrada
                )

                _estado.value = MovimientoUiState(
                    productoId = productoId,
                    tipo = s.tipo,
                    esEntrada = s.esEntrada,
                    mensaje = "Movimiento registrado correctamente"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        error = e.message ?: "Revisa los datos"
                    )
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        error = "No se pudo guardar el movimiento"
                    )
                }
            }
        }
    }
}