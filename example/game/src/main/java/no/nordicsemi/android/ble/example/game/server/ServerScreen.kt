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
import no.nordicsemi.android.ble.example.game.server.viewmodel.DownloadingQuestions
import no.nordicsemi.android.ble.example.game.server.viewmodel.Round
import no.nordicsemi.android.ble.example.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.ble.example.game.server.viewmodel.WaitingForPlayers
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
            val gameState by serverViewModel.state.collectAsState()
            val result by serverViewModel.savedResult.collectAsState()
            var openDialog by rememberSaveable { mutableStateOf(true) }
            var isDuplicate by rememberSaveable { mutableStateOf(false) }
            var isEmpty by rememberSaveable { mutableStateOf(false) }
            val userJoined by serverViewModel.userJoined.collectAsState()

            when (val currentState = gameState) {
                is WaitingForPlayers ->
                    if (currentState.connectedPlayers == 0) {
                        WaitingForClientsView()
                    } else {

                        if (openDialog) {
                            PlayersNameDialog(
                                playersName = playersName,
                                isDuplicate = isDuplicate,
                                isEmptyName = isEmpty,
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
                                        userJoined.find { it.name == playersName }
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
                            val clients by serverViewModel.clients.collectAsState()

                            StartGameView(
                                currentState = currentState,
                                isAllNameCollected = result.size >= (clients.size + 1),
                                onStartGame = { serverViewModel.startGame() }
                            )
                        }
                    }
                DownloadingQuestions -> {
                    LoadingView()
                }
                is Round -> {
                    val isGameOver by serverViewModel.isGameOver

                    when (isGameOver) {
                        true -> ResultView(result = result)
                        else -> {
                            val selectedAnswerId by serverViewModel.selectedAnswer
                            val correctAnswerId by serverViewModel.correctAnswerId.collectAsState()
                            val ticks by serverViewModel.ticks.collectAsState()

                            QuestionContentView(
                                question = currentState.question,
                                selectedAnswerId = selectedAnswerId,
                                correctAnswerId = correctAnswerId,
                                ticks = ticks,
                                modifier = Modifier.fillMaxWidth(),
                                onAnswerSelected = { answerChosen ->
                                    serverViewModel.selectedAnswerServer(answerChosen)
                                }
                            )
                            BottomNavigationView(
                                onNextClick = { serverViewModel.showNextQuestion() },
                                correctAnswerId = correctAnswerId
                            )
                        }
                    }
                }
            }
        }
    }
}