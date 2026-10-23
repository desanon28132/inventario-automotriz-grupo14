package com.example.dsy1105_006d_lc7.ui.reposicion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReposicionViewModel(application: Application) :
    AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.reposicionDao()

    val pendientes = dao.observarPendientes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val proveedores = database.proveedorDao().observarTodos()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val solicitudes = dao.observarSolicitudes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _procesando = MutableStateFlow(false)
    val procesando = _procesando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    fun crearSolicitud(proveedorId: Int) {
        ejecutar("Solicitud creada correctamente") {
            dao.crearSolicitud(proveedorId)
        }
    }

    fun avanzarEstado(id: Int) {
        ejecutar("Estado actualizado") {
            dao.avanzarEstado(id)
        }
    }
    fun guardarObservaciones(
        id: Int,
        observaciones: String,
        onGuardado: () -> Unit
    ) {
        ejecutar("Observaciones guardadas correctamente") {
            val actualizadas = dao.actualizarObservaciones(
                id = id,
                observaciones = observaciones.trim(),
                fecha = System.currentTimeMillis()
            )

            require(actualizadas == 1) {
                "La solicitud ya no existe"
            }

            onGuardado()
        }
    }

    private fun ejecutar(
        mensajeExito: String,
        accion: suspend () -> Unit
    ) {
        if (_procesando.value) return

        _procesando.value = true
        _mensaje.value = null

        viewModelScope.launch {
            try {
                accion()
                _mensaje.value = mensajeExito
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _mensaje.value = e.message ?: "Revisa los datos"
            } catch (e: Exception) {
                _mensaje.value = "No se pudo completar la operación"
            } finally {
                _procesando.value = false
            }
        }
    }
}