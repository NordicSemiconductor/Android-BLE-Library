package no.nordicsemi.android.ble.example.game.timer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ShowTimer(
    key: Any,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var progress by remember { mutableStateOf(1f) }
        val progressAnimation by animateFloatAsState(
            targetValue = progress,
            animationSpec = if (progress == 1f) snap(0) else tween(
                durationMillis = duration.toInt(),
                easing = LinearEasing
            ),
            finishedListener = { if (progress == 1f) progress = 0f }
        )
        CircularProgressIndicator(
            color = checkColor(duration),
            progress = progressAnimation,
            strokeWidth = 24.dp,
            modifier = Modifier.padding(end = 16.dp)
        )
        LaunchedEffect(key1 = key) {
            progress = if (progress == 1f) 0f else 1f
        }
        Text(
            text = "Timer:  ${duration / 1000} s",
            fontSize = 36.sp,
            color = checkColor(duration)
        )
    }
}

@Composable
private fun checkColor(ticks: Long) = when {
    ticks >= Timer.TOTAL_TIME / 4 -> Color.Green
    ticks >= Timer.TOTAL_TIME / 8 -> Color.Yellow
    else -> Color.Red
}
