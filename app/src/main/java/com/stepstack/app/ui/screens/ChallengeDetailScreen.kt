package com.stepstack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
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
import coil3.compose.AsyncImage
import com.stepstack.app.data.*
import com.stepstack.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,
    onBack: () -> Unit,
    vm: AppViewModel
) {
    var challenge by remember { mutableStateOf<Challenge?>(null) }
    var participants by remember { mutableStateOf<List<ChallengeParticipant>>(emptyList()) }
    var participantUsers by remember { mutableStateOf<Map<String, FirestoreUser>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(challengeId) {
        challenge = FirebaseManager.getChallenge(challengeId)
        participants = FirebaseManager.getChallengeParticipants(challengeId)
        
        val userIds = participants.map { it.userId }
        val users = userIds.chunked(30).flatMap { ids ->
            // Batch fetch participants' profile data
            FirebaseManager.getFriends().filter { ids.contains(it.uid) } + 
                (if (ids.contains(FirebaseManager.userId)) {
                    listOf(FirestoreUser(uid = FirebaseManager.userId!!, displayName = vm.user.name, avatarUrl = vm.user.photoUrl))
                } else emptyList())
        }.associateBy { it.uid }
        
        participantUsers = users
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge?.title ?: "Challenge") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
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
                    ChallengeHeader(c)
                }
                
                item {
                    Text("Participants", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
                
                val sortedParticipants = participants.sortedByDescending { it.cumulativeSteps }
                
                items(sortedParticipants) { p ->
                    val user = participantUsers[p.userId]
                    ParticipantRankRow(
                        user = user,
                        steps = p.cumulativeSteps,
                        rank = sortedParticipants.indexOf(p) + 1
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeHeader(challenge: Challenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ends in 3 days", // Mock countdown
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Type: ${challenge.type.name.replace("_", " ").lowercase().capitalize()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ParticipantRankRow(user: FirestoreUser?, steps: Int, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#$rank",
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            model = user?.avatarUrl?.ifBlank { "https://ui-avatars.com/api/?name=${user.displayName}" },
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user?.displayName ?: "Unknown", fontWeight = FontWeight.Bold)
            Text("${steps} steps", style = MaterialTheme.typography.bodySmall)
        }
        if (rank == 1) {
            Text("👑", fontSize = 24.sp)
        }
    }
}
