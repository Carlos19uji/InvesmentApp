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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore




@Composable
fun buy_screen(Name: String, Price: Double, Image: Int, Percentage: Double, type: String, auth: FirebaseAuth){

    var quantity by remember { mutableStateOf(1) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var cryptoCount by remember { mutableStateOf(0) }
    var stockCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var userHasItem by remember { mutableStateOf(false) }

        LaunchedEffect(quantity) {
        totalPrice = Price * quantity
    }
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        userId?.let { id ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(id).collection("portfolio")
                .get()
                .addOnSuccessListener { snapshot ->
                    var cryptoItems = 0
                    var stockItems = 0
                    for (document in snapshot) {
                        val name = document.getString("name") ?: ""
                        if (name != "crear") {
                            val type = items.find { it.name == name }?.type
                            if (type == "crypto") cryptoItems++
                            if (type == "stock") stockItems++
                            if (name == Name){
                                userHasItem = true
                            }
                        }
                    }
                    // Actualizamos los contadores
                    cryptoCount = cryptoItems
                    stockCount = stockItems
                }
                .addOnFailureListener {
                    // Error al obtener el portafolio
                    Log.e("Firestore", "Error getting portfolio items")
                }
        }
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
                        onClick = {
                            if ((type == "crypto" && cryptoCount < 3) || (type == "stock" && stockCount < 10) || userHasItem) {
                                // Si cumple los requisitos o ya tiene el ítem, se realiza la compra
                                addItemPortfolio(auth.currentUser?.uid.toString(), Name, quantity)
                                successMessage = "Purchase Successful!"
                                errorMessage = "" // Limpiar el mensaje de error si la compra es exitosa
                            } else {
                                if (type == "crypto"){
                                    errorMessage = "You cannot buy this item due to the limits (Maximum of 3 different crypto assets)."
                                }else{
                                    errorMessage = "You cannot buy this item due to the limits (Maximum of 10 different technology stocks)."
                                }
                                successMessage = "" // Limpiar el mensaje de éxito si la compra no es posible
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Purchase",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }else if (successMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = successMessage,
                        color = Color.Green,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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
