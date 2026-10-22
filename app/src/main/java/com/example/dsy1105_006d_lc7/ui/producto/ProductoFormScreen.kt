package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.form.collectAsState()
    val proveedores by viewModel.proveedores.collectAsState()

    var menuProveedorAbierto by remember {
        mutableStateOf(false)
    }

    val proveedorSeleccionado = proveedores.firstOrNull {
        it.id == state.proveedorId
    }
    val ubicaciones by viewModel.ubicaciones.collectAsState()

    var menuUbicacionAbierto by remember {
        mutableStateOf(false)
    }

    val ubicacionSeleccionada = ubicaciones.firstOrNull {
        it.id == state.ubicacionId
    }

    val formatoFechaUbicacion = remember {
        java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            java.util.Locale("es", "CL")
        )
    }

    var menuEstadoAbierto by remember { mutableStateOf(false) }

    val habilitado = !state.cargando && !state.guardando

    LaunchedEffect(state.guardadoExitosamente) {
        if (state.guardadoExitosamente) {
            viewModel.consumirGuardado()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.esEdicion) "Editar producto"
                        else "Nuevo producto"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = !state.guardando
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.cargando) {
                CircularProgressIndicator()
                Text("Cargando producto...")
            }

            state.errorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                "Información del producto",
                style = MaterialTheme.typography.titleMedium
            )

            CampoTexto(
                "Código", state.codigo,
                viewModel::onCodigoChange, state.codigoError,
                habilitado = habilitado
            )

            CampoTexto(
                "Nombre", state.nombre,
                viewModel::onNombreChange, state.nombreError,
                habilitado = habilitado
            )

            CampoTexto(
                "Descripción", state.descripcion,
                viewModel::onDescripcionChange, state.descripcionError,
                habilitado = habilitado
            )

            CampoTexto(
                "Categoría", state.categoria,
                viewModel::onCategoriaChange, state.categoriaError,
                habilitado = habilitado
            )

            CampoTexto(
                "Marca", state.marca,
                viewModel::onMarcaChange, state.marcaError,
                habilitado = habilitado
            )

            CampoTexto(
                "Unidad de medida (Unidad, Litro, etc.)",
                state.unidadMedida,
                viewModel::onUnidadMedidaChange,
                state.unidadMedidaError,
                habilitado = habilitado
            )

            Box {
                OutlinedButton(
                    onClick = { menuEstadoAbierto = true },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Estado: ${state.estado}")
                }

                DropdownMenu(
                    expanded = menuEstadoAbierto,
                    onDismissRequest = { menuEstadoAbierto = false }
                ) {
                    listOf("Activo", "Inactivo", "Descontinuado")
                        .forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    viewModel.onEstadoChange(opcion)
                                    menuEstadoAbierto = false
                                }
                            )
                        }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Proveedor principal",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = { menuProveedorAbierto = true },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        proveedorSeleccionado?.let {
                            "${it.nombre}${if (!it.activo) " (inactivo)" else ""}"
                        } ?: "Sin proveedor asignado"
                    )
                }

                DropdownMenu(
                    expanded = menuProveedorAbierto,
                    onDismissRequest = { menuProveedorAbierto = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin proveedor asignado") },
                        onClick = {
                            viewModel.onProveedorChange(null)
                            menuProveedorAbierto = false
                        }
                    )

                    proveedores
                        .filter { it.activo }
                        .forEach { proveedor ->
                            DropdownMenuItem(
                                text = {
                                    Text("${proveedor.codigo} - ${proveedor.nombre}")
                                },
                                onClick = {
                                    viewModel.onProveedorChange(proveedor.id)
                                    menuProveedorAbierto = false
                                }
                            )
                        }
                }
            }

            if (proveedores.none { it.activo }) {
                Text(
                    text = "Registra un proveedor activo desde el menú Proveedores.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Ubicación en bodega",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = { menuUbicacionAbierto = true },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        ubicacionSeleccionada?.let {
                            "${it.codigo} - ${it.zona}"
                        } ?: "Sin ubicación asignada"
                    )
                }

                DropdownMenu(
                    expanded = menuUbicacionAbierto,
                    onDismissRequest = { menuUbicacionAbierto = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin ubicación asignada") },
                        onClick = {
                            viewModel.onUbicacionChange(null)
                            menuUbicacionAbierto = false
                        }
                    )

                    ubicaciones.forEach { ubicacion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${ubicacion.codigo}: ${ubicacion.zona}, " +
                                            "estante ${ubicacion.estante}, " +
                                            "repisa ${ubicacion.repisa}, " +
                                            ubicacion.posicion
                                )
                            },
                            onClick = {
                                viewModel.onUbicacionChange(ubicacion.id)
                                menuUbicacionAbierto = false
                            }
                        )
                    }
                }
            }

            ubicacionSeleccionada?.let {
                Text("Zona: ${it.zona}")
                Text("Estante: ${it.estante}")
                Text("Repisa: ${it.repisa}")
                Text("Posición: ${it.posicion}")
            }

            state.fechaCambioUbicacion?.let { fecha ->
                Text(
                    text = "Último cambio guardado: ${
                        formatoFechaUbicacion.format(java.util.Date(fecha))
                    }",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (ubicaciones.isEmpty()) {
                Text(
                    text = "Primero registra una ubicación desde el menú Ubicaciones.",
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Text(
                "Existencias",
                style = MaterialTheme.typography.titleMedium
            )

            CampoTexto(
                label = if (state.esEdicion) {
                    "Stock actual"
                } else {
                    "Cantidad inicial"
                },
                valor = state.cantidad,
                onChange = viewModel::onCantidadChange,
                error = state.cantidadError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado && !state.esEdicion
            )

            if (state.esEdicion) {
                Text(
                    text = "Para cambiar el stock, registra un movimiento.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTexto(
                "Stock mínimo", state.stockMinimo,
                viewModel::onStockMinimoChange, state.stockMinimoError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            val cantidadActual = state.cantidad.trim().toIntOrNull()
            val minimoActual = state.stockMinimo.trim().toIntOrNull()

            if (
                cantidadActual != null &&
                minimoActual != null &&
                cantidadActual >= 0 &&
                minimoActual >= 0
            ) {
                val necesitaReposicion =
                    cantidadActual == 0 || cantidadActual < minimoActual

                val objetivo = (minimoActual.toLong() * 2L)
                    .coerceAtLeast(1L)
                    .coerceAtMost(Int.MAX_VALUE.toLong())

                val sugerida = if (necesitaReposicion) {
                    (objetivo - cantidadActual).coerceAtLeast(0L)
                } else {
                    0L
                }

                Text("Reposición sugerida automáticamente: $sugerida")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Precios referenciales",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Escribe pesos chilenos sin puntos ni comas.")

            CampoTexto(
                "Precio de compra", state.precioCompra,
                viewModel::onPrecioCompraChange, state.precioCompraError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            CampoTexto(
                "Precio de venta", state.precioVenta,
                viewModel::onPrecioVentaChange, state.precioVentaError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::guardar,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.guardando) "Guardando..."
                    else "Guardar producto"
                )
            }
        }
    }
}

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = habilitado,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado
        ),
        modifier = Modifier.fillMaxWidth()
    )
}