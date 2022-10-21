package no.nordicsemi.android.ble.example.game.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ShowTimer(
    ticks: Long,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color =  checkColor(ticks),
                progress = progress,
                strokeWidth = 24.dp
            )
        }
        Text(
            text = "Timer:  ${ticks / 1000} s",
            fontSize = 36.sp,
            color = checkColor(ticks)
        )
    }
}

@Composable
private fun checkColor(ticks: Long) = when {
    ticks >= Timer.TOTAL_TIME / 4 -> Color.Green
    ticks >= Timer.TOTAL_TIME / 8 -> Color.Yellow
    else -> Color.Red
}
