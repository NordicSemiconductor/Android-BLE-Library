package no.nordicsemi.andorid.ble.test.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)
class AdvertisingManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
) {
    private val TAG = AdvertisingManager::class.java.simpleName
    private var advertisingCallback: AdvertiseCallback? = null
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
            ?: throw NullPointerException("Bluetooth not initialized")
    }

    suspend fun startAdvertising() = suspendCancellableCoroutine { continuation ->
        advertisingCallback = object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "onStartSuccess:Started Advertising ")
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