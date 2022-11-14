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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.server.data.*
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
    val TAG = "Server ViewModel"
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
    val userJoined: MutableStateFlow<List<Player>> = MutableStateFlow(emptyList())

    private val mapNameWithDevice: MutableStateFlow<List<Name>> = MutableStateFlow(emptyList())

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
            showQuestion(
                (questionSaved?.questions?.get(questionIndex))
                    ?: throw NullPointerException("Unable to download the question.")
            )
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
                            it.gameOver(true)
                            it.sendResult(
                                Results(savedResult.value)
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
                    }
                }
                _correctAnswerId.value = question.correctAnswerId
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
                throw Exception("Could not start server.", exception)
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
                                        // Remove device name from the savedResult and all joined users list.
                                        removePlayer(device)

                                        when (currentState) {
                                            is WaitingForPlayers -> {
                                                _state.value = WaitingForPlayers(clients.value.size)
                                            }
                                            else -> {
                                                Log.d(TAG, "Device Disconnected: $device disconnected from the server.")
                                                removePlayer(device)
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
                            .onEach {
                                if (iInvalidateName(it)) {
                                    // Save only if the name is valid.
                                    mapNameAndDevice(
                                        playerName = it,
                                        deviceAddress = device.address
                                    )
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        clientAnswer
                            .onEach { saveScore(it, device.address) }
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
                Log.w(TAG, "Device Disconnected: $device disconnected from the server.")
                removePlayer(device)
            }
        })
        serverManager.open()
    }

    private fun removePlayer(device: BluetoothDevice) {
        if (userJoined.value.isNotEmpty()) {
            val disconnectedPlayer = mapName(device.address)!!
            userJoined.value -= Player(disconnectedPlayer)
            if (savedResult.value.find { it.name == disconnectedPlayer}?.name?.isNotEmpty() == true){
                savedResult.value -= Result(
                    disconnectedPlayer,
                    savedResult.value.find { it.name == disconnectedPlayer }?.score!!
                )
            }
        }
    }

    /** Validate players name sent from a client device. */
    private fun iInvalidateName(playersName: String): Boolean {
        val name = playersName.trim()
        return !(name.isEmpty() || (savedResult.value.find { it.name == name }?.name == name))
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

    /** Save the player's name and send it to all players to prevent duplicates. */
    private fun savePlayersName(playerName: String) {
        userJoined.value += Player(playerName)
        savedResult.value += Result(
            name = playerName,
            score = 0
        )
        viewModelScope.launch {
            clients.value.forEach {
                it.sendNameToAllPlayers(Players(userJoined.value))
            }
        }
    }

    fun saveServerPlayer(playerName: String) {
        advertiser.address?.let { mapNameAndDevice(playerName, it) }
    }

    fun selectedAnswerServer(selectedAnswer: Int) {
        _selectedAnswer.value = selectedAnswer
        advertiser.address?.let { saveScore(selectedAnswer, it) }
    }

    /** Save score. Before updating the score, it will check for the players' names that are
     * associated with the device address. The mapping is done to avoid the possibility of
     * the client providing a new name each time.
     */
    private fun saveScore(result: Int, deviceAddress: String) {
        questionSaved?.let { question ->
            // Check if selected answer is correct.
            result.takeIf { result == question.questions[questionIndex].correctAnswerId }
                ?.let {
                    // Update score
                    savedResult.value.find { it.name == mapName(deviceAddress) }
                        ?.let { it.score = it.score + 1 }
                }
        }
    }

    /**  Map players name with device address. */
    private fun mapNameAndDevice(playerName: String, deviceAddress: String) {
        mapNameWithDevice.value += Name(
            name = playerName,
            deviceAddress = deviceAddress
        )
        savePlayersName(playerName = playerName)
    }

    private fun mapName(deviceAddress: String): String? {
        return mapNameWithDevice.value.find { it.deviceAddress == deviceAddress }?.name
    }
}