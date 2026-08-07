package com.stepstack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepstack.app.data.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import com.stepstack.app.service.StepTrackerService

@kotlinx.serialization.Serializable
sealed class WorkoutEvent {
    @kotlinx.serialization.Serializable
    data class Speak(val text: String) : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object Whistle : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object CountBeep : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object GoBeep : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object RestEndChime : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object FinishFanfare : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object Ticking : WorkoutEvent()
    @kotlinx.serialization.Serializable
    data object Stop : WorkoutEvent()
}

class WorkoutViewModel : ViewModel() {

    private var database: AppDatabase? = null
    private val _events = MutableSharedFlow<WorkoutEvent>()
    val events = _events.asSharedFlow()

    private val _allWorkouts = MutableStateFlow<List<Workout>>(emptyList())
    val allWorkouts: StateFlow<List<Workout>> = _allWorkouts.asStateFlow()

    fun loadWorkouts(db: AppDatabase) {
        database = db
        viewModelScope.launch {
            val entities = db.workoutDao().getAllWorkouts()
            _allWorkouts.value = entities.map { e ->
                Workout(
                    id = e.id,
                    name = e.name,
                    category = e.category,
                    difficulty = e.difficulty,
                    durationMin = e.durationMin,
                    calories = e.calories,
                    equipment = e.equipment,
                    description = e.description,
                    thumbnailUrl = e.thumbnailUrl,
                    exercises = try { Json.decodeFromString<List<Exercise>>(e.exercisesJson) } catch(err: Exception) { emptyList() },
                    isFavorite = e.isFavorite
                )
            }
            // Initialize favorite IDs
            _favoriteIds.value = entities.filter { it.isFavorite }.map { it.id }.toSet()
        }
    }

    private val _selectedCategory = MutableStateFlow<WorkoutCategory?>(null)
    val selectedCategory: StateFlow<WorkoutCategory?> = _selectedCategory.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun setCategory(category: WorkoutCategory?) {
        _selectedCategory.value = category
    }

    fun setDifficulty(difficulty: Difficulty?) {
        _selectedDifficulty.value = difficulty
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(workoutId: String) {
        _favoriteIds.update { if (it.contains(workoutId)) it - workoutId else it + workoutId }
        viewModelScope.launch(Dispatchers.IO) {
            database?.workoutDao()?.updateFavorite(workoutId, _favoriteIds.value.contains(workoutId))
        }
    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = WorkoutEntity(
                id = workout.id,
                name = workout.name,
                category = workout.category,
                difficulty = workout.difficulty,
                durationMin = workout.durationMin,
                calories = workout.calories,
                equipment = workout.equipment,
                description = workout.description,
                thumbnailUrl = workout.thumbnailUrl,
                exercisesJson = Json.encodeToString(workout.exercises),
                isFavorite = workout.isFavorite
            )
            database?.workoutDao()?.insertWorkouts(listOf(entity))
            
            // Refresh local list
            database?.let { loadWorkouts(it) }
        }
    }

