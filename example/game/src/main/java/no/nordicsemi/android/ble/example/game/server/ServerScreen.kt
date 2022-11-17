package no.nordicsemi.android.ble.example.game.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.client.view.LoadingView
import no.nordicsemi.android.ble.example.game.quiz.view.PlayersNameDialog
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContentView
import no.nordicsemi.android.ble.example.game.quiz.view.ResultView
import no.nordicsemi.android.ble.example.game.server.view.BottomNavigationView
import no.nordicsemi.android.ble.example.game.server.view.StartGameView
import no.nordicsemi.android.ble.example.game.server.view.WaitingForClientsView
import no.nordicsemi.android.ble.example.game.server.viewmodel.*
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    onNavigationUp: () -> Unit,
) {
    Column {
        var playersName by rememberSaveable { mutableStateOf("") }
        NordicAppBar(
            text = when (playersName.isNotEmpty()) {
                true -> stringResource(id = R.string.good_luck_player, playersName)
                else -> stringResource(id = R.string.good_luck_player, "")
            },
            onNavigationButtonClick = onNavigationUp
        )

        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val serverViewState by serverViewModel.serverViewState.collectAsState()
            var openDialog by rememberSaveable { mutableStateOf(true) }
            var isDuplicate by rememberSaveable { mutableStateOf(false) }
            var isEmpty by rememberSaveable { mutableStateOf(false) }

            when (val currentState = serverViewState.state) {
                is WaitingForPlayers ->
                    if (currentState.connectedPlayers == 0) {
                        WaitingForClientsView()
                    } else {
                        if (openDialog) {
                            PlayersNameDialog(
                                playersName = playersName,
                                isDuplicate = isDuplicate,
                                isError = isEmpty || isDuplicate,
                                onDismiss = { openDialog = false },
                                onNameSet = {
                                    playersName = it
                                    isDuplicate = false
                                    isEmpty = false
                                },
                                onSendClick = {
                                    playersName = playersName.trim()
                                    if (playersName.isNotEmpty()) {
                                        isEmpty = false
                                        serverViewState.userJoined.find { it.name == playersName }
                                            ?.let {
                                                isDuplicate = true
                                            }
                                            ?: run {
                                                serverViewModel.saveServerPlayer(playersName)
                                                openDialog = false
                                            }
                                    } else isEmpty = true
                                },
                            )
                        } else {
                            StartGameView(
                                isAllNameCollected = serverViewState.userJoined.size == (currentState.connectedPlayers + 1),
                                joinedPlayer = serverViewState.userJoined
                            ) { serverViewModel.startGame() }
                        }
                    }
                DownloadingQuestions -> { LoadingView() }
                is Round -> {
                    when (serverViewState.isGameOver) {
                        true -> ResultView(result = serverViewState.result)
                        else -> {
                            val ticks by serverViewModel.ticks.collectAsState()

                            QuestionContentView(
                                question = currentState.question.question,
                                answers = serverViewState.toViewState(),
                                ticks = ticks,
                                modifier = Modifier.fillMaxWidth(),
                                onAnswerSelected = { answerChosen ->
                                    serverViewModel.selectedAnswerServer(answerChosen)
                                }
                            )
                            BottomNavigationView(
                                onNextClick = { serverViewModel.showNextQuestion() },
                                isTimerRunning = ticks > 0
                            )
                        }
                    }
                }
            }
        }
    }
}