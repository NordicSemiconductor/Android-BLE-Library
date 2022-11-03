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
import no.nordicsemi.android.ble.example.game.server.data.ClientResult
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.server.data.Result
import no.nordicsemi.android.ble.example.game.server.data.Results
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
    val TAG = "Server Connection"
    var clients: MutableStateFlow<List<ServerConnection>> = MutableStateFlow(emptyList())
    private var _state = MutableStateFlow<GameState>(WaitingForPlayers(0))
    val state = _state.asStateFlow()
    val isGameOver: MutableState<Boolean?> = mutableStateOf(null)

    private val _selectedAnswer: MutableState<Int?> = mutableStateOf(null)
    val selectedAnswer: State<Int?> = _selectedAnswer
    private val _correctAnswerId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val correctAnswerId = _correctAnswerId.asStateFlow()

    private var questionSaved: Questions? = null
    var questionIndex = 0
    private val totalQuestions = questionSaved?.questions?.size ?: 10
    val savedResult: MutableStateFlow<List<Result>> = MutableStateFlow(emptyList())

    init {
        startServer()
    }

    fun startGame(category: Int? = null) {
        stopAdvertising()
        isGameOver.value = false
        questionIndex = 0

        viewModelScope.launch {
            _state.emit(DownloadingQuestions)
            val questions = questionRepository.getQuestions(category = category)
            questionSaved = questions
            /** Send first Question */
            showQuestion(questions.questions[0])
        }
    }

    fun showNextQuestion() {
        viewModelScope.launch {
            questionSaved?.let { questions ->
                questionIndex.takeIf { it + 1 < totalQuestions }
                    ?.let { ++questionIndex }
                    ?.let { questions.questions[it] }
                    /** Send Next Question */
                    ?.let { showQuestion(it) }
                    ?: run {
                        isGameOver.value = true
                        /** Send game over flag and results to all players.*/
                        clients.value.forEach {
                            it.gameOver(
                                Results(
                                    isGameOver = true,
                                    result = savedResult.value
                                )
                            )
                        }
                    }
            }
        }
    }

    private suspend fun showQuestion(question: Question) {
        clients.value.forEach { it.sendQuestion(question) }
        startCountDown()
        var job: Job? = null
        job = timerFinished
            .onEach {
                clients.value.forEach {
                    question.correctAnswerId?.let { answer ->
                        it.sendCorrectAnswerId(answer)
                        _correctAnswerId.value = answer
                    }
                }
                job?.cancel()
            }
            .launchIn(viewModelScope)
        _selectedAnswer.value = null
        _correctAnswerId.value = null
        _state.value = Round(question)
    }

    private fun startServer() {
        viewModelScope.launch {
            try {
                advertiser.startAdvertising()
            } catch (exception: Exception) {
                Log.d(TAG, "Error in starting server with exception $exception")
            }
        }

        serverManager.setServerObserver(object : ServerObserver {

            override fun onServerReady() {
                Log.w(TAG, "Server is Ready.")
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                ServerConnection(context, viewModelScope, device)
                    .apply {
                        stateAsFlow()
                            .onEach { connectionState ->
                                val currentState = _state.value
                                when (connectionState) {
                                    ConnectionState.Ready -> {
                                        clients.value += this
                                        _state.value = WaitingForPlayers(clients.value.size)
                                    }
                                    is ConnectionState.Disconnected -> {
                                        clients.value -= this

                                        when (currentState) {
                                            is WaitingForPlayers -> {
                                                _state.value = WaitingForPlayers(clients.value.size)
                                            }
                                            else -> {
                                                // TODO handle disconnection during game
                                                Log.d(
                                                    TAG,
                                                    "Device Disconnected: $device Disconnected From the Server."
                                                )
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        playersName
                            .onEach { savePlayersName(it) }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        result
                            .onEach { saveScore(it) }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        useServer(serverManager)
                        viewModelScope.launch(Dispatchers.IO) {
                            connect()
                        }
                    }
            }

            override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
                Log.w(TAG, "Device Disconnected: $device Disconnected From the Server.")
            }
        })
        serverManager.open()
    }

    fun savePlayersName(playerName: String) {
        savedResult.value += Result(
            name = playerName,
            score = 0
        )
        /** Send name to all players to prevent duplicate */
        viewModelScope.launch {
            clients.value.forEach {
                it.gameOver(Results(false, savedResult.value))
            }
        }
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
        clients.value.forEach {
            it.release()
        }
        stopServer()
    }

    fun selectedAnswerServer(playerName: String, selectedAnswer: Int) {
        _selectedAnswer.value = selectedAnswer
        saveScore(ClientResult(playerName, selectedAnswer))
    }

    private fun saveScore(result: ClientResult) {
        questionSaved?.let { question ->
            result.takeIf { it.selectedAnswerId == question.questions[questionIndex].correctAnswerId }
                ?.let { updateScore(it.playersName) }
        }
    }

    private fun updateScore(playersName: String) {
        savedResult.value.find { it.name == playersName }
            ?.let { it.score = it.score + 1 }
    }
}