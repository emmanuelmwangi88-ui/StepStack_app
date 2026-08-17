package com.stepstack.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.stepstack.app.data.DailyActivity
import com.stepstack.app.data.Workout
import com.stepstack.app.data.AppNotification
import com.stepstack.app.data.Achievement
import com.stepstack.app.ui.theme.*
import kotlin.math.min

@Composable
fun MovementScoreRing(
    steps: Int,
    workoutMinutes: Int,
    targetScore: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    stroke: Dp = 20.dp,
) {
    val stepScore = steps
    val workoutScore = workoutMinutes * 120
    val total = stepScore + workoutScore
    val progress = if (targetScore > 0) (total.toFloat() / targetScore) else 0f
    val animated by animateFloatAsState(
        targetValue = min(progress, 1f),
        animationSpec = tween(900),
        label = "ring",
    )
    val stepFraction = if (total > 0) stepScore.toFloat() / total else 0f
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryColorAlpha = primaryColor.copy(alpha = 0.7f)

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(ringSize)) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val sw = stroke.toPx()
            val diameter = size.minDimension - sw
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            // track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            // steps portion (teal)
            val totalSweep = 360f * animated
            val stepsSweep = totalSweep * stepFraction
            drawArc(
                brush = Brush.linearGradient(listOf(primaryColor, primaryColorAlpha)),
                startAngle = -90f,
                sweepAngle = stepsSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            // workout portion (lime->coral)
            if (workoutScore > 0) {
                drawArc(
                    brush = Brush.linearGradient(listOf(Lime500, Coral500)),
                    startAngle = -90f + stepsSweep,
                    sweepAngle = totalSweep - stepsSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Unified Score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StreakBadge(streak: Int, graceDays: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Coral500.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Whatshot, null, tint = Coral500, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("${streak} Day Streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (streak > 0) "Keep it up!" else "Start your journey today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Grace Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (i < graceDays) Lime500 else MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekStrip(
    week: List<DailyActivity>, 
    targetScore: Int, 
    today: Long,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        week.forEach { day ->
            val isToday = day.epochDay == today
            val met = day.movementScore >= targetScore
            val progress = (day.movementScore.toFloat() / targetScore).coerceIn(0f, 1f)
            
            val date = java.time.LocalDate.ofEpochDay(day.epochDay)
            val dayOfWeek = date.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            val label = dayLabels[if (dayOfWeek == 7) 0 else dayOfWeek].take(1)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                met -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = if (isToday) 2.dp else 0.dp,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (met) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    } else if (progress > 0) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday || met) FontWeight.Black else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WorkoutSessionCard(
    workout: Workout,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    showFavorite: Boolean = true,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    
    val imageModel = remember(workout.thumbnailUrl) {
        val resId = context.resources.getIdentifier(workout.thumbnailUrl, "drawable", context.packageName)
        if (resId != 0) resId else workout.thumbnailUrl.ifBlank { null }
    }

    val imageRequest = remember(imageModel) {
        ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) { 
                if (imageModel != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = androidx.compose.ui.graphics.painter.ColorPainter(primary.copy(alpha = 0.1f)),
                        fallback = androidx.compose.ui.graphics.painter.ColorPainter(primary.copy(alpha = 0.1f))
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Teal600, Lime500)
                                )
                            )
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp).align(Alignment.Center)
                        )
                    }
                }
                
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 300f
                            )
                        )
                )

                if (showFavorite) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = CircleShape
                    ) {
                        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                                null, 
                                tint = if (isFavorite) Coral500 else Color.White, 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            workout.difficulty.label, 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (workout.equipment == com.stepstack.app.data.Equipment.NONE) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Bodyweight", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Text(
                    workout.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${workout.durationMin}m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${workout.calories} kcal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    workout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun AchievementBadge(a: Achievement) {
    val unlockedColor = when(a.id) {
        "a1" -> Color(0xFF4FC3F7)
        "a2" -> Color(0xFF81C784)
        "a3" -> Color(0xFFFFB74D)
        "a4" -> Color(0xFFE57373)
        "a5" -> Color(0xFF9575CD)
        "a6" -> Color(0xFFFFD54F)
        "a7" -> Color(0xFF4DB6AC)
        "a8" -> Color(0xFFBA68C8)
        "a9" -> Color(0xFFFF8A65)
        "a10" -> Color(0xFF90A4AE)
        "a11" -> Color(0xFFAED581)
        "a12" -> Color(0xFF7986CB)
        else -> Coral500
    }
    
    val color = if (a.unlocked) unlockedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    Column(
        modifier = Modifier.width(100.dp).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
        ) {
            // Glow effect for unlocked
            if (a.unlocked) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(unlockedColor.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        radius = size.minDimension / 1.2f
                    )
                }
            }

            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (a.unlocked) unlockedColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = if (a.unlocked) 6.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(a.icon) {
                            "footprint" -> Icons.Default.DirectionsWalk
                            "calendar" -> Icons.Default.CalendarToday
                            "steps" -> Icons.Default.DirectionsRun
                            "flame" -> Icons.Default.Whatshot
                            "dumbbell" -> Icons.Default.FitnessCenter
                            "trophy" -> Icons.Default.EmojiEvents
                            "explore" -> Icons.Default.Explore
                            "water_drop" -> Icons.Default.Opacity
                            "wb_sunny" -> Icons.Default.WbSunny
                            "bedtime" -> Icons.Default.Bedtime
                            "trending_up" -> Icons.Default.TrendingUp
                            "insights" -> Icons.Default.Insights
                            else -> Icons.Default.EmojiEvents
                        },
                        contentDescription = null,
                        tint = if (a.unlocked) Color.White else color.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            if (!a.unlocked) {
                Icon(
                    Icons.Default.Lock,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp).align(Alignment.BottomEnd)
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        Text(
            a.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = if (a.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutSessionCard() {
    StepStackTheme {
        Box(Modifier.padding(20.dp)) {
            WorkoutSessionCard(
                workout = com.stepstack.app.data.SampleData.workouts[0],
                onClick = {}
            )
        }
    }
}

@Composable
fun LabelValue(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NotificationFeed(
    notifications: List<AppNotification>, 
    onDismiss: (AppNotification) -> Unit
) {
    if (notifications.isEmpty()) return
    
    Column(Modifier.padding(vertical = 16.dp)) {
        Text("For You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notifications) { note ->
                NotificationCard(note, onDismiss)
            }
        }
    }
}

@Composable
private fun NotificationCard(
    note: AppNotification, 
    onDismiss: (AppNotification) -> Unit
) {
    val color = when (note.type) {
        "milestone" -> Amber500
        "grace" -> Coral500
        "achievement" -> Lime500
        else -> Teal600
    }
    
    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(note.type) {
                            "milestone" -> Icons.Default.EmojiEvents
                            "grace" -> Icons.Default.Info
                            "achievement" -> Icons.Default.Stars
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        Modifier.size(24.dp),
                        tint = color
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    note.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }
            
            IconButton(
                onClick = { onDismiss(note) },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

enum class ChartViewType { DAY, WEEK, MONTH }

@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color? = null,
    highlightLast: Boolean = false,
    viewType: ChartViewType = ChartViewType.WEEK,
    showExtremes: Boolean = true
) {
    val actualBarColor = barColor ?: MaterialTheme.colorScheme.primary
    val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val isEmpty = values.all { it == 0f }
    
    val maxIndex = if (showExtremes) values.indices.maxByOrNull { values[it] } ?: -1 else -1
    val minIndex = if (showExtremes && values.any { it > 0 }) values.indices.filter { values[it] > 0 }.minByOrNull { values[it] } ?: -1 else -1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(5) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val barWidth = when(viewType) {
                        ChartViewType.DAY -> 6.dp
                        ChartViewType.WEEK -> 24.dp
                        ChartViewType.MONTH -> 12.dp
                    }
                    
                    values.forEachIndexed { i, v ->
                        val frac = if (isEmpty) 0.05f else (v / maxVal).coerceIn(0.01f, 1f)
                        val animated by animateFloatAsState(
                            targetValue = frac,
                            animationSpec = tween(
                                durationMillis = 800,
                                delayMillis = i * (if (viewType == ChartViewType.DAY) 8 else 40),
                                easing = FastOutSlowInEasing
                            ),
                            label = "bar$i"
                        )
                        
                        val isHighlighted = highlightLast && i == values.lastIndex
                        val isMax = i == maxIndex && v > 0
                        val isMin = i == minIndex && v > 0
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Box(
                                    Modifier
                                        .width(barWidth)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f))
                                )
                                
                                Box(
                                    Modifier
                                        .width(barWidth)
                                        .fillMaxHeight(fraction = animated)
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                when {
                                                    isMax -> listOf(Lime500, Lime500.copy(alpha = 0.7f))
                                                    isMin -> listOf(Coral500, Coral500.copy(alpha = 0.7f))
                                                    isHighlighted -> listOf(actualBarColor.copy(alpha = 0.9f), actualBarColor.copy(alpha = 0.5f))
                                                    else -> listOf(actualBarColor, actualBarColor.copy(alpha = 0.6f))
                                                }
                                            )
                                        ),
                                )
                            }
                            if (labels.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                val labelText = labels.getOrElse(i) { "" }
                                
                                val shouldShowLabel = when(viewType) {
                                    ChartViewType.DAY -> i % 4 == 0 || i == values.lastIndex
                                    else -> labelText.isNotEmpty()
                                }
                                
                                if (shouldShowLabel && labelText.isNotEmpty()) {
                                    Text(
                                        labelText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = if (viewType == ChartViewType.DAY) 7.sp else 9.sp
                                        ),
                                        color = when {
                                            isMax -> Lime500
                                            isMin -> Coral500
                                            isHighlighted -> actualBarColor
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = if (isHighlighted || isMax || isMin) FontWeight.Black else FontWeight.Bold,
                                        maxLines = 1
                                    )
                                } else {
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            if (showExtremes && values.any { it > 0 }) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(Lime500, "Most Active")
                    Spacer(Modifier.width(24.dp))
                    LegendDot(Coral500, "Least Active")
                }
            }
        }
    }
}

@Composable
fun LiveStatsBar(
    heartRate: Int,
    calories: Int,
    elapsedSec: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeartStat(heartRate, Modifier.weight(1f))
        StatChip(Icons.Default.LocalFireDepartment, calories.toString(), "cal", Lime500, Modifier.weight(1f))
        StatChip(Icons.Default.Timer, formatTime(elapsedSec), "time", Teal600, Modifier.weight(1f))
    }
}

@Composable
private fun HeartStat(bpm: Int, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "hr")
    val beatMs = (60000 / bpm.coerceAtLeast(40)).coerceIn(300, 1200)
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(beatMs / 2), 
            RepeatMode.Reverse
        ),
        label = "beat",
    )
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Favorite,
            null,
            tint = Coral500,
            modifier = Modifier.size(20.dp).scale(scale),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("$bpm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("bpm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
