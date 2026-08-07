package com.stepstack.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.stepstack.app.data.Muscle
import com.stepstack.app.ui.theme.Coral500

/**
 * A stylized front-facing human body silhouette that pulses a glow on the
 * currently targeted muscle groups.
 */
@Composable
fun BodyMap(
    targets: List<Muscle>,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF3A4744),
    highlight: Color = Coral500,
) {
    val transition = rememberInfiniteTransition(label = "body")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )
    val active = targets.toSet()

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        fun hl(m: Muscle): Color =
            if ((m in active) || (Muscle.FULL in active)) highlight.copy(alpha = 0.25f + pulse * 0.55f)
            else baseColor.copy(alpha = 0.85f)

        val unit = h / 20f

        // Head
        drawCircle(baseColor.copy(alpha = 0.85f), unit * 1.4f, Offset(cx, unit * 1.6f))

        // Shoulders
        roundBar(Offset(cx - unit * 3.1f, unit * 3.6f), Offset(cx - unit * 1.6f, unit * 3.6f), unit * 0.9f, hl(Muscle.SHOULDERS))
        roundBar(Offset(cx + unit * 1.6f, unit * 3.6f), Offset(cx + unit * 3.1f, unit * 3.6f), unit * 0.9f, hl(Muscle.SHOULDERS))

        // Chest (two pecs)
        drawRoundRectGlow(Rect(cx - unit * 2.3f, unit * 4.0f, cx - unit * 0.15f, unit * 6.2f), hl(Muscle.CHEST), unit * 0.6f)
        drawRoundRectGlow(Rect(cx + unit * 0.15f, unit * 4.0f, cx + unit * 2.3f, unit * 6.2f), hl(Muscle.CHEST), unit * 0.6f)

        // Arms (upper)
        roundBar(Offset(cx - unit * 3.0f, unit * 4.2f), Offset(cx - unit * 3.4f, unit * 7.4f), unit * 0.7f, hl(Muscle.ARMS))
        roundBar(Offset(cx + unit * 3.0f, unit * 4.2f), Offset(cx + unit * 3.4f, unit * 7.4f), unit * 0.7f, hl(Muscle.ARMS))
        // forearms
        roundBar(Offset(cx - unit * 3.4f, unit * 7.4f), Offset(cx - unit * 3.7f, unit * 10.2f), unit * 0.55f, hl(Muscle.ARMS))
        roundBar(Offset(cx + unit * 3.4f, unit * 7.4f), Offset(cx + unit * 3.7f, unit * 10.2f), unit * 0.55f, hl(Muscle.ARMS))

        // Abs
        drawRoundRectGlow(Rect(cx - unit * 1.8f, unit * 6.4f, cx + unit * 1.8f, unit * 9.6f), hl(Muscle.ABS), unit * 0.55f)

        // Glutes / hips
        drawRoundRectGlow(Rect(cx - unit * 2.0f, unit * 9.8f, cx + unit * 2.0f, unit * 11.4f), hl(Muscle.GLUTES), unit * 0.7f)

        // Legs (thighs)
        roundBar(Offset(cx - unit * 1.1f, unit * 11.2f), Offset(cx - unit * 1.3f, unit * 14.8f), unit * 1.0f, hl(Muscle.LEGS))
        roundBar(Offset(cx + unit * 1.1f, unit * 11.2f), Offset(cx + unit * 1.3f, unit * 14.8f), unit * 1.0f, hl(Muscle.LEGS))

        // Calves
        roundBar(Offset(cx - unit * 1.3f, unit * 14.8f), Offset(cx - unit * 1.2f, unit * 17.8f), unit * 0.7f, hl(Muscle.CALVES))
        roundBar(Offset(cx + unit * 1.3f, unit * 14.8f), Offset(cx + unit * 1.2f, unit * 17.8f), unit * 0.7f, hl(Muscle.CALVES))

        // Back indicator (subtle band behind torso) — only glows when targeted
        if (Muscle.BACK in active || Muscle.FULL in active) {
            drawRoundRectGlow(
                Rect(cx - unit * 2.6f, unit * 4.0f, cx + unit * 2.6f, unit * 9.4f),
                highlight.copy(alpha = 0.12f + pulse * 0.2f),
                unit * 0.8f,
            )
        }
    }
}

private fun DrawScope.roundBar(a: Offset, b: Offset, width: Float, color: Color) {
    drawLine(color, a, b, strokeWidth = width * 2, cap = StrokeCap.Round)
}

private fun DrawScope.drawRoundRectGlow(rect: Rect, color: Color, radius: Float) {
    val path = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect,
                androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
        )
    }
    drawPath(path, color)
}
