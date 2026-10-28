package com.stepstack.app.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Workouts : Route("workouts")
    data object Steps : Route("steps")
    data object Profile : Route("profile")
    data object Competition : Route("competition")
    data object Leaderboard : Route("leaderboard")
    data object FriendProfile : Route("friend_profile/{uid}") {
        fun createRoute(uid: String) = "friend_profile/$uid"
    }
    data object ChallengeDetail : Route("challenge_detail/{id}") {
        fun createRoute(id: String) = "challenge_detail/$id"
    }
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
    data object Notifications : Route("notifications")
}
