package com.example.dsy1105_006d_lc7.ui.producto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.ProductoEntity
import com.example.dsy1105_006d_lc7.data.local.calcularReposicion
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

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
    var menuCategoriasAbierto by remember {
        mutableStateOf(false)
    }
    var productoAEliminar by remember {
        mutableStateOf<ProductoEntity?>(null)
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
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por nombre o código") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuCategoriasAbierto = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Categoría: ${
                            estado.categoriaSeleccionada ?: "Todas"
                        }"
                    )
                }

                DropdownMenu(
                    expanded = menuCategoriasAbierto,
                    onDismissRequest = {
                        menuCategoriasAbierto = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            viewModel.onFiltroCategoriaChange(null)
                            menuCategoriasAbierto = false
                        }
                    )

                    estado.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                viewModel.onFiltroCategoriaChange(categoria)
                                menuCategoriasAbierto = false
                            }
                        )
                    }
                }
            }

            if (
                estado.busqueda.isNotEmpty() ||
                estado.categoriaSeleccionada != null
            ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            if (estado.productos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (
                            estado.busqueda.isBlank() &&
                            estado.categoriaSeleccionada == null
                        ) {
                            "Aún no hay productos.\nPresiona + para agregar uno."
                        } else {
                            "No hay productos que coincidan con los filtros."
                        }
                    )

                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(
                        items = estado.productos,
                        key = { it.id }
                    ) { producto ->
                        ProductoItem(
                            producto = producto,
                            onEditar = { onEditar(producto.id) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = {
                productoAEliminar = null
            },
            title = {
                Text("Eliminar producto")
            },
            text = {
                Text("¿Seguro que quieres eliminar \"${producto.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(producto)
                        productoAEliminar = null
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

@Composable
private fun ProductoItem(
    producto: ProductoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val agotado = producto.cantidad == 0

    val stockBajo = producto.cantidad > 0 &&
            producto.cantidad < producto.stockMinimo

    val requiereAtencion = agotado || stockBajo

    val estadoStock = when {
        agotado -> "Agotado"
        stockBajo -> "Stock bajo"
        else -> "Disponible"
    }

    val formatoPrecio = remember {
        java.text.NumberFormat.getCurrencyInstance(
            java.util.Locale("es", "CL")
        ).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    val reposicionSugerida = if (
        producto.estado == "Activo" && requiereAtencion
    ) {
        calcularReposicion(producto)
    } else {
        0
    }
    val formatoFecha = remember {
        java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            java.util.Locale.forLanguageTag("es-CL")
        )
    }

    val ultimoIngreso = producto.fechaUltimoIngreso?.let { fecha ->
        formatoFecha.format(java.util.Date(fecha))
    } ?: "Sin ingresos registrados"

    val ultimaSalida = producto.fechaUltimaSalida?.let { fecha ->
        formatoFecha.format(java.util.Date(fecha))
    } ?: "Sin salidas registradas"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(
            containerColor = if (requiereAtencion) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                Text("Marca: ${producto.marca.ifBlank { "Sin registrar" }}")
                Text("Unidad de medida: ${producto.unidadMedida}")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Stock: ${producto.cantidad}",
                    fontWeight = FontWeight.Bold
                )

                Text("Stock mínimo: ${producto.stockMinimo}")

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

                        Spacer(modifier = Modifier.width(4.dp))
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

                Text("Estado del producto: ${producto.estado}")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Compra referencial: ${
                        formatoPrecio.format(producto.precioCompra)
                    }"
                )

                Text(
                    "Venta referencial: ${
                        formatoPrecio.format(producto.precioVenta)
                    }"
                )

                Text("Reposición sugerida: $reposicionSugerida")
                Spacer(modifier = Modifier.height(8.dp))

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


                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Toca para editar",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar producto"
                )
            }
        }
    }
}