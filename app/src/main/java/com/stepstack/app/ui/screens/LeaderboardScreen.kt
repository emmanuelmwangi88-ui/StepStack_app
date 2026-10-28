package com.stepstack.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepstack.app.data.DailyActivity
import com.stepstack.app.data.LeaderboardEntry
import com.stepstack.app.data.LeaderboardPeriod
import com.stepstack.app.data.LeaderboardScope
import com.stepstack.app.ui.components.MyRankBar
import com.stepstack.app.ui.components.RankItem
import com.stepstack.app.ui.components.RankListRow
import com.stepstack.app.ui.components.RankPodium
import com.stepstack.app.ui.components.formatCount
import com.stepstack.app.viewmodel.LeaderboardViewModel

private const val BOARD_SIZE = 50

/**
 * @param history  the signed-in user's local daily history (pass `vm.activityHistory.toList()`).
 * @param contentPadding  the Scaffold padding the NavHost already receives.
 * @param onOpenProfile  called with a user's uid when a row or podium spot is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    history: List<DailyActivity>,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: LeaderboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load once when the screen opens; the refresh button handles the rest.
    LaunchedEffect(Unit) { viewModel.refresh(history) }

    val rankItems = state.entries.map { it.toRankItem(state.entries) }
    val podium = rankItems.take(3)
    val rest = rankItems.drop(3)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.refresh(history) }, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh leaderboard")
                    }
                }
            }

            item {
                val scopes = LeaderboardScope.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    scopes.forEachIndexed { index, scope ->
                        SegmentedButton(
                            selected = state.scope == scope,
                            onClick = { viewModel.setScope(scope) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = scopes.size)
                        ) { Text(scope.label) }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeaderboardPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = state.period == period,
                            onClick = { viewModel.setPeriod(period) },
                            label = { Text(period.label) }
                        )
                    }
                }
            }

            when {
                state.isLoading && state.entries.isEmpty() -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                state.error != null && state.entries.isEmpty() -> item {
                    MessageBlock(
                        title = "Couldn't load the leaderboard",
                        body = state.error.orEmpty(),
                        actionLabel = "Try again",
                        onAction = { viewModel.refresh(history) }
                    )
                }

                state.entries.isEmpty() -> item {
                    MessageBlock(
                        title = if (state.scope == LeaderboardScope.FRIENDS) "No friends on the board yet"
                        else "Nobody has logged steps yet",
                        body = if (state.scope == LeaderboardScope.FRIENDS)
                            "Add friends to compete head to head."
                        else "Walk a few steps and you'll be first on the board.",
                        actionLabel = null,
                        onAction = {}
                    )
                }

                else -> {
                    item {
                        RankPodium(
                            top = podium,
                            onClick = { onOpenProfile(it.uid) },
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    items(rest, key = { it.uid }) { item ->
                        RankListRow(item = item, onClick = { onOpenProfile(it.uid) })
                    }
                }
            }
        }

        // Sticky "where am I" bar.
        if (!state.isLoading || state.entries.isNotEmpty()) {
            val me = state.me
            val myValue = viewModel.myLocalValue(history)
            val last = state.entries.lastOrNull()
            val bar: Pair<String, String>? = when {
                me != null -> {
                    val leader = state.entries.firstOrNull()
                    val gap = if (leader != null) (leader.steps - me.steps).coerceAtLeast(0) else 0
                    "You're #${me.rank}" to
                        if (me.rank == 1) "${formatCount(me.steps)} steps. You're leading!"
                        else "${formatCount(me.steps)} steps. ${formatCount(gap)} behind #1"
                }
                state.entries.size >= BOARD_SIZE && last != null -> {
                    val toGo = (last.steps - myValue + 1).coerceAtLeast(1)
                    "You're outside the top $BOARD_SIZE" to
                        "${formatCount(myValue)} steps. ${formatCount(toGo)} more to get on the board"
                }
                else -> null
            }
            if (bar != null) {
                MyRankBar(
                    headline = bar.first,
                    detail = bar.second,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
        }
    }
}

private fun LeaderboardEntry.toRankItem(all: List<LeaderboardEntry>): RankItem {
    val top = all.firstOrNull()?.steps?.coerceAtLeast(1) ?: 1
    return RankItem(
        uid = uid,
        name = name,
        avatarUrl = avatarUrl,
        rank = rank,
        valueText = "${formatCount(steps)} steps",
        fraction = steps.toFloat() / top,
        isMe = isMe
    )
}

@Composable
private fun MessageBlock(
    title: String,
    body: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
