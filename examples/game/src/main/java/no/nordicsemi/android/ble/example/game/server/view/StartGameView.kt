package no.nordicsemi.android.ble.example.game.server.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.server.data.Player
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun StartGameView(
    isAllNameCollected: Boolean,
    joinedPlayer: List<Player>,
    onStartGame: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .clickable(
                enabled = isAllNameCollected,
                onClick = onStartGame,
            ),
    ) {
        Text(text = stringResource(id = R.string.user_joined),
            modifier = Modifier.padding(8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(items = joinedPlayer) { players ->
                Text(text = players.name,
                modifier = Modifier.padding(start = 16.dp))
            }
        }
        Button(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            onClick = onStartGame,
            enabled = isAllNameCollected,
        ) {
            Text(text = stringResource(id = R.string.start_game))
        }
    }
}

@Preview
@Composable
fun StartGameView_Preview() {
    NordicTheme {
        StartGameView(
            isAllNameCollected = false,
            joinedPlayer = listOf(
                Player("User 1"),
                Player("User 2")
            )
        ) { }
    }
}