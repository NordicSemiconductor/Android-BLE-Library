package no.nordicsemi.android.ble.ble_gatt_server

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log

object BleAdvertiser {
	private const val TAG = "ble-advertiser"

	class Callback : AdvertiseCallback() {
		override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
			Log.i(TAG, "LE Advertise Started.")
		}

		override fun onStartFailure(errorCode: Int) {
			Log.w(TAG, "LE Advertise Failed: $errorCode")
		}
	}

	fun settings(): AdvertiseSettings {
		return AdvertiseSettings.Builder()
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
				.setConnectable(true)
				.setTimeout(0)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
				.build()
	}

	fun advertiseData(): AdvertiseData {
		return AdvertiseData.Builder()
				.setIncludeDeviceName(false) // Including it will blow the length
				.setIncludeTxPowerLevel(false)
				.addServiceUuid(ParcelUuid(GattService.MyServiceProfile.MY_SERVICE_UUID))
				.build()
	}
}