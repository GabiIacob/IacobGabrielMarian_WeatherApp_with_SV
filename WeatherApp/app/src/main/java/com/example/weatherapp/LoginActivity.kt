package com.example.weatherapp

import LoginResponse
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.weatherapp.network.LoginRequest
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.ui.theme.WeatherAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        val context = LocalContext.current
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.fundal),
                    contentScale = ContentScale.FillBounds
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        loginUser(email, password) { success, email, password, city ->
                            isLoading = false
                            if (success) {
                                navigateToMainApp(context, email, password, city)
                            } else {
                                showLoginError(context)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Completeaza ambele campuri", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Login")
            }
        }
    }

    private fun loginUser(
        email: String,
        password: String,
        callback: (Boolean, String?, String?, String?) -> Unit
    ) {
        RetrofitClient.getApiService().login(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            if (body.status == "success") {
                                callback(true, body.email, password, body.preferredCity)
                            } else {
                                callback(false, null, null, null)
                            }
                        }
                    } else {
                        callback(false, null, null, null)
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    callback(false, null, null, null)
                }
            })
    }

    private fun navigateToMainApp(
        context: android.content.Context,
        email: String?,
        password: String?,
        city: String?
    ) {
        Toast.makeText(context, "Autentificare reusita", Toast.LENGTH_SHORT).show()
        Intent(context, AppActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
            putExtra("preferredCity", city)
            context.startActivity(this)
        }
        finish()
    }

    private fun showLoginError(context: android.content.Context) {
        Toast.makeText(
            context,
            "Autentificare esuata: Verifica credentialele",
            Toast.LENGTH_SHORT
        ).show()
    }
}