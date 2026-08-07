package com.stepstack.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepstack.app.data.*
import com.stepstack.app.ui.components.*
import com.stepstack.app.ui.theme.*
import com.stepstack.app.viewmodel.AppViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    val vm = AppViewModel()
    StepStackTheme {
        ProfileScreen(vm = vm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    var isEditingName by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(vm.user.name) }
    
    var showGoalDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> vm.setReminderTime(context, hour, minute) },
        vm.notificationSettings.reminderHour,
        vm.notificationSettings.reminderMinute,
        false
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = UIConstants.ScreenPadding)
            .padding(top = padding.calculateTopPadding().coerceAtLeast(0.dp))
            .padding(bottom = padding.calculateBottomPadding().coerceAtLeast(0.dp) + 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        
        // --- Profile Header ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Lime500)))
                        .clickable { vm.setProfilePic(context, (vm.profilePicIndex + 1) % 5) },
                    contentAlignment = Alignment.Center,
                ) {
                    val icons = listOf(Icons.Default.Person, Icons.Default.DirectionsRun, Icons.Default.EmojiEvents, Icons.Default.Insights, Icons.Default.FitnessCenter)
                    Icon(icons[vm.profilePicIndex % icons.size], null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    vm.updateProfile(context, editName, vm.user.goal, vm.user.activityLevel, vm.user.equipment)
                                    isEditingName = false
                                }) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                            }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(vm.user.name.ifBlank { "Athlete" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    vm.user.difficultyLevel.label.uppercase(),
                                    modifier = Modifier.padding(6.dp, 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(vm.user.email.ifBlank { "Unlinked account" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Edit Name", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { isEditingName = true })
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Workout Settings Group ---
        SettingsGroup("Your Plan") {
            SettingsItem(
                icon = Icons.Default.DirectionsWalk,
                iconColor = Color(0xFF2196F3),
                label = "Daily Step Goal",
                subLabel = "${"%,d".format(vm.customStepGoal)} steps",
                onClick = { showStepGoalDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Flag,
                iconColor = Color(0xFF4CAF50),
                label = "Fitness Goal",
                subLabel = vm.user.goal.label,
                onClick = { showGoalDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Speed,
                iconColor = Color(0xFFFFA000),
                label = "Activity Level",
                subLabel = vm.user.activityLevel.label,
                onClick = { showActivityDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.FitnessCenter,
                iconColor = Color(0xFF9C27B0),
                label = "Equipment",
                subLabel = vm.user.equipment.label,
                onClick = { showEquipmentDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.NotificationsActive,
                iconColor = Color(0xFF009688),
                label = "Daily Reminder",
                subLabel = "%02d:%02d".format(vm.notificationSettings.reminderHour, vm.notificationSettings.reminderMinute),
                showDivider = false,
                onClick = { timePickerDialog.show() }
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Weekly Performance Group ---
        SettingsGroup("Weekly Performance") {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Weekly Insight", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Keep it up!", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You've met your movement goal ${vm.weekActivities().count { it.movementScore >= vm.baseTarget }} days this week. You're ${vm.weekActivities().count { it.movementScore >= vm.baseTarget }}/7 towards a perfect week!",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Preferences Group ---
        SettingsGroup("Preferences") {
            SettingsItem(
                icon = Icons.Default.Palette,
                iconColor = Color(0xFF673AB7),
                label = "Theme Mode",
                subLabel = vm.themeMode,
                onClick = { showThemeDialog = true }
            )
            SettingsToggleItem(
                icon = Icons.Default.VolumeUp,
                iconColor = Color(0xFF00BCD4),
                label = "Audio Cues",
                checked = vm.soundCues,
                onCheckedChange = { vm.soundCues = it }
            )
            SettingsToggleItem(
                icon = Icons.Default.Straighten,
                iconColor = Color(0xFFE91E63),
                label = "Metric Units (kg/km)",
                checked = vm.useMetric,
                onCheckedChange = { vm.useMetric = it }
            )
            SettingsItem(
                icon = Icons.Default.CloudSync,
                iconColor = Color(0xFF607D8B),
                label = "Google Fit & Health Connect",
                subLabel = if (vm.healthConnectLinked) "Synced ✨" else "Tap to Link 🔗",
                showDivider = true,
                onClick = { vm.syncWithGoogleFit(context) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Biometrics Card ---
        SectionHeader("Biometrics")
        BodyStatCard(vm)

        Spacer(Modifier.height(16.dp))

        // --- Achievements Lazy Row ---
        SectionHeader("Achievements")
        LazyRow(
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.achievements) { achievement ->
                AchievementBadge(achievement)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // --- Dialogs ---
    if (showGoalDialog) {
        OptionSelectionDialog("Fitness Goal", GoalType.entries.map { it.label }, {
            vm.updateProfile(context, vm.user.name, GoalType.entries.find { g -> g.label == it }!!, vm.user.activityLevel, vm.user.equipment)
            showGoalDialog = false
        }, { showGoalDialog = false })
    }
    if (showStepGoalDialog) {
        StepGoalInputDialog(vm.customStepGoal, {
            vm.updateStepGoal(context, it)
            showStepGoalDialog = false
        }, { showStepGoalDialog = false })
    }
    if (showActivityDialog) {
        OptionSelectionDialog("Activity Level", ActivityLevel.entries.map { it.label }, {
            vm.updateProfile(context, vm.user.name, vm.user.goal, ActivityLevel.entries.find { a -> a.label == it }!!, vm.user.equipment)
            showActivityDialog = false
        }, { showActivityDialog = false })
    }
    if (showEquipmentDialog) {
        OptionSelectionDialog("Equipment", Equipment.entries.map { it.label }, {
            vm.updateProfile(context, vm.user.name, vm.user.goal, vm.user.activityLevel, Equipment.entries.find { e -> e.label == it }!!)
            showEquipmentDialog = false
        }, { showEquipmentDialog = false })
    }
    if (showThemeDialog) {
        OptionSelectionDialog("Theme Mode", listOf("System", "Light", "Dark"), {
            vm.setThemeMode(context, it)
            showThemeDialog = false
        }, { showThemeDialog = false })
    }
}

@Composable
private fun PRItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, iconColor: Color, label: String, subLabel: String? = null, showDivider: Boolean = true, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (subLabel != null) Text(subLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
        if (showDivider) HorizontalDivider(Modifier.padding(start = 72.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun SettingsToggleItem(icon: ImageVector, iconColor: Color, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
}

@Composable
private fun BodyStatCard(vm: AppViewModel) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }
    var isLogging by remember { mutableStateOf(false) }
    val latest = vm.bodyStats.lastOrNull()?.weight ?: vm.user.weight.takeIf { it > 0 } ?: 70f
    val display = if (vm.useMetric) latest else latest * 2.20462f
    val unit = if (vm.useMetric) "kg" else "lbs"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorWeight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.1f %s".format(display, unit), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                Button(onClick = { isLogging = !isLogging }, shape = RoundedCornerShape(12.dp)) {
                    Text(if (isLogging) "Cancel" else "Log")
                }
            }
            if (isLogging) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight ($unit)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = {
                        weightInput.toFloatOrNull()?.let {
                            val kg = if (vm.useMetric) it else it / 2.20462f
                            vm.addBodyStat(context, kg)
                            isLogging = false
                        }
                    }) { Icon(Icons.Default.Save, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(a: Achievement) {
    val unlockedColor = when(a.id) {
        "a1" -> Color(0xFF4FC3F7) // Light Blue
        "a2" -> Color(0xFF81C784) // Green
        "a3" -> Color(0xFFFFB74D) // Orange
        "a4" -> Color(0xFFE57373) // Red
        "a5" -> Color(0xFF9575CD) // Purple
        "a6" -> Color(0xFFFFD54F) // Gold
        "a7" -> Color(0xFF4DB6AC) // Teal
        "a8" -> Color(0xFFBA68C8) // Violet
        else -> Coral500
    }
    
    val color = if (a.unlocked) unlockedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    Card(
        modifier = Modifier.width(140.dp).height(180.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (a.unlocked) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = if (a.unlocked) CardDefaults.cardElevation(defaultElevation = 4.dp) else CardDefaults.cardElevation(0.dp),
        border = if (a.unlocked) androidx.compose.foundation.BorderStroke(2.dp, Brush.linearGradient(listOf(unlockedColor, unlockedColor.copy(alpha = 0.5f)))) else null
    ) {
        Column(
            Modifier.padding(12.dp).fillMaxSize(), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                if (a.unlocked) {
                    // Animated-like glow
                    Surface(
                        Modifier.size(64.dp).scale(1.3f),
                        shape = CircleShape,
                        color = unlockedColor.copy(alpha = 0.15f)
                    ) {}
                    Surface(
                        Modifier.size(50.dp).scale(1.1f),
                        shape = CircleShape,
                        color = unlockedColor.copy(alpha = 0.1f)
                    ) {}
                }
                
                Surface(
                    Modifier.size(56.dp), 
                    shape = CircleShape, 
                    color = if (a.unlocked) unlockedColor.copy(alpha = 0.1f) else Color.Transparent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(a.icon) {
                                "footprint" -> Icons.Default.DirectionsWalk
                                "calendar" -> Icons.Default.CalendarToday
                                "steps" -> Icons.Default.DirectionsRun
                                "flame" -> Icons.Default.Whatshot
                                "dumbbell" -> Icons.Default.FitnessCenter
                                "trophy" -> Icons.Default.EmojiEvents
                                "explore" -> Icons.Default.Explore
                                "water_drop" -> Icons.Default.Opacity
                                else -> Icons.Default.EmojiEvents
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                if (!a.unlocked) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock, 
                            null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Text(
                a.title, 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Black, 
                maxLines = 1, 
                textAlign = TextAlign.Center,
                color = if (a.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                a.description, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                maxLines = 2, 
                textAlign = TextAlign.Center, 
                lineHeight = 13.sp,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun StepGoalInputDialog(current: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf(current.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Step Goal") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { c -> c.isDigit() } },
                label = { Text("Steps") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = { Button(onClick = { input.toIntOrNull()?.let { onSave(it) } }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OptionSelectionDialog(title: String, options: List<String>, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { opt ->
                    TextButton(onClick = { onSelect(opt) }, modifier = Modifier.fillMaxWidth()) {
                        Text(opt, textAlign = androidx.compose.ui.text.style.TextAlign.Left, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
