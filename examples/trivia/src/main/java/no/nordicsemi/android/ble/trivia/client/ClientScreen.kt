package no.nordicsemi.android.ble.trivia.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.trivia.R
import no.nordicsemi.android.ble.trivia.client.viewmodel.ClientViewModel
import no.nordicsemi.android.ble.trivia.server.view.PlayersNameDialog
import no.nordicsemi.android.ble.trivia.server.view.QuestionContentView
import no.nordicsemi.android.ble.trivia.client.data.toViewState
import no.nordicsemi.android.ble.trivia.client.view.*
import no.nordicsemi.android.ble.trivia.server.view.ResultView
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
                ConnectionState.Initializing -> { InitializingView() }
                ConnectionState.Connecting -> { ConnectingView() }
                is ConnectionState.Disconnected -> { DisconnectedView() }
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
                                if (clientViewState.openDialog) {
                                    PlayersNameDialog(
                                        playersName = playersName,
                                        isDuplicate = clientViewState.playersNameIsDuplicate,
                                        isError = clientViewState.playersNameIsError,
                                        onDismiss = {
                                            clientViewModel.dismissPlayersNameDialog()
                                                    },
                                        onNameSet = {
                                            playersName = it
                                            clientViewModel.onUserTyping()
                                        },
                                        onSendClick = {
                                            playersName = playersName.trim()
                                            clientViewModel.sendName(playersName)
                                        }
                                    )
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


