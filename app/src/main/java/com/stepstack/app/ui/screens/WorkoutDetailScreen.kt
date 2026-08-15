package com.stepstack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.stepstack.app.data.Exercise
import com.stepstack.app.data.ExerciseType
import com.stepstack.app.data.Workout
import com.stepstack.app.ui.components.LabelValue

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.data.SampleData
import com.stepstack.app.ui.theme.Lime500
import com.stepstack.app.ui.theme.StepStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    onStart: () -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 12.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                },
                actions = {
                    Surface(
                        onClick = onEdit,
                        modifier = Modifier.padding(end = 12.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Start Workout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HeroSection(workout)
            }
            item {
                WorkoutInfoSection(workout)
            }
            item {
                Text(
                    "Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            itemsIndexed(workout.exercises) { index, exercise ->
                ExerciseItem(exercise, index)
            }
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HeroSection(workout: Workout) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val imageModel = remember(workout.thumbnailUrl) {
        val resId = context.resources.getIdentifier(workout.thumbnailUrl, "drawable", context.packageName)
        if (resId != 0) resId else workout.thumbnailUrl.ifBlank { null }
    }

    val imageRequest = remember(imageModel) {
        ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        if (imageModel != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = androidx.compose.ui.graphics.painter.ColorPainter(primary.copy(alpha = 0.8f)),
                fallback = androidx.compose.ui.graphics.painter.ColorPainter(primary.copy(alpha = 0.8f))
            )
        }
        
        if (imageModel == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(primary, Lime500)
                        )
                    )
            )
        }
        
        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 400f)))

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Surface(
                color = primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    workout.category.label, 
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                workout.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun WorkoutInfoSection(workout: Workout) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoStat(Icons.Default.Timer, "${workout.durationMin}", "min")
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                InfoStat(Icons.Default.LocalFireDepartment, "${workout.calories}", "kcal")
                VerticalDivider(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                InfoStat(Icons.Default.FitnessCenter, workout.difficulty.label, "level")
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            workout.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(Modifier.height(20.dp))
        
        if (workout.equipment != com.stepstack.app.data.Equipment.NONE) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Handyman, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Equipment Needed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(workout.equipment.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExerciseItem(exercise: Exercise, index: Int) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            if (exercise.demoMediaUrl.isNotBlank()) {
                val exerciseImageRequest = remember(exercise.demoMediaUrl) {
                    ImageRequest.Builder(context)
                        .data(exercise.demoMediaUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                }
                AsyncImage(
                    model = exerciseImageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Circle
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (exercise.type == ExerciseType.TIME) Icons.Default.Timer else Icons.Default.Repeat,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        val detail = if (exercise.type == ExerciseType.REPS) {
                            "${exercise.sets} sets · ${exercise.reps} reps"
                        } else {
                            "${exercise.sets} sets · ${exercise.durationSeconds}s"
                        }
                        Text(
                            detail, 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (exercise.restSeconds > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${exercise.restSeconds}s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = secondary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutDetailScreen() {
    StepStackTheme {
        val workout = SampleData.workouts.firstOrNull()
        if (workout != null) {
            WorkoutDetailScreen(
                workout = workout,
                onStart = {},
                onBack = {}
            )
        }
    }
}
