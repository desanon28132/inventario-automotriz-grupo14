package com.example.dsy1105_006d_lc7.ui.qr

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors
import com.example.dsy1105_006d_lc7.utils.CameraPermissionHelper

// Escáner "de captura": solo lee el QR y devuelve el texto al formulario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCapturaScreen(
    onCodigoLeido: (String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

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
                title = { Text("Escanear código") },
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
            if (tienePermiso) {
                Escaner(onDetectado = { onCodigoLeido(it.trim()) })
            } else {
                PermisoRequerido(
                    onSolicitar = { launcher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}