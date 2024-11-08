package com.example.groupproject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun TopNavigationBar2(navController: NavController, isAdminUser: Boolean){
    val expanded = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.clickable {expanded.value = true}
        )
        DropdownMenu (
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false } // Cierra el menú al hacer clic fuera
        ) {
            DropdownMenuItem(
                text = {Text("Support")},
                onClick = {
                    navController.navigate(Screen.Support.route)
                    expanded.value = false}
            )
            if(isAdminUser) {
                DropdownMenuItem(
                    text = { Text("Clients List") },
                    onClick = {
                        navController.navigate(Screen.FundManagerClients.route)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, isAdminUser: Boolean, selectedClientIndex: Int?) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Gray),
            horizontalArrangement = Arrangement.SpaceAround
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (isAdminUser) {
                        selectedClientIndex?.let { index ->
                            navController.navigate(Screen.ClientDetails.createRoute(index)) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    } else{
                        navController.navigate(Screen.CorrectLogIn.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
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

        Text(
            text = "Support",
            modifier = Modifier
                .clickable {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(Screen.Support.route){
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
        )
    }
}
         