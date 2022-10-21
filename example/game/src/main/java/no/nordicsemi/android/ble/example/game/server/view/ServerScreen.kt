package no.nordicsemi.android.ble.example.game.server.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.R
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
            val questionState by serverViewModel.uiState.collectAsState(initial = null)

            when (val currentState = gameState) {
                is WaitingForPlayers -> when (currentState.connectedPlayers) {
                    0 -> {
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
                    else -> {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        ) {
                            Text(
                                text = "Connected clients: ${currentState.connectedPlayers}",
                                modifier = Modifier.padding(16.dp)
                            )

                            Button(onClick = { serverViewModel.startGame() }) {
                                Text(text = "Start game")
                            }
                        }
                    }
                }
                DownloadingQuestions -> {
                    Text(text = "Downloading...")
                }
                is Round -> {
                    questionState?.let { question ->
                        val selectedAnswerId by serverViewModel.selectedAnswer
                        val correctAnswerId by serverViewModel.correctAnswerId.collectAsState()
                        val ticks by serverViewModel.ticks.collectAsState()
                        val coroutineScope = rememberCoroutineScope()

                        QuestionsScreenServer(
                            question = question,
                            selectedAnswerId = selectedAnswerId,
                            correctAnswerId = correctAnswerId,
                            ticks = ticks,
                            modifier = Modifier.fillMaxWidth(),
                            onNextPressed = {
                                coroutineScope.launch {
                                    serverViewModel.increaseIndex()
                                }
                            },
                        ) { answerChosen ->
                            serverViewModel.saveScore(answerChosen)
                        }
                    }
                }
            }
        }
    }
}