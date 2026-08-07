package com.stepstack.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.ui.components.ExerciseAnimation
import com.stepstack.app.ui.components.LiveStatsBar
import com.stepstack.app.ui.theme.Teal600
import com.stepstack.app.ui.theme.StepStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedWorkoutScreen(
    state: ActiveWorkoutState,
    onDone: () -> Unit,
    onPauseResume: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit
) {
    val exercise = state.currentExercise

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.workout.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onExit) { Icon(Icons.Default.Close, "Exit") }
                },
                actions = {
                    IconButton(onClick = onPauseResume) {
                        Icon(if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Skip")
                    }
                    
                    if (exercise.type == ExerciseType.REPS && !state.isResting) {
                        Button(
                            onClick = onDone,
                            modifier = Modifier.weight(1.5f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.weight(1.5f).height(52.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (state.isResting) "Resting..." else "Keep going!",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "${state.currentExerciseIndex + 1}/${state.totalExercises}", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(16.dp))

            // Stats Bar
            LiveStatsBar(
                heartRate = 120 + (0..20).random(), 
                calories = (state.progress * state.workout.calories).toInt(),
                elapsedSec = state.elapsedSeconds
            )

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
                if (state.isResting) {
                    RestView(state, exercise)
                } else {
                    ExerciseView(state, exercise)
                }
            }

            // Up Next Hint
            if (!state.isResting && state.currentExerciseIndex < state.totalExercises - 1) {
                val next = state.workout.exercises[state.currentExerciseIndex + 1]
                Surface(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next Up: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(next.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RestView(state: ActiveWorkoutState, nextExercise: Exercise) {
    val secondary = MaterialTheme.colorScheme.secondary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { state.secondsRemaining.toFloat() / nextExercise.restSeconds },
                modifier = Modifier.size(200.dp),
                color = secondary,
                strokeWidth = 12.dp,
                trackColor = secondary.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "REST", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Black, 
                    color = secondary, 
                    letterSpacing = 4.sp
                )
                Text(
                    "${state.secondsRemaining}", 
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 70.sp), 
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(20.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SkipNext, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("UP NEXT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                    Text(nextExercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Set ${state.currentSet} of ${nextExercise.sets}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ExerciseView(state: ActiveWorkoutState, exercise: Exercise) {
    val primary = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(primary.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        ) {
            if (exercise.demoMediaUrl.isNotBlank()) {
                AsyncImage(
                    model = exercise.demoMediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(36.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }
            Box(Modifier.padding(20.dp).fillMaxSize()) {
                ExerciseAnimation(exercise.name, exercise.animation, Modifier.fillMaxSize())
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            exercise.name.uppercase(), 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Black, 
            color = primary,
            letterSpacing = 2.sp
        )
        
        Spacer(Modifier.height(24.dp))

        if (exercise.type == ExerciseType.TIME) {
            Text(
                "${state.secondsRemaining}s", 
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 90.sp), 
                fontWeight = FontWeight.Black, 
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${exercise.reps}", 
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 90.sp), 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "REPS", 
                    style = MaterialTheme.typography.headlineSmall, 
                    modifier = Modifier.padding(bottom = 20.dp, start = 8.dp), 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Text(
            "Set ${state.currentSet} of ${exercise.sets}", 
            style = MaterialTheme.typography.titleMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(32.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                exercise.instructions,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGuidedWorkoutScreen() {
    StepStackTheme {
        GuidedWorkoutScreen(
            state = ActiveWorkoutState(
                workout = SampleData.workouts[0],
                currentExerciseIndex = 0,
                currentSet = 1,
                isResting = false,
                secondsRemaining = 30
            ),
            onDone = {},
            onPauseResume = {},
            onSkip = {},
            onExit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGuidedWorkoutRestScreen() {
    StepStackTheme {
        GuidedWorkoutScreen(
            state = ActiveWorkoutState(
                workout = SampleData.workouts[0],
                currentExerciseIndex = 0,
                currentSet = 1,
                isResting = true,
                secondsRemaining = 15
            ),
            onDone = {},
            onPauseResume = {},
            onSkip = {},
            onExit = {}
        )
    }
}
