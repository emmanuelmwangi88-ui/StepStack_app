package com.stepstack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stepstack.app.ui.theme.Amber500
import java.text.NumberFormat

/** UI-only model so the same components render global boards AND challenge boards. */
data class RankItem(
    val uid: String,
    val name: String,
    val avatarUrl: String,
    val rank: Int,
    /** Already formatted, e.g. "12,430 steps" or "5 days". */
    val valueText: String,
    /** 0f..1f, relative to the leader. Drives the thin bar under the name. */
    val fraction: Float,
    val isMe: Boolean
)

private val Gold = Amber500
private val Silver = Color(0xFFB0BEC5)
private val Bronze = Color(0xFFCD7F32)

private fun medalColor(rank: Int): Color = when (rank) {
    1 -> Gold
    2 -> Silver
    3 -> Bronze
    else -> Color.Unspecified
}

fun formatCount(value: Int): String = NumberFormat.getIntegerInstance().format(value)

@Composable
fun RankAvatar(name: String, avatarUrl: String, size: Dp, modifier: Modifier = Modifier) {
    if (avatarUrl.isNotBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}

/** Top three as a podium: 2nd | 1st | 3rd. Renders with 1 or 2 people too. */
@Composable
fun RankPodium(
    top: List<RankItem>,
    onClick: (RankItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (top.isEmpty()) return
    // Visual order is silver, gold, bronze.
    val slots: List<Pair<RankItem?, Dp>> = listOf(
        top.getOrNull(1) to 72.dp,
        top.getOrNull(0) to 104.dp,
        top.getOrNull(2) to 52.dp
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        slots.forEach { (item, pedestalHeight) ->
            if (item == null) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                PodiumColumn(
                    item = item,
                    pedestalHeight = pedestalHeight,
                    isWinner = item === top.first(),
                    onClick = { onClick(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    item: RankItem,
    pedestalHeight: Dp,
    isWinner: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medal = medalColor(item.rank)
    val avatarSize = if (isWinner) 72.dp else 58.dp

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWinner) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Leader",
                tint = Gold,
                modifier = Modifier.size(24.dp)
            )
        }
        RankAvatar(
            name = item.name,
            avatarUrl = item.avatarUrl,
            size = avatarSize,
            modifier = Modifier.border(3.dp, if (medal == Color.Unspecified) Color.Transparent else medal, CircleShape)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (item.isMe) "You" else item.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.valueText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    if (medal == Color.Unspecified) MaterialTheme.colorScheme.surfaceVariant
                    else medal.copy(alpha = 0.85f)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = item.rank.toString(),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RankListRow(
    item: RankItem,
    onClick: (RankItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (item.isMe) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isMe) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${item.rank}",
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RankAvatar(item.name, item.avatarUrl, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isMe) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "YOU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.fraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.valueText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Sticky footer: where I stand right now, even if I'm outside the visible list. */
@Composable
fun MyRankBar(
    headline: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}
