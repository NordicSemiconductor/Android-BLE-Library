package no.nordicsemi.andorid.ble.test.scanner.viewmodel

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
import no.nordicsemi.andorid.ble.test.scanner.data.TestItem
import no.nordicsemi.andorid.ble.test.advertiser.view.TestEvent
import no.nordicsemi.andorid.ble.test.scanner.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.scanner.repository.ScanningManager
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

    private val _scanningStateView: MutableStateFlow<ClientViewState> = MutableStateFlow(ClientViewState())
    val scanningStateView = _scanningStateView.asStateFlow()

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
                        .onEach { _scanningStateView.value = _scanningStateView.value.copy(state = it) }
                        .launchIn(viewModelScope)
                }
                .apply {
                    connect()
                    hello()
                    updateTestList(TestEvent(TestItem.CONNECTED_WITH_SERVER.item, true))
                }
                .apply { clientConnection = this }
        }
    }


    override fun onCleared() {
        super.onCleared()
        clientConnection?.release()
        clientConnection = null
    }

    private fun updateTestList(testEvent: TestEvent):  List<TestEvent> {
        _scanningStateView.value.testItems.find { it.testName == testEvent.testName }
            ?.let { it.isPassed == testEvent.isPassed }
            ?: run {
                _scanningStateView.value = _scanningStateView.value.copy(
                    testItems = _scanningStateView.value.testItems + TestEvent(
                        testEvent.testName,
                        testEvent.isPassed
                    )
                )
            }
        return _scanningStateView.value.testItems
    }
}