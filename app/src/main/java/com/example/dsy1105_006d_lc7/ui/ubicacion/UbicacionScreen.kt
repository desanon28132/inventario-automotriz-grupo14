package com.example.dsy1105_006d_lc7.ui.ubicacion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbicacionScreen(
    viewModel: UbicacionViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val ubicaciones by viewModel.ubicaciones.collectAsState()
    val habilitado = !estado.guardando

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicaciones") },
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
                            "Nueva ubicación"
                        } else {
                            "Editar ubicación"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (estado.id != 0) {
                        Text(
                            "Los cambios de esta ficha se verán en todos " +
                                    "los productos asociados. Para trasladar un " +
                                    "producto, cambia su ubicación desde Productos."
                        )
                    }

                    CampoUbicacion(
                        "Código", estado.codigo,
                        viewModel::onCodigoChange, habilitado
                    )

                    CampoUbicacion(
                        "Zona o bodega", estado.zona,
                        viewModel::onZonaChange, habilitado
                    )

                    CampoUbicacion(
                        "Estante", estado.estante,
                        viewModel::onEstanteChange, habilitado
                    )

                    CampoUbicacion(
                        "Repisa", estado.repisa,
                        viewModel::onRepisaChange, habilitado
                    )

                    CampoUbicacion(
                        "Posición", estado.posicion,
                        viewModel::onPosicionChange, habilitado
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
                        onClick = viewModel::guardar,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (estado.guardando) "Guardando..."
                            else "Guardar ubicación"
                        )
                    }

                    if (estado.id != 0) {
                        OutlinedButton(
                            onClick = viewModel::nuevo,
                            enabled = habilitado,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar edición / Nueva ubicación")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Ubicaciones registradas",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (ubicaciones.isEmpty()) {
                item {
                    Text("Todavía no hay ubicaciones.")
                }
            }

            items(ubicaciones, key = { it.id }) { ubicacion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = habilitado) {
                            viewModel.editar(ubicacion)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            ubicacion.codigo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Zona: ${ubicacion.zona}")
                        Text("Estante: ${ubicacion.estante}")
                        Text("Repisa: ${ubicacion.repisa}")
                        Text("Posición: ${ubicacion.posicion}")

                        Text(
                            "Actualizada: ${
                                formatoFecha.format(
                                    Date(ubicacion.fechaActualizacion)
                                )
                            }"
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

@Composable
private fun CampoUbicacion(
    titulo: String,
    valor: String,
    onChange: (String) -> Unit,
    habilitado: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(titulo) },
        singleLine = true,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth()
    )
}