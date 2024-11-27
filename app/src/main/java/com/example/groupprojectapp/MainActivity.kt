package com.example.groupprojectapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupprojectapp.ui.theme.GroupProjectTheme
import androidx.compose.ui.draw.clip
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.groupprojectapp.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
    object Reviews : Screen("reviews")

    object Buy : Screen("buy_screen/{index}") {
        fun createRoute(index: Int): String {
            return "buy_screen/$index"
        }
    }
    object Sell : Screen("sell_screen/{index}") {
        fun createRoute(index: Int): String {
            return  "sell_screen/$index"
        }
    }
    object CryptoDetails : Screen("crypto_details/{cryptoName}"){
        fun createRoute(cryptoName: String): String {
            return "crypto_details/$cryptoName"
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var navController : NavController
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        Log.d("MainActivity", "Google SignIn Client initialized")

        setContent {
            GroupProjectTheme {
                navController = rememberNavController()
                MainApp(auth, ::signInWithGoogle, ::createAccountWithGoogle, ::logInWithFacebook, ::createAccountWithFacebook)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult called with requestCode: $requestCode") // Log aquí
        callbackManager.onActivityResult(requestCode, resultCode, data) // Facebook callback handling
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("MainActivity", "Google SignIn launcher result received: ${result.resultCode}") // Log aquí
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("MainActivity", "Google sign-in successful, account: ${account.displayName}") // Log aquí
            firebaseAuthWithGoogle(account.idToken!!, navController)
        } catch (e: ApiException) {
            Log.e("MainActivity", "Google sign-in failed: ${e.message}", e)  // Log de error aquí
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, navController: NavController) {
        Log.d("MainActivity", "firebaseAuthWithGoogle called with idToken: $idToken")  // Log aquí
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                Log.d("MainActivity", "Sign in successful, userId: $userId")  // Log aquí
                if (userId != null) {
                    createPortfolioForNewUser(userId)
                    Log.d("MainActivity", "Navigating to Correct Log In screen")
                    navController.navigate(Screen.CorrectLogIn.route) // Navegar a la pantalla correcta
                } else {
                    Log.e("MainActivity", "Authentication failed: Unable to retrieve user ID.")  // Log de error aquí
                    Toast.makeText(this, "Authentication failed: Unable to retrieve user ID.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("MainActivity", "Authentication Failed.")  // Log de error aquí
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signInWithGoogle() {
        Log.d("MainActivity", "signInWithGoogle called")  // Log aquí
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    fun createAccountWithGoogle() {
        Log.d("MainActivity", "createAccountWithGoogle called")  // Log aquí
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }


    fun logInWithFacebook() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(this, listOf("email", "public_profile"))
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken?.token
                if (token != null) {
                    val credential = FacebookAuthProvider.getCredential(token)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this@MainActivity) { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {

                                }
                            } else {
                                Toast.makeText(this@MainActivity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@MainActivity, "Token error: Token is null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Login canceled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@MainActivity, "Login error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun createAccountWithFacebook() {
        val loginManager = LoginManager.getInstance()
        loginManager.logInWithReadPermissions(this, listOf("email", "public_profile"))
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken?.token
                if (token != null) {
                    val credential = FacebookAuthProvider.getCredential(token)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this@MainActivity) { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                        createPortfolioForNewUser(userId)
                                }
                            } else {
                                Toast.makeText(this@MainActivity, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this@MainActivity, "Token error: Token is null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Account creation canceled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@MainActivity, "Account creation error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
fun log_in_facebook(logInWithFacebook: () -> Unit){
    Button(
        onClick = { logInWithFacebook() },
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
fun create_with_facebook(createAccountWithFacebook: () -> Unit){
    Button(
        onClick = { createAccountWithFacebook() },
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
            onClick = {

                signInWithGoogle() },
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

class UserViewModel : ViewModel() {
    var isUserLoggedIn = mutableStateOf(false)
        private set

    fun logIn() {
        isUserLoggedIn.value = true
    }

    fun logOut() {
        isUserLoggedIn.value = false
    }
}