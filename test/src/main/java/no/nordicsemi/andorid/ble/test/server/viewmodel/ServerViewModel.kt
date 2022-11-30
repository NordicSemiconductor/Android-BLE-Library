package no.nordicsemi.andorid.ble.test.server.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.andorid.ble.test.server.repository.AdvertisingManager
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val advertisingManager: AdvertisingManager,
): ViewModel() {
    private val TAG = ServerViewModel::class.java.simpleName

    init {
        viewModelScope.launch {
            try {
                advertisingManager.startAdvertising()
            } catch (exception: Exception) {
                throw Exception("Could not start server.", exception)
            }
        }
    }
}