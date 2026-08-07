package com.stepstack.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepstack.app.data.ExerciseAnim
import com.stepstack.app.ui.theme.Coral500
import com.stepstack.app.ui.theme.Lime500
import com.stepstack.app.ui.theme.Teal400
import com.stepstack.app.ui.theme.Teal600
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A looping, exercise-specific vector animation drawn on Canvas.
 * [rate] scales the animation speed (used for difficulty tuning).
 */
@Composable
fun ExerciseAnimation(
    exerciseName: String,
    anim: ExerciseAnim,
    modifier: Modifier = Modifier,
    accent: Color = Teal600,
    rate: Float = 1f,
) {
    val transition = rememberInfiniteTransition(label = "exanim")
    val periodMs = (1100 / rate).toInt().coerceIn(300, 4000)
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val slowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((periodMs * 3.2f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "slowphase",
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                // Ground shadow
                val shadowWidth = size.width * 0.4f
                val shadowHeight = size.height * 0.05f
                val shadowAlpha = when(anim) {
                    ExerciseAnim.JUMP_ROPE, ExerciseAnim.SQUAT, ExerciseAnim.RUN, ExerciseAnim.JUMPING_JACKS -> 0.15f
                    else -> 0.08f
                }
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = Offset(size.width / 2 - shadowWidth / 2, size.height * 0.82f),
                    size = Size(shadowWidth, shadowHeight)
                )

                when (anim) {
                    ExerciseAnim.JUMP_ROPE -> drawJumpRope(phase, accent)
                    ExerciseAnim.PUSH_UP -> drawPushUp(phase, accent)
                    ExerciseAnim.SQUAT -> drawSquat(phase, accent)
                    ExerciseAnim.BREATHING -> drawBreathing(slowPhase, accent)
                    ExerciseAnim.CORE -> drawCore(phase, accent)
                    ExerciseAnim.RUN -> drawRun(phase, accent)
                    ExerciseAnim.PLANK -> drawPlank(phase, accent)
                    ExerciseAnim.LUNGE -> drawLunge(phase, accent)
                    ExerciseAnim.JUMPING_JACKS -> drawJumpingJacks(phase, accent)
                    ExerciseAnim.GENERIC -> drawGeneric(phase, accent)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

private fun triWave(p: Float): Float {
    // 0..1 -> 0..1..0 triangle
    val x = (p % 1f)
    return if (x < 0.5f) x * 2f else (1f - x) * 2f
}

private fun DrawScope.jointedStickFigure(
    hipX: Float,
    hipY: Float,
    torsoLen: Float,
    color: Color,
    limbLen: Float,
    lean: Float = 0f,
    // (Angle1, Angle2) for upper and lower limb parts
    armL: Pair<Float, Float>,
    armR: Pair<Float, Float>,
    legL: Pair<Float, Float>,
    legR: Pair<Float, Float>,
    headOffset: Offset = Offset.Zero
) {
    val stroke = size.minDimension * 0.022f
    val shoulder = Offset(hipX + lean, hipY - torsoLen)
    val headR = size.minDimension * 0.065f
    
    // torso (slightly tapered)
    drawLine(color, Offset(hipX, hipY), shoulder, stroke, StrokeCap.Round)
    
    // head
    drawCircle(color, headR, Offset(shoulder.x + lean * 0.4f + headOffset.x, shoulder.y - headR * 1.1f + headOffset.y), style = Stroke(stroke))
    
    fun drawLimb(start: Offset, angles: Pair<Float, Float>) {
        val joint = Offset(start.x + limbLen * 0.5f * cos(angles.first), start.y + limbLen * 0.5f * sin(angles.first))
        val end = Offset(joint.x + limbLen * 0.5f * cos(angles.second), joint.y + limbLen * 0.5f * sin(angles.second))
        drawLine(color, start, joint, stroke, StrokeCap.Round)
        drawLine(color, joint, end, stroke, StrokeCap.Round)
    }

    drawLimb(shoulder, armL)
    drawLimb(shoulder, armR)
    drawLimb(Offset(hipX, hipY), legL)
    drawLimb(Offset(hipX, hipY), legR)
}

private fun DrawScope.drawJumpRope(phase: Float, accent: Color) {
    val cx = size.width / 2
    val ground = size.height * 0.85f
    val limb = size.minDimension * 0.22f
    val jump = triWave(phase) * size.height * 0.15f
    val hipY = ground - size.minDimension * 0.22f - jump
    
    // Whish lines
    if (phase < 0.2f || phase > 0.8f) {
        val alpha = if(phase < 0.2f) (0.2f - phase) * 5 else (phase - 0.8f) * 5
        drawLine(accent.copy(alpha = alpha * 0.2f), Offset(cx - 40.dp.toPx(), hipY - 20.dp.toPx()), Offset(cx - 60.dp.toPx(), hipY + 10.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
        drawLine(accent.copy(alpha = alpha * 0.2f), Offset(cx + 40.dp.toPx(), hipY - 20.dp.toPx()), Offset(cx + 60.dp.toPx(), hipY + 10.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
    }

    val ropePhase = phase * 2 * PI.toFloat()
    val ropeStroke = size.minDimension * 0.012f
    val rw = size.minDimension * 0.32f
    
    // Draw rope behind
    if (sin(ropePhase) <= 0) {
        drawRope(cx, rw, ground, hipY, ropePhase, jump, false)
    }

    jointedStickFigure(
        hipX = cx, hipY = hipY, torsoLen = size.minDimension * 0.22f,
        color = accent, limbLen = limb,
        armL = 2.4f to 2.4f, armR = 0.75f to 0.75f,
        legL = (PI / 2 + 0.1f).toFloat() to (PI / 2 + 0.3f).toFloat(),
        legR = (PI / 2 - 0.1f).toFloat() to (PI / 2 - 0.3f).toFloat()
    )

    // Draw rope in front
    if (sin(ropePhase) > 0) {
        drawRope(cx, rw, ground, hipY, ropePhase, jump, true)
    }
}

private fun DrawScope.drawRope(cx: Float, rw: Float, ground: Float, hipY: Float, ropePhase: Float, jump: Float, front: Boolean) {
    val path = Path()
    val steps = 40
    val bulgeBase = size.minDimension * 0.2f * abs(sin(ropePhase))
    for (i in 0..steps) {
        val t = i.toFloat() / steps
        val ang = t * PI.toFloat()
        val yBase = if (front) ground else hipY - size.minDimension * 0.32f
        val x = cx - rw + 2 * rw * t
        val bulge = sin(ang) * bulgeBase
        val y = if (front) yBase - bulge + jump * 0.4f else yBase + bulge
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, Lime500, style = Stroke(size.minDimension * 0.012f, cap = StrokeCap.Round))
}

private fun DrawScope.drawPushUp(phase: Float, accent: Color) {
    val depth = triWave(phase)
    val floorY = size.height * 0.82f
    val handX = size.width * 0.30f
    val footX = size.width * 0.78f
    val stroke = size.minDimension * 0.024f
    
    val shoulderY = (floorY - size.minDimension * 0.15f) + depth * size.minDimension * 0.2f
    val hipY = shoulderY + size.minDimension * 0.02f
    val shoulder = Offset(handX + size.minDimension * 0.04f, shoulderY)
    val hip = Offset(size.width * 0.58f, hipY)
    val foot = Offset(footX, floorY)
    
    // Arm with elbow bend
    val elbowY = floorY - (floorY - shoulderY) * 0.5f * (1 - depth)
    val elbow = Offset(handX - size.minDimension * 0.05f * depth, (shoulderY + floorY) / 2)
    
    drawLine(accent, shoulder, elbow, stroke, StrokeCap.Round)
    drawLine(accent, elbow, Offset(handX, floorY), stroke, StrokeCap.Round)
    
    // Body
    drawLine(accent, shoulder, hip, stroke, StrokeCap.Round)
    drawLine(accent, hip, foot, stroke, StrokeCap.Round)
    
    // Head looking down
    drawCircle(accent, size.minDimension * 0.06f, Offset(handX - size.minDimension * 0.05f, shoulderY - size.minDimension * 0.02f), style = Stroke(stroke))
    
    // Depth Indicator
    drawCircle(if (depth > 0.8f) Lime500 else Teal400, 6.dp.toPx(), Offset(shoulder.x, floorY + 10.dp.toPx()))
}

private fun DrawScope.drawSquat(phase: Float, accent: Color) {
    val depth = triWave(phase)
    val cx = size.width / 2
    val floorY = size.height * 0.88f
    val limb = size.minDimension * 0.24f
    
    val hipY = (floorY - size.minDimension * 0.4f) + depth * size.minDimension * 0.25f
    val upperLegAngle = (PI / 2 + 0.3f + depth * 1.1f).toFloat()
    val lowerLegAngle = (PI / 2 - 0.2f - depth * 0.4f).toFloat()

    jointedStickFigure(
        hipX = cx, hipY = hipY, torsoLen = size.minDimension * 0.24f,
        color = accent, limbLen = limb,
        lean = depth * size.minDimension * 0.08f,
        armL = (0.1f - depth * 0.4f) to (0.1f - depth * 0.4f),
        armR = (0.1f - depth * 0.4f) to (0.1f - depth * 0.4f),
        legL = upperLegAngle to lowerLegAngle,
        legR = upperLegAngle to lowerLegAngle
    )
    
    // Depth gauge
    val meterX = size.width * 0.9f
    drawLine(accent.copy(0.1f), Offset(meterX, floorY - 100.dp.toPx()), Offset(meterX, floorY), 8.dp.toPx(), StrokeCap.Round)
    drawLine(if (depth > 0.8f) Lime500 else accent, Offset(meterX, floorY - depth * 100.dp.toPx()), Offset(meterX, floorY), 8.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawBreathing(phase: Float, accent: Color) {
    val cx = size.width / 2
    val cy = size.height / 2
    val breath = (sin(phase * 2 * PI.toFloat()) + 1) / 2f
    
    for (i in 0..4) {
        val f = (breath + i * 0.2f) % 1f
        drawCircle(accent.copy(alpha = (1-f) * 0.4f), radius = size.minDimension * 0.1f + f * size.minDimension * 0.4f, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))
    }
    drawCircle(accent, size.minDimension * 0.15f + breath * 20.dp.toPx(), Offset(cx, cy))
}

private fun DrawScope.drawCore(phase: Float, accent: Color) {
    val crunch = triWave(phase)
    val floorY = size.height * 0.85f
    val hip = Offset(size.width * 0.45f, floorY - 10.dp.toPx())
    
    // Legs pinned
    val knee = Offset(size.width * 0.65f, floorY - size.minDimension * 0.2f)
    val foot = Offset(size.width * 0.75f, floorY)
    drawLine(accent, hip, knee, size.minDimension * 0.024f, StrokeCap.Round)
    drawLine(accent, knee, foot, size.minDimension * 0.024f, StrokeCap.Round)
    
    // Torso rocking
    val angle = (PI + 0.4f).toFloat() - crunch * 0.9f
    val shoulder = Offset(hip.x + size.minDimension * 0.3f * cos(angle), hip.y + size.minDimension * 0.3f * sin(angle))
    drawLine(accent, hip, shoulder, size.minDimension * 0.024f, StrokeCap.Round)
    drawCircle(accent, size.minDimension * 0.06f, Offset(shoulder.x + 5.dp.toPx() * cos(angle), shoulder.y + 5.dp.toPx() * sin(angle)), style = Stroke(size.minDimension * 0.024f))
}

private fun DrawScope.drawRun(phase: Float, accent: Color) {
    val cx = size.width / 2
    val bob = triWave(phase * 2) * 10.dp.toPx()
    val hipY = size.height * 0.6f - bob
    val swing = sin(phase * 2 * PI.toFloat())
    
    // Wind lines
    val wind1 = (phase + 0.5f) % 1f
    val wind2 = phase % 1f
    drawLine(accent.copy(alpha = (1-wind1)*0.3f), Offset(cx + 40.dp.toPx() + wind1 * 60.dp.toPx(), hipY - 10.dp.toPx()), Offset(cx + 20.dp.toPx() + wind1 * 60.dp.toPx(), hipY - 10.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
    drawLine(accent.copy(alpha = (1-wind2)*0.3f), Offset(cx + 50.dp.toPx() + wind2 * 60.dp.toPx(), hipY + 30.dp.toPx()), Offset(cx + 30.dp.toPx() + wind2 * 60.dp.toPx(), hipY + 30.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)

    jointedStickFigure(
        hipX = cx, hipY = hipY, torsoLen = size.minDimension * 0.22f,
        color = accent, limbLen = size.minDimension * 0.25f,
        lean = 0.15f,
        armL = (PI/2 + swing).toFloat() to (PI/2 + swing + 0.5f).toFloat(),
        armR = (PI/2 - swing).toFloat() to (PI/2 - swing + 0.5f).toFloat(),
        legL = (PI/2 - swing).toFloat() to (PI/2 - swing + 0.8f).toFloat(),
        legR = (PI/2 + swing).toFloat() to (PI/2 + swing + 0.8f).toFloat()
    )
}

private fun DrawScope.drawPlank(phase: Float, accent: Color) {
    val floorY = size.height * 0.82f
    val handX = size.width * 0.30f
    val footX = size.width * 0.78f
    val stroke = size.minDimension * 0.024f
    
    val shoulderY = floorY - size.minDimension * 0.15f
    val shoulder = Offset(handX + size.minDimension * 0.04f, shoulderY)
    val hip = Offset(size.width * 0.58f, shoulderY + size.minDimension * 0.02f)
    val foot = Offset(footX, floorY)
    
    // Elbows
    val elbow = Offset(handX, floorY)
    drawLine(accent, shoulder, elbow, stroke, StrokeCap.Round)
    
    // Body
    drawLine(accent, shoulder, hip, stroke, StrokeCap.Round)
    drawLine(accent, hip, foot, stroke, StrokeCap.Round)
    
    // Head looking down
    drawCircle(accent, size.minDimension * 0.06f, Offset(handX - size.minDimension * 0.05f, shoulderY - size.minDimension * 0.02f), style = Stroke(stroke))
}

private fun DrawScope.drawLunge(phase: Float, accent: Color) {
    val depth = triWave(phase)
    val cx = size.width / 2
    val floorY = size.height * 0.88f
    val limb = size.minDimension * 0.24f
    
    val hipY = (floorY - size.minDimension * 0.4f) + depth * size.minDimension * 0.2f
    val forwardFootX = cx - size.minDimension * 0.2f
    val backFootX = cx + size.minDimension * 0.2f
    
    // Jointed figure doesn't quite fit lunge well, manual draw
    val stroke = size.minDimension * 0.022f
    val shoulder = Offset(cx, hipY - size.minDimension * 0.24f)
    drawLine(accent, Offset(cx, hipY), shoulder, stroke, StrokeCap.Round)
    drawCircle(accent, size.minDimension * 0.065f, Offset(shoulder.x, shoulder.y - size.minDimension * 0.07f), style = Stroke(stroke))
    
    // Front leg
    val kneeF = Offset(forwardFootX, hipY + (floorY - hipY) * 0.5f)
    drawLine(accent, Offset(cx, hipY), kneeF, stroke, StrokeCap.Round)
    drawLine(accent, kneeF, Offset(forwardFootX, floorY), stroke, StrokeCap.Round)
    
    // Back leg
    val kneeB = Offset(cx + size.minDimension * 0.1f, floorY - (1-depth) * size.minDimension * 0.1f)
    drawLine(accent, Offset(cx, hipY), kneeB, stroke, StrokeCap.Round)
    drawLine(accent, kneeB, Offset(backFootX, floorY), stroke, StrokeCap.Round)
}

private fun DrawScope.drawJumpingJacks(phase: Float, accent: Color) {
    val spread = triWave(phase)
    val cx = size.width / 2
    val ground = size.height * 0.88f
    val limb = size.minDimension * 0.25f
    val jump = triWave(phase) * 15.dp.toPx()
    val hipY = ground - size.minDimension * 0.22f - jump
    
    val armAngle = (PI.toFloat() * 1.25f - spread * PI.toFloat() * 0.5f)
    val legAngle = (PI.toFloat() * 0.5f + 0.2f + spread * 0.5f)

    jointedStickFigure(
        hipX = cx, hipY = hipY, torsoLen = size.minDimension * 0.24f,
        color = accent, limbLen = limb,
        armL = armAngle to armAngle,
        armR = (PI.toFloat() - armAngle) to (PI.toFloat() - armAngle),
        legL = legAngle to legAngle,
        legR = (PI.toFloat() - legAngle) to (PI.toFloat() - legAngle)
    )
}

private fun DrawScope.drawGeneric(phase: Float, accent: Color) {
    val curl = triWave(phase)
    val cx = size.width / 2
    val hipY = size.height * 0.65f
    
    val armAngle = (PI/2 + 0.2f).toFloat() - curl * 1.8f
    val forearmAngle = armAngle - 0.5f * curl
    
    jointedStickFigure(
        hipX = cx, hipY = hipY, torsoLen = size.minDimension * 0.25f,
        color = accent, limbLen = size.minDimension * 0.25f,
        armL = armAngle to forearmAngle,
        armR = (PI/2 + 0.2f).toFloat() to (PI/2 + 0.5f).toFloat(),
        legL = (PI/2 + 0.15f).toFloat() to (PI/2 + 0.15f).toFloat(),
        legR = (PI/2 - 0.15f).toFloat() to (PI/2 - 0.15f).toFloat()
    )
    
    // Dumbbell
    val shoulder = Offset(cx, hipY - size.minDimension * 0.25f)
    val hand = Offset(shoulder.x + size.minDimension * 0.125f * cos(armAngle) + size.minDimension * 0.125f * cos(forearmAngle), 
                      shoulder.y + size.minDimension * 0.125f * sin(armAngle) + size.minDimension * 0.125f * sin(forearmAngle))
    drawCircle(Coral500, 8.dp.toPx(), hand)
}
