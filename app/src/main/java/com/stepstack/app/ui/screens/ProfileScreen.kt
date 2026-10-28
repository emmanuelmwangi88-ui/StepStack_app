package com.stepstack.app.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.ui.theme.*
import com.stepstack.app.util.CloudinaryManager
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

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
    var isUploading by remember { mutableStateOf(false) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                isUploading = true
                CloudinaryManager.uploadImage(
                    it,
                    onSuccess = { url ->
                        vm.updateProfilePhoto(context, url)
                        isUploading = false
                        scope.launch { 
                            // Using a simple toast as snackbar might be far away in Scaffold
                            android.widget.Toast.makeText(context, "Profile photo updated", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error -> 
                        isUploading = false
                        scope.launch {
                            android.widget.Toast.makeText(context, "Upload failed: $error", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    )

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

        ProfileHero(
            vm = vm,
            isEditingName = isEditingName,
            editName = editName,
            isUploading = isUploading,
            onEditNameChange = { editName = it },
            onStartEdit = { isEditingName = true },
            onConfirmEdit = {
                vm.updateProfile(context, editName, vm.user.goal, vm.user.activityLevel, vm.user.equipment)
                isEditingName = false
            },
            onPickPhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        Spacer(Modifier.height(14.dp))

        QuickStatsRow(vm = vm)

        Spacer(Modifier.height(20.dp))

        SettingsGroup("Your Plan") {
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
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

        SettingsGroup("Preferences") {
            SettingsItem(
                icon = Icons.Default.Palette,
                iconColor = Color(0xFF673AB7),
                label = "Theme Mode",
                subLabel = vm.themeMode,
                onClick = { showThemeDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.BatteryChargingFull,
                iconColor = Color(0xFFF44336),
                label = "Battery Optimization",
                subLabel = if (vm.isBatteryOptimizationIgnored) "Optimized for Background ✨" else "Background restricted ⚠️",
                onClick = { vm.requestIgnoreBatteryOptimizations(context) }
            )
            SettingsToggleItem(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
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
                showDivider = false,
                onClick = { vm.syncWithGoogleFit(context) }
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader(icon = Icons.Default.MonitorWeight, title = "Biometrics")
        BodyStatCard(vm)

        Spacer(Modifier.height(20.dp))

        AchievementsSection(vm)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.logout(context) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showGoalDialog) {
        OptionSelectionDialog("Fitness Goal", GoalType.entries.map { it.label }, { label ->
            val selectedGoal = GoalType.entries.find { g -> g.label == label } ?: vm.user.goal
            vm.updateProfile(context, vm.user.name, selectedGoal, vm.user.activityLevel, vm.user.equipment)
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
        OptionSelectionDialog("Activity Level", ActivityLevel.entries.map { it.label }, { label ->
            val selectedActivity = ActivityLevel.entries.find { a -> a.label == label } ?: vm.user.activityLevel
            vm.updateProfile(context, vm.user.name, vm.user.goal, selectedActivity, vm.user.equipment)
            showActivityDialog = false
        }, { showActivityDialog = false })
    }
    if (showEquipmentDialog) {
        OptionSelectionDialog("Equipment", Equipment.entries.map { it.label }, { label ->
            val selectedEquipment = Equipment.entries.find { e -> e.label == label } ?: vm.user.equipment
            vm.updateProfile(context, vm.user.name, vm.user.goal, vm.user.activityLevel, selectedEquipment)
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

// ---------------------------------------------------------------------------
// Hero header: gradient banner, avatar with ring, inline name edit, level pill
// ---------------------------------------------------------------------------
@Composable
private fun ProfileHero(
    vm: AppViewModel,
    isEditingName: Boolean,
    editName: String,
    isUploading: Boolean,
    onEditNameChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onConfirmEdit: () -> Unit,
    onPickPhoto: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, Lime500),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        // soft decorative circles for depth
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Box(
            Modifier
                .size(80.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Row(
            Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable(enabled = !vm.isDemoMode, onClick = onPickPhoto),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.White, strokeWidth = 2.dp)
                } else if (vm.user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = vm.user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val icons = listOf(Icons.Default.Person, Icons.AutoMirrored.Filled.DirectionsRun, Icons.Default.EmojiEvents, Icons.Default.Insights, Icons.Default.FitnessCenter)
                    Icon(icons[vm.profilePicIndex % icons.size], null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                // camera badge
                if (!vm.isDemoMode) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                if (isEditingName) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = onEditNameChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White
                        ),
                        trailingIcon = {
                            IconButton(onClick = onConfirmEdit) {
                                Icon(Icons.Default.Check, null, tint = Color.White)
                            }
                        }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            vm.user.name.ifBlank { "Athlete" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                vm.user.difficultyLevel.label.uppercase(),
                                modifier = Modifier.padding(8.dp, 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        vm.user.email.ifBlank { "Unlinked account" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    if (!vm.isDemoMode) {
                        Row(
                            Modifier.clickable(onClick = onStartEdit).padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit name", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text(
                            "Demo Mode: Login to edit profile",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Quick stats row: streak, total steps, achievements unlocked
// ---------------------------------------------------------------------------
@Composable
private fun QuickStatsRow(vm: AppViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFFF5722),
            value = "${vm.currentStreak}",
            label = "Day streak"
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            iconColor = Color(0xFF2196F3),
            value = formatCompact(vm.totalLifetimeSteps),
            label = "Total steps"
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.EmojiEvents,
            iconColor = Color(0xFFFFC107),
            value = "${vm.achievements.count { it.unlocked }}/${vm.achievements.size}",
            label = "Badges"
        )
    }
}

@Composable
private fun QuickStatCard(modifier: Modifier = Modifier, icon: ImageVector, iconColor: Color, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatCompact(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fk".format(n / 1_000.0)
    else -> n.toString()
}

// ---------------------------------------------------------------------------
// Settings building blocks
// ---------------------------------------------------------------------------
@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
private fun SectionHeader(icon: ImageVector? = null, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
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
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MonitorWeight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.1f %s".format(display, unit), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                FilledTonalButton(onClick = { isLogging = !isLogging }, shape = RoundedCornerShape(12.dp)) {
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
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            weightInput.toFloatOrNull()?.let {
                                val kg = if (vm.useMetric) it else it / 2.20462f
                                vm.addBodyStat(context, kg)
                                isLogging = false
                                weightInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) { Icon(Icons.Default.Save, null, tint = Color.White) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Achievements: header with progress bar + polished badge grid
// ---------------------------------------------------------------------------
@Composable
private fun AchievementsSection(vm: AppViewModel) {
    val unlocked = vm.achievements.count { it.unlocked }
    val total = vm.achievements.size
    val progress by animateFloatAsState(
        targetValue = if (total > 0) unlocked.toFloat() / total else 0f,
        animationSpec = tween(700), label = "achievementProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(vertical = 18.dp)) {
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFC107).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Stars, null, tint = Color(0xFFFFC107))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("$unlocked of $total unlocked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            )

            Spacer(Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(vm.achievements.sortedByDescending { it.unlocked }) { achievement ->
                    AchievementBadgeCard(achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadgeCard(achievement: Achievement) {
    val glowColor = if (achievement.unlocked) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = if (achievement.unlocked) 8.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = glowColor
                )
                .clip(CircleShape)
                .background(
                    if (achievement.unlocked) {
                        Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00)))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                .then(
                    if (!achievement.unlocked)
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAchievementIcon(achievement.icon),
                contentDescription = achievement.title,
                tint = if (achievement.unlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
            if (!achievement.unlocked) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(11.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            achievement.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (achievement.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------
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

@Composable
fun getAchievementIcon(icon: String): ImageVector {
    return when (icon) {
        "footprint" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "calendar" -> Icons.Default.CalendarToday
        "steps" -> Icons.AutoMirrored.Filled.DirectionsRun
        "flame" -> Icons.Default.Whatshot
        "dumbbell" -> Icons.Default.FitnessCenter
        "trophy" -> Icons.Default.EmojiEvents
        "explore" -> Icons.Default.Explore
        "water_drop" -> Icons.Default.Opacity
        "wb_sunny" -> Icons.Default.WbSunny
        "bedtime" -> Icons.Default.Bedtime
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "insights" -> Icons.Default.Insights
        else -> Icons.Default.EmojiEvents
    }
}
