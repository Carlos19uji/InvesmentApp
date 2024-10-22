package com.example.groupproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupproject.ui.theme.GroupProjectTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

sealed class Screen(val route : String, val title: String){
    object Home : Screen("home_screen", "Home")
    object Login : Screen("login_screen", "Log in")
    object CreateAccount : Screen("create_account_screen", "Register")
    object Password : Screen("password_screen", "Forgot Password")
    object Crypto : Screen("crypto_screen", "")
    object Portfolio : Screen("portfolio_screen", "")
    object Assets : Screen("assets_screen", "")
    object CorrectLogIn : Screen("correct_lon_in", "")
    object Support : Screen("support", "")
    object Buy : Screen("buy", "")
    object FundManagerClients : Screen("fund_manager_clients", "My Clients")
    object ClientDetails : Screen("client_detail/{clientIndex}", "Client Details") {
        fun createRoute(clientIndex: Int): String{
            return "clientDetails/$clientIndex"
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            GroupProjectTheme {
                navController = rememberNavController()
                MainApp(auth, googleSignInClient, ::signInWithGoogle)
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
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Signed in with Google", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.CorrectLogIn.route){
                    popUpTo(Screen.Login.route) {inclusive = true}
                }

            } else {
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
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
            onClick = { signInWithGoogle()},
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