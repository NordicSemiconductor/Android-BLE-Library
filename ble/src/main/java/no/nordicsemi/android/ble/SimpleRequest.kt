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
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException

/**
 * A request that requires a [callback][android.bluetooth.BluetoothGattCallback] or can't
 * have timeout for any other reason. This class defines the [.await] method.
 */
open class SimpleRequest : Request {

    internal constructor(type: Request.Type) : super(type)

    internal constructor(
        type: Request.Type,
        characteristic: BluetoothGattCharacteristic?
    ) : super(type, characteristic)

    internal constructor(
        type: Request.Type,
        descriptor: BluetoothGattDescriptor?
    ) : super(type, descriptor)

    /**
     * Synchronously waits until the request is done.
     *
     *
     * Callbacks set using [.done] and [.fail]
     * will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [BluetoothGatt.GATT_SUCCESS].
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     */
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun await() {
        Request.assertNotMainThread()

        val sc = successCallback
        val fc = failCallback
        try {
            syncLock.close()
            val callback = RequestCallback()
            done(callback).fail(callback).invalid(callback).enqueue()

            syncLock.block()
            if (!callback.isSuccess) {
                if (callback.status == FailCallback.REASON_DEVICE_DISCONNECTED) {
                    throw DeviceDisconnectedException()
                }
                if (callback.status == FailCallback.REASON_BLUETOOTH_DISABLED) {
                    throw BluetoothDisabledException()
                }
                if (callback.status == Request.REASON_REQUEST_INVALID) {
                    throw InvalidRequestException(this)
                }
                throw RequestFailedException(this, callback.status)
            }
        } finally {
            successCallback = sc
            failCallback = fc
        }
    }
}
