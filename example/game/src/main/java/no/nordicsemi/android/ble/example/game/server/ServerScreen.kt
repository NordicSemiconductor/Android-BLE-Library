package no.nordicsemi.android.ble.example.game.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.client.view.LoadingView
import no.nordicsemi.android.ble.example.game.quiz.view.PlayersNameDialog
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContentView
import no.nordicsemi.android.ble.example.game.quiz.view.ShowResultView
import no.nordicsemi.android.ble.example.game.server.view.BottomNavigationView
import no.nordicsemi.android.ble.example.game.server.view.StartGameView
import no.nordicsemi.android.ble.example.game.server.view.WaitingForClientsView
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
            var playersName by remember { mutableStateOf("") }

            when (val currentState = gameState) {
                is WaitingForPlayers ->
                    if (currentState.connectedPlayers == 0) {
                        WaitingForClientsView()
                    } else {
                        var openDialog by remember { mutableStateOf(true) }
                        var isDuplicate by remember { mutableStateOf(false) }

                        if (openDialog) {
                            PlayersNameDialog(
                                playersName = playersName,
                                isDuplicate = isDuplicate,
                                onDismiss = { openDialog = false },
                                onNameSet = {
                                    playersName = it
                                    isDuplicate = false
                                }) {
                                if (playersName.isNotEmpty()) {
                                    result.find { it.name == playersName }
                                        ?.let {
                                            isDuplicate = true
                                        }
                                        ?: run {
                                            serverViewModel.savePlayersName(playersName)
                                            openDialog = false
                                        }
                                }
                            }
                        } else {
                            val clients by serverViewModel.clients.collectAsState()

                            StartGameView(
                                currentState = currentState,
                                isAllNameCollected = result.size >= (clients.size + 1)
                            ) {
                                serverViewModel.startGame()
                            }
                        }
                    }
                DownloadingQuestions -> { LoadingView() }
                is Round -> {
                    val isGameOver by serverViewModel.isGameOver

                    when (isGameOver) {
                        true -> ShowResultView(result = result)
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
                            ) { answerChosen ->
                                serverViewModel.selectedAnswerServer(playersName, answerChosen)
                            }
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