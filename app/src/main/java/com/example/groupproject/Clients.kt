package com.example.groupproject

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ClientDetailScreen(client: Client, auth: FirebaseAuth) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = "Client Name: ${client.name}",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
        )
        Text(
            text = "ID: ${client.id}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        HomeSummary(auth, client.id)
        Spacer(modifier = Modifier.height(32.dp))
        ImportantAlertsVisual()
    }
}

@Composable
fun ClientPortfolioScreen(navController: NavController, client: Client, auth: FirebaseAuth) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = "Client Name: ${client.name}",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
        )
        Text(
            text = "ID: ${client.id}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            val id = client.id
            val portfolioItems = remember{ mutableStateListOf<PortfolioData>() }

            LaunchedEffect(id) {
                id?.let { userID ->
                    try {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userID).collection("portfolio")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                portfolioItems.clear()
                                for (document in snapshot) {
                                    val name = document.getString("name") ?: ""
                                    val units = document.getLong("units")?.toInt() ?: 0
                                    if (name != "crear") {
                                        portfolioItems.add(PortfolioData(name, units))
                                    }
                                }

                            }.addOnFailureListener { exception ->
                                Log.e("Firestore", "Error fetching portfolio", exception)
                            }
                    }catch (e: Exception) {
                        Log.e("Firestore", "Error: ${e.message}")
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    HomeSummary(auth, client.id)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cryptos",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                itemsIndexed(portfolioItems.filter { item -> item.name in items.filter { it.type == "crypto" }.map { it.name } }) { index, item ->
                    val originalIndex = items.indexOfFirst { it.name == item.name }
                    Spacer(modifier = Modifier.height(16.dp))
                    portfolioClientItem(item = item, navController = navController, index = originalIndex, id)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Stocks",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                itemsIndexed(portfolioItems.filter { item -> item.name in items.filter { it.type == "stock" }.map { it.name } }) { index, item ->
                    val originalIndex = items.indexOfFirst { it.name == item.name }
                    Spacer(modifier = Modifier.height(16.dp))
                    portfolioClientItem(item = item, navController = navController, index = originalIndex, id)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
fun portfolioClientItem(item: PortfolioData, navController: NavController, index: Int, id: String){

    val db = FirebaseFirestore.getInstance()
    val itemRef = db.collection("items")

    val price = remember { mutableStateOf(0.0) }
    val percentageChange = remember { mutableStateOf(0.0) }

    LaunchedEffect(item.name) {
        itemRef.document(item.name).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    price.value = document.getDouble("price") ?: 0.0
                    percentageChange.value = document.getDouble("percentageChange") ?: 0.0
                }
            } else {
                Log.e("Firestore", "Failed to fetch data for ${item.name}")
            }
        }
    }

    val selectedItem = items.find {it.name == item.name}

    if (selectedItem != null){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = Color.Black)
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = selectedItem.image),
                contentDescription = selectedItem.name,
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .width(80.dp)
            )
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            ) {
                Row() {
                    Text(
                        text = selectedItem.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (item.units > 1) {
                        Text(
                            text = "${item.units} units",
                            color = Color.White,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "${item.units} unit",
                            color = Color.White,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Price: \$${price.value}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Change: ${percentageChange.value}%",
                    color = if (percentageChange.value >= 0) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row() {
                    Button(
                        onClick = {navController.navigate(Screen.BuyForClients.createRoute(id, index))},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green), // Fondo negro
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Buy",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { navController.navigate(Screen.SellForClients.createRoute(id, index))},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Sell",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AssestsClient(navController: NavController, client: Client) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Stock Assests",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(items) { index, item ->
                    if (item.type == "stock") {
                        Spacer(modifier = Modifier.height(16.dp))
                        AssetsRowClient(stock = item, navController = navController, index = index, client)
                    }
                }
            }
        }
    }
}


@Composable
fun AssetsRowClient(stock: Item, navController: NavController, index: Int, client: Client){
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = stock.image),
            contentDescription = stock.name,
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .width(80.dp)
        )
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = stock.name,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Price: \$${stock.price}",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Change: ${stock.percentangeChange}%",
                color = if (stock.percentangeChange >= 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {navController.navigate(Screen.BuyForClients.createRoute(client.id, index))},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Fondo negro
                shape = RoundedCornerShape(16.dp) // Botón redondeado
            ) {
                Text(
                    text = "Buy",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold // Texto en verde y negrita
                )
            }
        }
    }
}
@Composable
fun CryptoClient(navController: NavController, client: Client) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Crypto Assests",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(items) { index, item ->
                    if (item.type == "crypto") {
                        Spacer(modifier = Modifier.height(16.dp))
                        CryptoRowClient(crypto = item, navController = navController, index = index, client)
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoRowClient(crypto: Item, navController: NavController, index: Int, client: Client){
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.White)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = crypto.image),
            contentDescription = crypto.name,
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .width(80.dp)
        )
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = crypto.name,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Price: \$${crypto.price}",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Change: ${crypto.percentangeChange}%",
                color = if (crypto.percentangeChange >= 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {navController.navigate(Screen.BuyForClients.createRoute(client.id, index))},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Buy",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
