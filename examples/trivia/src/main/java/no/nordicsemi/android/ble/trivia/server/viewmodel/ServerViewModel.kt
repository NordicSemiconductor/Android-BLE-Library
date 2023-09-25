package no.nordicsemi.android.ble.trivia.server.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
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
import no.nordicsemi.android.ble.trivia.server.repository.Question
import no.nordicsemi.android.ble.trivia.server.repository.QuestionRepository
import no.nordicsemi.android.ble.trivia.server.repository.Questions
import no.nordicsemi.android.ble.trivia.server.repository.AdvertisingManager
import no.nordicsemi.android.ble.trivia.server.data.*
import no.nordicsemi.android.ble.trivia.server.repository.ServerConnection
import no.nordicsemi.android.ble.trivia.server.repository.ServerManager
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.observer.ServerObserver
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val advertiser: AdvertisingManager,
    private val serverManager: ServerManager,
    private val questionRepository: QuestionRepository,
) : TimerViewModel(context) {
    private val TAG: String = ServerViewModel::class.java.simpleName

    private val _serverState: MutableStateFlow<ServerViewState> =
        MutableStateFlow(ServerViewState())
    val serverViewState = _serverState.asStateFlow()
    private var clients: MutableStateFlow<List<ServerConnection>> = MutableStateFlow(emptyList())
    private var questionSaved: Questions? = null
    private var questionIndex = 0
    private val totalQuestions = questionSaved?.questions?.size ?: 10
    private val mapNameWithDevice: MutableStateFlow<List<Name>> = MutableStateFlow(emptyList())

    init {
        startServer()
    }

    fun startGame(category: Int? = null) {
        stopAdvertising()
        questionIndex = 0

        viewModelScope.launch {
            _serverState.value = _serverState.value.copy(state = DownloadingQuestions)
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
                        _serverState.value = _serverState.value.copy(isGameOver = true)
                        /** Send game over flag and results to all players.*/
                        clients.value.forEach {
                            it.gameOver(true)
                            it.sendResult(
                                Results(_serverState.value.result)
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
                _serverState.value = _serverState.value.copy(
                    correctAnswerId = question.correctAnswerId,
                    ticks = ticks.value
                )
                job?.cancel()
            }
            .launchIn(viewModelScope)
        _serverState.value = _serverState.value.copy(
            state = Round(question),
            ticks = ticks.value,
            correctAnswerId = null,
            selectedAnswerId = null
        )
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
                ServerConnection(getApplication(), viewModelScope, device)
                    .apply {
                        stateAsFlow()
                            .onEach { connectionState ->
                                val currentState = _serverState.value.state
                                when (connectionState) {
                                    ConnectionState.Ready -> {
                                        clients.value += this
                                        _serverState.value = _serverState.value.copy(
                                            state = WaitingForPlayers(clients.value.size)
                                        )
                                    }
                                    is ConnectionState.Disconnected -> {
                                        clients.value -= this
                                        removePlayer(device)
                                        when (currentState) {
                                            is WaitingForPlayers -> {
                                                _serverState.value = _serverState.value.copy(
                                                    state = WaitingForPlayers(clients.value.size)
                                                )
                                            }
                                            else -> Log.d(TAG, "Device Disconnected: $device disconnected from the server.")
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
                                // Validates name and if its valid it will save the name,
                                // otherwise sends an error message to the client.
                                validateName(it, device)
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

    /**
     * A method to remove a disconnected player from the list of all connected users and the result list.
     */
    private fun removePlayer(device: BluetoothDevice) {
        if (_serverState.value.userJoined.isNotEmpty()) {
            val disconnectedPlayer = mapName(device.address)!!
            val oldState = _serverState.value
            _serverState.value =
                oldState.copy(userJoined = oldState.userJoined - Player(disconnectedPlayer))
            // Checks and removes the corresponding player's name if it is not removed from the list.
            val player = _serverState.value.result.find { it.name == disconnectedPlayer }
            when {
                player?.name?.isNotEmpty() == true -> {
                    _serverState.value = _serverState.value.copy(
                        result = _serverState.value.result - Result(
                            player.name,
                            player.score
                        )
                    )
                }
            }
        }
    }

    /** Validate players name sent from a client device.
     * If it is valid, it notifies the client that the name is correct; otherwise,
     * it sends the corresponding error message, either name empty or duplicate name.
     */
    private fun validateName(playersName: String, device: BluetoothDevice) {
        val name = playersName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendEmptyNameError(isEmptyName = true)
                    }
                }
            }
        } else if ((_serverState.value.userJoined.find { it.name == name }?.name == name)) {
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendDuplicateNameError(isDuplicateName = true)
                    }
                }
            }
        } else {
            mapNameAndDevice(
                playerName = playersName,
                deviceAddress = device.address
            )
            viewModelScope.launch {
                clients.value.forEach {
                    if (it.bluetoothDevice == device) {
                        it.sendDuplicateNameError(isDuplicateName = false)
                        it.sendEmptyNameError(isEmptyName = false)
                    }
                }
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


    /** Save the player's name and send it to all players to prevent duplicates. */
    private fun savePlayersName(playerName: String) {
        val oldState = _serverState.value
        _serverState.value = oldState.copy(
            userJoined = oldState.userJoined + Player(playerName),
            result = oldState.result + Result(
                name = playerName,
                score = 0
            )
        )
        viewModelScope.launch {
            clients.value.forEach {
                it.sendNameToAllPlayers(Players(_serverState.value.userJoined))
            }
        }
    }

    fun saveServerPlayer(playerName: String) {
        advertiser.address?.let { mapNameAndDevice(playerName, it) }
    }

    fun selectedAnswerServer(selectedAnswer: Int) {
        _serverState.value = _serverState.value.copy(selectedAnswerId = selectedAnswer)
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
                    _serverState.value.result.find { it.name == mapName(deviceAddress) }
                        ?.let {
                            it.score = it.score + 1
                        }
                }
        }
    }

    /** Map players name with device address. */
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