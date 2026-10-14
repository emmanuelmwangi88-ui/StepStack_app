package com.stepstack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    onBack: () -> Unit,
    vm: AppViewModel
) {
    val scope = rememberCoroutineScope()
    var challenge by remember { mutableStateOf<Challenge?>(null) }
    var participants by remember { mutableStateOf<List<ChallengeParticipant>>(emptyList()) }
    var participantUsers by remember { mutableStateOf<Map<String, FirestoreUser>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val currentUserId = FirebaseManager.userId
    val isJoined = participants.any { it.userId == currentUserId && it.status == ParticipantStatus.ACCEPTED }
    val isInvited = participants.any { it.userId == currentUserId && it.status == ParticipantStatus.INVITED }
    val isCreator = challenge?.createdBy == currentUserId

    var showEditDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        scope.launch {
            isLoading = true
            challenge = FirebaseManager.getChallenge(challengeId)
            participants = FirebaseManager.getChallengeParticipants(challengeId)
            
            val userIds = participants.map { it.userId }
            val users = mutableMapOf<String, FirestoreUser>()
            
            // Fetch users in chunks
            userIds.chunked(30).forEach { ids ->
                val friends = FirebaseManager.getFriends().filter { ids.contains(it.uid) }
                friends.forEach { users[it.uid] = it }
                
                if (ids.contains(currentUserId)) {
                    users[currentUserId!!] = FirestoreUser(
                        uid = currentUserId, 
                        displayName = vm.user.name, 
                        avatarUrl = vm.user.photoUrl
                    )
                }
            }
            
            // Also fetch unknown users (if any)
            val unknownIds = userIds.filter { !users.containsKey(it) }
            unknownIds.forEach { id ->
                FirebaseManager.getFirestoreUser(id)?.let { users[id] = it }
            }
            
            participantUsers = users
            isLoading = false
        }
    }

    LaunchedEffect(challengeId) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.title ?: "Challenge") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (isCreator) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                        IconButton(onClick = {
                            vm.deleteChallenge(challengeId)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (challenge == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Challenge not found")
            }
        } else {
            val c = challenge!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val myP = participants.find { it.userId == currentUserId }
                    ChallengeHeaderExtended(c, userSteps = myP?.cumulativeSteps ?: 0)
                }

                if (!isJoined) {
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    FirebaseManager.joinChallenge(challengeId)
                                    refreshData()
                                    vm.refreshCompetitionData()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isInvited) "Accept Invite" else "Join Challenge")
                        }
                    }
                }
                
                item {
                    Text("Leaderboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
                
                val sortedParticipants = participants
                    .filter { it.status == ParticipantStatus.ACCEPTED }
                    .sortedByDescending { it.cumulativeSteps }
                
                if (sortedParticipants.isEmpty()) {
                    item {
                        Text("No participants yet.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                    }
                }

                items(sortedParticipants) { p ->
                    val user = participantUsers[p.userId]
                    ParticipantRankRow(
                        user = user,
                        steps = p.cumulativeSteps,
                        rank = sortedParticipants.indexOf(p) + 1
                    )
                }

                val pending = participants.filter { it.status == ParticipantStatus.INVITED }
                if (pending.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pending Invites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(pending) { p ->
                        val user = participantUsers[p.userId]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).drawAlpha(0.6f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = user?.avatarUrl?.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}" },
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(user?.displayName ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("Invited", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
    
    if (showEditDialog && challenge != null) {
        // Simple edit dialog for the creator
        EditChallengeDialog(
            challenge = challenge!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                scope.launch {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("challenges").document(challengeId)
                        .set(updated, com.google.firebase.firestore.SetOptions.merge())
                        .await()
                    challenge = updated
                    showEditDialog = false
                    vm.refreshCompetitionData()
                }
            }
        )
    }
}

private fun Modifier.drawAlpha(alphaValue: Float): Modifier = this.then(
    Modifier.alpha(alphaValue)
)

@Composable
fun ChallengeHeaderExtended(challenge: Challenge, userSteps: Int = 0) {
    val goal = challenge.dailyGoalTarget ?: 1
    val progress = (userSteps.toFloat() / goal).coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = challenge.imageUrl.ifBlank { "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?q=80&w=2070&auto=format&fit=crop" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (challenge.visibility == ChallengeVisibility.COMMUNITY) Icons.Default.Public else Icons.Default.Lock,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (challenge.visibility == ChallengeVisibility.COMMUNITY) "Community Challenge" else "Friends Only",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active Challenge", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Progress Gauge
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Your Progress", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Goal", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${challenge.dailyGoalTarget ?: 0} steps/day", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Type", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(challenge.type.name.split("_").first().lowercase().customCapitalize(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantRankRow(user: FirestoreUser?, steps: Int, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                },
                modifier = Modifier.width(36.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = when(rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            AsyncImage(
                model = user?.avatarUrl?.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}" },
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user?.displayName ?: "Unknown", fontWeight = FontWeight.Bold)
                Text("${steps} steps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            if (rank == 1) {
                Text("👑", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun EditChallengeDialog(
    challenge: Challenge,
    onDismiss: () -> Unit,
    onSave: (Challenge) -> Unit
) {
    var title by remember { mutableStateOf(challenge.title) }
    var goal by remember { mutableStateOf(challenge.dailyGoalTarget?.toString() ?: "10000") }
    var imageUrl by remember { mutableStateOf(challenge.imageUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Challenge") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Step Goal") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(challenge.copy(title = title, dailyGoalTarget = goal.toIntOrNull(), imageUrl = imageUrl))
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun String.customCapitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

