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
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.repository.ScanningManager
import no.nordicsemi.andorid.ble.test.client.task.TaskPerformer
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.spec.Connections.SCANNING_FOR_SERVER
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
                updateTestList(TestCase(SCANNING_FOR_SERVER, true))
                scanningManager.scanningForServer()
            } catch (exception: Exception) {
                updateTestList(TestCase(SCANNING_FOR_SERVER, false))
                throw Exception("Could not start scanning.", exception)
            }
            ClientConnection(context, viewModelScope, device)
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
                    connect()
                    // Start the testing tasks after client connection
                    val taskPerformer = TaskPerformer(this)
                    taskPerformer.startTasks()
                    taskPerformer.testCases
                        .onEach {
                            it.forEach { tc -> updateTestList(tc) }
                        }
                        .launchIn(viewModelScope)
                }
                .apply { clientConnection = this }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clientConnection?.release()
        clientConnection = null
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
    @SuppressLint("SuspiciousIndentation")
    private fun updateTestList(testCase: TestCase): List<TestCase> {
        val updatedTestCaseList = _clientViewState.value.testItems.toMutableList()
        _clientViewState.value.testItems.find { it.testName == testCase.testName }
            ?.let {
                val index = _clientViewState.value.testItems.indexOf(it)
                if(it.isPassed != testCase.isPassed)
                    updatedTestCaseList[index] = TestCase(it.testName, testCase.isPassed)
                    _clientViewState.value = _clientViewState.value.copy(
                        testItems = updatedTestCaseList
                    )
            }
            ?: run {
                _clientViewState.value = _clientViewState.value.copy(
                    testItems = _clientViewState.value.testItems + TestCase(
                        testCase.testName,
                        testCase.isPassed
                    )
                )
            }
        return _clientViewState.value.testItems
    }
}