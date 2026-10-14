package com.stepstack.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepstack.app.ui.theme.Coral500
import com.stepstack.app.ui.theme.Lime500
import com.stepstack.app.ui.theme.Teal600

object ActivityConverter {
    data class ConversionResult(
        val equivalentSteps: Int,
        val estimatedCalories: Int,
        val metValue: Double,
        val rateDescription: String
    )

    fun convert(activity: String, durationMinutes: Int): ConversionResult {
        val (stepRate, calPerMin, met, desc) = when (activity.lowercase()) {
            "cycling" -> Quadruple(150, 8.5, 6.8, "1 min = 150 step points")
            "swimming" -> Quadruple(180, 10.0, 8.0, "1 min = 180 step points")
            "hiking" -> Quadruple(140, 8.8, 7.0, "1 min = 140 step points")
            "running" -> Quadruple(160, 11.5, 9.8, "1 min = 160 step points")
            "rowing" -> Quadruple(130, 7.5, 6.0, "1 min = 130 step points")
            "elliptical" -> Quadruple(120, 7.0, 5.0, "1 min = 120 step points")
            else -> Quadruple(100, 6.0, 4.5, "1 min = 100 step points")
        }

        val steps = durationMinutes * stepRate
        val calories = (durationMinutes * calPerMin).toInt()

        return ConversionResult(
            equivalentSteps = steps,
            estimatedCalories = calories,
            metValue = met,
            rateDescription = desc
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogOutdoorSheet(onLog: (minutes: Int, activity: String, convertedSteps: Int, calories: Int) -> Unit) {
    var minutesText by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("Cycling") }
    val activities = listOf("Cycling", "Swimming", "Hiking", "Running", "Rowing", "Elliptical", "Other")

    val durationMin = minutesText.toIntOrNull() ?: 0
    val conversion = remember(activity, durationMin) {
        ActivityConverter.convert(activity, durationMin)
    }

    Column(Modifier.padding(24.dp)) {
        Text("Activity Step Converter", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Convert non-phone cardio (Cycling, Swimming, Hiking) into equivalent step points & calories.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            activities.forEach { a ->
                FilterChip(
                    selected = activity == a,
                    onClick = { activity = a },
                    label = { Text(a, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = minutesText,
            onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
            label = { Text("Duration in Minutes") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (durationMin > 0) {
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when (activity.lowercase()) {
                                        "cycling" -> Icons.AutoMirrored.Filled.DirectionsBike
                                        "swimming" -> Icons.Default.Pool
                                        "hiking" -> Icons.Default.Hiking
                                        "running" -> Icons.AutoMirrored.Filled.DirectionsRun
                                        else -> Icons.Default.FitnessCenter
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("$activity Conversion", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            Text(conversion.rateDescription, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Teal600, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Step Points", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "+%,d".format(conversion.equivalentSteps),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Teal600
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = Coral500, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Estimated Burn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "%,d kcal".format(conversion.estimatedCalories),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Coral500
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (durationMin > 0) {
                    onLog(durationMin, activity, conversion.equivalentSteps, conversion.estimatedCalories)
                }
            },
            enabled = durationMin > 0,
            modifier = Modifier.fillMaxWidth().height(56.dp).navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log & Convert $activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

        val mins = seconds / 60
        val secs = seconds % 60
        Text(
            "%02d:%02d".format(mins, secs),
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
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
    }
}
