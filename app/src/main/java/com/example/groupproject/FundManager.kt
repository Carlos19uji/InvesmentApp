package com.example.groupproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun FundManagerClientsScreen(navController: NavHostController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp)
    ) {
        Text(
            text = "My Clients",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(clients){ index, client ->
                Spacer(modifier = Modifier.height(16.dp))
                ClientRow(client = client,
                    onClick = {navController.navigate(Screen.ClientDetails.createRoute(index))},
                    clientIndex = index)
            }
        }
    }
}


@Composable
fun NavigationComponent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "fundManagerClients") {
        composable("fundManagerClients") {
            FundManagerClientsScreen(navController)
        }
        composable(
            route = "clientDetail/{clientId}/{clientName}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType },
                navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            ClientDetailScreen(clientId = clientId, clientName = clientName)
        }
        composable(
            route = "portfolio/{clientId}/{clientName}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType },
                navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            ClientPortfolioScreen(navController = navController, clientId = clientId, clientName = clientName)
        }
        composable(
            route = "assets/{clientId}/{clientName}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType },
                navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            ClientAssetsScreen(navController = navController, clientId = clientId, clientName = clientName)
        }
        composable(
            route = "crypto/{clientId}/{clientName}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType },
                navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            ClientCryptoScreen(navController = navController, clientId = clientId, clientName = clientName)
        }
    }
}





