package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.server.data.FinalResult

@Composable
fun ShowResult(result: List<FinalResult>) {
    val sortedResult: List<FinalResult> = result.sortedByDescending { it.score }
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(id = R.string.game_over))
            },
            text = {
                Text(text = stringResource(id = R.string.show_result_des))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.show_result))
                }
            },
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(items = sortedResult) { sortedResult ->
                Row {
                    Text(
                        text = sortedResult.name,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = sortedResult.score.toString(),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GameOverPopUpDialog() {
    val openDialog = remember { mutableStateOf(true) }
    var clientName by remember { mutableStateOf(TextFieldValue("")) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Enter player's name",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                TextField(value = clientName,
                    onValueChange = { clientName = it }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Send")
                }
            },
        )
    }
}