    fun deleteWorkout(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.workoutDao()?.deleteWorkout(id)
            database?.let { loadWorkouts(it) }
        }
    }

    fun filteredWorkouts(): List<Workout> {
        val cat = _selectedCategory.value
        val diff = _selectedDifficulty.value
        val query = _searchQuery.value
        val favoritesOnly = _showFavoritesOnly.value
        val favs = _favoriteIds.value
        
        return _allWorkouts.value.filter { workout ->
            val matchesCategory = cat == null || workout.category == cat
            val matchesDifficulty = diff == null || workout.difficulty == diff
            val matchesSearch = query.isBlank() || workout.name.contains(query, ignoreCase = true)
            val matchesFavorites = !favoritesOnly || favs.contains(workout.id)
            matchesCategory && matchesDifficulty && matchesSearch && matchesFavorites
        }
    }

    fun workoutsByCategory(category: WorkoutCategory): List<Workout> {
        return _allWorkouts.value.filter { it.category == category }
    }

    fun getWorkoutById(id: String): Workout? {
        return _allWorkouts.value.find { it.id == id }
    }

    // ---- Active workout state ----
    private val _activeWorkout = MutableStateFlow<ActiveWorkoutState?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutState?> = _activeWorkout.asStateFlow()

    private val _workoutSummary = MutableStateFlow<WorkoutSummary?>(null)
    val workoutSummary: StateFlow<WorkoutSummary?> = _workoutSummary.asStateFlow()

    private var timerJob: Job? = null

    val warmUpWorkout = Workout(
        id = "warmup_auto",
        name = "Warm Up",
        category = WorkoutCategory.MOBILITY,
        difficulty = Difficulty.BEGINNER,
        durationMin = 3,
        calories = 20,
        exercises = listOf(
            Exercise("wue1", "Neck & Shoulder Rolls", ExerciseType.TIME, durationSeconds = 60, instructions = "Roll shoulders back slowly."),
            Exercise("wue2", "Hip Circles", ExerciseType.TIME, durationSeconds = 60, instructions = "Rotate hips in a large circle."),
            Exercise("wue3", "Arm Swings", ExerciseType.TIME, durationSeconds = 60, instructions = "Swing arms horizontally across chest.")
        )
    )

    val coolDownWorkout = Workout(
        id = "cooldown_auto",
        name = "Cool Down",
        category = WorkoutCategory.STRETCHING,
        difficulty = Difficulty.BEGINNER,
        durationMin = 3,
        calories = 15,
        exercises = listOf(
            Exercise("cde1", "Static Hamstring Stretch", ExerciseType.TIME, durationSeconds = 90, instructions = "Reach for toes and hold."),
            Exercise("cde2", "Deep Breathing", ExerciseType.TIME, durationSeconds = 90, instructions = "Slow, deep diaphragmatic breaths.")
        )
    )

    fun startWorkout(workout: Workout, autoWarmup: Boolean = false, autoCooldown: Boolean = false) {
        timerJob?.cancel()
        
        var finalExercises = workout.exercises
        if (autoWarmup) finalExercises = warmUpWorkout.exercises + finalExercises
        if (autoCooldown) finalExercises = finalExercises + coolDownWorkout.exercises
        
        val effectiveWorkout = workout.copy(exercises = finalExercises)
        val firstExercise = effectiveWorkout.exercises.firstOrNull() ?: return
        
        _activeWorkout.value = ActiveWorkoutState(
            workout = effectiveWorkout,
            currentExerciseIndex = 0,
            currentSet = 1,
            secondsRemaining = if (firstExercise.type == ExerciseType.TIME) firstExercise.durationSeconds else 0
        )
        
        StepTrackerService.updateWorkout(workout.name, if (firstExercise.type == ExerciseType.TIME) firstExercise.durationSeconds else null)

        viewModelScope.launch {
            _events.emit(WorkoutEvent.Whistle)
            delay(500)
            _events.emit(WorkoutEvent.Speak("Starting ${workout.name}. First exercise: ${firstExercise.name}"))
        }

        if (firstExercise.type == ExerciseType.TIME) {
            runTimer()
        }
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val state = _activeWorkout.value ?: break
                if (state.isPaused || state.isFinished) {
                    delay(200)
                    continue
                }
                if (state.secondsRemaining <= 0) {
                    advance()
                    continue
                }
                
                // Audio cues based on time
                val total = if (state.isResting) state.currentExercise.restSeconds else state.currentExercise.durationSeconds
                when (state.secondsRemaining) {
                    3 -> { _events.emit(WorkoutEvent.CountBeep); _events.emit(WorkoutEvent.Speak("3")) }
                    2 -> { _events.emit(WorkoutEvent.CountBeep); _events.emit(WorkoutEvent.Speak("2")) }
                    1 -> { _events.emit(WorkoutEvent.CountBeep); _events.emit(WorkoutEvent.Speak("1")) }
                    10 -> _events.emit(WorkoutEvent.Ticking)
                    total / 2 -> if (total >= 20 && !state.isResting) _events.emit(WorkoutEvent.Speak("Halfway there"))
                }

                delay(1000)
                _activeWorkout.update { it?.copy(
                    secondsRemaining = (it.secondsRemaining - 1).coerceAtLeast(0),
                    elapsedSeconds = it.elapsedSeconds + 1
                ) }
                _activeWorkout.value?.let { 
                    StepTrackerService.updateWorkout(it.workout.name, it.secondsRemaining)
                }
            }
        }
    }

    fun markExerciseDone() {
        advance()
    }

    fun pauseResume() {
        _activeWorkout.update { it?.copy(isPaused = !it.isPaused) }
    }

    fun skipExercise() {
        advance()
    }

    private fun advance() {
        val state = _activeWorkout.value ?: return
        val exercise = state.currentExercise

        // Handle multi-set exercises
        if (!state.isResting && state.currentSet < exercise.sets) {
            _activeWorkout.value = state.copy(
                isResting = true,
                secondsRemaining = exercise.restSeconds
            )
            viewModelScope.launch {
                _events.emit(WorkoutEvent.Speak("Rest for ${exercise.restSeconds} seconds"))
            }
            if (exercise.restSeconds > 0) runTimer() else advance()
            return
        }

        if (state.isResting) {
            val nextSet = state.currentSet + 1
            val startSeconds = if (exercise.type == ExerciseType.TIME) exercise.durationSeconds else 0
            _activeWorkout.value = state.copy(
                currentSet = nextSet,
                isResting = false,
                secondsRemaining = startSeconds
            )
            viewModelScope.launch {
                _events.emit(WorkoutEvent.RestEndChime)
                delay(200)
                _events.emit(WorkoutEvent.Whistle)
                delay(200)
                _events.emit(WorkoutEvent.Speak("Set $nextSet"))
            }
            if (exercise.type == ExerciseType.TIME) runTimer() else timerJob?.cancel()
            return
        }

        // Move to next exercise
        val nextIndex = state.currentExerciseIndex + 1
        if (nextIndex >= state.workout.exercises.size) {
            finishWorkout()
            return
        }
        val nextExercise = state.workout.exercises[nextIndex]
        _activeWorkout.value = state.copy(
            currentExerciseIndex = nextIndex,
            currentSet = 1,
            isResting = false,
            secondsRemaining = if (nextExercise.type == ExerciseType.TIME) nextExercise.durationSeconds else 0
        )
        
        viewModelScope.launch {
            _events.emit(WorkoutEvent.GoBeep)
            delay(200)
            _events.emit(WorkoutEvent.Whistle) // Add whistle when exercise starts
            delay(200)
            _events.emit(WorkoutEvent.Speak("Next exercise: ${nextExercise.name}"))
        }

        if (nextExercise.type == ExerciseType.TIME) {
            runTimer()
        } else {
            timerJob?.cancel()
        }
    }

    fun finishWorkout() {
        timerJob?.cancel()
        val state = _activeWorkout.value ?: return
        
        StepTrackerService.updateWorkout(null, null)

        viewModelScope.launch {
            _events.emit(WorkoutEvent.FinishFanfare)
            delay(1000)
            _events.emit(WorkoutEvent.Speak("Workout complete. Great job!"))
        }
        _workoutSummary.value = WorkoutSummary(
            workoutId = state.workout.id,
            workoutName = state.workout.name,
            durationMinutes = state.workout.durationMin,
            caloriesBurned = state.workout.calories,
            exercisesCompleted = state.workout.exercises.size
        )
        _activeWorkout.value = state.copy(isFinished = true)
    }

    fun clearActiveWorkout() {
        timerJob?.cancel()
        _activeWorkout.value = null
        _workoutSummary.value = null
        StepTrackerService.updateWorkout(null, null)
        viewModelScope.launch {
            _events.emit(WorkoutEvent.Stop)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
