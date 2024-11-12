package com.example.groupproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ClientDetailScreen(client: Client) {
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
        HomeSummary()
        Spacer(modifier = Modifier.height(32.dp))
        ImportantAlertsVisual()
    }
}

@Composable
fun ClientPortfolioScreen(navController: NavController, client: Client) {

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
            val db = FirebaseFirestore.getInstance()
            val portfolioItems = remember{ mutableStateListOf<PortfolioData>() }

            LaunchedEffect(Unit) {
                id.let { userID ->
                    db.collection("users").document(userID).collection("portfolio")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            portfolioItems.clear()
                            for (document in snapshot ){
                                val name = document.getString("name")?:""
                                val units = document.getLong("units")?.toInt()?:0
                                if (name != "crear" ) {
                                    portfolioItems.add(PortfolioData(name, units))
                                }
                            }

                        }.addOnFailureListener { exception ->
                            Log.e("Firestore", "Error fetching portfolio", exception)
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
                    HomeSummary()
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

                itemsIndexed(portfolioItems.filter { item -> item.name in items.filter { it.type == "stock" }.map { it.name } }) { index, item ->
                    val originalIndex = items.indexOfFirst { it.name == item.name }
                    Spacer(modifier = Modifier.height(16.dp))
                    portfolioItem(item = item, navController = navController, index = originalIndex)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio
                }
            }
        }
    }
}