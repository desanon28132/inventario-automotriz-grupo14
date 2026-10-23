
package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.calcularReposicion
import com.example.dsy1105_006d_lc7.data.local.stockBajo
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import com.example.dsy1105_006d_lc7.utils.CorreoHelper
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosScreen(
    viewModel: ProductoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Int) -> Unit,
    onEscanearQr: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.lista.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val productosBajos by viewModel.productosStockBajo.collectAsState()

    var productoAEliminar by remember {
        mutableStateOf<ProductoEntity?>(null)
    }

    var mostrarSolicitud by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mensajes al crear o editar productos
    LaunchedEffect(mensaje) {
        mensaje?.let {
            viewModel.mensajeMostrado()
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {

                    // Solicitar reposicion por correo
                    IconButton(
                        onClick = {
                            mostrarSolicitud = true
                        },
                        enabled = productosBajos.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Solicitar reposición"
                        )
                    }

                    // Escanear codigo QR
                    IconButton(onClick = onEscanearQr) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear QR"
                        )
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar producto"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            // RESUMEN GENERAL DEL INVENTARIO

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ResumenCard(
                    titulo = "Total productos",
                    valor = estado.total,
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )

                ResumenCard(
                    titulo = "Stock bajo",
                    valor = estado.stockBajo,
                    containerColor =
                        MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // BUSCADOR

            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = {
                    Text("Buscar por nombre o código")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // FILTRO DE STOCK BAJO

            FilterChip(
                selected = estado.soloStockBajo,
                onClick = {
                    viewModel.onSoloStockBajoChange(
                        !estado.soloStockBajo
                    )
                },
                label = {
                    Text("Solo stock bajo")
                },
                leadingIcon = {
                    Icon(
                        if (estado.soloStockBajo) {
                            Icons.Default.Check
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null
                    )
                }
            )

            Spacer(Modifier.height(8.dp))

            // FILTROS POR CATEGORIA

            if (estado.categorias.isNotEmpty()) {

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    item {
                        FilterChip(
                            selected =
                                estado.categoriaSeleccionada == null,
                            onClick = {
                                viewModel.onCategoriaSeleccionada(null)
                            },
                            label = {
                                Text("Todas")
                            }
                        )
                    }

                    items(estado.categorias) { categoria ->

                        FilterChip(
                            selected =
                                estado.categoriaSeleccionada.equals(
                                    categoria,
                                    ignoreCase = true
                                ),
                            onClick = {
                                viewModel.onCategoriaSeleccionada(
                                    categoria
                                )
                            },
                            label = {
                                Text(categoria)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // LIMPIAR FILTROS

            val filtrosActivos =
                estado.busqueda.isNotBlank() ||
                        estado.categoriaSeleccionada != null ||
                        estado.soloStockBajo

            if (filtrosActivos) {
                TextButton(
                    onClick = {
                        viewModel.limpiarFiltros()
                    }
                ) {
                    Text("Limpiar filtros")
                }
            }

            Text(
                text = "Productos encontrados: ${estado.productos.size}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            // LISTADO DE PRODUCTOS

            if (estado.productos.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            estado.busqueda.isNotBlank() ->
                                "Sin resultados para la búsqueda"

                            estado.soloStockBajo ->
                                "No hay productos con stock bajo"

                            estado.categoriaSeleccionada != null ->
                                "No hay productos en esta categoría"

                            else ->
                                "Aún no hay productos.\nPresiona + para agregar uno."
                        }
                    )
                }

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp),
                    contentPadding =
                        PaddingValues(bottom = 88.dp)
                ) {

                    items(
                        items = estado.productos,
                        key = { it.id }
                    ) { producto ->

                        ProductoItem(
                            modifier = Modifier.animateItem(),
                            producto = producto,
                            onEditar = {
                                onEditar(producto.id)
                            },
                            onEliminar = {
                                productoAEliminar = producto
                            }
                        )
                    }
                }
            }
        }
    }

    // DIALOGO DE SOLICITUD POR CORREO

    if (mostrarSolicitud) {

        val borradorInicial = remember {
            viewModel.generarBorrador(productosBajos)
        }

        SolicitudReposicionDialog(
            borradorInicial = borradorInicial,
            validarDestinatario =
                viewModel::validarDestinatario,
            onCancelar = {
                mostrarSolicitud = false
            },
            onConfirmar = { borrador ->

                mostrarSolicitud = false

                val abierto = CorreoHelper.abrirBorrador(
                    context,
                    borrador.destinatario,
                    borrador.asunto,
                    borrador.cuerpo
                )

                if (!abierto) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "No hay una aplicación de correo instalada"
                        )
                    }
                }
            }
        )
    }

    // CONFIRMAR ELIMINACION DE PRODUCTO

    productoAEliminar?.let { producto ->

        AlertDialog(
            onDismissRequest = {
                productoAEliminar = null
            },
            title = {
                Text("Eliminar producto")
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${producto.nombre}\"?"
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.eliminar(producto)
                        productoAEliminar = null

                        scope.launch {

                            val resultado =
                                snackbarHostState.showSnackbar(
                                    message = "Producto eliminado",
                                    actionLabel = "Deshacer",
                                    duration = SnackbarDuration.Long
                                )

                            if (
                                resultado ==
                                SnackbarResult.ActionPerformed
                            ) {
                                viewModel.restaurar(producto)
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        productoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// TARJETAS DE RESUMEN

@Composable
private fun ResumenCard(
    titulo: String,
    valor: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val valorAnimado by animateIntAsState(
        targetValue = valor,
        label = "contador"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = valorAnimado.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// TARJETA COMPLETA DE PRODUCTO

@Composable
private fun ProductoItem(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {

    val agotado = producto.cantidad == 0
    val bajo = producto.stockBajo
    val requiereAtencion = bajo

    val estadoStock = when {
        agotado -> "Agotado"
        bajo -> "Stock bajo"
        else -> "Disponible"
    }

    // ANIMACION DE COLOR SEGUN STOCK

    val colorTarjeta by animateColorAsState(
        targetValue = if (requiereAtencion) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "colorTarjeta"
    )

    // FORMATO DE PRECIOS EN PESOS CHILENOS

    val formatoPrecio = remember {
        NumberFormat.getCurrencyInstance(
            Locale("es", "CL")
        ).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    // REPOSICION SUGERIDA

    val reposicionSugerida =
        if (
            producto.estado == "Activo" &&
            requiereAtencion
        ) {
            calcularReposicion(producto)
        } else {
            0
        }

    // FORMATO DE FECHAS

    val formatoFecha = remember {
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.forLanguageTag("es-CL")
        )
    }

    val ultimoIngreso =
        producto.fechaUltimoIngreso?.let { fecha ->
            formatoFecha.format(Date(fecha))
        } ?: "Sin ingresos registrados"

    val ultimaSalida =
        producto.fechaUltimaSalida?.let { fecha ->
            formatoFecha.format(Date(fecha))
        } ?: "Sin salidas registradas"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onEditar()
            },
        colors = CardDefaults.cardColors(
            containerColor = colorTarjeta
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text("Código: ${producto.codigo}")

                if (producto.descripcion.isNotBlank()) {
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text("Categoría: ${producto.categoria}")

                Text(
                    "Marca: ${
                        producto.marca.ifBlank {
                            "Sin registrar"
                        }
                    }"
                )

                Text(
                    "Unidad de medida: ${producto.unidadMedida}"
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Stock: ${producto.cantidad}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Stock mínimo: ${producto.stockMinimo}"
                )

                // ALERTA DE EXISTENCIAS

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (requiereAtencion) {

                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = "Existencias: $estadoStock",
                        fontWeight = FontWeight.Bold,
                        color = if (requiereAtencion) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = "Estado del producto: ${producto.estado}"
                )

                Spacer(Modifier.height(4.dp))

                // PRECIOS

                Text(
                    text = "Compra referencial: ${
                        formatoPrecio.format(
                            producto.precioCompra
                        )
                    }"
                )

                Text(
                    text = "Venta referencial: ${
                        formatoPrecio.format(
                            producto.precioVenta
                        )
                    }"
                )

                // REPOSICION

                Text(
                    text = "Reposición sugerida: $reposicionSugerida"
                )

                Spacer(Modifier.height(8.dp))

                // HISTORIAL DE MOVIMIENTOS

                Text(
                    text = "Últimos movimientos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Último ingreso: $ultimoIngreso",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Última salida: $ultimaSalida",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Toca para editar",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ELIMINAR PRODUCTO

            IconButton(
                onClick = onEliminar
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar producto"
                )
            }
        }
    }
}

// DIALOGO DE CORREO PARA REPOSICION

@Composable
private fun SolicitudReposicionDialog(
    borradorInicial: BorradorCorreo,
    validarDestinatario: (String) -> String?,
    onCancelar: () -> Unit,
    onConfirmar: (BorradorCorreo) -> Unit
) {

    var destinatario by remember(borradorInicial) {
        mutableStateOf(borradorInicial.destinatario)
    }

    var asunto by remember(borradorInicial) {
        mutableStateOf(borradorInicial.asunto)
    }

    var cuerpo by remember(borradorInicial) {
        mutableStateOf(borradorInicial.cuerpo)
    }

    var errorDestinatario by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        icon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null
            )
        },
        title = {
            Text("Solicitud de reposición")
        },
        text = {

            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                ),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Revisa y modifica el correo. " +
                            "Se abrirá la aplicación de correo " +
                            "con un borrador. El envío lo haces tú.",
                    style = MaterialTheme.typography.bodySmall
                )

                // DESTINATARIO

                OutlinedTextField(
                    value = destinatario,
                    onValueChange = {
                        destinatario = it
                        errorDestinatario = null
                    },
                    label = {
                        Text("Correo del proveedor")
                    },
                    isError = errorDestinatario != null,
                    supportingText = {
                        errorDestinatario?.let {
                            Text(it)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ASUNTO

                OutlinedTextField(
                    value = asunto,
                    onValueChange = {
                        asunto = it
                    },
                    label = {
                        Text("Asunto")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // MENSAJE

                OutlinedTextField(
                    value = cuerpo,
                    onValueChange = {
                        cuerpo = it
                    },
                    label = {
                        Text("Mensaje de reposición")
                    },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {

            TextButton(
                onClick = {

                    val error =
                        validarDestinatario(destinatario)

                    if (error != null) {
                        errorDestinatario = error
                    } else {
                        onConfirmar(
                            BorradorCorreo(
                                destinatario = destinatario.trim(),
                                asunto = asunto,
                                cuerpo = cuerpo
                            )
                        )
                    }
                }
            ) {
                Text("Abrir correo")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
