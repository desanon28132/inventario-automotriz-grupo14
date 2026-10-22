package com.example.dsy1105_006d_lc7.ui.qr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrUiState(
    val codigoLeido: String? = null,
    val producto: ProductoEntity? = null,
    val buscando: Boolean = false
)

class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    )

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    // El contenido del QR es el código interno del producto
    fun onQrDetected(contenido: String) {
        val codigo = contenido.trim()
        _uiState.value = QrUiState(codigoLeido = codigo, buscando = true)

        viewModelScope.launch {
            val producto = repository.obtenerPorCodigo(codigo)
            _uiState.value = QrUiState(codigoLeido = codigo, producto = producto)
        }
    }

    // Permite un nuevo escaneo
    fun limpiar() {
        _uiState.value = QrUiState()
    }
}