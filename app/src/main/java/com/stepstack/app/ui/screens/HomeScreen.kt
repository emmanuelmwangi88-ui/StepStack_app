package com.stepstack.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.ui.components.*
import com.stepstack.app.ui.theme.*
import com.stepstack.app.viewmodel.AppViewModel
import com.stepstack.app.viewmodel.WorkoutViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val vm = AppViewModel()
    val wvm = WorkoutViewModel()
    StepStackTheme {
        HomeScreen(
            vm = vm,
            workoutVm = wvm,
            allWorkouts = SampleData.workouts,
            onStartQuickWorkout = {},
            onOpenWorkout = {},
            onOpenBuilder = {},
            onViewReport = {},
            onSearch = {},
            onOpenSteps = {},
            onLogOutdoor = {},
            onStartTimer = {},
            onToggleFavorite = {}
        )
    }
}

private fun greeting(name: String): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val base = when {
        h < 5 -> "Burning the midnight oil \uD83C\uDF19"
        h < 12 -> "Rise and shine \u2600\uFE0F"
        h < 17 -> "Keep pushing \uD83D\uDCAA"
        h < 21 -> "Good evening \uD83C\uDF05"
        else -> "Unwinding \u2728"
    }
    return "$base ${name.ifBlank { "Athlete" }}!"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    workoutVm: WorkoutViewModel,
    allWorkouts: List<Workout>,
    padding: PaddingValues = PaddingValues(0.dp),
    onStartQuickWorkout: () -> Unit,
    onOpenWorkout: (Workout) -> Unit,
    onOpenBuilder: () -> Unit,
    onViewReport: () -> Unit,
    onSearch: (String) -> Unit,
    onOpenSteps: () -> Unit,
    onLogOutdoor: () -> Unit,
    onStartTimer: () -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val today = vm.todayActivity
    val target = vm.adaptiveTargetScore
    val suggested = suggestedWorkout(vm, allWorkouts)
    val context = LocalContext.current
    
    val lastActiveDay = vm.activityHistory.filter { it.movementScore > 0 }.maxByOrNull { it.epochDay }?.epochDay ?: 0L
    val isWelcomeBack = vm.today - lastActiveDay >= 5 && lastActiveDay != 0L

    var showTargetInfo by remember { mutableStateOf(false) }
    var selectedDayInfo by remember { mutableStateOf<DailyActivity?>(null) }
    val favoriteIds by workoutVm.favoriteIds.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = UIConstants.ScreenPadding)
                .padding(top = padding.calculateTopPadding().coerceAtLeast(0.dp))
                .padding(bottom = padding.calculateBottomPadding().coerceAtLeast(0.dp) + 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Welcome Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (vm.user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = vm.user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        if (isWelcomeBack) "Welcome back, ${vm.user.name.ifBlank { "Athlete" }}!" else greeting(vm.user.name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        if (isWelcomeBack) "Take it easy today. We've set a smaller target to help you get back in the flow." 
                        else remember { java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                
                // Streak & Grace Days Badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { /* Could show streak details */ }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, null, tint = Coral500, modifier = Modifier.size(16.dp))
                        Text(" ${vm.currentStreak} ", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(4.dp))
                        repeat(3) { i ->
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (i < vm.graceDaysRemaining) Teal600 else MaterialTheme.colorScheme.outlineVariant)
                            )
                            if (i < 2) Spacer(Modifier.width(2.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Unified Score Ring
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth()) {
                        if (vm.healthConnectLinked) {
                            IconButton(
                                onClick = { vm.syncWithGoogleFit(context) },
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Sync, "Sync Google Fit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                        MovementScoreRing(
                            steps = today.steps,
                            workoutMinutes = today.workoutMinutes,
                            targetScore = if (isWelcomeBack) (target * 0.6).toInt() else target,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${if (isWelcomeBack) (target * 0.6).toInt() else target} goal today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = { showTargetInfo = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Text(
                        "Try ${"%,d".format(if (isWelcomeBack) (target * 0.6).toInt() else target)} steps or ${((if (isWelcomeBack) (target * 0.6).toInt() else target) / 130).coerceAtLeast(10)} min workout",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // 7-Day Completion Strip
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        vm.weekActivities().forEach { act ->
                            val reached = act.movementScore >= act.goalForDay
                            val dateLabel = java.time.LocalDate.ofEpochDay(act.epochDay).format(DateTimeFormatter.ofPattern("EEE"))
                            
                            Box(
                                Modifier
                                    .height(36.dp)
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .combinedClickable(
                                        onClick = { selectedDayInfo = null },
                                        onLongClick = { selectedDayInfo = act }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    dateLabel.take(1),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (reached) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    
                    selectedDayInfo?.let { act ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        java.time.LocalDate.ofEpochDay(act.epochDay).format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${act.steps} steps · ${act.workoutMinutes} min workout",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { selectedDayInfo = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (showTargetInfo) {
                AlertDialog(
                    onDismissRequest = { showTargetInfo = false },
                    title = { Text("Today's Target", fontWeight = FontWeight.Bold) },
                    text = { Text(vm.adaptiveReason) },
                    confirmButton = { TextButton(onClick = { showTargetInfo = false }) { Text("Got it") } }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Hydration & Energy check-in
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Energy", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(vm.energyLevel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { 
                                val next = when(vm.energyLevel) { "Low" -> "Okay"; "Okay" -> "Great"; else -> "Low" }
                                vm.updateEnergyLevel(next)
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Sync, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Hydration", style = MaterialTheme.typography.labelSmall)
                            Text("${vm.waterIntake.intValue}/${vm.waterTarget}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { vm.addWater(context) }, modifier = Modifier.size(32.dp).background(Color(0xFF42A5F5).copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Outlined.WaterDrop, null, tint = Color(0xFF42A5F5), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Daily Tip
            val tip = remember { SampleData.dailyTips.random() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(tip.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(tip.content, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            // Quick Actions
            Text("Quick Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionItem(Icons.Default.PlayArrow, "Start\nWorkout", MaterialTheme.colorScheme.primary, Modifier.weight(1f), onStartQuickWorkout)
                QuickActionItem(Icons.Default.History, "Log\nSteps", Lime500, Modifier.weight(1f), onOpenSteps)
                QuickActionItem(Icons.Default.Timer, "Start\nTimer", Coral500, Modifier.weight(1f), onStartTimer)
            }

            Spacer(Modifier.height(24.dp))

            // Analytics Section
            var chartType by remember { mutableStateOf(ChartViewType.WEEK) }
            var dataType by remember { mutableStateOf("Score") }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(2.dp)
                    ) {
                        listOf(ChartViewType.DAY, ChartViewType.WEEK, ChartViewType.MONTH).forEach { type ->
                            val selected = chartType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { chartType = type }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Data Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Score", "Steps", "Kcal").forEach { type ->
                        val selected = dataType == type
                        FilterChip(
                            selected = selected,
                            onClick = { dataType = type },
                            label = { Text(type) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp
                            )
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Activity Time",
                    value = "${today.workoutMinutes}",
                    unit = "mins",
                    icon = Icons.Default.Timer,
                    color = Teal600,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Calories",
                    value = "${today.caloriesBurned}",
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Lime500,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            val chartData = remember(chartType, dataType, vm.activityHistory.size, vm.todayActivity.steps) {
                val baseData = when(chartType) {
                    ChartViewType.DAY -> {
                        val hourly = vm.dayHourlyActivity
                        val vals = hourly.toList().map { it.toFloat() }
                        val lbls = List(24) { i -> 
                            when {
                                i == 0 -> "12a"
                                i == 6 -> "6a"
                                i == 12 -> "12p"
                                i == 18 -> "6p"
                                i == 23 -> "11p"
                                else -> ""
                            }
                        }
                        vals to lbls
                    }
                    ChartViewType.WEEK -> {
                        val history = vm.weekActivities()
                        val vals = history.map { 
                            when(dataType) {
                                "Score" -> it.movementScore.toFloat()
                                "Steps" -> it.steps.toFloat()
                                else -> it.caloriesBurned.toFloat()
                            }
                        }
                        val lbls = history.map { java.time.LocalDate.ofEpochDay(it.epochDay).format(DateTimeFormatter.ofPattern("EEE")) }
                        vals to lbls
                    }
                    ChartViewType.MONTH -> {
                        val data = vm.last12MonthsActivities()
                        val vals = data.map { it.second }
                        val lbls = data.map { it.first }
                        vals to lbls
                    }
                }
                baseData
            }
            
            BarChart(
                values = chartData.first,
                labels = chartData.second,
                viewType = chartType,
                highlightLast = chartType == ChartViewType.WEEK,
                barColor = when(dataType) {
                    "Steps" -> Teal600
                    "Kcal" -> Lime500
                    else -> null
                }
            )

            Spacer(Modifier.height(24.dp))

            // Weekly Recap Teaser
            Card(
                onClick = onViewReport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "You moved more this week than 80% of your past month. Tap to see your full recap.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun QuickActionItem(icon: ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit, isHighlighted: Boolean = false) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlighted) color.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface),
        border = if (isHighlighted) BorderStroke(1.5.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
            }
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = {}) { Text("See all", color = MaterialTheme.colorScheme.primary) }
    }
}

private fun suggestedWorkout(vm: AppViewModel, pool: List<Workout>): Workout? {
    if (pool.isEmpty()) return null
    return pool.firstOrNull { it.category == WorkoutCategory.STRENGTH } ?: pool.first()
}

private fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
