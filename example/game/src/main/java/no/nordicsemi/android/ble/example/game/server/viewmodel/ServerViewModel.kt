package no.nordicsemi.android.ble.example.game.server.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import no.nordicsemi.android.ble.example.game.client.view.Result
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.example.game.client.view.Results
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.server.repository.AdvertisingManager
import no.nordicsemi.android.ble.example.game.server.repository.ServerConnection
import no.nordicsemi.android.ble.example.game.server.repository.ServerManager
import no.nordicsemi.android.ble.example.game.timer.TimerViewModel
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.observer.ServerObserver
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val advertiser: AdvertisingManager,
    private val serverManager: ServerManager,
    private val questionRepository: QuestionRepository,
) : TimerViewModel() {
    private var clients = mutableListOf<ServerConnection>()

    private var _state = MutableStateFlow<GameState>(WaitingForPlayers(0))
    val state = _state.asStateFlow()
    private val gameStartedFlag = mutableStateOf(false)

    private val _clientAnswer = MutableSharedFlow<Int>()
    val clientAnswer = _clientAnswer.asSharedFlow()

    private val _clientDevice = MutableSharedFlow<String>()
    val clientDevice = _clientDevice.asSharedFlow()

    private val _selectedAnswer: MutableState<Int?> = mutableStateOf(null)
    val selectedAnswer: State<Int?> = _selectedAnswer

    private val _correctAnswerId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val correctAnswerId = _correctAnswerId.asStateFlow()
    private var questionSaved: Questions? = null
    var index = 0

    private val totalQuestions: Int = questionSaved?.questions?.size ?: 10

    init {
        startServer()
    }


    fun startGame(category: Int? = null) {
        advertiser.stopAdvertising()
        gameStartedFlag.value = true

        viewModelScope.launch {
            _state.emit(DownloadingQuestions)
            val questions = questionRepository.getQuestions(category = category)
            questionSaved = questions

            // TODO send questions
            showQuestion(questions)

        }
    }

    private suspend fun showQuestion(questions: Questions) {
        clients.forEach {
            if (gameStartedFlag.value) {
                it.sendQuestion(questions.questions[index])
            }
        }

        startCountDown()

        timerFinished
            .onEach {
                clients.forEach {
                    questions.questions[index].correctAnswerId?.let { answer ->
                        it.sendCorrectAnswerId(answer)
                        _correctAnswerId.value = answer
                    }
                }
            }
            .launchIn(viewModelScope)
        // start game :)
        _selectedAnswer.value = null
        _correctAnswerId.value = null
        _uiState.emit(questions.questions[index])
        _state.emit(Round(questions.questions[index]))
    }

    private fun startServer() {
        advertiser.startAdvertising()
        serverManager.setServerObserver(object : ServerObserver {

            override fun onServerReady() {
                Log.w("AAA startServer", "onServerReady")
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                ServerConnection(context, viewModelScope, device)
                    .apply {
                        stateAsFlow()
                            .onEach { connectionState ->
                                val currentState = _state.value
                                when (connectionState) {
                                    ConnectionState.Ready -> {
                                        clients.add(this)
                                        _state.value = WaitingForPlayers(clients.size)
                                    }
                                    is ConnectionState.Disconnected -> {
                                        clients.remove(this)

                                        when (currentState) {
                                            is WaitingForPlayers -> {
                                                _state.value = WaitingForPlayers(clients.size)
                                            }
                                            else -> {
                                                // TODO handle disconnection during game
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        replies
                            .onEach {
                                /* TODO handle response received */
                                _clientAnswer.tryEmit(it)
                                saveScore(it, deviceName = "Client-123")
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        clientDeviceName
                            .onEach {
                                /* TODO handle device name received from the client(s) */
                                _clientDevice.tryEmit(it)
                                Result(it, 0)
                                println("Result, ${Result(it, 0)}")
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        useServer(serverManager)

                        // TODO exceptions
                        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                            // Global handler
                            Log.e("AAA", "Error", throwable)
                        }
                        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
                            connect()
                        }
                    }

            }

            override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
                Log.w("AAA", "onDeviceDisconnectedFromServer")
            }

        })
        serverManager.open()
    }

    private fun stopServer() {
        serverManager.close()
    }

    private fun stopAdvertising() {
        advertiser.stopAdvertising()
    }

    override fun onCleared() {
        super.onCleared()
        stopAdvertising()

        clients.forEach {
            it.release()
        }

        stopServer()
    }

    fun selectedAnswerServer(selectedAnswer: Int) {
        _selectedAnswer.value = selectedAnswer
        saveScore(selectedAnswer, deviceName = "Server-123")
    }

    val result: Results? = null
    private fun saveScore(selectedAnswer: Int, deviceName: String) {
        questionSaved?.let { question ->
            if (selectedAnswer == question.questions[index].correctAnswerId) {
                Log.d(
                    "AAA Correct Answer 888",
                    "selectedAnswerServer: $selectedAnswer $deviceName "
                )
            }
        }
    }
}