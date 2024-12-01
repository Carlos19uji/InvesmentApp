package com.example.groupprojectapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import java.util.Locale

data class Review(
    val userId: String = "",
    val rating: Int = 0,
    val text: String = "",
    var date: Timestamp = Timestamp.now()
)

@Composable
fun support(navController: NavController) {
    val emailState = remember { mutableStateOf("") }
    val subjectState = remember { mutableStateOf("") }
    val messageState = remember { mutableStateOf("") }
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val messageSent = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Text(
            text = "Support",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))


        email(emailState)

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Subject", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = subjectState.value,
            onValueChange = { subjectState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (subjectState.value.isEmpty()) Text(text = "Enter the subject", color = Color.Gray)
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Message", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = messageState.value,
            onValueChange = { messageState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 4.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (messageState.value.isEmpty()) Text(text = "Enter your message", color = Color.Gray)
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (emailState.value.isNotEmpty() &&
                    subjectState.value.isNotEmpty() &&
                    messageState.value.isNotEmpty()
                ) {

                    val messageContent = """
                        From: ${emailState.value}
                        Subject: ${subjectState.value}
                        
                        ${messageState.value}
                    """.trimIndent()


                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("carlos.ripollesmotos@mycit.ie"))
                        putExtra(Intent.EXTRA_SUBJECT, subjectState.value)
                        putExtra(Intent.EXTRA_TEXT, messageContent)
                    }

                    try {
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                        messageSent.value = true
                        errorMessage.value = ""
                    } catch (e: Exception) {
                        errorMessage.value = "Error sending message"
                    }
                } else {
                    errorMessage.value = "All fields are required"
                }
            },
            modifier = Modifier
                .width(200.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Send Message",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        if (messageSent.value) {
            Text(
                text = "Your message has been sent successfully.",
                color = Color.Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (errorMessage.value.isNotEmpty()) {
            Text(
                text = errorMessage.value,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Call Support",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable {
                    showDialog.value = true
                }
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Confirm Call", color = Color.Black) },
            text = { Text("Do you want to call support (+34 601 00 59 83)?", color = Color.Black) },
            confirmButton = {
                Text(
                    text = "Yes",
                    color = Color.Black,
                    modifier = Modifier.clickable {
                        initiateCall(context, "+34601005983")
                        showDialog.value = false
                    }
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    color = Color.Black,
                    modifier = Modifier.clickable {
                        showDialog.value = false
                    }
                )
            }
        )
    }
}

fun initiateCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}

@Composable
fun reviews(navController: NavController, auth: FirebaseAuth) {
    val userId = auth.currentUser?.uid.toString()

    val reviews = remember { mutableStateListOf<Review>() }
    val reviewAddedMessage = remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        userId?.let { userId ->
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("reviews")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        reviews.clear() // Clear previous reviews
                        for (document in snapshot.documents) {
                            val review = document.toObject(Review::class.java)
                            if (review != null) {
                                reviews.add(review)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firestore", "Error fetching reviews", exception)
                    }
            } catch (e: Exception) {
                Log.e("Firestore", "Error: ${e.message}")
            }
        }
    }

    val ratingState = remember { mutableStateOf(0) }
    val reviewState = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(text = "Rate and Review", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= ratingState.value) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star $i",
                    modifier = Modifier
                        .clickable { ratingState.value = i }
                        .size(40.dp),
                    tint = Color.Yellow
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Write a Review", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = reviewState.value,
            onValueChange = { reviewState.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (reviewState.value.isEmpty()) Text(text = "Enter your review", color = Color.Gray)
                innerTextField()
            }
        )

        if (errorMessage.value.isNotEmpty()) {
            Text(
                text = errorMessage.value,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (ratingState.value > 0 && reviewState.value.isNotEmpty()) {
                    val newReview = Review(
                        userId = userId,
                        rating = ratingState.value,
                        text = reviewState.value,
                        date = Timestamp.now()
                    )
                    val db = FirebaseFirestore.getInstance()
                    db.collection("reviews").add(newReview)
                        .addOnSuccessListener {
                            navController.navigate(Screen.Reviews.route)
                        }
                        .addOnFailureListener {
                            reviewAddedMessage.value = "Failed to add review."
                        }
                    ratingState.value = 0
                    reviewState.value = ""
                    errorMessage.value = ""
                } else {
                    errorMessage.value = "Please fill in all fields to submit your review"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(text = "Submit Review", color = Color.Black)
        }

        if (reviewAddedMessage.value.isNotEmpty()) {
            Text(
                text = reviewAddedMessage.value,
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Reviews", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(reviews) { review ->
                ReviewItem(review = review)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(review.date.toDate())
            Text(
                text = "Date: $formattedDate",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )

            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Star $i",
                    tint = Color.Yellow
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = review.text, color = Color.White)
    }
}



