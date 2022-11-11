package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlayersNameDialog(
    playersName: String,
    isDuplicate: Boolean,
    isEmptyName: Boolean,
    onDismiss: () -> Unit,
    onNameSet: (String) -> Unit,
    onSendClick: () -> Unit
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.ask_players_name))
        },
        text = {
            TextField(
                value = playersName,
                onValueChange = onNameSet,
                trailingIcon = {
                    if (isDuplicate || isEmptyName)
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                },
                singleLine = true,
                isError = isDuplicate || isEmptyName,
            )
            if (isDuplicate || isEmptyName) {
                Text(
                    text =
                    if (isDuplicate) stringResource(id = R.string.duplicate_name_error)
                    else stringResource(id = R.string.empty_name_error),
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

@Preview
@Composable
fun PlayersNameDialog_Preview() {
    NordicTheme {
        PlayersNameDialog(
            playersName = "",
            isEmptyName = false,
            isDuplicate = true,
            onDismiss = { },
            onNameSet = { },
            onSendClick = {},
        )
    }
}