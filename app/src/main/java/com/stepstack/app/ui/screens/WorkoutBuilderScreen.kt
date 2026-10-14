package com.stepstack.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepstack.app.data.*
import com.stepstack.app.ui.theme.Teal600

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    editingWorkout: Workout? = null,
    onBack: () -> Unit, 
    onSave: (Workout) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(editingWorkout?.name ?: "") }
    var description by remember { mutableStateOf(editingWorkout?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(editingWorkout?.category ?: WorkoutCategory.STRENGTH) }
    var selectedDifficulty by remember { mutableStateOf(editingWorkout?.difficulty ?: Difficulty.BEGINNER) }
    var selectedEquipment by remember { mutableStateOf(editingWorkout?.equipment ?: Equipment.NONE) }
    val selectedExercises = remember { 
        mutableStateListOf<Exercise>().apply { 
            editingWorkout?.exercises?.let { addAll(it) } 
        } 
    }
    var showAddExercise by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (editingWorkout == null) "Create Routine" else "Edit Routine", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface) }
                },
                actions = {
                    if (editingWorkout != null && onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = { 
                            val totalMin = selectedExercises.sumOf { (if(it.type == ExerciseType.TIME) it.durationSeconds else it.reps * 5) + it.restSeconds } / 60
                            val cals = selectedExercises.size * 25
                            val workout = Workout(
                                id = editingWorkout?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                category = selectedCategory,
                                difficulty = selectedDifficulty,
                                durationMin = if(totalMin > 0) totalMin else 10,
                                calories = if(cals > 0) cals else 100,
                                equipment = selectedEquipment,
                                description = description,
                                exercises = selectedExercises.toList(),
                                thumbnailUrl = editingWorkout?.thumbnailUrl ?: ""
                            )
                            onSave(workout) 
                        }, 
                        enabled = name.isNotBlank() && selectedExercises.isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = if(name.isNotBlank() && selectedExercises.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text("Basic Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workout Name") },
                        placeholder = { Text("e.g. Morning Power Flow") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("What is this workout for?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3
                    )
                }
            }

            item {
                Column {
                    Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CategoryDropdown(selectedCategory, Modifier.weight(1f)) { selectedCategory = it }
                        DifficultyDropdown(selectedDifficulty, Modifier.weight(1f)) { selectedDifficulty = it }
                    }
                    Spacer(Modifier.height(12.dp))
                    EquipmentDropdown(selectedEquipment, Modifier.fillMaxWidth()) { selectedEquipment = it }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Exercises", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { showAddExercise = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add")
                    }
                }
            }

            if (selectedExercises.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            Modifier.padding(40.dp).fillMaxWidth(), 
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("No exercises added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(selectedExercises) { index, ex ->
                    var showEdit by remember { mutableStateOf(false) }
                    ExerciseRow(
                        ex = ex, 
                        onRemove = { selectedExercises.removeAt(index) },
                        onClick = { showEdit = true }
                    )
                    
                    if (showEdit) {
                        EditExerciseDialog(
                            exercise = ex,
                            onDismiss = { showEdit = false },
                            onConfirm = { updated ->
                                selectedExercises[index] = updated
                                showEdit = false
                            }
                        )
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    if (showAddExercise) {
        AddExerciseDialog(onDismiss = { showAddExercise = false }) {
            selectedExercises.add(it)
            showAddExercise = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentDropdown(current: Equipment, modifier: Modifier, onSelect: (Equipment) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = current.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Equipment Needed") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Equipment.entries.forEach { eq ->
                DropdownMenuItem(text = { Text(eq.label) }, onClick = { onSelect(eq); expanded = false })
            }
        }
    }
}

@Composable
private fun EditExerciseDialog(exercise: Exercise, onDismiss: () -> Unit, onConfirm: (Exercise) -> Unit) {
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var value by remember { mutableStateOf(if(exercise.type == ExerciseType.REPS) exercise.reps.toString() else exercise.durationSeconds.toString()) }
    var rest by remember { mutableStateOf(exercise.restSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${exercise.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Sets") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(
                    value = value, 
                    onValueChange = { value = it }, 
                    label = { Text(if(exercise.type == ExerciseType.REPS) "Reps" else "Duration (s)") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(value = rest, onValueChange = { rest = it }, label = { Text("Rest (s)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(exercise.copy(
                    sets = sets.toIntOrNull() ?: 1,
                    reps = if(exercise.type == ExerciseType.REPS) value.toIntOrNull() ?: 0 else 0,
                    durationSeconds = if(exercise.type == ExerciseType.TIME) value.toIntOrNull() ?: 0 else 0,
                    restSeconds = rest.toIntOrNull() ?: 0
                ))
            }) {
                Text("Confirm")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ExerciseRow(ex: Exercise, onClick: () -> Unit, onRemove: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (ex.type == ExerciseType.TIME) Icons.Default.Timer else Icons.Default.Repeat,
                        null,
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ex.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${ex.sets} sets · ${if(ex.type == ExerciseType.REPS) ex.reps.toString() + " reps" else ex.durationSeconds.toString() + "s"}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) { 
                Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)) 
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(current: WorkoutCategory, modifier: Modifier, onSelect: (WorkoutCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = current.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WorkoutCategory.entries.forEach { cat ->
                DropdownMenuItem(text = { Text(cat.label) }, onClick = { onSelect(cat); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyDropdown(current: Difficulty, modifier: Modifier, onSelect: (Difficulty) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = current.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Difficulty") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(16.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Difficulty.entries.forEach { diff ->
                DropdownMenuItem(text = { Text(diff.label) }, onClick = { onSelect(diff); expanded = false })
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(onDismiss: () -> Unit, onAdd: (Exercise) -> Unit) {
    val list = SampleData.masterExercises
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Exercise") },
        text = {
            LazyColumn(Modifier.height(400.dp)) {
                items(list) { ex ->
                    TextButton(onClick = { onAdd(ex) }, modifier = Modifier.fillMaxWidth()) {
                        Text(ex.name, textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutBuilderScreen() {
    StepStackTheme {
        WorkoutBuilderScreen(onBack = {}, onSave = {})
    }
}
