package no.nordicsemi.andorid.ble.test.client.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.client.repository.ScanningManager
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ClientViewModel @Inject constructor(
    private val scanningManager: ScanningManager,
): ViewModel() {
    private val TAG = ClientViewModel::class.java.simpleName
    val bluetoothDevice = scanningManager.bluetoothDevice

    init {
        viewModelScope.launch {
            try {
                scanningManager.scanningForServer()
            } catch (exception: Exception) {
                throw Exception("Could not start scanning.", exception)
            }
        }
    }
}