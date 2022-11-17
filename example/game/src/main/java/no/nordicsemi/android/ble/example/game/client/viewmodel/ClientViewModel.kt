package no.nordicsemi.android.ble.example.game.client.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.client.data.ClientViewState
import no.nordicsemi.android.ble.example.game.client.repository.ClientConnection
import no.nordicsemi.android.ble.example.game.client.repository.ScannerRepository
import no.nordicsemi.android.ble.example.game.server.viewmodel.Timer
import no.nordicsemi.android.ble.example.game.server.viewmodel.TimerViewModel
import no.nordicsemi.android.ble.ktx.stateAsFlow
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scannerRepository: ScannerRepository,
) : TimerViewModel() {
    private var clientManager: ClientConnection? = null
    private val _clientState: MutableStateFlow<ClientViewState> =
        MutableStateFlow(ClientViewState())
    val clientState = _clientState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val device = scannerRepository.searchForServer()

            ClientConnection(context, viewModelScope, device)
                .apply {
                    stateAsFlow()
                        .onEach { _clientState.value = _clientState.value.copy(state = it) }
                        .launchIn(viewModelScope)
                    userJoined
                        .onEach { _clientState.value = _clientState.value.copy(userJoined = it) }
                        .launchIn(viewModelScope)
                    question
                        .onEach {
                            _clientState.value = _clientState.value.copy(
                                selectedAnswerId = null,
                                correctAnswerId = null,
                                ticks = Timer.TOTAL_TIME,
                                question = it
                            )
                            startCountDown()
                        }
                        .launchIn(viewModelScope)
                    answer
                        .onEach {
                            _clientState.value = _clientState.value.copy(
                                correctAnswerId = it,
                                ticks = ticks.value
                            )
                        }
                        .launchIn(viewModelScope)
                    isGameOver
                        .onEach { _clientState.value = _clientState.value.copy(isGameOver = it) }
                        .launchIn(viewModelScope)
                    result
                        .onEach { _clientState.value = _clientState.value.copy(result = it) }
                        .launchIn(viewModelScope)
                }
                .apply { connect() }
                .apply { clientManager = this }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clientManager?.release()
        clientManager = null
    }

    fun sendAnswer(answerId: Int) {
        _clientState.value = _clientState.value.copy(
            selectedAnswerId = answerId,
            ticks = ticks.value
        )

        viewModelScope.launch(Dispatchers.IO) {
            clientManager?.sendSelectedAnswer(answerId)
        }
    }

    fun sendName(playersName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clientManager?.sendPlayersName(playersName)
        }
    }
}