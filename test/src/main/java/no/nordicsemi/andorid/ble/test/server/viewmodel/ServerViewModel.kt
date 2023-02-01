package no.nordicsemi.andorid.ble.test.server.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
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
import no.nordicsemi.andorid.ble.test.server.tasks.TaskPerformer
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
    application: Application,
) : AndroidViewModel(application) {
    private val TAG = ServerViewModel::class.java.simpleName
    private val context = application.applicationContext
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
                throw Exception("Could not start server.", exception)
            }
        }

        serverManager.setServerObserver(object : ServerObserver {
            override fun onServerReady() {
                updateTestList(TestCase(SERVER_READY, true))
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                ServerConnection(context, viewModelScope, device)
                    .apply {
                        useServer(serverManager)
                        viewModelScope
                            .launch {
                                connect()
                                // Start the testing tasks after server connection
                                val taskPerformer = TaskPerformer(this@apply)
                                taskPerformer.startTasks()
                                taskPerformer.testCases
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
     * This function takes a TestCase object as input and updates a list of TestCase objects.
     * If an object with the same testName as the input testCase exists in the list,
     * it checks if the `isPassed` field of the matched object is different from the `isPassed`
     * field of the input `testCase`. If it is different, it updates the `isPassed` field of the
     * matched object and updates the list. If no object with the same `testName` is found in the list,
     * the input `testCase` object is added to the list.
     * Finally, the updated list of TestCase objects is returned.
     *
     * @param testCase a TestCase object to be added or updated in the testItems list.
     * @return a list of TestCase objects.
     */
    private fun updateTestList(testCase: TestCase){
        val updatedTestCaseList = _serverViewState.value.testItems.map { tc ->
            if (tc.testName == testCase.testName) TestCase(tc.testName, testCase.isPassed)
            else tc
        }
        _serverViewState.value = _serverViewState.value.copy(
            testItems = if (updatedTestCaseList.any { it.testName == testCase.testName }) updatedTestCaseList
            else _serverViewState.value.testItems + testCase
        )
    }
}