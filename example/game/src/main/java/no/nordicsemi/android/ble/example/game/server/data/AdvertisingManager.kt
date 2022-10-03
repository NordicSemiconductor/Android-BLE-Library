package no.nordicsemi.android.ble.example.game.server.data

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject


class AdvertisingManager @Inject constructor(
    @ApplicationContext context: Context
        ) {
    private val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val leAdvertiser: BluetoothLeAdvertiser = manager.adapter.bluetoothLeAdvertiser

    private val advertisingSettings: AdvertiseSettings by lazy {
        AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
    }

    private val advertiseData:AdvertiseData by lazy {
        AdvertiseData.Builder()
            .setIncludeDeviceName(false) // Including it will blow the length
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE))
            .build()
    }

    private val advertisingCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d("BLE Advertising", "onStartSuccess: success!!")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("BLE Advertising", "Advertising onStartFailure: $errorCode")
            super.onStartFailure(errorCode)
        }
    }


    fun startAdvertising(){
        leAdvertiser.startAdvertising(advertisingSettings, advertiseData, advertisingCallback)
    }
}