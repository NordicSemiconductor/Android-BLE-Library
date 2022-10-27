package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.ble.example.game.R


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AskPlayersName(
    playersName: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = stringResource(id = R.string.ask_players_name))
        },
        text = {
            TextField(
                value = playersName,
                onValueChange = onValueChange
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSendClick
            ) {
                Text(text = stringResource(id = R.string.send_players_name))
            }
        },
    )
}