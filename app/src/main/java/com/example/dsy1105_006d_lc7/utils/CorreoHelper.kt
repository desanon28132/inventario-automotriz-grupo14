package com.example.dsy1105_006d_lc7.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

// Recurso nativo: cliente de correo del dispositivo.
// Solo abre un borrador; el envío queda bajo control del usuario.
// No requiere permisos ni almacena credenciales.
object CorreoHelper {

    // Devuelve false si el dispositivo no tiene ninguna app de correo
    fun abrirBorrador(
        context: Context,
        destinatario: String,
        asunto: String,
        cuerpo: String
    ): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
