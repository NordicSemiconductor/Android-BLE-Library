package no.nordicsemi.android.ble.example.game.server.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.ble.example.game.spec.DeviceSpecifications.Companion.UUID_SERVICE_DEVICE
import javax.inject.Inject

class ScannerRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = manager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    val devices: MutableStateFlow<List<ScanResult>> = MutableStateFlow(emptyList())
    private val leScanCallback: ScanCallback by lazy {
        object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result
                    ?.takeIf { checkDuplicateScanResult(devices.value, it) }
                    ?.also { devices.value += it }
                    .also { Log.d("Scanned devices", "onScanResult: ${devices.value}") }
            }
            override fun onBatchScanResults(result: List<ScanResult>) {
                result.forEach {
                    if (checkDuplicateScanResult(devices.value, it)) {
                        devices.value += it
                        Log.d("Scanned devices", "onScanResult: ${devices.value}")
                    }
                }
            }
            override fun onScanFailed(errorCode: Int) {
                Log.d("Scan Fail:", "onScanFailed: $errorCode")
            }
        }
    }

    private val scanSettings: ScanSettings by lazy {
        ScanSettings.Builder()
            .setLegacy(false)
            .setReportDelay(0)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }

    private fun scanFilters(): MutableList<ScanFilter> {
        val list: MutableList<ScanFilter> = ArrayList()
        val scanFilterName =
                ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID_SERVICE_DEVICE)).build()
        list.add(scanFilterName)
        return list
    }

    private fun checkDuplicateScanResult(value: List<ScanResult>, result: ScanResult): Boolean {
        return !value.any { it.device == result.device }
    }

    fun startScanning() {
        bluetoothLeScanner
            .startScan(
                scanFilters(),
                scanSettings,
                leScanCallback
            )
    }

    fun stopScan() {
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    fun clear() {
        devices.value = emptyList()
    }
}
