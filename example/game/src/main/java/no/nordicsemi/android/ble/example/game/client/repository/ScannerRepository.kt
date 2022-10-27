package no.nordicsemi.android.ble.example.game.client.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScannerRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val leAdapter: BluetoothAdapter,
) {
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        // FIXME This may return null if Bluetooth is off
        leAdapter.bluetoothLeScanner
    }

    /**
     * Starts scanning for an advertising server.
     * Returns the first found device.
     */
    suspend fun searchForServer() = suspendCancellableCoroutine { continuation ->
        Log.d("ScannerRepository", "startScanning: Scanning Started...")

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result
                    ?.takeIf {
                        it.scanRecord
                            ?.serviceUuids
                            ?.contains(ParcelUuid(UUID_SERVICE_DEVICE)) == true
                    }
                    ?.let { continuation.resume(it.device) }
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
            .setLegacy(true)
            .setReportDelay(0)
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
