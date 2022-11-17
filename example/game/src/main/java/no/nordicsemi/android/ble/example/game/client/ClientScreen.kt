package no.nordicsemi.android.ble.example.game.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.client.view.*
import no.nordicsemi.android.ble.example.game.client.viewmodel.ClientViewModel
import no.nordicsemi.android.ble.example.game.client.viewmodel.toViewState
import no.nordicsemi.android.ble.example.game.quiz.view.PlayersNameDialog
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContentView
import no.nordicsemi.android.ble.example.game.quiz.view.ResultView
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
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
            val clientViewModel: ClientViewModel = hiltViewModel()
            val clientViewState by clientViewModel.clientState.collectAsState()
            val ticks by clientViewModel.ticks.collectAsState()

            when (clientViewState.state) {
                ConnectionState.Initializing -> {
                    InitializingView()
                }
                ConnectionState.Connecting -> {
                    ConnectingView()
                }
                is ConnectionState.Disconnected -> {
                    DisconnectedView()
                }
                ConnectionState.Ready -> {
                    when (clientViewState.isGameOver) {
                        true -> clientViewState.result?.result?.let { result -> ResultView(result = result) }
                        else -> {
                            clientViewState.question?.let {
                                QuestionContentView(
                                    question = clientViewState.question?.question,
                                    answers = clientViewState.toViewState(),
                                    ticks = ticks,
                                    modifier = Modifier.fillMaxWidth(),
                                    onAnswerSelected = { answerChosen ->
                                        clientViewModel.sendAnswer(answerChosen)
                                    }
                                )
                            } ?: run {
                                    var openDialog by rememberSaveable { mutableStateOf(true) }
                                    var isDuplicate by rememberSaveable { mutableStateOf(false) }
                                    var isEmptyName by rememberSaveable { mutableStateOf(false) }

                                    if (openDialog) {
                                        PlayersNameDialog(
                                            playersName = playersName,
                                            isDuplicate = isDuplicate,
                                            isError = isDuplicate || isEmptyName,
                                            onDismiss = { openDialog = false },
                                            onNameSet = {
                                                playersName = it
                                                isDuplicate = false
                                                isEmptyName = false
                                            },
                                            onSendClick = {
                                                playersName = playersName.trim()
                                                clientViewModel.sendName(playersName)
                                                if (playersName.isNotEmpty()) {
                                                    isEmptyName = false
                                                    clientViewState.userJoined?.player?.find { it.name == playersName }
                                                        ?.let {
                                                            isDuplicate = true
                                                        }
                                                        ?: run {
                                                            openDialog = false
                                                        }
                                                } else isEmptyName = true
                                            })
                                    } else clientViewState.userJoined?.let { ConnectedView(it.player) }
                                }
                        }
                    }
                }
                else -> LoadingView()
            }
        }
    }
}


