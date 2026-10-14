package com.stepstack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepstack.app.ui.components.*
import com.stepstack.app.ui.theme.*
import com.stepstack.app.viewmodel.AppViewModel
import java.time.LocalDate

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@Preview(showBackground = true)
@Composable
fun PreviewStepsScreen() {
    val vm = AppViewModel()
    StepStackTheme {
        StepsScreen(vm = vm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsScreen(
    vm: AppViewModel, 
    padding: PaddingValues = PaddingValues(0.dp),
    onHistoryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val today = vm.todayActivity
    val yesterday = vm.activityFor(vm.today - 1)
    val target = vm.customStepGoal
    val progress = (today.steps.toFloat() / target).coerceIn(0f, 1.2f)
    
    var showGoalDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = UIConstants.ScreenPadding)
            .padding(top = padding.calculateTopPadding().coerceAtLeast(0.dp))
            .padding(bottom = padding.calculateBottomPadding().coerceAtLeast(0.dp) + 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!vm.isBatteryOptimizationIgnored) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                onClick = { vm.requestIgnoreBatteryOptimizations(context) }
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BatteryAlert, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Accuracy Warning", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Battery optimization is skipping steps in background. Tap to fix.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        
        // Large circular progress ring
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val primary = MaterialTheme.colorScheme.primary
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(200.dp)) {
                val sw = 16.dp.toPx()
                drawArc(
                    color = surfaceVariant.copy(alpha = 0.5f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(primary, Lime500, primary)),
                    startAngle = 135f,
                    sweepAngle = 270f * progress.coerceAtMost(1f),
                    useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = primary, modifier = Modifier.size(24.dp))
                Text(
                    "%,d".format(today.steps),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Goal: %,d".format(target),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Comparison Banner
        val diff = today.steps - yesterday.steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (diff >= 0) Color(0xFFE8F5E9) else Color(0xFFFBE9E7))
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (diff >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    null,
                    tint = if (diff >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (diff >= 0) "%,d steps more than yesterday".format(diff) else "%,d steps less than yesterday".format(-diff),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (diff >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Step-to-activity converter
        val remaining = (target - today.steps).coerceAtLeast(0)
        if (remaining > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "%,d steps to go ≈ %d min brisk walk".format(remaining, (remaining / 120).coerceAtLeast(1)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Stat Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val distance = if (vm.useMetric) today.steps * 0.00076f else today.steps * 0.00047f
                val unit = if (vm.useMetric) "km" else "mi"
                StatItem(Icons.Default.Route, "%.2f".format(distance), unit)
                StatItem(Icons.Default.LocalFireDepartment, "%d".format(today.caloriesBurned), "kcal")
                
                val displayMins = if (vm.isWalking) vm.walkSeconds / 60 + today.workoutMinutes else today.workoutMinutes
                StatItem(Icons.Default.Timer, "%d".format(displayMins), "min")
            }
        }

        Spacer(Modifier.height(16.dp))

        // History / Graph section
        Text(
            "Movement History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(start = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(16.dp)) {
                var viewMode by remember { mutableStateOf("Week") } // Day, Week, Month
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Day", "Week", "Month").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = viewMode == label,
                            onClick = { viewMode = label },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                        ) { Text(label) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                
                val chartData = remember(viewMode, vm.activityHistory.size, vm.todayActivity.steps) {
                    when (viewMode) {
                        "Day" -> {
                            val hourly = vm.dayHourlyActivity
                            val labels = List(24) { i -> 
                                when {
                                    i == 0 -> "12a"
                                    i == 6 -> "6a"
                                    i == 12 -> "12p"
                                    i == 18 -> "6p"
                                    i == 23 -> "11p"
                                    else -> ""
                                }
                            }
                            Triple(hourly.toList().map { it.toFloat() }, labels, ChartViewType.DAY)
                        }
                        "Week" -> {
                            val week = vm.weekActivities()
                            val labels = week.map { LocalDate.ofEpochDay(it.epochDay).dayOfWeek.name.take(3) }
                            Triple(week.map { it.steps.toFloat() }, labels, ChartViewType.WEEK)
                        }
                        "Month" -> {
                            val data = vm.last12MonthsActivities()
                            Triple(data.map { it.second }, data.map { it.first }, ChartViewType.MONTH)
                        }
                        else -> Triple(emptyList<Float>(), emptyList<String>(), ChartViewType.WEEK)
                    }
                }

                if (viewMode == "Week") {
                    WeekStrip(vm.weekActivities(), vm.adaptiveTargetScore, vm.today)
                    Spacer(Modifier.height(16.dp))
                }
                
                BarChart(
                    values = chartData.first,
                    labels = chartData.second,
                    viewType = chartData.third,
                    barColor = primary,
                    highlightLast = true
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Streak and History
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (vm.currentStreak > 0) androidx.compose.foundation.BorderStroke(1.dp, Coral500.copy(alpha = 0.3f)) else null
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (vm.currentStreak > 0) Icons.Default.Whatshot else Icons.Default.LocalFireDepartment, 
                        null, tint = Coral500, modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${vm.currentStreak}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(if (vm.currentStreak == 1) "day streak" else "day streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(
                onClick = onHistoryClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Full", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Goal Adjustment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Daily Step Goal", style = MaterialTheme.typography.labelLarge)
                    Text("${(progress * 100).toInt()}% towards goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showGoalDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Adjust")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Health Connect Sync
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CloudSync, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Google Fit / Health Connect", style = MaterialTheme.typography.labelLarge)
                    Text(if (vm.healthConnectLinked) "Synced" else "Last 7 days", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { vm.syncWithGoogleFit(context) }) {
                    Text("Sync Now")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showGoalDialog) {
        AdjustGoalDialog(vm.customStepGoal, {
            vm.updateStepGoal(context, it)
            showGoalDialog = false
        }, { showGoalDialog = false })
    }
}

@Composable
private fun AdjustGoalDialog(current: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var goal by remember { mutableStateOf(current.toFloat()) }
    val primary = MaterialTheme.colorScheme.primary
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Step Goal") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%.0f steps".format(goal), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = primary)
                Slider(
                    value = goal,
                    onValueChange = { goal = it },
                    valueRange = 2000f..20000f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = primary, activeTrackColor = primary)
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(goal.toInt()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
