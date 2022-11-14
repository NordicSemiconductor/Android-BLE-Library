package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.server.data.Player
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun ConnectedView(players: List<Player>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.user_joined))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(items = players) { players ->
                Text(text = players.name)
            }
        }
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.waiting_to_start_game))
            LoadingAnimation()
        }
    }
}

@Composable
fun LoadingAnimation(
    circleColor: Color = MaterialTheme.colorScheme.secondary,
    circleSize: Dp = 4.dp,
    animationDelay: Long = 400L,
    initialAlpha: Float = 0.3f
) {
    val circles = listOf(
        remember { Animatable(initialValue = initialAlpha) },
        remember { Animatable(initialValue = initialAlpha) },
        remember { Animatable(initialValue = initialAlpha) }
    )

    Row {
        circles.forEachIndexed { index, animate ->
            LaunchedEffect(Unit) {
                delay(timeMillis = (animationDelay / circles.size) * index)
                animate.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = animationDelay.toInt()
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            if (index != 0) Spacer(modifier = Modifier.width(width = 4.dp))
            Box(
                modifier = Modifier
                    .size(size = circleSize)
                    .clip(shape = CircleShape)
                    .background(
                        color = circleColor
                            .copy(alpha = animate.value)
                    )
            ) {
            }
        }
    }
}

@Preview
@Composable
fun ConnectedView_Preview() {
    NordicTheme {
        ConnectedView(
            listOf(
                Player("User 1"),
                Player("User 2")
            )
        )
    }
}