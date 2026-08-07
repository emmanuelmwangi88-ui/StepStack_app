package com.stepstack.app.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Workouts : Route("workouts")
    data object Steps : Route("steps")
    data object Profile : Route("profile")
    data object WorkoutDetail : Route("detail/{id}") {
        fun createRoute(id: String) = "detail/$id"
    }
    data object GuidedWorkout : Route("guided/{id}") {
        fun createRoute(id: String) = "guided/$id"
    }
    data object Builder : Route("builder")
    data object BuilderEdit : Route("builder/{id}") {
        fun createRoute(id: String) = "builder/$id"
    }
    data object History : Route("history")
    data object WorkoutComplete : Route("complete")
}
