package com.stepstack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stepstack.app.viewmodel.AppViewModel

@Composable
fun CompetitionHomeScreen(
    vm: AppViewModel,
    padding: PaddingValues
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Leaderboard", "Friends", "Challenges")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> LeaderboardTab(vm)
                1 -> FriendsTab(vm)
                2 -> ChallengesTab(vm)
            }
        }
    }
}

@Composable
fun LeaderboardTab(vm: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Leaderboard Coming Soon", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun FriendsTab(vm: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Friends Coming Soon", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ChallengesTab(vm: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Challenges Coming Soon", style = MaterialTheme.typography.bodyLarge)
    }
}
