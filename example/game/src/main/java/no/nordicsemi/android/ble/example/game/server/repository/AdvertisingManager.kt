package no.nordicsemi.android.ble.example.game.server.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject


class AdvertisingManager @Inject constructor(
    private val leAdapter: BluetoothAdapter,
) {
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser by lazy {
        leAdapter.bluetoothLeAdvertiser
    }

    private val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d("BLE Advertising", "onStartSuccess: success!!")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE Advertising", "Advertising onStartFailure: $errorCode")
        }

    }

    fun startAdvertising(){
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