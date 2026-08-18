package com.stepstack.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.stepstack.app.data.*
import com.stepstack.app.ui.theme.*
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    uid: String,
    onBack: () -> Unit,
    vm: AppViewModel
) {
    val scope = rememberCoroutineScope()
    var friend by remember { mutableStateOf<FirestoreUser?>(null) }
    var friendshipStatus by remember { mutableStateOf<FriendshipStatus?>(null) }
    var rank by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshStatus() {
        scope.launch {
            friendshipStatus = FirebaseManager.getFriendshipStatus(uid)
        }
    }

    LaunchedEffect(uid) {
        friend = FirebaseManager.getFirestoreUser(uid)
        
        // Calculate rank
        val allUsers = FirebaseManager.getAllUsers()
        val sortedUsers = allUsers.sortedByDescending { it.lastSyncedStepCount }
        val index = sortedUsers.indexOfFirst { it.uid == uid }
        if (index != -1) {
            rank = index + 1
        }
        
        refreshStatus()
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkPrimary)
            }
        } else if (friend == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found", color = DarkOnSurface)
            }
        } else {
            val u = friend!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Profile Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    // Cover Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(DarkPrimary.copy(alpha = 0.4f), DarkBackground)
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(u.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${u.displayName}&background=random" })
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = u.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = DarkOnSurface
                        )
                        Text(
                            text = "@${u.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkOnSurfaceVariant
                        )

                        Spacer(Modifier.height(20.dp))

                        // Friend Action Button
                        if (uid != FirebaseManager.userId) {
                            FriendActionButton(
                                status = friendshipStatus,
                                onAction = {
                                    scope.launch {
                                        when (friendshipStatus) {
                                            null -> {
                                                FirebaseManager.sendFriendRequest(uid)
                                                refreshStatus()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Stats Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Today's Steps",
                        value = u.lastSyncedStepCount.toString(),
                        icon = Icons.Default.DirectionsWalk,
                        color = DarkPrimary
                    )
                    ModernStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Global Rank",
                        value = if (rank != null) "#$rank" else "#--",
                        icon = Icons.Default.EmojiEvents,
                        color = Amber500
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Achievements Section
                Text(
                    "Achievements",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = DarkOnSurface
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Achievements Grid logic
                val achievementsList = listOf("a1", "a3", "a4", "a7", "a8", "a9")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    achievementsList.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { id ->
                                val ach = SampleData.achievements.find { it.id == id }
                                if (ach != null) {
                                    ModernAchievementItem(
                                        modifier = Modifier.weight(1f),
                                        achievement = ach
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            if (row.size < 3) {
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FriendActionButton(status: FriendshipStatus?, onAction: () -> Unit) {
    val containerColor = when (status) {
        null -> DarkPrimary
        FriendshipStatus.PENDING -> DarkSurfaceVariant
        FriendshipStatus.ACCEPTED -> Color.Transparent
        else -> DarkPrimary
    }
    
    val contentColor = when (status) {
        null -> Color.Black
        FriendshipStatus.PENDING -> DarkOnSurfaceVariant
        FriendshipStatus.ACCEPTED -> DarkPrimary
        else -> Color.Black
    }

    val text = when (status) {
        null -> "Add Friend"
        FriendshipStatus.PENDING -> "Pending"
        FriendshipStatus.ACCEPTED -> "Friends"
        else -> "Add Friend"
    }
    
    val icon = when (status) {
        null -> Icons.Default.PersonAdd
        FriendshipStatus.PENDING -> Icons.Default.AccessTime
        FriendshipStatus.ACCEPTED -> Icons.Default.Check
        else -> Icons.Default.PersonAdd
    }

    Button(
        onClick = onAction,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (status == FriendshipStatus.ACCEPTED) BorderStroke(1.dp, DarkPrimary) else null,
        enabled = status == null
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModernStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = DarkOnSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DarkOnSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernAchievementItem(modifier: Modifier = Modifier, achievement: Achievement) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, if (achievement.unlocked) Amber500.copy(alpha = 0.5f) else DarkOutline),
            tonalElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = if (achievement.unlocked) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(Amber500.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                } else Modifier
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 36.sp,
                    modifier = Modifier.alpha(if (achievement.unlocked) 1f else 0.3f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            achievement.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (achievement.unlocked) DarkOnSurface else DarkOnSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}
