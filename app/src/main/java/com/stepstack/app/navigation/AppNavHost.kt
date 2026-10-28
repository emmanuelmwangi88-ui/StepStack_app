package com.stepstack.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.stepstack.app.audio.LocalWorkoutAudioManager
import com.stepstack.app.data.WorkoutSession
import com.stepstack.app.ui.screens.*
import com.stepstack.app.viewmodel.AppViewModel
import com.stepstack.app.viewmodel.WorkoutEvent
import com.stepstack.app.viewmodel.WorkoutViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController,
    vm: AppViewModel,
    workoutVm: WorkoutViewModel = viewModel(),
    snackbarHostState: SnackbarHostState,
    padding: PaddingValues = PaddingValues(0.dp),
    onLogOutdoor: () -> Unit,
    onStartWalk: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { com.stepstack.app.data.AppDatabase.getDatabase(context) }
    val audioManager = LocalWorkoutAudioManager.current
    
    LaunchedEffect(Unit) {
        workoutVm.loadWorkouts(db)
    }

    LaunchedEffect(workoutVm) {
        workoutVm.events.collectLatest { event ->
            when (event) {
                is WorkoutEvent.Speak -> audioManager?.speak(event.text)
                is WorkoutEvent.Whistle -> audioManager?.whistle()
                is WorkoutEvent.CountBeep -> audioManager?.countBeep()
                is WorkoutEvent.GoBeep -> audioManager?.goBeep()
                is WorkoutEvent.RestEndChime -> audioManager?.restEndChime()
                is WorkoutEvent.FinishFanfare -> audioManager?.finishFanfare()
                is WorkoutEvent.Ticking -> audioManager?.ticking()
                is WorkoutEvent.Stop -> audioManager?.stopAll()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Route.Home.route) {
            val allWorkouts by workoutVm.allWorkouts.collectAsStateWithLifecycle()
            HomeScreen(
                vm = vm,
                workoutVm = workoutVm,
                allWorkouts = allWorkouts,
                padding = padding,
                onStartQuickWorkout = {
                    val w = allWorkouts.find { it.category == com.stepstack.app.data.WorkoutCategory.QUICK } ?: allWorkouts.firstOrNull()
                    w?.let { navController.navigate(Route.WorkoutDetail.createRoute(it.id)) }
                },
                onOpenWorkout = { navController.navigate(Route.WorkoutDetail.createRoute(it.id)) },
                onOpenBuilder = { navController.navigate(Route.Builder.route) },
                onViewReport = { navController.navigate(Route.Steps.route) },
                onSearch = { query ->
                    workoutVm.setSearchQuery(query)
                    navController.navigate(Route.Workouts.route)
                },
                onOpenSteps = { navController.navigate(Route.Steps.route) },
                onLogOutdoor = { onLogOutdoor() },
                onStartTimer = { onStartWalk() },
                onToggleFavorite = { workoutVm.toggleFavorite(it) }
            )
        }
        composable(Route.Workouts.route) {
            WorkoutsScreen(
                viewModel = workoutVm,
                padding = padding,
                onOpenWorkout = { navController.navigate(Route.WorkoutDetail.createRoute(it.id)) },
                onOpenBuilder = { navController.navigate(Route.Builder.route) }
            )
        }
        composable(Route.Steps.route) {
            StepsScreen(
                vm = vm,
                padding = padding,
                onHistoryClick = { navController.navigate(Route.History.route) }
            )
        }
        composable(Route.Profile.route) {
            ProfileScreen(
                vm = vm,
                padding = padding
            )
        }
        composable(Route.Competition.route) {
            CompetitionHomeScreen(
                vm = vm,
                padding = padding,
                onOpenFriend = { navController.navigate(Route.FriendProfile.createRoute(it)) },
                onOpenChallenge = { navController.navigate(Route.ChallengeDetail.createRoute(it)) }
            )
        }
        composable(Route.FriendProfile.route) { entry ->
            val uid = entry.arguments?.getString("uid") ?: return@composable
            FriendProfileScreen(
                uid = uid,
                onBack = { navController.popBackStack() },
                vm = vm
            )
        }
        composable(Route.ChallengeDetail.route) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            ChallengeScreen(
                challengeId = id,
                history = vm.activityHistory.toList(),
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Route.FriendProfile.createRoute(it)) }
            )
        }
        composable(Route.Leaderboard.route) {
            LeaderboardScreen(
                history = vm.activityHistory.toList(),
                onOpenProfile = { navController.navigate(Route.FriendProfile.createRoute(it)) }
            )
        }
        composable(Route.WorkoutDetail.route) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            val workout = workoutVm.getWorkoutById(id) ?: return@composable
            WorkoutDetailScreen(
                workout = workout,
                onBack = { navController.popBackStack() },
                onStart = {
                    workoutVm.startWorkout(
                        workout = workout,
                        autoWarmup = vm.user.autoWarmup,
                        autoCooldown = vm.user.autoCooldown
                    )
                    navController.navigate(Route.GuidedWorkout.createRoute(id))
                },
                onEdit = {
                    navController.navigate(Route.BuilderEdit.createRoute(id))
                }
            )
        }
        composable(Route.GuidedWorkout.route) {
            val activeState by workoutVm.activeWorkout.collectAsStateWithLifecycle()
            
            LaunchedEffect(activeState?.isFinished) {
                if (activeState?.isFinished == true) {
                    navController.navigate(Route.WorkoutComplete.route) {
                        popUpTo(Route.Workouts.route)
                    }
                }
            }

            activeState?.let { state ->
                if (!state.isFinished) {
                    GuidedWorkoutScreen(
                        state = state,
                        onDone = { workoutVm.markExerciseDone() },
                        onPauseResume = { workoutVm.pauseResume() },
                        onSkip = { workoutVm.skipExercise() },
                        onExit = {
                            workoutVm.clearActiveWorkout()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        composable(Route.WorkoutComplete.route) {
            val summary by workoutVm.workoutSummary.collectAsStateWithLifecycle()

            summary?.let { s ->
                WorkoutCompleteScreen(
                    summary = s,
                    onDone = {
                        vm.addWorkoutMinutes(
                            context,
                            s.durationMinutes,
                            WorkoutSession(
                                workoutId = s.workoutId,
                                workoutName = s.workoutName,
                                dateEpochDay = vm.today,
                                durationActualMin = s.durationMinutes,
                                completedPercent = 100,
                                exercisesCompleted = s.exercisesCompleted,
                                caloriesBurned = s.caloriesBurned,
                                startTimeMillis = s.startTimeMillis,
                                thumbnailUrl = s.thumbnailUrl
                            )
                        )
                        navController.navigate(Route.Workouts.route) {
                            popUpTo(Route.Workouts.route) { inclusive = true }
                        }
                        workoutVm.clearActiveWorkout()
                        scope.launch { snackbarHostState.showSnackbar("Workout saved to history!") }
                    }
                )
            }
        }
        composable(Route.Builder.route) {
            WorkoutBuilderScreen(
                onBack = { navController.popBackStack() },
                onSave = {
                    workoutVm.saveWorkout(it)
                    navController.popBackStack()
                    scope.launch { snackbarHostState.showSnackbar("Routine \"${it.name}\" saved") }
                },
            )
        }
        composable(Route.BuilderEdit.route) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            val workout = workoutVm.getWorkoutById(id) ?: return@composable
            WorkoutBuilderScreen(
                editingWorkout = workout,
                onBack = { navController.popBackStack() },
                onSave = {
                    workoutVm.saveWorkout(it)
                    navController.popBackStack()
                    scope.launch { snackbarHostState.showSnackbar("Routine \"${it.name}\" updated") }
                },
                onDelete = {
                    workoutVm.deleteWorkout(id)
                    navController.navigate(Route.Workouts.route) {
                        popUpTo(Route.Workouts.route) { inclusive = true }
                    }
                    scope.launch { snackbarHostState.showSnackbar("Routine deleted") }
                }
            )
        }
        composable(Route.History.route) {
            WorkoutHistoryScreen(vm = vm, onBack = { navController.popBackStack() })
        }
        composable(Route.Notifications.route) {
            NotificationsScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(Route.FriendProfile.createRoute(it)) }
            )
        }
    }
}
