package com.example.skinappp.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.skinappp.ui.theme.*
import com.example.skinappp.util.Resource // Make sure to import your Resource class
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // 1. Collect the Resource state from the ViewModel
    val loginState by viewModel.loginState.collectAsState()

    // 2. Derive UI boolean states cleanly from the Resource
    val isLoading = loginState is Resource.Loading
    val isSuccess = loginState is Resource.Success
    val viewModelError = (loginState as? Resource.Error)?.exception?.message

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Local state to hold errors that happen *before* the ViewModel is called (like Google Picker)
    var localAuthError by remember { mutableStateOf<String?>(null) }

    // Combine errors so the UI always has one single string to display
    val displayError = localAuthError ?: viewModelError

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                localAuthError = null // clear local errors
                viewModel.googleSignIn(account)
            } catch (e: ApiException) {
                // Handle Google Picker UI errors locally
                localAuthError = when (e.statusCode) {
                    12500 -> "Developer error: invalid Web Client ID"
                    12501 -> "Sign-in cancelled by user"
                    7     -> "Network error — check your connection"
                    else  -> "Google Sign-In error (code ${e.statusCode})"
                }
            }
        }
    }

    // 3. Navigate when the Resource switches to Success
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(TealPrimary, TealSecondary),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text("SkinAI", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Your AI-powered skin companion", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(40.dp))

            // Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Welcome Back", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Sign in to continue", fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null; localAuthError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = TealPrimary) },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            focusedLabelColor = TealPrimary
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null; localAuthError = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TealPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            focusedLabelColor = TealPrimary
                        )
                    )
                    Spacer(Modifier.height(20.dp))

                    // Error banner
                    AnimatedVisibility(visible = displayError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = displayError ?: "",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                    if (displayError != null) Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            localAuthError = null // Clear old errors
                            emailError = if (email.isBlank()) "Email is required" else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email" else null
                            passwordError = if (password.length < 6) "Password must be at least 6 characters" else null

                            if (emailError == null && passwordError == null) {
                                viewModel.login(email, password)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            localAuthError = null // Clear old errors
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(viewModel.webClientId)
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)

                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("G", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFDB4437))
                        Spacer(Modifier.width(8.dp))
                        Text("Continue with Google", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onNavigateToSignup) {
                        Text("Don't have an account? ", color = TextSecondary)
                        Text("Sign Up", color = TealPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}