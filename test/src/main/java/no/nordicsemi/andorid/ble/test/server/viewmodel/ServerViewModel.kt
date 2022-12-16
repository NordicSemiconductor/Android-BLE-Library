package no.nordicsemi.andorid.ble.test.server.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.data.TestEvent
import no.nordicsemi.andorid.ble.test.server.data.TestItem
import no.nordicsemi.andorid.ble.test.server.repository.AdvertisingManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.repository.ServerManager
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
                updateTestList(TestEvent(TestItem.START_ADVERTISING.item, true))
            } catch (exception: Exception) {
                updateTestList(TestEvent(TestItem.START_ADVERTISING.item, false))
                throw Exception("Could not start server.", exception)
            }
        }

        serverManager.setServerObserver(object : ServerObserver {
            override fun onServerReady() {
                _serverViewState.value = _serverViewState.value.copy(
                    testItems = updateTestList(
                        TestEvent(
                            TestItem.SERVER_READY.item,
                            true
                        )
                    )
                )
            }

            override fun onDeviceConnectedToServer(device: BluetoothDevice) {
                ServerConnection(context, viewModelScope, device)
                    .apply {
                        useServer(serverManager)
                        viewModelScope
                            .launch {
                                connect()
                                testWrite()
                                testWriteWithMerger()
                                testNotification()
                                testIndication()
                            }
                    }
                    .apply {
                        testingFeature
                            .onEach { updateTestList(TestEvent(it.testName, it.isPassed)) }
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
                Log.d(TAG, "onDeviceDisconnectedFromServer: $device disconnected")
                updateTestList(TestEvent(TestItem.DEVICE_DISCONNECTION.item, true))
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

    private fun updateTestList(testEvent: TestEvent): List<TestEvent> {
        _serverViewState.value.testItems.find { it.testName == testEvent.testName }
            ?.let { it.isPassed == testEvent.isPassed }
            ?: run {
                _serverViewState.value = _serverViewState.value.copy(
                    testItems = _serverViewState.value.testItems + TestEvent(
                        testEvent.testName,
                        testEvent.isPassed
                    )
                )
            }
        return _serverViewState.value.testItems
    }

}