package com.example.groupproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupproject.ui.theme.GroupProjectTheme
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen(val route : String){
    object Home : Screen("home_screen")
    object Login : Screen("login_screen")
    object CreateAccount : Screen("create_account_screen")
    object Password : Screen("password_screen")
    object Crypto : Screen("crypto_screen")
    object Portfolio : Screen("portfolio_screen")
    object Assets : Screen("assets_screen")
    object CorrectLogIn : Screen("correct_login_in")
    object Support : Screen("support")
    object FundManagerClients : Screen("fund_manager_clients")
    object AddClient : Screen("add_client")
    object ClientDetails : Screen("client_details/{clientIndex}") {
        fun createRoute(clientIndex: Int): String{
            return "client_details/$clientIndex"
        }
    }
    object Buy : Screen("buy_screen/{index}") {
        fun createRoute(index: Int): String {
            return "buy_screen/$index"
        }
    }
    object Sell : Screen("sell_screen/{index}") {
        fun createRoute(index: Int): String {
            return "sell_screen/$index"
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var navController : NavController
    private var selectedRoleForAccountCreation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            GroupProjectTheme {
                navController = rememberNavController()
                MainApp(auth, ::signInWithGoogle, ::createAccountWithGoogle)
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        try {
            auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        if (selectedRoleForAccountCreation != null) {
                            selectedRoleForAccountCreation?.let { role ->
                                saveUserRole(userId, role)
                                selectedRoleForAccountCreation = null
                            }
                        }
                        checkUserRol(userId, navController)
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed: Unable to retrieve user ID.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: Exception){
            Log.e("firebaseAuthWithGoogle", "Error during Google sign-in", e)
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    fun createAccountWithGoogle(selectedRole: String) {
        selectedRoleForAccountCreation = selectedRole  // Guardar el rol seleccionado
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun saveUserRole(userId: String, selectedRole: String) {
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(userId)
        val userData = hashMapOf("role" to selectedRole)

        userDoc.set(userData)
            .addOnSuccessListener {
                Log.d("saveUserRole", "User role saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("saveUserRole", "Error saving user role: ${e.message}")
                Toast.makeText(this, "Error saving user role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


fun checkUserRol(userId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userDoc = db.collection("users").document(userId)

    userDoc.get().addOnCompleteListener { document ->
        if (document.isSuccessful && document.result != null) {
            val role = document.result?.getString("role")
            Log.d("checkUserRol", "User role fetched: $role")

            if (role == "Fund Administrator") {
                val fundAdminRef = db.collection("fundAdministrators").document(userId)
                fundAdminRef.collection("clients").get().addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty){
                        Log.d("checkUserRol", "Clients collection is empty, ready to add clients.")
                    }
                }.addOnFailureListener { e ->
                        Log.e("checkUserRol", "Error creating clients collection", e)
                }
                Log.d("checkUserRol", "Navigating to FundManagerClients")
                navController.navigate(Screen.FundManagerClients.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }else{
                Log.d("checkUserRol", "Navigating to CorrectLogIn")
                navController.navigate(Screen.CorrectLogIn.route){
                    popUpTo(Screen.Login.route){inclusive = true}
                }
            }
        } else {
            Log.e("Firestore", "Error getting user role or document doesn't exist")
            Toast.makeText(navController.context, "Error retrieving user role", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Error getting user role", e)

    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun GreetingPreview() {
    GroupProjectTheme {
        Greeting("Android")
    }
}
@Composable
fun log_in_facebook(){
    Button(
        onClick = { "Accion iniciar sesion con Facebook" },
        colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF4267B2)),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .width(300.dp)
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Image(
                painter = painterResource(id = R.drawable.facebook),
                contentDescription = "Facebook Logo",
                modifier = Modifier.size(24.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(25.dp))

            Text(text = "Continue with Facebook", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun log_in_google(signInWithGoogle: () -> Unit){
        Button(
            onClick = { signInWithGoogle() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(30.dp))
                Text(text = "Continue with Google", color = Color.Black, fontSize = 18.sp)

            }
    }
}

@Composable
fun create_with_google(onGoogleSignInClick: () -> Unit){
    Button(
        onClick = { onGoogleSignInClick() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .width(300.dp)
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google logo",
                modifier = Modifier.size(24.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(30.dp))
            Text(text = "Continue with Google", color = Color.Black, fontSize = 18.sp)

        }
    }
}

@Composable
fun email(emailState: MutableState<String>){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mail),
            contentDescription = "Mail",
            modifier = Modifier.size(35.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        BasicTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (emailState.value.isEmpty()) Text(text = "Email", color = Color.LightGray)
                innerTextField()
            }
        )
    }
}

@Composable
fun password(passwordState: MutableState<String>){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.padlock),
            contentDescription = "padlock",
            modifier = Modifier.size(35.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        BasicTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (passwordState.value.isEmpty()) Text(text = "Password", color = Color.LightGray)
                innerTextField()
            }
        )
    }
}