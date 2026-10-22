package com.example.dsy1105_006d_lc7.ui.menu

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    username: String,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú principal") },
                colors = appTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hola, $username",
                style = MaterialTheme.typography.headlineSmall
            )

            NavigationDrawerItem(
                label = { Text("Productos") },
                selected = false,
                onClick = {
                    navController.navigate("lista_productos") {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null
                    )
                }
            )

            NavigationDrawerItem(
                label = { Text("Movimientos e historial") },
                selected = false,
                onClick = {
                    navController.navigate(
                        "movimientos/${Uri.encode(username)}"
                    ) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null
                    )
                }
            )

            NavigationDrawerItem(
                label = { Text("Proveedores") },
                selected = false,
                onClick = {
                    navController.navigate("proveedores") {
                        launchSingleTop = true
                    }
                }
            )

            NavigationDrawerItem(
                label = { Text("Ubicaciones") },
                selected = false,
                onClick = {
                    navController.navigate("ubicaciones") {
                        launchSingleTop = true
                    }
                }
            )

            NavigationDrawerItem(
                label = { Text("Reposición") },
                selected = false,
                onClick = {
                    navController.navigate("reposicion") {
                        launchSingleTop = true
                    }
                }
            )

            NavigationDrawerItem(
                label = { Text("Cerrar sesión") },
                selected = false,
                onClick = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null
                    )
                }
            )
        }
    }
}