
package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoViewModel,
    onEscanearCodigo: () -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.form.collectAsState()

    val proveedores by viewModel.proveedores.collectAsState()
    val ubicaciones by viewModel.ubicaciones.collectAsState()

    var menuProveedorAbierto by remember {
        mutableStateOf(false)
    }

    var menuUbicacionAbierto by remember {
        mutableStateOf(false)
    }

    var menuEstadoAbierto by remember {
        mutableStateOf(false)
    }

    val proveedorSeleccionado = proveedores.firstOrNull {
        it.id == state.proveedorId
    }

    val ubicacionSeleccionada = ubicaciones.firstOrNull {
        it.id == state.ubicacionId
    }

    val formatoFechaUbicacion = remember {
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("es", "CL")
        )
    }

    val habilitado = !state.cargando && !state.guardando

    // Volver a la lista cuando se guarda correctamente
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
                        if (state.esEdicion) {
                            "Editar producto"
                        } else {
                            "Nuevo producto"
                        }
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

            // ESTADO DE CARGA

            if (state.cargando) {
                CircularProgressIndicator()
                Text("Cargando producto...")
            }

            state.errorGeneral?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // INFORMACIÓN DEL PRODUCTO

            Text(
                text = "Información del producto",
                style = MaterialTheme.typography.titleMedium
            )

            // Código + lector QR

            CampoTexto(
                label = "Código",
                valor = state.codigo,
                onChange = viewModel::onCodigoChange,
                error = state.codigoError,
                iconoInicial = Icons.Default.Inventory2,
                habilitado = habilitado,
                trailingIcon = {
                    IconButton(
                        onClick = onEscanearCodigo,
                        enabled = habilitado
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear código QR"
                        )
                    }
                }
            )

            CampoTexto(
                label = "Nombre",
                valor = state.nombre,
                onChange = viewModel::onNombreChange,
                error = state.nombreError,
                iconoInicial = Icons.Default.Edit,
                habilitado = habilitado
            )

            CampoTexto(
                label = "Descripción",
                valor = state.descripcion,
                onChange = viewModel::onDescripcionChange,
                error = state.descripcionError,
                iconoInicial = Icons.Default.Edit,
                habilitado = habilitado
            )

            CampoTexto(
                label = "Categoría",
                valor = state.categoria,
                onChange = viewModel::onCategoriaChange,
                error = state.categoriaError,
                iconoInicial = Icons.Default.Category,
                habilitado = habilitado
            )

            CampoTexto(
                label = "Marca",
                valor = state.marca,
                onChange = viewModel::onMarcaChange,
                error = state.marcaError,
                habilitado = habilitado
            )

            CampoTexto(
                label = "Unidad de medida (Unidad, Litro, etc.)",
                valor = state.unidadMedida,
                onChange = viewModel::onUnidadMedidaChange,
                error = state.unidadMedidaError,
                habilitado = habilitado
            )

            // ESTADO DEL PRODUCTO

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
                    onDismissRequest = {
                        menuEstadoAbierto = false
                    }
                ) {
                    listOf(
                        "Activo",
                        "Inactivo",
                        "Descontinuado"
                    ).forEach { opcion ->

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

            // PROVEEDOR

            Text(
                text = "Proveedor principal",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = {
                        menuProveedorAbierto = true
                    },
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        proveedorSeleccionado?.let {
                            "${it.nombre}${
                                if (!it.activo) " (inactivo)"
                                else ""
                            }"
                        } ?: "Sin proveedor asignado"
                    )
                }

                DropdownMenu(
                    expanded = menuProveedorAbierto,
                    onDismissRequest = {
                        menuProveedorAbierto = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Sin proveedor asignado")
                        },
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
                                    Text(
                                        "${proveedor.codigo} - ${proveedor.nombre}"
                                    )
                                },
                                onClick = {
                                    viewModel.onProveedorChange(
                                        proveedor.id
                                    )
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

            Spacer(Modifier.height(8.dp))

            // UBICACIÓN

            Text(
                text = "Ubicación en bodega",
                style = MaterialTheme.typography.titleMedium
            )

            Box {
                OutlinedButton(
                    onClick = {
                        menuUbicacionAbierto = true
                    },
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
                    onDismissRequest = {
                        menuUbicacionAbierto = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Sin ubicación asignada")
                        },
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
                                viewModel.onUbicacionChange(
                                    ubicacion.id
                                )
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
                        formatoFechaUbicacion.format(Date(fecha))
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

            Spacer(Modifier.height(8.dp))

            // EXISTENCIAS

            Text(
                text = "Existencias",
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
                iconoInicial = Icons.Default.ShoppingCart,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado && !state.esEdicion
            )

            // No permitir editar stock directamente

            if (state.esEdicion) {
                Text(
                    text = "Para cambiar el stock, registra un movimiento.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTexto(
                label = "Stock mínimo",
                valor = state.stockMinimo,
                onChange = viewModel::onStockMinimoChange,
                error = state.stockMinimoError,
                iconoInicial = Icons.Default.Warning,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            // CÁLCULO DE REPOSICIÓN

            val cantidadActual =
                state.cantidad.trim().toIntOrNull()

            val minimoActual =
                state.stockMinimo.trim().toIntOrNull()

            if (
                cantidadActual != null &&
                minimoActual != null &&
                cantidadActual >= 0 &&
                minimoActual >= 0
            ) {
                val necesitaReposicion =
                    cantidadActual <= minimoActual

                val objetivo =
                    (minimoActual.toLong() * 2L)
                        .coerceAtLeast(1L)
                        .coerceAtMost(Int.MAX_VALUE.toLong())

                val sugerida = if (necesitaReposicion) {
                    (objetivo - cantidadActual)
                        .coerceAtLeast(0L)
                } else {
                    0L
                }

                Text(
                    text = "Reposición sugerida automáticamente: $sugerida"
                )
            }

            Spacer(Modifier.height(8.dp))

            // PRECIOS

            Text(
                text = "Precios referenciales",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Escribe pesos chilenos sin puntos ni comas.",
                style = MaterialTheme.typography.bodySmall
            )

            CampoTexto(
                label = "Precio de compra",
                valor = state.precioCompra,
                onChange = viewModel::onPrecioCompraChange,
                error = state.precioCompraError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            CampoTexto(
                label = "Precio de venta",
                valor = state.precioVenta,
                onChange = viewModel::onPrecioVentaChange,
                error = state.precioVentaError,
                tipoTeclado = KeyboardType.Number,
                habilitado = habilitado
            )

            Spacer(Modifier.height(8.dp))

            // GUARDAR PRODUCTO

            Button(
                onClick = viewModel::guardar,
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.guardando) {
                        "Guardando..."
                    } else {
                        "Guardar producto"
                    }
                )
            }
        }
    }
}

// CAMPO DE TEXTO REUTILIZABLE

@Composable
private fun CampoTexto(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    error: String?,
    iconoInicial: ImageVector? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val iconoFinal: (@Composable () -> Unit)? =
        trailingIcon ?: if (error != null) {
            {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else {
            null
        }

    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = habilitado,
        leadingIcon = if (iconoInicial != null) {
            {
                Icon(
                    imageVector = iconoInicial,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        trailingIcon = iconoFinal,
        isError = error != null,
        supportingText = {
            Crossfade(
                targetState = error,
                label = "errorCampo"
            ) { mensaje ->
                if (mensaje != null) {
                    Text(mensaje)
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
