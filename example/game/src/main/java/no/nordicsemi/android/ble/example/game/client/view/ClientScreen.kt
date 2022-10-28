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
import no.nordicsemi.android.ble.example.game.quiz.view.ShowResult
import no.nordicsemi.android.ble.example.game.quiz.view.AskPlayersName
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

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
            val finalResult by clientViewModel.finalResult.collectAsState()

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

                        if (finalResult?.isGameOver == true) ShowResult(result = finalResult!!.finalResult)
                        else {
                            QuestionScreenClient(
                                question = q,
                                correctAnswerId = correctAnswerId,
                                selectedAnswerId = selectedAnswerId,
                                ticks = ticks,
                                modifier = Modifier.fillMaxWidth()
                            ) { answerChosen ->
                                clientViewModel.sendAnswer(answerChosen)
                            }
                        }
                    } ?: run {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        ) {
                            val openDialog = remember { mutableStateOf(true) }
                            var playersName: String by remember { mutableStateOf("") }
                            var isDuplicate by remember { mutableStateOf(false) }

                            val onValueChange = { name: String ->
                                playersName = name
                                isDuplicate = false
                            }

                            val onSendClick = {
                                if (playersName != "") {
                                    finalResult?.finalResult?.find { it.name == playersName }
                                        ?.let {
                                            isDuplicate = true
                                        }
                                        ?: run {
                                            clientViewModel.sendName(playersName)
                                            openDialog.value = false
                                        }
                                }
                            }

                            if (openDialog.value) {
                                AskPlayersName(
                                    playersName = playersName,
                                    isDuplicate = isDuplicate,
                                    onValueChange = onValueChange,
                                    onSendClick = onSendClick
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.connected),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
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
private fun LoadingView() {
    Text(
        text = stringResource(id = R.string.loading),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun DisconnectedView() {
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

@Composable
private fun InitializingView() {
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

@Composable
private fun ConnectingView() {
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



