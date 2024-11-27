package com.example.groupprojectapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MainApp( auth: FirebaseAuth,
             signInWithGoogle: () -> Unit,
             createAccountWithGoogle: () -> Unit,
             logInWithFacebook: () -> Unit,
             createAccountWithFacebook: () -> Unit) {

    val navController = rememberNavController()
    val isUserLoggedIn = remember { mutableStateOf(false) }

    val stockViewModel: StockViewModel = viewModel()
    val viewModel: CryptoViewModel = viewModel()

    val stockList by stockViewModel.stockData.observeAsState(emptyList())
    val cryptoItems by viewModel.cryptoData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        stockViewModel.fetchStockPrices()
        viewModel.fetchCryptoPrices()
    }

    Scaffold(
            topBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
                if (currentScreen in listOf(
                        Screen.Login.route,
                        Screen.CreateAccount.route,
                        Screen.Password.route,
                        Screen.Buy.route,
                        Screen.Support.route,
                        Screen.AddClient.route,
                        Screen.Sell.route,
                        Screen.Reviews.route,
                        Screen.CryptoDetails.route
                    )
                ) {
                    val title = when (currentScreen) {
                        Screen.Login.route -> "Login"
                        Screen.CreateAccount.route -> "Register"
                        Screen.Password.route -> "Forgot Password"
                        Screen.Buy.route -> "Buy"
                        Screen.Support.route -> "Support"
                        Screen.Sell.route -> "Sell"
                        Screen.Reviews.route -> "Reviews"
                        Screen.CryptoDetails.route -> "Details"
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
                    NormalClientBar(navController, auth)
                }
            },
            bottomBar = {
                val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route
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
                 Screen.CorrectLogIn.route
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
                        onUserLoggedIn = { isUserLoggedIn.value }
                    )
                }
                composable(Screen.CreateAccount.route) {
                    create_account(
                        onLoginClick = { navController.navigate(Screen.Login.route) },
                        navController = navController,
                        auth = auth,
                        createAccountWithGoogle = createAccountWithGoogle,
                        createAccountWithFacebook = createAccountWithFacebook,
                        onUserLoggedIn = { isUserLoggedIn.value }
                    )
                }
                composable(Screen.Password.route) {
                    forgot_password(auth, navController)
                }
                composable(Screen.CorrectLogIn.route) {
                    Correct_Log_In_Screen(auth)
                }
                composable(Screen.Crypto.route) {
                    crypto(navController = navController, auth)
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
                composable(Screen.Reviews.route){
                    reviews(navController, auth)
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
                            type = item.type,
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
                composable(Screen.CryptoDetails.route){ backStackEntry ->
                    val cryptoName = backStackEntry.arguments?.getString("cryptoName") ?: ""
                    CryptoDetails(navController, cryptoName)
                }
            }
        }

}