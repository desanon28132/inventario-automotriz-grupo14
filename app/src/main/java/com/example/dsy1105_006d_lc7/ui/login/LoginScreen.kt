package com.example.dsy1105_006d_lc7.ui.login



//HomeScreen



import androidx.compose.foundation.Image

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.material3.TopAppBar

import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale

import com.example.dsy1105_006d_lc7.R

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import androidx.compose.runtime.collectAsState
import com.example.dsy1105_006d_lc7.ui.theme.DSY1105006DLC7Theme
import com.example.dsy1105_006d_lc7.ui.theme.appTopBarColors

@OptIn(ExperimentalMaterial3Api::class)

// Permite usar funciones Material 3 qe son experimentales

@Composable // Genera Interfz Garfica



fun LoginScreen(
    navController: NavController,
    vm: LoginViewModel = viewModel() // Conectamos la arquitectura
){
    val state by vm.uiState.collectAsState()
    var showPass by remember{ mutableStateOf(false) }

    MaterialTheme(

        colorScheme = MaterialTheme.colorScheme

    ){ // inicio Aplicar Material







        Scaffold (

            // Crea Estuctra basica de la pantalla Se define topBar, BottomBar



        ) // fin Scaff

        {// Inicio Inner

                innerPadding ->

            // Representa el espacio interno para que no choque con el topBar



            Column ( //  Colaca los elementos de la Ui

                modifier = Modifier

                    .padding( innerPadding)

                    // Evita que quede oculto

                    .fillMaxSize() // Hace que la columnna tome el todo el tamaño

                    .padding(16.dp)

                    .background(MaterialTheme.colorScheme.background), // gris Claro

                verticalArrangement = Arrangement.spacedBy(20.dp),

                horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente

                //Define que elementos dentro la columna estaran separados por 20.dp

            )// fin column

            {// inicio Contenido


                Image( // insertar una imagen en la interfaz

                    painter=   painterResource(id = R.drawable.logo_puenteauto),

                    contentDescription = "Logo App",

                    modifier = Modifier

                        .fillMaxWidth()

                        .height(200.dp),

                    contentScale = ContentScale.Fit

                    // Ajusta la imagen para que encaje dentro del espacio



                ) // Fin Image





// agregar un espacio entre la imagen y el boton



                Spacer(modifier = Modifier.height(66.dp))






                Text(
                    text = "¡Bienvenido a PuenteAuto!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                )

                OutlinedTextField(
                    value=state.username,
                    onValueChange = vm::onUsernameChange,
                    label={Text("Usuario")},
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = state.error != null,
                    singleLine = true,
                    modifier= Modifier.fillMaxWidth(0.95f)
                )

    OutlinedTextField(
        value=state.password,
        onValueChange = vm::onPasswordChange,
        label={Text("Contraseña")},
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        isError = state.error != null,
        singleLine = true,
        visualTransformation = if (showPass)
            VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = {showPass = !showPass}){
                Icon(
                    if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }

        }, // fin trail

        modifier=Modifier.fillMaxWidth(0.95f)
    )// fin pass

if(state.error!=null){
    Spacer(Modifier.height(8.dp))
    Text(
        text=state.error ?: "",
        color=MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold

    )
}



// agregar un espacio entre la imagen y el boton



                Spacer(modifier = Modifier.height(66.dp))



                Button(onClick = {/* accion futura*/
                    vm.submit { user ->
                        //navController.navigate("muestraDatos/$user")
                        navController.navigate("DrawerMenu/$user")

                        {
                            popUpTo("login") { inclusive = true } // no volver al login
                            launchSingleTop = true
                        }// fin navigate

                    }// fin submit
                },enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                {// aplicacion de propiedad
                    //Text("Presioname")
Text(if(state.isLoading) "Validando" else "Iniciar Sesion"

)

                } // fin boton







            }// fin Contenido



        } // Fin inner





    } // fin Aplicar Material

}// Fin HomeScreen





@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    val vm = LoginViewModel()
    DSY1105006DLC7Theme {
        LoginScreen(navController = navController, vm = vm)
    }
}


// Fin HomeScreen