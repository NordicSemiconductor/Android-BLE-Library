/*
 * Copyright (c) 2020, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.ble.ble_gatt_server

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleServerManager
import no.nordicsemi.android.ble.observer.ServerObserver
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Advertises a Bluetooth LE GATT service and takes care of its requests. The service
 * runs as a foreground service, which is generally required so that it can run even
 * while the containing app has no UI. It is also possible to have the service
 * started up as part of the OS boot sequence using code similar to the following:
 *
 * <pre>
 *     class OsNotificationReceiver : BroadcastReceiver() {
 *          override fun onReceive(context: Context?, intent: Intent?) {
 *              when (intent?.action) {
 *                  // Start our Gatt service as a result of the system booting up
 *                  Intent.ACTION_BOOT_COMPLETED -> {
 *                     context?.startForegroundService(Intent(context, GattService::class.java))
 *                  }
 *              }
 *          }
 *      }
 * </pre>
 */
class GattService : Service() {
    companion object {
        private const val TAG = "gatt-service"
    }

    private var serverManager: ServerManager? = null

    private lateinit var bluetoothObserver: BroadcastReceiver

    private var bleAdvertiseCallback: BleAdvertiser.Callback? = null

    override fun onCreate() {
        super.onCreate()

        // Setup as a foreground service

        val notificationChannel = NotificationChannel(
                GattService::class.java.simpleName,
                resources.getString(R.string.gatt_service_name),
                NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationService =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationService.createNotificationChannel(notificationChannel)

        val notification = NotificationCompat.Builder(this, GattService::class.java.simpleName)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.gatt_service_name))
                .setContentText(resources.getString(R.string.gatt_service_running_notification))
                .setAutoCancel(true)

        startForeground(1, notification.build())

        // Observe OS state changes in BLE

        bluetoothObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                                BluetoothAdapter.EXTRA_STATE,
                                -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_ON -> enableBleServices()
                            BluetoothAdapter.STATE_OFF -> disableBleServices()
                        }
                    }
                }
            }
        }
        ContextCompat.registerReceiver(this,
            bluetoothObserver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED)

        // Startup BLE if we have it

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) enableBleServices()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableBleServices()
    }

    override fun onBind(intent: Intent?): IBinder {
        return DataPlane()
    }

    private fun enableBleServices() {
        serverManager = ServerManager(this)
        serverManager!!.open()

        bleAdvertiseCallback = BleAdvertiser.Callback()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter.bluetoothLeAdvertiser?.startAdvertising(
                BleAdvertiser.settings(),
                BleAdvertiser.advertiseData(),
                bleAdvertiseCallback!!
        )
    }

    private fun disableBleServices() {
        bleAdvertiseCallback?.let {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter.bluetoothLeAdvertiser?.stopAdvertising(it)
            bleAdvertiseCallback = null
        }

        serverManager?.close()
        serverManager = null
    }

    /**
     * Functionality available to clients
     */
    private inner class DataPlane : Binder(), DeviceAPI {

        override fun setMyCharacteristicValue(value: String) {
            serverManager?.setMyCharacteristicValue(value)
        }

    }

    /*
     * Manages the entire GATT service, declaring the services and characteristics on offer
     */
    private class ServerManager(val context: Context) : BleServerManager(context), ServerObserver, DeviceAPI {

        companion object {
            private val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        }

        private val myGattCharacteristic = sharedCharacteristic(
            // UUID:
            MyServiceProfile.MY_CHARACTERISTIC_UUID,
            // Properties:
            BluetoothGattCharacteristic.PROPERTY_READ
                  or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            // Permissions:
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM,
            // Descriptors:
            // cccd() - this could have been used called, had no encryption been used.
            // Instead, let's define CCCD with custom permissions:
            descriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM
                       or BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM, byteArrayOf(0, 0)),
            description("A characteristic to be read", false) // descriptors
        )
        private val myGattService = service(
            // UUID:
            MyServiceProfile.MY_SERVICE_UUID,
            // Characteristics (just one in this case):
            myGattCharacteristic
        )

        private val myGattServices = Collections.singletonList(myGattService)

        private val serverConnections = mutableMapOf<String, ServerConnection>()

        override fun setMyCharacteristicValue(value: String) {
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            serverConnections.values.forEach { serverConnection ->
                serverConnection.sendNotificationForMyGattCharacteristic(bytes)
            }
        }

        override fun log(priority: Int, message: String) {
            if (BuildConfig.DEBUG || priority == Log.ERROR) {
                Log.println(priority, TAG, message)
            }
        }

        override fun initializeServer(): List<BluetoothGattService> {
            setServerObserver(this)

            return myGattServices
        }

        override fun onServerReady() {
            log(Log.INFO, "Gatt server ready")
        }

        override fun onDeviceConnectedToServer(device: BluetoothDevice) {
            log(Log.DEBUG, "Device connected ${device.address}")

            // A new device connected to the phone. Connect back to it, so it could be used
            // both as server and client. Even if client mode will not be used, currently this is
            // required for the server-only use.
            serverConnections[device.address] = ServerConnection().apply {
                useServer(this@ServerManager)
                connect(device).enqueue()
            }
        }

        override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
            log(Log.DEBUG, "Device disconnected ${device.address}")

            // The device has disconnected. Forget it and close.
            serverConnections.remove(device.address)?.close()
        }

        /*
         * Manages the state of an individual server connection (there can be many of these)
         */
        inner class ServerConnection : BleManager(context) {

            fun sendNotificationForMyGattCharacteristic(value: ByteArray) {
                sendNotification(myGattCharacteristic, value).enqueue()
            }

            override fun log(priority: Int, message: String) {
                this@ServerManager.log(priority, message)
            }

            // There are no services that we need from the connecting device, but
            // if there were, we could specify them here.
            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                return true
            }

            override fun onServicesInvalidated() {
                // This is the place to nullify characteristics obtained above.
            }
        }
    }

    object MyServiceProfile {
        val MY_SERVICE_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
        val MY_CHARACTERISTIC_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
    }
}
