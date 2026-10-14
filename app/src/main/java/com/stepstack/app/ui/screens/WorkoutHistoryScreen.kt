package com.stepstack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stepstack.app.data.WorkoutSession
import com.stepstack.app.ui.components.BarChart
import com.stepstack.app.viewmodel.AppViewModel
import java.time.LocalDate

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(vm: AppViewModel, onBack: () -> Unit) {
    var showLogManual by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showLogManual = true }) {
                        Icon(Icons.Default.Add, "Log Manual")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogManual = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Log Workout") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp, start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val totalMinutes = vm.sessions.sumOf { it.durationActualMin }
                    val totalCalories = vm.sessions.sumOf { it.caloriesBurned }
                    val totalWorkouts = vm.sessions.size

                    HistoryStatChip(label = "Sessions", value = "$totalWorkouts", modifier = Modifier.weight(1f))
                    HistoryStatChip(label = "Total Time", value = "${totalMinutes}m", modifier = Modifier.weight(1f))
                    HistoryStatChip(label = "Calories", value = "$totalCalories", modifier = Modifier.weight(1f))
                }
            }

            item {
                Text("Activity Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Minutes Exercised", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        val week = vm.weekActivities()
                        BarChart(
                            values = week.map { it.workoutMinutes.toFloat() },
                            labels = week.map { LocalDate.ofEpochDay(it.epochDay).dayOfWeek.name.take(1) },
                            barColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Past Sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            if (vm.sessions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No workouts recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(vm.sessions) { session ->
                    WorkoutSessionHistoryCard(session, onDelete = { vm.deleteSession(session) })
                }
            }
        }
    }

    if (showLogManual) {
        ManualWorkoutDialog(
            onDismiss = { showLogManual = false },
            onSave = { name, min, cal ->
                vm.addWorkoutMinutes(
                    context, 
                    min, 
                    WorkoutSession(
                        workoutId = "manual",
                        workoutName = name,
                        dateEpochDay = vm.today,
                        durationActualMin = min,
                        completedPercent = 100,
                        caloriesBurned = cal
                    )
                )
                showLogManual = false
            }
        )
    }
}

@Composable
private fun ManualWorkoutDialog(onDismiss: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("30") }
    var calories by remember { mutableStateOf("200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Workout Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Duration (min)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (kcal)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.ifBlank { "Manual Workout" }, minutes.toIntOrNull() ?: 0, calories.toIntOrNull() ?: 0) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun HistoryStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun WorkoutSessionHistoryCard(session: WorkoutSession, onDelete: () -> Unit) {
    val date = LocalDate.ofEpochDay(session.dateEpochDay)
    val primary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    val imageModel = remember(session.thumbnailUrl) {
        val resId = context.resources.getIdentifier(session.thumbnailUrl, "drawable", context.packageName)
        if (resId != 0) resId else if (session.thumbnailUrl.startsWith("http")) session.thumbnailUrl else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    com.stepstack.app.ui.components.FastImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.FitnessCenter, null, tint = primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(session.workoutName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${session.durationActualMin}m", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = primary)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
                Text("${session.caloriesBurned} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutHistoryScreen() {
    val vm = AppViewModel()
    StepStackTheme {
        WorkoutHistoryScreen(vm = vm, onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutSessionHistoryCard() {
    StepStackTheme {
        Box(Modifier.padding(16.dp)) {
            WorkoutSessionHistoryCard(
                WorkoutSession(
                    workoutId = "1",
                    workoutName = "Morning Strength",
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    durationActualMin = 45,
                    completedPercent = 100,
                    caloriesBurned = 350
                ),
                onDelete = {}
            )
        }
    }
}
