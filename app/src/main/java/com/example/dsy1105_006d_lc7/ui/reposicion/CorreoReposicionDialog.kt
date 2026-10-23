package com.example.dsy1105_006d_lc7.ui.reposicion

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.data.local.SolicitudReposicionEntity

@Composable
fun CorreoReposicionDialog(
    solicitud: SolicitudReposicionEntity,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current

    var destinatario by rememberSaveable(solicitud.id) {
        mutableStateOf(solicitud.proveedorCorreo)
    }

    var asunto by rememberSaveable(solicitud.id) {
        mutableStateOf(
            "Solicitud de cotización #${solicitud.id} - Taller PuenteAuto"
        )
    }

    var cuerpo by rememberSaveable(solicitud.id) {
        mutableStateOf(
            "Estimados ${solicitud.proveedorNombre}:\n\n" +
                    "Solicitamos disponibilidad y cotización de los " +
                    "siguientes productos:\n\n" +
                    solicitud.detalle +
                    "\n\nAgradeceremos indicar precios, condiciones de compra " +
                    "y plazo de entrega.\n\n" +
                    "Observaciones: ${
                        solicitud.observaciones.ifBlank { "Sin observaciones adicionales." }
                    }\n\n" +
                    "Saludos cordiales,\n" +
                    "Taller PuenteAuto"
        )
    }

    var error by rememberSaveable(solicitud.id) {
        mutableStateOf<String?>(null)
    }

    var aviso by rememberSaveable(solicitud.id) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text("Correo de reposición")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Solicitud #${solicitud.id} · " +
                            solicitud.proveedorNombre
                )

                OutlinedTextField(
                    value = destinatario,
                    onValueChange = {
                        destinatario = it
                        error = null
                        aviso = null
                    },
                    label = { Text("Destinatario ficticio") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = asunto,
                    onValueChange = {
                        asunto = it
                        error = null
                        aviso = null
                    },
                    label = { Text("Asunto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Puedes modificar los productos y las cantidades " +
                            "directamente en el mensaje.",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = cuerpo,
                    onValueChange = {
                        cuerpo = it
                        error = null
                        aviso = null
                    },
                    label = { Text("Mensaje") },
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Los cambios de este borrador no modifican el stock " +
                            "ni el detalle guardado de la solicitud.",
                    style = MaterialTheme.typography.bodySmall
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                aviso?.let {
                    Text(text = it)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val correo = destinatario.trim()
                    error = null
                    aviso = null

                    val validacion = when {
                        correo.isBlank() ->
                            "Escribe el destinatario."

                        !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                            "Escribe un correo válido."

                        asunto.isBlank() ->
                            "Escribe el asunto."

                        cuerpo.isBlank() ->
                            "Escribe el mensaje."

                        else -> null
                    }

                    if (validacion != null) {
                        error = validacion
                    } else {
                        // Se codifica cada campo para conservar caracteres
                        // especiales, espacios y saltos de línea.
                        val uri = Uri.parse(
                            "mailto:${Uri.encode(correo)}" +
                                    "?subject=${Uri.encode(asunto.trim())}" +
                                    "&body=${Uri.encode(cuerpo)}"
                        )

                        val intent = Intent(Intent.ACTION_SENDTO, uri)

                        try {
                            context.startActivity(intent)

                            aviso = "Se abrió la aplicación de correo. " +
                                    "Revisa el borrador allí. " +
                                    "El estado de la solicitud no ha cambiado."
                        } catch (_: ActivityNotFoundException) {
                            error = "No se encontró una aplicación de correo. " +
                                    "Instala o habilita una y vuelve a intentarlo."
                        } catch (_: SecurityException) {
                            error = "El dispositivo no permitió abrir " +
                                    "la aplicación de correo."
                        }
                    }
                }
            ) {
                Text("Abrir correo")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar")
            }
        }
    )
}