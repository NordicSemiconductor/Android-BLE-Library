package no.nordicsemi.andorid.ble.test.client.repository

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.andorid.ble.test.spec.DeviceSpecifications
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ClientConnection(
    context: Context,
    private val scope: CoroutineScope,
    private val device: BluetoothDevice,
) : BleManager(context) {
    private val TAG = ClientConnection::class.java.simpleName
    var notificationCharacteristic: BluetoothGattCharacteristic? = null
    var indicationCharacteristic: BluetoothGattCharacteristic? = null

    private val _replies: MutableSharedFlow<String> = MutableSharedFlow()
    val replies = _replies.asSharedFlow()

    override fun log(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback = ClientGattCallback()

    private inner class ClientGattCallback : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            // Return false if a required service has not been discovered.
            gatt.getService(DeviceSpecifications.UUID_SERVICE_DEVICE)?.let { service ->
                notificationCharacteristic =
                    service.getCharacteristic(DeviceSpecifications.NOTIFICATION_CHARACTERISTIC)
                indicationCharacteristic =
                    service.getCharacteristic(DeviceSpecifications.INDICATION_CHARACTERISTIC)
            }

            return notificationCharacteristic != null

        }

        override fun initialize() {
            requestMtu(512).enqueue()
        }

        override fun onServicesInvalidated() {
            notificationCharacteristic = null
            indicationCharacteristic = null
        }

    }

    suspend fun testIndicationsWithCallback() = suspendCoroutine { continuation ->
        setIndicationCallback(indicationCharacteristic)
            .with { _, data -> continuation.resume(data) }
        enableIndications(indicationCharacteristic).enqueue()
    }

    suspend fun testNotificationsWithCallback() = suspendCoroutine { continuation ->
        setNotificationCallback(notificationCharacteristic)
            .with { _, data -> continuation.resume(data) }
        enableNotifications(notificationCharacteristic).enqueue()
    }

    suspend fun connect() {
        try {
            connect(device)
                .retry(4, 300)
                .useAutoConnect(false)
                .timeout(100_000)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        cancelQueue()
        disconnect().enqueue()
    }
}