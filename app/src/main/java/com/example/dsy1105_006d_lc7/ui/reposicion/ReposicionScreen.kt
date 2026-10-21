package com.example.dsy1105_006d_lc7.ui.reposicion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.SolicitudReposicionEntity
import com.example.dsy1105_006d_lc7.data.local.calcularReposicion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposicionScreen(
    viewModel: ReposicionViewModel,
    onVolver: () -> Unit
) {
    val pendientes by viewModel.pendientes.collectAsState()
    val proveedores by viewModel.proveedores.collectAsState()
    val solicitudes by viewModel.solicitudes.collectAsState()
    val procesando by viewModel.procesando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var pestaña by remember { mutableStateOf(0) }
    var proveedorAConfirmar by remember {
        mutableStateOf<Int?>(null)
    }
    var solicitudAConfirmar by remember {
        mutableStateOf<SolicitudReposicionEntity?>(null)
    }

    val grupos = pendientes.groupBy { it.proveedorId }

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reposición") },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = !procesando
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = pestaña) {
                Tab(
                    selected = pestaña == 0,
                    onClick = { pestaña = 0 },
                    text = { Text("Por comprar") }
                )

                Tab(
                    selected = pestaña == 1,
                    onClick = { pestaña = 1 },
                    text = { Text("Solicitudes") }
                )
            }

            mensaje?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (pestaña == 0) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Se muestran productos activos agotados " +
                                    "o con stock inferior al mínimo."
                        )
                    }

                    if (pendientes.isEmpty()) {
                        item {
                            Text("No hay productos pendientes de reposición.")
                        }
                    }

                    grupos.forEach { (proveedorId, productos) ->
                        val proveedor = proveedores.firstOrNull {
                            it.id == proveedorId
                        }

                        val tieneSolicitudAbierta = solicitudes.any {
                            it.proveedorId == proveedorId &&
                                    it.estado != "Atendida"
                        }

                        item {
                            Text(
                                text = proveedor?.nombre
                                    ?: "Sin proveedor válido",
                                style = MaterialTheme.typography.titleLarge
                            )

                            if (proveedor == null) {
                                Text(
                                    "Asigna un proveedor desde " +
                                            "el formulario del producto."
                                )
                            } else if (!proveedor.activo) {
                                Text("El proveedor está inactivo.")
                            }
                        }

                        items(productos, key = { it.id }) { producto ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        producto.nombre,
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Text("Código: ${producto.codigo}")
                                    Text("Disponible: ${producto.cantidad}")
                                    Text("Stock mínimo: ${producto.stockMinimo}")

                                    Text(
                                        "Compra sugerida: ${
                                            calcularReposicion(producto)
                                        } ${producto.unidadMedida}"
                                    )
                                }
                            }
                        }

                        item {
                            if (tieneSolicitudAbierta) {
                                Text(
                                    "Ya existe una solicitud abierta " +
                                            "para este proveedor."
                                )
                            }

                            Button(
                                onClick = {
                                    proveedorAConfirmar = proveedorId
                                },
                                enabled = !procesando &&
                                        proveedor?.activo == true &&
                                        !tieneSolicitudAbierta,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Crear solicitud")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (solicitudes.isEmpty()) {
                        item {
                            Text("Todavía no hay solicitudes.")
                        }
                    }

                    items(solicitudes, key = { it.id }) { solicitud ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Solicitud #${solicitud.id}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(solicitud.proveedorNombre)
                                Text("Estado: ${solicitud.estado}")

                                Text(
                                    "Creada: ${
                                        formatoFecha.format(
                                            Date(solicitud.fechaCreacion)
                                        )
                                    }"
                                )

                                Text(
                                    "Actualizada: ${
                                        formatoFecha.format(
                                            Date(solicitud.fechaActualizacion)
                                        )
                                    }"
                                )

                                Text(solicitud.detalle)

                                val siguiente = siguienteEstado(
                                    solicitud.estado
                                )

                                if (siguiente != null) {
                                    OutlinedButton(
                                        onClick = {
                                            solicitudAConfirmar = solicitud
                                        },
                                        enabled = !procesando,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Marcar como $siguiente")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    proveedorAConfirmar?.let { proveedorId ->
        val nombre = proveedores.firstOrNull {
            it.id == proveedorId
        }?.nombre.orEmpty()

        AlertDialog(
            onDismissRequest = { proveedorAConfirmar = null },
            title = { Text("Crear solicitud") },
            text = {
                Text(
                    "Se guardará una solicitud para $nombre con los " +
                            "productos que necesiten reposición y sus " +
                            "cantidades calculadas al confirmar. " +
                            "No se enviará ningún correo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        proveedorAConfirmar = null
                        viewModel.crearSolicitud(proveedorId)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { proveedorAConfirmar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    solicitudAConfirmar?.let { solicitud ->
        val siguiente = siguienteEstado(solicitud.estado)

        val explicacion = when (siguiente) {
            "Preparada" ->
                "Confirma que revisaste los productos y cantidades."

            "Enviada" ->
                "Marca este estado solo si ya enviaste la solicitud " +
                        "por otro medio o estás simulando el envío académico. " +
                        "La aplicación no envía nada al cambiar el estado."

            "Atendida" ->
                "Confirma que la solicitud fue atendida. " +
                        "Recuerda registrar las unidades recibidas como " +
                        "Ingreso en Movimientos: este cambio no aumenta el stock."

            else -> ""
        }

        AlertDialog(
            onDismissRequest = { solicitudAConfirmar = null },
            title = { Text("Marcar como $siguiente") },
            text = { Text(explicacion) },
            confirmButton = {
                TextButton(
                    onClick = {
                        solicitudAConfirmar = null
                        viewModel.avanzarEstado(solicitud.id)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { solicitudAConfirmar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun siguienteEstado(estado: String): String? {
    return when (estado) {
        "Pendiente" -> "Preparada"
        "Preparada" -> "Enviada"
        "Enviada" -> "Atendida"
        else -> null
    }
}