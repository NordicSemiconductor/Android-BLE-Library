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

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import no.nordicsemi.android.ble.annotation.ConnectionPriority
import no.nordicsemi.android.ble.callback.*
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException

class ConnectionPriorityRequest internal constructor(type: Request.Type, @ConnectionPriority priority: Int) :
    SimpleValueRequest<ConnectionPriorityCallback>(type), Operation {

    @get:ConnectionPriority
    internal val requiredPriority: Int

    init {
        var priority = priority
        if (priority < 0 || priority > 2)
            priority = CONNECTION_PRIORITY_BALANCED
        this.requiredPriority = priority
    }

    override fun setManager(manager: BleManager<*>): ConnectionPriorityRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): ConnectionPriorityRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): ConnectionPriorityRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): ConnectionPriorityRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): ConnectionPriorityRequest {
        super.before(callback)
        return this
    }

    @RequiresApi(value = Build.VERSION_CODES.O)
    override fun with(callback: ConnectionPriorityCallback): ConnectionPriorityRequest {
        // The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
        super.with(callback)
        return this
    }

    @RequiresApi(value = Build.VERSION_CODES.O)
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    override fun <E : ConnectionPriorityCallback> await(responseClass: Class<E>): E {
        // The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
        return super.await(responseClass)
    }

    @RequiresApi(value = Build.VERSION_CODES.O)
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    override fun <E : ConnectionPriorityCallback> await(response: E): E {
        // The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
        return super.await(response)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    internal fun notifyConnectionPriorityChanged(
        device: BluetoothDevice,
        @IntRange(from = 6, to = 3200) interval: Int,
        @IntRange(from = 0, to = 499) latency: Int,
        @IntRange(from = 10, to = 3200) timeout: Int
    ) {
        valueCallback?.onConnectionUpdated(device, interval, latency, timeout)
    }

    companion object {

        /**
         * Connection parameter update - Use the connection parameters recommended by the
         * Bluetooth SIG. This is the default value if no connection parameter update
         * is requested.
         *
         *
         * Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec.
         */
        const val CONNECTION_PRIORITY_BALANCED = 0

        /**
         * Connection parameter update - Request a high priority, low latency connection.
         * An application should only request high priority connection parameters to transfer
         * large amounts of data over LE quickly. Once the transfer is complete, the application
         * should request [.CONNECTION_PRIORITY_BALANCED] connection parameters
         * to reduce energy use.
         *
         *
         * Interval: 11.25 - 15 ms (Android 6+) or 7.5 - 10 ms (Android 4.3 - 5.1),
         * latency: 0, supervision timeout: 20 sec.
         */
        const val CONNECTION_PRIORITY_HIGH = 1

        /**
         * Connection parameter update - Request low power, reduced data rate connection parameters.
         *
         *
         * Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.
         */
        const val CONNECTION_PRIORITY_LOW_POWER = 2
    }
}
