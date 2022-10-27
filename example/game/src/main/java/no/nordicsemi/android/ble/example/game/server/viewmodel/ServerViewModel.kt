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
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.server.data.ClientResult
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
    var clients: MutableStateFlow<List<ServerConnection>> = MutableStateFlow(emptyList())
    private val TAG = "Server Connection"
    private var _state = MutableStateFlow<GameState>(WaitingForPlayers(0))
    val state = _state.asStateFlow()
    val isGameOver: MutableState<Boolean?> = mutableStateOf(null)

    private val _clientDevice = MutableSharedFlow<String>()
    val clientDevice = _clientDevice.asSharedFlow()

    private val _selectedAnswer: MutableState<Int?> = mutableStateOf(null)
    val selectedAnswer: State<Int?> = _selectedAnswer

    private val _correctAnswerId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val correctAnswerId = _correctAnswerId.asStateFlow()
    private var questionSaved: Questions? = null
    var index = 0

    private val totalQuestions: Int = questionSaved?.questions?.size ?: 10
    val savedResult: MutableStateFlow<List<FinalResult>> = MutableStateFlow(emptyList())

    init {
        startServer()
    }


    fun startGame(category: Int? = null) {
        advertiser.stopAdvertising()
        isGameOver.value = false
        index = 0

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
                index.takeIf { it + 1 < totalQuestions }
                    ?.let { ++index }
                    ?.let { questions.questions[it] }
                    ?.let { showQuestion(it) }
                    ?: run {
                        isGameOver.value = true
                        clients.value.forEach {
                            it.gameOver(ResultToClient(isGameOver.value!!, savedResult.value))
                        }
                    }
            }
        }
    }

    private suspend fun showQuestion(question: Question) {

        clients.value.forEach {
            if (isGameOver.value == false) {
                it.sendQuestion(question)
                Log.d("AAA Server 44", "showQuestion: $question")
            }
        }

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
        advertiser.startAdvertising()
        serverManager.setServerObserver(object : ServerObserver {

            override fun onServerReady() {
                Log.w("Stat Server", "Server is Ready!!")
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
                                _clientAnswer.tryEmit(it)
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
                Log.w("Device Disconnected From Server", "Device Disconnected From the Server!")
            }

        })
        serverManager.open()
    }

    fun savePlayersName(playerName: String) {
        if (playerName != "") {
            savedResult.value += FinalResult(
                name = playerName,
                score = 0
            )
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
            result.takeIf { it.selectedAnswerId == question.questions[index].correctAnswerId }
                ?.let { updateScore(it.playersName) }
        }
    }

    private fun updateScore(deviceName: String) {
        savedResult.value.find { it.name == deviceName }
            ?.let { it.score = it.score + 1 }
            ?: run { savedResult.value += (FinalResult(deviceName, 1)) }
    }
}