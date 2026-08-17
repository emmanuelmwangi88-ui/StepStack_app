package com.stepstack.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.util.CloudinaryManager
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

// A monospace family gives step counts a pedometer / digital-readout feel,
// distinct from the rest of the UI's humanist sans.
private val StepDigits = FontFamily.Monospace

@Composable
fun CompetitionHomeScreen(
    vm: AppViewModel,
    padding: PaddingValues,
    onOpenFriend: (String) -> Unit,
    onOpenChallenge: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Leaderboard", "Friends", "Challenges")

    LaunchedEffect(Unit) {
        vm.refreshCompetitionData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompetitionHeader(vm)
        SegmentedTabs(tabs = tabs, selected = selectedTab, onSelect = { selectedTab = it })

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> LeaderboardTab(vm, onOpenFriend)
                1 -> FriendsTab(vm, onOpenFriend)
                2 -> ChallengesTab(vm, onOpenChallenge)
            }
        }
    }
}

@Composable
fun CompetitionHeader(vm: AppViewModel) {
    val myRank = remember(vm.allUsers.size, vm.todayActivity.steps) {
        val all = (vm.allUsers.map { it.lastSyncedStepCount } + vm.todayActivity.steps).distinct().sortedDescending()
        val r = all.indexOf(vm.todayActivity.steps) + 1
        if (r == 0) 1 else r
    }

    Column(modifier = Modifier.padding(24.dp, 28.dp, 24.dp, 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Competition hub",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFB347)
                )
                Text(
                    if (vm.currentStreak > 0) "Day ${vm.currentStreak} of your streak" else "Start today's streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFFFF3E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFEF6C00)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(label = "Today", value = vm.todayActivity.steps.toString(), modifier = Modifier.weight(1f))
            StatChip(label = "Your rank", value = "#$myRank", modifier = Modifier.weight(1f))
            StatChip(label = "Streak", value = "${vm.currentStreak}d", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp, 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = StepDigits,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SegmentedTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(3.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun LeaderboardTab(vm: AppViewModel, onOpenFriend: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val me = FirebaseManager.userId?.let { uid ->
        FirestoreUser(
            uid = uid,
            displayName = vm.user.name,
            username = vm.user.email.split("@").firstOrNull() ?: "me",
            avatarUrl = vm.user.photoUrl,
            lastSyncedStepCount = vm.todayActivity.steps
        )
    }

    val leaderboardUsers = remember(vm.allUsers.size, vm.todayActivity.steps) {
        (vm.allUsers + listOfNotNull(me))
            .distinctBy { it.uid }
            .sortedByDescending { it.lastSyncedStepCount }
    }

    if (vm.isFriendsLoading && leaderboardUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (leaderboardUsers.isEmpty()) {
        EmptyState(
            icon = Icons.Default.EmojiEvents,
            title = "No one on the board yet",
            body = "Sign in to start comparing today's steps with others.",
        )
    } else {
        val podium = leaderboardUsers.take(3)
        val rest = leaderboardUsers.drop(3)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                PodiumRow(
                    podium = podium, 
                    myUid = FirebaseManager.userId, 
                    friends = vm.friends,
                    onAddFriend = { uid ->
                        scope.launch {
                            FirebaseManager.sendFriendRequest(uid)
                            vm.refreshCompetitionData()
                        }
                    },
                    onOpenFriend = onOpenFriend
                )
            }
            if (rest.isNotEmpty()) {
                item { 
                    TrailedRows(
                        users = rest, 
                        startRank = 4, 
                        myUid = FirebaseManager.userId, 
                        friends = vm.friends,
                        onAddFriend = { uid ->
                            scope.launch {
                                FirebaseManager.sendFriendRequest(uid)
                                vm.refreshCompetitionData()
                            }
                        },
                        onOpenFriend = onOpenFriend
                    ) 
                }
            }
        }
    }
}

@Composable
fun PodiumRow(
    podium: List<FirestoreUser>, 
    myUid: String?, 
    friends: List<FirestoreUser>,
    onAddFriend: (String) -> Unit,
    onOpenFriend: (String) -> Unit
) {
    val first = podium.getOrNull(0)
    val second = podium.getOrNull(1)
    val third = podium.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        second?.let { 
            PodiumSpot(
                user = it, 
                place = 2, 
                size = 56.dp, 
                myUid = myUid, 
                isFriend = friends.any { f -> f.uid == it.uid },
                onAddFriend = { onAddFriend(it.uid) },
                onClick = { onOpenFriend(it.uid) }
            ) 
        }
        Spacer(modifier = Modifier.width(12.dp))
        first?.let { 
            PodiumSpot(
                user = it, 
                place = 1, 
                size = 76.dp, 
                myUid = myUid, 
                isFriend = friends.any { f -> f.uid == it.uid },
                onAddFriend = { onAddFriend(it.uid) },
                onClick = { onOpenFriend(it.uid) }
            ) 
        }
        Spacer(modifier = Modifier.width(12.dp))
        third?.let { 
            PodiumSpot(
                user = it, 
                place = 3, 
                size = 52.dp, 
                myUid = myUid, 
                isFriend = friends.any { f -> f.uid == it.uid },
                onAddFriend = { onAddFriend(it.uid) },
                onClick = { onOpenFriend(it.uid) }
            ) 
        }
    }
}

@Composable
fun PodiumSpot(
    user: FirestoreUser, 
    place: Int, 
    size: androidx.compose.ui.unit.Dp, 
    myUid: String?, 
    isFriend: Boolean,
    onAddFriend: () -> Unit,
    onClick: () -> Unit
) {
    val isMe = user.uid == myUid
    val medal = when (place) {
        1 -> "🥇"
        2 -> "🥈"
        else -> "🥉"
    }
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = user.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}&background=random" },
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Text(medal, fontSize = if (place == 1) 22.sp else 18.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (isMe) "You" else user.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
        Text(
            user.lastSyncedStepCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = StepDigits,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (!isMe && !isFriend) {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onAddFriend() },
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun TrailedRows(
    users: List<FirestoreUser>, 
    startRank: Int, 
    myUid: String?, 
    friends: List<FirestoreUser>,
    onAddFriend: (String) -> Unit,
    onOpenFriend: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)
            .padding(start = 39.dp)) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.35f),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 4f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 14f), 0f)
            )
        }
        Column {
            users.forEachIndexed { index, user ->
                val isFriend = friends.any { it.uid == user.uid }
                TrailRow(
                    user = user,
                    rank = startRank + index,
                    isMe = user.uid == myUid,
                    isFriend = isFriend,
                    onAddFriend = { onAddFriend(user.uid) },
                    onClick = { onOpenFriend(user.uid) }
                )
            }
        }
    }
}

