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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore




@Composable
fun buy_screen(Name: String, Price: Double, Image: Int, Percentage: Double, auth: FirebaseAuth){

    var quantity by remember { mutableStateOf(1) }
    var totalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(quantity) {
        totalPrice = Price * quantity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(500.dp)
                .width(400.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Purchase $Name",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = Image),
                        contentDescription = Name,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Price per unit: \$${Price}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${"%.2f" .format(Percentage)}%",
                        color = if (Percentage >= 0) Color.Green else Color.Red,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantity > 1) quantity -= 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$quantity",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { quantity += 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Total: \$${"%.2f".format(totalPrice)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Button(
                        onClick = {addItemPortfolio(auth.currentUser?.uid.toString(), Name, quantity)},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Purchase",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun sell_screen(Name: String, Price: Double, Image: Int, Percentage: Double, auth: FirebaseAuth){
    val userID = auth.currentUser?.uid?: return
    val db = FirebaseFirestore.getInstance()
    var unitsState by remember {  mutableStateOf(0) }
    DisposableEffect(Name) {
        val itemRef = db.collection("users").document(userID).collection("portfolio").document(Name)
        val listener = itemRef.addSnapshotListener{ snapshot, e ->
            if (e != null){
                Log.e("sell_screen", "Listen failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                unitsState = snapshot.getLong("units")?.toInt() ?: 0
            } else {
                Log.d("sell_screen", "No such document")
            }

        }
        onDispose{
            listener.remove()
        }
    }
    var quantityToSell by remember { mutableStateOf(1) }
    var totalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(quantityToSell) {
        totalPrice = Price * quantityToSell
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(500.dp)
                .width(400.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Sell $Name",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.width(16.dp))
                if (unitsState > 1) {
                    Text(
                        text = "You have ${unitsState} units",
                        color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }else{
                    Text(
                        text = "You have ${unitsState} unit",
                        color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = Image),
                        contentDescription = Name,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Value per unit: \$${Price}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${"%.2f" .format(Percentage)}%",
                        color = if (Percentage >= 0) Color.Green else Color.Red,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantityToSell > 1) quantityToSell -= 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$quantityToSell",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantityToSell < unitsState) quantityToSell += 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Total: \$${"%.2f".format(totalPrice)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Button(
                        onClick = { removeItemFromPortfolio(auth.currentUser?.uid.toString(), Name, quantityToSell) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Sell",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun buy_for_clients(Name: String, Price: Double, Image: Int, Percentage: Double, auth: FirebaseAuth, clientId: String?){

    var quantity by remember { mutableStateOf(1) }
    var totalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(quantity) {
        totalPrice = Price * quantity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(500.dp)
                .width(400.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Purchase $Name",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = Image),
                        contentDescription = Name,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Price per unit: \$${Price}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${"%.2f" .format(Percentage)}%",
                        color = if (Percentage >= 0) Color.Green else Color.Red,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantity > 1) quantity -= 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$quantity",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { quantity += 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Total: \$${"%.2f".format(totalPrice)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Button(
                        onClick = {if (clientId != null) {addItemPortfolio(clientId, Name, quantity)}},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Purchase",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun sell_for_clients(Name: String, Price: Double, Image: Int, Percentage: Double, auth: FirebaseAuth, clientId: String?){
    val userID = auth.currentUser?.uid?: return
    val db = FirebaseFirestore.getInstance()
    var unitsState by remember {  mutableStateOf(0) }
    DisposableEffect(Name) {
        val itemRef = db.collection("users").document(userID).collection("portfolio").document(Name)
        val listener = itemRef.addSnapshotListener{ snapshot, e ->
            if (e != null){
                Log.e("sell_screen", "Listen failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                unitsState = snapshot.getLong("units")?.toInt() ?: 0
            } else {
                Log.d("sell_screen", "No such document")
            }

        }
        onDispose{
            listener.remove()
        }
    }
    var quantityToSell by remember { mutableStateOf(1) }
    var totalPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(quantityToSell) {
        totalPrice = Price * quantityToSell
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(500.dp)
                .width(400.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Sell $Name",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.width(16.dp))
                if (unitsState > 1) {
                    Text(
                        text = "You have ${unitsState} units",
                        color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }else{
                    Text(
                        text = "You have ${unitsState} unit",
                        color = Color.White,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = Image),
                        contentDescription = Name,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Value per unit: \$${Price}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${"%.2f" .format(Percentage)}%",
                        color = if (Percentage >= 0) Color.Green else Color.Red,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantityToSell > 1) quantityToSell -= 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$quantityToSell",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable { if (quantityToSell < unitsState) quantityToSell += 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.Black,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Total: \$${"%.2f".format(totalPrice)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Button(
                        onClick = { if (clientId != null) { removeItemFromPortfolio(clientId, Name, quantityToSell)} },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Sell",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}