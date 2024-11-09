package com.example.groupproject

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Client(val name: String, val id: String)

@Composable
fun FundManagerClientsScreen(
    navController: NavController,
    selectedClientIndex: MutableState<Int?>,
    currentAdminId: String){

    val clients = remember { mutableStateOf<List<Client>>(emptyList()) }

    LaunchedEffect(currentAdminId) {
        if (currentAdminId.isNotEmpty()) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("fundAdministrators")
                    .document(currentAdminId)
                    .collection("clients")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        clients.value = querySnapshot.documents.mapNotNull { document ->
                            val clientId = document.getString("clientId")
                            val clientName = document.getString("clientName")
                            if (clientId != null && clientName != null) {
                                Client(clientId, clientName)
                            } else {
                                null
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching clients: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("Firestore", "Error: ${e.message}")
            }
        } else {
            Log.w("FundManagerClientsScreen", "currentAdminId is empty or invalid")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Clients",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(clientes) { index, client ->
                    Spacer(modifier = Modifier.height(16.dp))
                    ClientRow(client = client,
                        onClick = {selectedClientIndex.value = index
                        navController.navigate(Screen.ClientDetails.createRoute(index))
                        }
                    )
                }
            }
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = {navController.navigate(Screen.AddClient.route)},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .width(150.dp)
                .padding(16.dp)
        ) {
            Text("Add Client", color = Color.White)
        }
    }
}

@Composable
fun ClientRow(client: Client, onClick: () -> Unit){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(text = client.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = client.id, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AddClient(
    onForgotPasswordClick: () -> Unit,
    navController: NavController,
    auth: FirebaseAuth,
    currentAdminId: String) {

        val emailState = remember { mutableStateOf("") }
        val passwordState = remember { mutableStateOf("") }
        val context = LocalContext.current
        val name = remember { mutableStateOf("")  }
        val db = FirebaseFirestore.getInstance()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(30.dp))
            BasicTextField(
                value = name.value,
                onValueChange = { name.value = it },
                modifier = Modifier
                    .width(350.dp)
                    .padding(vertical = 8.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                decorationBox = { innerTextField ->
                    if (name.value.isEmpty()) Text(text = "Client Name", color = Color.LightGray)
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            email(emailState)
            password(passwordState)

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(emailState.value, passwordState.value)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val clientId = task.result?.user?.uid
                                        if (clientId != null) {
                                            checkClientRole(
                                                clientId,
                                                name.value,
                                                currentAdminId,
                                                navController,
                                                context
                                            )
                                        } else {
                                            Toast.makeText(context, "Failed to get client user ID", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show()
                                    }
                                }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(text = "Add client with e-mail", color = Color.Black, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(60.dp))

            TextButton(
                onClick = { onForgotPasswordClick() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Forgot your password?", color = Color(0xFFFFD700), fontSize = 18.sp)
            }
        }
}

fun checkClientRole(
    clientId: String,
    clientName: String,
    adminId: String,
    navController: NavController,
    context: Context
) {
    val user = FirebaseFirestore.getInstance().collection("users").document(clientId)

    user.get().addOnCompleteListener { document ->
        if (document.isSuccessful && document.result != null) {
            val role = document.result?.getString("role")
            Log.d("checkUserRol", "User role fetched: $role")

            if (role != "Fund Administrator") {
                addClientToAdmin(adminId, clientId, clientName, context, navController)
            } else {
                Toast.makeText(context, "Cannot add a Fund Administrator as a client", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error checking role: ${document.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Error checking role: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun addClientToAdmin(adminId: String, clientId: String, clientName: String, context: Context, navController: NavController){
    val db = FirebaseFirestore.getInstance()
    val fundAdminRef = db.collection("fundAdministrators").document(adminId)
    val clientData = hashMapOf(
        "clientId" to clientId,
        "clientName" to clientName
    )
    fundAdminRef.collection("clients").document(clientId).set(clientData)
        .addOnSuccessListener {
            Log.d("addClientToAdmin", "Client added successfully")
            Toast.makeText(context, "Client added successfully", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener { e ->
            Log.e("addClientToAdmin", "Error adding client: ${e.message}")
            Toast.makeText(context, "Error adding client: ${e.message}", Toast.LENGTH_LONG).show()
        }
}





