package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.server.data.Player
import no.nordicsemi.android.ble.example.game.R

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
        Text(text = stringResource(id = R.string.waiting_to_start_game))
    }
}

@Preview
@Composable
fun ConnectedView_Preview() {
    ConnectedView(
        listOf(
            Player("User 1"),
            Player("User 2")
        )
    )
}