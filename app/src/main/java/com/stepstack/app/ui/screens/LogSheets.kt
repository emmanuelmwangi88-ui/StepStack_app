package com.stepstack.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Only keeping LogOutdoorSheet as manual steps/walks are hardware-tracked now to prevent cheating.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogOutdoorSheet(onLog: (Int, String) -> Unit) {
    var minutes by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("Cycling") }
    val activities = listOf("Cycling", "Swimming", "Hiking", "Other")

    Column(Modifier.padding(24.dp)) {
        Text("Log Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Track cardio activities that don't involve your phone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            activities.forEach { a ->
                FilterChip(selected = activity == a, onClick = { activity = a }, label = { Text(a) })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it.filter { c -> c.isDigit() } },
            label = { Text("Duration (minutes)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { minutes.toIntOrNull()?.let { onLog(it, activity) } },
            enabled = minutes.toIntOrNull() != null,
            modifier = Modifier.fillMaxWidth().height(56.dp).navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log $activity")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun WalkTimerSheet(
    isWalking: Boolean,
    seconds: Int,
    steps: Int,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Outdoor Walk", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        
        Text(
            com.stepstack.app.ui.components.formatTime(seconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        if (isWalking || seconds > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "%,d steps taken".format(steps),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        if (!isWalking && seconds == 0) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Start Timer", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.width(8.dp))
                Text("Stop & Log Activity", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text(
            "Tracking your duration will contribute to your movement score.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
    }
}
