package com.example.funds.presentation.pages.userpage

import JWT_TOKEN_KEY
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.datastore.preferences.core.edit
import dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import  androidx.compose.ui.graphics.Color
import com.example.funds.UnsafeOkHttpClient

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    var isRegistration by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val client = remember {
        UnsafeOkHttpClient.getUnsafeClient()
    }

    fun sendAuthRequest() {
        isLoading = true
        errorMessage = null

        val url = if (isRegistration) {
            "https://10.0.2.2:5001/api/User/Register"
        } else {
            "https://10.0.2.2:5001/api/User/authenticate"
        }

        val json = JSONObject().apply {
            if (isRegistration) {
                put("firstName", firstName)
                put("secondName", secondName)
            }
            put("email", email)
            put("password", password)
        }

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json.toString()))
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    isLoading = false
                    val msg = "Network error: ${e.message}"
                    Log.e("AuthRequest", msg, e)
                    errorMessage = msg
                }

                override fun onResponse(call: Call, response: Response) {
                    isLoading = false

                    val code = response.code
                    val headers = response.headers
                    val body = response.body?.string()

                    Log.d("AuthRequest", "HTTP ${code}")
                    Log.d("AuthRequest", "Headers: $headers")
                    Log.d("AuthRequest", "Body: $body")

                    if (!response.isSuccessful || body == null) {
                        try {
                            val jsonError = JSONObject(body ?: "")
                            errorMessage = when {
                                jsonError.has("error") -> jsonError.getString("error")
                                jsonError.has("message") -> jsonError.getString("message")
                                else -> "Unknown error"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Unexpected error: $body"
                        }
                        return
                    }

                    if (isRegistration) {
                        isRegistration = false
                        errorMessage = "Registration successful. Please log in."
                        return
                    }

                    try {
                        val jsonResponse = JSONObject(body)
                        val token = jsonResponse.getString("authToken")

                        CoroutineScope(Dispatchers.IO).launch {
                            context.dataStore.edit { preferences ->
                                preferences[JWT_TOKEN_KEY] = token
                            }
                            onLoginSuccess(token)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthRequest", "Failed to parse token: ${e.message}", e)
                        errorMessage = "Invalid response format"
                    }
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistration) "Register" else "Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isRegistration) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = secondName,
                onValueChange = { secondName = it },
                label = { Text("Second Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(
            onClick = { sendAuthRequest() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = if (isRegistration) "Register" else "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isRegistration = !isRegistration }) {
            Text(
                text = if (isRegistration)
                    "Already have an account? Log in"
                else
                    "Don't have an account? Register",
                color = Color.Black
            )
        }
    }
}
