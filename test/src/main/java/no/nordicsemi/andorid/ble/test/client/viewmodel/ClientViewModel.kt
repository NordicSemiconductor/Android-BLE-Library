package no.nordicsemi.andorid.ble.test.client.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.repository.ScanningManager
import no.nordicsemi.andorid.ble.test.client.task.ClientTaskPerformer
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Connections.SCANNING_FOR_SERVER
import no.nordicsemi.android.ble.ktx.stateAsFlow
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ClientViewModel @Inject constructor(
    private val scanningManager: ScanningManager,
    private val clientConnection: ClientConnection,
    private val clientTaskPerformer: ClientTaskPerformer,
) : ViewModel() {
    private val _clientViewState: MutableStateFlow<ClientViewState> = MutableStateFlow(ClientViewState())
    val clientViewState = _clientViewState.asStateFlow()

    init {
        viewModelScope.launch {
            val device = try {
                updateTestList(TestCase(SCANNING_FOR_SERVER, true))
                scanningManager.scanningForServer()
            } catch (exception: Exception) {
                updateTestList(TestCase(SCANNING_FOR_SERVER, false))
                exception.printStackTrace()
                return@launch
            }
            clientConnection
                .apply {
                    stateAsFlow()
                        .onEach { _clientViewState.value = _clientViewState.value.copy(state = it) }
                        .launchIn(viewModelScope)
                    testCase
                        .onEach {
                            updateTestList(TestCase(it.testName, it.isPassed))
                        }
                        .launchIn(viewModelScope)
                }
                .apply {
                    connectDevice(device)
                    // Start testing tasks after client connection
                    clientTaskPerformer.testCases
                        .onEach {
                            it.forEach { tc -> updateTestList(tc) }
                        }
                        .launchIn(viewModelScope)
                    clientTaskPerformer.startTasks()
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clientConnection.release()
    }

    /**
     * It accepts as input a TestCase object and updates a list of TestCase objects.
     * The list of TestCase objects is first converted into a HashMap, with each TestCase being mapped to its testName.
     * The required TestCase is then changed in the map based on whether or not the item exists in the map.
     * The map is converted back into a list of TestCase objects, which are then set as the _clientViewState's new testItems.
     *
     * @param testCase a TestCase object to be added or updated in the testItems list.
     */
    private fun updateTestList(testCase: TestCase) {
        val testCaseMap =
            _clientViewState.value.testItems.associateBy { it.testName }.toMutableMap()
        testCaseMap[testCase.testName] = testCase
        _clientViewState.value =
            _clientViewState.value.copy(testItems = testCaseMap.values.toList())
    }

}