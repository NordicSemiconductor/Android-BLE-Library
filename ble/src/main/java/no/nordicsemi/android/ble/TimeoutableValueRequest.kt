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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException

/**
 * A value request that requires a [callback][android.bluetooth.BluetoothGattCallback] or
 * can't have timeout for any other reason. This class defines the [.await] methods.
 */
abstract class TimeoutableValueRequest<T> : TimeoutableRequest {
    internal var valueCallback: T? = null

    internal constructor(type: Request.Type) : super(type)

    internal constructor(
        type: Request.Type,
        characteristic: BluetoothGattCharacteristic?
    ) : super(type, characteristic)

    internal constructor(
        type: Request.Type,
        descriptor: BluetoothGattDescriptor?
    ) : super(type, descriptor)

    override fun timeout(@IntRange(from = 0) timeout: Long): TimeoutableValueRequest<T> {
        super.timeout(timeout)
        return this
    }

    /**
     * Sets the value callback. When the request is invoked synchronously, this callback will
     * be ignored and the received value will be returned by the `await(...)` method;
     *
     * @param callback the callback.
     * @return The request.
     */
    open fun with(callback: T): TimeoutableValueRequest<T> {
        this.valueCallback = callback
        return this
    }

    /**
     * Synchronously waits until the request is done.
     *
     *
     * When the timeout, set with [.timeout] occurs, the [InterruptedException]
     * will be thrown.
     *
     *
     * Callbacks set using [.done] and [.fail] and
     * [.with] will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @param response the response object.
     * @param <E>      a response class.
     * @return The response with a response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [android.bluetooth.BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread, or when the trigger was already enqueued.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
    </E> */
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    open fun <E : T> await(response: E): E {
        Request.assertNotMainThread()

        val vc = valueCallback
        try {
            with(response).await()
            return response
        } finally {
            valueCallback = vc
        }
    }

    /**
     * Synchronously waits until the request is done.
     *
     *
     * Callbacks set using [.done] and [.fail] and
     * [.with] will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @param responseClass the response class. This class will be instantiate, therefore it has
     * to have a default constructor.
     * @return The response with a response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [android.bluetooth.BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @see .await
     */
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    fun <E : T> await(responseClass: Class<E>): E {
        Request.assertNotMainThread()

        try {
            val response = responseClass.newInstance()
            return await(response)
        } catch (e: IllegalAccessException) {
            throw IllegalArgumentException(
                "Couldn't instantiate "
                        + responseClass.canonicalName
                        + " class. Is the default constructor accessible?"
            )
        } catch (e: InstantiationException) {
            throw IllegalArgumentException(
                "Couldn't instantiate "
                        + responseClass.canonicalName
                        + " class. Does it have a default constructor with no arguments?"
            )
        }

    }

    /**
     * Synchronously waits until the request is done, for at most given number of milliseconds
     * after which the [InterruptedException] will be thrown.
     *
     *
     * Callbacks set using [.done], [.fail] and
     * [.with] will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @param responseClass the response class. This class will be instantiate, therefore it has
     * to have a default constructor.
     * @param timeout       optional timeout in milliseconds. This value will override one set
     * in [.timeout].
     * @param <E>           a response class that extends [T].
     * @return The object with a response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [android.bluetooth.BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
    </E> */
    @Deprecated("Use {@link #timeout(long)} and {@link #await(Class)} instead.")
    @Throws(
        RequestFailedException::class,
        InterruptedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : T> await(
        responseClass: Class<E>,
        @IntRange(from = 0) timeout: Long
    ): E {
        return timeout(timeout).await(responseClass)
    }

    /**
     * Synchronously waits until the request is done, for at most given number of milliseconds
     * after which the [InterruptedException] will be thrown.
     *
     *
     * Callbacks set using [.done], [.fail] and
     * [.with] will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @param response the response object.
     * @param timeout  optional timeout in milliseconds.
     * @param <E>      a response class that extends [T].
     * @return The object with a response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [android.bluetooth.BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
    </E> */
    @Deprecated("Use {@link #timeout(long)} and {@link #await(E)} instead.")
    @Throws(
        RequestFailedException::class,
        InterruptedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : T> await(
        response: E,
        @IntRange(from = 0) timeout: Long
    ): E {
        return timeout(timeout).await(response)
    }
}
