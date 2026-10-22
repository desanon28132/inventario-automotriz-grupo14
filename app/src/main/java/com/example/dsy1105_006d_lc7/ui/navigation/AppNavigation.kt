package com.example.dsy1105_006d_lc7.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.ui.login.LoginScreen
import com.example.dsy1105_006d_lc7.ui.menu.MenuScreen
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoScreen
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoViewModel
import com.example.dsy1105_006d_lc7.ui.producto.ListaProductosScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoFormScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoViewModel
import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorScreen
import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorViewModel
import com.example.dsy1105_006d_lc7.ui.qr.QrScannerScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrViewModel
import com.example.dsy1105_006d_lc7.ui.reposicion.ReposicionScreen
import com.example.dsy1105_006d_lc7.ui.reposicion.ReposicionViewModel
import com.example.dsy1105_006d_lc7.ui.ubicacion.UbicacionScreen
import com.example.dsy1105_006d_lc7.ui.ubicacion.UbicacionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Compartido entre la lista, el formulario y la navegación desde QR.
    val productoViewModel: ProductoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable(
            route = "DrawerMenu/{username}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments
                ?.getString("username")
                .orEmpty()

            MenuScreen(
                username = username,
                navController = navController
            )
        }

        composable("lista_productos") {
            ListaProductosScreen(
                viewModel = productoViewModel,
                onAgregar = {
                    productoViewModel.nuevoProducto()
                    navController.navigate("producto_form/0")
                },
                onEditar = { id ->
                    productoViewModel.cargarProducto(id)
                    navController.navigate("producto_form/$id")
                },
                onEscanearQr = {
                    navController.navigate("qr_scanner")
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "producto_form/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                }
            )
        ) {
            ProductoFormScreen(
                viewModel = productoViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "movimientos/{username}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments
                ?.getString("username")
                .orEmpty()

            val movimientoViewModel: MovimientoViewModel = viewModel()

            MovimientoScreen(
                viewModel = movimientoViewModel,
                usuario = username,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable("proveedores") {
            val proveedorViewModel: ProveedorViewModel = viewModel()

            ProveedorScreen(
                viewModel = proveedorViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable("ubicaciones") {
            val ubicacionViewModel: UbicacionViewModel = viewModel()

            UbicacionScreen(
                viewModel = ubicacionViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable("reposicion") {
            val reposicionViewModel: ReposicionViewModel = viewModel()

            ReposicionScreen(
                viewModel = reposicionViewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable("qr_scanner") {
            val qrViewModel: QrViewModel = viewModel()

            QrScannerScreen(
                viewModel = qrViewModel,
                onVolver = {
                    navController.popBackStack()
                },
                onEditarProducto = { id ->
                    productoViewModel.cargarProducto(id)
                    navController.navigate("producto_form/$id")
                },
                onRegistrarProducto = { codigo ->
                    productoViewModel.nuevoProducto()
                    productoViewModel.onCodigoChange(codigo)
                    navController.navigate("producto_form/0")
                }
            )
        }
    }
}