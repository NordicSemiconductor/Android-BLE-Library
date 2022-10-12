package no.nordicsemi.android.ble.example.game.server.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionRepository
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionState
import no.nordicsemi.android.ble.example.game.quiz.view.Questions
import no.nordicsemi.android.ble.example.game.server.repository.AdvertisingManager
import no.nordicsemi.android.ble.example.game.server.repository.ServerConnection
import no.nordicsemi.android.ble.example.game.server.repository.ServerManager
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
) : AndroidViewModel(context as Application) {
    private var clients = mutableListOf<ServerConnection>()

    private var _state = MutableStateFlow<GameState>(WaitingForPlayers(0))
    val state = _state.asStateFlow()
    private val _uiState = MutableSharedFlow<Questions>()
    val uiState = _uiState
    private val gameStartedFlag = mutableStateOf(false)


    init {
        startServer()
    }

    fun startGame(category: Int? = null) {
        advertiser.stopAdvertising()
        gameStartedFlag.value = true


        viewModelScope.launch {
            _state.emit(DownloadingQuestions)
            val questions = questionRepository.getQuestions(category = category)

            val questionStates = questions.questions.mapIndexed { index, question ->
                val showDone = index == questions.questions.size - 1

                QuestionState(
                    question = question,
                    questionIndex = index,
                    totalQuestions = questions.questions.size,
                    showDone = showDone
                )
            }

            // TODO send questions
            clients.forEach {
                if (gameStartedFlag.value){
//                    it.sayHello()
                    it.gameStart(gameStartedFlag.value)
                    it.sendQuestion(questions.questions[0])
                }
            }
            // start game :)
            _uiState.emit(Questions(questionStates))
            _state.emit(Round(questions.questions[0]))
        }
    }

    private fun startServer(){
        Log.d("AAA", "startServer")
        advertiser.startAdvertising()
        serverManager.setServerObserver(object : ServerObserver {

            override fun onServerReady() {
                Log.w("AAA", "onServerReady")
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                Log.w("AAA", "onDeviceConnectedToServer")

                ServerConnection(getApplication(), viewModelScope, device)
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
                            .onEach { /* TODO handle response received */ }
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

    private fun stopServer(){
        Log.d("AAA", "stopServer")
        serverManager.close()
    }

    private fun stopAdvertising() {
        Log.d("AAA", "stopAdvertising")
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
}

