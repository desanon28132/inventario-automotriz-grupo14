package com.example.dsy1105_006d_lc7.ui.proveedor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorScreen(
    viewModel: ProveedorViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val proveedores by viewModel.proveedores.collectAsState()
    val productos by viewModel.productos.collectAsState()

    val productosPorProveedor = remember(productos) {
        productos.groupBy { it.proveedorId }
    }
    val habilitado = !estado.guardando

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proveedores") },
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (estado.id == 0) {
                            "Nuevo proveedor"
                        } else {
                            "Editar proveedor"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("Utiliza únicamente datos ficticios.")

                    OutlinedTextField(
                        value = estado.codigo,
                        onValueChange = viewModel::onCodigoChange,
                        label = { Text("Código") },
                        singleLine = true,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estado.nombre,
                        onValueChange = viewModel::onNombreChange,
                        label = { Text("Nombre comercial ficticio") },
                        singleLine = true,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estado.correo,
                        onValueChange = viewModel::onCorreoChange,
                        label = { Text("Correo ficticio") },
                        placeholder = {
                            Text("ventas@proveedor.example")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.observaciones,
                        onValueChange = viewModel::onObservacionesChange,
                        label = { Text("Observaciones") },
                        placeholder = {
                            Text("Ejemplo: atiende de lunes a viernes")
                        },
                        minLines = 2,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = estado.condicionesCompra,
                        onValueChange = viewModel::onCondicionesCompraChange,
                        label = { Text("Condiciones referenciales de compra") },
                        placeholder = {
                            Text("Ejemplo: pago al contado y entrega en 3 días")
                        },
                        minLines = 2,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    )


                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = estado.activo,
                            onCheckedChange = viewModel::onActivoChange,
                            enabled = habilitado
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (estado.activo) "Activo"
                            else "Inactivo"
                        )
                    }

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
                        onClick = viewModel::guardar,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (estado.guardando) "Guardando..."
                            else "Guardar proveedor"
                        )
                    }

                    if (estado.id != 0) {
                        OutlinedButton(
                            onClick = viewModel::nuevo,
                            enabled = habilitado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar edición / Nuevo proveedor")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Proveedores registrados",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (proveedores.isEmpty()) {
                item {
                    Text("Todavía no hay proveedores.")
                }
            }

            items(proveedores, key = { it.id }) { proveedor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = habilitado) {
                            viewModel.editar(proveedor)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            proveedor.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Código: ${proveedor.codigo}")
                        Text("Correo: ${proveedor.correo}")
                        Text(
                            text = "Observaciones: ${
                                proveedor.observaciones.ifBlank { "Sin registrar" }
                            }"
                        )

                        Text(
                            text = "Condiciones de compra: ${
                                proveedor.condicionesCompra.ifBlank { "Sin registrar" }
                            }"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val asociados = productosPorProveedor[proveedor.id].orEmpty()

                        Text(
                            text = "Productos asociados (${asociados.size})",
                            style = MaterialTheme.typography.titleSmall
                        )

                        if (asociados.isEmpty()) {
                            Text(
                                text = "Todavía no tiene productos asociados. " +
                                        "Selecciona este proveedor en el formulario de un producto.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            asociados.forEach { producto ->
                                Text(
                                    text = "• ${producto.codigo} — ${producto.nombre}\n" +
                                            "Categoría: ${producto.categoria} · " +
                                            "Stock: ${producto.cantidad} ${producto.unidadMedida}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Text(
                            if (proveedor.activo) "Estado: Activo"
                            else "Estado: Inactivo"
                        )

                        Text(
                            "Toca para editar",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}