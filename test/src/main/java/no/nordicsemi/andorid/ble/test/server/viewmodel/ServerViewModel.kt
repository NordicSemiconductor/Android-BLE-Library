package no.nordicsemi.andorid.ble.test.server.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.AdvertisingManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.repository.ServerManager
import no.nordicsemi.andorid.ble.test.server.tasks.ServerTaskPerformer
import no.nordicsemi.andorid.ble.test.spec.Connections.DEVICE_DISCONNECTION
import no.nordicsemi.andorid.ble.test.spec.Connections.SERVER_READY
import no.nordicsemi.andorid.ble.test.spec.Connections.START_ADVERTISING
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.observer.ServerObserver
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ServerViewModel @Inject constructor(
    private val advertisingManager: AdvertisingManager,
    private val serverManager: ServerManager,
    private val serverConnection: ServerConnection,
    private val serverTaskPerformer: ServerTaskPerformer,
) : ViewModel() {
    private val client: MutableStateFlow<List<ServerConnection>> = MutableStateFlow(emptyList())
    private val _serverViewState: MutableStateFlow<ServerViewState> =
        MutableStateFlow(ServerViewState())
    val serverViewState = _serverViewState.asStateFlow()

    init {
        startServer()
    }

    private fun startServer() {
        viewModelScope.launch {
            try {
                advertisingManager.startAdvertising()
                updateTestList(TestCase(START_ADVERTISING, true))
            } catch (exception: Exception) {
                updateTestList(TestCase(START_ADVERTISING, false))
                exception.printStackTrace()
                return@launch
            }
        }

        serverManager.setServerObserver(object : ServerObserver {
            override fun onServerReady() {
                updateTestList(TestCase(SERVER_READY, true))
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                serverConnection
                    .apply {
                        useServer(serverManager)
                        viewModelScope
                            .launch {
                                connectDevice(device)
                                stopAdvertising()
                                // Start the testing tasks after server connection
                                serverTaskPerformer.startTasks()
                                serverTaskPerformer.testCases
                                    .onEach {
                                        it.forEach { tc -> updateTestList(tc) }
                                    }
                                    .launchIn(viewModelScope)
                            }
                    }

                    .apply {
                        testCases
                            .onEach { updateTestList(TestCase(it.testName, it.isPassed)) }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        stateAsFlow()
                            .onEach { connectionState ->
                                val currentState = _serverViewState.value.state
                                when (connectionState) {
                                    ConnectionState.Ready -> {
                                        client.value += this
                                        _serverViewState.value = _serverViewState.value.copy(
                                            state = WaitingForClient(client.value.size)
                                        )
                                    }
                                    is ConnectionState.Disconnected -> {
                                        client.value -= this
                                        when (currentState) {
                                            is WaitingForClient -> {
                                                _serverViewState.value =
                                                    _serverViewState.value.copy(
                                                        state = WaitingForClient(client.value.size)
                                                    )
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }.launchIn(viewModelScope)
                    }
            }

            override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
                updateTestList(TestCase(DEVICE_DISCONNECTION, true))
            }
        })
        serverManager.open()
    }

    private fun stopServer() {
        serverManager.close()
    }

    private fun stopAdvertising() {
        advertisingManager.stopAdvertising()
    }

    override fun onCleared() {
        super.onCleared()
        stopAdvertising()

        client.value.forEach { it.release() }
        stopServer()
    }

    /**
     * It accepts as input a TestCase object and updates a list of TestCase objects.
     * The list of TestCase objects is first converted into a HashMap, with each TestCase being mapped to its testName.
     * The required TestCase is then changed in the map based on whether or not the item exists in the map.
     * The map is converted back into a list of TestCase objects, which are then set as the _serverViewState's new testItems.
     *
     * @param testCase a TestCase object to be added or updated in the testItems list.
     */
    private fun updateTestList(testCase: TestCase) {
        val testCaseMap =
            _serverViewState.value.testItems.associateBy { it.testName }.toMutableMap()
        testCaseMap[testCase.testName] = testCase
        _serverViewState.value =
            _serverViewState.value.copy(testItems = testCaseMap.values.toList())
    }
}