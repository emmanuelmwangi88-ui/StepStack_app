package com.stepstack.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.stepstack.app.data.ActivityLevel
import com.stepstack.app.data.Equipment
import com.stepstack.app.data.GoalType
import com.stepstack.app.data.OnboardingDraft
import com.stepstack.app.R
import com.stepstack.app.ui.theme.Lime500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.stepstack.app.ui.theme.StepStackTheme

@Composable
fun OnboardingScreen(
    onComplete: (OnboardingDraft) -> Unit,
    onBackToAuth: () -> Unit,
    onSkipAuth: (OnboardingDraft) -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var draft by remember { mutableStateOf(OnboardingDraft()) }
    val totalSteps = 7

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (step in 1..5) {
                LinearProgressIndicator(
                    progress = { step.toFloat() / (totalSteps - 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally(tween(350)) { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally(tween(350)) { -it / 3 } + fadeOut())
                },
                label = "onboarding",
                modifier = Modifier.weight(1f),
            ) { s ->
                when (s) {
                    0 -> WelcomeStep(onNext = { step = 1 })
                    1 -> GoalStep(draft.goal) { draft = draft.copy(goal = it); step = 2 }
                    2 -> ActivityStep(draft.activityLevel) { draft = draft.copy(activityLevel = it); step = 3 }
                    3 -> EquipmentStep(draft.equipment) { draft = draft.copy(equipment = it); step = 4 }
                    4 -> BiometricsStep(draft.weight, draft.height) { w, h -> draft = draft.copy(weight = w, height = h); step = 5 }
                    5 -> PermissionsStep(onNext = { step = 6 })
                    6 -> CreateAccountStep(
                        draft = draft,
                        onBack = { step = 5 },
                        onDone = { name, email ->
                            onComplete(draft.copy(name = name, email = email))
                        },
                        onSkip = { onSkipAuth(draft.copy(name = "Guest User", email = "demo@stepstack.app")) },
                        onSignIn = onNavigateToSignIn
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricsStep(initialWeight: Float, initialHeight: Float, onNext: (Float, Float) -> Unit) {
    var weight by remember { mutableStateOf(if (initialWeight > 0) initialWeight.toString() else "70") }
    var height by remember { mutableStateOf(if (initialHeight > 0) initialHeight.toString() else "170") }

    StepScaffold("Body details", "These help us calculate calories and distances accurately.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (kg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Height (cm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Next") {
                onNext(weight.toFloatOrNull() ?: 70f, height.toFloatOrNull() ?: 170f)
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .size(400.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Logo space intentionally left blank as requested
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.stepstacklogo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Step Stack",
            style = MaterialTheme.typography.displayMedium, 
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Unified movement tracking.\nSteps + Workouts in one score.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        Spacer(Modifier.height(48.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ValueRow(Icons.Default.Insights, "Workouts count towards your daily goals")
            ValueRow(Icons.Default.FavoriteBorder, "Adaptive targets that adjust to you")
            ValueRow(Icons.Default.CheckCircle, "Grace days to protect your streak")
        }
        
        Spacer(Modifier.height(48.dp))
        PrimaryButton("Get Started", onNext)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ValueRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StepScaffold(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            title, 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle, 
            style = MaterialTheme.typography.bodyLarge, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        content()
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SelectCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (selected) primary else MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = primary)
            }
        }
    }
}

@Composable
private fun GoalStep(current: GoalType?, onSelect: (GoalType) -> Unit) {
    StepScaffold("What's your goal?", "Choose what matters most to your fitness journey.") {
        GoalType.entries.forEach {
            SelectCard(it.label, it.description, current == it) { onSelect(it) }
        }
    }
}

@Composable
private fun ActivityStep(current: ActivityLevel?, onSelect: (ActivityLevel) -> Unit) {
    StepScaffold("Current activity level", "This helps us set your initial step targets.") {
        ActivityLevel.entries.forEach {
            SelectCard("${it.label} (${it.baselineSteps} steps)", it.description, current == it) { onSelect(it) }
        }
    }
}

@Composable
private fun EquipmentStep(current: Equipment?, onSelect: (Equipment) -> Unit) {
    StepScaffold("Your equipment", "We'll suggest workouts based on what you have available.") {
        Equipment.entries.forEach {
            SelectCard(it.label, it.description, current == it) { onSelect(it) }
        }
    }
}

@Composable
private fun PermissionsStep(onNext: () -> Unit) {
    StepScaffold("Enable tracking", "Step Stack works best when it can automatically track your activity.") {
        PermissionCard(
            Icons.AutoMirrored.Filled.DirectionsRun,
            "Activity Tracking",
            "Required to count your daily steps automatically.",
        )
        Spacer(Modifier.height(16.dp))
        PermissionCard(
            Icons.Default.Sensors,
            "Health Connect",
            "Optionally sync with other fitness apps like Google Fit.",
        )
        Spacer(Modifier.height(16.dp))
        PermissionCard(
            Icons.Default.Lock,
            "Privacy First",
            "Your health data never leaves your device.",
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Allow & Continue", onNext)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Set up manually", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CreateAccountStep(
    draft: OnboardingDraft,
    onBack: () -> Unit,
    onDone: (String, String) -> Unit,
    onSkip: () -> Unit,
    onSignIn: () -> Unit
) {
    var name by remember { mutableStateOf(draft.name) }
    var email by remember { mutableStateOf(draft.email) }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }

        Spacer(Modifier.height(24.dp))
        
        Text(
            "Create Your Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            "Secure your fitness data or continue as a guest.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
        
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        auth.createUserWithEmailAndPassword(email, password).await()
                        onDone(name, email)
                    } catch (e: Exception) {
                        errorMessage = e.localizedMessage ?: "Registration failed"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Join Step Stack", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text("Continue as Guest (Demo Mode)", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Sign In",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSignIn() }
            )
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() {
    StepStackTheme {
        OnboardingScreen(onComplete = {}, onBackToAuth = {}, onSkipAuth = {}, onNavigateToSignIn = {})
    }
}
