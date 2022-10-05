package no.nordicsemi.android.ble.example.game.server.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.example.game.server.repository.AdvertisingManager
import no.nordicsemi.android.ble.example.game.server.repository.ServerConnection
import no.nordicsemi.android.ble.example.game.server.repository.ServerManager
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.observer.ServerObserver
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val advertiser: AdvertisingManager,
    private val serverManager: ServerManager,
) : AndroidViewModel(context as Application) {
    private var clients = mutableListOf<ServerConnection>()

    private var _numberOfClients = MutableStateFlow(0)
    val clientsNumber = _numberOfClients.asStateFlow()

    init {
        startServer()
    }

    fun startGame() {
        Log.d("AAA", "startGame")
        advertiser.stopAdvertising()

        // TODO
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
                            .onEach {
                                // TODO disconnection events
                                clients.remove(this)
                                if (_numberOfClients.value>0) _numberOfClients.value -= 1
                            }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        replies
                            .onEach { reply() }
                            .launchIn(viewModelScope)
                    }
                    .apply {
                        useServer(serverManager)

                        // TODO exceptions
                        val exceptionHandler = CoroutineExceptionHandler { c, t ->
                            // Global handler
                            Log.e("AAA", "Error", t)
                        }
                        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
                            connect()
                            _numberOfClients.value += 1
                        }
                    }
                    .apply { clients.add(this) }

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