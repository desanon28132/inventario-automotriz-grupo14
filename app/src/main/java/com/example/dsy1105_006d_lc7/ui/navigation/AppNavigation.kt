
package com.example.dsy1105_006d_lc7.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// LOGIN Y MENU
import com.example.dsy1105_006d_lc7.ui.login.LoginScreen
import com.example.dsy1105_006d_lc7.ui.menu.MenuScreen

// PRODUCTOS
import com.example.dsy1105_006d_lc7.ui.producto.ListaProductosScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoFormScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoViewModel

// MOVIMIENTOS
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoScreen
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoViewModel

// PROVEEDORES
import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorScreen
import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorViewModel

// UBICACIONES
import com.example.dsy1105_006d_lc7.ui.ubicacion.UbicacionScreen
import com.example.dsy1105_006d_lc7.ui.ubicacion.UbicacionViewModel

// REPOSICION
import com.example.dsy1105_006d_lc7.ui.reposicion.ReposicionScreen
import com.example.dsy1105_006d_lc7.ui.reposicion.ReposicionViewModel

// CODIGOS QR
import com.example.dsy1105_006d_lc7.ui.qr.QrCapturaScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrScannerScreen
import com.example.dsy1105_006d_lc7.ui.qr.QrViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // ViewModel compartido entre productos y QR
    val productoViewModel: ProductoViewModel = viewModel()

    // NAVEGACION CON ANIMACIONES
    NavHost(
        navController = navController,
        startDestination = "login",

        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },

        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },

        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        }

    ) {

        // LOGIN
        composable("login") {

            LoginScreen(
                navController = navController
            )
        }

        // MENU PRINCIPAL
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

        // LISTA DE PRODUCTOS
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

        // FORMULARIO DE PRODUCTOS
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

                // Nueva funcionalidad de Dario:
                // capturar codigo QR desde el formulario
                onEscanearCodigo = {
                    navController.navigate("qr_codigo")
                },

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // MOVIMIENTOS DE INVENTARIO
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

        // PROVEEDORES
        composable("proveedores") {

            val proveedorViewModel: ProveedorViewModel = viewModel()

            ProveedorScreen(
                viewModel = proveedorViewModel,

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // UBICACIONES DE BODEGA
        composable("ubicaciones") {

            val ubicacionViewModel: UbicacionViewModel = viewModel()

            UbicacionScreen(
                viewModel = ubicacionViewModel,

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // SOLICITUDES DE REPOSICION
        composable("reposicion") {

            val reposicionViewModel: ReposicionViewModel = viewModel()

            ReposicionScreen(
                viewModel = reposicionViewModel,

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // ESCANER QR PARA BUSCAR PRODUCTOS
        composable("qr_scanner") {

            val qrViewModel: QrViewModel = viewModel()

            QrScannerScreen(
                viewModel = qrViewModel,

                onVolver = {
                    navController.popBackStack()
                },

                onEditarProducto = { id ->

                    productoViewModel.cargarProducto(id)

                    navController.navigate(
                        "producto_form/$id"
                    )
                },

                onRegistrarProducto = { codigo ->

                    productoViewModel.nuevoProducto()

                    productoViewModel.onCodigoChange(codigo)

                    navController.navigate(
                        "producto_form/0"
                    )
                }
            )
        }

        // CAPTURA QR DESDE FORMULARIO
        composable("qr_codigo") {

            QrCapturaScreen(

                onCodigoLeido = { codigo ->

                    // Colocar el codigo escaneado
                    // directamente en el formulario
                    productoViewModel.onCodigoChange(codigo)

                    navController.popBackStack()
                },

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

    } // FIN NAVHOST

} // FIN APPNAVIGATION
