package com.stepstack.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.stepstack.app.data.Challenge
import com.stepstack.app.data.ChallengeRepository
import com.stepstack.app.data.ChallengeSnapshot
import com.stepstack.app.data.ChallengeStanding
import com.stepstack.app.data.ChallengeType
import com.stepstack.app.data.ChallengeVisibility
import com.stepstack.app.data.DailyActivity
import com.stepstack.app.ui.components.RankAvatar
import com.stepstack.app.ui.components.RankItem
import com.stepstack.app.ui.components.RankListRow
import com.stepstack.app.ui.components.RankPodium
import com.stepstack.app.ui.components.formatCount
import com.stepstack.app.ui.theme.Teal400
import com.stepstack.app.ui.theme.Teal600
import com.stepstack.app.viewmodel.ChallengeUiState
import com.stepstack.app.viewmodel.ChallengeViewModel

/**
 * Replacement for ChallengeDetailScreen.
 *
 * @param history  the signed-in user's local daily history (pass `vm.activityHistory.toList()`).
 *                 My progress is computed from it and published to my participant row on open.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    challengeId: String,
    history: List<DailyActivity>,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: ChallengeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(challengeId) { viewModel.load(challengeId, history) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenge") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.load(challengeId, history) },
                        enabled = !state.isBusy
                    ) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }

                    if (state.snapshot?.isCreator == true) {
                        IconButton(onClick = { confirmDelete = true }, enabled = !state.isBusy) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete challenge")
                        }
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val snap = state.snapshot
            when {
                snap == null && state.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                snap == null -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error ?: "Couldn't load this challenge",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.load(challengeId, history) }) { Text("Try again") }
                }

                else -> ChlContent(
                    snap = snap,
                    state = state,
                    onJoin = { viewModel.join(history) },
                    onDecline = { viewModel.decline(history) },
                    onDismissError = viewModel::dismissError,
                    onOpenProfile = onOpenProfile
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this challenge?") },
            text = { Text("It will be removed for everyone taking part. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ChlContent(
    snap: ChallengeSnapshot,
    state: ChallengeUiState,
    onJoin: () -> Unit,
    onDecline: () -> Unit,
    onDismissError: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val challenge = snap.challenge
    val ended = challenge.endDate > 0 && System.currentTimeMillis() > challenge.endDate
    val rankItems = snap.toRankItems()
    val showPodium = rankItems.size >= 3
    val listItems = if (showPodium) rankItems.drop(3) else rankItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (state.error != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.error,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onDismissError) { Text("Dismiss") }
                    }
                }
            }
        }

        item { ChlHero(challenge) }

        if (ended && snap.standings.isNotEmpty()) {
            item { ChlWinnerBanner(snap.standings.first(), challenge) }
        }

        when {
            snap.iAmInvited && !ended -> item {
                ChlInviteCard(busy = state.isBusy, onAccept = onJoin, onDecline = onDecline)
            }

            !snap.iAmJoined && snap.myStatus == null &&
                challenge.visibility == ChallengeVisibility.COMMUNITY && !ended -> item {
                ChlJoinCard(busy = state.isBusy, onJoin = onJoin)
            }

            snap.iAmJoined -> item { ChlMyProgress(snap) }
        }

        item {
            Text(
                text = "Standings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (rankItems.isEmpty()) {
            item {
                Text(
                    text = "Nobody has joined yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            if (showPodium) {
                item {
                    RankPodium(
                        top = rankItems.take(3),
                        onClick = { onOpenProfile(it.uid) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
            items(listItems, key = { it.uid }) { row ->
                RankListRow(item = row, onClick = { onOpenProfile(it.uid) })
            }
        }

        if (snap.invited.isNotEmpty()) {
            item {
                Text(
                    text = "Invited (${snap.invited.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(snap.invited, key = { "invited_" + it.uid }) { person ->
                ChlInvitedRow(person, onClick = { onOpenProfile(person.uid) })
            }
        }

        if (snap.iAmJoined && !snap.isCreator && !ended) {
            item {
                TextButton(
                    onClick = onDecline,
                    enabled = !state.isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Leave challenge", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

// ---------- hero ----------

@Composable
private fun ChlHero(challenge: Challenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            if (challenge.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = challenge.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Teal600, Teal400)))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)))
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
            ) {
                Text(
                    text = challenge.title.ifBlank { "Untitled challenge" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChlPill(text = challenge.type.shortLabel())
                    ChlPill(
                        text = if (challenge.visibility == ChallengeVisibility.COMMUNITY) "Public" else "Friends",
                        icon = if (challenge.visibility == ChallengeVisibility.COMMUNITY) Icons.Default.Public
                        else Icons.Default.Lock
                    )
                    ChlPill(text = timeLeftLabel(challenge))
                }
            }
        }
    }
}

@Composable
private fun ChlPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChlWinnerBanner(winner: ChallengeStanding, challenge: Challenge) {
    val value = if (challenge.type == ChallengeType.DAILY_GOAL_STREAK) "${winner.score} days"
    else "${formatCount(winner.score)} steps"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankAvatar(winner.name, winner.avatarUrl, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Final result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (winner.isMe) "You won with $value" else "${winner.name} won with $value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

// ---------- my progress / invite / join ----------

@Composable
private fun ChlMyProgress(snap: ChallengeSnapshot) {
    val c = snap.challenge
    val me = snap.me
    val steps = me?.steps ?: 0
    val daysHit = me?.daysHit ?: 0
    val target = c.dailyGoalTarget ?: 0
    val leader = snap.standings.firstOrNull()

    // ring: 0f..1f, or null when the challenge has no fixed goal to fill
    val ring: Float?
    val headline: String
    val detail: String

    when (c.type) {
        ChallengeType.RACE_TOTAL_STEPS -> {
            ring = if (target > 0) (steps.toFloat() / target).coerceIn(0f, 1f) else null
            headline = if (target > 0) "${formatCount(steps)} / ${formatCount(target)} steps"
            else "${formatCount(steps)} steps"
            detail = when {
                target > 0 && steps >= target -> "Goal reached!"
                target > 0 -> "${formatCount(target - steps)} steps to go"
                else -> "First to the finish wins"
            }
        }

        ChallengeType.DAILY_GOAL_STREAK -> {
            val total = ChallengeRepository.totalDays(c)
            ring = total?.let { (daysHit.toFloat() / it).coerceIn(0f, 1f) }
            headline = if (total != null) "$daysHit / $total days" else "$daysHit days"
            detail = if (target > 0) "Hit ${formatCount(target)} steps a day" else "Hit your daily goal"
        }

        ChallengeType.TOTAL_STEPS_DURATION -> {
            ring = null
            headline = "${formatCount(steps)} steps"
            detail = when {
                me == null || leader == null -> "Most steps by the deadline wins"
                me.rank == 1 -> "You're in the lead"
                else -> "${formatCount((leader.score - me.score).coerceAtLeast(0))} behind the leader"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                if (ring != null) {
                    CircularProgressIndicator(
                        progress = { ring },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                    Text(
                        text = "${(ring * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = me?.rank?.let { "#$it" } ?: "-",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text(
                    text = "Your progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun ChlInviteCard(busy: Boolean, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "You're invited",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Accept to start counting your steps toward this challenge.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAccept, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Accept") }
                OutlinedButton(onClick = onDecline, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Decline") }
            }
        }
    }
}

@Composable
private fun ChlJoinCard(busy: Boolean, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Open to everyone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Join to get ranked. Your steps since the challenge started count toward the standings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(14.dp))
            Button(onClick = onJoin, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Join challenge") }
        }
    }
}

@Composable
private fun ChlInvitedRow(person: ChallengeStanding, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankAvatar(person.name, person.avatarUrl, 34.dp)
        Spacer(Modifier.width(12.dp))
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(person.name, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Pending",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------- helpers ----------

private fun ChallengeSnapshot.toRankItems(): List<RankItem> {
    val streak = challenge.type == ChallengeType.DAILY_GOAL_STREAK
    val goal = challenge.dailyGoalTarget ?: 0
    // Race: bars fill toward the finish line. Other types: relative to the current leader.
    val denominator = if (challenge.type == ChallengeType.RACE_TOTAL_STEPS && goal > 0) goal
    else (standings.firstOrNull()?.score ?: 1).coerceAtLeast(1)

    return standings.map { s ->
        RankItem(
            uid = s.uid,
            name = s.name,
            avatarUrl = s.avatarUrl,
            rank = s.rank,
            valueText = if (streak) "${s.score} days" else "${formatCount(s.score)} steps",
            fraction = s.score.toFloat() / denominator,
            isMe = s.isMe
        )
    }
}

private fun ChallengeType.shortLabel(): String = when (this) {
    ChallengeType.RACE_TOTAL_STEPS -> "Race"
    ChallengeType.DAILY_GOAL_STREAK -> "Streak"
    ChallengeType.TOTAL_STEPS_DURATION -> "Most steps"
}

private fun timeLeftLabel(challenge: Challenge): String {
    if (challenge.endDate <= 0L) return "No end date"
    val remainingMs = challenge.endDate - System.currentTimeMillis()
    if (remainingMs <= 0) return "Ended"
    val days = remainingMs / 86_400_000L
    val hours = (remainingMs % 86_400_000L) / 3_600_000L
    return when {
        days >= 1 -> "${days}d ${hours}h left"
        hours >= 1 -> "${hours}h left"
        else -> "Under 1h left"
    }
}
