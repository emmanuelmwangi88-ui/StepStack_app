package com.stepstack.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stepstack.app.audio.LocalWorkoutAudioManager
import com.stepstack.app.audio.rememberWorkoutAudioManager
import com.stepstack.app.navigation.AppNavHost
import com.stepstack.app.navigation.Route
import com.stepstack.app.ui.screens.LogOutdoorSheet
import com.stepstack.app.ui.screens.OnboardingScreen
import com.stepstack.app.ui.screens.WalkTimerSheet
import com.stepstack.app.ui.theme.StepStackTheme
import com.stepstack.app.ui.theme.UIConstants
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            val darkTheme = when (vm.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            StepStackTheme(darkTheme = darkTheme) {
                val audioManager = rememberWorkoutAudioManager(
                    soundEnabled = vm.soundCues,
                    voiceEnabled = vm.soundCues
                )
                
                CompositionLocalProvider(LocalWorkoutAudioManager provides audioManager) {
                    val context = LocalContext.current

                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val activityGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
                        } else true

                        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
                        } else true

                        if (activityGranted && notificationGranted) {
                            vm.startHardwareTracking(context)
                        }
                    }

                    LaunchedEffect(Unit) {
                        vm.loadData(context)
                        
                        val needsActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
                        } else false
                        
                        val needsNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        } else false

                        val activityGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
                        } else true
                        vm.activityPermissionGranted = activityGranted

                        if (needsActivity || needsNotifications) {
                            val perms = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) perms.add(Manifest.permission.ACTIVITY_RECOGNITION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            
                            if (perms.isNotEmpty()) {
                                launcher.launch(perms.toTypedArray())
                            } else {
                                vm.startHardwareTracking(context)
                            }
                        } else {
                            vm.startHardwareTracking(context)
                        }
                    }

                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        com.stepstack.app.ui.screens.SplashScreen { showSplash = false }
                    } else if (!vm.onboardingComplete) {
                        OnboardingScreen(onComplete = { vm.completeOnboarding(context, it) })
                    } else {
                        MainApp(vm)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(vm: AppViewModel) {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showLogOutdoor by remember { mutableStateOf(false) }
    var showWalkTimer by remember { mutableStateOf(false) }

    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBars = route in listOf(Route.Home.route, Route.Workouts.route, Route.Steps.route, Route.Profile.route)
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = {
                        val title = when (route) {
                            Route.Home.route -> "Step Stack"
                            Route.Workouts.route -> "Workouts"
                            Route.Steps.route -> "Steps"
                            Route.Profile.route -> "Profile"
                            else -> ""
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    actions = {
                        if (route == Route.Steps.route) {
                            IconButton(onClick = { showLogOutdoor = true }) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (route == Route.Workouts.route) {
                            IconButton(onClick = { navController.navigate(Route.History.route) }) {
                                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.height(UIConstants.TopAppBarHeight)
                )
            }
        },
        bottomBar = {
            if (showBars) BottomNav(navController)
        },
    ) { padding ->
        AppNavHost(
            navController = navController,
            vm = vm,
            snackbarHostState = snackbar,
            padding = padding,
            onLogSteps = { navController.navigate(Route.Steps.route) },
            onLogOutdoor = { showLogOutdoor = true },
            onStartWalk = { showWalkTimer = true }
        )
    }

    if (showWalkTimer) {
        ModalBottomSheet(onDismissRequest = { showWalkTimer = false }) {
            WalkTimerSheet(
                isWalking = vm.isWalking,
                seconds = vm.walkSeconds,
                onStart = { vm.startWalk() },
                onStop = { 
                    vm.stopWalk()
                    val mins = vm.walkSeconds / 60
                    if (mins > 0) {
                        vm.addWorkoutMinutes(context, mins, com.stepstack.app.data.WorkoutSession(
                            workoutId = "outdoor_walk",
                            workoutName = "Outdoor Walk",
                            dateEpochDay = vm.today,
                            durationActualMin = mins,
                            completedPercent = 100
                        ))
                    }
                    showWalkTimer = false
                }
            )
        }
    }

    if (showLogOutdoor) {
        ModalBottomSheet(onDismissRequest = { showLogOutdoor = false }) {
            LogOutdoorSheet { minutes, activity ->
                vm.addWorkoutMinutes(context, minutes, com.stepstack.app.data.WorkoutSession(
                    workoutId = "outdoor",
                    workoutName = activity,
                    dateEpochDay = vm.today,
                    durationActualMin = minutes,
                    completedPercent = 100
                ))
                showLogOutdoor = false
                scope.launch { snackbar.showSnackbar("Logged $minutes min of $activity") }
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    data class Tab(val route: String, val label: String, val sel: androidx.compose.ui.graphics.vector.ImageVector, val unsel: androidx.compose.ui.graphics.vector.ImageVector)
    val tabs = listOf(
        Tab(Route.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        Tab(Route.Workouts.route, "Workouts", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
        Tab(Route.Steps.route, "Steps", Icons.Filled.DirectionsWalk, Icons.Outlined.DirectionsWalk),
        Tab(Route.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )

    NavigationBar(
        modifier = Modifier.height(UIConstants.BottomNavHeight),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            val color = MaterialTheme.colorScheme.primary
            NavigationBarItem(
                icon = { Icon(if (selected) tab.sel else tab.unsel, contentDescription = tab.label, modifier = Modifier.size(24.dp)) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(Route.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = color,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = color.copy(alpha = 0.1f)
                ),
            )
        }
    }
}
