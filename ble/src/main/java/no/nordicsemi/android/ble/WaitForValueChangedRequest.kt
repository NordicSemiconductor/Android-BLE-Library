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
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.callback.*
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.data.DataFilter
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import no.nordicsemi.android.ble.exception.*

class WaitForValueChangedRequest internal constructor(
    type: Request.Type,
    characteristic: BluetoothGattCharacteristic?
) : TimeoutableValueRequest<DataReceivedCallback>(type, characteristic), Operation {

    private var progressCallback: ReadProgressCallback? = null
    private var dataMerger: DataMerger? = null
    private var buffer: DataStream? = null
    private var filter: DataFilter? = null
    internal var trigger: Request? = null
        private set
    private val deviceDisconnected: Boolean = false
    private val bluetoothDisabled: Boolean = false
    private var triggerStatus = BluetoothGatt.GATT_SUCCESS
    private var count = 0

    internal val isTriggerPending: Boolean
        get() = triggerStatus == NOT_STARTED

    internal val isTriggerCompleteOrNull: Boolean
        get() = triggerStatus != STARTED

    override fun setManager(manager: BleManager<*>): WaitForValueChangedRequest {
        super.setManager(manager)
        return this
    }

    override fun timeout(@IntRange(from = 0) timeout: Long): WaitForValueChangedRequest {
        super.timeout(timeout)
        return this
    }

    override fun done(callback: SuccessCallback): WaitForValueChangedRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): WaitForValueChangedRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): WaitForValueChangedRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): WaitForValueChangedRequest {
        super.before(callback)
        return this
    }

    override fun with(callback: DataReceivedCallback): WaitForValueChangedRequest {
        super.with(callback)
        return this
    }

    /**
     * Sets a filter which allows to skip some incoming data.
     *
     * @param filter the data filter.
     * @return The request.
     */
    fun filter(filter: DataFilter): WaitForValueChangedRequest {
        this.filter = filter
        return this
    }

    /**
     * Adds a merger that will be used to merge multiple packets into a single Data.
     * The merger may modify each packet if necessary.
     *
     * @return The request.
     */
    fun merge(merger: DataMerger): WaitForValueChangedRequest {
        this.dataMerger = merger
        this.progressCallback = null
        return this
    }

    /**
     * Adds a merger that will be used to merge multiple packets into a single Data.
     * The merger may modify each packet if necessary.
     *
     * @return The request.
     */
    fun merge(
        merger: DataMerger,
        callback: ReadProgressCallback
    ): WaitForValueChangedRequest {
        this.dataMerger = merger
        this.progressCallback = callback
        return this
    }

    /**
     * Sets an optional request that is suppose to trigger the notification or indication.
     * This is to ensure that the characteristic value won't change before the callback was set.
     *
     * @param trigger the operation that triggers the notification, usually a write characteristic
     * request that write some OP CODE.
     * @return The request.
     */
    fun trigger(trigger: Operation): WaitForValueChangedRequest {
        if (trigger is Request) {
            this.trigger = trigger
            this.triggerStatus = NOT_STARTED
            // The trigger will never receive invalid request event.
            // If the BluetoothDevice wasn't set, the whole WaitForValueChangedRequest would be invalid.
            /*this.trigger.invalid(() -> {
				// never called
			});*/
            this.trigger!!.internalBefore(object : BeforeCallback {
                override fun onRequestStarted(device: BluetoothDevice) {
                    triggerStatus = STARTED
                }
            })
            this.trigger!!.internalSuccess(object : SuccessCallback {
                override fun onRequestCompleted(device: BluetoothDevice) {
                    triggerStatus = BluetoothGatt.GATT_SUCCESS
                }
            })
            this.trigger!!.internalFail(object : FailCallback {
                override fun onRequestFailed(device: BluetoothDevice, status: Int) {
                    triggerStatus = status
                    syncLock.open()
                    notifyFail(device, status)
                }
            })
        }
        return this
    }

    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    override fun <E : DataReceivedCallback> await(response: E): E {
        Request.assertNotMainThread()

        try {
            // Ensure the trigger request it enqueued after the callback has been set.
            if (trigger != null && trigger!!.enqueued) {
                throw IllegalStateException("Trigger request already enqueued")
            }
            super.await(response)
            return response
        } catch (e: RequestFailedException) {
            if (triggerStatus != BluetoothGatt.GATT_SUCCESS) {
                // Trigger will never have invalid request status. The outer request will.
                /*if (triggerStatus == RequestCallback.REASON_REQUEST_INVALID) {
					throw new InvalidRequestException(trigger);
				}*/
                throw trigger?.let { RequestFailedException(it, triggerStatus) }!!
            }
            throw e
        }

    }

    /**
     * Similar to [.await], but if the response class extends
     * [ProfileReadResponse] and the received response is invalid, an exception is thrown.
     * This allows to keep all error handling in one place.
     *
     * @param response the result object.
     * @param <E>      a response class that extends [ProfileReadResponse].
     * @return Object with a valid response.
     * @throws IllegalStateException       thrown when you try to call this method from
     * the main (UI) thread.
     * @throws InterruptedException        thrown when the timeout occurred before the
     * characteristic value has changed.
     * @throws RequestFailedException      thrown when the trigger request has failed.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the
     * notification or indication was received.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
    </E> */
    @Throws(
        RequestFailedException::class,
        InvalidDataException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    fun <E : ProfileReadResponse> awaitValid(response: E): E {
        val result = await(response)
        if (result != null && !result.isValid) {
            throw InvalidDataException(result)
        }
        return result
    }

    /**
     * Similar to [.await], but if the response class extends
     * [ProfileReadResponse] and the received response is invalid, an exception is thrown.
     * This allows to keep all error handling in one place.
     *
     * @param responseClass the result class. This class will be instantiate, therefore it
     * has to have a default constructor.
     * @param <E>           a response class that extends [ProfileReadResponse].
     * @return Object with a valid response.
     * @throws IllegalStateException       thrown when you try to call this method from
     * the main (UI) thread.
     * @throws InterruptedException        thrown when the timeout occurred before the
     * characteristic value has changed.
     * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
     * @throws RequestFailedException      thrown when the trigger request has failed.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the
     * notification or indication was received.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
    </E> */
    @Throws(
        RequestFailedException::class,
        InvalidDataException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    fun <E : ProfileReadResponse> awaitValid(responseClass: Class<E>): E {
        val response = await(responseClass)
        if (response != null && !response.isValid) {
            throw InvalidDataException(response)
        }
        return response
    }

    /**
     * Same as [.await], but if received response is not valid, this method will
     * thrown an exception.
     *
     * @param responseClass the result class. This class will be instantiate, therefore it
     * has to have a default constructor.
     * @param timeout       optional timeout in milliseconds.
     * @param <E>           a response class that extends [ProfileReadResponse].
     * @return Object with a valid response.
     * @throws IllegalStateException       thrown when you try to call this method from
     * the main (UI) thread.
     * @throws InterruptedException        thrown when the timeout occurred before the
     * characteristic value has changed.
     * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
     * @throws RequestFailedException      thrown when the trigger request has failed.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the
     * notification or indication was received.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
    </E> */
    @Deprecated("Use {@link #timeout(long)} and {@link #awaitValid(Class)} instead.")
    @Throws(
        InterruptedException::class,
        InvalidDataException::class,
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : ProfileReadResponse> awaitValid(
        responseClass: Class<E>,
        @IntRange(from = 0) timeout: Long
    ): E {
        return timeout(timeout).awaitValid(responseClass)
    }

    /**
     * Same as [.await], but if received response is not valid,
     * this method will thrown an exception.
     *
     * @param response the result object.
     * @param timeout  optional timeout in milliseconds.
     * @param <E>      a response class that extends [ProfileReadResponse].
     * @return Object with a valid response.
     * @throws IllegalStateException       thrown when you try to call this method from
     * the main (UI) thread.
     * @throws InterruptedException        thrown when the timeout occurred before the
     * characteristic value has changed.
     * @throws RequestFailedException      thrown when the trigger request has failed.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the
     * notification or indication was received.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
    </E> */
    @Deprecated("Use {@link #timeout(long)} and {@link #awaitValid(E)} instead.")
    @Throws(
        InterruptedException::class,
        InvalidDataException::class,
        DeviceDisconnectedException::class,
        RequestFailedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : ProfileReadResponse> awaitValid(
        response: E,
        @IntRange(from = 0) timeout: Long
    ): E {
        return timeout(timeout).awaitValid(response)
    }

    internal fun matches(packet: ByteArray): Boolean {
        return filter == null || filter!!.filter(packet)
    }

    internal fun notifyValueChanged(device: BluetoothDevice, value: ByteArray) {
        // Keep a reference to the value callback, as it may change during execution
        val valueCallback = this.valueCallback

        // With no value callback there is no need for any merging

        if (dataMerger == null) {
            if (valueCallback != null) {
                valueCallback.onDataReceived(device, Data(value))
            }
        } else {
            if (progressCallback != null)
                progressCallback!!.onPacketReceived(device, value, count)
            if (buffer == null)
                buffer = DataStream()
            if (dataMerger!!.merge(buffer!!, value, count++)) {
                if (valueCallback != null) {
                    valueCallback.onDataReceived(device, buffer!!.toData())
                }
                buffer = null
                count = 0
            } // else
            // wait for more packets to be merged
        }
    }

    internal fun hasMore(): Boolean {
        return count > 0
    }

    companion object {
        internal const val NOT_STARTED = -123456
        internal const val STARTED = NOT_STARTED + 1
    }
}
