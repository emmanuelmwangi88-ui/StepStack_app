package com.stepstack.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepstack.app.data.Difficulty
import com.stepstack.app.data.Equipment
import com.stepstack.app.data.Workout
import com.stepstack.app.data.WorkoutCategory
import com.stepstack.app.ui.theme.*
import com.stepstack.app.viewmodel.WorkoutViewModel
import com.stepstack.app.ui.components.WorkoutSessionCard

import androidx.compose.ui.tooling.preview.Preview
import com.stepstack.app.ui.theme.StepStackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutViewModel = viewModel(),
    padding: PaddingValues = PaddingValues(0.dp),
    onOpenWorkout: (Workout) -> Unit,
    onOpenBuilder: () -> Unit = {},
) {
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    val favoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    var selectedCategory by remember { mutableStateOf<WorkoutCategory?>(null) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var selectedDuration by remember { mutableStateOf<String?>(null) } // "Under 10", "10-20", "20+"

    val allWorkouts by viewModel.allWorkouts.collectAsState()
    val filtered = remember(allWorkouts, searchQuery, selectedDifficulty, favoritesOnly, favorites, selectedCategory, selectedEquipment, selectedDuration) {
        allWorkouts.filter { workout ->
            val matchesSearch = searchQuery.isEmpty() ||
                    workout.name.contains(searchQuery, ignoreCase = true)
            val matchesDifficulty = selectedDifficulty == null || workout.difficulty == selectedDifficulty
            val matchesCategory = selectedCategory == null || workout.category == selectedCategory
            val matchesEquipment = selectedEquipment == null || workout.equipment == selectedEquipment
            val matchesFavorites = !favoritesOnly || favorites.contains(workout.id)
            val matchesDuration = when (selectedDuration) {
                "Under 10" -> workout.durationMin < 10
                "10-20" -> workout.durationMin in 10..20
                "20+" -> workout.durationMin > 20
                else -> true
            }
            matchesSearch && matchesDifficulty && matchesCategory && matchesEquipment && matchesDuration && matchesFavorites
        }
    }

    val hasActiveFilters = selectedCategory != null || selectedEquipment != null ||
            selectedDuration != null || selectedDifficulty != null || favoritesOnly || searchQuery.isNotEmpty()

    fun clearAllFilters() {
        selectedCategory = null
        selectedEquipment = null
        selectedDuration = null
        viewModel.setSearchQuery("")
        if (favoritesOnly) viewModel.toggleFavoritesOnly()
    }

    val primary = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenBuilder,
                containerColor = primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Add, "Create Routine") },
                text = { Text("Build", fontWeight = FontWeight.Bold) },
                modifier = Modifier.padding(bottom = 1.dp, end = 1.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            )
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primary.copy(alpha = 0.14f), Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(0f, 260f)
                            )
                        )
                        .padding(22.dp, 20.dp, 24.dp, 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Workouts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Find your perfect flow \u2728",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { viewModel.toggleFavoritesOnly() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Saved workouts",
                                tint = primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "${favorites.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                    }
                }
            }

            // Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    // Categories
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selectedCategory == null, "All") { selectedCategory = null } }
                        items(WorkoutCategory.entries) { cat ->
                            FilterChip(selectedCategory == cat, cat.label) { selectedCategory = cat }
                        }
                    }

                    // Equipment
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selectedEquipment == null, "Any Equipment") { selectedEquipment = null } }
                        items(Equipment.entries) { eq ->
                            FilterChip(selectedEquipment == eq, eq.label) { selectedEquipment = eq }
                        }
                    }

                    // Duration
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selectedDuration == null, "Any Length") { selectedDuration = null } }
                        items(listOf("Under 10", "10-20", "20+")) { d ->
                            FilterChip(selectedDuration == d, "$d min") { selectedDuration = d }
                        }
                    }

                    AnimatedVisibility(visible = hasActiveFilters) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${filtered.size} workout${if (filtered.size == 1) "" else "s"} found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { clearAllFilters() }) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Clear filters", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyWorkoutsState(
                        isFavorites = favoritesOnly,
                        onClearFilters = { clearAllFilters() }
                    )
                }
            } else {
                // Rows by Category
                WorkoutCategory.entries.forEach { cat ->
                    val inCat = filtered.filter { it.category == cat }
                    if (inCat.isNotEmpty()) {
                        item {
                            WorkoutSectionHeader(cat.label, count = inCat.size)
                            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(inCat, key = { it.id }) { w ->
                                    WorkoutSessionCard(
                                        workout = w,
                                        isFavorite = favorites.contains(w.id),
                                        onClick = { onOpenWorkout(w) },
                                        onFavoriteClick = { viewModel.toggleFavorite(w.id) },
                                        modifier = Modifier.width(260.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "chipColor"
    )
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun WorkoutSectionHeader(
    title: String,
    count: Int = 0,
    showViewAll: Boolean = false,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        if (showViewAll) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyWorkoutsState(
    isFavorites: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isFavorites) Icons.Default.FavoriteBorder else Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (isFavorites) "No Favorites Yet" else "No Workouts Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isFavorites)
                "Save your favorite routines to find them quickly here."
            else "We couldn't find any workouts matching your current filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onClearFilters) {
            Text("Clear all filters", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutsScreen() {
    StepStackTheme {
        WorkoutsScreen(
            viewModel = WorkoutViewModel(),
            onOpenWorkout = {}
        )
    }
}