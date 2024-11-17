package com.example.groupproject

import android.util.Log
import android.widget.Space
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Item(val name: String, val image: Int, val price: Double, val percentangeChange : Double, val type: String)

data class PortfolioData(val name: String, val units: Int)

val items = listOf(
    Item("Bitcoin", R.drawable.bitcoin, 38000.0, 3.5, "crypto"),
    Item("Ethereum", R.drawable.ethereum, 2500.0, -1.2, "crypto"),
    Item("Ripple", R.drawable.ripple, 1.2, 4.0, "crypto"),
    Item("Litecoin", R.drawable.litecoin, 150.0, -0.5, "crypto"),
    Item("Apple", R.drawable.apple, 150.0, 2.5, "stock"),
    Item("Tesla", R.drawable.tesla, 720.0, -3.8, "stock"),
    Item("Amazon", R.drawable.amazon, 3500.0, 1.1, "stock"),
    Item("Google", R.drawable.google, 2800.0, -0.6, "stock"),
)


@Composable
fun Correct_Log_In_Screen(auth: FirebaseAuth) {
    val userId = auth.currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HomeSummary(auth, userId= userId)
        Spacer(modifier = Modifier.height(16.dp))

        ImportantAlertsVisual()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HomeSummary(auth: FirebaseAuth, userId: String?) {
    var TotalStock by remember { mutableStateOf(0.0) }
    var TotalCrypto by remember { mutableStateOf(0.0) }
    var Total by remember { mutableStateOf(0.0) }
    val portfolioItems = remember { mutableStateListOf<PortfolioData>() }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            try {
                val db = FirebaseFirestore.getInstance()
                val portfolioSnapshot = db.collection("users").document(uid).collection("portfolio").get().await()
                portfolioItems.clear()

                for (document in portfolioSnapshot) {
                    val name = document.getString("name") ?: ""
                    val units = document.getLong("units")?.toInt() ?: 0
                    if (name != "crear") {
                        portfolioItems.add(PortfolioData(name, units))
                    }
                }
                TotalStock = 0.0
                TotalCrypto = 0.0
                Total = 0.0
                val itemsSnapshot = db.collection("items").get().await()

                for (document in itemsSnapshot) {
                    val name = document.getString("name") ?: ""
                    val price = document.getLong("price")?.toInt() ?: 0
                    val type = document.getString("type") ?: ""
                    for (item in portfolioItems) {
                        if (item.name == name) {
                            val units = item.units
                            when (type) {
                                "stock" -> {
                                    TotalStock += price * units
                                    Total += price * units
                                }
                                "crypto" -> {
                                    TotalCrypto += price * units
                                    Total += price * units
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Portfolio Summary", fontSize = 20.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total Value: ${Total}$",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Stocks Value: ${TotalStock}$", color = Color.White)
            Text(text = "Crypto Valuea: ${TotalCrypto}$", color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Today's Change: +2.5%", color = Color.Green)
        }
    }
}

@Composable
fun ImportantAlertsVisual() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = "Important Alerts", fontSize = 20.sp, color = Color.White)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bitcoin has dropped by 10%", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tesla stocks are up by 5%", color = Color.Green, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun assests(navController: NavController) {
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
                        AssetsRow(stock = item, navController = navController, index = index)
                    }
                }
            }
        }
    }
}


@Composable
fun AssetsRow(stock: Item, navController: NavController, index: Int){
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
                onClick = {navController.navigate(Screen.Buy.createRoute(index)) },
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
fun crypto(navController: NavController) {
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
                        CryptoRow(crypto = item, navController = navController, index = index)
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoRow(crypto: Item, navController: NavController, index: Int){
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
                onClick = { navController.navigate(Screen.Buy.createRoute(index))},
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
fun portfolio(navController: NavController, auth: FirebaseAuth) {
    val id = auth.currentUser?.uid
    val portfolioItems = remember { mutableStateListOf<PortfolioData>() }

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
                        Log.d("PortfolioScreen", "Fetched portfolio items: ${portfolioItems.size}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firestore", "Error fetching portfolio", exception)
                    }
            } catch (e: Exception) {
                Log.e("Firestore", "Error: ${e.message}")
            }
        }
    }

    // Verifica que portfolioItems tenga datos antes de intentar mostrar
    Log.d("PortfolioScreen", "Portfolio items size: ${portfolioItems.size}")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            HomeSummary(auth, id)
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

        itemsIndexed(portfolioItems.filter { item ->
            item.name in items.filter { it.type == "crypto" }.map { it.name }
        }) { index, item ->
            val originalIndex = items.indexOfFirst { it.name == item.name }
            Spacer(modifier = Modifier.height(16.dp))
            portfolioItem(item = item, navController = navController, index = originalIndex)
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

        itemsIndexed(portfolioItems.filter { item ->
            item.name in items.filter { it.type == "stock" }.map { it.name }
        }) { index, item ->
            val originalIndex = items.indexOfFirst { it.name == item.name }
            Spacer(modifier = Modifier.height(16.dp))
            portfolioItem(item = item, navController = navController, index = originalIndex)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun portfolioItem(item: PortfolioData, navController: NavController, index: Int){

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
                        onClick = { navController.navigate(Screen.Buy.createRoute(index)) },
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
                        onClick = { navController.navigate(Screen.Sell.createRoute(index)) },
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
fun support(navController: NavController) {
    val emailState = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        email(emailState)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Message", color = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = message.value,
            onValueChange = { message.value = it },
            modifier = Modifier
                .width(350.dp)
                .height(200.dp)
                .padding(vertical = 8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (message.value.isEmpty()) Text(text = "You do not put any message", color = Color.Gray)
                innerTextField()
            }
        )
    }
}


