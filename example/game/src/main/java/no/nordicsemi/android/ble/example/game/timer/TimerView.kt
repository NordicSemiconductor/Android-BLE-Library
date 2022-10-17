package no.nordicsemi.android.ble.example.game.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ShowTimer(ticks: Long, progress: Float) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
            text = "Timer:  $ticks",
            fontSize = 36.sp,
            color =
            checkColor(ticks)
        )
    }
}

@Composable
private fun checkColor(ticks: Long) = when {
    ticks >= 10_000 -> {
        Color.Green
    }
    ticks in 5001..9999 -> {
        Color.Yellow
    }
    else -> Color.Red
}
