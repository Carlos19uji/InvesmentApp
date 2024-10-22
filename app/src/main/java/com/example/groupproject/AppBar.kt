package com.example.groupproject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(onBackClick: () -> Unit, title: String) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth().background(Color.Black),
        title = {
            Text(
                text = title,
                color = Color.Black
            )
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Arrow",
                modifier = Modifier.clickable(onClick = onBackClick)
                    .width(50.dp)
                    .height(30.dp)
            )
        }
    )
}

@Composable
fun TopNavigationBar2(onMenuClick: () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color.Gray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.clickable {onMenuClick()}
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Gray),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.CorrectLogIn.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ){
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                )
                Text("Home", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Crypto.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ){
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Crypto",
                )
                Text("Crypto", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Assets.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ){
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "Assests",
                )
                Text("Assests", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Portfolio.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            ){
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Portfolio",
                )
                Text("Porftolio", color = Color.Black)
            }
        }
}

@Composable
fun BottomNavigationBar2(navController: NavController, clientId: String, clientName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                navController.navigate("clientDetail/$clientId/$clientName") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home"
            )
            Text("Home", color = Color.Black)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                navController.navigate("crypto/$clientId/$clientName") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Crypto"
            )
            Text("Crypto", color = Color.Black)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                navController.navigate("assets/$clientId/$clientName") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Assets"
            )
            Text("Assets", color = Color.Black)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                navController.navigate("portfolio/$clientId/$clientName") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Portfolio"
            )
            Text("Portfolio", color = Color.Black)
        }
    }
}
@Composable
fun DrawerContent(navController: NavController, scope: CoroutineScope, drawerState: DrawerState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Menu",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Opción "Buy"
        Text(
            text = "Buy",
            modifier = Modifier
                .clickable {
                    // Cerrar el drawer antes de navegar
                    scope.launch {
                        drawerState.close()
                    }
                    // Navegar a la pantalla de compra
                    navController.navigate(Screen.Buy.route)
                }
                .padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opción "Support"
        Text(
            text = "Support",
            modifier = Modifier
                .clickable {
                    // Cerrar el drawer antes de navegar
                    scope.launch {
                        drawerState.close()
                    }
                    // Navegar a la pantalla de soporte
                    navController.navigate(Screen.Support.route)
                }
                .padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}