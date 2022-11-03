package no.nordicsemi.android.ble.example.game.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.client.view.*
import no.nordicsemi.android.ble.example.game.client.viewmodel.ClientViewModel
import no.nordicsemi.android.ble.example.game.quiz.view.PlayersNameDialog
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContentView
import no.nordicsemi.android.ble.example.game.quiz.view.ShowResultView
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

            when (state) {
                ConnectionState.Initializing -> { InitializingView() }
                ConnectionState.Connecting -> { ConnectingView() }
                is ConnectionState.Disconnected -> { DisconnectedView() }
                ConnectionState.Ready -> {
                    val question by clientViewModel.question.collectAsState()
                    val results by clientViewModel.result.collectAsState()

                    question?.let { q ->
                        val correctAnswerId by clientViewModel.answer.collectAsState()
                        val selectedAnswerId by clientViewModel.selectedAnswer
                        val ticks by clientViewModel.ticks.collectAsState()

                        when (results?.isGameOver) {
                            true -> results?.result?.let { result -> ShowResultView(result = result) }
                            else -> {
                                QuestionContentView(
                                    question = q,
                                    correctAnswerId = correctAnswerId,
                                    selectedAnswerId = selectedAnswerId,
                                    ticks = ticks,
                                    modifier = Modifier.fillMaxWidth()
                                ) { answerChosen ->
                                    clientViewModel.sendAnswer(answerChosen)
                                }
                            }
                        }
                    } ?: run {
                        var openDialog by remember { mutableStateOf(true) }
                        var playersName by remember { mutableStateOf("") }
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
                                    results?.result?.find { it.name == playersName }
                                        ?.let {
                                            isDuplicate = true
                                        }
                                        ?: run {
                                            clientViewModel.sendName(playersName)
                                            openDialog = false
                                        }
                                }
                            }
                        } else ConnectedView()
                    }
                }
                else -> LoadingView()
            }
        }
    }
}


