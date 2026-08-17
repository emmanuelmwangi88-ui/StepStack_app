package com.stepstack.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stepstack.app.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showLoginForm by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val context = androidx.compose.ui.platform.LocalContext.current
    val credentialManager = CredentialManager.create(context)

    fun handleGoogleSignIn() {
        isLoading = true
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                
                if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    auth.signInWithCredential(firebaseCredential).await()
                    onAuthSuccess()
                }
            } catch (e: Exception) {
                errorMessage = "Google sign-in failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showLoginForm) {
            // Landing UI
            LandingUI(
                onLoginClick = { showLoginForm = true },
                onGetStartedClick = onNavigateToOnboarding,
                onGoogleSignIn = { handleGoogleSignIn() }
            )
        } else {
            // Login Form UI (Distinct from Signup)
            LoginUI(
                email = email,
                onEmailChange = { email = it; errorMessage = null },
                password = password,
                onPasswordChange = { password = it; errorMessage = null },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                errorMessage = errorMessage,
                isLoading = isLoading,
                onBack = { showLoginForm = false },
                onLogin = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                    } else {
                        isLoading = true
                        scope.launch {
                            try {
                                auth.signInWithEmailAndPassword(email, password).await()
                                onAuthSuccess()
                            } catch (e: Exception) {
                                errorMessage = e.localizedMessage ?: "Authentication failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LandingUI(onLoginClick: () -> Unit, onGetStartedClick: () -> Unit, onGoogleSignIn: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Hero Background
        Image(
            painter = painterResource(id = R.drawable.stepstacksplashscreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        startY = 300f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Step Stack",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Text(
                "Stack your progress, one step at a time.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text("OR", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LoginUI(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            "Log in to your account to continue your journey",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(Modifier.height(48.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )
        
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onAuthSuccess = {},
        onNavigateToOnboarding = {}
    )
}