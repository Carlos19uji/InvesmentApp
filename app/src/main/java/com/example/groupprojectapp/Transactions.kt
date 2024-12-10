package com.example.groupprojectapp

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.Query
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun transactionsScreen(
    navController: NavController,
    auth: FirebaseAuth
) {
    val userId = auth.currentUser?.uid.orEmpty()
    val context = LocalContext.current

    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Opciones de intervalos
    val intervalOptions = listOf("24 Hours", "7 Days", "1 Month", "1 Year", "Max")
    var selectedInterval by remember { mutableStateOf("24 Hours") }

    // Formato para convertir strings de fecha a Date
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Para mostrar la hora

    // Función para convertir string a Date
    fun stringToDate(dateString: String?): Date? {
        return try {
            dateString?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(userId, selectedInterval) {
        val db = FirebaseFirestore.getInstance()

        Log.d("transactionsScreen", "Fetching transactions for userId: $userId and interval: $selectedInterval")

        db.collection("admins")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val allTransactions = documentSnapshot.get("transactions") as? List<Map<String, Any>>
                    Log.d("transactionsScreen", "All transactions: $allTransactions")

                    if (allTransactions != null) {
                        val now = Date()
                        val filteredTransactions = allTransactions.filter { transaction ->
                            val dateString = transaction["date"] as? String
                            val date = stringToDate(dateString)
                            Log.d("transactionsScreen", "Transaction date: $date")

                            date?.let {
                                when (selectedInterval) {
                                    "24 Hours" -> it.after(Date(now.time - 24 * 60 * 60 * 1000))
                                    "7 Days" -> it.after(Date(now.time - 7 * 24 * 60 * 60 * 1000))
                                    "1 Month" -> it.after(Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time)
                                    "1 Year" -> it.after(Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time)
                                    "Max" -> true
                                    else -> false
                                }
                            } ?: false
                        }

                        // Ordenar las transacciones de más reciente a más antigua
                        val sortedTransactions = filteredTransactions.sortedByDescending {
                            stringToDate(it["date"] as? String)
                        }

                        Log.d("transactionsScreen", "Sorted transactions: $sortedTransactions")

                        transactions = sortedTransactions
                    } else {
                        transactions = emptyList()
                    }
                } else {
                    transactions = emptyList()
                }
            }
            .addOnFailureListener { e ->
                Log.e("transactionsScreen", "Error fetching user document: ${e.message}")
                transactions = emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Interval selection buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            intervalOptions.forEach { interval ->
                Box(
                    modifier = Modifier
                        .background(
                            if (interval == selectedInterval) Color.Blue else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedInterval = interval
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = interval,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
            // Mensaje cuando no hay transacciones para el intervalo seleccionado
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions for the selected interval: $selectedInterval",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            // Transactions list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions) { transaction ->
                    val description = transaction["description"] as? String ?: "No description"
                    val dateString = transaction["date"] as? String
                    val timeString = transaction["time"] as? String
                    val formattedDate = dateString ?: "No date"
                    val formattedTime = timeString ?: "No time"
                    val amount = transaction["amount"] as? Double ?: 0.0

                    // Definir el color del círculo según la descripción
                    val circleColor = when {
                        description.contains("sold", ignoreCase = true) -> Color.Red
                        description.contains("purchased", ignoreCase = true) -> Color.Green
                        else -> Color.Gray
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Círculo colorido
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(circleColor)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = description,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$formattedDate at $formattedTime",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "${String.format("%.2f", amount)}€",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}