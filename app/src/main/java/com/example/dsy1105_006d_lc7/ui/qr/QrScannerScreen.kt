package com.example.dsy1105_006d_lc7.ui.qr

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import com.example.dsy1105_006d_lc7.utils.CameraPermissionHelper
import com.example.dsy1105_006d_lc7.utils.QrScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    onVolver: () -> Unit,
    onEditarProducto: (Int) -> Unit,
    onRegistrarProducto: (String) -> Unit
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsState()

    var tienePermiso by remember {
        mutableStateOf(CameraPermissionHelper.hasCameraPermission(context))
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
        if (!concedido) {
            Toast.makeText(
                context, "Se necesita el permiso de cámara para escanear", Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = appTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val producto = estado.producto
            when {
                !tienePermiso -> PermisoRequerido(
                    onSolicitar = { launcher.launch(Manifest.permission.CAMERA) }
                )
                estado.codigoLeido == null -> Escaner(onDetectado = viewModel::onQrDetected)
                estado.buscando -> Text("Buscando producto...")
                producto != null -> ResultadoProducto(
                    producto = producto,
                    onEditar = { onEditarProducto(producto.id) },
                    onOtro = viewModel::limpiar
                )
                else -> NoEncontrado(
                    codigo = estado.codigoLeido.orEmpty(),
                    onRegistrar = { onRegistrarProducto(estado.codigoLeido.orEmpty()) },
                    onOtro = viewModel::limpiar
                )
            }
        }
    }
}

@Composable
private fun PermisoRequerido(onSolicitar: () -> Unit) {
    Text(
        "Permiso de cámara requerido",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text("Para escanear códigos QR necesitamos acceso a la cámara.")
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onSolicitar) { Text("Conceder permiso de cámara") }
}

@Composable
private fun Escaner(onDetectado: (String) -> Unit) {
    Text("Escanea el QR del producto", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        QrScanner(onQrCodeScanned = onDetectado)
        Surface(
            modifier = Modifier.size(250.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        ) {}
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text("Enfoca el código dentro del marco")
}

@Composable
private fun ResultadoProducto(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onOtro: () -> Unit
) {
    val stockBajo = producto.cantidad <= producto.stockMinimo

    Text("Producto encontrado", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (stockBajo) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                producto.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Código: ${producto.codigo}")
            Text("Categoría: ${producto.categoria}")
            Text("Cantidad disponible: ${producto.cantidad}")
            Text("Existencia mínima: ${producto.stockMinimo}")
            if (stockBajo) {
                Text(
                    "Stock bajo",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
        Text("Ver / editar producto")
    }
    OutlinedButton(onClick = onOtro, modifier = Modifier.fillMaxWidth()) {
        Text("Escanear otro código")
    }
}

@Composable
private fun NoEncontrado(
    codigo: String,
    onRegistrar: () -> Unit,
    onOtro: () -> Unit
) {
    Text("Producto no registrado", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text("No existe un producto con el código \"$codigo\".")
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
        Text("Registrar con este código")
    }
    OutlinedButton(onClick = onOtro, modifier = Modifier.fillMaxWidth()) {
        Text("Escanear otro código")
    }
}