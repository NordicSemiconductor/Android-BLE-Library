package no.nordicsemi.andorid.ble.test.client.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.client.data.TestItem
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.repository.ScanningManager
import no.nordicsemi.andorid.ble.test.server.data.TestEvent
import no.nordicsemi.android.ble.ktx.stateAsFlow
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ClientViewModel @Inject constructor(
    private val scanningManager: ScanningManager,
    application: Application
) : AndroidViewModel(application) {
    private val TAG = ClientViewModel::class.java.simpleName
    private var clientConnection: ClientConnection? = null
    private val context = application.applicationContext

    private val _clientViewState: MutableStateFlow<ClientViewState> = MutableStateFlow(ClientViewState())
    val clientViewState = _clientViewState.asStateFlow()

    init {
        viewModelScope.launch {
            val device = try {
                updateTestList(TestEvent(TestItem.SCANNING_FOR_SERVER.item, true))
                scanningManager.scanningForServer()
            } catch (exception: Exception) {
                updateTestList(TestEvent(TestItem.SCANNING_FOR_SERVER.item, false))
                throw Exception("Could not start scanning.", exception)
            }
            ClientConnection(context, viewModelScope, device)
                .apply {
                    stateAsFlow()
                        .onEach { _clientViewState.value = _clientViewState.value.copy(state = it) }
                        .launchIn(viewModelScope)
                    testingFeature
                        .onEach {
                            updateTestList(TestEvent(it.testName, it.isPassed))
                        }
                        .launchIn(viewModelScope)
                }
                .apply {
                    connect()
                    testWrite()
                    testNotificationsWithCallback()
                    testIndicationsWithCallback()
                }
                .apply { clientConnection = this }
        }
    }


    override fun onCleared() {
        super.onCleared()
        clientConnection?.release()
        clientConnection = null
    }

    private fun updateTestList(testEvent: TestEvent): List<TestEvent> {
        _clientViewState.value.testItems.find { it.testName == testEvent.testName }
            ?.let { it.isPassed == testEvent.isPassed }
            ?: run {
                _clientViewState.value = _clientViewState.value.copy(
                    testItems = _clientViewState.value.testItems + TestEvent(
                        testEvent.testName,
                        testEvent.isPassed
                    )
                )
            }
        return _clientViewState.value.testItems
    }
}