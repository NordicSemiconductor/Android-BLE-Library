package no.nordicsemi.andorid.ble.test.client.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.ParcelUuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.andorid.ble.test.spec.Characteristics.UUID_SERVICE_DEVICE
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
class ScanningManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
) {
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
            ?: throw NullPointerException("Bluetooth not initialized")
    }

    /**
     * Starts scanning for an advertising server.
     * Returns the first found device.
     */
    suspend fun scanningForServer(): BluetoothDevice = suspendCancellableCoroutine { continuation ->

        val callback = object : ScanCallback() {
            var found = false

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                if (found) return
                result
                    ?.let {
                        found = true
                        continuation.resume(it.device) {}
                    }
                    .also { bluetoothLeScanner.stopScan(this) }
            }

            override fun onScanFailed(errorCode: Int) {
                continuation.resumeWithException(ScanningException(errorCode))
            }
        }
        continuation.invokeOnCancellation {
            bluetoothLeScanner.stopScan(callback)
        }

        val scanSettings = ScanSettings.Builder()
            .setReportDelay(0) // Set to 0 to be notified of scan results immediately.
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE))
                .build()
        )

        bluetoothLeScanner.startScan(
            scanFilters,
            scanSettings,
            callback
        )
    }
}