@Composable
fun TrailRow(
    user: FirestoreUser, 
    rank: Int, 
    isMe: Boolean, 
    isFriend: Boolean,
    onAddFriend: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else Color.Transparent)
            .padding(24.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("#$rank", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        AsyncImage(
            model = user.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}&background=random" },
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isMe) "You" else user.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isMe && !isFriend) {
            IconButton(onClick = onAddFriend) {
                Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        Text(
            text = user.lastSyncedStepCount.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = StepDigits,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FriendsTab(vm: AppViewModel, onOpenFriend: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FirestoreUser>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.length >= 3) {
                    isSearching = true
                    scope.launch {
                        searchResults = FirebaseManager.searchUsers(it)
                        isSearching = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(24.dp, 8.dp),
            placeholder = { Text("Search by username") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        if (searchQuery.length >= 3) {
            SectionLabel("Search results")
            if (!isSearching && searchResults.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.PersonSearch,
                    title = "No matches",
                    body = "Try a different username."
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { user ->
                        val isFriend = vm.friends.any { it.uid == user.uid }
                        FlatFriendRow(
                            user = user, 
                            isFriend = isFriend,
                            onAction = {
                                scope.launch { FirebaseManager.sendFriendRequest(user.uid) }
                            }, 
                            actionLabel = if (isFriend) "Friends" else "Add", 
                            onClick = { onOpenFriend(user.uid) }
                        )
                    }
                }
            }
        } else {
            SectionLabel("My friends")
            if (vm.isFriendsLoading && vm.friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (vm.friends.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.GroupAdd,
                    title = "Walk with friends",
                    body = "Search a username above to add your first friend and start comparing steps."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(vm.friends) { friend ->
                        FlatFriendRow(
                            user = friend,
                            isFriend = true,
                            onAction = { /* Challenge? */ },
                            actionLabel = "Challenge",
                            onClick = { onOpenFriend(friend.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlatFriendRow(
    user: FirestoreUser, 
    isFriend: Boolean,
    onAction: () -> Unit, 
    actionLabel: String, 
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}&background=random" },
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${user.lastSyncedStepCount} steps",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFriend && actionLabel != "Challenge") Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = if (isFriend && actionLabel != "Challenge") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ChallengesTab(vm: AppViewModel, onOpenChallenge: (String) -> Unit) {
    var showCreate by remember { mutableStateOf(false) }

    val communityChallenges = vm.activeChallenges.filter { it.visibility == ChallengeVisibility.COMMUNITY }
    val friendsChallenges = vm.activeChallenges.filter { it.visibility == ChallengeVisibility.FRIENDS_ONLY }

    Box(modifier = Modifier.fillMaxSize()) {
        if (vm.isChallengesLoading && vm.activeChallenges.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (vm.activeChallenges.isEmpty()) {
            EmptyState(
                icon = Icons.Default.EmojiEvents,
                title = "No live challenges",
                body = "Start a race or a streak goal and invite your friends to join.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    SectionLabel("Friends challenges")
                    if (friendsChallenges.isEmpty()) {
                        EmptyStateLabel("No friends challenges yet.")
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(friendsChallenges) { challenge ->
                                ChallengeFlatCard(challenge = challenge, onClick = { onOpenChallenge(challenge.id) })
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    SectionLabel("Community challenges")
                    if (communityChallenges.isEmpty()) {
                        EmptyStateLabel("No community challenges yet.")
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(communityChallenges) { challenge ->
                                ChallengeFlatCard(challenge = challenge, onClick = { onOpenChallenge(challenge.id) })
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("New challenge") }
        )
    }

    if (showCreate) {
        CreateChallengeDialog(
            friends = vm.friends,
            onDismiss = { showCreate = false },
            onCreate = { title, type, visibility, goal, imageUrl, endDate, invites ->
                vm.createChallenge(title, type, visibility, goal, imageUrl, invites)
                showCreate = false
            }
        )
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black
    )
}

@Composable
fun EmptyStateLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 24.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

/**
 * Shared empty state: names the space, explains it in one line, and gives
 * an icon focal point instead of a bare line of gray text.
 */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(
        modifier = modifier.padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ChallengeFlatCard(challenge: Challenge, onClick: () -> Unit) {
    val progress = if (challenge.dailyGoalTarget != null && challenge.dailyGoalTarget > 0) {
        0.4f // Placeholder 40%
    } else 0f

    Column(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = challenge.imageUrl.ifBlank { "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?q=80&w=2070&auto=format&fit=crop" },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            // Progress Gauge Overlay
            if (challenge.dailyGoalTarget != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(Color(0xFFFFB347))
                    )
                }
            }

            Surface(
                modifier = Modifier.padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (challenge.visibility == ChallengeVisibility.COMMUNITY) Icons.Default.Public else Icons.Default.Lock,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (challenge.visibility == ChallengeVisibility.COMMUNITY) "Community" else "Friends",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            challenge.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )

        Text(
            "${challenge.dailyGoalTarget ?: "Race"} steps",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = StepDigits,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeDialog(
    friends: List<FirestoreUser>,
    onDismiss: () -> Unit,
    onCreate: (String, ChallengeType, ChallengeVisibility, Int, String, Long, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("10000") }
    var imageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ChallengeType.RACE_TOTAL_STEPS) }
    var selectedVisibility by remember { mutableStateOf(ChallengeVisibility.FRIENDS_ONLY) }
    var durationDays by remember { mutableIntStateOf(7) }
    val selectedFriends = remember { mutableStateListOf<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                isUploading = true
                CloudinaryManager.uploadImage(
                    it,
                    onSuccess = { url ->
                        imageUrl = url
                        isUploading = false
                    },
                    onError = { isUploading = false }
                )
            }
        }
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Start challenge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Challenge title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Daily steps") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else if (imageUrl.isNotEmpty()) {
                            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Column {
                    Text("Duration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(1, 3, 7, 30)) { days ->
                            val selected = durationDays == days
                            FilterChip(
                                selected = selected,
                                onClick = { durationDays = days },
                                label = { Text(if (days >= 30) "1 month" else if (days == 7) "1 week" else "$days days") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        ChallengeType.entries.forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = type }.padding(vertical = 4.dp)) {
                                RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                                Text(type.name.split("_").first().lowercase().customCapitalize(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Visibility", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        ChallengeVisibility.entries.forEach { vis ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedVisibility = vis }.padding(vertical = 4.dp)) {
                                RadioButton(selected = selectedVisibility == vis, onClick = { selectedVisibility = vis })
                                Text(vis.name.replace("_", " ").lowercase().customCapitalize(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (selectedVisibility == ChallengeVisibility.FRIENDS_ONLY) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Invite friends", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        if (friends.isEmpty()) {
                            Text("No friends yet.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        } else {
                            Column(modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
                                friends.forEach { f ->
                                    val isSelected = selectedFriends.contains(f.uid)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            if (isSelected) selectedFriends.remove(f.uid) else selectedFriends.add(f.uid)
                                        }.padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(f.displayName, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val end = System.currentTimeMillis() + (durationDays.toLong() * 24 * 60 * 60 * 1000)
                            onCreate(title, selectedType, selectedVisibility, goal.toIntOrNull() ?: 0, imageUrl, end, selectedFriends.toList())
                        },
                        enabled = title.isNotBlank() && !isUploading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Launch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun String.customCapitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
