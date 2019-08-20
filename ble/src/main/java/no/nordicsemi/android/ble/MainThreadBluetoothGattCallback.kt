/*
 * Copyright (c) 2018, Nordic Semiconductor
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

package no.nordicsemi.android.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import android.os.Handler

import androidx.annotation.IntRange
import androidx.annotation.Keep
import androidx.annotation.RequiresApi

import no.nordicsemi.android.ble.annotation.ConnectionState
import no.nordicsemi.android.ble.annotation.PhyValue

/**
 * This class ensures that the BLE callbacks will be called on the main (UI) thread.
 * Handler parameter was added to [ #connectGatt(Context, boolean, BluetoothGattCallback, int, int, Handler)][android.bluetooth.BluetoothDevice]
 * in Android Oreo, before that the behavior was undefined.
 */
abstract class MainThreadBluetoothGattCallback : BluetoothGattCallback() {
    private var mHandler: Handler? = Handler()

    fun setHandler(handler: Handler) {
        mHandler = handler
    }

    private fun runOnUiThread(runnable: () -> Unit) {
        mHandler!!.post(runnable)
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//
//        } else {
//            runnable.run()
//        }
    }

    internal abstract fun onConnectionStateChangeSafe(
        gatt: BluetoothGatt, status: Int,
        newState: Int
    )

    internal abstract fun onServicesDiscoveredSafe(gatt: BluetoothGatt, status: Int)

    internal abstract fun onCharacteristicReadSafe(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray?,
        status: Int
    )

    internal abstract fun onCharacteristicWriteSafe(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray?,
        status: Int
    )

    internal abstract fun onCharacteristicChangedSafe(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray?
    )

    internal abstract fun onDescriptorReadSafe(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        data: ByteArray?,
        status: Int
    )

    internal abstract fun onDescriptorWriteSafe(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        data: ByteArray?,
        status: Int
    )

    internal abstract fun onReadRemoteRssiSafe(
        gatt: BluetoothGatt,
        @IntRange(from = -128, to = 20) rssi: Int,
        status: Int
    )

    internal abstract fun onReliableWriteCompletedSafe(gatt: BluetoothGatt, status: Int)

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal abstract fun onMtuChangedSafe(
        gatt: BluetoothGatt,
        @IntRange(from = 23, to = 517) mtu: Int, status: Int
    )

    @RequiresApi(api = Build.VERSION_CODES.O)
    internal abstract fun onPhyReadSafe(
        gatt: BluetoothGatt,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int, status: Int
    )

    @RequiresApi(api = Build.VERSION_CODES.O)
    internal abstract fun onPhyUpdateSafe(
        gatt: BluetoothGatt,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int, status: Int
    )

    @RequiresApi(api = Build.VERSION_CODES.O)
    internal abstract fun onConnectionUpdatedSafe(
        gatt: BluetoothGatt,
        @IntRange(from = 6, to = 3200) interval: Int,
        @IntRange(from = 0, to = 499) latency: Int,
        @IntRange(from = 10, to = 3200) timeout: Int,
        status: Int
    )

    override fun onConnectionStateChange(
        gatt: BluetoothGatt, status: Int,
        @ConnectionState newState: Int
    ) {
        runOnUiThread {
            onConnectionStateChangeSafe(gatt, status, newState)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        runOnUiThread { onServicesDiscoveredSafe(gatt, status) }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val data = characteristic.value
        runOnUiThread { onCharacteristicReadSafe(gatt, characteristic, data, status) }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val data = characteristic.value
        runOnUiThread { onCharacteristicWriteSafe(gatt, characteristic, data, status) }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val data = characteristic.value
        runOnUiThread { onCharacteristicChangedSafe(gatt, characteristic, data) }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        val data = descriptor.value
        runOnUiThread { onDescriptorReadSafe(gatt, descriptor, data, status) }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        val data = descriptor.value
        runOnUiThread { onDescriptorWriteSafe(gatt, descriptor, data, status) }
    }

    override fun onReadRemoteRssi(
        gatt: BluetoothGatt,
        @IntRange(from = -128, to = 20) rssi: Int,
        status: Int
    ) {
        runOnUiThread { onReadRemoteRssiSafe(gatt, rssi, status) }
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        runOnUiThread { onReliableWriteCompletedSafe(gatt, status) }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onMtuChanged(
        gatt: BluetoothGatt,
        @IntRange(from = 23, to = 517) mtu: Int, status: Int
    ) {
        runOnUiThread { onMtuChangedSafe(gatt, mtu, status) }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onPhyRead(
        gatt: BluetoothGatt,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int,
        status: Int
    ) {
        runOnUiThread { onPhyReadSafe(gatt, txPhy, rxPhy, status) }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onPhyUpdate(
        gatt: BluetoothGatt,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int,
        status: Int
    ) {
        runOnUiThread { onPhyUpdateSafe(gatt, txPhy, rxPhy, status) }
    }

    // This method is hidden in Android Oreo and Pie
    // @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Keep
    fun onConnectionUpdated(
        gatt: BluetoothGatt,
        @IntRange(from = 6, to = 3200) interval: Int,
        @IntRange(from = 0, to = 499) latency: Int,
        @IntRange(from = 10, to = 3200) timeout: Int,
        status: Int
    ) {
        runOnUiThread { onConnectionUpdatedSafe(gatt, interval, latency, timeout, status) }
    }
}
