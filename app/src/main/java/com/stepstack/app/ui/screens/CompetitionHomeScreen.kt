package com.stepstack.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.stepstack.app.data.*
import com.stepstack.app.util.CloudinaryManager
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val StepDigits = FontFamily.Monospace

private fun formatNumber(num: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(num)
}

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
                0 -> LeaderboardScreen(history = vm.activityHistory.toList(), onOpenProfile = onOpenFriend)
                1 -> FriendsTab(vm, onOpenFriend)
                2 -> ChallengesTab(vm, onOpenChallenge)
            }
        }
    }
}

@Composable
fun CompetitionHeader(vm: AppViewModel) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val currentUid = FirebaseManager.userId ?: "demo_me"
    val leaderboardUsers = remember(vm.allUsers.toList(), vm.todayActivity.steps) {
        val list = vm.allUsers.toMutableList()
        val index = list.indexOfFirst { it.uid == currentUid }
        if (index >= 0) {
            val existing = list[index]
            list[index] = existing.copy(
                displayName = vm.user.name.ifBlank { existing.displayName },
                lastSyncedStepCount = maxOf(existing.lastSyncedStepCount, vm.todayActivity.steps)
            )
        } else {
            list.add(
                FirestoreUser(
                    uid = currentUid,
                    displayName = vm.user.name.ifBlank { "You" },
                    username = vm.user.email.split("@").firstOrNull() ?: "athlete",
                    avatarUrl = vm.user.photoUrl,
                    lastSyncedStepCount = vm.todayActivity.steps
                )
            )
        }
        list.sortedByDescending { it.lastSyncedStepCount }
    }

    val myRank = remember(leaderboardUsers, currentUid) {
        val r = leaderboardUsers.indexOfFirst { it.uid == currentUid } + 1
        if (r <= 0) 1 else r
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFB347).copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Leaderboard & Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (vm.currentStreak > 0) "🔥 ${vm.currentStreak} day streak" else "Start today's streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        isRefreshing = true
                        scope.launch {
                            vm.refreshCompetitionData()
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Leaderboard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Your Rank",
                    value = "#$myRank",
                    icon = Icons.Default.Leaderboard,
                    color = Color(0xFFFFB347),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Today's Steps",
                    value = formatNumber(vm.todayActivity.steps),
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Streak",
                    value = "${vm.currentStreak}d",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = StepDigits,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SegmentedTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selected == index
            val icon = when (index) {
                0 -> Icons.Default.EmojiEvents
                1 -> Icons.Default.Group
                else -> Icons.Default.FlashOn
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
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
                if (it.length >= 2) {
                    isSearching = true
                    scope.launch {
                        searchResults = FirebaseManager.searchUsers(it)
                        isSearching = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 8.dp),
            placeholder = { Text("Search runners by username...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (isSearching) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                }
            },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        if (searchQuery.length >= 2) {
            SectionLabel("Search Results")
            if (!isSearching && searchResults.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.PersonSearch,
                    title = "No users found",
                    body = "Try searching with a different username."
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { user ->
                        val isFriend = vm.friends.any { it.uid == user.uid }
                        FlatFriendRow(
                            user = user,
                            isFriend = isFriend,
                            onAction = {
                                scope.launch {
                                    FirebaseManager.sendFriendRequest(user.uid)
                                    vm.refreshCompetitionData()
                                }
                            },
                            actionLabel = if (isFriend) "Friends" else "Add Friend",
                            onClick = { onOpenFriend(user.uid) }
                        )
                    }
                }
            }
        } else {
            SectionLabel("My Walking Buddies")
            if (vm.isFriendsLoading && vm.friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (vm.friends.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.GroupAdd,
                    title = "Walk with friends",
                    body = "Search for users above to add your first friend and compare daily step scores."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(vm.friends) { friend ->
                        FlatFriendRow(
                            user = friend,
                            isFriend = true,
                            onAction = { onOpenFriend(friend.uid) },
                            actionLabel = "View Profile",
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
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}&background=random" })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${formatNumber(user.lastSyncedStepCount)} steps",
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
                    containerColor = if (isFriend && actionLabel != "Challenge") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ChallengesTab(vm: AppViewModel, onOpenChallenge: (String) -> Unit) {
    var showCreate by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All") }

    val filteredChallenges = remember(vm.activeChallenges, searchQuery, selectedFilterChip) {
        vm.activeChallenges.filter { c ->
            val matchesSearch = searchQuery.isBlank() ||
                c.title.contains(searchQuery, ignoreCase = true) ||
                c.type.name.contains(searchQuery, ignoreCase = true)

            val matchesChip = when (selectedFilterChip) {
                "Community" -> c.visibility == ChallengeVisibility.COMMUNITY
                "Friends" -> c.visibility == ChallengeVisibility.FRIENDS_ONLY
                "Races" -> c.type == ChallengeType.RACE_TOTAL_STEPS || c.type == ChallengeType.TOTAL_STEPS_DURATION
                "Streaks" -> c.type == ChallengeType.DAILY_GOAL_STREAK
                else -> true
            }
            matchesSearch && matchesChip
        }
    }

    val communityChallenges = filteredChallenges.filter { it.visibility == ChallengeVisibility.COMMUNITY }
    val friendsChallenges = filteredChallenges.filter { it.visibility == ChallengeVisibility.FRIENDS_ONLY }
    val featuredChallenge = filteredChallenges.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        if (vm.isChallengesLoading && vm.activeChallenges.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (vm.activeChallenges.isEmpty() && searchQuery.isBlank()) {
            EmptyState(
                icon = Icons.Default.EmojiEvents,
                title = "No live challenges",
                body = "Start a race or a streak goal and invite your friends to join.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Header Search & Filter Bar
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        // Quick Stats Header Banner
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFB347), modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("${vm.activeChallenges.size} Active Challenges", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        "${vm.activeChallenges.sumOf { it.participantIds.size.coerceAtLeast(1) }} Racers",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search challenges...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, null)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Category Filter Chips Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            val chips = listOf("All", "Community", "Friends", "Races", "Streaks")
                            items(chips) { chip ->
                                val selected = selectedFilterChip == chip
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFilterChip = chip },
                                    label = { Text(chip, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Featured Hero Banner (Top Challenge)
                if (featuredChallenge != null && searchQuery.isBlank() && selectedFilterChip == "All") {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            Text("Featured Challenge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            FeaturedChallengeCard(challenge = featuredChallenge, onClick = { onOpenChallenge(featuredChallenge.id) })
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                // Friends Challenges Section
                if (friendsChallenges.isNotEmpty()) {
                    item {
                        SectionLabel("Friends challenges")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(friendsChallenges) { challenge ->
                                ChallengeFlatCard(challenge = challenge, onClick = { onOpenChallenge(challenge.id) })
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                // Community Challenges Section
                if (communityChallenges.isNotEmpty()) {
                    item {
                        SectionLabel("Community challenges")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(communityChallenges) { challenge ->
                                ChallengeFlatCard(challenge = challenge, onClick = { onOpenChallenge(challenge.id) })
                            }
                        }
                    }
                }

                if (filteredChallenges.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No matching challenges",
                            body = "Try searching for a different title or filter category.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
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
                vm.createChallenge(title, type, visibility, goal, imageUrl, endDate, invites)
                showCreate = false
            }
        )
    }
}

@Composable
fun FeaturedChallengeCard(challenge: Challenge, onClick: () -> Unit) {
    val displayImage = remember(challenge.id, challenge.imageUrl) {
        challenge.imageUrl.ifBlank {
            "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?q=80&w=2070&auto=format&fit=crop"
        }
    }

    val daysLeft = remember(challenge.endDate) {
        val diff = challenge.endDate - System.currentTimeMillis()
        if (diff > 0) (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(1) else 1
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF9800)
                    ) {
                        Text(
                            "🏆 FEATURED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$daysLeft days left", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        challenge.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${formatNumber(challenge.dailyGoalTarget ?: 10000)} steps/day • ${if (challenge.visibility == ChallengeVisibility.COMMUNITY) "Community" else "Friends"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = StepDigits,
                        color = Color(0xFFFFB347),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${challenge.participantIds.size.coerceAtLeast(1)} Participants", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Join Challenge 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
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
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChallengeFlatCard(challenge: Challenge, onClick: () -> Unit) {
    val displayImage = remember(challenge.id, challenge.imageUrl) {
        challenge.imageUrl.ifBlank {
            val photoIds = listOf(
                "1552674605-db6ffd4facb5",
                "1517836357463-d25dfeac3438",
                "1534438327276-14e5300c3a48",
                "1571019613454-1cb2f99b2d8b"
            )
            val photoId = photoIds[abs(challenge.id.hashCode()) % photoIds.size]
            "https://images.unsplash.com/photo-$photoId?q=80&w=2070&auto=format&fit=crop"
        }
    }

    Card(
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                Surface(
                    modifier = Modifier.padding(10.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (challenge.visibility == ChallengeVisibility.COMMUNITY) Icons.Default.Public else Icons.Default.Lock,
                            null,
                            modifier = Modifier.size(11.dp),
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

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${formatNumber(challenge.dailyGoalTarget ?: 0)} steps",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = StepDigits,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            challenge.type.name.split("_").first().lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
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

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?q=80&w=1000&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1000&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1000&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?q=80&w=1000&auto=format&fit=crop"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                isUploading = true
                CloudinaryManager.uploadImage(
                    it,
                    onSuccess = { url ->
                        isUploading = false
                        imageUrl = url
                    },
                    onError = {
                        isUploading = false
                    }
                )
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Challenge", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Challenge Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it.filter { c -> c.isDigit() } },
                    label = { Text("Step Target Goal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Visibility", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedVisibility == ChallengeVisibility.FRIENDS_ONLY,
                        onClick = { selectedVisibility = ChallengeVisibility.FRIENDS_ONLY },
                        label = { Text("Friends Only") }
                    )
                    FilterChip(
                        selected = selectedVisibility == ChallengeVisibility.COMMUNITY,
                        onClick = { selectedVisibility = ChallengeVisibility.COMMUNITY },
                        label = { Text("Public Community") }
                    )
                }

                if (selectedVisibility == ChallengeVisibility.FRIENDS_ONLY && friends.isNotEmpty()) {
                    Text("Invite Friends", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(friends) { friend ->
                            val selected = selectedFriends.contains(friend.uid)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedFriends.remove(friend.uid)
                                    else selectedFriends.add(friend.uid)
                                },
                                label = { Text(friend.displayName) }
                            )
                        }
                    }
                }

                Text("Cover Image", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUploading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("Upload Image")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goalNum = goal.toIntOrNull() ?: 10000
                    val finalImage = imageUrl.ifBlank { presetImages.first() }
                    val endDate = System.currentTimeMillis() + durationDays * 86400000L
                    onCreate(title.ifBlank { "Step Challenge" }, selectedType, selectedVisibility, goalNum, finalImage, endDate, selectedFriends)
                },
                enabled = title.isNotBlank() && !isUploading
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
