package no.nordicsemi.android.ble.example.game.server.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.example.game.client.repository.AdvertisingException
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class AdvertisingManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?,
) {
    private val TAG = "BLE Advertising"
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        if (bluetoothAdapter != null) bluetoothAdapter.bluetoothLeAdvertiser
        else throw NullPointerException("Bluetooth not initialized")
    }
    var advertisingCallback: AdvertiseCallback? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startAdvertising() = suspendCancellableCoroutine { continuation ->
        advertisingCallback = object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "onStartSuccess: success!!")
                continuation.resume(Unit) { }
            }

            override fun onStartFailure(errorCode: Int) {
                continuation.resumeWithException(AdvertisingException(errorCode))
            }
        }

        continuation.invokeOnCancellation {
            bluetoothLeAdvertiser.stopAdvertising(advertisingCallback)
        }

        val advertisingSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()

        val advertisingData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        bluetoothLeAdvertiser.startAdvertising(
            advertisingSettings,
            advertisingData,
            scanResponse,
            advertisingCallback
        )
    }

    fun stopAdvertising() {
        bluetoothLeAdvertiser.stopAdvertising(
            advertisingCallback
        )
    }
}