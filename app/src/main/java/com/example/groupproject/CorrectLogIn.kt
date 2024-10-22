package com.example.groupproject

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

data class Crypto(val name: String, val image: Int, val price: Double, val percentangeChange : Double)
data class Stock(val name: String, val image: Int, val price: Double, val percentangeChange: Double)

val cryptos = listOf(
    Crypto("Bitcoin", R.drawable.crypto, 38000.0, 3.5),
    Crypto("Ethereum", R.drawable.crypto, 2500.0, -1.2),
    Crypto("Ripple", R.drawable.crypto, 1.2, 4.0),
    Crypto("Litecoin", R.drawable.crypto, 150.0, -0.5)
)

val stocks = listOf(
    Stock("Apple", R.drawable.stock, 150.0, 2.5),
    Stock("Tesla", R.drawable.stock, 720.0, -3.8),
    Stock("Amazon", R.drawable.stock, 3500.0, 1.1),
    Stock("Google", R.drawable.stock, 2800.0, -0.6)
)
@Composable
fun Correct_Log_In_Screen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HomeSummary()
        Spacer(modifier = Modifier.height(16.dp))

        ImportantAlertsVisual()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HomeSummary() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Portfolio Summary", fontSize = 20.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total Value: \$150,000.00",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Stocks: \$85,000.00", color = Color.White)
            Text(text = "Crypto: \$65,000.00", color = Color.White)

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
fun assests() {
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
                itemsIndexed(stocks) { index, stock ->
                    Spacer(modifier = Modifier.height(16.dp))
                    AssetsRow(stock = stock)
                }
            }
        }
    }
}


@Composable
fun AssetsRow(stock: Stock){
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
                onClick = { /* Acción para comprar */ },
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
fun crypto() {
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
                itemsIndexed(cryptos) { index, crypto ->
                    Spacer(modifier = Modifier.height(16.dp))
                    CryptoRow(crypto = crypto)
                }
            }
        }
    }
}

@Composable
fun CryptoRow(crypto: Crypto){
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
                onClick = { /* Acción para comprar */ },
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
fun portfolio(){
    Column(
        Modifier.fillMaxSize().
        background(Color.Gray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.width(300.dp).height(500.dp)
                .background(Color.Black,RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ){
            Column(
                Modifier.fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Total Assests", color = Color.White)

                Text(text = "$150,257.98", color = Color.White)

                Spacer(modifier = Modifier.height(60.dp))

                Text("Distribution", color = Color.White)

                Row(

                ) {
                    Text("Stocks", color = Color.White)
                    Spacer(modifier = Modifier.width(25.dp))
                    Text("$85,250.90", color = Color.White)
                }

                Row(

                ) {
                    Text("Crypto", color = Color.White)
                    Spacer(modifier = Modifier.width(25.dp))
                    Text("62,054.27", color = Color.White)
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

    ) {
        email(emailState)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Message", color = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = message.value,
            onValueChange = { message.value = it },
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (message.value.isEmpty()) Text(text = "You do not put any message", color = Color.LightGray)
                innerTextField()
            }
        )
    }
}

@Composable
fun buy_screen(){

}
