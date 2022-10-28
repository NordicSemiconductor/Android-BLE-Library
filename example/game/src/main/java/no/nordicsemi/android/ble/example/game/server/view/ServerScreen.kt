package no.nordicsemi.android.ble.example.game.server.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.quiz.view.AskPlayersName
import no.nordicsemi.android.ble.example.game.quiz.view.ShowResult
import no.nordicsemi.android.ble.example.game.server.viewmodel.DownloadingQuestions
import no.nordicsemi.android.ble.example.game.server.viewmodel.Round
import no.nordicsemi.android.ble.example.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.ble.example.game.server.viewmodel.WaitingForPlayers
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@Composable
fun ServerScreen(
    navigationManager: NavigationManager,
) {
    Column {
        NordicAppBar(
            text = stringResource(id = R.string.server),
            onNavigationButtonClick = {
                navigationManager.navigateUp()
            }
        )
        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val gameState by serverViewModel.state.collectAsState()
            val result by serverViewModel.savedResult.collectAsState()
            val clients by serverViewModel.clients.collectAsState()
            var playersName: String by remember { mutableStateOf("") }

            when (val currentState = gameState) {
                is WaitingForPlayers -> when (currentState.connectedPlayers) {
                    0 -> {
                        WaitingForClientsView()
                    }
                    else -> {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .clickable { serverViewModel.startGame() },
                        ) {
                            Text(
                                text = "Connected clients: ${currentState.connectedPlayers}",
                                modifier = Modifier.padding(16.dp)
                            )

                            val openDialog = remember { mutableStateOf(true) }
                            var isDuplicate by remember { mutableStateOf(false) }

                            val onValueChange = { name: String ->
                                playersName = name
                                isDuplicate = false
                            }
                            val onSendClick = {
                                if (playersName != "") {
                                    result.find { it.name == playersName }
                                        ?.let {
                                            isDuplicate = true
                                        }
                                        ?: run {
                                            serverViewModel.savePlayersName(playersName)
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
                                Button(
                                    onClick = { serverViewModel.startGame() },
                                    enabled = result.size >= clients.size + 1,

                                    ) {
                                    Text(text = stringResource(id = R.string.start_game))
                                }
                            }
                        }
                    }
                }
                DownloadingQuestions -> {
                    Text(text = stringResource(id = R.string.downloading))
                }
                is Round -> {
                    val selectedAnswerId by serverViewModel.selectedAnswer
                    val correctAnswerId by serverViewModel.correctAnswerId.collectAsState()
                    val ticks by serverViewModel.ticks.collectAsState()
                    val isGameOver by serverViewModel.isGameOver

                    if (isGameOver == false) {
                        QuestionsScreenServer(
                            question = currentState.question,
                            selectedAnswerId = selectedAnswerId,
                            correctAnswerId = correctAnswerId,
                            ticks = ticks,
                            modifier = Modifier.fillMaxWidth(),
                            onNextPressed = { serverViewModel.showNextQuestion() },
                        ) { answerChosen ->
                            serverViewModel.selectedAnswerServer(playersName, answerChosen)
                        }
                    } else {
                        ShowResult(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitingForClientsView() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.looking_for_clients),
            modifier = Modifier.padding(16.dp)
        )
    }
}