package com.example.dsy1105_006d_lc7.ui.movimiento


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(
    viewModel: MovimientoViewModel,
    usuario: String,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val historial by viewModel.historial.collectAsState()

    var pestaña by remember { mutableStateOf(0) }
    var confirmar by remember { mutableStateOf(false) }

    val producto = productos.firstOrNull {
        it.id == estado.productoId
    }

    val habilitado = !estado.guardando

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos") },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        enabled = habilitado
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
                    text = { Text("Registrar") }
                )

                Tab(
                    selected = pestaña == 1,
                    onClick = { pestaña = 1 },
                    text = { Text("Historial") }
                )
            }

            if (pestaña == 0) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Usuario: $usuario")

                    if (productos.isEmpty()) {
                        Text("Primero registra un producto.")
                    }

                    SelectorMovimiento(
                        titulo = "Producto",
                        valor = producto?.let {
                            "${it.codigo} - ${it.nombre}"
                        } ?: "Seleccionar",
                        opciones = productos.map {
                            "${it.codigo} - ${it.nombre}"
                        },
                        habilitado = habilitado && productos.isNotEmpty(),
                        onSeleccionar = { indice ->
                            viewModel.onProductoChange(productos[indice].id)
                        }
                    )

                    producto?.let {
                        Text(
                            text = "Stock disponible: ${it.cantidad}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    val tipos = listOf(
                        "Ingreso",
                        "Venta",
                        "Consumo",
                        "Ajuste",
                        "Devolución"
                    )

                    SelectorMovimiento(
                        titulo = "Tipo",
                        valor = estado.tipo,
                        opciones = tipos,
                        habilitado = habilitado,
                        onSeleccionar = {
                            viewModel.onTipoChange(tipos[it])
                        }
                    )

                    if (
                        estado.tipo == "Ajuste" ||
                        estado.tipo == "Devolución"
                    ) {
                        SelectorMovimiento(
                            titulo = "Dirección",
                            valor = if (estado.esEntrada) {
                                "Entrada: suma al stock"
                            } else {
                                "Salida: resta del stock"
                            },
                            opciones = listOf(
                                "Entrada: suma al stock",
                                "Salida: resta del stock"
                            ),
                            habilitado = habilitado,
                            onSeleccionar = {
                                viewModel.onEntradaChange(it == 0)
                            }
                        )

                        Text(
                            "En una devolución: entrada si vuelve al taller; " +
                                    "salida si se devuelve al proveedor."
                        )
                    }

                    OutlinedTextField(
                        value = estado.cantidad,
                        onValueChange = viewModel::onCantidadChange,
                        label = { Text("Unidades que entran o salen") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estado.motivo,
                        onValueChange = viewModel::onMotivoChange,
                        label = { Text("Motivo") },
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estado.observacion,
                        onValueChange = viewModel::onObservacionChange,
                        label = { Text("Observación opcional") },
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    estado.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    estado.mensaje?.let {
                        Text(it)
                    }

                    Button(
                        onClick = { confirmar = true },
                        enabled = habilitado && producto != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (estado.guardando) "Guardando..."
                            else "Registrar movimiento"
                        )
                    }
                }
            } else {
                if (historial.isEmpty()) {
                    Text(
                        text = "Todavía no hay movimientos registrados.",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(historial, key = { it.id }) { movimiento ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = movimiento.nombreProducto,
                                        style =
                                            MaterialTheme.typography.titleMedium
                                    )

                                    Text("Código: ${movimiento.codigoProducto}")
                                    Text("Tipo: ${movimiento.tipo}")
                                    Text("Cantidad: ${movimiento.cantidad}")

                                    Text(
                                        if (movimiento.variacion > 0) {
                                            "Entrada: +${movimiento.cantidad}"
                                        } else {
                                            "Salida: -${movimiento.cantidad}"
                                        }
                                    )

                                    Text(
                                        "Stock: ${movimiento.stockAnterior}" +
                                                " → ${movimiento.stockResultante}"
                                    )

                                    Text(
                                        formatoFecha.format(
                                            Date(movimiento.fechaHora)
                                        )
                                    )

                                    Text("Motivo: ${movimiento.motivo}")

                                    if (movimiento.observacion.isNotBlank()) {
                                        Text(
                                            "Observación: ${movimiento.observacion}"
                                        )
                                    }

                                    Text("Usuario: ${movimiento.usuario}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Confirmar movimiento") },
            text = {
                Text(
                    "Producto: ${producto?.nombre.orEmpty()}\n" +
                            "Tipo: ${estado.tipo}\n" +
                            "Cantidad: ${estado.cantidad}\n" +
                            "Dirección: ${
                                if (estado.esEntrada) "Entrada" else "Salida"
                            }\n\n" +
                            "Se actualizará el stock y se guardará el historial."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmar = false
                        viewModel.guardar(usuario)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SelectorMovimiento(
    titulo: String,
    valor: String,
    opciones: List<String>,
    habilitado: Boolean,
    onSeleccionar: (Int) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { abierto = true },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$titulo: $valor")
        }

        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false }
        ) {
            opciones.forEachIndexed { indice, texto ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        abierto = false
                        onSeleccionar(indice)
                    }
                )
            }
        }
    }
}