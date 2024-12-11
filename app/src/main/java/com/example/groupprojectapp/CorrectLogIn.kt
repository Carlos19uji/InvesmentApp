package com.example.groupprojectapp

import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs


data class Item(val name: String, val image: Int, var price: Double, var percentangeChange : Double, val type: String)

data class PortfolioData(val name: String, val units: Int)

val items = listOf(
    // Cryptocurrencies (10)
    Item("Bitcoin", R.drawable.bitcoin, 38000.0, 3.5, "crypto"),
    Item("Litecoin", R.drawable.litecoin, 150.0, -0.5, "crypto"),
    Item("Cardano", R.drawable.cardano, 200.0, 2.3, "crypto"),
    Item("Polkadot", R.drawable.polkadot, 5.0, -1.8, "crypto"),
    Item("Solana", R.drawable.solana, 90.0, 5.1, "crypto"),
    Item("Ethereum", R.drawable.ethereum, 2500.0, -1.2, "crypto"),
    Item("Ripple", R.drawable.ripple, 1.2, 4.0, "crypto"),
    Item("Dogecoin", R.drawable.dogecoin, 0.25, 3.0, "crypto"),
    Item("Shiba Inu", R.drawable.shibainu, 54.0, 8.7, "crypto"),
    Item("Binance Coin", R.drawable.binancecoin, 500.0, -2.0, "crypto"),

    // Technology Stocks (15)
    Item("Apple", R.drawable.apple, 150.0, 2.5, "stock"),
    Item("Tesla", R.drawable.tesla, 720.0, -3.8, "stock"),
    Item("Amazon", R.drawable.amazon, 3500.0, 1.1, "stock"),
    Item("Google", R.drawable.google, 2800.0, -0.6, "stock"),
    Item("Microsoft", R.drawable.microsoft, 310.0, 2.2, "stock"),
    Item("Meta", R.drawable.meta, 240.0, -1.5, "stock"),
    Item("NVIDIA", R.drawable.nvidia, 290.0, 4.3, "stock"),
    Item("AMD", R.drawable.amd, 140.0, -0.7, "stock"),
    Item("Intel", R.drawable.intel, 50.0, 0.8, "stock"),
    Item("Netflix", R.drawable.netflix, 500.0, 3.1, "stock"),
    Item("Spotify", R.drawable.spotify, 200.0, 1.7, "stock"),
    Item("Salesforce", R.drawable.salesforce, 260.0, -2.4, "stock"),
    Item("Oracle", R.drawable.oracle, 100.0, 2.6, "stock"),
    Item("Shopify", R.drawable.shopify, 1400.0, -1.9, "stock"),
    Item("X", R.drawable.x, 65.0, 0.3, "stock")
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
    val context = LocalContext.current
    var TotalStock by remember { mutableStateOf(0.0) }
    var TotalCrypto by remember { mutableStateOf(0.0) }
    var Total by remember { mutableStateOf(0.0) }
    val portfolioItems = remember { mutableStateListOf<PortfolioData>() }



    val stockViewModel: StockViewModel = viewModel()
    val viewModel: CryptoViewModel = viewModel()

    val cryptoItems by viewModel.cryptoData.observeAsState(emptyList())

    val stockList by stockViewModel.stockData.observeAsState(emptyList())
    LaunchedEffect(Unit) {
        stockViewModel.fetchStockPrices()
        viewModel.fetchCryptoPrices()
    }

    LaunchedEffect(userId, stockList, cryptoItems) {
        userId?.let { uid ->
            try {
                val db = FirebaseFirestore.getInstance()
                val portfolioSnapshot = db.collection("admins").document(uid).collection("portfolio").get().await()
                portfolioItems.clear()

                for (document in portfolioSnapshot) {
                    val name = document.getString("name") ?: ""
                    val units = document.getLong("units")?.toInt() ?: 0
                    if (name.isNotEmpty()) {
                        portfolioItems.add(PortfolioData(name, units))
                    }
                }

                TotalStock = 0.0
                TotalCrypto = 0.0
                Total = 0.0

                for (item in portfolioItems) {
                    val matchingItem = items.find { it.name == item.name}
                    val realTime = stockList.find { it.name == item.name }

                    if (realTime != null && matchingItem != null){
                        val value = realTime.price * item.units
                        when (realTime.type){
                            "stock" -> TotalStock += value
                            "crypto" -> TotalCrypto += value
                        }
                        Total += value
                        if (abs(realTime.percentangeChange) >= 5){
                            val direction = if (realTime.percentangeChange > 0) "increased" else "decreased"
                            val message = "${realTime.name} has $direction by ${"%.2f".format(abs(realTime.percentangeChange))}%"
                            Notification.sendPriceNotification(context, "${realTime.name} Alert", message)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Portfolio Summary", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total Value: ${"%.2f".format(Total)}$",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Stocks Value: ${"%.2f".format(TotalStock)}$", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Crypto Value: ${"%.2f".format(TotalCrypto)}$", color = Color.White, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Today's Change: +2.5%", color = Color.Green, fontSize = 16.sp)

        }
    }
}


@Composable
fun ImportantAlertsVisual() {
    val stockViewModel: StockViewModel = viewModel()
    val viewModel: CryptoViewModel = viewModel()

    // Lista de criptomonedas y acciones tecnológicas
    val cryptoItems by viewModel.cryptoData.observeAsState(emptyList())
    val stockList by stockViewModel.stockData.observeAsState(emptyList())

    // Variables para almacenar la crypto/stock con mayor crecimiento y mayor bajada
    var topCryptoUp by remember { mutableStateOf<Item?>(null) }
    var topCryptoDown by remember { mutableStateOf<Item?>(null) }
    var topStockUp by remember { mutableStateOf<Item?>(null) }
    var topStockDown by remember { mutableStateOf<Item?>(null) }

    // Lógica para determinar el crecimiento y la caída más grande
    LaunchedEffect(cryptoItems, stockList) {
        // Filtrar las criptomonedas y acciones tecnológicas
        val cryptos = cryptoItems.filter { it.type == "crypto" }
        val stocks = stockList.filter { it.type == "stock" }

        // Encontrar la crypto con mayor y menor crecimiento
        topCryptoUp = cryptos.maxByOrNull { it.percentangeChange }
        topCryptoDown = cryptos.minByOrNull { it.percentangeChange }

        // Encontrar la acción con mayor y menor crecimiento
        topStockUp = stocks.maxByOrNull { it.percentangeChange }
        topStockDown = stocks.minByOrNull { it.percentangeChange }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = "Important Alerts", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            // Mostrar alerta de mayor crecimiento en criptomonedas
            topCryptoUp?.let { crypto ->
                val message = if (crypto.percentangeChange >= 0) {
                    "${crypto.name} has increased by ${"%.2f".format(crypto.percentangeChange)}%"
                } else {
                    "${crypto.name} has dropped by ${"%.2f".format(crypto.percentangeChange)}%"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (crypto.percentangeChange >= 0) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar alerta de mayor caída en criptomonedas
            topCryptoDown?.let { crypto ->
                val message = if (crypto.percentangeChange >= 0) {
                    "${crypto.name} has increased by ${"%.2f".format(crypto.percentangeChange)}%"
                } else {
                    "${crypto.name} has dropped by ${"%.2f".format(crypto.percentangeChange)}%"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (crypto.percentangeChange >= 0) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar alerta de mayor crecimiento en acciones tecnológicas
            topStockUp?.let { stock ->
                val message = if (stock.percentangeChange >= 0) {
                    "${stock.name} stocks have increased by ${"%.2f".format(stock.percentangeChange)}%"
                } else {
                    "${stock.name} stocks have dropped by ${"%.2f".format(stock.percentangeChange)}%"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (stock.percentangeChange >= 0) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar alerta de mayor caída en acciones tecnológicas
            topStockDown?.let { stock ->
                val message = if (stock.percentangeChange >= 0) {
                    "${stock.name} stocks have increased by ${"%.2f".format(stock.percentangeChange)}%"
                } else {
                    "${stock.name} stocks have dropped by ${"%.2f".format(stock.percentangeChange)}%"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (stock.percentangeChange >= 0) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun assests(navController: NavController) {

    val stockViewModel: StockViewModel = viewModel()

    val stockList by stockViewModel.stockData.observeAsState(emptyList())

    Log.d("Assets", "Tamaño de la lista de acciones: ${stockList.size}")

    LaunchedEffect(Unit) {
        stockViewModel.fetchStockPrices()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Stock Assets",
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

            if (stockList.isEmpty()) {
                Log.d("Assets", "No se encontraron datos para mostrar.")
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(stockList) { index, item ->
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
            .clickable{ navController.navigate(Screen.StockDetails.createRoute(stock.name)) }
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

            val formattedPrice = if (stock.price < 0.01) {

                stock.price.toString()
            } else {

                "%.2f".format(stock.price)
            }

            Text(
                text = "Price: $formattedPrice$",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Change:  ${"%.2f".format(stock.percentangeChange)}%",
                color = if (stock.percentangeChange >= 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {navController.navigate(Screen.Buy.createRoute(index)) },
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

@Composable
fun crypto(navController: NavController, auth: FirebaseAuth) {

    val viewModel: CryptoViewModel = viewModel()

    val cryptoItems by viewModel.cryptoData.observeAsState(emptyList())
    val id = auth.currentUser?.uid

    LaunchedEffect(id) {
        viewModel.fetchCryptoPrices()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crypto Assets",
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
                itemsIndexed(cryptoItems) { index, item ->
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
            .clickable{ navController.navigate(Screen.CryptoDetails.createRoute(crypto.name)) }
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

            val formattedPrice = if (crypto.price < 0.01) {

                crypto.price.toString()
            } else {

                "%.2f".format(crypto.price)
            }

            Text(
                text = "Price: $formattedPrice$",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Change: ${"%.2f".format(crypto.percentangeChange)}%",
                color = if (crypto.percentangeChange >= 0) Color.Green else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { navController.navigate(Screen.Buy.createRoute(index))},
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

@Composable
fun portfolio(navController: NavController, auth: FirebaseAuth) {
    val id = auth.currentUser?.uid
    val portfolioItems = remember { mutableStateListOf<PortfolioData>() }

    val stockViewModel: StockViewModel = viewModel()

    val stockList by stockViewModel.stockData.observeAsState(emptyList())
    val viewModel: CryptoViewModel = viewModel()


    val cryptoItems by viewModel.cryptoData.observeAsState(emptyList())


    LaunchedEffect(Unit) {
        stockViewModel.fetchStockPrices()
        viewModel.fetchCryptoPrices()
    }

    LaunchedEffect(id, stockList, cryptoItems) {
        id?.let { userID ->
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("admins").document(userID).collection("portfolio")
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
            val realTimeItem = stockList.find { it.name == item.name}
            Spacer(modifier = Modifier.height(16.dp))
            portfolioItem(item = item, navController = navController, index = originalIndex, realTime = realTimeItem)
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
            val realTimeItem = stockList.find { it.name == item.name}
            Spacer(modifier = Modifier.height(16.dp))
            portfolioItem(item = item, navController = navController, index = originalIndex, realTime = realTimeItem)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun portfolioItem(item: PortfolioData, navController: NavController, index: Int, realTime: Item?){

    val price = realTime?.price
    val percentageChange = realTime?.percentangeChange
    val selectedItem = items.find{ it.name == item.name}
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = Color.Black)
                .padding(8.dp)
        ) {
            if (selectedItem != null) {
                Image(
                    painter = painterResource(id = selectedItem.image),
                    contentDescription = selectedItem.name,
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .width(80.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            ) {
                Row() {
                    if (selectedItem != null) {
                        Text(
                            text = selectedItem.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    text = "Price: \$${price}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (percentageChange != null) {
                    Text(
                        text = "Change: ${percentageChange}%",
                        color = if (percentageChange >= 0) Color.Green else Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

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




