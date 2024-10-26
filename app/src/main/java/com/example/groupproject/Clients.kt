package com.example.groupproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

data class Client(val name: String, val id: String)

val clients = listOf(
    Client("Client A", "ID123"),
    Client("Cliente B", "ID456"),
    Client("Cliente C", "ID789")
)

@Composable
fun ClientRow(client: Client, onClick: () -> Unit, clientIndex: Int){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable {
               onClick()
            }
            .padding(8.dp)
    ) {
        Text(text = client.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = client.id, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ClientDetailScreen(clientId: String, clientName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = "Cliente: $clientName (ID: $clientId)",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            HomeSummary()
            Spacer(modifier = Modifier.height(16.dp))
            ImportantAlertsVisual()
        }
    }
}
@Composable
fun ClientPortfolioScreen(navController: NavHostController, clientId: String, clientName: String) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        // Mostrar el nombre y el ID del cliente
        Text(
            text = "Cliente: $clientName (ID: $clientId)",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Mostrar el contenido principal de la pantalla Portfolio
        Column(
            modifier = Modifier
                .weight(1f)  // Ocupa el espacio restante
                .padding(16.dp)
        ) {
            portfolio()  // Reutilizamos la función portfolio
        }
    }
}

@Composable
fun ClientAssetsScreen(navController: NavHostController, clientId: String, clientName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        // Mostrar el nombre y el ID del cliente
        Text(
            text = "Cliente: $clientName (ID: $clientId)",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Mostrar el contenido principal de la pantalla Assets
        Column(
            modifier = Modifier
                .weight(1f)  // Ocupa el espacio restante
                .padding(16.dp)
        ) {
            assests()  // Reutilizamos la función assets
        }
    }
}

@Composable
fun ClientCryptoScreen(navController: NavHostController, clientId: String, clientName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = "Cliente: $clientName (ID: $clientId)",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            crypto()
        }
    }
}