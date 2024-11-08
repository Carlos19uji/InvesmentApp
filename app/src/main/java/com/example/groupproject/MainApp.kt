package com.example.groupproject

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun MainApp( auth: FirebaseAuth,
             signInWithGoogle: () -> Unit,
             createAccountWithGoogle: (String) -> Unit) {

    val currentUser = auth.currentUser
    val currentAdminId = currentUser?.uid?:""
    val navController = rememberNavController()
    val isUserLoggedIn = remember { mutableStateOf(false) }
    val isAdminUser = remember { mutableStateOf(false) }
    val selectedClientIndex = remember { mutableStateOf<Int?>(null) }


    Scaffold(
            topBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentScreen in listOf(
                        Screen.Login.route,
                        Screen.CreateAccount.route,
                        Screen.Password.route,
                        Screen.Buy.route,
                        Screen.Support.route,
                        Screen.AddClient.route
                    )
                ) {
                    val title = when (currentScreen) {
                        Screen.Login.route -> "Login"
                        Screen.CreateAccount.route -> "Register"
                        Screen.Password.route -> "Forgot Password"
                        Screen.Buy.route -> "Buy"
                        Screen.Support.route -> "Support"
                        Screen.AddClient.route -> "Add Client"
                        else -> ""
                    }
                    TopNavigationBar(onBackClick = { navController.popBackStack() }, title = title)
                }
                if (currentScreen in listOf(
                        Screen.CorrectLogIn.route,
                        Screen.Crypto.route,
                        Screen.Assets.route,
                        Screen.Portfolio.route,
                        Screen.ClientDetails.route
                    )
                ) {
                    TopNavigationBar2(navController, isAdminUser.value)
                }
            },
            bottomBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentScreen in listOf(
                    Screen.CorrectLogIn.route,
                    Screen.Assets.route,
                    Screen.Crypto.route,
                    Screen.Portfolio.route,
                    Screen.ClientDetails.route
                    )
                ) {
                    BottomNavigationBar(navController, isAdminUser = isAdminUser.value, selectedClientIndex= selectedClientIndex.value)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isUserLoggedIn.value) {
                    if (isAdminUser.value) Screen.CorrectLogIn.route else Screen.FundManagerClients.route
                } else Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    home_screen(onLoginClick = { navController.navigate(Screen.Login.route) },
                        onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) })
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        navController = navController,
                        auth = auth,
                        signInWithGoogle = signInWithGoogle,
                        onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                        onForgotPasswordClick = { navController.navigate(Screen.Password.route) },
                        onUserLoggedIn = { isAdminUser.value = it }
                    )
                }
                composable(Screen.CreateAccount.route) {
                    create_account(
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        navController = navController,
                        auth = auth,
                        createAccountWithGoogle = createAccountWithGoogle,
                        signInWithGoogle = signInWithGoogle
                    )
                }
                composable(Screen.Password.route) {
                    forgot_password(auth, navController)
                }
                composable(Screen.CorrectLogIn.route) {
                    Correct_Log_In_Screen()
                    isUserLoggedIn.value = true
                }
                composable(Screen.Crypto.route) {
                    crypto(navController = navController)
                }
                composable(Screen.Portfolio.route) {
                    portfolio()
                }
                composable(Screen.Assets.route) {
                    assests(navController = navController)
                }
                composable(Screen.Support.route) {
                    support(navController)
                }
                composable(Screen.Buy.route) { backStackEntry ->
                    val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()
                    index?.let {
                        val item = items[index]
                        buy_screen(
                            Name = item.name,
                            Price = item.price,
                            Image = item.image,
                            Percentage = item.percentangeChange
                        )
                    }
                }
                composable(Screen.FundManagerClients.route) {
                    FundManagerClientsScreen(
                        navController,
                        selectedClientIndex,
                        currentAdminId = currentAdminId)
                }
                composable(Screen.ClientDetails.route) { backStackEntry ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        val client = clients[it]
                        ClientDetailScreen(
                            clientName = client.name,
                            clientId = client.id
                        )
                    }
                }
                composable(Screen.AddClient.route) {
                    AddClient(
                        onForgotPasswordClick = { navController.navigate(Screen.Password.route) },
                        navController = navController,
                        auth = auth,
                        currentAdminId = currentAdminId)
                }
            }
        }

}