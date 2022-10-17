package no.nordicsemi.android.ble.example.game.client.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.example.game.client.repository.ClientConnection
import no.nordicsemi.android.ble.example.game.client.repository.ScannerRepository
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.common.navigation.NavigationManager
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val scannerRepository: ScannerRepository,
    private val navigationManager: NavigationManager,
) : AndroidViewModel(context as Application) {
    private var clientManager: ClientConnection? = null
    private var job: Job? = null

    private val _question: MutableStateFlow<Question?> = MutableStateFlow(null)
    val question = _question.asStateFlow()
    private val _answer: MutableStateFlow<Int?> = MutableStateFlow(null)
    val answer = _answer.asStateFlow()

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
            Log.w("AAA", "Device found!")

            ClientConnection(
                getApplication(),
                viewModelScope,
                device
            )
                .apply {
                    stateAsFlow()
                        .onEach {
                            Log.w("AAA", "State: $it")
                            _state.value = it
                        }
                        .launchIn(viewModelScope)
                    question
                        .onEach {
                            _answer.value = null
                            _question.value = it
                        }
                        .launchIn(viewModelScope)
                    answer
                        .onEach { _answer.value = it }
                        .launchIn(viewModelScope)
                }
                .apply {
                    connect()
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
}