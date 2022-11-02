package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.server.data.Result
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun ShowResultView(result: List<Result?>) {
    val sortedResult: List<Result?> = result.sortedByDescending { it?.score }
    var openDialog by remember { mutableStateOf(true) }
    if (openDialog) {
        ShowResultDialog(
            onDismiss = { openDialog = false }) {
            openDialog = false
        }
    } else {
        Row(
            modifier = Modifier.padding(start = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.winner),
            )
            Text(
                text = stringResource(id = R.string.score),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(24.dp),
        ) {
            items(items = sortedResult) { sortedResult ->
                Row {
                    sortedResult?.name?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(
                        text = sortedResult?.score.toString(),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShowResultDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnClickOutside = false
        ),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.game_over),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onClick
            ) {
                Text(text = stringResource(id = R.string.show_result))
            }
        },
    )
}

@Preview
@Composable
fun ShowResultDialog_Preview() {
    NordicTheme {
        ShowResultView(
            result = listOf(
                Result("User 1", 1),
                Result("User 2", 5),
                Result("User 3", 4)
            )
        )
    }
}

