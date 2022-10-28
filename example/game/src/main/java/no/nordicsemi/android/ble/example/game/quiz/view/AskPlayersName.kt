package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.R


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AskPlayersName(
    playersName: String,
    isDuplicate: Boolean,
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
                onValueChange = onValueChange,
                trailingIcon = {
                    if (isDuplicate)
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Duplicate name error message",
                            tint = MaterialTheme.colorScheme.error
                        )
                },
                singleLine = true,
                isError = isDuplicate,
            )
            if (isDuplicate) {
                Text(
                    text = stringResource(id = R.string.duplicate_name_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
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