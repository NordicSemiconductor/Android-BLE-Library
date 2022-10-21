package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.client.viewmodel.ClientViewModel
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    navigationManager: NavigationManager,
) {
    Column {
        NordicAppBar(
            text = stringResource(id = R.string.client),
            onNavigationButtonClick = {
                navigationManager.navigateUp()
            }
        )

        RequireBluetooth {
            val clientViewModel: ClientViewModel = hiltViewModel()
            val state by clientViewModel.state.collectAsState()

            when (state) {
                ConnectionState.Initializing -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.initializing),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                ConnectionState.Connecting -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.scanning_for_server),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                ConnectionState.Ready -> {
                    val question by clientViewModel.question.collectAsState()

                    question?.let { q ->
                        val correctAnswerId by clientViewModel.answer.collectAsState()
                        val selectedAnswerId by clientViewModel.selectedAnswer
                        val ticks by clientViewModel.ticks.collectAsState()

                        QuestionScreenClient(
                            question = q,
                            correctAnswerId = correctAnswerId,
                            selectedAnswerId = selectedAnswerId,
                            ticks = ticks,
                            modifier = Modifier.fillMaxWidth()
                        ) { answerChosen ->
                            clientViewModel.sendAnswer(answerChosen)
                        }
                    } ?: run {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.connected),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is ConnectionState.Disconnected -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.disconnected),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.loading),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AskDeviceName(textState: String, onTextChange: (String) -> Unit) {
    TextField(value = textState, onValueChange = onTextChange)
}


