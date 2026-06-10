package com.example.skinappp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    // Map the Resource state to UI variables
    val isLoading = loginState is Resource.Loading
    val isSuccess = loginState is Resource.Success
    val apiError = (loginState as? Resource.Error)?.exception?.message

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Trigger navigation when the ViewModel state becomes Success
    LaunchedEffect(isSuccess) {
        if (isSuccess) onSignupSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(
                Brush.linearGradient(
                    listOf(TealPrimary, TealSecondary),
                    Offset(0f, 0f), Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("  Join SkinAI today", fontSize = 14.sp, color = Color.White.copy(0.85f), modifier = Modifier.align(Alignment.Start).padding(start = 20.dp))
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    @Composable
                    fun Field(value: String, onValue: (String) -> Unit, label: String, icon: @Composable () -> Unit, error: String?, keyboard: KeyboardType = KeyboardType.Text, isPassword: Boolean = false) {
                        var vis by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = value, onValueChange = onValue, label = { Text(label) },
                            leadingIcon = icon,
                            trailingIcon = if (isPassword) { { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } } } else null,
                            visualTransformation = if (isPassword && !vis) PasswordVisualTransformation() else VisualTransformation.None,
                            isError = error != null, supportingText = error?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary, focusedLabelColor = TealPrimary)
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    Field(fullName, { fullName = it; fullNameError = null }, "Full Name", { Icon(Icons.Default.Person, null, tint = TealPrimary) }, fullNameError)
                    Field(email, { email = it; emailError = null }, "Email Address", { Icon(Icons.Default.Email, null, tint = TealPrimary) }, emailError, KeyboardType.Email)
                    Field(phone, { phone = it }, "Phone (Optional)", { Icon(Icons.Default.Phone, null, tint = TealPrimary) }, null, KeyboardType.Phone)
                    Field(password, { password = it; passwordError = null }, "Password", { Icon(Icons.Default.Lock, null, tint = TealPrimary) }, passwordError, KeyboardType.Password, true)

                    // Error banner
                    AnimatedVisibility(visible = apiError != null) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                            Text(apiError ?: "", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                    if (apiError != null) Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            fullNameError = if (fullName.isBlank()) "Full name is required" else null
                            emailError = if (email.isBlank()) "Email is required" else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email" else null
                            passwordError = if (password.length < 6) "Password must be at least 6 characters" else null

                            if (fullNameError == null && emailError == null && passwordError == null) {
                                viewModel.register(email, password, fullName, phone.ifBlank { null })
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Already have an account? ", color = TextSecondary)
                        Text("Sign In", color = TealPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}