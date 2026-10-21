package com.example.dsy1105_006d_lc7.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.ui.login.HomeScreen
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoScreen
import com.example.dsy1105_006d_lc7.ui.movimiento.MovimientoViewModel
import com.example.dsy1105_006d_lc7.ui.producto.ListaProductosScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoFormScreen
import com.example.dsy1105_006d_lc7.ui.producto.ProductoViewModel
import com.example.dsy1105_006d_lc7.view.DrawerMenu

import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorScreen
import com.example.dsy1105_006d_lc7.ui.proveedor.ProveedorViewModel
@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // Compartido entre la lista y el formulario de productos.
    val productoViewModel: ProductoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // Inicio de sesión.
        composable("login") {
            HomeScreen(navController = navController)
        }

        // Menú principal.
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

            DrawerMenu(
                username = username,
                navController = navController
            )
        }

        // Lista de productos.
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

                onVolver = {
                    navController.popBackStack()
                }
            )
        }
        composable("proveedores") {
            val proveedorViewModel: ProveedorViewModel = viewModel()

            ProveedorScreen(
                viewModel = proveedorViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        // Formulario para crear o editar productos.
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

        // Registro de movimientos e historial.
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

    } // Fin de NavHost.

} // Fin de AppNavigation.