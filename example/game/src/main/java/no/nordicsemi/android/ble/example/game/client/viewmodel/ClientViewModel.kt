package no.nordicsemi.android.ble.example.game.client.viewmodel

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.client.repository.ClientConnection
import no.nordicsemi.android.ble.example.game.client.repository.ScannerRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.timer.TimerViewModel
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scannerRepository: ScannerRepository,
    private val leAdapter: BluetoothAdapter,
) : TimerViewModel() {
    private var clientManager: ClientConnection? = null
    private var job: Job? = null

    private val _question: MutableStateFlow<Question?> = MutableStateFlow(null)
    val question = _question.asStateFlow()
    private val _answer: MutableStateFlow<Int?> = MutableStateFlow(null)
    val answer = _answer.asStateFlow()
    private val _selectedAnswer: MutableState<Int?> = mutableStateOf(null)
    val selectedAnswer: State<Int?> = _selectedAnswer
    val deviceName = clientManager?.deviceName
    private val _state: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Initializing)
    val state = _state.asStateFlow()

    init {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            // Global handler
            Log.e("AAA", "Error", t)
        }
        job = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val device = scannerRepository.searchForServer()

            ClientConnection(context, viewModelScope, device, leAdapter )
                .apply {
                    stateAsFlow()
                        .onEach {
                            _state.value = it
                        }
                        .launchIn(viewModelScope)
                    question
                        .onEach {
                            _answer.value = null
                            _selectedAnswer.value = null
                            _question.value = it
                            startCountDown()
                        }
                        .launchIn(viewModelScope)
                    answer
                        .onEach { _answer.value = it }
                        .launchIn(viewModelScope)
                }
                .apply {
                    connect()
                    // Send device name
                    sendDeviceName(deviceName)
                }
                .apply { clientManager = this }
        }
    }


    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        job = null

        clientManager?.release()
        clientManager = null
    }


    fun sendAnswer(answerId: Int) {
        // TODO
        _selectedAnswer.value = answerId

        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            // Global handler
            Log.e("AAA", "Error", t)
        }
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            clientManager?.sendSelectedAnswer(answerId)
        }
    }
}