package com.example.groupproject

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@Composable
fun MainApp( auth: FirebaseAuth,
             signInWithGoogle: () -> Unit,
             createAccountWithGoogle: (String) -> Unit,
             logInWithFacebook: () -> Unit,
             createAccountWithFacebook: (String) -> Unit) {

    val navController = rememberNavController()
    val isUserLoggedIn = remember { mutableStateOf(false) }
    val isAdminUser = remember { mutableStateOf(false) }
    val selectedClientIndex = remember { mutableStateOf<Int?>(null) }
    val currentAdminId = auth.currentUser?.uid.toString()
    val clients = remember { mutableStateOf<List<Client>>(emptyList()) }


    LaunchedEffect(currentAdminId) {
        if (currentAdminId.isNotEmpty()) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(currentAdminId)
                    .collection("clients")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        clients.value = querySnapshot.documents.mapNotNull { document ->
                            val clientId = document.getString("clientId")
                            val clientName = document.getString("clientName")
                            if (clientId != null && clientName != null) {
                                Client(clientName, clientId)
                            } else {
                                null
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching clients: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("Firestore", "Error: ${e.message}")
            }
        } else {
            Log.w("FundManagerClientsScreen", "currentAdminId is empty or invalid")
        }
    }

    Scaffold(
            topBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentScreen in listOf(
                        Screen.Login.route,
                        Screen.CreateAccount.route,
                        Screen.Password.route,
                        Screen.Buy.route,
                        Screen.BuyForClients.route,
                        Screen.Support.route,
                        Screen.AddClient.route,
                        Screen.Sell.route,
                        Screen.SellForClients.route,
                        Screen.DeleteClient.route
                    )
                ) {
                    val title = when (currentScreen) {
                        Screen.Login.route -> "Login"
                        Screen.CreateAccount.route -> "Register"
                        Screen.Password.route -> "Forgot Password"
                        Screen.Buy.route -> "Buy"
                        Screen.BuyForClients.route -> "Buy"
                        Screen.Support.route -> "Support"
                        Screen.AddClient.route -> "Add Client"
                        Screen.Sell.route -> "Sell"
                        Screen.SellForClients.route -> "Sell"
                        Screen.DeleteClient.route -> "Delete Client"
                        else -> ""
                    }
                    TopNavigationBar(onBackClick = { navController.popBackStack() }, title = title)
                }
                if (currentScreen in listOf(
                        Screen.CorrectLogIn.route,
                        Screen.Crypto.route,
                        Screen.Assets.route,
                        Screen.Portfolio.route
                    )
                ) {
                    NormalClientBar(navController)
                }
                if (currentScreen in listOf(
                        Screen.ClientDetails.route,
                        Screen.ClientPortfolio.route,
                        Screen.CryptoClient.route,
                        Screen.AssetsClient.route
                )){
                    FundAdminBar(navController)
                }
            },
            bottomBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentScreen in listOf(
                        Screen.AssetsClient.route,
                        Screen.CryptoClient.route,
                        Screen.ClientDetails.route,
                        Screen.ClientPortfolio.route
                    )
                ) {
                    BottomNavigationBar(navController, isAdminUser = isAdminUser.value, selectedClientIndex= selectedClientIndex.value)
                }
                if(currentScreen in listOf(
                        Screen.CorrectLogIn.route,
                        Screen.Assets.route,
                        Screen.Crypto.route,
                        Screen.Portfolio.route,
                )){
                    BottomNavigationBar2(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isUserLoggedIn.value) {
                    if (isAdminUser.value) Screen.CorrectLogIn.route else Screen.FundManagerClients.route
                } else {
                    Screen.Home.route
                },
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
                        logInWithFacebook = logInWithFacebook,
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
                        createAccountWithFacebook = createAccountWithFacebook,
                        onUserLoggedIn = { isAdminUser.value = it }
                    )
                }
                composable(Screen.Password.route) {
                    forgot_password(auth, navController)
                }
                composable(Screen.CorrectLogIn.route) {
                    Correct_Log_In_Screen(auth)
                    isUserLoggedIn.value = true
                }
                composable(Screen.Crypto.route) {
                    crypto(navController = navController)
                }
                composable(Screen.Portfolio.route) {
                    portfolio(navController, auth)
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
                            Percentage = item.percentangeChange,
                            auth = auth
                        )
                    }
                }
                composable(Screen.Sell.route) { backStackEntry ->
                    val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()
                    index?.let {
                        val item = items[index]
                        sell_screen(
                            Name = item.name,
                            Price = item.price,
                            Image = item.image,
                            Percentage = item.percentangeChange,
                            auth = auth
                        )
                    }
                }
                composable(Screen.BuyForClients.route) { backStackEntry ->
                    val clientId = backStackEntry.arguments?.getString("clientId")
                    val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()
                    index?.let {
                        val item = items[index]
                        buy_for_clients(
                            Name = item.name,
                            Price = item.price,
                            Image = item.image,
                            Percentage = item.percentangeChange,
                            auth = auth,
                            clientId = clientId
                        )
                    }
                }
                composable(Screen.SellForClients.route) { backStackEntry ->
                    val clientId = backStackEntry.arguments?.getString("clientId")
                    val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()
                    index?.let {
                        val item = items[index]
                        sell_for_clients(
                            Name = item.name,
                            Price = item.price,
                            Image = item.image,
                            Percentage = item.percentangeChange,
                            auth = auth,
                            clientId = clientId
                        )
                    }
                }

                composable(Screen.FundManagerClients.route) {
                    FundManagerClientsScreen(
                        navController,
                        auth,
                        selectedClientIndex = selectedClientIndex.value,
                        onClientSelected = { index -> selectedClientIndex.value = index})
                }
                composable(Screen.ClientDetails.route) { backStackEntry ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        if (it >= 0 && it < clients.value.size) {
                            val client = clients.value[it]
                            ClientDetailScreen(
                                client = client,
                                auth = auth
                            )
                        }
                    }
                }
                composable(Screen.ClientPortfolio.route){ backStackEntry ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        if (it >= 0 && it < clients.value.size) {
                            val client = clients.value[it]
                            ClientPortfolioScreen(
                                navController = navController,
                                client = client,
                                auth = auth
                            )
                        }
                    }
                }
                composable(Screen.AssetsClient.route) { backStackEntry  ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        if (it >= 0 && it < clients.value.size) {
                            val client = clients.value[it]
                            AssestsClient(
                                navController = navController,
                                client = client
                            )
                        }
                    }
                }
                composable(Screen.CryptoClient.route) { backStackEntry  ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        if (it >= 0 && it < clients.value.size) {
                            val client = clients.value[it]
                            CryptoClient(
                                navController = navController,
                                client = client
                            )
                        }
                    }
                }
                composable(Screen.AddClient.route) {
                    AddClient(
                        onForgotPasswordClick = { navController.navigate(Screen.Password.route) },
                        navController = navController,
                        auth = auth)
                }
                composable(Screen.DeleteClient.route){ backStackEntry ->
                    val clientIndex = backStackEntry.arguments?.getString("clientIndex")?.toIntOrNull()
                    clientIndex?.let {
                        if (it >= 0 && it < clients.value.size) {
                            val client = clients.value[it]
                            DeleteUser(navController, client, auth)
                        }
                    }
                }

            }
        }

}