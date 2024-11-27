package com.example.groupprojectapp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth


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
fun NormalClientBar(navController: NavController, auth: FirebaseAuth){
    val expanded = remember { mutableStateOf(false) }
    val showLogOutDialog = remember { mutableStateOf(false) }

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
            DropdownMenuItem(
                text = {Text("Reviews")},
                onClick = {
                    navController.navigate(Screen.Reviews.route)
                    expanded.value = false}
            )
            DropdownMenuItem(
                text = {Text("Log out")},
                onClick = {
                    showLogOutDialog.value = true
                    expanded.value = false
                }
            )
        }
    }
    if (showLogOutDialog.value){
        LogoutConfirmationDialog(
            onConfirm = {
                auth.signOut()
                navController.navigate(Screen.Home.route){
                    popUpTo(Screen.Home.route){ inclusive = true}
                }
                showLogOutDialog.value = false
            },
            onCancel = {
                showLogOutDialog.value = false
            }
        )
    }
}

@Composable
fun LogoutConfirmationDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BottomNavigationBar2(navController: NavController) {
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